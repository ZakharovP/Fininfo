package com.example.fininfo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

public class RoomActivity extends BaseActivity {
    int userId; // идентификатор юзера, получаемый из главного экрана

    // класс для POST запросов, которые создают чат-комнаты

    private void handleAnswer(String answer) {
        try {
            // получаем JSON объект ответа
            JSONObject answerData = new JSONObject(answer);

            String queryType = answerData.getString("type");

            System.out.println("queryType = " + queryType);
            System.out.println("queryType = " + queryType.equals("room_password"));

            if (queryType.equals("create_room")) {
                // если создание комнаты было успешным
                if (answerData.getBoolean("success")) {

                    // получаем заголовок комнаты
                    String title = answerData.getString("title");
                    // и ее идентификатор
                    final Integer roomId = answerData.getInt("id");

                    // находим слой со списком комнат
                    LinearLayout roomListLayout = (LinearLayout)findViewById(R.id.roomListLayout);

                    // создаем текстовый элемент с названием новой комнаты
                    TextView roomTextView = new TextView(RoomActivity.this);
                    roomTextView.setText(title); // устанавливаем текст элементу
                    roomListLayout.addView(roomTextView); // добавляем элемент в слой с комнатами

                    // при клике на комнату вешаем обработчик событий
                    roomTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // при клике на комнату - переходим на экран чата с нужным id комнаты
                            goChatRoom(roomId);
                        }
                    });
                } else {
                    // если создание комнаты прошло неуспешно, выводим об этом сообщение
                    new AlertDialog.Builder(RoomActivity.this)
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
            } else if (queryType.equals("room_password")) {
                final String password = answerData.getString("password");
                final Integer roomId = answerData.getInt("roomId");
                System.out.println(">>>PASSWORD 1: " + password);
                System.out.println(">>>PASSWORD 2: " + password.length());

                if (password.length() > 0) {
                    System.out.println(">>>PASSWORD  REQUEST");

                    AlertDialog.Builder alert = new AlertDialog.Builder(RoomActivity.this, R.style.AlertDialogCustom);


                    alert.setTitle("Пароль");
                    alert.setMessage("Введите пароль для этой комнаты");

                    final EditText input = new EditText(RoomActivity.this);
                    alert.setView(input);

                    input.setTextColor(ContextCompat.getColor(RoomActivity.this, R.color.colorText));

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            Boolean result = password.equals(value);

                            if (result) {
                                // создаем intent для перехода к чат-экрану
                                // туда передаем ID комнаты и юзера
                                Intent intent = new Intent(RoomActivity.this, ChatActivity.class);
                                Bundle b = new Bundle();
                                b.putInt("roomId", roomId);
                                b.putInt("userId", userId);
                                intent.putExtras(b);
                                startActivity(intent);
                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(RoomActivity.this, R.style.AlertDialogCustom).create();
                                alertDialog.setTitle("Ошибка");
                                alertDialog.setMessage("Неправильно введене пароль!");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }

                            System.out.println("USER INPUT PASS = " + value);
                            System.out.println(password.equals(value));
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            System.out.println("USER INPUT CANCELED!!!");
                        }
                    });

                    alert.show();
                } else {
                    // создаем intent для перехода к чат-экрану
                    // туда передаем ID комнаты и юзера
                    Intent intent = new Intent(RoomActivity.this, ChatActivity.class);
                    Bundle b = new Bundle();
                    b.putInt("roomId", roomId);
                    b.putInt("userId", userId);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            }
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN ROOMS ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        //this.activityLayout = R.layout.activity_room;

        // получаем ID юзера из прошлого активити
        Intent intent = getIntent();
        userId = Integer.parseInt(intent.getStringExtra("userId"));

        // делаем запрос на получение комнат
        try {

            RequestGET req = new RequestGET(
                    new RequestGET.TaskListener() {
                        @Override
                        public void onFinished(String answer) {
                            try {
                                // получаем слой комнат из представления
                                LinearLayout roomListLayout = (LinearLayout)findViewById(R.id.roomListLayout);

                                JSONArray answerArray = new JSONArray(answer); // JSON массив всех комнат

                                for (int i = 0 ; i < answerArray.length(); i++) {
                                    // объект комнаты
                                    JSONObject obj = answerArray.getJSONObject(i);

                                    // получаем название комнаты
                                    String messageText = obj.getString("title");

                                    // создаем текстовый элемент, хранящий название комнаты
                                    TextView roomTextView = new TextView(RoomActivity.this);
                                    roomTextView.setText(messageText); // устанавливаем текст
                                    roomListLayout.addView(roomTextView); // добавляем элемент в слой


                                    final int roomId = obj.getInt("ID_ROOM"); // получаем id комнаты

                                    // ставим обработчик клика на название комнаты - переход на активити чата
                                    roomTextView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            goChatRoom(roomId);
                                        }
                                    });
                                }
                            } catch(Exception e){
                                System.out.println("!!!!!!!ERROR IN ROOMS ACTIVITY!!!!!!!!");
                                e.printStackTrace();
                            }
                        }
                    }
            );


            String url = "http://10.0.2.2:3000/rooms";
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN LOGIN ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }

    // обработчик клика на создание новой комнаты
    public void createRoom(View view) {
        try {
            // создаем объект запроса на сервер


            RequestPOST req = new RequestPOST(
                    new RequestPOST.TaskListener() {
                        @Override
                        public void onFinished(String answer) {
                            handleAnswer(answer);
                        }
                    }
            );

            String url = "http://10.0.2.2:3000/create_room";

            // находим поле с названием новой комнаты
            EditText roomField = ((EditText) findViewById(R.id.newRoomText));
            EditText passwordField = ((EditText) findViewById(R.id.newRoomPassword));

            // получаем название новой комнаты
            String roomTitle = roomField.getText().toString().trim();

            // получаем пароль для комнаты
            String roomPassword = passwordField.getText().toString().trim();

            // если название пустое - ничего не делаем
            if (roomTitle.length() == 0) {
                return;
            }

            // обнуляем текстовое значение поля новой комнаты
            roomField.setText("");
            passwordField.setText("");

            // создаем тело POST запроса
            String params = String.format("room=%s&password=%s", roomTitle, roomPassword);

            // выполняем запрос
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN LOGIN ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }

    // переход к чату
    public void goChatRoom(int roomId) {
        try {
            // запрос пароля
            RequestPOST req = new RequestPOST(
                    new RequestPOST.TaskListener() {
                        @Override
                        public void onFinished(String answer) {
                            handleAnswer(answer);
                        }
                    }
            );
            String url = "http://10.0.2.2:3000/room_password";

            // создаем тело POST запроса
            String params = String.format("roomId=%s", roomId);

            // выполняем запрос
            req.execute(url, params).get();
            /*
                // создаем intent для перехода к чат-экрану
                // туда передаем ID комнаты и юзера
                Intent intent = new Intent(this, ChatActivity.class);
                Bundle b = new Bundle();
                b.putInt("roomId", roomId);
                b.putInt("userId", userId);
                intent.putExtras(b);
                startActivity(intent);
            */
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN LOGIN ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }

    }
}
