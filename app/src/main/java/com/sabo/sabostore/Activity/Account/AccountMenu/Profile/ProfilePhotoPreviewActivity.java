package com.sabo.sabostore.Activity.Account.AccountMenu.Profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.sabo.sabostore.BottomSheet.ProfilePhotoSheetFragment;
import com.sabo.sabostore.Common.Common;
import com.sabo.sabostore.EventBus.ActionProfilePhotoEvent;
import com.sabo.sabostore.EventBus.PreviewPhotoEvent;
import com.sabo.sabostore.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import maes.tech.intentanim.CustomIntent;

public class ProfilePhotoPreviewActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference imgRef;

    private PhotoView photoView;

    private String sendUrlPhoto = "";
    private boolean isEdit = false;
    private Uri resultImageUri = null, pickImgUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_photo_preview);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser =  firebaseAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF).child(firebaseUser.getUid());
        imgRef = FirebaseStorage.getInstance().getReference()
                .child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.png");

        initViews();
    }

    private void initViews() {
        photoView = findViewById(R.id.pvProfile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile photo");
    }

    private void loadPhoto() {
        if (sendUrlPhoto.equals(""))
            Picasso.get().load(R.drawable.no_profile).into(photoView);
        else
            Picasso.get().load(sendUrlPhoto).placeholder(R.drawable.no_profile).into(photoView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_photo, menu);
        menu.findItem(R.id.action_edit_photo);

        if (isEdit)
            menu.findItem(R.id.action_edit_photo).setVisible(true);
        else
            menu.findItem(R.id.action_edit_photo).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CustomIntent.customType(this, Common.Anim_Fadein_to_Fadeout);
                finish();
                break;
            case R.id.action_edit_photo:
                ProfilePhotoSheetFragment profilePhotoSheetFragment = ProfilePhotoSheetFragment.getInstance();
                profilePhotoSheetFragment.show(getSupportFragmentManager(), "ProfilePhotoSheetFragment");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPhoto();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isEdit = false;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(ProfilePhotoPreviewActivity.this, Common.Anim_Fadein_to_Fadeout);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void previewProfilePhoto(PreviewPhotoEvent event) {
        if (event.isPreview()) {
            sendUrlPhoto = event.getUrlPhoto();
            loadPhoto();

            if (event.isEdit())
                isEdit = event.isEdit(); /** True */
            else
                isEdit = event.isEdit(); /** False */

            event.setPreview(false);
            event.setUrlPhoto("");
        }
        else
            sendUrlPhoto = "";
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

    /** Remove Photo */
    private void removePhoto() {
        SweetAlertDialog loading = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        loading.setTitleText("Please wait...").show();

        if (imgRef != null){
            imgRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        HashMap<String, Object> deleteImage = new HashMap<>();
                        deleteImage.put(Common.KEY_IMG, "");

                        userRef.updateChildren(deleteImage)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        loading.dismissWithAnimation();
                                        loading.dismiss();

                                        Picasso.get().load(R.drawable.no_profile).into(photoView);
                                        Toast.makeText(this, "Image has been deleted.", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
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
        }
        else {
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
        } else {
            /** IF Cancel Crop pickImgUri return NULL & Preview Image set Default */
            pickImgUri = null;
            //finish();
        }

    }

    /** Upload Image to FirebaseStorage & Update Child FirebaseDatabase User Information */
    private void uploadImgToFirebase(Uri resultUri, SweetAlertDialog uploading) {

        imgRef.putFile(resultUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        resultImageUri = null;
                        Picasso.get().load(uri).placeholder(R.drawable.no_profile).into(photoView);

                        HashMap<String, Object> updateImage = new HashMap<>();
                        updateImage.put(Common.KEY_IMG, uri.toString());

                        userRef.updateChildren(updateImage)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        uploading.dismissWithAnimation();
                                        uploading.dismiss();

                                        Toast.makeText(this, "Image has been changed.", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
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
