package Moment;

import static java.util.Calendar.DAY_OF_YEAR;
import static Moment.Builder.MomentBuilder.FRIENDS;
import static Moment.Builder.MomentBuilder.PRIVATE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import wang.bogong.anuchat.InformationActivity;
import wang.bogong.anuchat.R;
import wang.bogong.anuchat.SearchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialTextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import Moment.Content.Comment;
import Moment.Content.Moment;

/**
 * This class has set of static methods. These methods helps to bind information
 * between model and view.
 *
 * @author Bogong Wang
 * @project Android Project
 * @created 09 / 10 / 2021 - 07:15
 */
public class MomentViewModel {

    /**
     * Display how many hours ago the moment is posted.
     * <p>
     * This format of this method can be checked in {@link MomentModel#getTimeAgo(Calendar)}
     * <p>
     * This method will also respond to click on time display text.
     * If displaying how many time ago the moment is posted, it will display accurate time and
     * vice versa.
     *
     * @param timeAgoTextView TextView to display time information.
     * @param moment Moment to be displayed time information
     * @throws ParseException Error when parsing time.
     */
    public static void setTimeDisplayAndActions(TextView timeAgoTextView, Moment moment) throws ParseException {
        // Set display of time.
        if (moment.getSchedule() == null) {
            timeAgoTextView.setText(MomentModel.getTimeAgo(moment.getTimestamp()));
        } else {
            timeAgoTextView.setText(MomentModel.getTimeAgo(moment.getSchedule()));
        }

        // Set actions respond to click.
        timeAgoTextView.setTag("rough time");
        timeAgoTextView.setOnClickListener(v -> {
            if (timeAgoTextView.getTag().equals("rough time")) {
                timeAgoTextView.setTag("exact time");
                if (moment.getSchedule() == null) {
                    try {
                        timeAgoTextView.setText(MomentModel.getExactTime(moment.getTimestamp()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    timeAgoTextView.setText(MomentModel.getExactTime(moment.getSchedule()));
                }
            } else {
                timeAgoTextView.setTag("rough time");
                if (moment.getSchedule() == null) {
                    try {
                        timeAgoTextView.setText(MomentModel.getTimeAgo(moment.getTimestamp()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    timeAgoTextView.setText(MomentModel.getTimeAgo(moment.getSchedule()));
                }
            }
        });
    }

    /**
     * Set display of location.
     *
     * @param locationTextView TextView to display location.
     * @param locationIconImageView ImageView to display location icon.
     * @param moment Current moment.
     */
    public static void setLocationDisplay(TextView locationTextView,
                                          ImageView locationIconImageView,
                                          Moment moment) {
        String location = moment.getLocation();
        if (location != null) {
            locationTextView.setText(location.split(",")[2]);
            locationTextView.setVisibility(View.VISIBLE);
            locationIconImageView.setVisibility(View.VISIBLE);
        } else {
            locationTextView.setVisibility(View.GONE);
            locationIconImageView.setVisibility(View.GONE);
        }
    }

    /**
     * Display the image of moment.
     *
     * @param context Context where holds the ImageView
     * @param momentImageImageView ImageView to display moment image.
     * @param moment Current moment.
     */
    public static void setMomentImageDisplay(Context context,
                                             ImageView momentImageImageView,
                                             Moment moment) {
        String imageURL = moment.getImageURL();
        if (imageURL != null) {
            Glide.with(context).load(imageURL).into(momentImageImageView);
            momentImageImageView.setVisibility(View.VISIBLE);
        } else {
            momentImageImageView.setVisibility(View.GONE);
        }
    }

    /**
     * Set the text content of moment.
     *
     * @param textContentSocialTextView SocialTextView to display text content.
     * @param moment Current moment.
     */
    public static void setTextContentDisplay(SocialTextView textContentSocialTextView,
                                             Moment moment) {
        String textContent = moment.getTextContent();
        if (textContent != null) {
            textContentSocialTextView.setText(textContent);
            textContentSocialTextView.setVisibility(View.VISIBLE);
        } else {
            textContentSocialTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Display sender exclusive components.
     * <p>
     * This method will display more button, visibility icon, schedule icon and text (if has schedule).
     * <p>
     * This method will also enable deletion on moment in more button.
     *
     * @param moreImageButton ImageButton shows more button.
     * @param visibilityImageView ImageView shows visibility icon.
     * @param scheduleImageView ImageView shows schedule icon.
     * @param scheduleTextView TextView shows schedule text.
     * @param moment Current moment.
     */
    public static void displaySenderSpecificComponents(ImageButton moreImageButton,
                                                       ImageView visibilityImageView,
                                                       ImageView scheduleImageView,
                                                       TextView scheduleTextView,
                                                       Moment moment) {
        moreImageButton.setVisibility(View.VISIBLE);

        String visibility = moment.getVisibility();
        if (visibility == null) {
            visibilityImageView.setVisibility(View.VISIBLE);
            visibilityImageView.setImageResource(R.drawable.ic_public);
        } else if (visibility.equals(FRIENDS)) {
            visibilityImageView.setVisibility(View.VISIBLE);
            visibilityImageView.setImageResource(R.drawable.ic_friends);
        } else if (visibility.equals(PRIVATE)) {
            visibilityImageView.setVisibility(View.VISIBLE);
            visibilityImageView.setImageResource(R.drawable.ic_lock);
        }

        Calendar schedule = moment.getSchedule();
        if (schedule != null) {
            if (!moment.isScheduleFinished()) {
                scheduleImageView.setVisibility(View.VISIBLE);
                scheduleTextView.setVisibility(View.VISIBLE);
                scheduleImageView.setImageResource(R.drawable.ic_time);
                DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                String scheduledTime = dateFormat.format(schedule.getTime());
                if (Calendar.getInstance().get(DAY_OF_YEAR) == schedule.get(DAY_OF_YEAR)) {
                    scheduleTextView.setText("Today " + scheduledTime);
                } else {
                    scheduleTextView.setText("Tomorrow " + scheduledTime);
                }
            }
        } else {
            scheduleImageView.setVisibility(View.GONE);
            scheduleTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Set actions on like button.
     *
     * @param likeImageButton ImageButton displays like.
     * @param moment Current moment.
     */
    public static void setLikeButtonActions(ImageButton likeImageButton, Moment moment) {
        String momentID = moment.getMomentID();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String NOT_LIKED = "not liked";
        String LIKED = "liked";

        likeImageButton.setOnClickListener(v -> {
            if (likeImageButton.getTag().equals(NOT_LIKED)) {
                FirebaseDatabase.getInstance().getReference().child("Likes").child(momentID).child(firebaseUser.getUid()).setValue(true);
                likeImageButton.setImageResource(R.drawable.ic_liked);
                likeImageButton.setTag(LIKED);
            } else {
                FirebaseDatabase.getInstance().getReference().child("Likes").child(momentID).child(firebaseUser.getUid()).removeValue();
                likeImageButton.setTag(NOT_LIKED);
                likeImageButton.setImageResource(R.drawable.ic_like);
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Likes").child(momentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()) {
                    likeImageButton.setTag(LIKED);
                    likeImageButton.setImageResource(R.drawable.ic_liked);
                } else {
                    likeImageButton.setTag(NOT_LIKED);
                    likeImageButton.setImageResource(R.drawable.ic_like);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public static void setAvatarClickActions(Context context, ImageView avatarImage, String senderID) {
        avatarImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, InformationActivity.class);
            intent.putExtra("USER_FROM_CONTACT", senderID);
            context.startActivity(intent);
        });
    }

    public static void setFollowTextViewActions(TextView textView, String otherUserID) {
        textView.setOnClickListener(v -> {
            String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("Following").child(currentUserID).child(otherUserID).setValue(otherUserID);
            FirebaseDatabase.getInstance().getReference().child("Follower").child(otherUserID).child(currentUserID).setValue(currentUserID);

            textView.setVisibility(View.GONE);
        });
    }

    public static void setLinkClickActions(Context context, SocialTextView socialTextView) {
        socialTextView.setOnHyperlinkClickListener((view, text) -> {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("link", text);
            clipboardManager.setPrimaryClip(clip);
            Toast.makeText(context, "Link has been copied to the clipboard.", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Set number of likes.
     *
     * @param numberOfLikesTextView TextView displays number of likes.
     * @param moment Current moment.
     */
    public static void setNumberOfLikes(TextView numberOfLikesTextView, Moment moment) {
        String momentID = moment.getMomentID();
        FirebaseDatabase.getInstance().getReference().child("Likes").child(momentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numberOfLikesTextView.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Set number of likes.
     *
     * @param numberOfComments TextView displays number of comments.
     * @param moment Current moment.
     */
    public static void setNumberOfComments(TextView numberOfComments, Moment moment) {
        String momentID = moment.getMomentID();
        FirebaseDatabase.getInstance().getReference().child("Comments").child(momentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Only display schedule finished moments.
                int counter = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Comment comment = snapshot1.getValue(Comment.class);
                    if (comment.isScheduleFinished()) {
                        counter ++;
                    }
                }
                numberOfComments.setText(String.valueOf(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Set hash tag click action.
     * <p>
     * If click on the hash tag, jump to search activity.
     *
     * @param context Context which holds the SocialTextView.
     * @param socialTextView SocialTextView that displays text content.
     */
    public static void setHashTagClickActions(Context context, SocialTextView socialTextView) {
        socialTextView.setOnHashtagClickListener((view, text) -> {
            Intent intent = new Intent(context, SearchActivity.class);
            intent.putExtra("search content", "#" + text + ";");
            context.startActivity(intent);
        });
    }

    /**
     * Display how many hours ago the comment is posted.
     *
     * @param timeAgoTextView TextView to display time information.
     * @param comment Comment to be displayed time information
     * @throws ParseException Error when parsing time.
     */
    public static void setTimeAgoTextViewActions(TextView timeAgoTextView, Comment comment) throws ParseException {
        // Set display of time.
        if (comment.getSchedule() == null) {
            timeAgoTextView.setText(MomentModel.getTimeAgo(comment.getTimestamp()));
        } else {
            timeAgoTextView.setText(MomentModel.getTimeAgo(comment.getSchedule()));
        }

        // Set actions respond to click.
        timeAgoTextView.setTag("rough time");
        timeAgoTextView.setOnClickListener(v -> {
            if (timeAgoTextView.getTag().equals("rough time")) {
                timeAgoTextView.setTag("exact time");
                if (comment.getSchedule() == null) {
                    try {
                        timeAgoTextView.setText(MomentModel.getExactTime(comment.getTimestamp()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    timeAgoTextView.setText(MomentModel.getExactTime(comment.getSchedule()));
                }
            } else {
                timeAgoTextView.setTag("rough time");
                if (comment.getSchedule() == null) {
                    try {
                        timeAgoTextView.setText(MomentModel.getTimeAgo(comment.getTimestamp()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    timeAgoTextView.setText(MomentModel.getTimeAgo(comment.getSchedule()));
                }
            }
        });
    }

}
