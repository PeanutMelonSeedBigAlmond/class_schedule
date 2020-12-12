package com.wp.csmu.classschedule.activity.loginactivity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.activity.BaseActivity
import com.wp.csmu.classschedule.activity.mainactivity.MainActivity
import com.wp.csmu.classschedule.data.sharedpreferences.TimetableViewConfigData
import com.wp.csmu.classschedule.data.sharedpreferences.User
import com.wp.csmu.classschedule.exception.InvalidPasswordException
import com.wp.csmu.classschedule.io.IO
import com.wp.csmu.classschedule.network.LoginState
import com.wp.csmu.classschedule.network.login.LoginClient
import com.wp.csmu.classschedule.network.service.ServiceClient
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import net.nashlegend.anypref.AnyPref

class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(loginToolbar)
        loginButton!!.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) { login() }
        }
    }

    private suspend fun login() {
        loginTextInputLayout2!!.isErrorEnabled = false
        textInputLayout3!!.isErrorEnabled = false
        val account = loginTextInputLayout1!!.editText!!.text.toString().trim()
        val password = loginTextInputLayout2!!.editText!!.text.toString().trim()
        button!!.visibility = View.GONE
        loginProgressBar!!.visibility = View.VISIBLE
        try {
            if (account != "" && password != "") {
                val state = withContext(Dispatchers.IO) { LoginClient.login(account, password) }
                if (state == LoginState.WRONG_PASSWORD) {
                    throw InvalidPasswordException()
                }
                val terms= withContext(Dispatchers.IO){ ServiceClient.getTermId() }
                showTermSelection(terms)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onOperationFailed(e)
        }
        val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(loginTextInputLayout1!!.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun showTermSelection(terms:HashMap<Pair<String,String>,Boolean>){
        var selectedTermIndex=terms.findValue(true)
        val termId=terms.keys.map { it.first }
        val termName=terms.keys.map { it.second }
        val dialog=AlertDialog.Builder(this)
                .setTitle("选择学期")
                .setSingleChoiceItems(
                        termName.toTypedArray(),
                        selectedTermIndex
                ){ _, i -> selectedTermIndex=i }
                .setPositiveButton(
                        "确定"
                ){_,_-> onOperationSucceed(termId[selectedTermIndex]) }
                .setCancelable(false)
        dialog.create().show()
    }

    private fun onOperationSucceed(selectedTermId:String) {
        // 保存用户名密码
        val user = User(
                loginTextInputLayout1.editText!!.text.toString().trim(),
                loginTextInputLayout2.editText!!.text.toString().trim()
        )
        AnyPref.put(user)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val termBeginsTime= withContext(Dispatchers.IO){ ServiceClient.getTermBeginsTime(selectedTermId) }
                //写入开学时间
                val timetableViewConfigData = AnyPref.get(TimetableViewConfigData::class.java)
                timetableViewConfigData.termBeginsTime = termBeginsTime
                val schedule= withContext(Dispatchers.IO){ ServiceClient.getScheduleList(selectedTermId) }
                //写入课程
                IO.writeSchedule(schedule)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }catch (e:Exception){
                e.printStackTrace()
                onOperationFailed(e)
            }
        }
    }

    private fun onOperationFailed(reason: Exception) {
        loginButton!!.visibility = View.VISIBLE
        loginProgressBar!!.visibility = View.GONE
        Snackbar.make(loginCoordinatorLayout!!, reason.toString(), Snackbar.LENGTH_SHORT).show()
    }

    // 寻找map中第一次出现value的index
    private fun <K,V> Map<K,V>.findValue(value:V):Int{
        var index=0
        this.entries.forEachIndexed{ i, entry ->
            if (entry.value==value){
                index=i
                return@forEachIndexed
            }
        }
        return index
    }
}