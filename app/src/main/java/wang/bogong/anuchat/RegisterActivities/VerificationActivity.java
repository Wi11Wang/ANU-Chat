package wang.bogong.anuchat.RegisterActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import wang.bogong.anuchat.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {
    View parentLayout;
    TextView emailTextView;
    ProgressBar progressBar;
    MaterialButton verifiedButton;
    TextView wrongAccountTextView;

    TextView skip;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        linkUIComponents();
        updateUserEmailTextView();
        setVerifiedButtonActions();
        setWrongAccountTextViewActions();

        // For testing.
        setSkipActions();
    }

    private void linkUIComponents() {
        parentLayout = findViewById(R.id.activity_verification_parentLayout);
        emailTextView = findViewById(R.id.activity_verification_emailTextView);
        progressBar = findViewById(R.id.activity_verification_progressBar);
        verifiedButton = findViewById(R.id.activity_verification_verifiedMaterialButton);
        wrongAccountTextView = findViewById(R.id.activity_verification_wrongAccountTextView);

        // For testing.
        skip = findViewById(R.id.activity_verification_skipTextView);
    }

    /**
     * Skip verification step.
     * ONLY FOR TESTING.
     */
    private void setSkipActions() {
        skip.setOnClickListener(view -> {
            startActivity(new Intent(this, SetProfileActivity.class));
        });
    }

    /**
     * Set verification button on click actions.
     */
    private void setVerifiedButtonActions() {
        verifiedButton.setOnClickListener(view -> {
            checkIsUserVerified();
        });
    }

    /**
     * Check if the email is verified.
     */
    private void isEmailVerified() {
        FirebaseUser user = auth.getCurrentUser();
        user.reload().addOnCompleteListener(task -> {
            if (user.isEmailVerified()) {
                startActivity(new Intent(this, SetProfileActivity.class));
            } else {
                verifiedButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Snackbar.make(parentLayout,
                        "You are not verified",
                        5000)
                        .setAction("Send me again", view -> sendEmail())
                        .setActionTextColor(ContextCompat.getColor(this, R.color.sandstone_yellow))
                        .show();
            }
        });
    }

    /**
     * Send verification email to current user.
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

    /**
     * Check if user is verified and adjust display of UI components..
     */
    private void checkIsUserVerified() {
        verifiedButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        isEmailVerified();
    }

    /**
     * Update user Email display text view.
     * Display as uxxxxxx@anu.edu.au
     */
    private void updateUserEmailTextView() {
        emailTextView.setText(auth.getCurrentUser().getEmail());
    }

    /**
     * Set action on clicking wrong account.
     * Delete current account.
     */
    private void setWrongAccountTextViewActions() {
        wrongAccountTextView.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setTitle("Is your email wrong?");
            alertDialogBuilder.setMessage(Html.fromHtml("<em><b>" + auth.getCurrentUser().getEmail() + "</b></em>" + " is not my email.\nI want to register again."));
            alertDialogBuilder.setPositiveButton("Register Again", (dialog, which) -> {
                ProgressBar progressBar = new ProgressBar(this);
                progressBar.setVisibility(View.VISIBLE);
                auth.getCurrentUser().delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, RegisterActivity.class));
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                });
            });
            alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.anu_logo_yellow));
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.anu_logo_yellow));
        });
    }
}