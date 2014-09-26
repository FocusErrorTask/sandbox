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

package at.tugraz.ist.akm.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.trace.LogClient;

public class AboutFragment extends Fragment
{
    LogClient mLog = new LogClient(AboutFragment.class.getCanonicalName());


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.about_fragment, container, false);
    }


    @Override
    public void onStart()
    {
        super.onStart();
        Linkify.addLinks((TextView) getView().findViewById(R.id.aboutInfoLink),
                Linkify.ALL);

        Linkify.addLinks(
                (TextView) getView().findViewById(
                        R.id.about_credits_onlinelogomaker_id), Linkify.ALL);

        Linkify.addLinks(
                (TextView) getView().findViewById(
                        R.id.about_credits_online_launcher_icon_generator_id),
                Linkify.ALL);

        Linkify.addLinks(
                (TextView) getView().findViewById(R.id.about_credits_zutubi_id),
                Linkify.ALL);

        Linkify.addLinks(
                (TextView) getView().findViewById(
                        R.id.about_credits_robotium_id), Linkify.ALL);

        Linkify.addLinks(
                (TextView) getView().findViewById(
                        R.id.about_credits_bouncycastle_id), Linkify.ALL);

        Linkify.addLinks(
                (TextView) getView().findViewById(R.id.about_credits_apache_id),
                Linkify.ALL);
    }

}
