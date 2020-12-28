package com.example.fininfo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringJoiner;

public class RegisterActivity extends BaseActivity {
    // класс асинхронной задачи для отправки запроса на сервер о регистрации
    private class Request extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null; // переменная соединения
            String path = params[0]; // получаем путь к запросу
            String parammetrs = params[1];

            String response = ""; // переменная для формирование ответа


            byte[] data = null;
            InputStream is = null;

            try {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // создаем экземпляр соединения
                conn.setRequestMethod("POST"); // для регистрации используем POST запрос
                conn.setDoOutput(true); // используем соединение для отправки данных
                conn.setDoInput(true); // используем соединение для входящих данных

                // заголовок длины тела запроса
                conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                OutputStream os = conn.getOutputStream();  // поток для отправки данных
                data = parammetrs.getBytes("UTF-8"); // данные в байтах
                os.write(data); // записываем в поток полученные байты
                data = null;

                conn.connect(); // установка соединение, запрос
                int responseCode = conn.getResponseCode(); // код ответа

                // поток для записи полученных байт с сервера из буфера
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                // если запрос был успешен
                if (responseCode == 200) {
                    is = conn.getInputStream(); // поток входящих данных

                    byte[] buffer = new byte[8192]; // буфер данных с сервера

                    int bytesRead; // кол-во байт
                    // в цикле считываем из входящего потока байты, пришедшие с сервера
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead); // записываем в поток baos данные буфера
                    }
                    data = baos.toByteArray(); // получаем записанные байты из потока baos
                    response = new String(data, "UTF-8");  // формируем строку ответа сервера
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
            // обработка ответа сервера
            super.onPostExecute(answer);
            try {
                // формируем JSON объект из строки ответа
                JSONObject answerData = new JSONObject(answer);

                // если регистрация прошла успешно
                if (answerData.getBoolean("success")) {
                    String userId = answerData.getString("userId"); // получаем ID нового юзера

                    // делаем переход на главный экран
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    // передаем параметр userId для дальнейшего использования в другиъ экранах
                    intent.putExtra("userId", userId);
                    startActivity(intent); // переход
                } else {
                    // если запрос не успешен, то выдаем сообщение об ошибке
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
        this.activityLayout = R.layout.activity_register;
    }

    // обработчик нажатия кнопки регистрации
    public void register(View view) {
        try {
            // получение всех полей формы
            Resources resources = getResources();
            StringJoiner joiner = new StringJoiner("&");
            String[] fields = new String[]{"login", "password", "firstName", "secondName", "thirdName"};
            String[] attrs = new String[]{"login", "password", "first_name", "second_name", "third_name"};

            for (int i = 0; i < fields.length; i++) {
                String key = attrs[i];
                int id = resources.getIdentifier(fields[i] + "Text", "id", this.getPackageName());
                String value = ((EditText) findViewById(id)).getText().toString().trim();
                joiner.add(String.format("%s=%s", key, value));
            }

            // формируем тело  запроса
            String params = joiner.toString();

            // создаем запрос к HTTP серверу по URL
            String url = "http://10.0.2.2:3000/register";
            RegisterActivity.Request req = new RegisterActivity.Request();

            // отправка запроса
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN REGISTER ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }

    // обработка нажатия кнопки Назад и переход на экран логина
    public void goBack(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
