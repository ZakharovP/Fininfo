package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {
    private class Request extends AsyncTask<String, Integer, String> {
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
                Log.i("app", "!!!result = " + answer);

                JSONObject answerData = new JSONObject(answer);
                Log.i("app", answerData.getString("success"));




                if (answerData.getBoolean("success")) {
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("userId", 1);
                    startActivity(intent);
                } else {

                    new AlertDialog.Builder(RegisterActivity.this)
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
                System.out.println("!!!!!!!ERROR IN REGISTER ACTIVITY!!!!!!!!");
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void register(View view) {
        try {
            String login = ((EditText) findViewById(R.id.loginText)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.passwordText)).getText().toString().trim();
            String firstName = ((EditText) findViewById(R.id.firstNameText)).getText().toString().trim();
            String secondName = ((EditText) findViewById(R.id.secondNameText)).getText().toString().trim();
            String thirdName = ((EditText) findViewById(R.id.thirdNameText)).getText().toString().trim();

            String url = "http://10.0.2.2:3000/register";
            RegisterActivity.Request req = new RegisterActivity.Request();


            String params = String.format("login=%s&password=%s&first_name=%s&second_name=%s&third_name=%s", login, password, firstName, secondName, thirdName);
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN REGISTER ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
