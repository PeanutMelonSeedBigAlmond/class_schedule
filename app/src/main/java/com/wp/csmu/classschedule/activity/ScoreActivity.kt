package com.wp.csmu.classschedule.activity

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.io.IO
import com.wp.csmu.classschedule.network.Config
import com.wp.csmu.classschedule.network.DataClient
import com.wp.csmu.classschedule.network.LoginClient
import com.wp.csmu.classschedule.network.LoginClient.downloadVerifyCode
import com.wp.csmu.classschedule.network.LoginClient.login
import com.wp.csmu.classschedule.view.adapter.ScoreRecyclerAdapter
import com.wp.csmu.classschedule.view.bean.Score
import com.wp.csmu.classschedule.view.utils.BindView
import java.io.IOException
import java.util.*

class ScoreActivity : BaseActivity(), ScoreRecyclerAdapter.OnClickListener {
    @BindView(R.id.scoreRecyclerView)
    internal var recyclerView: RecyclerView? = null
    @BindView(R.id.scoreToolbar)
    internal var toolbar: Toolbar? = null
    @BindView(R.id.scoreCoordinatorLayout)
    internal var coordinatorLayout: CoordinatorLayout? = null
    @BindView(R.id.scoreSwipeRefreshLayout)
    internal var swipeRefreshLayout: SwipeRefreshLayout? = null
    private lateinit var handler: Handler
    lateinit var adapter: ScoreRecyclerAdapter
    lateinit var data: ArrayList<Score>

    internal var termName = ArrayList<String>()
    internal var termId = ArrayList<String>()

