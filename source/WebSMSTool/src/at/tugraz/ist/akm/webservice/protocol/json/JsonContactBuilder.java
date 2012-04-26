package at.tugraz.ist.akm.webservice.protocol.json;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.Contact.Number;
import at.tugraz.ist.akm.trace.Logable;

public class JsonContactBuilder implements IJsonBuilder {

    @Override
    public JSONObject build(Object data) {
    	Logable log = new Logable();
    	
        // TODO: check type compatibility
        Contact contact = (Contact) data;
        try {
            JSONObject json = new JSONObject();
            json.put("display_name", contact.getDisplayName());
            json.put("last_name", contact.getFamilyName());
            json.put("name", contact.getName());
            json.put("id", contact.getId());
            json.put("image", contact.getPhotoBytes());       
            json.put("phone_numers", buildPhoneNumbers(contact.getPhoneNumbers()));
           
            log.w("contact photo uri: "+contact.getPhotoUri());
            log.i(json.toString());
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONArray buildPhoneNumbers(List<Number> phoneNumbers) throws JSONException {
        JSONArray jsonNumberList = new JSONArray();

        for (Contact.Number number : phoneNumbers) {
            JSONObject jsonNumber = new JSONObject();
            jsonNumber.put("number", number.getNumber());
            jsonNumber.put("type", Integer.toString(number.getType()));
            jsonNumberList.put(jsonNumber);
        }

        return jsonNumberList;
    }
}
