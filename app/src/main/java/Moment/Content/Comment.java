package Moment.Content;

import static Moment.Builder.ContentBuilder.NOT_DEFINED_STRING_VALUE;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import Moment.Builder.ContentBuilder;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 07 / 10 / 2021 - 10:45
 */
public class Comment {
    private String commentID;
    private String parentMomentID;
    private String senderID;
    private String timestamp;
    private String schedule;
    private String textContent;

    /**
     * This constructor is dedicated for Firebase, do NOT remove.
     */
    public Comment() {}

    /**
     * Constructor for moments.
     *
     * @param commentID ID of moment.
     * @param parentMomentID ID of parent moment.
     * @param senderID
     * @param timestamp
     * @param schedule
     * @param textContent
     */
    public Comment(String commentID,
                   String parentMomentID,
                   String senderID,
                   String timestamp,
                   String schedule,
                   String textContent) {
        this.commentID = commentID;
        this.parentMomentID = parentMomentID;
        this.senderID = senderID;
        this.timestamp = timestamp;
        this.schedule = schedule;
        this.textContent = textContent;
    }

    public String getCommentID() {
        return commentID;
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

    public HashMap<String, String> toMap() {
        HashMap<String, String> commentMap = new HashMap<>();
        /* Logic of this program:
         * Not null items:
         *   - commentID
         *   - senderID
         *   - timestamp
         *   - textContent
         * Except for *not null* items, other items are added respectively.
         */
        if (commentID.equals(NOT_DEFINED_STRING_VALUE)) {
            throw new IllegalArgumentException("Cannot cast moment to map. Lacking some moment parameters");
        }

        if (senderID.equals(NOT_DEFINED_STRING_VALUE)) {
            throw new IllegalArgumentException("Cannot cast moment to map. Lacking some moment parameters");
        }

        if (timestamp.equals(NOT_DEFINED_STRING_VALUE)) {
            throw new IllegalArgumentException("Cannot cast moment to map. Lacking some moment parameters");
        }

        if (textContent.equals(NOT_DEFINED_STRING_VALUE)) {
            throw new IllegalArgumentException("Cannot cast moment to map. Lacking some moment parameters");
        }

        commentMap.put("commentID", commentID);
        commentMap.put("parentMomentID", parentMomentID);
        commentMap.put("senderID", senderID);
        commentMap.put("timestamp", timestamp);
        commentMap.put("textContent", textContent);

        if (!schedule.equals(NOT_DEFINED_STRING_VALUE)) {
            commentMap.put("schedule", schedule);
        }

        return commentMap;
    }
}
