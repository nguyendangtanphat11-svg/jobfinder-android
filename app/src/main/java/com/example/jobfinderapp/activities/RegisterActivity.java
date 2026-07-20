package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private RadioGroup rgRole;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ImageView ivBack;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        dbHelper = new DBHelper(this);

        // Nút quay lại
        ivBack.setOnClickListener(v -> finish());

        // Chuyển sang màn hình đăng nhập
        tvLogin.setOnClickListener(v -> finish());

        // Xử lý đăng ký
        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void handleRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        String role = "candidate";
        int checkedId = rgRole.getCheckedRadioButtonId();
        if (checkedId == R.id.rbEmployer) {
            role = "employer";
        }

        // Validate dữ liệu
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ tên");
            return;
        }

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

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        // Kiểm tra email tồn tại
        if (dbHelper.checkEmailExists(email)) {
            Toast.makeText(this, "Email này đã được đăng ký", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu vào SQLite
        boolean isInserted = dbHelper.insertUser(fullName, email, password, role);

        if (isInserted) {
            Toast.makeText(this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
            // Chuyển sang Login
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }
}
