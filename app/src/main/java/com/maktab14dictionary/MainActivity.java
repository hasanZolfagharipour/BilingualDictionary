package com.maktab14dictionary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportFragmentManager().findFragmentById(R.id.fragmentWordListContainer) == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentWordListContainer, WordListFragment.newInstance()).commit();
    }
}