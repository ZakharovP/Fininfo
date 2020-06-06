package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TeacherActivity extends AppCompatActivity {

    private class RequestGET extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            String path = params[0];
            String parammetrs = params[1];

            String response = "";


            byte[] data = null;
            InputStream is = null;

            try {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                StringBuilder content;
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {

                    String line;
                    content = new StringBuilder();

                    while ((line = in.readLine()) != null) {
                        content.append(line);
                        content.append(System.lineSeparator());
                    }
                }

                return content.toString();

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
        protected void onPostExecute(String answer) {
            super.onPostExecute(answer);
            try {
                JSONArray answerArray = new JSONArray(answer);
                for (int i = 0 ; i < answerArray.length(); i++) {
                    JSONObject obj = answerArray.getJSONObject(i);
                    String id = obj.getString("id");
                    String description = obj.getString("description");
                    String label = obj.getString("label");

                    LinearLayout teacherListLayout = (LinearLayout)findViewById(R.id.teacherListLayout);
                    TextView teacherTextView = new TextView(TeacherActivity.this);
                    teacherTextView.setText(label);
                    teacherTextView.setPadding(10, 30, 30, 10);

                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(4, Color.parseColor("#AA2211"));
                    gd.setColor(Color.parseColor("#CCDEFF"));
                    teacherTextView.setBackground(gd);

                    final String teacherInfo = String.format(" id: %s\n ИФО: %s\n Отдел: %s", id, label, description);

                    teacherTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(TeacherActivity.this, TeacherInfoActivity.class);
                            intent.putExtra("teacherInfo", teacherInfo);
                            startActivity(intent);
                        }
                    });


                    teacherListLayout.addView(teacherTextView);
                }
            } catch(Exception e){
                System.out.println("!!!!!!!ERROR IN TEACHER ACTIVITY!!!!!!!!");
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        try {
            TeacherActivity.RequestGET req = new TeacherActivity.RequestGET();
            String url = "https://ruz.fa.ru/api/search?type=person";
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN TEACHER ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }
}
