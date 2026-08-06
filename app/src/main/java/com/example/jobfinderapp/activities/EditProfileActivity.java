package com.example.jobfinderapp.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.CV;
import com.example.jobfinderapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private CircleImageView ivEditAvatar;
    private TextInputEditText etAvatarUrl, etFullname, etPhone, etObjective, etSkills, etEducation, etExperience;
    private MaterialButton btnSaveProfile;
    private DBHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper = DBHelper.getInstance(this);
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadData();
        setupEvents();
    }

    private void initViews() {
        ivEditAvatar = findViewById(R.id.ivEditAvatar);
        etAvatarUrl = findViewById(R.id.etAvatarUrl);
        etFullname = findViewById(R.id.etFullname);
        etPhone = findViewById(R.id.etPhone);
        etObjective = findViewById(R.id.etObjective);
        etSkills = findViewById(R.id.etSkills);
        etEducation = findViewById(R.id.etEducation);
        etExperience = findViewById(R.id.etExperience);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void loadData() {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            etFullname.setText(user.getFullname());
            etPhone.setText(user.getPhone());
            etAvatarUrl.setText(user.getAvatar());
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(this)
                        .load(user.getAvatar())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivEditAvatar);
            }
        }

        CV cv = dbHelper.getCVByUserId(userId);
        if (cv != null) {
            etObjective.setText(cv.getObjective());
            etSkills.setText(cv.getSkills());
            etEducation.setText(cv.getEducation());
            etExperience.setText(cv.getExperience());
        }
    }

    private void setupEvents() {
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        
        // Toolbar navigation icon consumes its own touch event, so use the dedicated listener.
        ((com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.toolbar))
                .setNavigationOnClickListener(v -> finish());
        
        // Cập nhật ảnh preview khi URL thay đổi
        etAvatarUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = etAvatarUrl.getText().toString().trim();
                if (!url.isEmpty()) {
                    Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(ivEditAvatar);
                }
            }
        });
    }

    private void saveProfile() {
        String fullName = etFullname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String avatar = etAvatarUrl.getText().toString().trim();
        String objective = etObjective.getText().toString().trim();
        String skills = etSkills.getText().toString().trim();
        String education = etEducation.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullname.setError("Tên không được để trống");
            return;
        }

        // Cập nhật User
        User user = dbHelper.getUserById(userId);
        user.setFullname(fullName);
        user.setPhone(phone);
        user.setAvatar(avatar);
        boolean userUpdated = dbHelper.updateUser(user);

        // Cập nhật CV
        CV cv = dbHelper.getCVByUserId(userId);
        if (cv == null) {
            cv = new CV();
            cv.setUserId(userId);
        }
        cv.setObjective(objective);
        cv.setSkills(skills);
        cv.setEducation(education);
        cv.setExperience(experience);
        boolean cvUpdated = dbHelper.updateCV(cv);

        if (userUpdated && cvUpdated) {
            Toast.makeText(this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi lưu", Toast.LENGTH_SHORT).show();
        }
    }
}
