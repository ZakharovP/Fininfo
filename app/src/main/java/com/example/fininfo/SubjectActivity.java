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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubjectActivity extends AppCompatActivity {
    // класс для получения в асинхронном режиме списка предметов
    private class RequestGET extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null; // переменная соединения
            String path = params[0]; // путь запроса к серверу

            try {
                URL url = new URL(path);
                // создаем соединение
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET"); // делаем GET запрос

                // объект для считывания данных из входящего с сервера потока
                BufferedReader in = new BufferedReader( new InputStreamReader(conn.getInputStream()));

                String line; // строка для записи считываемых данных
                StringBuilder content = new StringBuilder();  // объект для хранения строк ответа

                // считываем в цикле данные и записываем их в line, пока она не пустая
                while ((line = in.readLine()) != null) {
                    content.append(line); // добавляем данные в builder
                    content.append(System.lineSeparator()); // добавляем разделитель в builder
                }

                return content.toString(); // преобразуем объект в строку и возвращаем ее

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
            // парсим ответ в виде строки
            super.onPostExecute(answer);
            try {
                //преобразовываем в JSON
                JSONArray answerArray = new JSONArray(answer);
                for (int i = 0 ; i < answerArray.length(); i++) {
                    // получаем JSON объект для предмета
                    JSONObject obj = answerArray.getJSONObject(i);

                    // добываем данные для текущего предмета
                    String lecturer = obj.getString("lecturer");
                    String discipline = obj.getString("discipline");
                    String detailInfo = obj.getString("detailInfo");

                    // получаем слой, куда будем добавлять текстовые данные предметов
                    LinearLayout subjectListLayout = (LinearLayout)findViewById(R.id.subjectListLayout);

                    // текстый элемент для предмета
                    TextView subjectTextView = new TextView(SubjectActivity.this);
                    subjectTextView.setText(lecturer + "\n" + discipline + "\n" + detailInfo); // ставим текст
                    subjectListLayout.addView(subjectTextView); // добавляем элемент в слой


                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(4, Color.RED); // стиль границы
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

        // при загрузке экрана делаем запрос на получение списка предметов
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
