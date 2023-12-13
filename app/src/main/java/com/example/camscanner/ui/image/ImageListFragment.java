package com.example.camscanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
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
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.camscanner.models.ModelImage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageListFragment extends Fragment {

    private static final String TAG ="IMAGE_LIST_TAG";


    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri = null ;
    private FloatingActionButton addImageFab;
    private RecyclerView imagesRv;

    private ArrayList<ModelImage> allimageArrayList;
    private AdapterImage adapterImage;

    private ProgressDialog progressDialog;

    



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
        imagesRv = view.findViewById(R.id.imagesRv);

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        loadImages();

        addImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputImageDialog();

            }
        });

    }

   @Override
   public void onCreate(@NonNull Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
   }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_images,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        int itemId = item.getItemId();
        if(itemId == R.id.images_item_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Delete Images")
                    .setMessage("Are you sure you want to delete All/Selected images?")
                    .setPositiveButton("Delete All", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteImages(true);
                        }
                    })
                    .setNeutralButton("Delete Selected", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            deleteImages(false);
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    })
                    .show();

        }

        else if (itemId == R.id.image_item_pdf){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Convert to PDF")
                    .setMessage("Convert All/Selected Images to PDF")
                    .setPositiveButton("CONVERT ALL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            convertImagesTpPdf(true);
                        }
                    })
                    .setNeutralButton("CONVERT SELECTED", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            convertImagesTpPdf(false);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void convertImagesTpPdf(boolean converAll){
        Log.d(TAG, "convertImagesTpPdf: converAll: "+converAll);

        progressDialog.setMessage("Converting to PDF..");
        progressDialog.show();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            private float of;

            @Override
            public void run() {
                Log.d(TAG, "run: BG work start....");

                ArrayList<ModelImage> imagesToPdfList = new ArrayList<>();
                if (converAll){
                    imagesToPdfList = allimageArrayList;
                }
                else {
                    for (int i = 0; i < allimageArrayList.size();i++){
                        if (allimageArrayList.get(i).isChecked()){
                            imagesToPdfList.add(allimageArrayList.get(i));
                        }
                    }
                }

                Log.d(TAG, "run: imagesToPdfList size:" + imagesToPdfList.size());
                try {

                    File root = new File(mContext.getExternalFilesDir(null), Constants.PDF_FOLDER);
                    root.mkdirs();

                    long timestamp = System.currentTimeMillis();
                    String fileName = "PDF" + timestamp + ".pdf";

                    Log.d(TAG, "run: fileName:"+fileName);

                    File file = new File(root,fileName);

                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    PdfDocument pdfDocument = new PdfDocument();

                    for (int i = 0; i<imagesToPdfList.size(); i++){
                        Uri imageToAdInPdfUri = imagesToPdfList.get(i).getImageUri();

                        try {

                            Bitmap bitmap;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.getContentResolver(),imageToAdInPdfUri));
                            }
                            else {
                                bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),imageToAdInPdfUri);
                            }

                            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,false);
                            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(),bitmap.getHeight(),i+1).create();
                            PdfDocument.Page page = pdfDocument.startPage(pageInfo);


                            Paint paint = new Paint();
                            paint.setColor(Color.WHITE);

                            Canvas canvas = page.getCanvas();
                            canvas.drawPaint(paint);
                            canvas.drawBitmap(bitmap,of,of,null);

                            pdfDocument.finishPage(page);
                            bitmap.recycle();
                        }
                        catch (Exception e){

                            Log.d(TAG, "run: ",e);
                        }
                    }

                    pdfDocument.writeTo(fileOutputStream);
                    pdfDocument.close();
                }
                catch (Exception e){
                    progressDialog.dismiss();
                    Log.d(TAG, "run: " ,e);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: Converted");
                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Converted", Toast.LENGTH_SHORT).show();

                    }
                });
                
            }
        });

    }


    private void deleteImages(boolean deleteAll){

        ArrayList<ModelImage> imagesToDeleteList = new ArrayList<>();
        if (deleteAll){

            imagesToDeleteList = allimageArrayList;

        }
        else {

            for (int i = 0; i<allimageArrayList.size(); i++){
                if (allimageArrayList.get(i).isChecked()){
                    imagesToDeleteList.add(allimageArrayList.get(i));
                }
            }
        }
        for (int i=0; i<imagesToDeleteList.size();i++){
            try {
                String pathOfImageToDelete = imagesToDeleteList.get(i).getImageUri().getPath();
                File file = new File(pathOfImageToDelete);
                if (file.exists()){
                    boolean isDeleted = file.delete();
                    Log.d(TAG, "deleteImages: isDelete"+isDeleted);

                }

            }
            catch (Exception e){
                Log.d(TAG, "deleteImages: ",e);
            }
        }
        Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
        loadImages();
    }


    private void  loadImages(){
        Log.d(TAG, "loadImages: ");

        allimageArrayList = new ArrayList<>();
        adapterImage = new AdapterImage(mContext,allimageArrayList);


        imagesRv.setAdapter(adapterImage);

        File folder = new File(mContext.getExternalFilesDir(null),Constants.IMAGES_FOLDER);

        if (folder.exists()){

            Log.d(TAG, "loadImages: folder exists");

            File[] files = folder.listFiles();

            if ( files != null){

                Log.d(TAG, "loadImages: Folder exists and have images");
                
                for (File file: files){
                    Log.d(TAG, "loadImages: fileName: "+file.getName());

                    Uri imageUri = Uri.fromFile(file);

                    ModelImage modelImage = new ModelImage(imageUri,false);

                    allimageArrayList.add(modelImage);
                    adapterImage.notifyItemInserted(allimageArrayList.size());
                }

            }
            else {
                Log.d(TAG, "loadImages: Folder exists but empty");
            }

        }

        else {
            Log.d(TAG, "loadImages: Folder doesn't exists");
        }

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

                        ModelImage modelImage = new ModelImage(imageUri,false);
                        allimageArrayList.add(modelImage);
                        adapterImage.notifyItemInserted(allimageArrayList.size());

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

                        ModelImage modelImage = new ModelImage(imageUri,false);
                        allimageArrayList.add(modelImage);
                        adapterImage.notifyItemInserted(allimageArrayList.size());


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
