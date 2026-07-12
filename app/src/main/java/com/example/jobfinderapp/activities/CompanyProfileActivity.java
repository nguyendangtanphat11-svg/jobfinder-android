package com.example.jobfinderapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Company;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompanyProfileActivity extends AppCompatActivity {

    private CircleImageView imgLogo;
    private TextInputEditText etName, etWebsite, etAddress, etDesc;
    private MaterialButton btnUpdate, btnChangeLogo;
    private DBHelper dbHelper;
    private int userId;
    private Company currentCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_profile);

        dbHelper = new DBHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = pref.getInt("USER_ID", -1);

        initViews();
        setupToolbar();
        loadCompanyData();

        btnUpdate.setOnClickListener(v -> updateProfile());
        btnChangeLogo.setOnClickListener(v -> Toast.makeText(this, "Tính năng đổi logo đang phát triển", Toast.LENGTH_SHORT).show());
    }

    private void initViews() {
        imgLogo = findViewById(R.id.imgCompanyLogo);
        etName = findViewById(R.id.etCompanyName);
        etWebsite = findViewById(R.id.etCompanyWebsite);
        etAddress = findViewById(R.id.etCompanyAddress);
        etDesc = findViewById(R.id.etCompanyDesc);
        btnUpdate = findViewById(R.id.btnUpdateCompany);
        btnChangeLogo = findViewById(R.id.btnChangeLogo);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCompanyData() {
        currentCompany = dbHelper.getCompanyByUserId(userId);
        if (currentCompany != null) {
            etName.setText(currentCompany.getName());
            etWebsite.setText(currentCompany.getWebsite());
            etAddress.setText(currentCompany.getAddress());
            etDesc.setText(currentCompany.getDescription());

            Glide.with(this)
                    .load(currentCompany.getLogo())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(imgLogo);
        }
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String website = etWebsite.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Tên công ty không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        currentCompany.setName(name);
        currentCompany.setWebsite(website);
        currentCompany.setAddress(address);
        currentCompany.setDescription(desc);

        if (dbHelper.updateCompany(currentCompany)) {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}
