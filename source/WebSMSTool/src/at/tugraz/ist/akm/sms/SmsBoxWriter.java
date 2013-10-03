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

package at.tugraz.ist.akm.sms;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import at.tugraz.ist.akm.content.SmsContent;
import at.tugraz.ist.akm.trace.LogClient;

public class SmsBoxWriter extends LogClient {

	private ContentResolver mContentResolver = null;

	public SmsBoxWriter(ContentResolver contentResolver) {
		super(SmsBoxWriter.class.getSimpleName());
		mContentResolver = contentResolver;
	}

	/**
	 * Stores sms to SmsContent.ContentUri.OUTBOX_URI. The OUTBOX_URI contains
	 * queued sms.
	 * 
	 * @param message
	 * @return @see putTextMessageToUri
	 */
	public Uri writeOutboxTextMessage(TextMessage message) {
		return putTextMessageToUri(message, SmsContent.ContentUri.OUTBOX_URI);
	}

	/**
	 * Stores sms to SmsContent.ContentUri.SENT_URI. The SENT_URI contains sent
	 * sms.
	 * 
	 * @param message
	 * @return @see putTextMessageToUri
	 */
	public Uri writeSentboxTextMessage(TextMessage message) {
		return putTextMessageToUri(message, SmsContent.ContentUri.SENT_URI);
	}

	/**
	 * Update the (non-auto-generated) fields of a TextMessage stored in content://sms
	 * @param message Threaad-id and messagei-id must be set correctly to match the message that has to be updated
	 * @return amount of affected rows; usually 1 else 0
	 */
	public int updateTextMessage(TextMessage message) {
		return updateTextMessage(message, SmsContent.ContentUri.BASE_URI);
	}

	/**
	 * stores a text message to Uri
	 * 
	 * @param message
	 * @param destination
	 *            to content://sms/*
	 * @return the Uri pointing to the newly inserted text message
	 */
	private Uri putTextMessageToUri(TextMessage message, Uri destination) {
		return mContentResolver.insert(destination,
				textMessageToValues(message));
	}

	/**
	 * @param message
	 *            The text message to be updated. The update query depends on
	 *            message id AND thread-id.
	 * @param destination
	 *            content://sms/*
	 * @return number of affected rows; normally 0 or 1
	 */
	private int updateTextMessage(TextMessage message, Uri destination) {
		StringBuffer where = new StringBuffer();
		List<String> likeArgs = new ArrayList<String>();

		where.append(SmsContent.Content.ID + " = ? AND ");
		where.append(SmsContent.Content.THREAD_ID + " = ? ");

		likeArgs.add(message.getId());
		likeArgs.add(message.getThreadId());

		String[] like = new String[likeArgs.size()];
		like = likeArgs.toArray(like);
		int rows = mContentResolver.update(destination,
				textMessageToValues(message), where.toString(), like);

		logVerbose("Updated [" + rows + "] rows on [" + destination.toString() + "]");
		return rows;
	}

	/**
	 * Puts the fields from TextMessage to ContentValues, where the value keys
	 * are the same as the column names of content://sms/*
	 * 
	 * @param message
	 * @return
	 */
	private ContentValues textMessageToValues(TextMessage message) {
		ContentValues values = new ContentValues();
		values.put(SmsContent.Content.ADDRESS, message.getAddress());
		values.put(SmsContent.Content.BODY, message.getBody());
		values.put(SmsContent.Content.DATE, message.getDate());
		values.put(SmsContent.Content.ERROR_CODE, message.getErrorCode());
		values.put(SmsContent.Content.LOCKED, message.getLocked());
		values.put(SmsContent.Content.SUBJECT, message.getSubject());
		values.put(SmsContent.Content.PERSON, message.getPerson());
		values.put(SmsContent.Content.PROTOCOL, message.getProtocol());
		values.put(SmsContent.Content.READ, message.getRead());
		values.put(SmsContent.Content.REPLY_PATH_PRESENT,
				message.getReplyPathPresent());
		values.put(SmsContent.Content.SEEN, message.getSeen());
		values.put(SmsContent.Content.SERVICE_CENTER,
				message.getServiceCenter());
		values.put(SmsContent.Content.STATUS, message.getStatus());
		// the following ones will be auto generated
		// values.put(SmsContent.Content.ID, message.getId());
		// values.put(SmsContent.Content.THREAD_ID, message.getThreadId());
		// values.put(SmsContent.Content.TYPE, message.getType());
		return values;
	}
}
