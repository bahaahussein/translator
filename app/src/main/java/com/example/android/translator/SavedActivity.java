package com.example.android.translator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SavedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_saved,new SavedFragment())
                .commit();
    }
}
