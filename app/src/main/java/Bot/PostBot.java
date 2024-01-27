package Bot;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Moment.Builder.MomentBuilder;

/**
 * @author Bogong Wang
 * @project Android Project
 * @created 18 / 10 / 2021 - 14:47
 */
public class PostBot {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private ArrayList<String> posts = new ArrayList<>();

    private static PostBot instance = null;

    private PostBot() {}

    public static PostBot getInstance() {
        if (instance == null) {
            instance = new PostBot();
        }

        return instance;
    }

    private void getTestTexts(Context context) {
        BufferedReader bufferedReader;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("test_data.csv"), StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    posts.add(line);
                } catch (Exception ignored) {}
            }
            Collections.shuffle(posts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getTags(String str) {
        Pattern MY_PATTERN = Pattern.compile("#([A-Za-z0-9]+)");
        Matcher mat = MY_PATTERN.matcher(str);
        List<String> tags = new ArrayList<>();
        while (mat.find()) {
            //System.out.println(mat.group(1));
            tags.add(mat.group(1));
        }
        return tags;
    }

    public void postToFireBase(Context context, int numOfPosts) {
        getTestTexts(context);

        for (int i = 0; i < numOfPosts; i++) {
            // Set random post user.
            Random rand = new Random();
            final String password = "11111111";

            int id = rand.nextInt(30);
            StringBuilder uid = new StringBuilder();
            for (int j = 0; j < 7 - String.valueOf(id).length(); j++) {
                uid.append("0");
            }
            uid.append(id);

            int finalI = i;
            // Sign in
            auth.signInWithEmailAndPassword("u" + uid.toString() + "@anu.edu.au", password).addOnCompleteListener(task -> {
                // If finished sign in
                if (task.isSuccessful()) {
                    MomentBuilder momentBuilder = MomentBuilder.getInstance();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Moments");
                    String momentID = databaseReference.push().getKey();
                    momentBuilder.setContentID(momentID);
                    momentBuilder.setSenderID(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    String textContent = posts.get(finalI);
                    List<String> tags = getTags(textContent);
                    if (tags.size() != 0) {
                        for (String tag : tags) {
                            FirebaseDatabase.getInstance().getReference().child("Tags").child(tag).child(momentID).setValue(momentID);
                        }
                    }
                    momentBuilder.setTextContent(textContent);

                    momentBuilder.setTimeStamp();

                    Map<String, String> momentMap = momentBuilder.getResult().toMap();
                    databaseReference.child(momentID).setValue(momentMap);
                    int numOfPostsLeft = numOfPosts - finalI - 1;
                    Log.d("Post bot", "Post Bot " + id + ": Post Successful. Moment ID: " + momentID + ". " +  numOfPostsLeft + " left.");
                }
            });
        }
    }
}
