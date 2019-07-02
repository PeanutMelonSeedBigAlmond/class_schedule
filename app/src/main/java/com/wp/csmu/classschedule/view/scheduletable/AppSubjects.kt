package com.wp.csmu.classschedule.view.scheduletable


class AppSubjects {
    companion object {
        var subjects = HashSet<Subjects>()
            set(value) {
                if (subjects.isNotEmpty()) {
                    subjects.clear()
                }
                field = value
            }
    }
}