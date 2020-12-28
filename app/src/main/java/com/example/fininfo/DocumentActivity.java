package com.example.fininfo;

import android.app.DownloadManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DocumentActivity extends BaseActivity {
    // класс для получения списка документов в асинхронном режиме
    private class RequestGET extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null;
            String path = params[0]; // путь URL на веб-сервере

            try {
                URL url = new URL(path);
                // создаем объект соединения
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET"); // метод GET
                // объект для считывания данных ответа сервера
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line; // строка-порция данных
                StringBuilder content = new StringBuilder(); // объект для хранения строк данных

                while ((line = in.readLine()) != null) { // считываем в цикле из ридера данные и записываем имх в line
                    content.append(line); // добавляем line в StringBuilder
                    content.append(System.lineSeparator()); // добавляем разделитель в StringBuilder
                }

                return content.toString(); // возвращаем строку данных из StringBuilder

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        this.activityLayout = R.layout.activity_document;

        // при инициализации делаем запрос на сервер для получения списка документов
        try {
            DocumentActivity.RequestGET req = new DocumentActivity.RequestGET();
            String url = "http://192.168.1.26:3000/documents";
            String params = "";
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN TEACHER ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }
    }
}
