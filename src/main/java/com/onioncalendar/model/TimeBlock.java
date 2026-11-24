package com.onioncalendar.model;

import java.time.LocalDateTime;

/** 캘린더 위의 시간 덩어리. 일정(Schedule)과 수업(Lecture)이 구현한다. */
public interface TimeBlock {
    String getTitle();

    /** [start, end) 구간과 겹치면 true */
    boolean occupies(LocalDateTime start, LocalDateTime end);
}
