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

package at.tugraz.ist.akm.webservice.protocol.json;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import at.tugraz.ist.akm.monitoring.TelephonySignalStrength;
import at.tugraz.ist.akm.trace.LogClient;

public class JsonTelephonySignalStrengthBuilder implements IJsonBuilder
{

    private final static String DEFAULT_ENCODING = "UTF8";

    public class TelephonySignalValueNames
    {
        public static final String SIGNAL_STRENGTH = "signal_strength";
        public static final String SIGNAL_ICON = "signal_icon";
    }


    @Override
    public JSONObject build(Object data)
    {
        LogClient log = new LogClient(this);

        TelephonySignalStrength signal = (TelephonySignalStrength) data;
        try
        {
            JSONObject json = new JSONObject();
            json.put(TelephonySignalValueNames.SIGNAL_STRENGTH,
                    signal.getSignalStrength());
            json.put(TelephonySignalValueNames.SIGNAL_ICON,
                    new String(signal.getSignalStrengthIconBytes(),
                            DEFAULT_ENCODING));
            return json;
        }
        catch (JSONException jsonException)
        {
            log.error("failed to create jsonTelephonySignalStrength Object",
                    jsonException);
        }
        catch (UnsupportedEncodingException encEx)
        {
            log.error(
                    "failed to create jsonTelephonySignalStrength Object due to encoding error",
                    encEx);
        }
        return null;
    }

}
