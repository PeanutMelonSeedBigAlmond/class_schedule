package com.wp.csmu.classschedule.network

import android.content.Context
import android.util.Base64
import android.util.Log
import com.wp.csmu.classschedule.activity.ScoreActivity
import com.wp.csmu.classschedule.application.MyApplication
import com.wp.csmu.classschedule.view.bean.Score
import com.wp.csmu.classschedule.view.scheduletable.Subjects
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.regex.Pattern

object NetworkHelper {
    private val client = OkHttpClient()
    var cookie = ""
    var termSelection = HashMap<String, String>()
    @JvmStatic
    var schedules = HashSet<Subjects>()

    @JvmStatic
    var termBeginsTime = ""

    object Login {
        @Throws(Exception::class)
        internal fun login(username: String, password: String) {
            if (cookie == "") {
                getCookie()
            }
            val url = "http://jiaowu.csmu.edu.cn:8099/jsxsd/xk/LoginToXk"
            val formBody = FormBody.Builder().add("encoded", encryptPassword(username, password)).build()
            val request = Request.Builder().url(url).addHeader("Cookie", cookie).post(formBody).build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body()!!.string())
            if (document.title() == "学生个人中心") {
                println("登录成功")
            } else {
                Log.i("NetworkHelper", document.title())
                throw Exception("登录失败")
            }
        }

        private fun encryptPassword(username: String, password: String): String {
            return String(Base64.encode(username.toByteArray(), Base64.DEFAULT)).plus("%%%")
                    .plus(String(Base64.encode(password.toByteArray(), Base64.DEFAULT)))
        }

