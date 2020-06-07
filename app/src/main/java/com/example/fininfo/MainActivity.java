package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    String userId; // переменная для хренения ID юзера после авторизации, передается в следующие экраны

    // класс для запроса и получения данных группы
    private class Request extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = params[0]; // получаем URL запроса
            HttpURLConnection connection = null;

            try {
                URL obj = new URL(url);
                // создаем объект соединениея
                connection = (HttpURLConnection) obj.openConnection();
                connection.setRequestMethod("GET"); // устанавливаем метод GET

                // объект чтения данных с сервера
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine; // строка данных
                StringBuffer content = new StringBuffer(); // объект для хренения и конкатенации строк

                while ((inputLine = in.readLine()) != null) { // в цикле считываем по строке и добавляем в StringBuilder
                    content.append(inputLine);
                    content.append(System.lineSeparator()); // добавялем разделитель
                }
                in.close();
                return content.toString(); // формируем строку
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
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId"); // получаем userId из экрана авторизации

        try {
            String url = "https://ruz.fa.ru/api/schedule/group/8892?lng=1"; // URL для получения данных
            Request req = new Request(); // создаем объект запроса

            String s_classes = req.execute(url).get(); // выполняем запрос

            // парсим ответ
            JSONArray classArray = new JSONArray(s_classes);
            ArrayList<String> classes_t = new ArrayList<>();
            for (int i = 0; i < classArray.length(); i++) {
                // извлекаем данные для каждого элемента
                String startTime = classArray.getJSONObject(i).getString("beginLesson");
                String endTime = classArray.getJSONObject(i).getString("endLesson");
                String dayOfWeek = classArray.getJSONObject(i).getString("dayOfWeekString");
                String location = classArray.getJSONObject(i).getString("building");
                String teacherName = classArray.getJSONObject(i).getString("lecturer");
                String groupName = classArray.getJSONObject(i).getString("groupOid");

                // формируем строку информации
                String class_ = String.format("%s - %s (%s), %s\n%s\n%s", startTime, endTime, dayOfWeek, groupName, teacherName, location);
                classes_t.add(class_);
            }

            // массив строк данных
            String[] classes_a = new String[classes_t.size()];
            final String[] classes = classes_t.toArray(classes_a);


            ListView itemList = (ListView) findViewById(R.id.itemsListView); // получаем список из представления

            // добавляем данные списка classes в ListView
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, classes);
            itemList.setAdapter(itemsAdapter);
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR!!!!!!!!");
            e.printStackTrace();
        }

    }

    // переход на страницу с предметами через intent
    public void goSubjects(View view) {
        Intent intent = new Intent(this, SubjectActivity.class);
        startActivity(intent);
    }

    // переход на страницу чат комнат через intent
    public void goChats(View view) {
        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra("userId", userId); // передаем ID юзера
        startActivity(intent);
    }

    // переход на страницу документов через intent
    public void goDocs(View view) {
        Intent intent = new Intent(this, DocumentActivity.class);
        startActivity(intent);
    }

    // переход на страницу преподавателей через intent
    public void goTeachers(View view) {
        Intent intent = new Intent(this, TeacherActivity.class);
        startActivity(intent);
    }
}