package wang.bogong.anuchat.Fragments;


import static User.Gender.FEMALE;
import static User.Gender.MALE;
import static User.Gender.TRANS;

import Adapter.MomentAdapter;
import Moment.Content.Moment;
import User.Gender;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import wang.bogong.anuchat.BotConfigActivity;
import wang.bogong.anuchat.R;
import wang.bogong.anuchat.SettingActivity;
import wang.bogong.anuchat.StartActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import User.User;
import User.Bio;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeFragment#} factory method to
 * create an instance of this fragment.
 *
 */
public class MeFragment extends Fragment {

    private ShapeableImageView avatarImageView;
    private TextView usernameTextView;
    private ImageView genderImageView;

    private TextView likeCountTextView;
    private ImageView likeCountImageView;
    private TextView momentsCountTextView;
    private ImageView momentsCountImageView;

    private TextView collegeNameView;
    private TextView fullnameView;
    private TextView gradView;
    private TextView ageView;
    private DatabaseReference userRef;
    private Toolbar toolbar;

    private LinearLayout topLinearLayout;
    private LinearLayout bottomLinearLayout;

    private FirebaseAuth firebaseAuth;

    private NestedScrollView nestedScrollView;
    private RecyclerView momentsRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private MomentAdapter momentAdapter;
    private List<Moment> momentsList;
    private Set<String> momentIDSet;
    private int likeCount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
        linkUIComponents();
        setToolbarSettingsButtonActions();
        setUserInformation();
        setUserBio();

        initializeMomentsTimeline();
        refreshMomentsTimeline();
        setMomentCounter();

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Link UI components.
     */
    private void linkUIComponents() {
        avatarImageView = getActivity().findViewById(R.id.fragment_me_avatarShapeableImageView);
        usernameTextView = getActivity().findViewById(R.id.fragment_me_userNameTextView);
        genderImageView = getActivity().findViewById(R.id.fragment_me_genderImageView);

        likeCountTextView = getActivity().findViewById(R.id.fragment_me_likeCountTextView);
        likeCountImageView = getActivity().findViewById(R.id.information_like_icon);
        momentsCountTextView = getActivity().findViewById(R.id.fragment_me_momentsCountTextView);
        momentsCountImageView = getActivity().findViewById(R.id.information_tag_icon);

        collegeNameView = getActivity().findViewById(R.id.College_name);
        ageView = getActivity().findViewById(R.id.DisplayMyAge);
        gradView = getActivity().findViewById(R.id.DisplayGradYr);
        fullnameView = getActivity().findViewById(R.id.DisplayMyFullName);
        toolbar = getActivity().findViewById(R.id.fragment_me_toolbar);
        momentsRecyclerView = getActivity().findViewById(R.id.fragment_me_recyclerView);
        topLinearLayout = getActivity().findViewById(R.id.fragment_me_linearLayoutTop);
        bottomLinearLayout = getActivity().findViewById(R.id.fragment_me_linearLayoutBottom);

        topLinearLayout = getActivity().findViewById(R.id.fragment_me_linearLayoutTop);
        bottomLinearLayout = getActivity().findViewById(R.id.fragment_me_linearLayoutBottom);
    }

