package de.lmu.delusio.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;

import de.lmu.delusio.databinding.ActivityHomeBinding;
import de.lmu.delusio.helper.ImageHelper;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<String> imageList;

    private ActivityHomeBinding binding;
    private ImageHelper imageHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setImageList();

        imageHelper = new ImageHelper(imageList);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(imageHelper);

    }

    private void setImageList() {

        imageList = new ArrayList<>();

        if (getIntent().getBooleanExtra("isEyeClosed", false)) {
            imageList.add("https://images.unsplash.com/photo-1667339406244-24977ed6e1ca?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw1fHx8ZW58MHx8fHw%3D&auto=format&fit=crop&w=500&q=60");
            imageList.add("https://images.unsplash.com/photo-1667226569516-457bc34613dd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw0fHx8ZW58MHx8fHw%3D&auto=format&fit=crop&w=500&q=60");
            imageList.add("https://images.unsplash.com/photo-1667325688507-6e2e670bd140?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw4fHx8ZW58MHx8fHw%3D&auto=format&fit=crop&w=500&q=60");
        }
        else {
            imageList.add("https://images.unsplash.com/photo-1667416542628-59ea3acda78f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwxNHx8fGVufDB8fHx8&auto=format&fit=crop&w=500&q=60");
        }

    }
}