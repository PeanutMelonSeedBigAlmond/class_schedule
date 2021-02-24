package com.wp.csmu.classschedule.view.bean

data class Score(
        var term: String,
        var name: String,
        var score: String,
        var skillScores: String,
        var performanceScore: String,
        var knowledgePoints: String,
        var credits: Double,
        var examAttribute: String,
        var subjectNature: String,
        var subjectAttribute: String
)