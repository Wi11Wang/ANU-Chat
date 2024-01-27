package Moment.Builder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 06 / 10 / 2021 - 14:46
 */
public interface ContentBuilder {
     DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MM yyyy HH:mm:ss Z", Locale.ENGLISH);
     String NOT_DEFINED_STRING_VALUE = "NOT_DEFINED_STRING";

    void setContentID(String contentID);
    void setSenderID(String senderID);
    void setTimeStamp();
    void setSchedule(Calendar time);
    void setTextContent(String textContent);
}
