package Moment.Builder;

import java.util.Calendar;

import Moment.Content.Comment;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 07 / 10 / 2021 - 10:37
 */
public class CommentBuilder implements ContentBuilder{
    private String commentID = NOT_DEFINED_STRING_VALUE;
    private String parentMomentID = NOT_DEFINED_STRING_VALUE;
    private String senderID = NOT_DEFINED_STRING_VALUE;
    private String timestamp = NOT_DEFINED_STRING_VALUE;
    private String schedule = NOT_DEFINED_STRING_VALUE;
    private String textContent = NOT_DEFINED_STRING_VALUE;

    private static CommentBuilder instance = null;

    private CommentBuilder() {}

    public static CommentBuilder getInstance() {
        if (instance == null) {
            instance = new CommentBuilder();
        }

        return instance;
    }

    @Override
    public void setContentID(String commentID) {
        this.commentID = commentID;
    }

    public void setParentMomentID(String parentMomentID) {
        this.parentMomentID = parentMomentID;
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

    public Comment getResult() {
        Comment comment = new Comment(commentID, parentMomentID, senderID, timestamp, schedule, textContent);
        commentID = NOT_DEFINED_STRING_VALUE;
        parentMomentID = NOT_DEFINED_STRING_VALUE;
        senderID = NOT_DEFINED_STRING_VALUE;
        timestamp = NOT_DEFINED_STRING_VALUE;
        schedule = NOT_DEFINED_STRING_VALUE;
        textContent = NOT_DEFINED_STRING_VALUE;
        return comment;
    }
}
