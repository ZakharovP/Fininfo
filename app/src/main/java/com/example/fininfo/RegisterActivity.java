package com.example.fininfo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.StringJoiner;

public class RegisterActivity extends BaseActivity {
    // класс асинхронной задачи для отправки запроса на сервер о регистрации

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //this.activityLayout = R.layout.activity_register;
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


            RequestPOST req = new RequestPOST(
                new RequestPOST.TaskListener() {
                    @Override
                    public void onFinished(String answer) {
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
            );
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
