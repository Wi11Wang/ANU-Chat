package wang.bogong.anuchat.Model;

public class MessageModel
{
    String userID;
    String message;
    String messageID;
    long timestamp;

    public MessageModel() {}

    public MessageModel(String userID, String message)
    {
        this.userID = userID;
        this.message = message;
    }

    public MessageModel(String userID, String message, long timestamp)
    {
        this.userID = userID;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUserID()
    {
        return userID;
    }

    public void setUserID(String userID)
    {
        this.userID = userID;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getMessageID()
    {
        return messageID;
    }

    public void setMessageID(String messageID)
    {
        this.messageID = messageID;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }
}
