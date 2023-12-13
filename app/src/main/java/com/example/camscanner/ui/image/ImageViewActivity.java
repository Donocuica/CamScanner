package com.example.camscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView image2IV;
    private String image;

    private static final String TAG = "IMAGE_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);


        getSupportActionBar().setTitle("ImageView");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        image2IV = findViewById(R.id.image2IV);

        Log.d(TAG, "onCreate: Image:"+image);


        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.ic_image_black)
                .into(image2IV);

        image = getIntent().getStringExtra("imageUri");

    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}