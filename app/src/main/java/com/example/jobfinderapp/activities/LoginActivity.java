package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.utils.UserSession;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnLogin, btnGoogleSignIn;
    private ProgressBar progressBar;
    private DBHelper dbHelper;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getInstance(this);
        UserSession session = new UserSession(this);
        if (session.isLoggedIn()) {
            User user = dbHelper.getUserById(session.getUserId());
            if (isActiveAllowedUser(user)) {
                session.save(user);
                navigateToHome(user);
                return;
            }
            session.clear();
        }

        setContentView(R.layout.activity_login);
        initViews();
        mAuth = FirebaseAuth.getInstance();
        setupGoogleSignIn();
        findViewById(R.id.tvRegister).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnLogin.setOnClickListener(v -> handleLogin());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, options);
        googleSignInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                setLoading(false);
                return;
            }
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException exception) {
                Log.w("GOOGLE_AUTH", "Google sign in failed", exception);
                setLoading(false);
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithGoogle() {
        setLoading(true);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent()));
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful() || mAuth.getCurrentUser() == null) {
                setLoading(false);
                Toast.makeText(this, "Firebase authentication failed", Toast.LENGTH_SHORT).show();
                return;
            }
            handleGoogleUserSession(mAuth.getCurrentUser());
        });
    }

    private void handleGoogleUserSession(FirebaseUser firebaseUser) {
        String email = DBHelper.normalizeEmail(firebaseUser.getEmail());
        if (TextUtils.isEmpty(email)) {
            setLoading(false);
            Toast.makeText(this, "Google account does not provide an email", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.getUserByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullname(firebaseUser.getDisplayName() == null ? "Google User" : firebaseUser.getDisplayName());
            user.setRole(DBHelper.ROLE_CANDIDATE);
            user.setStatus(DBHelper.STATUS_ACTIVE);
            user.setPassword(null);
            int userId = (int) dbHelper.insertUser(user);
            if (userId == -1) {
                setLoading(false);
                Toast.makeText(this, "Unable to create Google account", Toast.LENGTH_SHORT).show();
                return;
            }
            user.setId(userId);
        }
        setLoading(false);
        completeLogin(user, true);
    }

    private void handleLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        String email = DBHelper.normalizeEmail(etEmail.getText().toString());
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        setLoading(true);
        new Handler().postDelayed(() -> {
            try {
                User user = dbHelper.login(email, password);
                if (user == null) {
                    setLoading(false);
                    tilPassword.setError("Email hoặc mật khẩu không chính xác");
                    return;
                }
                completeLogin(user, false);
            } catch (Exception exception) {
                setLoading(false);
                Log.e("LOGIN_ERROR", "Database login failed", exception);
                Toast.makeText(this, "Lỗi kết nối cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            }
        }, 1000);
    }

    private void completeLogin(User user, boolean googleLogin) {
        if (!isActiveAllowedUser(user)) {
            setLoading(false);
            if (user != null && DBHelper.STATUS_PENDING.equals(user.getStatus())) {
                Toast.makeText(this, "Tài khoản nhà tuyển dụng đang chờ duyệt", Toast.LENGTH_LONG).show();
            } else if (user != null && DBHelper.STATUS_REJECTED.equals(user.getStatus())) {
                Toast.makeText(this, "Yêu cầu nhà tuyển dụng đã bị từ chối", Toast.LENGTH_LONG).show();
            } else if (user != null && DBHelper.STATUS_BLOCKED.equals(user.getStatus())) {
                Toast.makeText(this, "Tài khoản đã bị khóa", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Tài khoản không được phép đăng nhập", Toast.LENGTH_SHORT).show();
            }
            new UserSession(this).clear();
            return;
        }
        new UserSession(this).save(user);
        Toast.makeText(this, googleLogin ? "Đăng nhập Google thành công" : "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
        navigateToHome(user);
    }

    private boolean isActiveAllowedUser(User user) {
        return user != null
                && DBHelper.STATUS_ACTIVE.equals(user.getStatus())
                && (DBHelper.ROLE_CANDIDATE.equals(user.getRole()) || DBHelper.ROLE_EMPLOYER.equals(user.getRole()) || DBHelper.ROLE_ADMIN.equals(user.getRole()));
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnGoogleSignIn.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }

    private void navigateToHome(User user) {
        Class<?> target = DBHelper.ROLE_ADMIN.equals(user.getRole()) ? AdminDashboardActivity.class
                : (DBHelper.ROLE_EMPLOYER.equals(user.getRole()) ? EmployerDashboardActivity.class : HomeActivity.class);
        Intent intent = new Intent(this, target);
        startActivity(intent);
        finish();
    }
}
