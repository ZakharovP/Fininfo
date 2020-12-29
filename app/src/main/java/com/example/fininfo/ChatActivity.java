package com.example.fininfo;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.UUID;

public class ChatActivity extends BaseActivity {
    private Socket socket; // сокет TCP сервера
    private String ServerIP = "10.0.2.2"; // IP для TCP сервера
    private static final int ServerPort = 3001; // порт длл TCP сервера
    InputStream in; // поток входных данных TCP соединения
    OutputStream out; // поток выходных данных для TCP
    ImageView imgView; // элемент с превью загруженной картинки
    TextView filePathTextView; // элемент с текстом выбранного файла
    public static final int FILE_RESULT_CODE = 1; // код расположения при поиске документов
    public static final int GALLERY_PHOTO = 111; // код расположения при приске картинок

    String imagePath = ""; // путь к выбранной картинке
    String filePath = ""; // путь к выбранному файлу
    private int roomId; // ID текущей комнаты
    private int userId = 4; // ID текущего юзера

    // класс загружает картинку с сервера для текущего сообщения
    public class RequestImage extends AsyncTask<String, Void, Bitmap> {
        ImageView newChatImageView;
        @Override
        public Bitmap doInBackground(String... params) {
            // создаем новый элемент рисунка
            newChatImageView = new ImageView(ChatActivity.this);

            // ставим размеры рисунка
            newChatImageView.setLayoutParams(new LinearLayout.LayoutParams(150, 150));

            // ищем в представлении слой всех сообщений
            LinearLayout chatMessagesLayout = (LinearLayout)findViewById(R.id.chatMessagesLayout);

            // добавляем туда рисунок
            chatMessagesLayout.addView(newChatImageView);

            // берем URL картинки
            String urldisplay = params[0];

            // делаем запрос для рисунка
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            // возвращаем объект BitMap рисунка
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // после загрузки устанавливаем bitmap на элемент картинки
            newChatImageView.setImageBitmap(result);
        }
    }

    // функция реализовывает получения правильного пути выбранного файла
    public final static class GetFilePathFromDevice {
        public static String getPath(final Context context, final Uri uri) {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

                // формируем адрес в зависимости от типа/источника документа
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();
                return getDataColumn(context, uri, null, null);
            }
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
            return null;
        }

