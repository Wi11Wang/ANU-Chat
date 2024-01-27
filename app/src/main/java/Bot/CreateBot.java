package Bot;

import static User.Gender.FEMALE;
import static User.Gender.OTHERS;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

import User.User;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 18 / 10 / 2021 - 15:03
 */
public class CreateBot {
    FirebaseAuth auth = FirebaseAuth.getInstance();

    private static CreateBot instance = null;

    private CreateBot() {}

    public static CreateBot getInstance() {
        if (instance == null) {
            instance = new CreateBot();
        }

        return instance;
    }

    public ArrayList<String> createUser(int numberOfUsers) {
        ArrayList<String> createdUsers = new ArrayList<>();

        final String password = "11111111";

        for (int i = 0; i < numberOfUsers; i++) {
            StringBuilder uid = new StringBuilder();
            for (int j = 0; j < 7 - String.valueOf(i).length(); j++) {
                uid.append("0");
            }
            uid.append(i);
            int finalI = i;
            auth.createUserWithEmailAndPassword("u" + uid.toString() + "@anu.edu.au", password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    createdUsers.add(uid.toString());

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
                    String firebaseID = auth.getUid();
                    User user = new User(uid.toString(), firebaseID, "", "testBot" + finalI, OTHERS);
                    databaseReference.child(firebaseID).setValue(user).addOnCompleteListener(task1 -> {
                        createdUsers.add(uid.toString());
                    });
                }
            });
        }

        return createdUsers;
    }
}