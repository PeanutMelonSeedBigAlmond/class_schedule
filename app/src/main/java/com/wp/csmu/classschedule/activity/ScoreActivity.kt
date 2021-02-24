package com.wp.csmu.classschedule.activity

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.network.service.ServiceClient
import com.wp.csmu.classschedule.view.adapter.ScoreRecyclerAdapter
import com.wp.csmu.classschedule.view.bean.Score
import com.wp.csmu.classschedule.view.utils.BindView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    lateinit var adapter: ScoreRecyclerAdapter
    lateinit var data: ArrayList<Score>
    private var termName = ArrayList<String>()
    private var termId = ArrayList<String>()

    private var clickedItem: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        swipeRefreshLayout!!.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark)
        //设置布局管理器
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        //设置动画
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        //初始化适配器
        adapter = ScoreRecyclerAdapter(ArrayList(), this)
        recyclerView!!.adapter = adapter
        swipeRefreshLayout!!.setOnRefreshListener { getScore1() }
        getScore(true)
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

    // [useDefault]=true表示第一次启动时
    private fun getScore(useDefault:Boolean=false) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                swipeRefreshLayout!!.isRefreshing = true
                val terms = withContext(Dispatchers.IO) { ServiceClient.getTermId() }
                termId = terms.keys.map { it.first }.toMutableList().also { it.add(0, "") } as ArrayList<String>
                termName = terms.keys.map { it.second }.toMutableList().also { it.add(0, "全部学期") } as ArrayList<String>
                if(useDefault){
                    clickedItem = terms.findValue(true) + 1
                }
                withContext(Dispatchers.IO) { getScore1() }
            } catch (e: Exception) {
                e.printStackTrace()
                onOperationFailed(e)
            }
        }
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
            getScore()
            dialog.dismiss()
        }.setNegativeButton("取消") { dialog, which -> }
        build.show()
    }

    private fun getScore1() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                swipeRefreshLayout!!.isRefreshing = true
                data = withContext(Dispatchers.IO) { ServiceClient.queryGrades(termId[clickedItem]) }
                onOperationSucceed()
            } catch (e: Exception) {
                e.printStackTrace()
                onOperationFailed(e)
            }
        }
    }

    private fun onOperationSucceed() {
        swipeRefreshLayout!!.isRefreshing = false
        adapter.updateData(data)
        recyclerView!!.scrollToPosition(0)
        supportActionBar!!.subtitle = termName[clickedItem]
        if (data.size == 0) {
            Snackbar.make(coordinatorLayout!!, "暂无信息", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun onOperationFailed(reason: Exception) {
        swipeRefreshLayout!!.isRefreshing = false
        Snackbar.make(coordinatorLayout!!, reason.toString(), Snackbar.LENGTH_SHORT).show()
    }

    // 寻找map中第一次出现value的index
    private fun <K, V> Map<K, V>.findValue(value: V): Int {
        var index = 0
        this.entries.forEachIndexed { i, entry ->
            if (entry.value == value) {
                index = i
                return@forEachIndexed
            }
        }
        return index
    }
}
