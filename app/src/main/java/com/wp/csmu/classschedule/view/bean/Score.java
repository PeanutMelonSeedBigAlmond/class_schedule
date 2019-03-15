package com.wp.csmu.classschedule.view.bean;

public class Score {
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubjectEnglish() {
        return subjectEnglish;
    }

    public void setSubjectEnglish(String subjectEnglish) {
        this.subjectEnglish = subjectEnglish;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getSubjectAttribute() {
        return subjectAttribute;
    }

    public void setSubjectAttribute(String subjectAttribute) {
        this.subjectAttribute = subjectAttribute;
    }

    public String getExamAttribute() {
        return examAttribute;
    }

    public void setExamAttribute(String examAttribute) {
        this.examAttribute = examAttribute;
    }

    public String getSubjectNature() {
        return subjectNature;
    }

    public void setSubjectNature(String subjectNature) {
        this.subjectNature = subjectNature;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    String note;
    String subject;
    String subjectEnglish;
    String term;
    String subjectAttribute;
    String examAttribute;
    String subjectNature;
    int credit;
    String score;
}
