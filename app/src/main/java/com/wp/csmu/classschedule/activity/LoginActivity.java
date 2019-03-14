package com.wp.csmu.classschedule.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.view.utils.BindView;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.loginTextInputLayout1)
    TextInputLayout textInputLayout1;
    @BindView(R.id.loginTextInputLayout2)
    TextInputLayout textInputLayout2;
    @BindView(R.id.loginButton)
    Button button;
    @BindView(R.id.loginToolbar)
    Toolbar toolbar;

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            return true;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setSupportActionBar(toolbar);

    }

    private void login(){
        String account=textInputLayout1.getEditText().getText().toString().trim();
        String password=textInputLayout2.getEditText().getText().toString().trim();
        if (!(account.equals("")||password.equals(""))){
            new Thread(new Runnable() {
                @Override
                public void run() {

                }
            }).start();
        }
    }
}
