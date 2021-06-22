package com.wp.csmu.classschedule.view.bean

data class ScoreFilterBean(
        val termId:LinkedHashMap<Pair<String,String>,Boolean>,
        val courseXZ:LinkedHashMap<Pair<String,String>,Boolean>,
        val displayMode:LinkedHashMap<Pair<String,String>,Boolean>,
)