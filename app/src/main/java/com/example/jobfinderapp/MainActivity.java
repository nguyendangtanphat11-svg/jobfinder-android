package com.example.jobfinderapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobfinderapp.database.DBHelper;

public class MainActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword;
    Button btnSave;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        btnSave = findViewById(R.id.btnSave);

        dbHelper = new DBHelper(this);

        btnSave.setOnClickListener(v -> {

            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            boolean result = dbHelper.insertUser(
                    name,
                    email,
                    password
            );

            if (result) {
                Toast.makeText(this,
                        "Lưu thành công",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Lưu thất bại",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}