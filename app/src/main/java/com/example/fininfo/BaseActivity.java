package com.example.fininfo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;


public abstract class BaseActivity extends AppCompatActivity {

    String theme = "DarkTheme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        theme = PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "DarkTheme");
        //System.out.println(">>>> READ THEME: " + theme);
        try {
            Field resourceField = R.style.class.getDeclaredField(theme);
            int resourceId = resourceField.getInt(resourceField);
            setTheme(resourceId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!theme.equals(prefs.getString("theme", "DarkTheme"))) {
            this.recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.theme, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        switch(item.getItemId())
        {
            case R.id.action_light_theme:
                theme = "LightTheme";
                prefsEdit.putString("theme", theme);
                prefsEdit.commit();
                setTheme(R.style.LightTheme);
                super.recreate();
                return true;

            case R.id.action_dark_theme:
                theme = "DarkTheme";
                prefsEdit.putString("theme", theme);
                prefsEdit.commit();
                setTheme(R.style.DarkTheme);
                super.recreate();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}