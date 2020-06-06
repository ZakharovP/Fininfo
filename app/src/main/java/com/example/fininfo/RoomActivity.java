package com.example.fininfo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RoomActivity extends AppCompatActivity {
    int userId;
    private class RequestPost extends AsyncTask<String, Integer, String> {
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
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                OutputStream os = conn.getOutputStream();
                data = parammetrs.getBytes("UTF-8");
                os.write(data);
                data = null;

                conn.connect();
                int responseCode= conn.getResponseCode();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                if (responseCode == 200) {
                    is = conn.getInputStream();

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    data = baos.toByteArray();
                    response = new String(data, "UTF-8");
                }

                return response;
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
                JSONObject answerData = new JSONObject(answer);

                if (answerData.getBoolean("success")) {
                    String title = answerData.getString("title");
                    final Integer roomId = answerData.getInt("id");

                    LinearLayout roomListLayout = (LinearLayout)findViewById(R.id.roomListLayout);

                    TextView roomTextView = new TextView(RoomActivity.this);
                    roomTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goChatRoom(roomId);
                            //System.out.println("CLICKED!!!!!!!!!!" + String.valueOf(roomId));
                        }
                    });

                    roomTextView.setText(title);
                    roomListLayout.addView(roomTextView);
                } else {

                    new AlertDialog.Builder(RoomActivity.this)
                            .setTitle("Ошибка авторизации")
                            .setMessage(answerData.getString("error"))
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();


                }
            } catch(Exception e){
                System.out.println("!!!!!!!ERROR IN ROOMS ACTIVITY!!!!!!!!");
                e.printStackTrace();
            }
        }
    }


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
                    LinearLayout roomListLayout = (LinearLayout)findViewById(R.id.roomListLayout);
                    String messageText = obj.getString("title");
                    TextView roomTextView = new TextView(RoomActivity.this);
                    roomTextView.setText(messageText);
                    roomListLayout.addView(roomTextView);


                    final int roomId = obj.getInt("ID_ROOM");

                    roomTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goChatRoom(roomId);
                        }
                    });
                }
            } catch(Exception e){
                System.out.println("!!!!!!!ERROR IN ROOMS ACTIVITY!!!!!!!!");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);


        Intent intent = getIntent();
        userId = Integer.parseInt(intent.getStringExtra("userId"));


        try {
            RoomActivity.RequestGET req = new RoomActivity.RequestGET();
            String url = "http://10.0.2.2:3000/rooms";
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN LOGIN ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }

    public void createRoom(View view) {
        try {
            RoomActivity.RequestPost req = new RoomActivity.RequestPost();
            String url = "http://10.0.2.2:3000/create_room";
            EditText roomFiled = ((EditText) findViewById(R.id.newRoomText));
            String roomTitle = roomFiled.getText().toString().trim();
            if (roomTitle.length() == 0) {
                return;
            }
            roomFiled.setText("");
            String params = String.format("room=%s", roomTitle);
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN LOGIN ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }

    public void goChatRoom(int roomId) {
        //System.out.println("roomId = " + Integer.toString(roomId));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle b = new Bundle();
        b.putInt("roomId", roomId);
        b.putInt("userId", userId);
        intent.putExtras(b);
        startActivity(intent);
    }
}
