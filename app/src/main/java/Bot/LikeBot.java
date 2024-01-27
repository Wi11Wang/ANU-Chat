package Bot;

import android.util.Log;

import androidx.annotation.NonNull;

import wang.bogong.anuchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Moment.Content.Moment;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 18 / 10 / 2021 - 15:03
 */
public class LikeBot {
    FirebaseAuth auth = FirebaseAuth.getInstance();

    private static LikeBot instance = null;

    private LikeBot() {}

    public static LikeBot getInstance() {
        if (instance == null) {
            instance = new LikeBot();
        }

        return instance;
    }

    public void like(int numberOfLikes) {
        FirebaseUser firebaseUser = auth.getCurrentUser();

        List<String> momentIDs = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Moments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        Moment moment = dataSnapshot.getValue(Moment.class);
                        if (moment.isScheduleFinished()) {
                            momentIDs.add(moment.getMomentID());
                            Collections.shuffle(momentIDs);
                        }
                    } catch (Exception ignored) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        for (int j = 0; j < 30; j++) {
            // Set random like user.
            Random rand = new Random();
            final String password = "11111111";

            int id = rand.nextInt(30);
            StringBuilder uid = new StringBuilder();
            for (int k = 0; k < 7 - String.valueOf(id).length(); k++) {
                uid.append("0");
            }
            uid.append(id);

            // Sign in
            auth.signInWithEmailAndPassword("u" + uid.toString() + "@anu.edu.au", password).addOnCompleteListener(task -> {

                for (int i = 0; i < Math.min(numberOfLikes, momentIDs.size()); i++) {

                    String momentID = momentIDs.get(i);
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(momentID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.child(firebaseUser.getUid()).exists()) {
                                FirebaseDatabase.getInstance().getReference().child("Likes").child(momentID).child(firebaseUser.getUid()).setValue(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            });
        }
    }
}
