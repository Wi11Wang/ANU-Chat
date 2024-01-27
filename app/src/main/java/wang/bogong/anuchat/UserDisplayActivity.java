package wang.bogong.anuchat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wang.bogong.anuchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import Adapter.ContactAdapter;
import User.User;

public class UserDisplayActivity extends AppCompatActivity {
    String content;

    ArrayList<User> mutualFriends = new ArrayList<>();
    ArrayList<User> following = new ArrayList<>();
    ArrayList<User> follower = new ArrayList<>();

    ContactAdapter mutualAdapter;
    ContactAdapter followingAdapter;
    ContactAdapter followerAdapter;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    RecyclerView recyclerView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_display);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        content = getIntent().getStringExtra("content");

        linkUIComponents();
        setBackIcon();

        displayFriends();
        updateContact();
    }

    private void linkUIComponents() {
        recyclerView = findViewById(R.id.activity_user_display_recyclerView);
        toolbar = findViewById(R.id.activity_user_display_toolbar);
    }

    /**
     * Set back icon in the tool bar.
     */
    private void setBackIcon() {
        if (content.equals("following")) {
            toolbar.setTitle("Following");
        } else {
            toolbar.setTitle("Follower");
        }

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    /**
     * Display friends.
     */
    private void displayFriends() {
        mutualAdapter = new ContactAdapter(mutualFriends, UserDisplayActivity.this);
        followingAdapter = new ContactAdapter(following, UserDisplayActivity.this);
        followerAdapter = new ContactAdapter(follower, UserDisplayActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UserDisplayActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        if (content.equals("following")) {
            recyclerView.setAdapter(followingAdapter);
        } else {
            recyclerView.setAdapter(followerAdapter);
        }
    }

    /**
     * Update contacts.
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
                                        followerAdapter.notifyDataSetChanged();
                                        followingAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error)
                                    {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
            }
        });
    }
}