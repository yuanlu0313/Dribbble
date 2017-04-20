package com.yl.yuanlu.dribbbo.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yl.yuanlu.dribbbo.auth.Auth;
import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.auth.AuthActivity;
import com.yl.yuanlu.dribbbo.dribbble.Dribbble;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 4/12/2017.
 */

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.activity_login_btn) TextView loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //Try to load AccessToken and User info from SP, if present
        Dribbble.init(this);

        //check whether AccessToken is present
        //if not, go to AuthActivity
        //if yes, jump to MainActivity
        if(!Dribbble.isLoggedIn()) {
            //Start the AuthActivity when button clicked, startActivityForResult
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Auth.openAuthActivity(LoginActivity.this);
                }
            });
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Auth.REQ_CODE && resultCode == RESULT_OK) {
            final String tmp_token = data.getStringExtra(AuthActivity.KEY_CODE);
            Log.i("YUAN_DBG", tmp_token);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String accessToken = Auth.fetchAccessToken(tmp_token);


                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
