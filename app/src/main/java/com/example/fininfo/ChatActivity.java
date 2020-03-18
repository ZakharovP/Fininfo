package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {
    private Socket socket;
    private String ServerIP = "10.0.2.2";
    private static final int ServerPort = 3001;
    InputStream in;
    OutputStream out;
    int counter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        new Thread(new ClientThread()).start();
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try
            {
                socket = new Socket(ServerIP, ServerPort);

                in = socket.getInputStream();
                out = socket.getOutputStream();

                int count;
                byte[] buffer = new byte[8192];
                TextView resultText = findViewById(R.id.resultText);


                while ((count = in.read(buffer)) > 0) {
                    byte[] slice = Arrays.copyOfRange(buffer, 0, count);
                    String line = new String(slice, "UTF-8");
                    JSONObject data = new JSONObject(line);
                    resultText.setText(
                            data.getString("text") + "  +++  " +
                                    Integer.toString(data.getInt("other")) + " " +
                                    Integer.toString(data.getInt("counter"))
                    );
                }


            } catch(Exception e) {
                System.out.print("ERROR IN ClientThread");
                System.out.print(e.getLocalizedMessage());
                System.out.print("\n");
            }
        }
    }

    public void sendMessage(View view) {
        final String text = ((EditText)findViewById(R.id.messageText)).getText().toString().trim();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("START!!!");
                try {
                    JSONObject data = new JSONObject();
                    data.put("text", text);
                    data.put("other", 1);
                    data.put("counter", counter);
                    out.write(data.toString().getBytes("UTF-8"));
                    counter++;


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
