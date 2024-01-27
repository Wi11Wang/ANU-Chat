package Moment;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import Moment.Content.Comment;
import Moment.Content.Moment;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 08 / 10 / 2021 - 10:29
 */
public class MomentModel {
    /**
     * This method takes duration, convert duration to the following format:
     * 1y, 1mo, 1d, 1h, 1m, 1s
     *
     * Reference: https://stackoverflow.com/a/23215152/13380588
     *
     * @param duration Duration to be converted to above format.
     * @return String value of specific duration.
     */
    private static String toDuration(long duration) {
        List<Long> times = Arrays.asList(
                TimeUnit.DAYS.toMillis(365),
                TimeUnit.DAYS.toMillis(30),
                TimeUnit.DAYS.toMillis(1),
                TimeUnit.HOURS.toMillis(1),
                TimeUnit.MINUTES.toMillis(1),
                TimeUnit.SECONDS.toMillis(1) );
        List<String> timesString = Arrays.asList("y","mo","d","h","m","s");

        if (duration < 0) {
            return "";
        }

        StringBuilder res = new StringBuilder();

        for(int i=0;i < times.size(); i++) {
            Long current = times.get(i);
            long temp = duration/current;
            if(temp>0) {
                res.append(temp).append(timesString.get(i));
                break;
            }
        }
        if("".equals(res.toString())) {
            return "now";
        } else {
            return res.toString();
        }
    }

    /**
     * Get how much time ago the moment is displayed.
     *
     * @param givenTime Time that the moment is posted.
     * @return How many ago the moment is posted.
     */
    public static String getTimeAgo(Calendar givenTime) {
        long timeElapsed = Calendar.getInstance().getTimeInMillis() - givenTime.getTimeInMillis();
        return toDuration(timeElapsed);
    }

    /**
     * Get how accurate time the moment is displayed.
     * <p>
     * Format of this method will be: dd MMM yyyy HH:mm
     * i.e. 31 Oct 2021, 12:00
     *
     * @param givenTime Time that the moment is posted.
     * @return How many ago the moment is posted.
     */
    public static String getExactTime(Calendar givenTime) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
        return dateFormat.format(givenTime.getTime());
    }

    /**
     * Delete a moment and it's relative data.
     *
     * @param moment Moment to be deleted.
     */
    public static void deleteMoment(Moment moment) {
        String momentID = moment.getMomentID();
        List<String> tags = moment.getTags();
        // Delete moment
        FirebaseDatabase.getInstance().getReference().child("Moments").child(momentID).removeValue();
        // Delete comments.
        FirebaseDatabase.getInstance().getReference().child("Comments").child(momentID).removeValue();
        // Delete likes.
        FirebaseDatabase.getInstance().getReference().child("Likes").child(momentID).removeValue();
        // Delete image (if any).
        if (moment.getImageURL() != null) {
            FirebaseStorage.getInstance().getReferenceFromUrl(moment.getImageURL()).delete();
        }

        for (String tag : tags){
            // Delete Tag
            FirebaseDatabase.getInstance().getReference().child("Tags").child(tag).child(momentID).removeValue();
        }
    }
}