        internal fun getCookie() {
            val url = "http://jiaowu.csmu.edu.cn:8099/jsxsd/"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            cookie = response.header("Set-Cookie")!!
        }
    }

    object GetSchedule {
        @Throws(Exception::class)
        @JvmStatic
        fun getSchedule(account: String, password: String) {
            NetworkHelper.Login.login(account, password)
            getSchedulePage()
        }

        private fun getSchedulePage() {
            val url = "http://jiaowu.csmu.edu.cn:8099/jsxsd/xskb/xskb_list.do"
            //val formBody = FormBody.Builder().add("xnxq01id", "2018-2019-1").build()
            val request = Request.Builder().url(url).header("Cookie", cookie).get().build()
            val response = client.newCall(request).execute()
            val document = Jsoup.parse(response.body()!!.string())
            /*********获取学期选择************/
            val termDocument = document.select("select[name=xnxq01id]").first().select("option")
            for (element in termDocument) {
                val key = element.attr("value")
                val value = element.text()
                termSelection[key] = value
                //println("key=$key map=$value")
            }
            /********获取课程表***************/
            val scheduleDocument = document.select("table[id=kbtable]").first()
            getSchedule(scheduleDocument)
            /********获取开学时间***************/
            termBeginsTime = NetworkHelper.GetTermBeginsTime.mGetTermBeginsTime()
        }

        private fun getSchedule(htmlTable: Element) {
            val elements = htmlTable.selectFirst("tbody").select("tr")
            for (i in 1 until elements.size) {
                val classElements = elements[i].select("td").select("div[class=kbcontent]")
                for (j in 0 until classElements.size) {
                    parseSchedule(classElements[j], j + 1)
                }
            }
        }

        private fun parseSchedule(element: Element, currentDay: Int) {
            val string = element.toString().replace(Regex("-{3,}"), "</div><div>")
            val document = Jsoup.parse(string)
            val elements = document.select("div")
            for (element1 in elements) {
                val className = element1.ownText()
                //无教师时
                var teacherName = ""
                try {
                    teacherName = element1.selectFirst("font[title=老师]").text()
                } catch (e: java.lang.NullPointerException) {
                }
                //无时间时
                var weeks: Array<Int>? = null
                var startTime = 0
                var endTime = 0
                try {
                    val string2 = element1.selectFirst("font[title=周次(节次)]").text()
                    weeks = parseWeeks(string2)!!
                    val times = parseTimes(string2)!!
                    startTime = times[0]
                    endTime = times[1]
                } catch (e: java.lang.NullPointerException) {
                }
                //无教室时
                var classRoom = ""
                try {
                    classRoom = element1.selectFirst("font[title=教室]").text()
                } catch (e: java.lang.NullPointerException) {
                }
                //无课程时
                try {
                    val subjects = Subjects()
                    subjects.day = currentDay
                    subjects.name = className
                    subjects.room = classRoom
                    subjects.start = startTime
                    subjects.step = endTime - startTime + 1
                    subjects.teacher = teacherName
                    subjects.weeks = weeks!!.toMutableList()
                    subjects.end = endTime
                    schedules.add(subjects)
                } catch (e: Exception) {
                }
            }
        }

        private fun parseWeeks(text: String): Array<Int>? {
            val pattern = Pattern.compile("(.*)\\(周\\).*")
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val array = matcher.group(1).split(",")
                val list = ArrayList<Int>()
                for (string in array) {
                    if (string.contains("-")) {
                        val array2 = string.split("-")
                        for (i in array2[0].toInt()..array2.last().toInt()) {
                            list.add(i)
                        }
                    } else {
                        list.add(string.toInt())
                    }
                }
                return list.toArray(Array(list.size) { 0 })
            }
            return null
        }

        private fun parseTimes(text: String): Array<Int>? {
            val pattern = Pattern.compile(".*\\[(.*)节]")
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val string = matcher.group(1)
                return if (string.length == 2) {
                    arrayOf(string.toInt(), string.toInt())
                } else {
                    val array = string.split("-")
                    arrayOf(array[0].toInt(), array.last().toInt())
                }
            }
            return null
        }
    }

    object GetTermBeginsTime {
        private fun getTermBeginsPage(): String {
            val document = Jsoup.connect("http://jiaowu.csmu.edu.cn:8099/jsxsd/jxzl/jxzl_query").header("Cookie", cookie).get()
            val table = document.select("table[id=kbtable] > tbody")[0]
            return parseTermBeginsTime(table)
        }

        internal fun mGetTermBeginsTime(): String = getTermBeginsPage()

        private fun parseTermBeginsTime(element: Element): String {
            val trs = element.select("tr")
            for (i in 1 until trs.size) {
                val tds = trs[i].select("td")
                for (j in 0 until tds.size) {
                    if (tds[j].attr("title") === null || tds[j].attr("title").trim() == "") {

                    } else {
                        return tds[j].attr("title").replace("年", "-").replace("月", "-")
                    }
                }
            }
            return ""
        }
    }

    object GetGrades {
        @Throws(Exception::class)
        @JvmStatic
        public fun getGrades(term: String): ArrayList<Score> {
            val url = "http://jiaowu.csmu.edu.cn:8099/jsxsd/kscj/cjcx_list"
            val formBody = FormBody.Builder().add("kksj", term).add("kcxz", "").add("kcmc", "").add("xsfs", "all").build()
            val request = Request.Builder().url(url).addHeader("Cookie", cookie).post(formBody).build()
            val document = client.newCall(request).execute().body()!!.string()
            return parseGrades(document)
        }

        //传入学期名和ID，会修改内容
        //返回目前默认选择的学期编号
        @Throws(Exception::class)
        @JvmStatic
        public fun queryTerms(scoreActivity: ScoreActivity): Int {
            val sharedPreferences = MyApplication.getContext().getSharedPreferences("user", Context.MODE_PRIVATE)
            val username = sharedPreferences.getString("account", "")
            val password = sharedPreferences.getString("password", "")
            Login.login(username, password)
            val url = "http://jiaowu.csmu.edu.cn:8099/jsxsd/kscj/cjcx_query"
            val request = Request.Builder().url(url).addHeader("Cookie", cookie).get().build()
            val document = client.newCall(request).execute().body()!!.string()
            val termDocument = Jsoup.parse(document).select("select[id=kksj]").select("option")
            var selected = 0
            for ((i, element) in termDocument.withIndex()) {
                scoreActivity.termName.add(element.text())
                scoreActivity.termId.add(element.attr("value"))
                if (element.hasAttr("selected")) {
                    selected = i
                }
            }
            return selected
        }

        private fun parseGrades(document: String): ArrayList<Score> {
            val document = Jsoup.parse(document)
            val table = document.select("table[id=dataList] > tbody")
            val trs = table.select("tr")

            if (trs.size == 2 && trs[1].text() == "未查询到数据") {
                return ArrayList()
            }
            val scores = mutableListOf<Score>() as ArrayList
            for (i in 1 until trs.size) {
                val tds = trs[i].select("td")
                val term = tds[1].text()
                val name = tds[3].text()
                val score = tds[4].text()
                val skillScores = tds[5].text()
                val performanceScore = tds[6].text()
                val knowledgePoints = tds[7].text()
                val credits = tds[9].text().toDouble()
                val examAttribute = tds[10].text()
                val subjectNature = tds[14].text()
                val subjectAttribute = tds[15].text()
                scores.add(Score(term, name, score, skillScores, performanceScore, knowledgePoints, credits, examAttribute, subjectNature, subjectAttribute))
            }
            return scores
        }
    }
}