package com.farmers.farmersflickr;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

/*
 * Display image in one screen after clicking an image from thumbnail layout
 */
public class FullImageActivity extends Activity {

    private ImageView imageView;
    private ImageLoader mLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        imageView = (ImageView) findViewById(R.id.fullImageView);
        mLoader = new ImageLoader(getApplicationContext());

        mLoader.DisplayImage(getIntent().getStringExtra("IMAGE_URI"), imageView);
    }
}
