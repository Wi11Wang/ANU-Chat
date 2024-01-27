package wang.bogong.anuchat;

import static User.Gender.FEMALE;
import static User.Gender.getIndex;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
//import com.example.androidproject.Fragments.MeFragment;
//import com.example.androidproject.SignUpActivities.SetProfileActivity;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
import wang.bogong.anuchat.R;
import com.google.android.material.imageview.ShapeableImageView;
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
import com.theartofdev.edmodo.cropper.CropImage;

import User.Gender;
import User.User;
import User.College;
import User.Bio;



public class SettingActivity extends AppCompatActivity {

    private ShapeableImageView avatarImageView;
    private EditText nicknameEditText;
    private EditText ageEditText;
    private EditText fullnameEditText;
    private EditText gradYr;
    private TextView userID;
    private Toolbar toolbar;

    private FirebaseUser currUser;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private Button save;
    private Button EditProfilePhoto;

    private Spinner genderSpinner;
    private Spinner collegeSpinner;

    private Uri imageUri;
    private User userInfo;
    private Bio bioInfo;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_setting);

        getUserInformation();
//        getUserBio();
        linkUIComponents();
        setBackIcon();

        //Button
        save = findViewById(R.id.save_setting_button);
        EditProfilePhoto = findViewById(R.id.EditProfilePhoto);

        //Get reference of the user
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        databaseReference = database.getReference().child("User");
        currUser = FirebaseAuth.getInstance().getCurrentUser();

        EditProfilePhoto.setOnClickListener(v -> {
            CropImage.activity().setAspectRatio(1, 1).start(SettingActivity.this);
        });
        setSaveButton();
    }


    /**
     * Set on click button action to update both
     * User Basic info, User Bio, and upload the edited profile pic
     */
    private void setSaveButton() {
        save.setOnClickListener(v -> {
            updateUser();
            updateBio();
            resetProfileImage();
        });
    }

    /**
     * Some of the UI components for navigation to the requested display position
     */
    protected void linkUIComponents() {

        nicknameEditText = findViewById(R.id.EditNicknameHere);
        fullnameEditText = findViewById(R.id.EditFullNameHere);
        avatarImageView = findViewById(R.id.edit_profile_avatarShapeableImageView);
        ageEditText = findViewById(R.id.InputAge);
        genderSpinner = findViewById(R.id.SelectGender);
        collegeSpinner = findViewById(R.id.SelectCollege);
        gradYr = findViewById(R.id.EditGradYrHere);
        userID = findViewById(R.id.DisplayID);
        toolbar = findViewById(R.id.fragment_setting_toolbar);
    }

    /**
     * Set back icon in the tool bar.
     */
    private void setBackIcon() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    /**
     * This method retrieve the User class data from realtime database and
     * download to the current setting page for reference.
     */
    private void getUserInformation() {
        //read data at our uerID reference under User
        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo = snapshot.getValue(User.class);
                if (userInfo != null) {
                    nicknameEditText.setText(userInfo.getNickName());

                    userID.setText(userInfo.getId());

                    genderSpinner.setSelection(getIndex(userInfo.getGender()));

                    if (!userInfo.getAvatarURL().equals("")) {
                        Glide.with(getApplicationContext()).load(userInfo.getAvatarURL()).into(avatarImageView);
                    } else {
                        if (userInfo.gender.equals(FEMALE)) {
                            avatarImageView.setImageResource(R.drawable.default_avatar_female);
                        } else {
                            avatarImageView.setImageResource(R.drawable.default_avatar_male);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Bio").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bioInfo = snapshot.getValue(Bio.class);
                if (bioInfo != null) {
                    if (!bioInfo.getCollege().name().equals("")) {
                        collegeSpinner.setSelection(College.getIndex(bioInfo.getCollege()));
                    }

                    if (bioInfo.getAge() != null) {
                        ageEditText.setText(bioInfo.getAge());
                    }

                    if (bioInfo.getFullName() != null && !bioInfo.getFullName().equals("")) {
                        fullnameEditText.setText(bioInfo.getFullName());
                    }

                    if (bioInfo.getGradYear() != null) {
                        gradYr.setText(bioInfo.getGradYear());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private Gender convertStringToGender(String genderString) {
        if (genderString.equals("Female")) {
            return Gender.FEMALE;
        }
        if (genderString.equals("Male")) {
            return Gender.MALE;
        }
        if (genderString.equals("Transgender")) {
            return Gender.TRANS;
        } else {
            return Gender.OTHERS;
        }
    }

    private College convertStringToCollege(String collegeString) {
        if (collegeString.equals("College of Arts and Social Sciences")) {
            return College.CASS;
        }
        if (collegeString.equals("College of Asia and the Pacific")) {
            return College.CAP;
        }
        if (collegeString.equals("College of Business and Economics")) {
            return College.CBE;
        }
        if (collegeString.equals("College of Engineering and Computer Science")) {
            return College.CECS;
        }
        if (collegeString.equals("College of Health and Medicine")) {
            return College.CHM;
        }
        if (collegeString.equals("College of Law")) {
            return College.LAW;
        } else {
            return College.SCIENCE;
        }
    }

    /**
     * This method will update the basic user info from the set profile activity.
     */
    private void updateUser() {
        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    String EditNickName = nicknameEditText.getText().toString();
                    String getIDfromFB = currUser.getUid();
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail().substring(1, 8);

                    String imageUriForSet = "";
                    if (imageUri != null) {
                        imageUriForSet = imageUri.toString();
                    }

                    String imageUriNew = user.avatarURL;

                    String genderSetSTR = genderSpinner.getSelectedItem().toString();
                    Gender genderSet = convertStringToGender(genderSetSTR);

                    if (user.getAvatarURL().equals("")) {
                        User user1 = new User(uid, getIDfromFB, imageUriForSet, EditNickName, genderSet);
                        databaseReference.child(getIDfromFB).setValue(user1);
                    }
                    else{
                        User user1 = new User(uid, getIDfromFB, imageUriNew, EditNickName, genderSet);
                        databaseReference.child(getIDfromFB).setValue(user1);
                    }
                }
            }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        this.finish();
        Toast.makeText(getApplicationContext(),"Successfully updated!", Toast.LENGTH_SHORT).show();
    }


    /**
     * This method will create a new bio and update the bio by implementing the bio class.
     * It will get the userID from the User and return the unique bio
     * to a separate path in firebase.
     */
    private void updateBio () {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Bio");

        String userID = currUser.getUid();

        String age = null;
        String fullName = null;
        String graduationYr = null;

        if (bioInfo != null) {
            age = bioInfo.getAge();
            fullName = bioInfo.getFullName();
            graduationYr = bioInfo.getGradYear();
        }


        if (!ageEditText.getText().toString().equals("")) {
             age = ageEditText.getText().toString();
        }

        if (!fullnameEditText.getText().toString().equals("")) {
            fullName = fullnameEditText.getText().toString();
        }

        if (!gradYr.getText().toString().equals("")) {
            graduationYr = gradYr.getText().toString();
        }

        String collegeSetSTR = collegeSpinner.getSelectedItem().toString();
        College collegeSet = convertStringToCollege(collegeSetSTR);

        Bio bio1 = new Bio(userID,age,fullName,collegeSet, graduationYr);
        dbRef.child(userID).setValue(bio1);

    }


    /**
     * This method will display crop action.
     */
    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);

        // check the request code and upload the uri
        if ((request == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
                && (result == RESULT_OK && data != null)) {
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            imageUri = activityResult.getUri();

            //set the on click action to reset the specific uri
            EditProfilePhoto.setOnClickListener(v -> {
               avatarImageView.setImageURI(imageUri);
            });
        } else {
            Toast.makeText(this,"Avatar has not been reset", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * @param uri
     * @return Extension
     * @Reference: SetProfileActivity.getFileExtension
     */
    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }


    /**
     * This function will upload the new avatar image uri to firebase
     * @Reference: SetProfileActivity.uploadProfileImage()
     */
    private void resetProfileImage() {
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
                    updateUser();
                }
            });
        } else {
            updateUser();
        }
    }
}





