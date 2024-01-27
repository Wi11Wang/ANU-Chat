package wang.bogong.anuchat.LoginActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import wang.bogong.anuchat.MainActivity;
import wang.bogong.anuchat.R;
import wang.bogong.anuchat.RegisterActivities.RegisterActivity;
import wang.bogong.anuchat.RegisterActivities.VerificationActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText email;
    private TextInputLayout emailInputLayout;
    private TextInputEditText password;
    private Button login;
    private ProgressBar progressBar;
    private TextView forgotPassword;
    private TextView notRegistered;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        email = findViewById(R.id.activity_login_uidEditText);
        emailInputLayout = findViewById(R.id.activity_login_uidTextInputLayout);
        password = findViewById(R.id.activity_login_passwordEditText);
        login = findViewById(R.id.activity_login_loginButton);
        progressBar = findViewById(R.id.activity_login_loginProgressBar);
        forgotPassword = findViewById(R.id.activity_login_forgetPasswordTextView);
        notRegistered = findViewById(R.id.activity_login_not_registered);

        auth = FirebaseAuth.getInstance();

        forgotPassword.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        notRegistered.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        login.setEnabled(false);
        getIntentFromRegisterActivity();
        setLoginButtonActions();
        setEmailTextChangeListener();
    }

    private void getIntentFromRegisterActivity() {
        try {
            String uid = getIntent().getStringExtra("uid");
            email.setText(uid);
            login.setEnabled(true);
        } catch (Exception ignored) {}
    }

    /**
     * Set actions of login button.
     */
    private void setLoginButtonActions() {
        login.setOnClickListener(v -> {
            String txt_email = email.getText().toString();
            String txt_password = password.getText().toString();
            login.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                Toast.makeText(LoginActivity.this, "Empty Credentials!", Toast.LENGTH_SHORT).show();
            } else {
                loginUser("u" + txt_email + "@anu.edu.au", txt_password);
            }
        });
    }

    /**
     * Login using user's email password stored in firebase.
     * @param email Email given.
     * @param password Password given.
     */
    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email , password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressBar.setVisibility(View.GONE);
                // If user is verified.
//                if (auth.getCurrentUser().isEmailVerified()) {
                    Intent home = new Intent(LoginActivity.this, MainActivity.class);
                    home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(home);
                    finish();
//                } else {
//                    login.setVisibility(View.VISIBLE);
//                    int id = Integer.parseInt(email.substring(1, 8));
//                    if (id < 100) {
//                        Intent home = new Intent(LoginActivity.this, MainActivity.class);
//                        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(home);
//                        finish();
//                    } else {
//                        showSendEmailDialog();
//                    }
//                }
            } else {
                Toast.makeText(LoginActivity.this,"Invalid email or password",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                login.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Set email text change listener.
     */
    private void setEmailTextChangeListener() {
        // Listen to email edit text.
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {

                // Check if input uid is valid
                if (editable.toString().matches("\\d{7}")) {
                    emailInputLayout.setError(null);
                    login.setEnabled(true);
                } else {
                    emailInputLayout.setError("Not a valid uid");
                    login.setEnabled(false);
                }
            }
        });
    }

    /**
     * Display send email dialog.
     */
    private void showSendEmailDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Account not verified");
        alertDialogBuilder.setMessage(Html.fromHtml("We will send a verification email to \n<b>" + auth.getCurrentUser().getEmail() + "</b>"));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Accept", (dialog, which) -> {
            sendEmail();
        });
        alertDialogBuilder.setNeutralButton("Decline", (dialog, which) -> {
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.anu_logo_yellow));
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this, R.color.alert_red));
    }

    /**
     * Send verification email.
     */
    private void sendEmail() {
        FirebaseUser user = auth.getCurrentUser();
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Register page: sendVerificationEmail", "Successful send verification email to " + user.getEmail());
                startActivity(new Intent(this, VerificationActivity.class));
            }
            progressBar.setVisibility(View.INVISIBLE);
        });
    }
}
