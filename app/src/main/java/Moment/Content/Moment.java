package Moment.Content;

import static Moment.Builder.ContentBuilder.NOT_DEFINED_STRING_VALUE;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Moment.Builder.ContentBuilder;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 07 / 10 / 2021 - 10:34
 */
public class Moment implements Comparable<Moment> { // Addition: from Xinyi Li 17/10/2021: implement comparable for RBTree
    private String momentID;
    private String senderID;
    private String timestamp;
    private String schedule;
    private String textContent;
    private String imageURL;
    private String location;
    private String visibility;

    public Moment() {};

    public Moment(String momentID,
                  String senderID,
                  String timestamp,
                  String schedule,
                  String textContent,
                  String imageURL,
                  String location,
                  String visibility
    ) {
        this.momentID = momentID;
        this.senderID = senderID;
        this.timestamp = timestamp;
        this.schedule = schedule;
        this.textContent = textContent;
        this.imageURL = imageURL;
        this.location = location;
        this.visibility = visibility;
    }

    public String getMomentID() {
        return momentID;
    }

    public String getSenderID() {
        return senderID;
    }

    public Calendar getTimestamp() throws ParseException {
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTime(Objects.requireNonNull(ContentBuilder.DATE_FORMAT.parse(this.timestamp)));
        return timestamp;
    }

    public Calendar getSchedule() {
        Calendar schedule = Calendar.getInstance();
        try {
            schedule.setTime(ContentBuilder.DATE_FORMAT.parse(this.schedule));
        } catch (Exception ignored) {
            schedule = null;
        }
        return schedule;
    }

    public boolean isScheduleFinished() {
        Calendar scheduledTime = getSchedule();
        if (scheduledTime == null) {
            return true;
        } else {
            return scheduledTime.getTime().before(Calendar.getInstance().getTime());
        }
    }

    public String getTextContent() {
        return textContent;
    }

    public List<String> getTags() {
        Pattern hastTagPattern = Pattern.compile("#(\\S+)");
        Matcher mat = hastTagPattern.matcher(textContent);
        List<String> tags = new ArrayList<>();
        while (mat.find()) {
            tags.add(mat.group(1));
        }
        return tags;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getLocation() {
        return location;
    }

    public String getVisibility() {
        return visibility;
    }

    public HashMap<String, String> toMap() {
        HashMap<String, String> momentMap = new HashMap<>();
        /* Logic of this program:
         * Not null items:
         *   - momentID
         *   - senderID
         *   - timestamp
         *   - textContent or imageURL  i.e. Must contain text content or image.
         * Except for *not null* items, other items are added respectively.
         */
        if (momentID.equals(NOT_DEFINED_STRING_VALUE)) {
            throw new IllegalArgumentException("Cannot cast moment to map. Lacking some moment parameters");
        }

        if (senderID.equals(NOT_DEFINED_STRING_VALUE)) {
            throw new IllegalArgumentException("Cannot cast moment to map. Lacking some moment parameters");
        }

        if (timestamp.equals(NOT_DEFINED_STRING_VALUE)) {
            throw new IllegalArgumentException("Cannot cast moment to map. Lacking some moment parameters");
        }

        if (textContent.equals(NOT_DEFINED_STRING_VALUE) && imageURL.equals(NOT_DEFINED_STRING_VALUE)) {
            throw new IllegalArgumentException("Cannot cast moment to map. Lacking some moment parameters");
        }

        momentMap.put("momentID", momentID);
        momentMap.put("senderID", senderID);
        momentMap.put("timestamp", timestamp);

        if (!textContent.equals(NOT_DEFINED_STRING_VALUE)) {
            momentMap.put("textContent", textContent);
        }

        if (!imageURL.equals(NOT_DEFINED_STRING_VALUE)) {
            momentMap.put("imageURL", imageURL);
        }

        if (!location.equals(NOT_DEFINED_STRING_VALUE)) {
            momentMap.put("location", location);
        }

        if (!schedule.equals(NOT_DEFINED_STRING_VALUE)) {
            momentMap.put("schedule", schedule);
        }

        if (!visibility.equals(NOT_DEFINED_STRING_VALUE)) {
            momentMap.put("visibility", visibility);
        }

        return momentMap;
    }

    @Override
    public String toString() {
        return "Moment{" +
                "momentID='" + momentID + '\'' +
                ", senderID='" + senderID + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", schedule='" + schedule + '\'' +
                ", textContent='" + textContent + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", location='" + location + '\'' +
                '}';
    }

    /**
     * Addition from: Xinyi Li
     * Created: 17/10/2021
     * compare two moments by moment ID
     *
     * @param moment the moment to be compared with
     * @return an integer ranging from [-1,0,1]
     */
    @Override
    public int compareTo(Moment moment) {
        String thisM = this.momentID;
        String newM = moment.getMomentID();

        return thisM.compareTo(newM);
    }
}
