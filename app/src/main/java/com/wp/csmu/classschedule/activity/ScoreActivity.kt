package com.wp.csmu.classschedule.activity

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
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
import com.wp.csmu.classschedule.view.bean.ScoreFilterBean
import com.wp.csmu.classschedule.view.utils.BindView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.LinkedHashSet

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
    lateinit var data: LinkedHashSet<Score>

    lateinit var filterBean: ScoreFilterBean
    private val filterTemp = Array(3) { 0 } // 存储用户的筛选选项，学期id、课程性质，显示方式

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
        adapter = ScoreRecyclerAdapter(LinkedHashSet(), this)
        recyclerView!!.adapter = adapter
        swipeRefreshLayout!!.setOnRefreshListener {
            GlobalScope.launch(Dispatchers.Main) {
                getScore1()
            }
        }
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
        val (term1, courseName, _, skillScores, performanceScore, knowledgePoints1, credits, examAttribute, subjectNature1, subjectAttribute) = data.elementAt(position)

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
                .setTitle(courseName).setNegativeButton("确定") { _, _ -> }
        dialog.show()
    }

    // [useDefault]=true表示第一次启动时
    private fun getScore() {
        GlobalScope.launch(Dispatchers.Main) {
            swipeRefreshLayout!!.isRefreshing = true
            try {
                filterBean = withContext(Dispatchers.IO) { ServiceClient.getGradeQueryFilter() }

                filterTemp[0] = filterBean.termId.findValueIndex(true)
                filterTemp[1] = filterBean.courseXZ.findValueIndex(true)
                filterTemp[2] = filterBean.displayMode.findValueIndex(true)

                getScore1()
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
            R.id.scoreMenuFilter -> {
                if (this::filterBean.isInitialized) {
                    showFilter()
                }
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFilter() {
        val filterView = layoutInflater.inflate(R.layout.score_filter_dialog_layout, null)

        filterView.findViewById<Spinner>(R.id.termFilterSpinner).apply { initSpinner(0, this, filterBean.termId) }
        filterView.findViewById<Spinner>(R.id.courseXZFilterSpinner).apply { initSpinner(1, this, filterBean.courseXZ) }
        filterView.findViewById<Spinner>(R.id.displayModeFilterSpinner).apply { initSpinner(2, this, filterBean.displayMode) }

        val dialog = AlertDialog.Builder(this).also {
            it.setView(filterView)
            it.setTitle("筛选")
            it.setPositiveButton("确定") { _, _ ->
                GlobalScope.launch(Dispatchers.Main) {
                    getScore1()
                }
            }
        }
        dialog.setOnCancelListener {
            filterTemp[0] = filterBean.termId.findValueIndex(true)
            filterTemp[1] = filterBean.courseXZ.findValueIndex(true)
            filterTemp[2] = filterBean.displayMode.findValueIndex(true)
        }
        dialog.show()
    }

    private fun initSpinner(index: Int, spinner: Spinner, item: LinkedHashMap<Pair<String, String>, Boolean>) {
        val adapter = ArrayAdapter(
                this@ScoreActivity,
                android.R.layout.simple_spinner_item,
                item.keys.map { it.second }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(filterTemp[index])
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterTemp[index] = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private suspend fun getScore1(): Boolean {
        if (!this::filterBean.isInitialized) {
            getScore()
            return true
        }
        try {
            withContext(Dispatchers.Main) { swipeRefreshLayout!!.isRefreshing = true }
            data = withContext(Dispatchers.IO) {
                Log.i("ScoreActivity", "paused")
                if (filterBean.courseXZ.keys.elementAt(filterTemp[1]).first == "") { // 如果查询全部性质成绩
                    val list = filterBean.courseXZ.keys.map { it.first }.drop(1) as ArrayList
                    ServiceClient.queryAllXZGrades(
                            filterBean.termId.keys.elementAt(filterTemp[0]).first,
                            list,
                            filterBean.displayMode.keys.elementAt(filterTemp[2]).first
                    )
                } else {
                    ServiceClient.queryGrades(
                            filterBean.termId.keys.elementAt(filterTemp[0]).first,
                            filterBean.courseXZ.keys.elementAt(filterTemp[1]).first,
                            filterBean.displayMode.keys.elementAt(filterTemp[2]).first
                    )
                }
            }
            withContext(Dispatchers.Main) { onOperationSucceed() }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onOperationFailed(e)
            }
            return false
        }
    }

    private fun onOperationSucceed() {
        swipeRefreshLayout!!.isRefreshing = false
        adapter.updateData(data)
        recyclerView!!.scrollToPosition(0)
        filterBean.termId.assignElementTrue(filterTemp[0])
        filterBean.courseXZ.assignElementTrue(filterTemp[1])
        filterBean.displayMode.assignElementTrue(filterTemp[2])
        setToolbarSubtitle()
        if (data.size == 0) {
            Snackbar.make(coordinatorLayout!!, "暂无信息", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setToolbarSubtitle() {
        val term = filterBean.termId.findKey(true)?.second
        val courseXZ = filterBean.courseXZ.findKey(true)?.second
        val displayMode = filterBean.displayMode.findKey(true)?.second
        supportActionBar!!.subtitle = "$term / $courseXZ / $displayMode"
    }

    private fun onOperationFailed(reason: Exception) {
        swipeRefreshLayout!!.isRefreshing = false
        Snackbar.make(coordinatorLayout!!, reason.toString(), Snackbar.LENGTH_SHORT).show()
    }

    // 寻找map中第一次出现value的index
    private fun <K, V> Map<K, V>.findValueIndex(value: V): Int {
        var index = 0
        this.entries.forEachIndexed { i, entry ->
            if (entry.value == value) {
                index = i
                return@forEachIndexed
            }
        }
        return index
    }

    private fun <K, V> Map<K, V>.findKey(value: V): K? {
        var key: K? = null
        this.entries.forEach { entry ->
            if (entry.value == value) {
                key = entry.key
                return@forEach
            }
        }
        return key
    }

    private fun <K> HashMap<K, Boolean>.assignElementTrue(index: Int) {
        var count = 0
        this.forEach {
            this[it.key] = count == index
            count++
        }
    }
}
