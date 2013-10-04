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

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ToggleButton;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.R.id;
import at.tugraz.ist.akm.R.layout;
import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.trace.LogClient;

public class SettingsActivity extends DefaultActionBar
{
    EditText userName = null;
    EditText passWord = null;
    EditText port = null;
    RadioButton http = null;
    RadioButton https = null;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        userName = (EditText) findViewById(R.id.username);
        passWord = (EditText) findViewById(R.id.password);
        port = (EditText) findViewById(R.id.port);
        http = (RadioButton) findViewById(R.id.http);
        https = (RadioButton) findViewById(R.id.https);

        getSettings();
    }


    public void saveSettings(View view)
    {
        if (port.getText() == null || port.getText().toString().length() == 0)
        {
            port.setError("This field is mandatory, please add a value (default 8887).");
            return;
        }

        Config config = new Config(getApplicationContext());

        config.setUserName(userName.getText().toString());
        config.setPassword(passWord.getText().toString());

        config.setPort(port.getText().toString());
        if (http.isChecked())
        {
            config.setProtocol("http");
        } else
        {
            config.setProtocol("https");
        }
        finish();
    }


    private void getSettings()
    {
        Config config = new Config(getApplicationContext());

        userName.setText(config.getUserName());
        passWord.setText(config.getPassWord());
        port.setText(config.getPort());
        if (config.getProtocol().equals("http"))
        {
            http.setChecked(true);
            https.setChecked(false);
        } else
        {
            http.setChecked(false);
            https.setChecked(true);
        }
    }


    public void onToggleHttpRestriction(View v)
    {
        LogClient l = new LogClient(this);
        boolean isChecked = ((ToggleButton) v).isChecked();
        l.info("restriction toggled: [" + isChecked + "]");
    }
}
