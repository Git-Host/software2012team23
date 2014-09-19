package at.tugraz.ist.akm.phonebook.contact;

import java.util.List;

import at.tugraz.ist.akm.content.query.ContactFilter;

public interface IContactReader
{
    abstract public List<Contact> fetchContacts(ContactFilter filter);
}
