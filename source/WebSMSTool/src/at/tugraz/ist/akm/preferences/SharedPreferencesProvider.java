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

package at.tugraz.ist.akm.preferences;

import java.io.Closeable;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.providers.PrivateApplicationContentProvider;

public class SharedPreferencesProvider implements Closeable
{
    private SharedPreferences mSharedPreferences = null;
    private PrivateApplicationContentProvider mSettings = null;
    private Context mApplicationContext = null;
    private final static String KEYSTORE_FILE_NAME = "websms-keystore.bks";
    private final static String HTTPS_PROTOCOL_NAME = "https";
    private final static String HTTP_PROTOCOL_NAME = "http";


    public SharedPreferencesProvider(Context context)
    {
        mApplicationContext = context;
        PrivateApplicationContentProvider.construct(mApplicationContext);
        mSettings = PrivateApplicationContentProvider.instance();
        mSettings.openDatabase();
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }


    @Override
    public void close() throws IOException
    {
        if (mSettings != null)
            mSettings.closeDatabase();
        mSettings = null;
        mApplicationContext = null;
        mSharedPreferences = null;
    }


    public boolean isFirstLaunch()
    {
        boolean isFirstLaunched = mSharedPreferences
                .getBoolean(
                        resourceIdString(R.string.preferences_is_fist_launch_key),
                        true);
        if (isFirstLaunched)
        {
            mSharedPreferences
                    .edit()
                    .putBoolean(
                            resourceIdString(R.string.preferences_is_fist_launch_key),
                            false).commit();
        }
        return isFirstLaunched;
    }


    public String getUsername()
    {
        return mSharedPreferences.getString(
                resourceIdString(R.string.preferences_username_key), "");
    }


    public void setUserName(String userName)
    {
        Editor editor = mSharedPreferences.edit();
        editor.putString(resourceIdString(R.string.preferences_username_key),
                userName);
        editor.apply();
    }


    public boolean isAccessRestrictionEnabled()
    {
        return mSharedPreferences.getBoolean(
                resourceIdString(R.string.preferences_access_restriction_key),
                false);
    }


    public void setAccessRestriction(boolean isEnabled)
    {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(
                resourceIdString(R.string.preferences_access_restriction_key),
                isEnabled);
        editor.apply();
    }


    private String resourceIdString(int resourceId)
    {
        return mApplicationContext.getString(resourceId);
    }


    public String getPassword()
    {
        return mSharedPreferences.getString(
                resourceIdString(R.string.preferences_password_key), "");
    }


    public void setPassword(String password)
    {
        Editor editor = mSharedPreferences.edit();
        editor.putString(resourceIdString(R.string.preferences_password_key),
                password);
        editor.apply();
    }


    public int getPort()
    {
        return Integer.parseInt(mSharedPreferences.getString(
                resourceIdString(R.string.preferences_server_port_key), "-1"));
    }


    public void setPort(int port)
    {
        Editor editor = mSharedPreferences.edit();
        editor.putString(
                resourceIdString(R.string.preferences_server_port_key),
                Integer.toString(port));
        editor.apply();
    }


    public String getProtocol()
    {
        if (mSharedPreferences.getBoolean(
                resourceIdString(R.string.preferences_protocol_checkbox_key),
                true))
        {
            return HTTPS_PROTOCOL_NAME;
        }
        return HTTP_PROTOCOL_NAME;
    }


    public boolean isHttpsEnabled()
    {
        return getProtocol().compareTo(HTTPS_PROTOCOL_NAME) == 0;
    }


    public void setProtocol(String protocol)
    {
        Boolean isHttps = true;
        if (protocol.equals(HTTP_PROTOCOL_NAME))
        {
            isHttps = false;
        }
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(
                resourceIdString(R.string.preferences_protocol_checkbox_key),
                isHttps);
        editor.apply();
    }


    public String getKeyStorePassword()
    {
        return mSettings.restoreKeystorePassword();
    }


    public void setKeyStorePassword(String keyStorePassword)
    {
        mSettings.storeKeystorePassword(keyStorePassword);
    }


    public String getKeyStoreFilePath()
    {
        return mApplicationContext.getFilesDir().getPath().toString() + "/"
                + KEYSTORE_FILE_NAME;
    }

}
