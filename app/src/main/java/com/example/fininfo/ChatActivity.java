package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {
    private Socket socket;
    private String ServerIP = "10.0.2.2";
    private static final int ServerPort = 3001;

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

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("!!!!!!!!!!!!!!!LISTENNING!!!!!");
                while(in.ready()) {
                    System.out.println("++++++++++++++++++++");
                    String line = in.readLine();
                    System.out.println("!!!!!!!!!!!!!!!MSG:" + line);
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

    public void sendMessage(View view) {
        final String text = ((EditText)findViewById(R.id.messageText)).getText().toString().trim();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("START!!!");
                try {
                    PrintWriter outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    outToServer.print(text + "\n");
                    outToServer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        Log.i("app", text);
    }
}
