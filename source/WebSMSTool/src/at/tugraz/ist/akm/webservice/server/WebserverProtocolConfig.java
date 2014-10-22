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

package at.tugraz.ist.akm.webservice.server;

public class WebserverProtocolConfig
{
    public String protocolName = null;
    public String username = null;
    public String password = null;
    public int port = -1;
    public boolean isHttpsEnabled = false;
    public boolean isUserAuthEnabled = false;


    public WebserverProtocolConfig()
    {
    }


    public WebserverProtocolConfig(WebserverProtocolConfig src)
    {
        this.isHttpsEnabled = src.isHttpsEnabled;
        this.password = src.password;
        this.username = src.username;
        this.port = src.port;
        this.protocolName = src.protocolName;
        this.isUserAuthEnabled = src.isUserAuthEnabled;
    }

}
