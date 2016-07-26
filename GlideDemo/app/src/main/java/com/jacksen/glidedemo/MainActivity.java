package com.jacksen.glidedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private Button loadImgBtn;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadImgBtn = (Button) findViewById(R.id.load_img_btn);
        imageView = (ImageView) findViewById(R.id.image_view);

        loadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(MainActivity.this).load(Constants.URL_DOWNLOAD_IMG).into(imageView);
            }
        });

    }
}
