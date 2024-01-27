package wang.bogong.anuchat.MomentsActivities;

import static android.text.format.DateFormat.is24HourFormat;
import static java.util.Calendar.DAY_OF_YEAR;
import static Moment.MomentModel.deleteMoment;
import static Moment.MomentViewModel.displaySenderSpecificComponents;
import static Moment.MomentViewModel.setAvatarClickActions;
import static Moment.MomentViewModel.setFollowTextViewActions;
import static Moment.MomentViewModel.setHashTagClickActions;
import static Moment.MomentViewModel.setLikeButtonActions;
import static Moment.MomentViewModel.setLinkClickActions;
import static Moment.MomentViewModel.setLocationDisplay;
import static Moment.MomentViewModel.setMomentImageDisplay;
import static Moment.MomentViewModel.setNumberOfComments;
import static Moment.MomentViewModel.setNumberOfLikes;
import static Moment.MomentViewModel.setTextContentDisplay;
import static Moment.MomentViewModel.setTimeDisplayAndActions;
import static User.Gender.FEMALE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import wang.bogong.anuchat.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialEditText;
import com.hendraanggrian.appcompat.widget.SocialTextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Adapter.CommentAdapter;
import Moment.Builder.CommentBuilder;
import Moment.Content.Comment;
import Moment.Content.Moment;
import User.User;

/**
 * This activity displays comment and moment detail.
 */
