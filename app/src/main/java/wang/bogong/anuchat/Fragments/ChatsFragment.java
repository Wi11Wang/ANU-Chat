package wang.bogong.anuchat.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import wang.bogong.anuchat.databinding.FragmentChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import Adapter.UserAdapter;
import User.User;

public class ChatsFragment extends Fragment {

    ArrayList<User> list = new ArrayList<>();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FragmentChatsBinding binding;
    Set<String> mutualFriends = new HashSet<>();
    UserAdapter userAdapter;

    public ChatsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        userAdapter = new UserAdapter(list, getContext());
        binding.chatListRecylerView.setAdapter(userAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.chatListRecylerView.setLayoutManager(layoutManager);

        updateMutualFriends();

        return binding.getRoot();
    }

    /**
     * Update mutual friends.
     */
    public void updateMutualFriends()
    {
        Set<String> follower = new HashSet<>();
        Set<String> following = new HashSet<>();
        firebaseDatabase.getReference().child("Follower").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        follower.clear();
                        for (DataSnapshot d : snapshot.getChildren())
                        {
                            follower.add(d.getKey());
                        }

                        firebaseDatabase.getReference().child("Following")
                                .child(FirebaseAuth.getInstance().getUid())
                                .addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {
                                        following.clear();
                                        for (DataSnapshot d : snapshot.getChildren())
                                        {
                                            following.add(d.getKey());
                                        }
                                        mutualFriends.clear();
                                        follower.retainAll(following);
                                        mutualFriends.addAll(follower);

                                        firebaseDatabase.getReference().child("User")
                                                .addValueEventListener(new ValueEventListener()
                                                {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                                    {
                                                        list.clear();
                                                        for (DataSnapshot d : snapshot.getChildren())
                                                        {
                                                            if (mutualFriends.contains(d.getKey()))
                                                            {
                                                                list.add(d.getValue(User.class));
                                                            }
                                                        }
                                                        userAdapter.notifyDataSetChanged();
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