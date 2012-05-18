package at.tugraz.ist.akm.webservice.cookie;


public class Cookie {
    private final String name;
    private final String value;
    private long timeLastAccess = System.currentTimeMillis(); 

    Cookie(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void accessed() {
        timeLastAccess = System.currentTimeMillis();
    }
    
    public long getTimeLastAccess() {
        return timeLastAccess;
    }


    @Override
    public String toString() {
        return String.format("name=%s value=%s", name, value);
    }
}
