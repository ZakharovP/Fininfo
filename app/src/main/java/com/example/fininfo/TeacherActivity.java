package com.example.fininfo;

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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TeacherActivity extends BaseActivity {
    // класс запроса в асинхронном режиме списка преподавателей
    private class RequestGET extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            // переменная HTTP соединения
            HttpURLConnection connection = null;
            String path = params[0]; // получаем путь URL веб-сервера

            try {
                URL url = new URL(path);
                // создаем объект соединения
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET"); // это будет GET запрос

                // создаем объект чтения из серверных данных
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line; // строка для считывания

                // объект для хранения строк и их конкатенации
                StringBuilder content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    content.append(line); // добавляем строку в StringBuilder
                    content.append(System.lineSeparator()); // добавляем туда резделитель
                }
                // формируем результирующую строку из StringBuilder
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
            // Обработка ответа от сервера.
            // Здесь нужно заполнить список преподавателей
            super.onPostExecute(answer);
            try {
                JSONArray answerArray = new JSONArray(answer); // из строкового ответа формируем JSON объект
                for (int i = 0 ; i < answerArray.length(); i++) { // цикл по данным преподавателей
                    JSONObject obj = answerArray.getJSONObject(i);
                    String id = obj.getString("id"); // индетификатор учителя
                    String description = obj.getString("description"); // его описание
                    String label = obj.getString("label"); // метка

                    LinearLayout teacherListLayout = (LinearLayout)findViewById(R.id.teacherListLayout); // ищем контейнер в представлении
                    TextView teacherTextView = new TextView(TeacherActivity.this); // создаем новый текстовый элемент
                    teacherTextView.setText(label); // устанавливаем текст
                    teacherTextView.setPadding(10, 30, 30, 10);


                    // стили текста
                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(4, Color.parseColor("#AA2211")); // цвет границы
                    gd.setColor(Color.parseColor("#CCDEFF")); // цвет фона
                    teacherTextView.setBackground(gd);


                    // формируем строку расширенных данных о преподавателе
                    final String teacherInfo = String.format(" id: %s\n ИФО: %s\n Отдел: %s", id, label, description);

                    // добавляем обработчик клика по элементу списка
                    teacherTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // при клике на учителе переходим на экран с его расширенной информацией
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
        this.activityLayout = R.layout.activity_teacher;

        // в начале, при загрузке, получаем от сервера список учителей
        try {
            // объект запроса
            TeacherActivity.RequestGET req = new TeacherActivity.RequestGET();
            String url = "https://ruz.fa.ru/api/search?type=person"; // URL для получения учителей
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN TEACHER ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }
}
