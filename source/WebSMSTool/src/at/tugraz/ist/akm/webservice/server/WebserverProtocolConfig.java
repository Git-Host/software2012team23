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
