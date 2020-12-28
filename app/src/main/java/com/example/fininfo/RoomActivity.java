package com.example.fininfo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RoomActivity extends BaseActivity {
    int userId; // идентификатор юзера, получаемый из главного экрана

    // класс для POST запросов, которые создают чат-комнаты
    private class RequestPost extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null;  // переменная соединения
            String path = params[0];  // путь на веб-сервере
            String parammetrs = params[1];

            String response = ""; // строка ответа

            byte[] data = null;
            InputStream is = null;

            try {
                URL url = new URL(path);

                // создаем объект запроса
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST"); // устанавливаем метод POST
                conn.setDoOutput(true); // возможность отправки тела POST запроса
                conn.setDoInput(true); // возможность приема данных

                // заголовок
                conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                OutputStream os = conn.getOutputStream(); // поток для отправки данных
                data = parammetrs.getBytes("UTF-8"); // тело запроса в байтах
                os.write(data); // запись данных в поток

                conn.connect(); // запрос
                int responseCode = conn.getResponseCode(); // получаем код ответа

                //поток, в который будем записывать данные с сервера из буфера
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                if (responseCode == 200) { // если запрос успешен
                    is = conn.getInputStream(); // входной поток с сервера

                    byte[] buffer = new byte[8192]; // буфер
                    int bytesRead; // кол-во считанных байт

                    // пока есть что считывать - считываем это в буфер и записмываем в поток baos
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    data = baos.toByteArray(); // получаем данные в байтах из потока
                    response = new String(data, "UTF-8"); // формируем строку
                }
                return response;
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
        protected void onPostExecute(String answer) {
            super.onPostExecute(answer);
            // при получении ответа его надо обработать
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
    }

    // класс для получения комнат чатов в асинхронном режиме
    private class RequestGET extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null; // переменная соединения
            String path = params[0]; // url запроса

            try {
                URL url = new URL(path);
                // объект соединения
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET"); // указываем, что будет метод GET

                // объект чтения данных с сервера
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line; // строка, в нее записываем результат считывания
                StringBuilder content = new StringBuilder(); // хранит и конкатенирует строки

                // в цикле считываем строки с сервера и добавляем их в StringBuilder
                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }

                // возвращаем итоговую строку данных
                return content.toString();

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
        protected void onPostExecute(String answer) {
            super.onPostExecute(answer);
            // обработка строки чат-комнат
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        this.activityLayout = R.layout.activity_room;

        // получаем ID юзера из прошлого активити
        Intent intent = getIntent();
        userId = Integer.parseInt(intent.getStringExtra("userId"));

        // делаем запрос на получение комнат
        try {
            RoomActivity.RequestGET req = new RoomActivity.RequestGET();
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
            RoomActivity.RequestPost req = new RoomActivity.RequestPost();
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
            RoomActivity.RequestPost req = new RoomActivity.RequestPost();
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
