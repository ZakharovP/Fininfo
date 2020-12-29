package com.example.fininfo;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// класс асинхронной задачи для отправки запроса на сервер
class RequestPOST extends AsyncTask<String, Integer, String> {

    public interface TaskListener {
        void onFinished(String result);
    }

    private final TaskListener taskListener;

    RequestPOST(TaskListener listener) {
        this.taskListener = listener;
    }



    @Override
    protected String doInBackground(String... params) {

        HttpURLConnection connection = null;
        String path = params[0]; // получаем путь url для запроса
        String parameters = params[1];

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
            conn.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
            OutputStream os = conn.getOutputStream(); // выходной поток на отправку к серверу
            data = parameters.getBytes("UTF-8"); // получение байт тела запроса
            os.write(data); // отправка на сервер
            //data = null;

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