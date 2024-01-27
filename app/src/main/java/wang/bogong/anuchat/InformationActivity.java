package wang.bogong.anuchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import wang.bogong.anuchat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import Adapter.MomentAdapter;
import Moment.Content.Moment;
import User.*;

public class InformationActivity extends AppCompatActivity
{

    String userID;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    ImageView avatar;
    ImageView likeIcon;
    ImageView genderIcon;
    ImageView hashTag;
    TextView nickname;
    TextView college;
    TextView gradYear;
    TextView fullname;
    TextView age;
    RecyclerView recyclerView;
    TextView momentsTagCount;
    TextView likeCountView;

    Toolbar toolbar;

    ArrayList<Moment> momentsList = new ArrayList<>();
    MomentAdapter momentAdapter;
    Set<String> momentIDSet = new HashSet<>();
    LinearLayoutManager layoutManager;
    int likeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        Intent intent = getIntent();
        userID = intent.getStringExtra("USER_FROM_CONTACT");

        linkWidget();
        setUserInformation();
        setUserBio();

        initializeMomentsTimeline();
        refreshMomentsTimeline();
        setMomentCounter();
        setToolbar();
    }

    /**
     * Link widgets.
     */
    private void linkWidget()
    {
        avatar = findViewById(R.id.information_avatar1);
        likeIcon = findViewById(R.id.information_like_icon1);
        genderIcon = findViewById(R.id.information_gender_icon);
        hashTag = findViewById(R.id.information_tag_icon1);
        toolbar = findViewById(R.id.activity_information_toolbar);

        nickname = findViewById(R.id.information_nickname);
        college = findViewById(R.id.information_college);
        gradYear = findViewById(R.id.information_grad_year);
        fullname = findViewById(R.id.information_fullname);
        age = findViewById(R.id.information_age);

        recyclerView = findViewById(R.id.information_moments);
        momentsTagCount = findViewById(R.id.information_tag_count1);
        likeCountView = findViewById(R.id.information_like_count1);
    }

    /**
     * Set tool bar back action.
     */
    private void setToolbar() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Set user's avatar, username and gender.
     */
    private void setUserInformation()
    {
        firebaseDatabase.getReference().child("User").child(userID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                User user = snapshot.getValue(User.class);
                if(user != null)
                {
                    nickname.setText(user.getNickName());
                    toolbar.setTitle(user.getNickName() + "'s Profile");
                    if(!user.getAvatarURL().isEmpty())
                    {
                        Glide.with(InformationActivity.this).load(user.getAvatarURL()).into(avatar);
                    }
                    else
                    {
                        if(user.gender.equals(Gender.MALE))
                        {
                            avatar.setImageResource(R.drawable.default_avatar_male);
                        }
                        else
                        {
                            avatar.setImageResource(R.drawable.default_avatar_female);
                        }
                    }
                    genderIcon.setVisibility(View.VISIBLE);
                    if(user.gender.equals(Gender.MALE))
                    {
                        genderIcon.setImageResource(R.drawable.ic_male);
                        genderIcon.setColorFilter(Color.rgb(119, 202, 247));
                    }
                    else if(user.gender.equals(Gender.FEMALE))
                    {
                        genderIcon.setImageResource(R.drawable.ic_female);
                        genderIcon.setColorFilter(Color.rgb(235, 171, 184));
                    }
                    else if(user.gender.equals(Gender.TRANS))
                    {
                        genderIcon.setImageResource(R.drawable.ic_transgender);
                    }
                    else
                    {
                        genderIcon.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    /**
     * Set Bio of user.
     */
    private void setUserBio()
    {
        firebaseDatabase.getReference().child("Bio").child(userID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Bio bio = snapshot.getValue(Bio.class);
                if(bio != null)
                {
                    if (bio.getAge() != null) {
                        age.setText("Age: " + bio.getAge());
                    } else {
                        age.setVisibility(View.GONE);
                    }

                    if (bio.getFullName() != null && !bio.getFullName().equals("")) {
                        fullname.setText("Full name: " + bio.getFullName());
                    } else {
                        fullname.setVisibility(View.GONE);
                    }

                    if(!bio.getCollege().name().isEmpty())
                    {
                        college.setText("College: " + bio.getCollege().name());
                    }
                    else
                    {
                        college.setVisibility(View.INVISIBLE);
                    }
                    if(bio.getGradYear() != null)
                    {
                        gradYear.setText("Graduate at " + bio.getGradYear());
                    }
                    else
                    {
                        gradYear.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    /**
     * Display user's timeline
     */
    private void initializeMomentsTimeline()
    {
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        momentAdapter = new MomentAdapter(this, momentsList);
        recyclerView.setAdapter(momentAdapter);
    }

    /**
     * Refresh timeline of moments.
     */
    private void refreshMomentsTimeline()
    {
        firebaseDatabase.getReference().child("Moments").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                momentsList.clear();
                momentIDSet.clear();
                ArrayList<Moment> scheduledUserMoments = new ArrayList<>();
                for(DataSnapshot d : snapshot.getChildren())
                {
                    Moment moment = d.getValue(Moment.class);
                    if(userID.equals(moment.getSenderID()))
                    {
                        if (moment.getVisibility() == null) {
                            if (moment.getSchedule() != null) {
                                if (moment.isScheduleFinished()) {
                                    scheduledUserMoments.add(moment);
                                    momentIDSet.add(moment.getMomentID());
                                }
                            } else {
                                momentsList.add(moment);
                                momentIDSet.add(moment.getMomentID());
                            }
                        }
                    }
                }

                try
                {
                    updateMomentList(momentsList, scheduledUserMoments);
                } catch (ParseException ignored)
                {
                }
                momentAdapter.notifyDataSetChanged();

                momentsTagCount.setText(String.valueOf(momentsList.size()));
                if(momentsList.size() > 0)
                {
                    hashTag.setColorFilter(ContextCompat.getColor(InformationActivity.this, R.color.anu_logo_yellow));
                }
                else
                {
                    hashTag.setColorFilter(ContextCompat.getColor(InformationActivity.this, R.color.default_icon_grey));
                }

               setMomentCounter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    /**
     * Set counter of moment.
     */
    private void setMomentCounter()
    {
        firebaseDatabase.getReference().child("Likes").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                likeCount = 0;
                for(DataSnapshot d : snapshot.getChildren())
                {
                    if(momentIDSet.contains(d.getKey()))
                    {
                        likeCount += d.getChildrenCount();
                    }
                }
                if(likeCount > 0)
                {
                    likeIcon.setImageResource(R.drawable.ic_liked);
                }
                else
                {
                    likeIcon.setImageResource(R.drawable.ic_like);
                }
                likeCountView.setText(String.valueOf(likeCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    /**
     * Update list of moment.
     * @param sortedMoments Sorted moments.
     * @param scheduledMoments Scheduled moments.
     * @throws ParseException Exception when parsing time.
     */
    private void updateMomentList(ArrayList<Moment> sortedMoments, ArrayList<Moment> scheduledMoments) throws ParseException
    {
        for (Moment scheduledMoment : scheduledMoments)
        {
            Date schedule = scheduledMoment.getSchedule().getTime();
            int left = 0;
            int right = sortedMoments.size();
            while (left < right)
            {
                int middle = (left + right) / 2;
                Date sortedMomentTimeStamp = sortedMoments.get(middle).getTimestamp().getTime();
                if (schedule.after(sortedMomentTimeStamp))
                {
                    left = middle + 1;
                } else
                {
                    right = middle;
                }
            }
            sortedMoments.add(left, scheduledMoment);
        }
    }
}