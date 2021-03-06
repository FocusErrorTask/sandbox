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

package at.tugraz.ist.akm.trace.ui;

import at.tugraz.ist.akm.R;

public class ServiceEvent extends UiEvent
{
    private boolean mIsStarted = true;


    @SuppressWarnings("unused")
    private ServiceEvent()
    {
    }


    public ServiceEvent(boolean isStarted)
    {
        mIsStarted = isStarted;
    }


    public UiEvent load(ResourceStringLoader loader)
    {
        setDrawableIconId(R.drawable.ic_action_hardware_computer);
        setTitle(loader.getServiceTitle());
        if (mIsStarted)
        {
            setDescription(loader.getServiceStarted());
        } else
        {
            setDescription(loader.getServiceStopped());
        }
        return super.load(loader);
    }
}
