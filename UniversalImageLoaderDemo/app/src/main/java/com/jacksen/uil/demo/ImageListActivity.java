package com.jacksen.uil.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class ImageListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private ImageRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ImageRecyclerAdapter(this, Constants.IMG_LIST);

        recyclerView.setAdapter(adapter);
    }
}
