package at.tugraz.ist.akm.sms;


public interface SentSmsStorage
{
    public TextMessage takeMessage(long sentId);
}
