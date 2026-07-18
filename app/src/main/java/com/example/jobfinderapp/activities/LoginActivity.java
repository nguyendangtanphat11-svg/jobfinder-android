package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra nếu đã login thì vào đúng màn hình theo role
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        if (pref.getBoolean("LOGIN_STATUS", false)) {
            String role = pref.getString("USER_ROLE", "candidate");
            if ("employer".equals(role)) {
                startActivity(new Intent(LoginActivity.this, EmployerDashboardActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            }
            finish();
            return;
        }
        
        setContentView(R.layout.activity_login);

        initViews();
        dbHelper = new DBHelper(this);

        // Chuyển sang màn hình đăng ký
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate dữ liệu đầu vào
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        // Kiểm tra trong SQLite
        User user = dbHelper.login(email, password);
        if (user != null) {
            // Lưu vào SharedPreferences
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("USER_ID", user.getId());
            editor.putString("USER_EMAIL", user.getEmail());
            editor.putString("USER_NAME", user.getFullname());
            editor.putString("USER_ROLE", user.getRole());
            editor.putBoolean("LOGIN_STATUS", true);
            editor.apply();

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            
            // Chuyển sang màn hình tương ứng với role
            if ("employer".equals(user.getRole())) {
                startActivity(new Intent(LoginActivity.this, EmployerDashboardActivity.class));
            } else {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            }
            finish();
        } else {
            Toast.makeText(this, "Email hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
        }
    }
}
