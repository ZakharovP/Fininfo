package com.example.fininfo;

import android.app.DownloadManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;



public class DocumentActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        // при инициализации делаем запрос на сервер для получения списка документов
        try {
            RequestGET req = new RequestGET(
                    new RequestGET.TaskListener() {
                    @Override
                    public void onFinished(String answer) {
                        // После окончания запроса и формирования строки ответа ее нужно распарсить и выполнить все необходимые действия,
                        // такие как переход на главный экран
                        try {
                            // обработка данных с сервера в виде строки
                            // получаем масив документов
                            JSONArray answerArray = new JSONArray(answer);
                            for (int i = 0 ; i < answerArray.length(); i++) {
                                // получаем объект документа
                                JSONObject document = answerArray.getJSONObject(i);

                                // данные документа (имя файла)
                                String filename = document.getString("filename");

                                // находим слой для размещения списка новых текстовых элементов
                                LinearLayout documentListLayout = (LinearLayout)findViewById(R.id.documentListLayout);

                                // создаем новый текстовый элемент
                                TextView documentTextView = new TextView(DocumentActivity.this);
                                documentTextView.setText(filename); // ставим текст
                                documentTextView.setPadding(10, 30, 30, 10); // отступы

                                // стили
                                GradientDrawable gd = new GradientDrawable();
                                gd.setStroke(4, Color.parseColor("#EEEEEE"));
                                gd.setColor(Color.parseColor("#CCDEFF"));
                                documentTextView.setBackground(gd);

                                // добавялем новый текстовый документ в слой
                                documentListLayout.addView(documentTextView);

                                final String documentInfo = filename;

                                // добавляем обработчик на событие клика по документу, чтобы скачать файл
                                documentTextView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        // делаем в этом же потоке, поэтому блокируем сообщения об ошибках
                                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                        StrictMode.setThreadPolicy(policy);

                                        try {
                                            // создаем менеджер скачивания
                                            DownloadManager downloadmanager = (DownloadManager) getSystemService(DocumentActivity.DOWNLOAD_SERVICE);
                                            // url документа
                                            Uri uri = Uri.parse("http://192.168.1.26:3000/download/" + documentInfo);

                                            // делаем запрос
                                            DownloadManager.Request request = new DownloadManager.Request(uri);

                                            // настройки
                                            request.setTitle(documentInfo);
                                            request.setDescription("Downloading");
                                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                            request.setVisibleInDownloadsUi(false);

                                            // куда скачиваем
                                            String destination = "";
                                            request.setDestinationUri(Uri.parse("file://" + Environment.getExternalStorageDirectory() + destination  + "/"  + documentInfo));

                                            downloadmanager.enqueue(request); // запуск скачивания
                                        } catch(Exception e){
                                            System.out.println("!!!!!!!ERROR IN DOCUMENT ACTIVITY!!!!!!!!");
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        } catch(Exception e){
                            System.out.println("!!!!!!!ERROR IN DOCUMENT ACTIVITY!!!!!!!!");
                            e.printStackTrace();
                        }

                    }
                }
            );


            String url = "http://192.168.1.26:3000/documents";
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN TEACHER ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }
}
