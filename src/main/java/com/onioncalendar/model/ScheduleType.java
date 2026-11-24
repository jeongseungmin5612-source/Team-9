package com.onioncalendar.model;

/** 일정 유형. 수업(시간표)은 Lecture로 따로 관리한다. */
public enum ScheduleType {
    ASSIGNMENT("과제"),
    EXAM("시험"),
    STUDY("공부"),
    PERSONAL("개인");

    private final String label;

    ScheduleType(String label) { this.label = label; }

    /** 화면에 보여줄 한글 이름 */
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
