package com.example.fininfo;


import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;


public class SubjectActivity extends BaseActivity {
    // класс для получения в асинхронном режиме списка предметов
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        //this.activityLayout = R.layout.activity_subject;

        // при загрузке экрана делаем запрос на получение списка предметов
        try {
            RequestGET req = new RequestGET(
                    new RequestGET.TaskListener() {
                        @Override
                        public void onFinished(String answer) {
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
            );




            String url = "https://ruz.fa.ru/api/schedule/group/8892?lng=1";
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN SUBJECT ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }
}
