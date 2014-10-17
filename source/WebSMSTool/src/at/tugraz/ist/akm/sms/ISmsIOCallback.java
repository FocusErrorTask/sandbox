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

import java.util.List;

import android.content.Context;

public interface ISmsIOCallback {

	public void smsSentCallback(Context context, List<TextMessage> messages);
	
	public void smsSentErrorCallback(Context context, List<TextMessage> messages);

	public void smsDeliveredCallback(Context context, List<TextMessage> messagea);
	
	public void smsReceivedCallback(Context context, List<TextMessage> messages);

}