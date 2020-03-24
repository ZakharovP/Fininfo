package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
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
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {
    private Socket socket;
    private String ServerIP = "10.0.2.2";
    private static final int ServerPort = 3001;
    InputStream in;
    OutputStream out;
    int counter = 0;
    ImageView imgView;
    public static final int PICK_IMAGE = 1;
    public static final int GALLERY_PHOTO = 111;
    String filePath = "";


    public class RequestImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        public Bitmap doInBackground(String... params) {
            String urldisplay = params[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imgView.setImageBitmap(result);
        }
    }

    public final static class GetFilePathFromDevice {

        /**
         * Get file path from URI
         *
         * @param context context of Activity
         * @param uri     uri of file
         * @return path of given URI
         */
        public static String getPath(final Context context, final Uri uri) {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
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
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // Return the remote address
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();
                return getDataColumn(context, uri, null, null);
            }
            // File
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
        imgView = (ImageView) findViewById(R.id.imageView);
        new Thread(new ClientThread()).start();


        ChatActivity.RequestImage req = new ChatActivity.RequestImage();
        String url = "https://avatars.mds.yandex.net/get-zen_doc/1606228/pub_5d4441e1cfcc8600adf16f6a_5d4441f443863f00ad8389ce/scale_1200";
        String params = "";
        try {
            req.execute(url, params).get();
        } catch(Exception e){
            System.out.println("!!!!!!!ERROR IN CHAT ACTIVITY!!!!!!!!");
            e.printStackTrace();
        }

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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("requestCode = " + Integer.toString(requestCode));
        System.out.println("resultCode = " + Integer.toString(resultCode));
        //System.out.println("!!!data = " + data.toString());
        if (requestCode == GALLERY_PHOTO && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgView.setImageBitmap(imageBitmap);

            if (data.getData() != null) {
                filePath = GetFilePathFromDevice.getPath(ChatActivity.this, data.getData());

                System.out.println("filepath = " + filePath);
                Bitmap bm = BitmapFactory.decodeFile(filePath);
                imgView.setImageBitmap(bm);


                ImageView newImageView = new ImageView(ChatActivity.this);
                newImageView.setImageBitmap(bm);


                LinearLayout chatMessagesLayout = (LinearLayout)findViewById(R.id.chatMessagesLayout);
                chatMessagesLayout.addView(newImageView);

                TextView txt1 = new TextView(ChatActivity.this);
                txt1.setText("OK!!!!");
                txt1.setTextColor(Color.BLUE);

                chatMessagesLayout.addView(txt1);

            }
        }
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try
            {
                socket = new Socket(ServerIP, ServerPort);

                //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("!!!!!!!!!!!!!!!LISTENNING!!!!!");
                /*String line;
                while(in.ready() && (line = in.readLine()) != null) {
                    System.out.println("++++++++++++++++++++");
                    Log.i("app", "++++++++++++++++++++");
                    System.out.println("!!!!!!!!!!!!!!!MSG:" + line);
                    Log.i("app", "!!!!!!!!!!!!!!!MSG:" + line);
                }*/

                in = socket.getInputStream();
                out = socket.getOutputStream();

                int count;
                byte[] buffer = new byte[8192];
                TextView resultText = findViewById(R.id.resultText);
                //int lastNum = 0;

                //ByteArrayOutputStream streamData = new ByteArrayOutputStream();
                String body = "";

                while ((count = in.read(buffer)) > 0) {
                    byte[] slice = Arrays.copyOfRange(buffer, 0, count);
                    //streamData.write(data.toString().getBytes("UTF-8"));
                    //lastNum += count;
                    //int sepIndex = slice
                    //System.out.println(buffer);
                    String line = new String(slice, "UTF-8");
                    body += line;
                    int sepIndex = body.lastIndexOf("\0");
                    if (sepIndex > -1) {
                        String[] datas = body.substring(0, sepIndex).split("\0");
                        body = body.substring(sepIndex + 1, body.length());
                        for (int i = 0; i < datas.length; i++) {
                            String stringData = datas[i];
                            System.out.println("Получено:");
                            //System.out.println(stringData);
                            char dataType = stringData.charAt(stringData.length() - 1);
                            if (dataType == '\1') {
                                System.out.println("Обычный тип данных...");
                                JSONObject data = new JSONObject(line);

                                final String messageText = data.getString("text");
                                final String messageUser = data.getString("user");
                                //System.out.println("На вывод = " + data.getString("text"));

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LinearLayout chatMessagesLayout = (LinearLayout)findViewById(R.id.chatMessagesLayout);
                                        //chatMessagesLayout.addView(newImageView);


                                        TextView userTextView = new TextView(ChatActivity.this);
                                        userTextView.setText(messageUser);
                                        userTextView.setTextColor(Color.BLUE);
                                        chatMessagesLayout.addView(userTextView);

                                        TextView messageTextView = new TextView(ChatActivity.this);
                                        messageTextView.setText(messageText);
                                        chatMessagesLayout.addView(messageTextView);
                                    }
                                });
                            } else if (dataType == '\2') {
                                System.out.println("Иной тип данных!!!");
                                URL newurl = new URL("https://avatars.mds.yandex.net/get-zen_doc/1606228/pub_5d4441e1cfcc8600adf16f6a_5d4441f443863f00ad8389ce/scale_1200");
                                Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());

                                imgView.setImageBitmap(mIcon_val);
                            }
                            //System.out.println("out = " + data.getString("text"));
                        }
                    }
                }
                System.out.println("OKKKKKKKK!!!!!!");


            }
            catch(Exception e)
            {
                System.out.print("Whoops! It didn't work!:");
                System.out.print(e.getLocalizedMessage());
                System.out.print("\n");
            }
        }
    }

    public void pickPhoto(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PHOTO);
    }

    public void sendMessage(View view) {
        final String text = ((EditText)findViewById(R.id.messageText)).getText().toString().trim();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("START!!!");
                try {
                    if (filePath != "") {
                        try {

                            JSONObject data = new JSONObject();
                            data.put("text", text);
                            out.write(data.toString().getBytes("UTF-8"));

                            /*File imageFile = new File(filePath);
                            int fileSize = (int)imageFile.length();

                            System.out.println(">>>Размер файла = " + Integer.toString(fileSize));

                            InputStream fileStream = new FileInputStream(imageFile);
                            BufferedInputStream buf = new BufferedInputStream(fileStream);
                            byte[] imageBytes = new byte[fileSize];
                            buf.read(imageBytes);

                            byte[] encodedImageBytes = Base64.encode(imageBytes, Base64.NO_WRAP | Base64.URL_SAFE);

                            out.write(encodedImageBytes);
                            out.write("\2\0".getBytes());

                            out.flush();*/
                        //} catch (FileNotFoundException e) {
                        //    e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        JSONObject data = new JSONObject();
                        data.put("text", text);
                        data.put("other", 1);
                        data.put("counter", counter);
                        ByteArrayOutputStream streamData = new ByteArrayOutputStream();
                        streamData.write(data.toString().getBytes("UTF-8"));
                        streamData.write((byte)'\1');
                        streamData.write((byte)'\0');
                        out.write(streamData.toByteArray());
                        counter++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        Log.i("app", text);
    }
}
