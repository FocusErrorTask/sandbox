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

package at.tugraz.ist.akm.monitoring;

import java.io.Closeable;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import at.tugraz.ist.akm.trace.LogClient;

public class SystemMonitor extends PhoneStateListener implements Closeable
{

    private Context mContext = null;
    private TelephonyManager mTel = null;
    private SignalStrength mSingalStrength = null;
    private LogClient mLog = new LogClient(this);


    public SystemMonitor(Context context)
    {
        mContext = context;
        mTel = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);

    }


    public void start()
    {
        mLog.debug("register phone state listener");
        mTel.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }


    public void stop()
    {
        mLog.debug("unregister phone state listener");
        mTel.listen(this, PhoneStateListener.LISTEN_NONE);
    }


    @Override
    public void close() throws IOException
    {
        mContext = null;
        mTel = null;
        mSingalStrength = null;
        mLog = null;
    }


    public BatteryStatus getBatteryStatus()
    {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryInfos = mContext.registerReceiver(null, filter);
        return new BatteryStatus(mContext, batteryInfos);
    }


    public synchronized SignalStrength getSignalStrength()
    {
        if (null != mSingalStrength)
        {
            mLog.debug("passig SignalStrength forward");
        } else
        {
            mLog.warning("no SignalStrength available, passing NULL forward");
        }
        return mSingalStrength;
    }


    public synchronized TelephonySignalStrength getTelephonySignalStrength()
    {
        if (mSingalStrength == null)
        {
            return null;
        }

        TelephonySignalStrength telSignalStrength = new TelephonySignalStrength(
                mContext);
        telSignalStrength.takeNewSignalStrength(mSingalStrength);
        return telSignalStrength;
    }


    @Override
    public synchronized void onSignalStrengthsChanged(
            SignalStrength signalStrength)
    {
        mLog.debug("signal strength changed [" + signalStrength + "]");
        super.onSignalStrengthsChanged(signalStrength);
        mSingalStrength = signalStrength;
    }
}
