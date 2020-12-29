package com.example.fininfo;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


// класс асинхронной задачи для отправки запроса на сервер
class RequestGET extends AsyncTask<String, Integer, String> {

    public interface TaskListener {
        void onFinished(String result);
    }

    private final TaskListener taskListener;

    RequestGET(TaskListener listener) {
        this.taskListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {

        HttpURLConnection conn = null;
        String path = params[0]; // путь URL на веб-сервере

        try {
            URL url = new URL(path);
            // создаем объект соединения
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); // метод GET
            // объект для считывания данных ответа сервера
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line; // строка-порция данных
            StringBuilder content = new StringBuilder(); // объект для хранения строк данных

            while ((line = in.readLine()) != null) { // считываем в цикле из ридера данные и записываем имх в line
                content.append(line); // добавляем line в StringBuilder
                content.append(System.lineSeparator()); // добавляем разделитель в StringBuilder
            }

            return content.toString(); // возвращаем строку данных из StringBuilder

        } catch (Exception e) {
            System.out.println("ERROR!!!!!");
            System.out.println(e.toString());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
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