package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String[] teachers = {"Иванов А. С.", "Петров В. С."};

        String rawItems[][] = {
                {"08:00", "Математика", teachers[0]},
                {"09:30", "Javascript", teachers[1]}
        };

        ArrayList<String> items = new ArrayList<>();

        for (int i = 0; i < rawItems.length; i++) {
            items.add(rawItems[i][0] + " " + rawItems[i][1] + "\n" + rawItems[i][2]);
        }

        ListView itemList = (ListView) findViewById(R.id.itemsListView);
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items.toArray());
        itemList.setAdapter(itemsAdapter);


        ListView teacherList = (ListView) findViewById(R.id.teachersListView);
        ArrayAdapter<String> teachersAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, teachers);
        teacherList.setAdapter(teachersAdapter);

        teacherList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String teacher = teachers[position];
                Log.i("app", teacher);
                Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
                intent.putExtra("teacher", teacher);
                startActivity(intent);
            }
        });


    }

    public void goSubjects(View view) {
        Intent intent = new Intent(this, SubjectActivity.class);
        startActivity(intent);
    }

    public void goChats(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    public void goDocs(View view) {
        Intent intent = new Intent(this, DocumentActivity.class);
        startActivity(intent);
    }

    public void goTeachers(View view) {
        Intent intent = new Intent(this, TeacherActivity.class);
        startActivity(intent);
    }
}