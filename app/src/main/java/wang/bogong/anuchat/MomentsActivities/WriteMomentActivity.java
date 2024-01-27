package wang.bogong.anuchat.MomentsActivities;

import static android.text.format.DateFormat.is24HourFormat;
import static java.util.Calendar.DAY_OF_YEAR;
import static Moment.Builder.ContentBuilder.NOT_DEFINED_STRING_VALUE;
import static Moment.Builder.MomentBuilder.FRIENDS;
import static Moment.Builder.MomentBuilder.PRIVATE;
import static Moment.Builder.MomentBuilder.PUBLIC;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.bumptech.glide.Glide;
import wang.bogong.anuchat.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.platform.MaterialArcMotion;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Moment.Builder.MomentBuilder;

public class WriteMomentActivity extends AppCompatActivity {

    private static final String MOMENTID = "momentID";
    private static final String TAG = "tag";
    private final int REQUEST_IMAGE = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;

    private MaterialButton cancelButton;
    private MaterialButton postButton;

    private ImageView selectedImageView;
    private Uri selectedImageUri;
    private ImageButton removeImageButton;
    private SocialAutoCompleteTextView textContentSocialAutoCompleteTextView;

    private ImageButton setLocationImageButton;
    private ImageButton setScheduleImageButton;
    private ImageButton setVisibilityImageButton;
    private ImageButton addImageImageButton;

    private ProgressBar loadingLocationProgressBar;

    private TextView locationDisplayTextView;
    private TextView scheduleDisplayTextView;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private MomentBuilder momentBuilder = MomentBuilder.getInstance();
    private String momentImageUrl;

    boolean isScheduled = false;
    Calendar scheduleCalender;

    String momentID;
    String tag;

