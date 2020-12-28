package com.example.fininfo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;


// класс асинхронной задачи для отправки запроса на сервер
class Request extends AsyncTask<String, Integer, String> {

    public interface TaskListener {
        void onFinished(String result);
    }

    private final TaskListener taskListener;

    Request(TaskListener listener) {
        this.taskListener = listener;
    }



    @Override
    protected String doInBackground(String... params) {

        HttpURLConnection connection = null;
        String path = params[0]; // получаем путь url для запроса
        String parammetrs = params[1];

        String response = ""; // строка для ответа сервера


        byte[] data = null; // считываемые двоичные данные
        InputStream is = null; // поток входящих байт ответа сервера

        try {
            // устанавливаем соединение
            URL url = new URL(path); // объект url
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");  // POST запрос
            conn.setDoOutput(true); // нужно для отправки тела POST запроса
            conn.setDoInput(true);

            // заголовок HTTP
            conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
            OutputStream os = conn.getOutputStream(); // выходной поток на отправку к серверу
            data = parammetrs.getBytes("UTF-8"); // получение байт тела запроса
            os.write(data); // отправка на сервер
            data = null;

            conn.connect();
            int responseCode = conn.getResponseCode();  // получаем код ответа от сервера

            // поток для записи полученных данных
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // если запрос успешен
            if (responseCode == 200) {
                is = conn.getInputStream();

                // буфер для записи порций пришедших данных
                byte[] buffer = new byte[8192];
                int bytesRead; // кол-ов считанных байт

                // считываем данные
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                data = baos.toByteArray();
                // формируем строку ответа от сервера
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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(this.taskListener != null) {
            this.taskListener.onFinished(result);
        }
    }
}


public class LoginActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.activityLayout = R.layout.activity_login;
    }

    // обработчик нажатия клавиши
    public void logIn(View view) {
        try {
            // получаем поля логина и пароля из формы
            String login = ((EditText) findViewById(R.id.loginText)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.passwordText)).getText().toString().trim();

            // URL веб-сервера для входа
            String url = "http://10.0.2.2:3000/login";

            // создаем экземпляр запроса и выполняем его
            Request req = new Request(
                new Request.TaskListener() {
                    @Override
                    public void onFinished(String answer) {
                        // После окончания запроса и формирования строки ответа ее нужно распарсить и выполнить все необходимые действия,
                        // такие как переход на главный экран
                        try {
                            // из строки формируем JSON объект
                            JSONObject answerData = new JSONObject(answer);

                            // если авторизация успешна
                            if (answerData.getBoolean("success")) {
                                String userId = answerData.getString("userId");  // получаем ID юзера

                                // формируем Intent для перехода на главный экран
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("userId", userId); // необходимо сохранить параметр userId
                                startActivity(intent);
                            } else {
                                // если авторизация была неуспешной, то выводим сообщение об этом
                                new AlertDialog.Builder(LoginActivity.this)
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
                            System.out.println("!!!!!!!ERROR IN LOGIN ACTIVITY!!!!!!!!");
                            e.printStackTrace();
                        }
                    }
                }
            );

            String params = String.format("login=%s&password=%s", login, password);
            req.execute(url, params).get();

        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN LOGIN ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }

    public void goRegister(View view) {
        // при клике на кнопке Регистрации переход на activity с регистрацией
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}