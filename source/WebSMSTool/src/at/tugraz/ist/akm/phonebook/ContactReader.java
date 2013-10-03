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

package at.tugraz.ist.akm.phonebook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.ContactQueryBuilder;
import at.tugraz.ist.akm.content.query.ContentProviderQueryParameters;
import at.tugraz.ist.akm.trace.LogClient;

public class ContactReader {

	private ContentResolver mContentResolver = null;
	private LogClient mLog = new LogClient(this);

	public ContactReader(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}

	public List<Contact> fetchContacts(ContactFilter filter) {

		Cursor people = queryContacts(filter);
		List<Contact> contacts = null;

		try {
			if (people != null) {
				contacts = new Vector<Contact>(people.getCount());
				while (people.moveToNext()) {
					contacts.add(parseToContact(people));
				}
				people.close();
			}
		} catch (Exception ex) {
			mLog.logError("can not get resouce [cursor to contacts]", ex);
		}
		
		return contacts;
	}

	private Cursor queryContacts(ContactFilter filter) {
		ContactQueryBuilder qBuild = new ContactQueryBuilder(filter);
		ContentProviderQueryParameters queryParameters = qBuild.getQueryArgs();
		return mContentResolver.query(queryParameters.uri, queryParameters.as, queryParameters.where, queryParameters.like, queryParameters.sortBy);
	}

	private Contact parseToContact(Cursor person) {
		Contact contact = new Contact();

		String contactId = person.getString(person
				.getColumnIndex(ContactsContract.Contacts._ID));
		String displayName = person.getString(person
				.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		boolean starred = (1 == Integer.parseInt(person.getString(person
				.getColumnIndex(ContactsContract.Contacts.STARRED))));

		contact.setDisplayName(displayName);
		contact.setId(Integer.parseInt(contactId));
		contact.setStarred(starred);
		collectPhotoData(contact, contactId);
		collectPhoneNumberDetails(contact, contactId);
		collectStructuredNameDetails(contact, contactId);

		mLog.logDebug("parsed contact [" + contact.getDisplayName() + "] with id [" + contact.getId() + "]");
		return contact;
	}

	private void collectPhoneNumberDetails(Contact contact, String contactId) {
		String where = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
				+ " = ?";
		String[] as = { ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Phone.TYPE };
		String[] like = { contactId };
		Cursor phoneNumbers = mContentResolver.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, as, where,
				like, null);

		if (phoneNumbers != null) {
			List<Contact.Number> phoneNumberList = new ArrayList<Contact.Number>(phoneNumbers.getCount());
			while (phoneNumbers.moveToNext()) {

				String phone = phoneNumbers
						.getString(phoneNumbers
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
				if (phone == null) {
					phone = new String("0");
				}

				phoneNumberList
						.add(new Contact.Number(
								phoneNumbers
										.getString(phoneNumbers
												.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
								Integer.parseInt(phone)));
			}
			contact.setPhoneNumbers(phoneNumberList);
			phoneNumbers.close();
		}
	}

	private void collectStructuredNameDetails(Contact contact, String contactId) {

		Uri selectFrom = ContactsContract.Data.CONTENT_URI;
		String[] as = {
				ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
				ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME };
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ? ";
		String[] like = new String[] {
				contactId,
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };

		Cursor structuredName = mContentResolver.query(selectFrom, as, where,
				like, null);

		if (structuredName != null) {
			if (structuredName.moveToNext()) {
				String givenName = structuredName
						.getString(structuredName
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
				String familyName = structuredName
						.getString(structuredName
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
				contact.setName(givenName);
				contact.setFamilyName(familyName);
			}
			structuredName.close();
		}

	}

	private void collectPhotoData(Contact contact, String contactId) {
		Uri select = ContactsContract.Data.CONTENT_URI;
		String[] as = { ContactsContract.Data.CONTACT_ID };
		String where = ContactsContract.Data.CONTACT_ID + "= ? " + " AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] like = { contactId,
				ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE };
		Cursor cur = mContentResolver.query(select, as, where, like, null);

		Uri person = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI,
				Long.parseLong(contactId));

		// get photo Uri
		Uri photoUri = null;
		if (cur != null) {
			if (cur.moveToFirst()) {

				photoUri = Uri.withAppendedPath(person,
						ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
			}
			cur.close();
		}
		contact.setPhotoUri(photoUri);
		contact.setPhotoBytes(getPhotoBytes(person));
	}

	/**
	 * @param person
	 *            the contact uri
	 * @return byte array of contacts picture
	 */
	private byte[] getPhotoBytes(Uri person) {
		byte[] bytes = null;

		InputStream iStream = ContactsContract.Contacts
				.openContactPhotoInputStream(mContentResolver, person);
		if (iStream != null) {
			try {
				ByteArrayOutputStream oStream = new ByteArrayOutputStream();
				int character = 0;
				while ((character = iStream.read()) != -1) {
					oStream.write(character);
				}
				bytes = oStream.toByteArray();
			} catch (IOException ioException) {
				// If there is no picture I really don't care!
			}
		}

		return bytes;
	}
}