    // Deprecated components.
    ImageButton addHashTagImageButton;
    ImageButton addAtImageButton;
    ImageButton takePhotoImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable transition animation.
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_moment);

        scheduleCalender = Calendar.getInstance();

        // Set up animations.
        animationSetup();
        locationServiceSetup();
        linkUIComponents();

        setPostButtonActions();
        setCancelButtonActions();

        setSetLocationImageButtonActions();
        setSetScheduleImageButtonActions();
        setScheduleDisplayTextViewActions();
        setSetVisibilityButtonActions();
        setAddImageImageButtonActions();

        setRemoveImageButtonActions();
    }

    //**************************************************************************
    //                            INITIALIZATION                               *
    //**************************************************************************
    /**
     * Setup floating action button animation with write moments activity.
     */
    private void animationSetup() {
        findViewById(R.id.activity_write_moment_background).setTransitionName("fragment_moments_floatingActionButton");
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());

        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.addTarget(R.id.activity_write_moment_background);
        transform.setDuration(300);
        transform.setAllContainerColors(ContextCompat.getColor(this, R.color.white));
        transform.setStartShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(80));
        transform.setEndShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(80));
        transform.setPathMotion(new MaterialArcMotion());
        transform.setInterpolator(new FastOutSlowInInterpolator());
        transform.setEndElevation(3);

        getWindow().setSharedElementEnterTransition(transform);
        getWindow().setSharedElementReturnTransition(transform);
    }

    /**
     * Set up location service.
     */
    private void locationServiceSetup() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Link UI components.
     */
    private void linkUIComponents() {
        cancelButton = findViewById(R.id.activity_write_moment_cancel_button);
        postButton = findViewById(R.id.activity_write_moment_post_button);

        textContentSocialAutoCompleteTextView = findViewById(R.id.activity_write_moment_socialTextView);
        selectedImageView = findViewById(R.id.activity_write_moment_selectedImageImageView);
        removeImageButton = findViewById(R.id.activity_write_moment_removeImageButton);

        setLocationImageButton = findViewById(R.id.activity_write_moment_setLocationImageButton);
        setScheduleImageButton = findViewById(R.id.activity_write_moment_setScheduleImageButton);
        setVisibilityImageButton = findViewById(R.id.activity_write_moment_setVisibilityImageButton);
        addImageImageButton = findViewById(R.id.activity_write_moment_addImageImageButton);

        loadingLocationProgressBar = findViewById(R.id.activity_write_moment_loadingLocationProgressBar);

        locationDisplayTextView = findViewById(R.id.activity_write_moment_locationDisplayTextView);
        scheduleDisplayTextView = findViewById(R.id.activity_write_moment_scheduledDisplayTextView);

        // Deprecated components.
        addHashTagImageButton = findViewById(R.id.activity_write_moment_addHashTagImageButton);
        addAtImageButton = findViewById(R.id.activity_write_moment_addAtImageButton);
        takePhotoImageButton = findViewById(R.id.activity_write_moment_takePhotoImageButton);
    }

    //**************************************************************************
    //                         SET BUTTON ACTIONS                              *
    //**************************************************************************
    /**
     * Set actions of post button.
     */
    private void setPostButtonActions() {
        // TODO: set post button
        postButton.setOnClickListener(view -> {
            if (!textContentSocialAutoCompleteTextView.getText().toString().equals("") || selectedImageUri != null) {
                initializeUploading();
            }
        });
    }

    /**
     * Set actions of cancel button.
     */
    private void setCancelButtonActions() {
        cancelButton.setOnClickListener(view -> {
            if (!textContentSocialAutoCompleteTextView.getText().toString().equals("")
                    || selectedImageUri != null) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Discard moment draft?");
                alertDialogBuilder.setMessage("Your moment will not be saved.");
                alertDialogBuilder.setPositiveButton("Discard", (dialog, which) -> {
                    this.onBackPressed();
                });
                alertDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.alert_red));
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.anu_logo_yellow));
            } else {
                this.onBackPressed();
            }
        });
    }

    /**
     * Set actions of set location button.
     */
    private void setSetLocationImageButtonActions() {
        setLocationImageButton.setOnClickListener(view -> {
            if (locationDisplayTextView.getText().equals("")) {
                addLocationInfoToMoment();
            } else {
                removeLocationInfoFromMoment();
            }
        });
    }

    /**
     * Set actions of set schedule button.
     */
    private void setSetScheduleImageButtonActions() {
        setScheduleImageButton.setOnClickListener(view -> {
            if (!isScheduled) {
                setupTimePicker();
            } else {
                isScheduled = false;
                scheduleCalender = Calendar.getInstance();
                scheduleDisplayTextView.setText("");
                setScheduleImageButton.setColorFilter(ContextCompat.getColor(this, R.color.default_icon_grey));
            }
        });
    }

    /**
     * Set actions of set visibility button.
     */
    private void setSetVisibilityButtonActions() {
        setVisibilityImageButton.setTag("public");
        setVisibilityImageButton.setOnClickListener(v -> {
            if (setVisibilityImageButton.getTag().equals(PUBLIC)) {
                setVisibilityImageButton.setImageResource(R.drawable.ic_friends);
                setVisibilityImageButton.setTag(FRIENDS);
                setVisibilityImageButton.setTooltipText("Moment visibility: friends");
                Toast.makeText(this, "Visibility: Friends", Toast.LENGTH_SHORT).show();
            } else if (setVisibilityImageButton.getTag().equals(FRIENDS)) {
                setVisibilityImageButton.setImageResource(R.drawable.ic_lock);
                setVisibilityImageButton.setTag(PRIVATE);
                setVisibilityImageButton.setTooltipText("Moment visibility: private");
                Toast.makeText(this, "Visibility: Myself", Toast.LENGTH_SHORT).show();
            } else if (setVisibilityImageButton.getTag().equals(PRIVATE)) {
                setVisibilityImageButton.setImageResource(R.drawable.ic_public);
                setVisibilityImageButton.setTag(PUBLIC);
                setVisibilityImageButton.setTooltipText("Moment visibility: public");
                Toast.makeText(this, "Visibility: Everyone", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set actions of set display scheduled time textview.
     */
    private void setScheduleDisplayTextViewActions() {
        scheduleDisplayTextView.setOnClickListener(view -> {
            setupTimePicker();
        });
    }

    /**
     * Set actions of add image button.
     */
    private void setAddImageImageButtonActions() {
        addImageImageButton.setOnClickListener(view -> {
            Intent getPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            getPicture.setType("image/*");
            startActivityForResult(getPicture, REQUEST_IMAGE);
        });
    }

    /**
     * Set remove image button actions.
     */
    private void setRemoveImageButtonActions() {
        removeImageButton.setOnClickListener(view -> {
            selectedImageView.setVisibility(View.GONE);
            removeImageButton.setVisibility(View.GONE);
        });
    }
    //**************************************************************************
    //                            Helper methods                               *
    //**************************************************************************
    /**
     * Initialize uploading moment.
     * <p>
     * Including text content and image.
     */
    private void initializeUploading() {
        momentBuilder.setTimeStamp();

        if (selectedImageUri != null) {
            StorageReference imagePath = FirebaseStorage.getInstance().getReference("MomentsImages").child(System.currentTimeMillis() + "." + getFileExtension(selectedImageUri));
            StorageTask uploadTask = imagePath.putFile(selectedImageUri);
            Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show();
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagePath.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                Uri downloadUri = task.getResult();
                momentImageUrl = downloadUri.toString();
                momentBuilder.setImageURL(momentImageUrl);

                uploadMomentToFirebase();
            });
        } else {
            uploadMomentToFirebase();
        }
        this.finish();
    }

    /**
     * Upload moment to firebase.
     */
    private void uploadMomentToFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Moments");
        momentID = databaseReference.push().getKey();
        momentBuilder.setContentID(momentID);
        momentBuilder.setSenderID(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if (!textContentSocialAutoCompleteTextView.getText().toString().equals("")) {
            momentBuilder.setTextContent(textContentSocialAutoCompleteTextView.getText().toString());

            // Set tag information.
            List<String> tags = textContentSocialAutoCompleteTextView.getHashtags();
            for (String tag : tags) {
                FirebaseDatabase.getInstance().getReference().child("Tags").child(tag).child(momentID).setValue(momentID);
            }
        }

        if (isScheduled) {
            momentBuilder.setSchedule(scheduleCalender);
        }

        if (setVisibilityImageButton.getTag().equals(FRIENDS)) {
            momentBuilder.setVisibility(FRIENDS);
        } else if (setVisibilityImageButton.getTag().equals(PRIVATE)){
            momentBuilder.setVisibility(PRIVATE);
        }

        Map<String, String> momentMap = momentBuilder.getResult().toMap();
        databaseReference.child(momentID).setValue(momentMap);

        Toast.makeText(this, "Moment posted!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Set up time picker for picking scheduled time.
     */
    private void setupTimePicker() {
        Calendar calendar = Calendar.getInstance();

        int displayHour = calendar.get(Calendar.HOUR_OF_DAY);
        int displayMinute = calendar.get(Calendar.MINUTE);

        if (isScheduled) {
            displayHour = scheduleCalender.get(Calendar.HOUR_OF_DAY);
            displayMinute = scheduleCalender.get(Calendar.MINUTE);
        } else {
            scheduleCalender = Calendar.getInstance();
        }
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
            Calendar pickedTime = new GregorianCalendar(0, 0, 0, hourOfDay, minute);
            Calendar currentTime = new GregorianCalendar(0, 0, 0, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE));

            if (pickedTime.before(currentTime)) {
                scheduleCalender.add(DAY_OF_YEAR, 1);
            }

            scheduleCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
            scheduleCalender.set(Calendar.MINUTE, minute);
            scheduleCalender.set(Calendar.SECOND, 0);

            setSetScheduleDisplayTextView();
            setScheduleImageButton.setColorFilter(ContextCompat.getColor(this, R.color.anu_logo_yellow));
            isScheduled = true;
        }, displayHour, displayMinute, is24HourFormat(this));

        timePickerDialog.show();
    }

    /**
     * Set scheduled text view content.
     */
    private void setSetScheduleDisplayTextView() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        String scheduledTime = dateFormat.format(scheduleCalender.getTime());
        if (Calendar.getInstance().get(DAY_OF_YEAR) == scheduleCalender.get(DAY_OF_YEAR)) {
            scheduleDisplayTextView.setText("Today " + scheduledTime);
        } else {
            scheduleDisplayTextView.setText("Tomorrow " + scheduledTime);
        }
    }

    /**
     * Get file's type information (extension).
     *
     * @param uri url of image
     * @return File's extionsion.
     */
    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    /**
     * Add location information to moment.
     */
    private void addLocationInfoToMoment() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setLocationImageButton.setVisibility(View.INVISIBLE);
            loadingLocationProgressBar.setVisibility(View.VISIBLE);

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = null;
                try {
                     location = task.getResult();
                } catch (Exception ignored) {}

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                if (null != location) {
                    List<Address> addresses;

                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        String countryName = addresses.get(0).getCountryName();
                        String countryCode = addresses.get(0).getCountryCode();
                        String cityName = addresses.get(0).getLocality();
                        String streetName = addresses.get(0).getThoroughfare();

                        locationDisplayTextView.setText(setLocationDisplayName(countryName, countryCode, cityName, streetName));
                        locationDisplayTextView.setTooltipText(countryCode + ", " + cityName + ", " + streetName);
                        setLocationImageButton.setColorFilter(ContextCompat.getColor(this, R.color.anu_logo_yellow));

                        setMomentsLocationInfo(countryName, cityName, streetName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setLocationImageButton.setVisibility(View.VISIBLE);
                loadingLocationProgressBar.setVisibility(View.INVISIBLE);
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    /**
     * Give country, city and street, set these information to moment.
     *
     * @param country Country where moment is sent.
     * @param city City where the moment is sent.
     * @param street Street where the moment is sent.
     */
    private void setMomentsLocationInfo(String country, String city, String street) {
        momentBuilder.setLocation(country + "," + city + "," + street);
    }

    /**
     * Clear location information in moments.
     */
    private void removeLocationInfoFromMoment() {
        setLocationImageButton.setColorFilter(ContextCompat.getColor(this, R.color.default_icon_grey));
        locationDisplayTextView.setText("");
        momentBuilder.setLocation(NOT_DEFINED_STRING_VALUE);
    }

    /**
     * Set display text in location.
     * <p>
     * Based on length of street name, determine content being displayed.
     *
     * @param countryName Name of country.
     * @param countryCode Country code.
     * @param city Name of city.
     * @param street Name of street.
     * @return Displayed location content (in string).
     */
    private String setLocationDisplayName(String countryName, String countryCode, String city, String street) {
        StringBuilder locationSb = new StringBuilder();

        if (countryCode.length() + city.length() + street.length() < 20) {
            if (!countryName.equals("Australia")) {
                locationSb.append(countryCode);
                locationSb.append(" · ");
            }
        }

        if (city.length() + street.length() < 20) {
            locationSb.append(city);
            locationSb.append(" · ");
        }

        if (street.length() > 15) {
            locationSb.append(street.substring(0, 15));
            locationSb.append(" ...");
        } else {
            locationSb.append(street);
        }

        return locationSb.toString();
    }

    /**
     * Deprecated.
     *
     * Open camera and take picture.
     */
    private void setTakePhotoImageButtonActions() {
        takePhotoImageButton.setOnClickListener(view -> {
            Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(takePhoto, REQUEST_IMAGE_CAPTURE);
        });
    }

    /**
     * Set view of selected image in write moment activity.
     */
    private void setSelectedImageView() {
        selectedImageView.setVisibility(View.VISIBLE);
        removeImageButton.setVisibility(View.VISIBLE);
        Glide.with(this).load(selectedImageUri).into(selectedImageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            setSelectedImageView();
        }

        // WARNING: BELOW CODE WILL NOT RUN.
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File file = new File("/sdcard/tmp");
            try {
                selectedImageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), null, null));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (selectedImageUri == null) {
                Toast.makeText(this, "NULL", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, selectedImageUri.toString(), Toast.LENGTH_LONG).show();
            }
            setSelectedImageView();
        }
    }
}