    /**
     * Set action when clicking on tool bar icons.
     */
    private void setToolbarSettingsButtonActions() {
        toolbar.setOnMenuItemClickListener(item-> {
            if(item.getItemId() == R.id.fragment_me_appbar_setting) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
            if(item.getItemId() == R.id.fragment_me_appbar_logout) {
                firebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), StartActivity.class);
                startActivity(intent);
            }

            if(item.getItemId() == R.id.fragment_me_appbar_bot) {
                Intent intent = new Intent(getActivity(), BotConfigActivity.class);
                startActivity(intent);
            }
            return true;
        });
    }

    /**
     * Set display of user information. Including avatar, nickname and gender.
     */
    private void setUserInformation() {
        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    usernameTextView.setText(user.getNickName());

                    if (!user.getAvatarURL().equals("")) {
                        Glide.with(getContext()).load(user.getAvatarURL()).into(avatarImageView);
                    } else {
                        if (user.gender.equals(FEMALE)) {
                            avatarImageView.setImageResource(R.drawable.default_avatar_female);
                        }
                        else{
                            avatarImageView.setImageResource(R.drawable.default_avatar_male);
                        }
                    }
                    Gender gender = user.gender;
                    genderImageView.setVisibility(View.VISIBLE);
                    if (gender.equals(MALE)) {
                        genderImageView.setImageResource(R.drawable.ic_male);
                        genderImageView.setColorFilter(Color.rgb(119,202,247));
                    } else if (gender.equals(FEMALE)) {
                        genderImageView.setImageResource(R.drawable.ic_female);
                        genderImageView.setColorFilter(Color.rgb(235,171,184));
                    } else if (gender.equals(TRANS)) {
                        genderImageView.setImageResource(R.drawable.ic_transgender);
                    } else {
                        genderImageView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    startActivity(new Intent(getContext(), StartActivity.class));
                    getActivity().finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Set display of user bio information. Including full name, age, college and graduation year.
     */
    private void setUserBio() {
        FirebaseDatabase.getInstance().getReference().child("Bio").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Bio bio = snapshot.getValue(Bio.class);
                if (bio != null) {
                    // Set display of college and gradation year.
                    if (bio.getCollege().name().equals("") && bio.getGradYear() == null) {
                        topLinearLayout.setVisibility(View.GONE);
                    } else {
                        topLinearLayout.setVisibility(View.VISIBLE);
                    }

                    if (!bio.getCollege().name().equals("")) {
                        collegeNameView.setText("College: " + bio.getCollege().name());
                    } else {
                        collegeNameView.setVisibility(View.GONE);
                    }

                    if (bio.getGradYear() != null) {
                        gradView.setText("Graduate at " + bio.getGradYear());
                    } else {
                        gradView.setVisibility(View.GONE);
                    }

                    // Set display of age and full name.
                    if ((bio.getFullName() == null || bio.getFullName().equals("")) && bio.getAge() == null) {
                        bottomLinearLayout.setVisibility(View.GONE);
                    } else {
                        bottomLinearLayout.setVisibility(View.VISIBLE);
                    }

                    if (bio.getAge() != null) {
                        ageView.setText("Age: " + bio.getAge());
                    } else {
                        ageView.setVisibility(View.GONE);
                    }
                    if (bio.getFullName() != null && !bio.getFullName().equals("")) {
                        fullnameView.setText("Full name: " + bio.getFullName());
                    } else {
                        fullnameView.setVisibility(View.GONE);
                    }
                    if (bio.getGradYear() != null) {
                        gradView.setText("Graduate at " + bio.getGradYear());
                    } else {
                        gradView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Initialize moments time line for current user.
     */
    private void initializeMomentsTimeline() {
        momentsRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        momentsRecyclerView.setLayoutManager(linearLayoutManager);

        momentsList = new ArrayList<>();
        momentIDSet = new HashSet<>();
        momentAdapter = new MomentAdapter(getContext(), momentsList);
        momentsRecyclerView.setAdapter(momentAdapter);
    }

    /**
     * Refresh moments timeline.
     */
    private void refreshMomentsTimeline() {
        FirebaseDatabase.getInstance().getReference().child("Moments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                momentsList.clear();
                momentIDSet.clear();
                ArrayList<Moment> scheduledCurrentUserMoments = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Moment moment = dataSnapshot.getValue(Moment.class);
                    String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        if (currentUserID.equals(moment.getSenderID())) {
                            momentIDSet.add(moment.getMomentID());
                            if (moment.getSchedule() != null) {
                                    scheduledCurrentUserMoments.add(moment);
                            } else {
                                    momentsList.add(moment);
                            }
                        }
                }

                try {
                    updateMomentsList((ArrayList<Moment>) momentsList, scheduledCurrentUserMoments);
                } catch (ParseException ignored) {}

                momentAdapter.notifyDataSetChanged();

                momentsCountTextView.setText(String.valueOf(momentsList.size()));
                if (momentsList.size() > 0) {
                    momentsCountImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.anu_logo_yellow));
                }

                setMomentCounter();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Set the display of moment counter.
     */
    public void setMomentCounter() {
        FirebaseDatabase.getInstance().getReference().child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likeCount = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (momentIDSet.contains(dataSnapshot.getKey())) {
                        likeCount += dataSnapshot.getChildrenCount();
                    }
                }
                if (likeCount > 0) {
                    likeCountImageView.setImageResource(R.drawable.ic_liked);
                } else {
                    likeCountImageView.setImageResource(R.drawable.ic_like);
                }
                likeCountTextView.setText(String.valueOf(likeCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Adding scheduled moments to moments list.
     *
     * @param scheduledMoments Scheduled moments.
     * @throws ParseException Error when parsing time.
     */
    private void updateMomentsList(ArrayList<Moment> sortedMoments, ArrayList<Moment> scheduledMoments) throws ParseException {
        for (Moment scheduledMoment : scheduledMoments) {
            Date schedule = scheduledMoment.getSchedule().getTime();
            int left = 0;
            int right = sortedMoments.size();
            while (left < right) {
                int middle = (left + right) / 2;
                Date sortedMomentTimeStamp = sortedMoments.get(middle).getTimestamp().getTime();
                if (schedule.after(sortedMomentTimeStamp)) {
                    left = middle + 1;
                } else {
                    right = middle;
                }
            }
            sortedMoments.add(left, scheduledMoment);
        }
    }
}