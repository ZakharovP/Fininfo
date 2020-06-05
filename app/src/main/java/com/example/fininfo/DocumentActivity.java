package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

public class DocumentActivity extends AppCompatActivity {
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
                System.out.println(answer);
                JSONArray answerArray = new JSONArray(answer);
                for (int i = 0 ; i < answerArray.length(); i++) {
                    JSONObject document = answerArray.getJSONObject(i);
                    String filename = document.getString("filename");

                    System.out.println(filename);

                    LinearLayout documentListLayout = (LinearLayout)findViewById(R.id.documentListLayout);

                    TextView documentTextView = new TextView(DocumentActivity.this);
                    documentTextView.setText(filename);
                    documentTextView.setPadding(10, 30, 30, 10);

                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(4, Color.parseColor("#EEEEEE"));
                    gd.setColor(Color.parseColor("#CCDEFF"));
                    documentTextView.setBackground(gd);

                    final String documentInfo = filename;

                    documentTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            System.out.println(documentInfo);
                            try {
                                URL url = new URL("http://192.168.1.26:3000/download/proba.txt");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                int data;
                                while ((data = connection.getInputStream().read()) != -1) {
                                    System.out.print((char) data);
                                }
                            } catch(Exception e){
                                System.out.println("!!!!!!!ERROR IN DOCUMENT ACTIVITY!!!!!!!!");
                                e.printStackTrace();
                            }
                        }
                    });


                    documentListLayout.addView(documentTextView);
                }
            } catch(Exception e){
                System.out.println("!!!!!!!ERROR IN DOCUMENT ACTIVITY!!!!!!!!");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);


        try {
            DocumentActivity.RequestGET req = new DocumentActivity.RequestGET();
            String url = "http://192.168.1.26:3000/documents";
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN TEACHER ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }
}
