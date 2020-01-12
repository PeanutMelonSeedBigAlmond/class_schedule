package com.wp.csmu.classschedule.activity.loginactivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.activity.BaseActivity;
import com.wp.csmu.classschedule.activity.mainactivity.MainActivity;
import com.wp.csmu.classschedule.io.IO;
import com.wp.csmu.classschedule.network.Config;
import com.wp.csmu.classschedule.network.DataClient;
import com.wp.csmu.classschedule.network.LoginClient;
import com.wp.csmu.classschedule.view.utils.BindView;

import java.io.IOException;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.loginTextInputLayout1)
    TextInputLayout textInputLayout1;
    @BindView(R.id.loginTextInputLayout2)
    TextInputLayout textInputLayout2;
    @BindView(R.id.loginButton)
    Button button;
    @BindView(R.id.loginToolbar)
    Toolbar toolbar;
    @BindView(R.id.loginCoordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.loginProgressBar)
    ProgressBar progressBar;
    @BindView(R.id.textInputLayout3)
    TextInputLayout textInputLayout3;
    @BindView(R.id.imageView4)
    ImageView imageView;
    @BindView(R.id.button)
    Button refreshButton;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    button.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    if ((msg.obj instanceof IOException)) {
                        Snackbar.make(coordinatorLayout, "网络连接不可用", Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (((Exception) msg.obj).getMessage().equals("账号或密码错误")) {
                            textInputLayout2.setError("账号或密码错误");
                            textInputLayout2.setErrorEnabled(true);
                        } else if (((Exception) msg.obj).getMessage().equals("需要验证码")) {
                            textInputLayout3.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.VISIBLE);
                            refreshButton.setVisibility(View.VISIBLE);
                            textInputLayout3.setError("请输入验证码");
                            textInputLayout3.setErrorEnabled(true);
                        } else {
                            Snackbar.make(coordinatorLayout, msg.obj.toString(), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case 0:
                    SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("account", textInputLayout1.getEditText().getText().toString().trim());
                    editor.putString("password", textInputLayout2.getEditText().getText().toString().trim());
                    editor.commit();
                    try {
                        //写入课程
                        IO.writeSchedule(Config.getSchedules());
                        //写入开学时间
                        SharedPreferences sharedPreferences1 = getSharedPreferences("com.wp.csmu.classschedule_preferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                        editor1.putString("term_begins_time", Config.getTermBeginsTime());
                        editor1.commit();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Snackbar.make(coordinatorLayout, "导入失败\n" + e.toString(), Snackbar.LENGTH_SHORT).show();
                    }
                    finish();
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setSupportActionBar(toolbar);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadVerifyCode();
            }
        });

    }

    private void showVerifyCode() {
        Bitmap bitmap = BitmapFactory.decodeFile(IO.verifyCodeImg);
        imageView.setImageBitmap(bitmap);
    }

    private void downloadVerifyCode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LoginClient.INSTANCE.downloadVerifyCode();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showVerifyCode();
                    }
                });
            }
        }).start();
    }

    private void login() {
        textInputLayout2.setErrorEnabled(false);
        textInputLayout3.setErrorEnabled(false);
        final String account = textInputLayout1.getEditText().getText().toString().trim();
        final String password = textInputLayout2.getEditText().getText().toString().trim();
        button.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (!(account.equals("") || password.equals(""))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        LoginClient.State state = LoginClient.INSTANCE.login(account, password, textInputLayout3.getEditText().getText().toString().trim());
                        switch (state) {
                            case SUCCESS:
                                IO.writeSchedule(DataClient.INSTANCE.getSchedule());
                                break;
                            case WRONG_PASSWORD:
                                throw new Exception("账号或密码错误");
                            case NEED_VERIFY_CODE:
                                downloadVerifyCode();
                                throw new Exception("需要验证码");
                        }
                        handler.sendEmptyMessage(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message message = new Message();
                        message.obj = e;
                        message.what = -1;
                        handler.sendMessage(message);
                    }
                }
            }).start();
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(textInputLayout1.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