    internal var clickedItem: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        handler = Handler(Handler.Callback { msg ->
            when (msg.what) {
                0 -> {
                    swipeRefreshLayout!!.isRefreshing = false
                    adapter.updateData(data)
                    recyclerView!!.scrollToPosition(0)
                    supportActionBar!!.subtitle = termName[clickedItem]
                    if (data.size == 0) {
                        Snackbar.make(coordinatorLayout!!, "暂无信息", Snackbar.LENGTH_SHORT).show()
                    }
                }
                -1 -> {
                    swipeRefreshLayout!!.isRefreshing = false
                    val e = msg.obj as Exception
                    if (e is IOException) {
                        Snackbar.make(coordinatorLayout!!, "网络连接不可用", Snackbar.LENGTH_SHORT).show()
                    } else {
                        when (e.message) {
                            "need verify code" -> showVerifyCode()
                            else -> Snackbar.make(coordinatorLayout!!, e.message.toString(), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            true
        })

        swipeRefreshLayout!!.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark)
        //设置布局管理器
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        //设置动画
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        //初始化适配器
        adapter = ScoreRecyclerAdapter(ArrayList(), this)
        recyclerView!!.adapter = adapter
        swipeRefreshLayout!!.setOnRefreshListener { getScore1() }
        getScore()
    }

    override fun onClick(view: View, position: Int) {
        val view1 = LayoutInflater.from(this).inflate(R.layout.score_info_dialog_layout, null)
        val credit = view1.findViewById<TextView>(R.id.scoreInfoDialogCredit)
        val term = view1.findViewById<TextView>(R.id.scoreInfoDialogTerm)
        val subjectAttr = view1.findViewById<TextView>(R.id.scoreInfoDialogSubjectAttr)
        val examAttr = view1.findViewById<TextView>(R.id.scoreInfoDialogExamAttr)
        val subjectNature = view1.findViewById<TextView>(R.id.scoreInfoDialogSubjectNature)
        val skillGrade = view1.findViewById<TextView>(R.id.scoreInfoDialogSkillGrade)
        val performanceGrade = view1.findViewById<TextView>(R.id.scoreInfoDialogPerformanceGrade)
        val knowledgePoints = view1.findViewById<TextView>(R.id.scoreInfoDialogKnowledgePoints)
        val (term1, _, _, skillScores, performanceScore, knowledgePoints1, credits, examAttribute, subjectNature1, subjectAttribute) = data[position]

        credit.text = credits.toString() + "分"
        term.text = term1
        subjectAttr.text = subjectAttribute
        examAttr.text = examAttribute
        subjectNature.text = subjectNature1
        skillGrade.text = skillScores
        performanceGrade.text = performanceScore
        knowledgePoints.text = knowledgePoints1

        if (skillGrade.text.toString() != "" && skillGrade.text.toString().toDouble() < 60.0) skillGrade.setTextColor(Color.RED)
        if (performanceGrade.text.toString() != "" && performanceGrade.text.toString().toDouble() < 60.0) performanceGrade.setTextColor(Color.RED)
        if (knowledgePoints.text.toString() != "" && knowledgePoints.text.toString().toDouble() < 60.0) knowledgePoints.setTextColor(Color.RED)

        val dialog = AlertDialog.Builder(this).setView(view1)
                .setTitle(data[position].name).setNegativeButton("确定") { _, _ -> }
        dialog.show()
    }

    private fun getScore(verifyCode: String = "") {
        swipeRefreshLayout!!.isRefreshing = true
        Thread(Runnable {
            try {
                clickedItem = DataClient.queryTerms(this@ScoreActivity)
                getScore1(verifyCode)
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = Message()
                msg.what = -1
                msg.obj = e
                handler.sendMessage(msg)
            }
        }).start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.score_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.scoreMenuSelectTerm -> selectTerm()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    internal fun selectTerm() {
        val build = AlertDialog.Builder(this).setTitle("选择学期").setSingleChoiceItems(termName.toTypedArray(), clickedItem) { dialog, which ->
            clickedItem = which
            getScore1()
            dialog.dismiss()
        }.setNegativeButton("取消") { dialog, which -> }
        build.show()
    }

    internal fun getScore1(verifyCode: String = "") {
        Log.i("TAG", verifyCode)
        swipeRefreshLayout!!.isRefreshing = true
        Thread(Runnable {
            try {
                if (Config.state == LoginClient.State.SUCCESS) {
                    data = DataClient.getGrades(termId[clickedItem])
                    handler.sendEmptyMessage(0)
                } else {
                    val sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
                    val account = sharedPreferences.getString("account", "")
                    val password = sharedPreferences.getString("password", "")
                    val state = login(account!!, password!!, verifyCode)
                    when (state) {
                        LoginClient.State.SUCCESS -> {
                            clickedItem = DataClient.queryTerms(this@ScoreActivity)
                            data = DataClient.getGrades(termId[clickedItem])
                            handler.sendEmptyMessage(0)
                        }
                        LoginClient.State.NEED_VERIFY_CODE -> throw java.lang.Exception("need verify code")
                        LoginClient.State.WRONG_PASSWORD -> throw java.lang.Exception("wrong password")
                        else -> {
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = Message()
                msg.what = -1
                msg.obj = e
                handler.sendMessage(msg)
            }
        }).start()
    }

    fun showVerifyCode() {
        val view = LayoutInflater.from(this).inflate(R.layout.show_verify_code, null)
        refreshVerifyCode(view.findViewById(R.id.verifyCodeImage))
        view.findViewById<View>(R.id.refreshVerifyCode).setOnClickListener { refreshVerifyCode(view.findViewById(R.id.verifyCodeImage)) }
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this).setView(view).setPositiveButton("确定") { dialog, which -> getScore1(view.findViewById<TextInputLayout>(R.id.textInputLayout5).editText!!.text.toString().trim()) }
        dialog.show()
    }

    private fun refreshVerifyCode(imageView: ImageView) {
        Thread(Runnable {
            downloadVerifyCode()
            runOnUiThread {
                val bitmap = BitmapFactory.decodeFile(IO.verifyCodeImg)
                imageView.setImageBitmap(bitmap)
            }
        }).start()
    }
}
