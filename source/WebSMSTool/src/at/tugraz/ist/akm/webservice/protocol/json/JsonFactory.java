package at.tugraz.ist.akm.webservice.protocol.json;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.trace.Logable;

public class JsonFactory {
    private Logable log = new Logable(getClass().getSimpleName());
    private HashMap<Class<?>, IJsonBuilder> jsonObjectBuilders = new HashMap<Class<?>, IJsonBuilder>();

    public JsonFactory() {
        jsonObjectBuilders.put(Contact.class, new JsonContactBuilder());
    }

    public JSONObject createJsonObject(Object object) {
        IJsonBuilder builder = jsonObjectBuilders.get(object.getClass());
        if (builder != null) {
            return builder.build(object);
        }
        log.w("no json builder available for object <" + object.getClass() + ">");
        return null;
    }

    public JSONObject createJsonObjectFromList(List<?> dataList) {
        return null;
    }
}
