package com.example.fininfo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TeacherInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_info);

        TextView textView = (TextView) findViewById(R.id.teacherInfo);



        String teacherInfo = getIntent().getStringExtra("teacherInfo");
        textView.setText(teacherInfo);

    }
}
