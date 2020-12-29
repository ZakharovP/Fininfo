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


public class TeacherActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        // в начале, при загрузке, получаем от сервера список учителей
        try {
            // объект запроса
            //TeacherActivity.RequestGET req = new TeacherActivity.RequestGET();
            // создаем экземпляр запроса и выполняем его
            RequestGET req = new RequestGET(
                    new RequestGET.TaskListener() {
                        @Override
                        public void onFinished(String answer) {
                            // После окончания запроса и формирования строки ответа ее нужно распарсить и выполнить все необходимые действия,
                            // такие как переход на главный экран
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
                                    gd.setColor(Color.parseColor("#4466FF")); // цвет фона
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
            );
            String url = "https://ruz.fa.ru/api/search?type=person"; // URL для получения учителей
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN TEACHER ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }
}
