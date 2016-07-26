package com.jacksen.picassodemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {


    private Button loadImgBtn;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image_view);
        loadImgBtn = (Button) findViewById(R.id.load_img_btn);
        loadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Picasso picasso = new Picasso.Builder(MainActivity.this).build();
                picasso.setIndicatorsEnabled(true);
                picasso.load(Constants.URL_DOWNLOAD_IMG).into(imageView);
            }
        });
    }
}
