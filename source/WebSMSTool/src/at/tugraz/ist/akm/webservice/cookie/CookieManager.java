package at.tugraz.ist.akm.webservice.cookie;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CookieManager {
    public static int COOKIE_VALID_TIME = 300; // seconds

    public enum CookieType {
        SESSION_COOKIE
    }

    private final static HashMap<String, Cookie> COOKIES = new HashMap<String, Cookie>();

    public static synchronized Cookie createCookie() {
        String cookieID = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(CookieType.SESSION_COOKIE.name(), cookieID);
        COOKIES.put(cookieID, cookie);
        return cookie;
    }

    public static synchronized Cookie lookupCookie(String cookie) {
        Pattern p = Pattern.compile("value=.*");
        Matcher matcher = p.matcher(cookie);

        String cookieId = null;
        if (matcher.find()) {
            String value = matcher.group();
            int idx = value.indexOf("=");
            if (idx >= 0) {
                cookieId = value.substring(idx + 1);
            }
        }
        return cookieId != null ? COOKIES.get(cookieId) : null;
    }

    public static synchronized boolean validateCookie(Cookie cookie) {
        long now = System.currentTimeMillis();
        if ((now - cookie.getTimeLastAccess()) > (COOKIE_VALID_TIME * 1000)) {
            return false;
        }
        return true;
    }

    public static void clear() {
        COOKIES.clear();
    }
}
