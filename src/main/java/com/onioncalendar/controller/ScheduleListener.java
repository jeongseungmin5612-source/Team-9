package com.onioncalendar.controller;

/** 일정이 바뀌었을 때 화면을 다시 그리도록 알림을 받는 쪽 */
public interface ScheduleListener {
    void onScheduleChanged();
}
