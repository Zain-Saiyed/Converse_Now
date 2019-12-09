package com.zain.deeplearning.converse_now;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setLogo(R.drawable.conv_now_192); // change with your icon name
//        getSupportActionBar().setDisplayUseLogoEnabled(true);

        Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                startActivity(new Intent(SplashScreen.this,login.class));
                startActivity(new Intent(SplashScreen.this,Login.class));
                finish();
            }
        },3000);
    }
}
