package com.example.camscanner;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;

public class ImageListFragment extends Fragment {

    private static final String TAG ="IMAGE_LIST_TAG";


    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri = null ;
    private FloatingActionButton addImageFab;

    private Context mContext;

    public ImageListFragment() {
        // Constructor vacío requerido por las convenciones de Fragment
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        addImageFab = view.findViewById(R.id.addImageFab);

        addImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputImageDialog();

            }
        });

    }


    private void saveImageToAppLevelDirectory(Uri imageUriToBeSaved){

        Log.d(TAG, "saveImageToAppLevelDirectory: ");
        try {

            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.getContentResolver(),imageUriToBeSaved));
            }
            else {

                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),imageUriToBeSaved);
            }

            File directory = new File(mContext.getExternalFilesDir(null),Constants.IMAGES_FOLDER);
            directory.mkdir();

            long timestamp = System.currentTimeMillis();
            String fileName = timestamp+ ".jpeg";

            File file = new File(mContext.getExternalFilesDir(null),""+Constants.IMAGES_FOLDER +"/" +fileName);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                fos.flush();
                fos.close();
                Log.d(TAG, "saveImageToAppLevelDirectory: Image Saved");
                Toast.makeText(mContext, "Image Saved", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e){
                Log.d(TAG, "saveImageToAppLevelDirectory: ", e);
                Log.d(TAG, "saveImageToAppLevelDirectory: Failed to save imege due to" +e.getMessage());
                Toast.makeText(mContext, "Failed to save imege due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

            }

        }
        catch (Exception e){
            Log.d(TAG, "saveImageToAppLevelDirectory: ", e);
            Log.d(TAG, "saveImageToAppLevelDirectory: Failed to save imege due to" +e.getMessage());
            Toast.makeText(mContext, "Failed to prepare image due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void showInputImageDialog(){
        Log.d(TAG,"showInputImageDialog: ");

        PopupMenu popupMenu = new PopupMenu(mContext,addImageFab);

        popupMenu.getMenu().add(Menu.NONE,1,1,"CAMERA");
        popupMenu.getMenu().add(Menu.NONE,2,2,"GALLERY");


        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int itemId = menuItem.getItemId();
                if (itemId == 1){

                    Log.d(TAG, "onMenuItemClick: Camera is clicked, check if camera permissions are grated or not");

                    if (checkCameraPermissions()){

                        pickImageCamera();
                    }
                    else {
                        requestCameraPermission();
                    }
                }
                else if (itemId == 2){
                    Log.d(TAG, "onMenuItemClick: Gallery is clicked,check if storage perission is granted or not");

                    if (checkStoragePermissionn()){

                        pickImageGallery();
                    }
                    else {
                        requestStoragePermission();
                    }
                }

                return true;
            }
        });
    }

    private void pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ");
        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){

                        Intent data = result.getData();

                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked image gallery:"+imageUri);

                        saveImageToAppLevelDirectory(imageUri);

                    }
                    else {
                        Toast.makeText(mContext, "Cancelled..", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"TEM IMAGE TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEM IMAGE DESCRIPTION");

        imageUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK){


                        Log.d(TAG, "onActivityResult: Picked image camera:"+imageUri);
                        saveImageToAppLevelDirectory(imageUri);

                    }
                    else {
                        Toast.makeText(mContext, "Cancelled..", Toast.LENGTH_SHORT).show();
                        
                    }
                }
            }
    );



    private boolean checkStoragePermissionn(){
        Log.d(TAG, "checkStoragePermissionn: ");

        boolean result = ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return result;
    }

    private void requestStoragePermission(){
        Log.d(TAG, "requestStoragePermission: ");
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermissions(){
        Log.d(TAG, "checkCameraPermissions: ");
        boolean cameraResult = ContextCompat.checkSelfPermission(mContext,Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
        boolean storageResult = ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;

        return cameraResult && storageResult;

    }


    private void requestCameraPermission(){
        Log.d(TAG, "requestCameraPermission: ");
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {

                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){


                        Log.d(TAG, "onRequestPermissionsResult: both permissions (Camera & Gallery) aare granted,we can launch camera intent");
                        pickImageCamera();
                    }
                    else {
                        Log.d(TAG, "onRequestPermissionsResult: CAMERA &STORAGE PERMISSIONS ARE REQUIRED");
                        Toast.makeText(mContext, "CAMERA &STORAGE PERMISSIONS ARE REQUIRED", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Log.d(TAG, "onRequestPermissionsResult: Cancelled...");
                    Toast.makeText(mContext, "Cancelled...", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length >0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        Log.d(TAG, "onRequestPermissionsResult: storege permission granted,we can launch gallery intent");
                        pickImageGallery();
                        
                    }
                    else {
                        Log.d(TAG, "onRequestPermissionsResult: storage permission denied,can't launch gallery intent");
                        Toast.makeText(mContext, "Storege permissioon is required...", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Log.d(TAG, "onRequestPermissionsResult: Cancelled...");
                    Toast.makeText(mContext, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }
}
