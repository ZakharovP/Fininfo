package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    String userId;

    private class Request extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            //String url = "http://10.0.2.2:3000";
            String url = params[0];
            HttpURLConnection connection = null;

            try {
                URL obj = new URL(url);
                connection = (HttpURLConnection) obj.openConnection();

                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } catch (Exception e) {
                System.out.println("ERROR!!!!!");
                System.out.println(e.toString());
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        try {
            String url = "https://ruz.fa.ru/api/schedule/group/8892?lng=1";
            Request req = new Request();

            String s_classes = req.execute(url).get();


            JSONArray classArray = new JSONArray(s_classes);
            ArrayList<String> classes_t = new ArrayList<>();
            for (int i = 0; i < classArray.length(); i++) {

                String startTime = classArray.getJSONObject(i).getString("beginLesson");
                String endTime = classArray.getJSONObject(i).getString("endLesson");
                String dayOfWeek = classArray.getJSONObject(i).getString("dayOfWeekString");
                String location = classArray.getJSONObject(i).getString("building");
                String teacherName = classArray.getJSONObject(i).getString("lecturer");
                String groupName = classArray.getJSONObject(i).getString("groupOid");

                String class_ = String.format("%s - %s (%s), %s\n%s\n%s", startTime, endTime, dayOfWeek, groupName, teacherName, location);
                classes_t.add(class_);
            }


            String[] classes_a = new String[classes_t.size()];
            final String[] classes = classes_t.toArray(classes_a);


            ListView itemList = (ListView) findViewById(R.id.itemsListView);
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, classes);
            itemList.setAdapter(itemsAdapter);
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR!!!!!!!!");
            e.printStackTrace();
        }

    }

    public void goSubjects(View view) {
        Intent intent = new Intent(this, SubjectActivity.class);
        startActivity(intent);
    }

    public void goChats(View view) {
        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra("userId", userId);
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