        public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(index);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        }
        // вспомогательные функции для предыдущей для определения источника файла
        public static boolean isExternalStorageDocument(Uri uri) {
            return "com.android.externalstorage.documents".equals(uri.getAuthority());
        }

        public static boolean isDownloadsDocument(Uri uri) {
            return "com.android.providers.downloads.documents".equals(uri.getAuthority());
        }

        public static boolean isMediaDocument(Uri uri) {
            return "com.android.providers.media.documents".equals(uri.getAuthority());
        }

        public static boolean isGooglePhotosUri(Uri uri) {
            return "com.google.android.apps.photos.content".equals(uri.getAuthority());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //this.activityLayout = R.layout.activity_chat;

        // при инициализации экрана получаем ID комнаты и юзера
        Bundle b = getIntent().getExtras();
        roomId = -1;
        if (b != null) {
            roomId = b.getInt("roomId");
            userId = b.getInt("userId");
        }

        // находим элементы превью для картинки и документа
        imgView = (ImageView) findViewById(R.id.imageView);
        filePathTextView = (TextView) findViewById(R.id.filePathTextView);

        // запускаем поток установки TCP соединения и обработки входящих данных по нему
        new Thread(new ClientThread()).start();

        // запрашиваем нужные права на возможность выбирать файлы (чтение и запись)
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    // при уходе с экрана нужно закрыть TCP соединение
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (Exception e) {
           System.out.println("Error during closing a socket");
        }
    }


    // при ыыборе файла нужно сделать обработку - показать нужное на экране
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PHOTO) { // если выбирали картинку
            if (requestCode == GALLERY_PHOTO && resultCode == Activity.RESULT_OK) {
                if (data.getData() != null) {
                    // нужно поставить ее на превью
                    imagePath = GetFilePathFromDevice.getPath(ChatActivity.this, data.getData()); // достаем путь
                    Bitmap bm = BitmapFactory.decodeFile(imagePath); // по найденному пути создаем Bitmap
                    imgView.setImageBitmap(bm); // устанавливаем bitmap для превью
                }
            }
        } else if (requestCode == FILE_RESULT_CODE) { // если выбирали файл
            if (data.getData() != null) {
                filePath = GetFilePathFromDevice.getPath(ChatActivity.this, data.getData()); // достаем путь

                // оставляем только название файла (без пути к нему)
                String[] arr = filePath.split("/");
                String filename = arr[arr.length - 1];
                filePathTextView.setText("Выбранный документ: " + filename); // устанавливаем имя файла в строку выбранного файла
            }
        }

    }

    // обработчик TCP соединения
    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                // создаем новый сокет
                socket = new Socket(ServerIP, ServerPort);

                // получаем входной и выходной потоки
                in = socket.getInputStream();
                out = socket.getOutputStream();

                int count;
                byte[] buffer = new byte[8192]; // буфер

                String body = ""; // накапливаем, то что приходит


                getMessages(); // получаем сообщения (историю) чата


                while ((count = in.read(buffer)) > 0) { // считываем в буфер из потока входа
                    byte[] slice = Arrays.copyOfRange(buffer, 0, count); // получаем массив новых байт
                    String line = new String(slice, "UTF-8"); // преобразуем в строку
                    body += line; // прибавляем к накопленному значению
                    int sepIndex = body.lastIndexOf("\0"); // ищем разделитель сообщений
                    if (sepIndex > -1) { // если найден
                        // делаем срез, содержащий новые сообщения, разбиваем строку на сообщения
                        String[] datas = body.substring(0, sepIndex).split("\0");
                        body = body.substring(sepIndex + 1, body.length()); // остаток
                        for (int i = 0; i < datas.length; i++) { // проходимпо сообщениям в цикле
                            String stringData = datas[i]; // сообщенеи в виде строки

                            // получаем тип сообщения - текстовые или файлы
                            char dataType = stringData.charAt(stringData.length() - 1);
                            if (dataType == '\1') { // если текстовое сообщение
                                // формируем JSON объект из строки
                                JSONObject data = new JSONObject(stringData.substring(0, stringData.length() - 1));
                                final String messageText = data.getString("text");
                                final String messageImage = data.getString("image");
                                final String messageFile = data.getString("file");
                                final String messageUser = data.getString("user");

                                // в отдельном потоке добавим его к списку наших сообщений в чат-комнате
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // находим слой сообщений
                                        LinearLayout chatMessagesLayout = (LinearLayout)findViewById(R.id.chatMessagesLayout);

                                        // создаем текстовый элемент сообщения для имени пользователя и добавляем его в слой
                                        TextView userTextView = new TextView(ChatActivity.this);
                                        userTextView.setText(messageUser);
                                        userTextView.setTextColor(Color.BLUE);
                                        chatMessagesLayout.addView(userTextView);

                                        // создаем текстовый элемент сообщения для текста и добавляем его в слой
                                        TextView messageTextView = new TextView(ChatActivity.this);
                                        messageTextView.setText(messageText);
                                        chatMessagesLayout.addView(messageTextView);

                                        // если у сообщения есть картинка
                                        if (messageImage.length() > 0) {
                                            // создаем объект запроса и получаем картинку с веб сервера
                                            String url = "http://10.0.2.2:3000/static/img/" + messageImage;

                                            ChatActivity.RequestImage req = new ChatActivity.RequestImage();
                                            String params = "";
                                            try {
                                                req.execute(url, params).get();
                                            } catch(Exception e){
                                                System.out.println("An error in getting an image...");
                                                e.printStackTrace();
                                            }
                                        }

                                        // если есть прикрепленный документ
                                        if (messageFile.length() > 0) {
                                            // смоздаем текстовый элемент с именем файла и добавляем его в слой
                                            TextView documentTextView = new TextView(ChatActivity.this);
                                            documentTextView.setText(messageFile);
                                            documentTextView.setTextColor(0xFFFF0000);
                                            chatMessagesLayout.addView(documentTextView);

                                            // вешаем обработчик лика на нем - для загрузки файла
                                            final String filename = messageFile;
                                            documentTextView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    // для возможности загрузки файла без создания отдельного потока
                                                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                                    StrictMode.setThreadPolicy(policy);
                                                    try {
                                                        // создаем объект DownloadManager и делаем запрос
                                                        DownloadManager downloadmanager = (DownloadManager) getSystemService(DocumentActivity.DOWNLOAD_SERVICE);
                                                        Uri uri = Uri.parse("http://192.168.1.26:3000/download/" + filename);

                                                        DownloadManager.Request request = new DownloadManager.Request(uri);
                                                        request.setTitle(filename);
                                                        request.setDescription("Downloading");
                                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                        request.setVisibleInDownloadsUi(false);
                                                        String destination = "";
                                                        System.out.println("file://" + Environment.getExternalStorageDirectory() + destination  + "/"  + filename);
                                                        request.setDestinationUri(Uri.parse("file://" + Environment.getExternalStorageDirectory() + destination  + "/"  + filename));

                                                        downloadmanager.enqueue(request);
                                                    } catch(Exception e){
                                                        System.out.println("!!!!!!!ERROR IN DOWNLOADING DOCUMENT ACTIVITY!!!!!!!!");
                                                        e.printStackTrace();
                                                    }

                                                }
                                            });

                                        }
                                    }
                                });
                            } else if (dataType == '\2') {
                                System.out.println("Иной тип данных!!!");
                            }
                        }
                    }
                }


            }
            catch(Exception e)
            {
                System.out.print("Whoops! It didn't work!:");
                System.out.print(e.getLocalizedMessage());
                System.out.print("\n");
            }
        }
    }

    // функция-обработчик выбора картинки
    public void pickPhoto(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PHOTO);
    }

    // функция-обработчик выбора файла
    public void pickFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");

        ComponentName testedActivity = intent.resolveActivity(getPackageManager());
        if (testedActivity != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Document"), FILE_RESULT_CODE);
        } else {
            Toast.makeText(this,"No file explorer available",Toast.LENGTH_LONG).show();
        }
    }

    // получение сообщений чата с сервера
    public void getMessages() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // создаем сообщение-объект для сервера
                    JSONObject data = new JSONObject();
                    data.put("type", "init"); // тип текстового сообщения, init - значит при заходе в комнату нужно получить историю чата
                    data.put("roomId", roomId); // передаем комнату
                    data.put("userId", userId); // передаем юзера

                    // преобразуем JSON в строку и в байты и записываем в поток вывода
                    out.write(data.toString().getBytes("UTF-8"));

                    // добавляем разделитель 0 - разделитель сообщений, 1 - тип сообщения - текстовое (а не файл)
                    out.write("\1\0".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start(); // запускаем поток
    }


    // функция обработчик отправки сообщений
    public void sendMessage(View view) {
        // получаем текстовое поле нового сообщения
        final EditText newMessageEditText = (EditText)findViewById(R.id.messageText);

        // получаем сам текст сообщения
        final String text = newMessageEditText.getText().toString().trim();

        // если сообщения нет, то ничего не делаем
        if (text.length() == 0) {
            return;
        }

        // запуска поток, в котором отправляем сообщение
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // случайный идентификатор, нужен, чтобы потом связать текстовую часть сообщения и файлы (отправляются по отдельности)
                String uid = UUID.randomUUID().toString();


                // если есть выбранная картинка
                if (imagePath != "") {
                    try {
                        File imageFile = new File(imagePath); // объект файла
                        int fileSize = (int)imageFile.length(); // размер файла

                        // получаем файловых входной поток
                        InputStream fileStream = new FileInputStream(imageFile);
                        BufferedInputStream buf = new BufferedInputStream(fileStream);

                        // создаем байтовый массив для файла
                        byte[] imageBytes = new byte[fileSize];

                        // считываем из файла в байтовый массив
                        buf.read(imageBytes);

                        // шифруем файл в base64 (чтобы случайно не попались служебные байты)
                        byte[] encodedImageBytes = Base64.encode(imageBytes, Base64.NO_WRAP | Base64.URL_SAFE);
                        out.write(encodedImageBytes); // записываем в поток зашифрованные байты
                        out.write("\3".getBytes()); // добавляем разделитель данных самого файла и метаинформации

                        // получаем имя файла
                        String[] arr = imagePath.split("/");
                        String filename = arr[arr.length - 1];

                        // создаем JSON объект метаинформации
                        JSONObject metaInfo = new JSONObject();
                        metaInfo.put("filename", filename);
                        metaInfo.put("type", "image");
                        metaInfo.put("uid", uid);

                        // кодируем в base64 метаинформацию
                        byte[] encodedMetaDataBytes = Base64.encode(metaInfo.toString().getBytes("UTF-8"), Base64.NO_WRAP | Base64.URL_SAFE);

                        // записывааем все в поток
                        out.write(encodedMetaDataBytes);

                        // добавляем служебные байты: 2-тип данных - файл, 0 - конец сообщения
                        out.write("\2\0".getBytes());
                        out.flush(); // сбрасываем ввс в сокет
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    imagePath = ""; // обнуляем имеющуюся картинку
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgView.setImageResource(0); // удаляем превью
                        }
                    });
                }


                // если есть прикрепленный документ, то его надо отправить
                // делаем это аналогично картинке
                if (filePath != "") {
                    try {
                        File documentFile = new File(filePath);
                        int fileSize = (int)documentFile.length();

                        System.out.println(">>>Размер файла документа = " + Integer.toString(fileSize));

                        InputStream fileStream = new FileInputStream(documentFile);
                        BufferedInputStream buf = new BufferedInputStream(fileStream);
                        byte[] fileBytes = new byte[fileSize];
                        buf.read(fileBytes);

                        byte[] encodedFileBytes = Base64.encode(fileBytes, Base64.NO_WRAP | Base64.URL_SAFE);
                        out.write(encodedFileBytes);
                        out.write("\3".getBytes());

                        String[] arr = filePath.split("/");
                        String filename = arr[arr.length - 1];


                        JSONObject metaInfo = new JSONObject();
                        metaInfo.put("filename", filename);
                        metaInfo.put("type", "file");
                        metaInfo.put("uid", uid);

                        byte[] encodedMetaDataBytes = Base64.encode(metaInfo.toString().getBytes("UTF-8"), Base64.NO_WRAP | Base64.URL_SAFE);
                        out.write(encodedMetaDataBytes);

                        out.write("\2\0".getBytes());
                        out.flush();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    filePath = "";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            filePathTextView.setText("");
                        }
                    });
                }


                // отправляем текстовое сообщение
                try {
                    // формируем JSON объект сообщения
                    JSONObject data = new JSONObject();
                    data.put("text", text);
                    data.put("roomId", roomId);
                    data.put("userId", userId);
                    data.put("type", "new");
                    data.put("uid", uid);


                    // преобразовываем JSON объект в строку и в байты
                    out.write(data.toString().getBytes("UTF-8"));

                    //дописываем служебные байты: 1 - текстовое сообщение, 0 - конец сообщения
                    out.write("\1\0".getBytes());
                    out.flush();
                    newMessageEditText.setText(""); // обнуляем текстовое поле нового сообщения
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }
}