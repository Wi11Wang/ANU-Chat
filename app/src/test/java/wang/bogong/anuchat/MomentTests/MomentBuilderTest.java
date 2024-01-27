package wang.bogong.anuchat.MomentTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import Moment.Builder.MomentBuilder;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 22 / 10 / 2021 - 16:39
 */
public class MomentBuilderTest {
    MomentBuilder momentBuilder = MomentBuilder.getInstance();

    @Test
    public void testAddMomentID() {
        String momentID = "Akfajls81j398sfhaL";
        momentBuilder.setContentID(momentID);
        assertEquals(momentID, momentBuilder.getResult().getMomentID());
    }

    @Test
    public void testAddSenderID() {
        String senderID = "Akfajls81j398sfhaL";
        momentBuilder.setSenderID(senderID);
        assertEquals(senderID, momentBuilder.getResult().getSenderID());
    }

    @Test
    public void testAddImageURL() {
        String imageURL = "firebase.com/some-url";
        momentBuilder.setImageURL(imageURL);
        assertEquals(imageURL, momentBuilder.getResult().getImageURL());
    }

    @Test
    public void testAddTextContent() {
        String textContent = "Hi, this is my test";
        momentBuilder.setTextContent(textContent);
        assertEquals(textContent, momentBuilder.getResult().getTextContent());
    }

    @Test
    public void testAddVisibility() {
        String visibility = "PRIVATE";
        momentBuilder.setVisibility(visibility);
        assertEquals(visibility, momentBuilder.getResult().getVisibility());
    }
}
