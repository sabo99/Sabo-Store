package com.sabo.sabostore.Activity.Account.AccountMenu.Profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.sabo.sabostore.Activity.Account.AccountMenu.AccountSettings.SubMenu.ChangeEmailActivity;
import com.sabo.sabostore.BottomSheet.ProfileNameSheetFragment;
import com.sabo.sabostore.BottomSheet.ProfilePhotoSheetFragment;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.ActionProfilePhotoEvent;
import com.sabo.sabostore.EventBus.PreviewPhotoEvent;
import com.sabo.sabostore.EventBus.UpdateProfileNameEvent;
import com.sabo.sabostore.Model.UserModel;
import com.sabo.sabostore.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference imgRef;

    private AlertDialog dialog;

    private CircleImageView civProfilePhoto;
    private ImageButton ibPreviewPhoto, ibChangePhoto;
    private LinearLayout llState1, llState2;
    private RelativeLayout rlName, rlEmail, rlPhone;
    private TextView tvProfileName, tvProfileEmail, tvProfilePhone;
    private String sendUrlPhoto = "";
    private Uri resultImageUri = null,
            pickImgUri = null;
    private boolean isShowDialog = false;

    @Override
    protected void onResume() {
        super.onResume();
        retrieveData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF).child(firebaseUser.getUid());
        imgRef = FirebaseStorage.getInstance().getReference()
                .child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.png");

        initViews();
    }

    private void initViews() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        civProfilePhoto = findViewById(R.id.civProfilePhoto);
        ibPreviewPhoto = findViewById(R.id.ibPreviewPhoto);
        ibChangePhoto = findViewById(R.id.ibChangePhoto);
        rlName = findViewById(R.id.rlName);
        rlEmail = findViewById(R.id.rlEmail);
        rlPhone = findViewById(R.id.rlPhone);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);

        ibPreviewPhoto.setOnClickListener(v -> {
            EventBus.getDefault().postSticky(new PreviewPhotoEvent(true, true, sendUrlPhoto));
            startActivity(new Intent(ProfileActivity.this, ProfilePhotoPreviewActivity.class));
            CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
        });

        ibChangePhoto.setOnClickListener(v -> {
            ProfilePhotoSheetFragment profilePhotoSheetFragment = ProfilePhotoSheetFragment.getInstance();
            profilePhotoSheetFragment.show(getSupportFragmentManager(), "ProfilePhotoSheetFragment");
        });

        rlName.setOnClickListener(v -> {
            Common.currentUser.setName(tvProfileName.getText().toString());
            ProfileNameSheetFragment profileNameSheetFragment = ProfileNameSheetFragment.getInstance();
            profileNameSheetFragment.show(getSupportFragmentManager(), "ProfileNameSheetFragment");
        });

        rlEmail.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangeEmailActivity.class));
            CustomIntent.customType(this, Common.Anim_Left_to_Right);
        });

        rlPhone.setOnClickListener(v -> {
            showDialogPhone();
            isShowDialog = true;
        });

    }

    private void retrieveData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);

                    Common.currentUser = userModel;

                    if (userModel.getImage().equals("")) {
                        sendUrlPhoto = "";
                        Picasso.get().load(R.drawable.no_profile).into(civProfilePhoto);
                    } else {
                        sendUrlPhoto = userModel.getImage();
                        Picasso.get().load(userModel.getImage()).placeholder(R.drawable.no_profile).into(civProfilePhoto);
                    }


                    tvProfileName.setText(userModel.getName());
                    tvProfileEmail.setText(userModel.getEmail());

                    if (userModel.getPhone().equals(""))
                        tvProfilePhone.setText("None");
                    else
                        tvProfilePhone.setText(Common.formatPhoneNumber(getBaseContext(), userModel.getPhone()));


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("e", error.getMessage());
            }
        });
    }

    int state = 0;
    private String verificationID, codeVerification;
    private void showDialogPhone() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_phone_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(false);
        builder.setView(view);

        /** InitViews Dialog */
        EditText etPhone, etCode;
        CountryCodePicker ccPicker;
        Button btnNext, btnConfirm;
        ImageButton ibBack, ibClose;
        ProgressBar progressBar;

        progressBar = view.findViewById(R.id.progressBar);
        llState1 = view.findViewById(R.id.llState1);
        llState2 = view.findViewById(R.id.llState2);
        ccPicker = view.findViewById(R.id.ccPicker);
        etPhone = view.findViewById(R.id.etPhone);
        etCode = view.findViewById(R.id.etCode);
        btnNext = view.findViewById(R.id.btnNext);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        ibBack = view.findViewById(R.id.ibBack);
        ibClose = view.findViewById(R.id.ibClose);

        dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            state = 0;

            if (state == 0){
                llState1.setVisibility(View.VISIBLE);
                llState2.setVisibility(View.INVISIBLE);
            }
            if (state == 1){
                llState1.setVisibility(View.INVISIBLE);
                llState2.setVisibility(View.VISIBLE);
            }

            ibBack.setVisibility(View.INVISIBLE);
            ibClose.setVisibility(View.VISIBLE);

            btnNext.setOnClickListener(v -> {
                String countryCode = "+" + ccPicker.getSelectedCountryCode();
                String phone = countryCode + etPhone.getText().toString();
                String mPhone = etPhone.getText().toString();

                if (TextUtils.isEmpty(mPhone))
                    etPhone.setError("Please fill out this field.");
                else {
                    progressBar.setVisibility(View.VISIBLE);

                    btnNext.setEnabled(false);
                    btnConfirm.setBackground(getDrawable(R.color.colorPrimaryDark));
                    btnConfirm.setEnabled(false);
                    ibClose.setVisibility(View.INVISIBLE);

                    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(s, forceResendingToken);
                            verificationID = s;
                        }

                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            codeVerification = phoneAuthCredential.getSmsCode();

                            progressBar.setVisibility(View.INVISIBLE);

                            etCode.setText(codeVerification);

                            ibBack.setVisibility(View.VISIBLE);
                            btnConfirm.setBackground(getDrawable(R.color.colorPrimary));
                            btnConfirm.setEnabled(true);
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {

                        }
                    };

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phone,
                            60,
                            TimeUnit.SECONDS,
                            TaskExecutors.MAIN_THREAD,
                            mCallBack);


                    /** Next Page */
                    Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_to_left);
                    llState1.startAnimation(animation);
                    llState1.setVisibility(View.INVISIBLE);

                    new Handler().postDelayed(() -> {
                        btnNext.setEnabled(true);
                        Animation animation1 = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_from_right_to_left);
                        llState2.setVisibility(View.VISIBLE);
                        llState2.setAnimation(animation1);
                        state = 1;
                    }, 50);
                }

            });


            btnConfirm.setOnClickListener(v -> {
                String code = etCode.getText().toString();

                if (TextUtils.isEmpty(code))
                    Toast.makeText(this, "Please enter verify code!", Toast.LENGTH_SHORT).show();
                else {
                    progressBar.setVisibility(View.VISIBLE);

                    if (code.equals(codeVerification)){
                        HashMap<String, Object> updatePhone = new HashMap<>();
                        updatePhone.put(Common.KEY_PHONE, etPhone.getText().toString());
                        userRef.updateChildren(updatePhone)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        progressBar.setVisibility(View.INVISIBLE);
                                        dialog.dismiss();

                                        tvProfilePhone.setText(Common.formatPhoneNumber(getBaseContext(), etPhone.getText().toString()));
                                        Toast.makeText(this, "Phone number has been changed.", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                        progressBar.setVisibility(View.INVISIBLE);
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.INVISIBLE);
                                });
                    }
                    else
                        Toast.makeText(this, "Code verification is wrong!", Toast.LENGTH_SHORT).show();

                }
            });


            ibBack.setOnClickListener(v -> {
                ibBack.setVisibility(View.INVISIBLE);
                ibClose.setVisibility(View.VISIBLE);
                if (state == 0) {
                    dialog.dismiss();
                }
                if (state == 1) {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_to_right);
                    llState2.setAnimation(animation);
                    llState2.setVisibility(View.INVISIBLE);

                    new Handler().postDelayed(() -> {
                        Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.slide_from_left_to_right);
                        llState1.setVisibility(View.VISIBLE);
                        llState1.setAnimation(animation1);
                        state = 0;
                    }, 50);
                }
            });

            ibClose.setOnClickListener(v -> {
                dialog.dismiss();
                state = 0;
            });
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
    }

    @Override
    public void onBackPressed() {
        if (state == 0){
            if (isShowDialog)
                dialog.dismiss();
            else
                super.onBackPressed();
        }
        if (state == 1){
            // Not Close Dialog
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void updateProfileName(UpdateProfileNameEvent event){
        if (event.isUpdated())
            if (event.getName() != null || !event.getName().isEmpty()){
                event.setUpdated(false);
                tvProfileName.setText(event.getName());
            }

    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void actionProfilePhoto(ActionProfilePhotoEvent event) {
        if (event.isChange()) {
            event.setChange(false);
            checkPermission();
        }

        if (event.isRemove()) {
            event.setRemove(false);
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Remove")
                    .setContentText("Profile photo?")
                    .showCancelButton(true)
                    .setCancelText("Cancel")
                    .setCancelClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                    })
                    .setConfirmText("Remove")
                    .setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        removePhoto();
                    })
                    .show();
        }
    }

    /**
     * Remove Photo
     */
    private void removePhoto() {
        SweetAlertDialog loading = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        loading.setTitleText("Please wait...").show();

        if (imgRef != null) {
            imgRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        HashMap<String, Object> deleteImage = new HashMap<>();
                        deleteImage.put(Common.KEY_IMG, "");

                        userRef.updateChildren(deleteImage)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        loading.dismissWithAnimation();
                                        loading.dismiss();

                                        /** Send Url to Profile Photo Preview */
                                        sendUrlPhoto = "";

                                        Picasso.get().load(R.drawable.no_profile).into(civProfilePhoto);
                                        Toast.makeText(this, "Image has been deleted.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loading.dismiss();
                                        Log.d("task", task.getException().getMessage());
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    loading.dismiss();
                                    Log.d("e", e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        loading.dismiss();
                        Log.d("e", e.getMessage());
                    });
        } else {
            loading.dismissWithAnimation();
            Toast.makeText(this, "Image does not exist.", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Change Photo from Gallery OR Camera
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    Common.REQUEST_PERMISSION_CHANGE_PHOTO);
        else {
            if (pickImgUri == null)
                openPickImageChooser();
            else
                return;
        }
    }

    private void openPickImageChooser() {
        CropImage.startPickImageActivity(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Common.REQUEST_PERMISSION_CHANGE_PHOTO && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (pickImgUri == null)
                openPickImageChooser();
            else
                return;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK){
            pickImgUri = CropImage.getPickImageResultUri(this, data);
            CropImage.activity(pickImgUri)
                    .setFixAspectRatio(true)
                    .setRequestedSize(500, 500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultImageUri = result.getUri();

                SweetAlertDialog uploading = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                uploading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
                uploading.setTitleText("Uploading...").show();

                uploadImgToFirebase(resultImageUri, uploading);
            }
        } else /** IF Cancel Crop pickImgUri return NULL */
            pickImgUri = null;

    }

    /** Upload Image to FirebaseStorage & Update Child FirebaseDatabase User Information */
    private void uploadImgToFirebase(Uri resultUri, SweetAlertDialog uploading) {

        imgRef.putFile(resultUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        resultImageUri = null;
                        Picasso.get().load(uri).placeholder(R.drawable.no_profile).into(civProfilePhoto);

                        HashMap<String, Object> updateImage = new HashMap<>();
                        updateImage.put(Common.KEY_IMG, uri.toString());

                        /** Send Url to Profile Photo Preview */
                        sendUrlPhoto = uri.toString();

                        userRef.updateChildren(updateImage)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        uploading.dismissWithAnimation();
                                        uploading.dismiss();

                                        Toast.makeText(this, "Image has been changed.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        uploading.dismissWithAnimation();
                                        uploading.dismiss();

                                        new SweetAlertDialog(getBaseContext(), SweetAlertDialog.WARNING_TYPE)
                                                .setTitleText("Oops!")
                                                .setContentText(task.getException().getMessage())
                                                .show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    uploading.dismiss();
                                    Log.d("e", e.getMessage());
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    uploading.dismiss();
                    Log.d("e", e.getMessage());
                });
    }
}
