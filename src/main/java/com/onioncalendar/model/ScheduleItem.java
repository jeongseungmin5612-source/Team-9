package com.onioncalendar.model;

/**
 * 캘린더에 들어가는 개별 일정 데이터 모델
 */
public class ScheduleItem {
    private String title;
    private String type;       // "수업", "과제", "개인", "공부"
    private String timeRange;  // 예: "09-12시", "23:59"
    private boolean isAutoAssigned;

    public ScheduleItem(String title, String type, String timeRange, boolean isAutoAssigned) {
        this.title = title;
        this.type = type;
        this.timeRange = timeRange;
        this.isAutoAssigned = isAutoAssigned;
    }

    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getTimeRange() { return timeRange; }
    public boolean isAutoAssigned() { return isAutoAssigned; }
}
