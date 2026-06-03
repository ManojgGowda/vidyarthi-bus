package com.vidyarthi.bus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.vidyarthi.bus.R;
import com.vidyarthi.bus.utils.FirebaseHelper;

public class SplashActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseHelper = new FirebaseHelper();

        // Attempt sign in in background, but don't let it block the app navigation
        try {
            firebaseHelper.signInAnonymously(() -> {
                // Background task completed
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Always transition to MainActivity after a short delay for branding
        new Handler(Looper.getMainLooper()).postDelayed(this::goToMain, 2000);
    }

    private void goToMain() {
        if (!isFinishing()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}
