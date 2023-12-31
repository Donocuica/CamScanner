package com.example.camscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.camscanner.ui.image.ImageListFragment;
import com.example.camscanner.ui.pdf.PdfListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        loadImagesFragment();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.bottom_menu_images) {
                    loadImagesFragment();
                } else if (itemId == R.id.bottom_menu_pdfs){
                    loadPdfsFragment();
                }
                return true;
            }


        });
    }
    private void loadImagesFragment() {
        setTitle("Imagenes");

        ImageListFragment imageListFragment = new ImageListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,imageListFragment,"ImageListFragment");
        fragmentTransaction.commit();
    }
    private void loadPdfsFragment() {
        setTitle("PDF List");

        PdfListFragment pdfListFragment = new PdfListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,pdfListFragment,"PdfListFragment");
        fragmentTransaction.commit();
    }
}