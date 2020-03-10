package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TeacherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        String teacher = getIntent().getStringExtra("teacher");
        TextView t = findViewById(R.id.teacherId);
        t.setText(teacher);
    }
}
