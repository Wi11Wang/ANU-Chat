package wang.bogong.anuchat.RegisterActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import wang.bogong.anuchat.MainActivity;
import wang.bogong.anuchat.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Objects;

import User.Gender;
import User.User;

public class SetProfileActivity extends AppCompatActivity {

    private TextInputEditText nickNameEditText;
    private ShapeableImageView avatarImage;
    private Spinner genderSpinner;

    private Button submitButton;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference databaseReference;
    private StorageReference storageProfilePicsRef;

    private StorageTask uploadTask;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initialize_bio_one);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        mAuth = FirebaseAuth.getInstance();

        avatarImage = findViewById(R.id.activity_set_profile_avatarShapebaleImageView);
        nickNameEditText = findViewById(R.id.activity_set_profile_setNicknameEditText);
        genderSpinner = findViewById(R.id.activity_set_profile_setGenderSpinner);
        submitButton = findViewById(R.id.activity_set_profile_submitButton);

        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference().child("User");
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference("Profiles");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        /**
        click the picture and choose a photo from gallery with pic ratio constraint(1:1)
         */
        avatarImage.setOnClickListener(v -> {
            CropImage.activity().setAspectRatio(1,1).start(SetProfileActivity.this);
        });

        setSubmitButtonActions();
    }

    /**
     * Set action to submit user profile.
     */
    private void setSubmitButtonActions() {
        submitButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(nickNameEditText.toString())) {
                Toast.makeText(getApplicationContext(),"You have not set the nick name yet", Toast.LENGTH_SHORT).show();
            } else {
                uploadProfileImage();
            }
        });
    }

    /**
     * Upload user information to firebase.
     */
    private void uploadUserInfoToFirebase() {
        String nickNameForSet = nickNameEditText.getText().toString();
        String idForSetFB = currentUser.getUid();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail().substring(1, 8);

        String imageUriForSet = "";
        if (imageUri != null) {
            imageUriForSet = imageUri.toString();
        }

        String genderForSetSTR = genderSpinner.getSelectedItem().toString();
        Gender genderForSet = convertStringToGender(genderForSetSTR);
        User user = new User(uid, idForSetFB, imageUriForSet, nickNameForSet, genderForSet);

        databaseReference.child(idForSetFB).setValue(user);

        Intent intent = new Intent(SetProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
        Toast.makeText(getApplicationContext(),"Successfully registered!", Toast.LENGTH_SHORT).show();
    }

    /**
     * convert a gender data in format of string into a Gender
     *
     * @param genderString Gender String
     * @return Correspond gender in Gender class.
     */
    private Gender convertStringToGender(String genderString) {
        if (genderString.equals("Female")) {
            return Gender.FEMALE;
        }
        if (genderString.equals("Male")) {
            return Gender.MALE;
        }
        if (genderString.equals("Transgender")) {
            return Gender.TRANS;
        }
        else {
            return Gender.OTHERS;
        }
    }

    /**
     * crop the selected image and show it on the screen
     *
     * @param requestCode for checking if it is the request code for cropping
     * @param resultCode for checking the result if is ready
     * @param data image selected
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check and upload
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            avatarImage.setImageURI(imageUri);
        }
        // raise error
        else {
            Toast.makeText(this,"Avatar not set", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * TODO: @Xinyi Fix this?
     */
    private void getUserInfo() {
        databaseReference.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    if (snapshot.hasChild("image")) {
                        String image = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                        Picasso.get().load(image).into(avatarImage);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * Get extension of file.
     *
     * @param uri URI to get extension.
     * @return Extension.
     */
    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    /**
     * Upload avatar image to firebase.
     */
    private void uploadProfileImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Set your profile");
        progressDialog.setMessage("Please wait...We are setting your data");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileRef = FirebaseStorage.getInstance().getReference("Profiles").child(System.currentTimeMillis() +"." + getFileExtension(imageUri));
            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = (Uri) task.getResult();
                    imageUri = downloadUri;

                    progressDialog.dismiss();

                    uploadUserInfoToFirebase();
                }
            });
        } else {
            uploadUserInfoToFirebase();
            progressDialog.dismiss();
        }
    }
}
