/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.phonebook.PhonebookCache;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.CacheModifiedHandler;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.phonebook.contact.IContactModifiedCallback;
import at.tugraz.ist.akm.phonebook.contact.IContactReader;
import at.tugraz.ist.akm.trace.LogClient;

public class CachedAsyncPhonebookReader extends Thread implements
        IContactModifiedCallback, IContactReader
{

    private static class ThreadInfo
    {
        public int breathingPauseMs = 750;
    }

    private static class ContactSources
    {
        public List<Contact> noContacts = new Vector<Contact>(0);
        public List<Contact> cached = noContacts;
        public List<Contact> contentProvider = noContacts;
    }

    private static class TimingInfo
    {
        public long readDBDurationMs = 0;
        public long readContentProvicerDurationMs = 0;
    }

    private static final long CACHE_READY_MESSAGE_DELAY_MS = 5 * 1000;

    private LogClient mLog = new LogClient(
            CachedAsyncPhonebookReader.class.getCanonicalName(), true);
    private ContactSources mContactSources = new ContactSources();
    private ContactFilter mContactFilter = null;
    private Context mApplicationContext = null;
    private PhonebookCacheDB mPhonebookCacheDB = null;
    private IContactReader mContentproviderContactReader = null;
    private ThreadInfo mThreadInfo = new ThreadInfo();
    private TimingInfo mTimingInfo = new TimingInfo();
    protected Object mWaitMonitor = new Object();
    private CacheModifiedHandler mCacheModifiedHandler = null;
    protected CacheStateMachine mStateMachine = new CacheStateMachine();
    protected CacheStateMachine mBufferedState = new CacheStateMachine();


    public CachedAsyncPhonebookReader(ContactFilter filter,
            Context applicationContext, IContactReader contactReader)
    {
        this.setName(CachedAsyncPhonebookReader.class.getSimpleName());
        mApplicationContext = applicationContext;
        mPhonebookCacheDB = new PhonebookCacheDB(mApplicationContext);
        mContactFilter = filter;
        mContentproviderContactReader = contactReader;
        mStateMachine.state(CacheStates.ALIVE);
    }


    public void registerCacheModifiedHandler(CacheModifiedHandler handler)
    {
        mCacheModifiedHandler = handler;
    }


    public void unregisterCacheModifiedHandler()
    {
        mCacheModifiedHandler = null;
    }


    @Override
    public List<Contact> fetchContacts(ContactFilter filter)
    {
        mLog.debug("contact filter not considered yet");
        synchronized (mContactSources)
        {
            switch (mStateMachine.state())
            {
            case ALIVE:
            case STARTED:
            case READ_DB:
                mLog.debug("requested contacts from uncomplete cache. returning [0] entries");
                return extract(mContactSources.noContacts, filter);

            case READ_DB_DONE:
            case READ_CONTENTPROVIDER:
                mLog.debug("requested contacts from cache: ["
                        + mContactSources.cached.size() + "] entries");
                return extract(mContactSources.cached, filter);

            case READ_CONTENTPROVIDER_DONE:
            case READY_FOR_CHANGES:
            case STOP:
            case STOPPED:
                mLog.debug("requested contacts from content provider: "
                        + mContactSources.contentProvider.size() + " entries");
                return extract(mContactSources.contentProvider, filter);

            default:
                return extract(mContactSources.noContacts, filter);
            }
        }
    }


    protected List<Contact> extract(List<Contact> source, ContactFilter filter)
    {
        if (filter == null)
        {
            return new Vector<Contact>(source);
        }

        Vector<Contact> filtered = new Vector<Contact>();

        for (Contact contact : source)
        {
            if (isConditionSatisfied(contact, filter))
            {
                filtered.add(contact);
            }
        }

        mLog.debug("filtered [" + source.size() + "] contacts ["
                + filtered.size() + "] satisfied");
        return filtered;
    }


    private boolean isConditionSatisfied(Contact contact, ContactFilter filter)
    {
        boolean isIdSatisfied = false, isStarredSatisfied = false, isWithFoneSatisfied = false;

        if (filter.getIsIdActive())
        {
            if (filter.getId() == contact.getId())
            {
                isIdSatisfied = true;
            }
        } else
        {
            isIdSatisfied = true;
        }

        if (filter.getIsStarredActive())
        {
            if (filter.isStarred() == contact.isStarred())
            {
                isStarredSatisfied = true;
            }
        } else
        {
            isStarredSatisfied = true;
        }

        if (filter.getIsWithPhoneActive())
        {
            if (filter.getWithPhone() == true)
            {
                if (null != contact.getPhoneNumbers())
                {
                    isWithFoneSatisfied = true;
                }
            } else if (filter.getWithPhone() == false)
            {
                if (null == contact.getPhoneNumbers())
                {
                    isWithFoneSatisfied = true;
                }
            }
        } else
        {
            isWithFoneSatisfied = true;
        }

        return (isIdSatisfied && isStarredSatisfied && isWithFoneSatisfied);
    }


    @Override
    public void run()
    {
        mLog.debug("started contact catche");
        while (mStateMachine.state() != CacheStates.STOPPED)
        {
            tick();
            synchronized (mWaitMonitor)
            {
                if (mStateMachine.state() == CacheStates.READY_FOR_CHANGES)
                {
                    try
                    {
                        mWaitMonitor.wait();
                    }
                    catch (InterruptedException e)
                    {
                        mLog.error(
                                "failed to wait, ignore interrupted exception",
                                e);
                    }
                }
            }
        }
    }


    protected synchronized void tick()
    {
        switch (mStateMachine.state())
        {
        case ALIVE:
        case STARTED:
        case READ_DB:
        case READ_DB_DONE:
        case READ_CONTENTPROVIDER:
            readContactsFromCacheAndProvider();
            break;

        case READ_CONTENTPROVIDER_DONE:
            double speedupRatio = mTimingInfo.readContentProvicerDurationMs
                    / (mTimingInfo.readDBDurationMs + 1);
            mLog.debug("cache speedup " + speedupRatio
                    + "[times] - read cached database in "
                    + mTimingInfo.readDBDurationMs
                    + " [ms] - ContentProvider in "
                    + mTimingInfo.readContentProvicerDurationMs + " [ms]");
            break;

        case READY_FOR_CHANGES:
            break;

        case STOP:
            onClose();
            break;

        default:
            mLog.warning("ignoring unacceptable state");
            waitMs(mThreadInfo.breathingPauseMs);
            break;
        }
        mStateMachine.transit();
    }


    private void waitMs(long sleepMs)
    {
        try
        {
            Thread.sleep(sleepMs);
        }
        catch (InterruptedException ie)
        {
            mLog.error("ignore interrupted exception", ie);
        }
    }


    public synchronized void finish()
    {
        synchronized (mWaitMonitor)
        {
            mStateMachine.state(CacheStates.STOP);
            mWaitMonitor.notify();
            mLog.debug("cache stopped");
        }
    }


    private void onClose()
    {
        mPhonebookCacheDB.clear();
        for (Contact c : mContactSources.contentProvider)
        {
            mPhonebookCacheDB.cache(c);
            mLog.debug("store to db [" + c + "]");
        }
        mLog.debug("cache closed, stored entries ["
                + mPhonebookCacheDB.numEntries() + "]");
        mPhonebookCacheDB.close();
    }


    private void sendcacheModified()
    {
        sendMessage(0);
    }


    private void sendDelayedCacheModified()
    {
        sendMessage(CACHE_READY_MESSAGE_DELAY_MS);
    }


    private void sendMessage(long msDelay)
    {
        if (mCacheModifiedHandler != null)
        {
            mLog.debug("sending cache modified in threadID ["
                    + Thread.currentThread().getId() + "]");
            mCacheModifiedHandler.sendMessageDelayed(
                    mCacheModifiedHandler.newCacheModifiedMessage(), msDelay);
        }
    }


    private void readContactsFromCacheAndProvider()
    {
        switch (mStateMachine.state())
        {
        case READ_DB:
            synchronized (mContactSources)
            {
                mContactSources.cached = tryReadFromDatabase();
                sendcacheModified();
            }
            break;

        case READ_CONTENTPROVIDER:
            synchronized (mContactSources)
            {
                mContactSources.contentProvider = fetchFromContentProvider();

                for (Contact c : mContactSources.contentProvider)
                {
                    mLog.debug("c [" + c + "]");

                }
                sendDelayedCacheModified();
            }
            break;
        default:
        }
    }


    private List<Contact> fetchFromContentProvider()
    {
        Date start = new Date();
        List<Contact> watchlist = mContentproviderContactReader
                .fetchContacts(mContactFilter);
        mTimingInfo.readContentProvicerDurationMs = new Date().getTime()
                - start.getTime();
        mLog.debug("found contacts [" + watchlist.size()
                + "] in content provider");
        return watchlist;
    }


    private List<Contact> tryReadFromDatabase()
    {
        Date start = new Date();
        List<Contact> watchlist = mPhonebookCacheDB.getCached(mContactFilter);
        mTimingInfo.readDBDurationMs = new Date().getTime() - start.getTime();

        mLog.debug("found contacts " + watchlist.size() + " in cache DB");
        return watchlist;
    }


    @Override
    public void contactModifiedCallback()
    {
        synchronized (this)
        {
            CacheStates currentState = mStateMachine.state();
            if (currentState == CacheStates.READ_CONTENTPROVIDER
                    || currentState == CacheStates.READ_CONTENTPROVIDER_DONE
                    || currentState == CacheStates.READY_FOR_CHANGES)
            {
                mStateMachine.state(CacheStates.READ_CONTENTPROVIDER);
                synchronized (mWaitMonitor)
                {
                    mWaitMonitor.notifyAll();
                }
            }
        }
    }

}
