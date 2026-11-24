package com.onioncalendar.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** 매주 반복되는 수업 (시간표 한 칸). */
public class Lecture implements TimeBlock {
    private String subject;
    private DayOfWeek day;
    private LocalTime start;
    private LocalTime end;
    private String room;

    public Lecture(String subject, DayOfWeek day, LocalTime start, LocalTime end, String room) {
        this.subject = subject;
        this.day = day;
        this.start = start;
        this.end = end;
        this.room = room;
    }

    /** 같은 요일이고 시간이 겹치면 true. 10:30 종료와 10:30 시작은 겹치지 않는다. */
    public boolean overlaps(Lecture other) {
        return day == other.day && start.isBefore(other.end) && other.start.isBefore(end);
    }

    @Override
    public String getTitle() { return subject; }

    /** 구간 안에서 이 수업이 열리는 날의 수업 시간과 겹치면 true */
    @Override
    public boolean occupies(LocalDateTime from, LocalDateTime to) {
        for (var d = from.toLocalDate(); !d.isAfter(to.toLocalDate()); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != day) continue;
            LocalDateTime s = d.atTime(start), e = d.atTime(end);
            if (s.isBefore(to) && from.isBefore(e)) return true;
        }
        return false;
    }

    public String getSubject() { return subject; }
    public DayOfWeek getDay() { return day; }
    public LocalTime getStart() { return start; }
    public LocalTime getEnd() { return end; }
    public String getRoom() { return room; }
}
