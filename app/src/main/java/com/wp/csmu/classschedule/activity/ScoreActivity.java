package com.wp.csmu.classschedule.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.network.NetWorkHelper;
import com.wp.csmu.classschedule.view.adapter.ScoreRecyclerAdapter;
import com.wp.csmu.classschedule.view.bean.Score;
import com.wp.csmu.classschedule.view.utils.BindView;

import java.util.ArrayList;
import java.util.List;

public class ScoreActivity extends BaseActivity implements ScoreRecyclerAdapter.OnClickListener {
    @BindView(R.id.scoreRecyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.scoreToolbar)
    Toolbar toolbar;
    @BindView(R.id.scoreCoordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.scoreSwipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    Handler handler;
    ScoreRecyclerAdapter adapter;
    List<Score> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        setSupportActionBar(toolbar);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        swipeRefreshLayout.setRefreshing(false);
                        if(data.size()==0){
                            Snackbar.make(coordinatorLayout,"暂无信息",Snackbar.LENGTH_SHORT).show();
                        }else{
                            adapter.updateData(data);
                            recyclerView.scrollToPosition(0);
                        }
                        break;
                    case -1:
                        swipeRefreshLayout.setRefreshing(false);
                        Exception e = (Exception) msg.obj;
                        Snackbar.make(coordinatorLayout, e.toString(), Snackbar.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        //设置布局管理器
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //设置动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //初始化适配器
        adapter = new ScoreRecyclerAdapter(new ArrayList<Score>(), this);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getScore();
            }
        });
        getScore();
    }

    @Override
    public void onClick(View view, int position) {
        View view1 = LayoutInflater.from(this).inflate(R.layout.score_info_dialog_layout, null);
        TextView credit = view1.findViewById(R.id.scoreInfoDialogCredit);
        TextView term=view1.findViewById(R.id.scoreInfoDialogTerm);
        TextView subjectAttr=view1.findViewById(R.id.scoreInfoDialogSubjectAttr);
        TextView examAttr=view1.findViewById(R.id.scoreInfoDialogExamAttr);
        TextView subjectNature=view1.findViewById(R.id.scoreInfoDialogSubjectNature);
        TextView note=view1.findViewById(R.id.scoreInfoDialogNote);
        Score score=data.get(position);
        credit.setText(score.getCredit()+"分");
        term.setText(score.getTerm());
        subjectAttr.setText(score.getSubjectAttribute());
        examAttr.setText(score.getExamAttribute());
        subjectNature.setText(score.getSubjectNature());
        note.setText(score.getNote());
        AlertDialog.Builder dialog = new AlertDialog.Builder(this).setView(view1)
                .setTitle(data.get(position).getSubject()).setNegativeButton("确定", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        dialog.show();
    }

    void getScore() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        final String account = sharedPreferences.getString("account", "");
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    data = NetWorkHelper.getScore(account);
                    handler.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = -1;
                    msg.obj = e;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
}
