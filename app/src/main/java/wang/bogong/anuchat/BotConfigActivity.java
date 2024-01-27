package wang.bogong.anuchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import wang.bogong.anuchat.R;

import Bot.CreateBot;
import Bot.LikeBot;
import Bot.PostBot;

public class BotConfigActivity extends AppCompatActivity {
    EditText createBotEditText;
    Button createBotButton;

    Button postBotButton;
    EditText postBotEditText;

    Button likeBotButton;
    EditText likeBotEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_config);

        linkUIComponents();

        setCreateBotButton();
        setPostBotButton();
        setLikeBotButton();
    }


    private void linkUIComponents() {
        createBotButton = findViewById(R.id.activity_bot_config_createBotButton);
        createBotEditText = findViewById(R.id.activity_bot_config_createBotEditText);

        postBotButton = findViewById(R.id.activity_bot_config_postBotButton);
        postBotEditText = findViewById(R.id.activity_bot_config_postBotEditText);

        likeBotButton = findViewById(R.id.activity_bot_config_likeBotButton);
        likeBotEditText = findViewById(R.id.activity_bot_config_likeBotEditText);
    }

    private void setCreateBotButton() {
        createBotButton.setOnClickListener(v -> {
            setBot();
        });
    }

    private void setPostBotButton() {
        postBotButton.setOnClickListener(v -> {
            generatePosts();
        });
    }

    private void setLikeBotButton() {
        likeBotButton.setOnClickListener(v -> {
            setLikes();
        });
    }

    private void setBot() {
        CreateBot createBot = CreateBot.getInstance();
        createBot.createUser(Integer.parseInt(createBotEditText.getText().toString()));
    }

    private void generatePosts() {
        PostBot postBot = PostBot.getInstance();
        postBot.postToFireBase(this, Integer.parseInt(postBotEditText.getText().toString()));
    }

    private void setLikes() {
        LikeBot likeBot = LikeBot.getInstance();
        likeBot.like(Integer.parseInt(likeBotEditText.getText().toString()));
    }
}