public class MomentDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private View baseLayout;
    private SocialEditText commentEditText;
    private TextView scheduleDisplayTextView;
    private ImageButton setScheduleImageButton;
    private ImageButton sendCommentImageButton;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String momentID;

    private FirebaseUser firebaseUser;

    private boolean isScheduled = false;
    private Calendar scheduleCalender;

    private ShapeableImageView avatar;
    private TextView userName;
    private ImageView visibilityImageView;
    private ImageView scheduleImageView;
    private TextView scheduleTextView;
    private TextView timeAgo;
    private ImageView locationIconImageView;
    private TextView locationTextView;
    private ImageButton moreImageButton;
    private TextView followTextView;

    private SocialTextView momentContentTextView;
    private ShapeableImageView momentImageImageView;

    private ImageButton likeButton;
    private TextView likeCount;
    private TextView commentCount;

    private DatabaseReference momentDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment_detail);

        scheduleCalender = Calendar.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        animationSetUp();
        linkUIComponents();
        getMomentInfoFromIntent();
        setBackIcon();

        setSetSendCommentImageButtonActions();
        setSetScheduleImageButtonActions();

        loadOriginalMoment();
        setSwipeRefreshLayoutActions();
        initializeCommentsTimeLine();
        setLinkClickActions(this, momentContentTextView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        momentDatabaseReference.removeEventListener(momentListener);
    }

    //**************************************************************************
    //                            Basic setup                                  *
    //**************************************************************************
    /**
     * Link UI components.
     */
    private void linkUIComponents() {
        recyclerView = findViewById(R.id.activity_moment_detail_commentsRecyclerView);
        baseLayout = findViewById(R.id.activity_moment_detail_baseLayout);
        commentEditText = findViewById(R.id.activity_moment_detail_commentEditText);
        scheduleDisplayTextView = findViewById(R.id.activity_moment_detail_scheduleTextView);
        setScheduleImageButton = findViewById(R.id.activity_moment_detail_setScheduleImageButton);
        sendCommentImageButton = findViewById(R.id.activity_moment_detail_sendCommentImageButton);
        toolbar = findViewById(R.id.activity_moment_detail_topToolbar);
        swipeRefreshLayout = findViewById(R.id.activity_moment_detail_swipeRefreshLayout);

        avatar = findViewById(R.id.activity_moment_detail_moment_avatarShapeableImageView);
        userName = findViewById(R.id.activity_moment_detail_moment_userNameTextView);
        visibilityImageView = findViewById(R.id.activity_moment_detail_moment_visibilityImageView);
        scheduleImageView = findViewById(R.id.activity_moment_detail_moment_scheduleImageView);
        scheduleTextView = findViewById(R.id.activity_moment_detail_moment_scheduleTextView);
        timeAgo = findViewById(R.id.activity_moment_detail_moment_timeAgoTextView);
        locationIconImageView = findViewById(R.id.activity_moment_detail_moment_locationIconImageView);
        locationTextView = findViewById(R.id.activity_moment_detail_moment_atLocationTextView);
        moreImageButton = findViewById(R.id.activity_moment_detail_moment_moreImageView);
        followTextView = findViewById(R.id.activity_moment_detail_followTextView);

        momentContentTextView = findViewById(R.id.activity_moment_detail_moment_momentContentTextView);
        momentImageImageView = findViewById(R.id.activity_moment_detail_moment_momentImageShapeableImageView);

        likeButton = findViewById(R.id.activity_moment_detail_moment_likeImageView);
        likeCount = findViewById(R.id.activity_moment_detail_moment_likeCountTextView);
        commentCount = findViewById(R.id.activity_moment_detail_moment_commentCountTextView);
    }

    /**
     * Setup search button animation with search activity.
     */
    private void animationSetUp() {
        findViewById(android.R.id.content).setTransitionName("moment_cardview_baseCard");
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());

        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.addTarget(android.R.id.content);
        transform.setStartShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(10));
        transform.setEndShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(10));
        transform.setDuration(300);

        getWindow().setSharedElementEnterTransition(transform);
        getWindow().setSharedElementReturnTransition(transform);
    }

    /**
     * Obtain moment id from intent.
     * <p>
     * Aim for setting this method is to identify the clicked moment.
     */
    private void getMomentInfoFromIntent() {
        Intent intent = getIntent();
        momentID = intent.getStringExtra("momentID");
        String source = intent.getStringExtra("source");

        FirebaseDatabase.getInstance().getReference().child("Moments").child(momentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    MomentDetailActivity.this.finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (source.equals("moment")) {
            hideKeyboard();
        } else {
            commentEditText.requestFocusFromTouch();
        }
    }

    /**
     * Set back icon in the tool bar.
     */
    private void setBackIcon() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    //**************************************************************************
    //                         Main Button Actions                             *
    //**************************************************************************

    /**
     * Set action to setSendCommentImageButton.
     * <p>
     * This method sends the comment to firebase.
     */
    private void setSetSendCommentImageButtonActions() {
        sendCommentImageButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(commentEditText.getText().toString())) {
                Toast.makeText(this, "Commnet cannot be empty.", Toast.LENGTH_LONG).show();
            } else {
                sendCommentToFireBase();
                commentEditText.getText().clear();
                hideKeyboard();
                unsetTimePicker();
            }
        });
    }

    /**
     * Hide keyboard.
     * <p>
     * Helper function of {@code setSetSendCommentImageButtonActions}
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(baseLayout.getWindowToken(), 0);
    }

    /**
     * Sends comment to firebase.
     * <p>
     * Helper function of {@code setSetSendCommentImageButtonActions}.
     */
    private void sendCommentToFireBase() {
        CommentBuilder commentBuilder = CommentBuilder.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Comments").child(momentID);
        String commentID = databaseReference.push().getKey();

        commentBuilder.setContentID(commentID);
        commentBuilder.setParentMomentID(momentID);;
        commentBuilder.setSenderID(FirebaseAuth.getInstance().getCurrentUser().getUid());
        commentBuilder.setTimeStamp();
        commentBuilder.setTextContent(commentEditText.getText().toString());

        if (isScheduled) {
            commentBuilder.setSchedule(scheduleCalender);
        }

        Map<String, String> commentMap = commentBuilder.getResult().toMap();
        databaseReference.child(commentID).setValue(commentMap);
    }

    /**
     * Set action to setSendCommentImageButton.
     * <p>
     * This method sends the comment to firebase.
     */
    private void setSetScheduleImageButtonActions() {
        setScheduleImageButton.setOnClickListener(view -> {
            if (!isScheduled) {
                setupTimePicker();
            } else {
                unsetTimePicker();
            }
        });
    }

    /**
     * Set up time picker for picking scheduled time.
     * <p>
     * Helper function of {@code setSetScheduleImageButtonActions}
     */
    private void setupTimePicker() {
        Calendar calendar = Calendar.getInstance();

        int displayHour = calendar.get(Calendar.HOUR_OF_DAY);
        int displayMinute = calendar.get(Calendar.MINUTE);

        if (isScheduled) {
            displayHour = scheduleCalender.get(Calendar.HOUR_OF_DAY);
            displayMinute = scheduleCalender.get(Calendar.MINUTE);
        } else {
            scheduleCalender = Calendar.getInstance();
        }
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
            Calendar pickedTime = new GregorianCalendar(0, 0, 0, hourOfDay, minute);
            Calendar currentTime = new GregorianCalendar(0, 0, 0, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE));

            if (pickedTime.before(currentTime)) {
                scheduleCalender.add(DAY_OF_YEAR, 1);
            }

            scheduleCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
            scheduleCalender.set(Calendar.MINUTE, minute);
            scheduleCalender.set(Calendar.SECOND, 0);

            setSetScheduleDisplayTextView();
            setScheduleImageButton.setColorFilter(ContextCompat.getColor(this, R.color.anu_logo_yellow));
            isScheduled = true;
        }, displayHour, displayMinute, is24HourFormat(this));

        timePickerDialog.show();
    }

    /**
     * Unset time picker (unschedule the comment)
     * <p>
     * Helper function of {@code setSetScheduleImageButtonActions}
     */
    private void unsetTimePicker() {
        isScheduled = false;
        scheduleCalender = Calendar.getInstance();
        scheduleDisplayTextView.setVisibility(View.GONE);
        setScheduleImageButton.setColorFilter(ContextCompat.getColor(this, R.color.default_icon_grey));
    }

    /**
     * Set scheduled text view content.
     * <p>
     * Helper function of {@code setupTimepicker}
     */
    private void setSetScheduleDisplayTextView() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        String scheduledTime = dateFormat.format(scheduleCalender.getTime());
        scheduleDisplayTextView.setText(scheduledTime);
        scheduleDisplayTextView.setVisibility(View.VISIBLE);
    }

    //**************************************************************************
    //                          Timeline relevant                              *
    //**************************************************************************
    /**
     * Initialize time line and adapters.
     */
    private void initializeCommentsTimeLine() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Add divider.
        DividerItemDecoration itemDecorator = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.recyclerview_divider));
        recyclerView.addItemDecoration(itemDecorator);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, momentID, commentList);
        recyclerView.setAdapter(commentAdapter);

        refreshCommentsTimeline();
    }

    /**
     * Retrieve all comments data from firebase and update to timeline.
     */
    private void refreshCommentsTimeline() {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseDatabase.getInstance().getReference().child("Comments").child(momentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                ArrayList<Comment> scheduledComments = new ArrayList<>();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (!comment.getSenderID().equals(firebaseUser.getUid())) {
                        if (comment.isScheduleFinished()) {
                            if (comment.getSchedule() == null) {
                                commentList.add(comment);
                            } else {
                                scheduledComments.add(comment);
                            }
                        }
                    } else {
                        if (comment.getSchedule() == null) {
                            commentList.add(comment);
                        } else {
                            scheduledComments.add(comment);
                        }
                    }
                }

                try {
                    updateCommentsList(scheduledComments);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                commentAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Adding scheduled moments to moments list.
     *
     * @param scheduledComments Scheduled moments.
     * @throws ParseException Error when parsing time.
     */
    private void updateCommentsList(ArrayList<Comment> scheduledComments) throws ParseException {
        for (Comment scheduledComment : scheduledComments) {
            Date schedule = scheduledComment.getSchedule().getTime();
            int left = 0;
            int right = commentList.size();
            while (left < right) {
                int middle = (left + right) / 2;
                Date sortedMomentTimeStamp = commentList.get(middle).getTimestamp().getTime();
                if (schedule.after(sortedMomentTimeStamp)) {
                    left = middle + 1;
                } else {
                    right = middle;
                }
            }
            commentList.add(left, scheduledComment);
        }
    }

    /**
     * Set swipe to refresh layout actions.
     *
     * Current actions: swipe to obtain data in firebase.
     */
    private void setSwipeRefreshLayoutActions() {
        int anu_logo_yellow = ContextCompat.getColor(this, R.color.anu_logo_yellow);
        int anu_original_dark = ContextCompat.getColor(this, R.color.anu_original_dark);
        swipeRefreshLayout.setColorSchemeColors(anu_original_dark, anu_logo_yellow);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshCommentsTimeline();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    /**
     * Load original moment.
     */
    private void loadOriginalMoment() {
        momentDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Moments").child(momentID);
        momentDatabaseReference.addValueEventListener(momentListener);
    }

    /**
     * Set moment listener.
     */
    private final ValueEventListener momentListener = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

            Moment moment = snapshot.getValue(Moment.class);

            setAvatarClickActions(MomentDetailActivity.this, avatar, moment.getSenderID());
            FirebaseDatabase.getInstance().getReference().child("User").child(moment.getSenderID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user == null) {
                        userName.setText("Deleted Account");
                        avatar.setImageResource(R.drawable.ic_delete);
                    } else {
                        userName.setText(user.getNickName());
                        if (!user.getAvatarURL().equals("")) {
                            Glide.with(MomentDetailActivity.this).load(user.getAvatarURL()).into(avatar);
                        } else {
                            if (user.gender.equals(FEMALE)) {
                                avatar.setImageResource(R.drawable.default_avatar_female);
                            } else {
                                avatar.setImageResource(R.drawable.default_avatar_male);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });


            // Display sender exclusive message.
            if (moment.getSenderID().equals(firebaseUser.getUid())) {
                displaySenderSpecificComponents(moreImageButton,
                        visibilityImageView,
                        scheduleImageView,
                        scheduleTextView,
                        moment);
                setMenuClickActions(moreImageButton, moment);
            }

            // Time
            try {
                setTimeDisplayAndActions(timeAgo, moment);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Display location information.
            setLocationDisplay(locationTextView, locationIconImageView, moment);

            // Get post image.
            setMomentImageDisplay(MomentDetailActivity.this, momentImageImageView, moment);

            // Display text content.
            setTextContentDisplay(momentContentTextView, moment);

            setLikeButtonActions(likeButton, moment);
            setNumberOfLikes(likeCount, moment);

            setNumberOfComments(commentCount, moment);

            // Set hashtag click actions.
            setHashTagClickActions(MomentDetailActivity.this, momentContentTextView);

            setFollowFunctionality(moment);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    /**
     * This method shows menu when click more button.
     *
     * @param moreImageButton "more" button.
     * @param moment Current moment.
     */
    private void setMenuClickActions(ImageView moreImageButton,
                                     Moment moment) {
        moreImageButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.moment_more_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.moment_menu_delete) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Delete this moment?");
                    alertDialogBuilder.setMessage("This process cannot withdraw.");
                    alertDialogBuilder.setPositiveButton("Delete", (dialog, which) -> {
                        momentDatabaseReference.removeEventListener(momentListener);
                        deleteMoment(moment);
                        this.finish();
                    });
                    alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {});
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.alert_red));
                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.anu_logo_yellow));
                }
                return true;
            });
            popupMenu.show();
        });
    }

    /**
     * Show follow text view if current user is not following the other user.
     *
     * @param moment Moment to get sender info.
     */
    private void setFollowFunctionality(Moment moment) {
        if (!firebaseUser.getUid().equals(moment.getSenderID())) {
            FirebaseDatabase.getInstance().getReference().child("Following").child(firebaseUser.getUid()).child(moment.getSenderID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // If not following:
                        followTextView.setVisibility(View.VISIBLE);
                        setFollowTextViewActions(followTextView, moment.getSenderID());
                    } else {
                        // If following:
                        followTextView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }



}