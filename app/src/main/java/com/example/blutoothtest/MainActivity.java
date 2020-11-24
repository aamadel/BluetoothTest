package com.example.blutoothtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String[] userArray = {"User 1", "User 2", "User 3", "User 4",
                "User 5", "User 6", "User 7", "User 8", "User 9", "User 10", "User 11"};


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_main, userArray);

        ListView listView = (ListView) findViewById(R.id.liste);
        listView.setAdapter(adapter);
    }
}