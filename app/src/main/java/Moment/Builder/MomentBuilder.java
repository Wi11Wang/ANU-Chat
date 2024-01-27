package Moment.Builder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import Moment.Builder.ContentBuilder;
import Moment.Content.Moment;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 07 / 10 / 2021 - 10:23
 */
public class MomentBuilder implements ContentBuilder {
    public final static String PUBLIC = "public";
    public final static String FRIENDS = "friends";
    public final static String PRIVATE = "private";

    private String momentID = NOT_DEFINED_STRING_VALUE;
    private String senderID = NOT_DEFINED_STRING_VALUE;
    private String timestamp = NOT_DEFINED_STRING_VALUE;
    private String schedule = NOT_DEFINED_STRING_VALUE;
    private String textContent = NOT_DEFINED_STRING_VALUE;
    private String imageURL = NOT_DEFINED_STRING_VALUE;
    private String location = NOT_DEFINED_STRING_VALUE;
    private String visibility = NOT_DEFINED_STRING_VALUE;

    private static MomentBuilder instance = null;

    private MomentBuilder() {}

    public static MomentBuilder getInstance() {
        if (instance == null) {
            instance = new MomentBuilder();
        }

        return instance;
    }

    @Override
    public void setContentID(String momentID) {
        this.momentID = momentID;
    }

    @Override
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    @Override
    public void setTimeStamp() {
        this.timestamp = DATE_FORMAT.format(Calendar.getInstance().getTime());
    }

    @Override
    public void setSchedule(Calendar time) {
        this.schedule = DATE_FORMAT.format(time.getTime());
    }

    @Override
    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Moment getResult() {
        Moment moment = new Moment(momentID, senderID, timestamp, schedule, textContent, imageURL, location, visibility);

        momentID = NOT_DEFINED_STRING_VALUE;
        senderID = NOT_DEFINED_STRING_VALUE;
        timestamp = NOT_DEFINED_STRING_VALUE;
        schedule = NOT_DEFINED_STRING_VALUE;
        textContent = NOT_DEFINED_STRING_VALUE;
        imageURL = NOT_DEFINED_STRING_VALUE;
        location = NOT_DEFINED_STRING_VALUE;
        visibility = NOT_DEFINED_STRING_VALUE;

        return moment;
    }
}
