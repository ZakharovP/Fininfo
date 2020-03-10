package com.example.fininfo;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.ViewModelStoreOwner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
    }

    public void logIn(View view){
        String url = "";
        String login = ((EditText)findViewById(R.id.loginText)).getText().toString().trim();
        String password = ((EditText)findViewById(R.id.passwordText)).getText().toString().trim();




        Log.i("app", "login = " + login);
        Log.i("app", "pass = " + password);

        if (login.equals("admin") && password.equals("secret")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("userId", 1);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(AuthActivity.this)
                    .setTitle("Ошибка авторизации")
                    .setMessage("Имя или пароль не правильны!")
                    .setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();
        }

        /*try {
            Connection connection = DriverManager.getConnection(url, login, password);
            Statement statement = connection.createStatement();
            String sql = "select * from Project.Users";


        } catch (SQLException e){
            e.printStackTrace();
        }*/
    }
}