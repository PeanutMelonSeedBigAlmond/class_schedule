package com.wp.csmu.classschedule.network

import android.util.Base64
import com.wp.csmu.classschedule.activity.ScoreActivity
import com.wp.csmu.classschedule.io.IO
import com.wp.csmu.classschedule.view.bean.Score
import com.wp.csmu.classschedule.view.scheduletable.Subjects
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import retrofit2.Retrofit
import java.util.regex.Pattern

object LoginClient {
    public enum class State {
        SUCCESS, WRONG_PASSWORD, NEED_VERIFY_CODE, NOT_LOGIN
    }

    private val retrofit = Retrofit.Builder().baseUrl(Config.baseUrl).build()
    private val client = retrofit.create(LoginApi::class.java)
    fun getCookie() {
        val response = client.getCookie().execute().raw().header("set-cookie")
        Config.cookie = response
    }

    private fun mLogin(userName: String, password: String): State {
        val encoded = encode(userName, password)
        val response = client.login(Config.cookie!!, encoded).execute().body()
        val document = Jsoup.parse(response.string())
        if (document.title() == "学术个人中心") {
            Config.state = State.SUCCESS
            return State.SUCCESS
        } else {
            if (document.select("div[class=dlmi] > font[color=red]").text().trim().contains("验证码")) {
                Config.state = State.NEED_VERIFY_CODE
                return State.NEED_VERIFY_CODE
            }
            Config.state = State.NEED_VERIFY_CODE
            return State.WRONG_PASSWORD
        }
    }

    private fun mLoginWithVerifyCode(userName: String, password: String, verifyCode: String): State {
        val encoded = encode(userName, password)
        val response = client.loginWithVerifyCode(Config.cookie!!, encoded, verifyCode).execute().body()
        val document = Jsoup.parse(response.string())
        return if (document.title() == "学生个人中心") {
            State.SUCCESS
        } else {
            State.WRONG_PASSWORD
        }
    }

    fun downloadVerifyCode() {
        val response = client.getVerifyCode(Config.cookie!!).execute().body()
        val inputStream = response.byteStream()
        IO.writeVerifyCodeImage(inputStream)
    }

    public fun login(userName: String, password: String, verifyCode: String = ""): State {
        return if (verifyCode == "") {
            mLogin(userName, password)
        } else {
            if (mLoginWithVerifyCode(userName, password, verifyCode) == State.NEED_VERIFY_CODE) {
                downloadVerifyCode()
                Config.state = State.NEED_VERIFY_CODE
                State.NEED_VERIFY_CODE
            } else {
                Config.state = State.SUCCESS
                State.SUCCESS
            }
        }
    }

    private fun encode(userName: String, password: String) = String(Base64.encode(userName.toByteArray(), Base64.DEFAULT)).plus("%%%").plus(String(Base64.encode(password.toByteArray(), Base64.DEFAULT)))
}

object DataClient {
    private val retrofit = Retrofit.Builder().baseUrl(Config.baseUrl).build()
    private val client = retrofit.create(DataApi::class.java)

    public fun getSchedule() = run {
        parseSchedule(mGetSchedule())
        Config.schedules
    }

    private fun mGetSchedule(): Document {
        val response = client.getSchedule(Config.cookie!!).execute().body()
        val content = response.string()
        val document = Jsoup.parse(content)
        return document
    }

    fun parseSchedule(document: Document) {
        val termDocument = document.select("select[name=xnxq01id]").first().select("option")
        for (element in termDocument) {
            val key = element.attr("value")
            val value = element.text()
            Config.termSelection[key] = value
            //println("key=$key map=$value")
        }
        /********获取课程表***************/
        val scheduleDocument = document.select("table[id=kbtable]").first()
        getSchedule(scheduleDocument)
        /********获取开学时间***************/
        Config.termBeginsTime = getTermBeginsTime()
    }

    fun getSchedule(htmlTable: Element) {
        fun parseWeeks(text: String): Array<Int>? {
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

        fun parseTimes(text: String): Array<Int>? {
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

        fun parseSchedule(element: Element, currentDay: Int) {
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
                    Config.schedules.add(subjects)
                } catch (e: Exception) {
                }
            }
        }

        val elements = htmlTable.selectFirst("tbody").select("tr")
        for (i in 1 until elements.size) {
            val classElements = elements[i].select("td").select("div[class=kbcontent]")
            for (j in 0 until classElements.size) {
                parseSchedule(classElements[j], j + 1)
            }
        }
    }

    fun mGetTermBeginsTime(): Document {
        val response = client.getTermBeginsTime(Config.cookie!!).execute().body()
        val content = response.string()
        val document = Jsoup.parse(content)
        return document
    }

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

    public fun getTermBeginsTime() = parseTermBeginsTime(mGetTermBeginsTime().selectFirst("table[id=kbtable] > tbody"))

    private fun parseGrades(document: Document): ArrayList<Score> {
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

    public fun queryTerms(scoreActivity: ScoreActivity): Int {
        val response = client.queryTerms(Config.cookie!!).execute().body()
        val content = response.string()
        val document = Jsoup.parse(content)
        val termDocument = document.select("select[id=kksj]").select("option")
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

    public fun getGrades(term: String): ArrayList<Score> {
        val response = client.getGrades(Config.cookie!!, term).execute().body()
        val content = response.string()
        val document = Jsoup.parse(content)
        return parseGrades(document)
    }
}

object Config {
    var termSelection = HashMap<String, String>()
    @JvmStatic
    var schedules = HashSet<Subjects>()

    @JvmStatic
    var termBeginsTime = ""

    internal var cookie: String? = null
        get() {
            if (field == null) {
                LoginClient.getCookie()
            }
            return field
        }
    internal const val baseUrl = "http://jiaowu.csmu.edu.cn:8099/jsxsd/"

    var state = LoginClient.State.NOT_LOGIN
}