package wang.bogong.anuchat.RegisterActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import wang.bogong.anuchat.LoginActivities.LoginActivity;
import wang.bogong.anuchat.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {
    private View parentLayout;

    private TextInputEditText email;
    private TextInputLayout emailInputLayout;

    private TextInputEditText password;
    private TextInputLayout passwordInputLayout;

    private TextInputEditText passwordConfirm;
    private TextInputLayout passwordConfirmInputLayout;

    private Button registerButton;
    private ProgressBar progressBar;        // Invisible on start

    private FirebaseAuth auth;

    private final boolean isDebugMode = false;
    // Enter your email and password here, this is for testing.
    private final String testEmailAddress = "test@test.test";
    private final String testEmailPassword = "12345678";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        // Instantiate firebase authentication module.
        auth = FirebaseAuth.getInstance();
        auth.signOut();

        linkUIComponents();

        if (isDebugMode) {
            registerUser("", "");
        }

        setEmailTextChangeListener();

        setPasswordTextChangeListener();
        setPasswordConfirmTextChangeListener();

        setRegisterButtonActions();
    }

    /**
     * Link UI components.
     */
    private void linkUIComponents() {
        parentLayout = findViewById(R.id.registerPage_parentLayout);

        // Bind registerEmail EditText with email.
        email = findViewById(R.id.registerPage_registerEmail);
        email.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_semibold_file));
        emailInputLayout = findViewById(R.id.registerPage_emailLayout);

        // Bind registerPassword and registerPasswordConfirmed EditText with email.
        password = findViewById(R.id.registerPage_registerPassword);
        passwordInputLayout = findViewById(R.id.registerPage_registerPasswordLayout);
        passwordConfirm = findViewById(R.id.registerPage_registerPasswordConfirm);
        passwordConfirm.setEnabled(false);
        passwordConfirmInputLayout = findViewById(R.id.registerPage_registerPasswordConfirmLayout);

        // Bind register Button View with register.
        registerButton = findViewById(R.id.registerPage_registerInRegisterPageButton);
        // Disable register button.
        registerButton.setText(R.string.uid_and_password_not_set);
        registerButton.setEnabled(false);

        TextView signInTextView = findViewById(R.id.registerPage_signIn);
        signInTextView.setOnClickListener(view -> startActivity(new Intent(this, LoginActivity.class)));

        // Bind progressBar View with progressBar.
        progressBar = findViewById(R.id.registerPage_registerWaitingProgressBar);
        // When initialize the current activity, hide the progress bar.
        progressBar.setVisibility(View.INVISIBLE);
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
                changeRegisterButtonHint();
                // Check if input uid is valid
                if (editable.toString().matches("\\d{7}")) {
                    emailInputLayout.setError(null);
                    emailInputLayout.setHelperText("u" + editable.toString() + "@anu.edu.au will be your account");
                } else {
                    emailInputLayout.setError("Not a valid uid");
                }
            }
        });
    }

    /**
     * Set password text change listener.
     */
    private void setPasswordTextChangeListener() {
        // Listen to password edit text.
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                changeRegisterButtonHint();
                // Check if input uid is valid
                if (editable.toString().length() < 8) {
                    passwordInputLayout.setError("Password should contain 8 or more characters.");
                } else {
                    // If the confirmation password doesn't match with original password, show error.
                    if (!Objects.requireNonNull(passwordConfirm.getText()).toString().equals("")
                            && !editable.toString().equals(Objects.requireNonNull(passwordConfirm.getText()).toString())) {
                        passwordConfirmInputLayout.setError("The passwords you entered do not match.");
                    } else {
                        passwordConfirmInputLayout.setError(null);
                    }
                    passwordConfirm.setEnabled(true);
                    passwordInputLayout.setError(null);
                }
            }
        });
    }

    /**
     * Set password confirm text change listener.
     */
    private void setPasswordConfirmTextChangeListener() {
        // Listen to confirmation password edit text.
        passwordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                changeRegisterButtonHint();
                // Check if confirmation password is equal to password.
                if (!editable.toString().equals(Objects.requireNonNull(password.getText()).toString())) {
                    passwordConfirmInputLayout.setError("The passwords you entered do not match.");
                } else {
                    // If confirmation password has at least 8 arbitrary chars, it's valid.
                    if (editable.toString().length() > 7) {
                        passwordConfirmInputLayout.setError(null);
                    } else {
                        // Else show error.
                        passwordConfirmInputLayout.setError("Password should contain 8 or more characters.");
                    }
                }
            }
        });
    }

    /**
     * Set actions of register button.
     */
    private void setRegisterButtonActions() {
        registerButton.setOnClickListener(view -> {
            if (registerButton.getText().equals(getResources().getString(R.string.continue_to_verify))) {
                startActivity(new Intent(this, VerificationActivity.class));
                this.finish();
            } else {
                registerNow();
            }
        });
    }

    /**
     * Packaging Register behaviour. Performs register method.
     * <p>
     * This method is used by {@link RegisterActivity#onCreate(Bundle)}
     * and {@link RegisterActivity#registerUser(String, String)} for retrying.
     */
    private void registerNow() {
        /* When clicking the register button,
         * 1. freeze the email and password input box
         * 2. hide the register button
         * 3. show progress bar */
        email.setEnabled(false);
        passwordInputLayout.setEnabled(false);
        passwordConfirmInputLayout.setEnabled(false);
        registerButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        String emailText = "u" + Objects.requireNonNull(email.getText()).toString() + "@anu.edu.au";
        String passwordText = Objects.requireNonNull(password.getText()).toString();
        registerUser(emailText, passwordText);
    }

    /**
     * Based on current state of email, password and confirm password, show different
     * text on register button.
     * <p>
     * Once all requirements meets, enable the register bar.
     *
     * @return ture is we can register.
     */
    private boolean changeRegisterButtonHint() {
        String emailText = Objects.requireNonNull(email.getText()).toString();
        String passwordText = Objects.requireNonNull(password.getText()).toString();
        String passwordConfirmText = Objects.requireNonNull(passwordConfirm.getText()).toString();

        registerButton.setEnabled(false);
        if (emailText.equals("") && passwordText.equals("")) {
            /* If both email and password are not set:
             * Show: uid and password not set. */
            registerButton.setText(R.string.uid_and_password_not_set);
        } else if (emailText.equals("")){
            /* If only set password:
             * Show: uid not set*/
            registerButton.setText(R.string.uid_not_set);
        } else if (passwordText.equals("")) {
            /* If only set uid:
             * Show: password not set*/
            registerButton.setText(R.string.password_not_set);
        } else {
            if (!passwordText.equals(passwordConfirmText)) {
                /* If password and confirm password not set:
                 * Show: password not confirmed */
                registerButton.setText(R.string.password_not_confirmed);
            } else {
                if (!isValidUid(emailText)) {
                    /* If uid not valid:
                     * Show: uid not valid */
                    registerButton.setText(R.string.uid_not_valid);
                } else if (!isValidPassword(passwordText)){
                    /* If password not valid:
                     * Show: password not valid */
                    registerButton.setText(R.string.password_not_valid);
                } else {
                    /* If all requirements meets:
                     * Show: Join ANU Chat! */
                    registerButton.setText(R.string.join_anu_chat);
                    registerButton.setEnabled(true);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if a given uid id number is valid. (7-digits number)
     *
     * @param uid id number with out "u". e.g. 1234567 is valid, u1234567 is not
     * @return true if uid is a 7-digits number
     */
    private boolean isValidUid(String uid) {
        return uid.matches("\\d{7}");
    }

    /**
     * Check if a password is valid.
     *
     * @param password Password to be verified.
     * @return True if password has 8 or more characters.
     */
    private boolean isValidPassword(String password) {
        return password.length() > 7;
    }


    /**
     * Register a user to Firebase.
     * <p>
     * This method will do following things:
     * <ol start="0">
     *     <li>
     *         [OPTIONAL] Check if we're in debug mode.
     *         <p>If we're: set test email and test password.</p>
     *     </li>
     *     <li>Check if we can register with given email and password.</li>
     *     <li>Block the input box.</li>
     *     <ul>
     *         <li>
     *             If we can:
     *             <p>Jump to verification activity.</p>
     *         </li>
     *         <li>
     *             If we can't:
     *             <p>1. Enable input box and button.</p>
     *             <p>2. Pop up error message.</p>
     *         </li>
     *     </ol>
     * </ol>
     *
     * @param email Email address, in string format.
     * @param password Password, in string format.
     */
    private void registerUser(String email, String password) {
        if (isDebugMode) {
            email = testEmailAddress;
            password = testEmailPassword;
        }

        String finalEmail = email;
        // Create a user via firebase.
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                task -> {
            /* If successfully created a user:
             * 1. Show SnackBar to notify user. (Disabled)
             * 2. Hide progress bar.
             * 3. Jump to next activity.
             */
            if (task.isSuccessful()) {
                progressBar.setVisibility(View.INVISIBLE);
                registerButton.setVisibility(View.VISIBLE);
                this.email.setEnabled(true);
                this.passwordInputLayout.setEnabled(true);
                this.passwordConfirmInputLayout.setEnabled(true);
                showSendEmailDialog();
            /* If fails:
             * 1. Show register button.
             * 2. Hide progress bar.
             * 3. Enable edit texts.
             * 4. Show snack bar to notify user.
             */
            } else {
                String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                if (Objects.equals(errorMessage, "The email address is already in use by another account.")) {
                    Snackbar.make(parentLayout,
                            "This account is registered.",
                            8000)
                            .setAction("Sign In", view -> {
                                // Jump to login activity
                                Intent jumpToLoginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
                                jumpToLoginActivity.putExtra("uid", finalEmail.substring(1,8));
                                startActivity(jumpToLoginActivity);
                                // Auto add account.
                            })
                            .setActionTextColor(ContextCompat.getColor(this, R.color.sandstone_yellow))
                            .show();
                } else {
                    Snackbar.make(parentLayout,
                            "Fail to join ANU Chat.",
                            8000)
                            .setAction("Retry", view -> {
                                boolean areAllInputsValid = changeRegisterButtonHint();
                                if (areAllInputsValid) {
                                    registerNow();
                                }
                            })
                            .setActionTextColor(ContextCompat.getColor(this, R.color.sandstone_yellow))
                            .show();
                }

                progressBar.setVisibility(View.INVISIBLE);

                registerButton.setVisibility(View.VISIBLE);

                this.email.setEnabled(true);
                this.passwordInputLayout.setEnabled(true);
                this.passwordConfirmInputLayout.setEnabled(true);
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
     * Display send email dialog.
     */
    private void showSendEmailDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Send verification email");
        alertDialogBuilder.setMessage(Html.fromHtml("We will send a verification email to \n<b>" + auth.getCurrentUser().getEmail() + "</b>"));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Accept", (dialog, which) -> {
            sendEmail();
        });
        alertDialogBuilder.setNeutralButton("Decline", (dialog, which) -> {
            auth.getCurrentUser().delete();
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.anu_logo_yellow));
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this, R.color.alert_red));
    }
}