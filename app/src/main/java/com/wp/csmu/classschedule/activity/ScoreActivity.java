package com.wp.csmu.classschedule.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.network.NetWorkHelper;
import com.wp.csmu.classschedule.view.adapter.ScoreRecyclerAdapter;
import com.wp.csmu.classschedule.view.bean.Score;
import com.wp.csmu.classschedule.view.utils.BindView;

import org.json.JSONArray;
import org.json.JSONObject;

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

    String[]terms;
    String[]termId;
    String currentTermName;
    String currentTermId;
    int clickedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.updateData(data);
                        recyclerView.scrollToPosition(0);
                        getSupportActionBar().setSubtitle(terms[clickedItem]);
                        if(data.size()==0) {
                            Snackbar.make(coordinatorLayout, "暂无信息", Snackbar.LENGTH_SHORT).show();
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
                getScore1();
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
                    JSONArray jsonArray=NetWorkHelper.getTerms(account);
                    terms=new String[jsonArray.length()];
                    termId=new String[jsonArray.length()];
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        if ("1".equals(jsonObject.getString("isdqxq"))){
                            clickedItem=i;
                            currentTermName=jsonObject.getString("xqmc");
                            currentTermId=jsonObject.getString("xnxq01id");
                        }
                        terms[i]=jsonObject.getString("xqmc");
                        termId[i]=jsonObject.getString("xnxq01id");
                    }
                    data = NetWorkHelper.getScore(account,currentTermId);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.score_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.scoreMenuSelectTerm:
                selectTerm();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void selectTerm(){
        final AlertDialog.Builder build=new AlertDialog.Builder(this).setTitle("选择学期").setSingleChoiceItems(terms, clickedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickedItem=which;
                getScore1();
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        build.show();
    }

    void getScore1() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        final String account = sharedPreferences.getString("account", "");
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    data = NetWorkHelper.getScore(account,termId[clickedItem]);
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
