package wang.bogong.anuchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import wang.bogong.anuchat.LoginActivities.LoginActivity;

import wang.bogong.anuchat.R;

import wang.bogong.anuchat.RegisterActivities.RegisterActivity;


public class StartActivity extends AppCompatActivity {
    private Button signUp;
    private Button login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        setContentView(R.layout.activity_start);

        signUp = (Button) findViewById(R.id.activity_start_registerButton);
        login = (Button) findViewById(R.id.activity_start_loginButton);


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this , RegisterActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this , LoginActivity.class));
            }
        });

    }
}