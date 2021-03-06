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

package at.tugraz.ist.akm.test.content.query;

import at.tugraz.ist.akm.content.SmsContentConstants;
import at.tugraz.ist.akm.content.query.ContentProviderQueryParameters;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.content.query.TextMessageQueryBuilder;
import at.tugraz.ist.akm.test.base.WebSMSToolInstrumentationTestcase;

public class TextMessageQueryBuilderTest extends
        WebSMSToolInstrumentationTestcase
{

    public TextMessageQueryBuilderTest()
    {
        super(TextMessageQueryBuilderTest.class.getSimpleName());
    }


    public void testSetAddress()
    {
        TextMessageFilter filter = new TextMessageFilter();
        filter.setBox(SmsContentConstants.Uri.INBOX_URI);
        filter.setAddress("01906666");
        TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
        ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

        assertTrue(queryParameters.uri
                .equals(SmsContentConstants.Uri.INBOX_URI));
        assertTrue(queryParameters.where
                .compareTo(SmsContentConstants.Column.ADDRESS + " = ? ") == 0);
        String[] queryArgs = queryParameters.like;
        assertTrue(queryArgs[0].compareTo("01906666") == 0);
    }


    public void testSetId()
    {
        TextMessageFilter filter = new TextMessageFilter();
        filter.setBox(SmsContentConstants.Uri.BASE_URI);
        filter.setId(1234);
        TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
        ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

        assertTrue(queryParameters.uri.equals(SmsContentConstants.Uri.BASE_URI));
        assertTrue(queryParameters.where
                .compareTo(SmsContentConstants.Column.ID + " = ? ") == 0);
        String[] queryArgs = queryParameters.like;
        assertTrue(queryArgs[0].compareTo("1234") == 0);
    }


    public void testSetPerson()
    {
        TextMessageFilter filter = new TextMessageFilter();
        filter.setBox(SmsContentConstants.Uri.DRAFT_URI);
        filter.setPerson("foo bar");
        TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
        ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

        assertTrue(queryParameters.uri
                .equals(SmsContentConstants.Uri.DRAFT_URI));
        assertTrue(queryParameters.where
                .compareTo(SmsContentConstants.Column.PERSON + " = ? ") == 0);
        String[] queryArgs = queryParameters.like;
        assertTrue(queryArgs[0].compareTo("foo bar") == 0);
    }


    public void testSetRead()
    {
        TextMessageFilter filter = new TextMessageFilter();
        filter.setBox(SmsContentConstants.Uri.FAILED_URI);
        filter.setRead(true);
        TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
        ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

        assertTrue(queryParameters.uri
                .equals(SmsContentConstants.Uri.FAILED_URI));
        assertTrue(queryParameters.where
                .compareTo(SmsContentConstants.Column.READ + " = ? ") == 0);
        String[] queryArgs = queryParameters.like;
        assertTrue(queryArgs[0].compareTo("1") == 0);
    }


    public void testSetSeen()
    {
        TextMessageFilter filter = new TextMessageFilter();
        filter.setBox(SmsContentConstants.Uri.QUEUED_URI);
        filter.setSeen(true);
        TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
        ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

        assertTrue(queryParameters.uri
                .equals(SmsContentConstants.Uri.QUEUED_URI));
        assertTrue(queryParameters.where
                .compareTo(SmsContentConstants.Column.SEEN + " = ? ") == 0);
        String[] queryArgs = queryParameters.like;
        assertTrue(queryArgs[0].compareTo("1") == 0);
    }


    public void testSetThreadId()
    {
        TextMessageFilter filter = new TextMessageFilter();
        filter.setBox(SmsContentConstants.Uri.UNDELIVERED_URI);
        filter.setThreadId(42L);
        TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
        ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

        assertTrue(queryParameters.uri
                .equals(SmsContentConstants.Uri.UNDELIVERED_URI));
        assertTrue(queryParameters.where
                .compareTo(SmsContentConstants.Column.THREAD_ID + " = ? ") == 0);
        String[] queryArgs = queryParameters.like;
        assertTrue(queryArgs[0].compareTo("42") == 0);
    }


    public void testSetMultipleQueries()
    {
        TextMessageFilter filter = new TextMessageFilter();
        filter.setBox(SmsContentConstants.Uri.UNDELIVERED_URI);
        filter.setAddress("01906666");
        filter.setBox(SmsContentConstants.Uri.SENT_URI);
        filter.setId(10987L);
        filter.setThreadId(42L);
        filter.setPerson("dr. akula");
        filter.setSeen(false);
        filter.setRead(false);
        TextMessageQueryBuilder builder = new TextMessageQueryBuilder(filter);
        ContentProviderQueryParameters queryParameters = builder.getQueryArgs();

        assertTrue(queryParameters.uri.equals(SmsContentConstants.Uri.SENT_URI));
        logVerbose(queryParameters.where + "|"
                + SmsContentConstants.Column.ADDRESS + " = ?  AND "
                + SmsContentConstants.Column.THREAD_ID + " = ?  AND "
                + SmsContentConstants.Column.ID + " = ?  AND "
                + SmsContentConstants.Column.PERSON + " = ?  AND "
                + SmsContentConstants.Column.SEEN + " = ?  AND "
                + SmsContentConstants.Column.READ + " = ? ");
        assertTrue(queryParameters.where
                .compareTo(SmsContentConstants.Column.ADDRESS + " = ?  AND "
                        + SmsContentConstants.Column.THREAD_ID + " = ?  AND "
                        + SmsContentConstants.Column.ID + " = ?  AND "
                        + SmsContentConstants.Column.PERSON + " = ?  AND "
                        + SmsContentConstants.Column.SEEN + " = ?  AND "
                        + SmsContentConstants.Column.READ + " = ? ") == 0);
        String[] queryArgs = queryParameters.like;
        assertTrue(queryArgs[0].compareTo("01906666") == 0);
        assertTrue(queryArgs[1].compareTo("42") == 0);
        assertTrue(queryArgs[2].compareTo("10987") == 0);
        assertTrue(queryArgs[3].compareTo("dr. akula") == 0);
        assertTrue(queryArgs[4].compareTo("0") == 0);
        assertTrue(queryArgs[5].compareTo("0") == 0);

    }

}
