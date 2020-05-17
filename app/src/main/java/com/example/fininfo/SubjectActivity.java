package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubjectActivity extends AppCompatActivity {
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
                System.out.println("Данные с сервера!!!");
                JSONArray answerArray = new JSONArray(answer);
                for (int i = 0 ; i < answerArray.length(); i++) {
                    JSONObject obj = answerArray.getJSONObject(i);
                    String lecturer = obj.getString("lecturer");
                    String discipline = obj.getString("discipline");
                    String detailInfo = obj.getString("detailInfo");

                    LinearLayout subjectListLayout = (LinearLayout)findViewById(R.id.subjectListLayout);
                    TextView subjectTextView = new TextView(SubjectActivity.this);
                    subjectTextView.setText(lecturer + "\n" + discipline + "\n" + detailInfo);
                    subjectListLayout.addView(subjectTextView);


                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(4, Color.RED);

                    subjectTextView.setBackground(gd);
                }
            } catch(Exception e){
                System.out.println("!!!!!!!ERROR IN SUBJECT ACTIVITY!!!!!!!!");
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        try {
            SubjectActivity.RequestGET req = new SubjectActivity.RequestGET();
            String url = "https://ruz.fa.ru/api/schedule/group/8892?lng=1";
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN SUBJECT ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }
}
