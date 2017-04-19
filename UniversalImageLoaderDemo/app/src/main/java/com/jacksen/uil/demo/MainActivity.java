package com.jacksen.uil.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button displayerBtn;
    private Button imageListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayerBtn = (Button) findViewById(R.id.displayer_btn);
        displayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ImageDisplayerActivity.class));
            }
        });


        imageListBtn = (Button) findViewById(R.id.image_list_btn);
        imageListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ImageListActivity.class));
            }
        });
    }
}
