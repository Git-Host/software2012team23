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

package at.tugraz.ist.akm.test.webservice.protocol.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Base64;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.webservice.protocol.json.JsonContactBuilder;
import at.tugraz.ist.akm.webservice.protocol.json.JsonFactory;
import at.tugraz.ist.akm.webservice.protocol.json.JsonTextMessageBuilder;

public class JsonFactoryTest extends TestCase
{
    private JsonFactory mFactory = new JsonFactory();


    public void test_jsonFactoryValidClassesBuildsNotNULLObject()
    {
        try
        {
            TextMessage message = newDummyMessage("10", "100", "fraunz huaba",
                    "1396537050000", "01905555", "18", "body", "status");
            Contact contact = newDummyContact("47", 99,
                    "imageBytes".getBytes(), "232344", "234234", "3333", 2);

            assertNotNull(mFactory.createJsonObject(message));
            assertNotNull(mFactory.createJsonObject(contact));
        }
        catch (Exception e)
        {
            assertTrue(false);
        }
    }


    public void test_jsonFactoryInvalidClassReturnsNULL()
    {
        boolean testHasFailed = true;
        Integer doesNotExist = 42;
        if (null == mFactory.createJsonObject(doesNotExist))
        {
            testHasFailed = false;
        }
        assertFalse(testHasFailed);
    }


    private Contact newDummyContact(String displayName, Integer id,
            byte[] imageBytes, String nr1, String nr2, String nr3,
            Integer phoneNumberType)
    {

        Contact contact = new Contact();
        contact.setDisplayName(displayName);
        contact.setId(id);
        contact.setPhotoBytes(imageBytes);

        List<Contact.Number> phoneNumbers = new ArrayList<Contact.Number>();
        Contact.Number number = new Contact.Number(nr1, phoneNumberType);
        phoneNumbers.add(number);
        number = new Contact.Number(nr2, phoneNumberType);
        phoneNumbers.add(number);
        number = new Contact.Number(nr3, phoneNumberType);
        phoneNumbers.add(number);

        contact.setPhoneNumbers(phoneNumbers);

        return contact;
    }


    public void test_buildJsonContactfromContact()
    {
        String magicDisplayName = "therock";
        String magicNr1 = "1231", magicNr2 = "1232", magicNr3 = "1233";
        Integer phoneNumberType = 1, id = 99;
        byte[] imageBytes = new String("0xb00b5").getBytes();
        byte[] imageEncoded = Base64.encode(imageBytes, Base64.DEFAULT);

        Contact contact = newDummyContact(magicDisplayName, id, imageBytes,
                magicNr1, magicNr2, magicNr3, phoneNumberType);

        JSONObject json = mFactory.createJsonObject(contact);
        try
        {
            assertTrue(json.getString(
                    JsonContactBuilder.ContactValueNames.DISPLAY_NAME).equals(
                    magicDisplayName));
            assertTrue(id == json
                    .getInt(JsonContactBuilder.ContactValueNames.ID));

            assertTrue(Arrays.equals(
                    json.getString(
                            JsonContactBuilder.ContactValueNames.IMAGE_BASE64)
                            .getBytes(), imageEncoded));

            JSONArray jsonPhoneNumbers = json
                    .getJSONArray(JsonContactBuilder.ContactValueNames.PHONE_NUMBERS);

            assertEquals(3, jsonPhoneNumbers.length());
            assertJsonNumberEquals(jsonPhoneNumbers.getJSONObject(0), magicNr1,
                    phoneNumberType.toString());
            assertJsonNumberEquals(jsonPhoneNumbers.getJSONObject(1), magicNr2,
                    phoneNumberType.toString());
            assertJsonNumberEquals(jsonPhoneNumbers.getJSONObject(2), magicNr3,
                    phoneNumberType.toString());
        }
        catch (Exception e)
        {
            assertTrue(false);
        }
    }


    private void assertJsonNumberEquals(JSONObject number, String magicNr,
            String magicNrType) throws Exception
    {
        assertEquals(
                magicNr,
                number.getString(JsonContactBuilder.ContactValueNames.NumbersValueNames.NUMBER));

        assertEquals(
                magicNr,
                number.getString(JsonContactBuilder.ContactValueNames.NumbersValueNames.CLEAN_NUMBER));

        assertEquals(
                magicNrType,
                number.getString(JsonContactBuilder.ContactValueNames.NumbersValueNames.TYPE));

    }


    public void test_buildJsonTextMessageFromTextMessage()
    {

        String id = "133";
        String threadId = "10";
        String address = "019000000";
        String body = "the text message";

        String date = "1396537050000"; // == Thu, 03 Apr 2014 14:57:30 GMT
        String expedtedJsonDate = "3.4.2014 10:57:30"; // d.M.y HH:mm:ss
        String person = "fraunz huaba";
        String seen = "3";
        String status = "99";

        TextMessage textMessage = newDummyMessage(id, threadId, person, date,
                address, seen, body, status);
        JSONObject json = mFactory.createJsonObject(textMessage);

        try
        {
            assertEquals(
                    id,
                    json.getString(JsonTextMessageBuilder.TextMessageValueNames.ID));
            assertEquals(
                    threadId,
                    json.getString(JsonTextMessageBuilder.TextMessageValueNames.THREAD_ID));
            assertEquals(
                    person,
                    json.getString(JsonTextMessageBuilder.TextMessageValueNames.PERSON));
            assertEquals(
                    expedtedJsonDate,
                    json.getString(JsonTextMessageBuilder.TextMessageValueNames.DATE));
            assertEquals(
                    address,
                    json.getString(JsonTextMessageBuilder.TextMessageValueNames.ADDRESS));
            assertEquals(
                    seen,
                    json.getString(JsonTextMessageBuilder.TextMessageValueNames.SEEN));
            assertEquals(
                    body,
                    json.getString(JsonTextMessageBuilder.TextMessageValueNames.BODY));
            assertEquals(
                    status,
                    json.getString(JsonTextMessageBuilder.TextMessageValueNames.STATUS));
        }
        catch (Exception e)
        {
            assertTrue(false);
        }
    }


    private TextMessage newDummyMessage(String id, String threadId,
            String person, String date, String address, String seen,
            String body, String status)
    {
        String protocol = "xxx";
        String type = "xxx";
        String serviceCenter = "xxx";
        String locked = "xxx";
        String replyPathPresent = "xxx";
        String subject = "xxx";
        String errorCode = "xxx";
        String read = "xxx";

        TextMessage textMessage = new TextMessage();
        textMessage.setThreadId(threadId);
        textMessage.setSeen(seen);
        textMessage.setStatus(status);
        textMessage.setType(type);
        textMessage.setServiceCenter(serviceCenter);
        textMessage.setSubject(subject);
        textMessage.setReplyPathPresent(replyPathPresent);
        textMessage.setAddress(address);
        textMessage.setBody(body);
        textMessage.setDate(date);
        textMessage.setErrorCode(errorCode);
        textMessage.setId(id);
        textMessage.setLocked(locked);
        textMessage.setPerson(person);
        textMessage.setProtocol(protocol);
        textMessage.setRead(read);
        return textMessage;
    }


    public void test_buildJsonTelephonySignalStrengthFromTelephonySignalStrength()
    {
        assertFalse(true);
    }


    public void test_buildJsonBatteryStatusFromBatteryStatus()
    {
        assertTrue(false);
    }
}
