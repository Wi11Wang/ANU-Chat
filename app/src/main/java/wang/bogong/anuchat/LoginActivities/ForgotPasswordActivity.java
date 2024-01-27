package wang.bogong.anuchat.LoginActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import wang.bogong.anuchat.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText email;
    private TextInputLayout emailInputLayout;
    private MaterialButton reset;
    private ProgressBar progressBar;

    FirebaseAuth auth = FirebaseAuth.getInstance();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        email = findViewById(R.id.activity_forgot_password_resetPasswordTextInputEditText);
        emailInputLayout = findViewById(R.id.activity_forgot_password_resetPasswordTextInputLayout);
        reset = findViewById(R.id.activity_forgot_password_resetPasswordButton);

        setEmailTextChangeListener();
        reset.setEnabled(false);
        /**
        reset password when clicking the reset button
         */
        reset.setOnClickListener(view -> {
            resetPassword();
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
                if (editable.toString().matches("u\\d{7}@anu\\.edu\\.au")) {
                    emailInputLayout.setError(null);
                    reset.setEnabled(true);
                } else {
                    emailInputLayout.setError("Not a valid uid");
                    reset.setEnabled(false);
                }
            }
        });
    }

    /**
     * reset user's password with an registered email
     */
    private void resetPassword() {
        String emailUser = email.getText().toString().trim();

        /**
        check if the email is filled
         */
        if (emailUser.isEmpty()) {
            Toast.makeText(ForgotPasswordActivity.this,"Please enter your email",Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        }

        /**
        check if the email is valid
         */
        if (!Patterns.EMAIL_ADDRESS.matcher(emailUser).matches()) {
            Toast.makeText(ForgotPasswordActivity.this,"Email is not valid",Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        }
        Toast.makeText(ForgotPasswordActivity.this,"Check your email to reset the password",Toast.LENGTH_SHORT).show();

        /**
        connect to firebase to reset user's password
         */
        auth.sendPasswordResetEmail(emailUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ForgotPasswordActivity.this,"Check your email to reset the password",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                finish();
            }
            else {
                Toast.makeText(ForgotPasswordActivity.this,"Try again",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
