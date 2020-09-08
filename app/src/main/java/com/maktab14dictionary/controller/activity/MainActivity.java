package com.maktab14dictionary.controller.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import com.maktab14dictionary.R;
import com.maktab14dictionary.controller.fragments.WordListFragment;
import com.zeugmasolutions.localehelper.LocaleHelperActivityDelegateImpl;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class MainActivity extends AppCompatActivity implements WordListFragment.OnWordListListener {

    private LocaleHelperActivityDelegateImpl mDelegate = new LocaleHelperActivityDelegateImpl();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDelegate.onCreate(this);
        SQLiteStudioService.instance().start(this);

        if (getSupportFragmentManager().findFragmentById(R.id.fragmentWordListContainer) == null)
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentWordListContainer, WordListFragment.newInstance()).commit();
    }

    @Override
    protected void onDestroy() {
        SQLiteStudioService.instance().stop();
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(mDelegate.attachBaseContext(newBase));
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        super.applyOverrideConfiguration(mDelegate.applyOverrideConfiguration(getBaseContext(), overrideConfiguration));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDelegate.onResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDelegate.onPaused();
    }

    @Override
    public Resources getResources() {
        return mDelegate.getResources(super.getResources());
    }

    private void updateLocale(String lang) {
        mDelegate.setLocale(this, new Locale(lang));
    }


    @Override
    public void onChangeLanguage(String lang) {
        updateLocale(lang);
    }

    @Override
    public void finished() {
        finish();
    }
}