package com.example.fininfo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

public class LoginActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
            RequestPOST req = new RequestPOST(
                new RequestPOST.TaskListener() {
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