package com.maktab14dictionary.controller.activity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.maktab14dictionary.R;
import com.maktab14dictionary.controller.fragments.WordListFragment;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements WordListFragment.OnWordListListener {

    public static final String SHARED_PREFERENCE_CURRENT_LANGUAGE = "SharedPreferenceCurrentLanguage";

    private SharedPreferences mPreferences;
    private String mCurrentLanguage = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportFragmentManager().findFragmentById(R.id.fragmentWordListContainer) == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentWordListContainer, WordListFragment.newInstance()).commit();
    }

    @Override
    public void onChangeLanguage(String lang) {
        mCurrentLanguage = lang;
        mPreferences.edit().putString(SHARED_PREFERENCE_CURRENT_LANGUAGE, mCurrentLanguage).apply();
        recreate();
    }

    @Override
    public void finished() {
        finish();
    }


    @Override
    protected void attachBaseContext(Context newBase) {

        mPreferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        mCurrentLanguage = mPreferences.getString(SHARED_PREFERENCE_CURRENT_LANGUAGE, mCurrentLanguage);
        Context context = changeLanguage(newBase, mCurrentLanguage);
        super.attachBaseContext(context);
    }

    public static ContextWrapper changeLanguage(Context context, String lang) {
        Locale currentLocal;
        Resources res  = context.getResources();
        Configuration conf = res.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentLocal = conf.getLocales().get(0);
        } else {
            currentLocal = conf.locale;
        }

        if (!lang.equals("") && !currentLocal.getLanguage().equals(lang)) {
            Locale newLocal = new Locale(lang);
            Locale.setDefault(newLocal);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                conf.setLocale(newLocal);
            } else {
                conf.locale = newLocal;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                context = context.createConfigurationContext(conf);
            } else {
                res.updateConfiguration(conf, context.getResources().getDisplayMetrics());
            }
        }

        return new ContextWrapper(context);
    }
}