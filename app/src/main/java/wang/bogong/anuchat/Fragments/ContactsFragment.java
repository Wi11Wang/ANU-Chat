package wang.bogong.anuchat.Fragments;

import static User.Gender.FEMALE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import wang.bogong.anuchat.R;
import wang.bogong.anuchat.UserDisplayActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import Adapter.ContactAdapter;
import User.User;

public class ContactsFragment extends Fragment {

    CardView followingCardView;
    CardView followerCardView;
    ImageView followingAvatar;
    ImageView followerAvatar;
    RecyclerView friendsRecyclerView;

    ArrayList<User> mutualFriends = new ArrayList<>();
    ArrayList<User> following = new ArrayList<>();
    ArrayList<User> follower = new ArrayList<>();
    ContactAdapter mutualAdapter;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        linkUIComponents();
        setCardClickActions();
        displayFriends();
        updateContact();
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Link UI Components.
     */
    private void linkUIComponents() {
        followingCardView = getActivity().findViewById(R.id.fragment_contacts_followingCardView);
        followerCardView = getActivity().findViewById(R.id.fragment_contacts_followersCardView);
        followerAvatar = getActivity().findViewById(R.id.fragment_contacts_followerAvatar);
        followingAvatar = getActivity().findViewById(R.id.fragment_contacts_followingAvatar);

        friendsRecyclerView = getActivity().findViewById(R.id.fragment_contacts_recyclerView);
    }

    /**
     * Set action when click on follower card and following card.
     */
    private void setCardClickActions() {
        followerCardView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserDisplayActivity.class);
            intent.putExtra("content", "follower");

            startActivity(intent);
        });

        followingCardView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserDisplayActivity.class);
            intent.putExtra("content", "following");

            startActivity(intent);
        });
    }

    /**
     * Set the recycler view for displaying firends.
     */
    private void displayFriends() {
        mutualAdapter = new ContactAdapter(mutualFriends, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        friendsRecyclerView.setLayoutManager(linearLayoutManager);
        friendsRecyclerView.setAdapter(mutualAdapter);
    }

    /**
     * Update contact information. Including follower, following and friends.
     */
    public void updateContact()
    {
        Set<String> mutualSet = new HashSet<>();
        Set<String> followingSet = new HashSet<>();
        Set<String> followerSet = new HashSet<>();
        Set<String> tempFollowerSet = new HashSet<>();

        firebaseDatabase.getReference().child("Follower").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                followerSet.clear();
                for(DataSnapshot d : snapshot.getChildren())
                {
                    followerSet.add(d.getKey());
                }

                firebaseDatabase.getReference().child("Following")
                        .child(FirebaseAuth.getInstance().getUid())
                        .addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                followingSet.clear();
                                for(DataSnapshot d : snapshot.getChildren())
                                {
                                    followingSet.add(d.getKey());
                                }
                                mutualSet.clear();
                                tempFollowerSet.clear();
                                tempFollowerSet.addAll(followerSet);
                                followerSet.retainAll(followingSet);
                                mutualSet.addAll(followerSet);

                                firebaseDatabase.getReference().child("User").addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {
                                        mutualFriends.clear();
                                        following.clear();
                                        follower.clear();
                                        for(DataSnapshot d : snapshot.getChildren())
                                        {
                                            if(mutualSet.contains(d.getKey()))
                                            {
                                                mutualFriends.add(d.getValue(User.class));
                                            }
                                            if(tempFollowerSet.contains(d.getKey()))
                                            {
                                                follower.add(d.getValue(User.class));
                                            }
                                            if(followingSet.contains(d.getKey()))
                                            {
                                                following.add(d.getValue(User.class));
                                            }
                                        }
                                        mutualAdapter.notifyDataSetChanged();

                                        if (followingSet.size() == 0) {
                                            followingCardView.setVisibility(View.GONE);
                                        } else {
                                            followingCardView.setVisibility(View.VISIBLE);
                                            User user = following.get(new Random().nextInt(following.size()));
                                            if (user.getAvatarURL().equals("")) {
                                                if (user.gender.equals(FEMALE)) {
                                                    followerAvatar.setImageResource(R.drawable.default_avatar_female);
                                                }
                                                else{
                                                    followerAvatar.setImageResource(R.drawable.default_avatar_male);
                                                }
                                            } else {
                                                Glide.with(getContext()).load(user.getAvatarURL()).into(followingAvatar);
                                            }
                                        }

                                        if (followerSet.size() == 0) {
                                            followerCardView.setVisibility(View.GONE);
                                        } else {
                                            followerCardView.setVisibility(View.VISIBLE);
                                            User user = follower.get(new Random().nextInt(follower.size()));
                                            if (user.getAvatarURL().equals("")) {
                                                if (user.gender.equals(FEMALE)) {
                                                    followingAvatar.setImageResource(R.drawable.default_avatar_female);
                                                }
                                                else{
                                                    followingAvatar.setImageResource(R.drawable.default_avatar_male);
                                                }
                                            } else {
                                                Glide.with(getContext()).load(user.getAvatarURL()).into(followingAvatar);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error)
                                    {}
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {}
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
                });
    }
}