package wang.bogong.anuchat.MomentTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import Moment.Builder.CommentBuilder;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 22 / 10 / 2021 - 17:51
 */
public class CommentBuilderTest {
    CommentBuilder commentBuilder = CommentBuilder.getInstance();

    @Test
    public void testAddMomentID() {
        String commentID = "Akfajls81j398sfhaL";
        commentBuilder.setContentID(commentID);
        assertEquals(commentID, commentBuilder.getResult().getCommentID());
    }

    @Test
    public void testAddSenderID() {
        String senderID = "Akfajls81j398sfhaL";
        commentBuilder.setSenderID(senderID);
        assertEquals(senderID, commentBuilder.getResult().getSenderID());
    }

    @Test
    public void testAddTextContent() {
        String textContent = "Hi, this is my test";
        commentBuilder.setTextContent(textContent);
        assertEquals(textContent, commentBuilder.getResult().getTextContent());
    }
}
