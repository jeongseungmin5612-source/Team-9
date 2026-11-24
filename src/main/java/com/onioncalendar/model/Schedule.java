package com.onioncalendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 일정 하나.
 * <ul>
 *   <li>시간이 정해진 일정: start, end 모두 있음</li>
 *   <li>마감형 일정(과제, 시간 없는 공부): start = null, end = 마감 시각</li>
 * </ul>
 */
public class Schedule implements TimeBlock {
    private String id;
    private ScheduleType type;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean done;
    private String examId;

    public Schedule(ScheduleType type, String title, LocalDateTime start, LocalDateTime end) {
        this(UUID.randomUUID().toString(), type, title, start, end, false, null);
    }

    public Schedule(String id, ScheduleType type, String title, LocalDateTime start,
                    LocalDateTime end, boolean done, String examId) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.start = start;
        this.end = end;
        this.done = done;
        this.examId = examId;
    }

    /** 시작·종료 시각이 모두 있는가 */
    public boolean isTimed() { return start != null && end != null; }

    /** 캘린더에 표시할 날짜. 시작이 없으면 마감(end) 날짜. */
    public LocalDate getDate() { return start != null ? start.toLocalDate() : end.toLocalDate(); }

    /** 완료되지 않았고 마감이 지금부터 hours 시간 안에 있으면 true (강제 알림용) */
    public boolean isDueWithin(int hours) {
        LocalDateTime now = LocalDateTime.now();
        return !done && end != null && !end.isBefore(now) && !end.isAfter(now.plusHours(hours));
    }

    @Override
    public boolean occupies(LocalDateTime from, LocalDateTime to) {
        return isTimed() && start.isBefore(to) && from.isBefore(end);
    }

    /** 내보내기용 복사본 (원본은 건드리지 않음) */
    public Schedule copy() {
        return new Schedule(id, type, title, start, end, done, examId);
    }

    public String getId() { return id; }
    public ScheduleType getType() { return type; }
    public void setType(ScheduleType type) { this.type = type; }
    @Override public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }
}
