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

package at.tugraz.ist.akm.environment;

import android.os.Build;
import at.tugraz.ist.akm.BuildConfig;

public class AppEnvironment
{

    public static boolean isRunningOnEmulator()
    {
        return ("google_sdk".equals(Build.PRODUCT)
                || "sdk_x86".equals(Build.PRODUCT) || Build.FINGERPRINT
                    .startsWith("generic"));
    }


    public static boolean isDebuggable()
    {
        // return (0 != (getApplicationInfo().flags &
        // ApplicationInfo.FLAG_DEBUGGABLE));
        return BuildConfig.DEBUG;
    }
}
