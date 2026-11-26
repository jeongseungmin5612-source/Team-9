package com.onioncalendar.model;

/**
 * 캘린더에 등록되는 개별 일정 항목 데이터를 관리하는 모델 클래스입니다.
 */
public class ScheduleItem {
    private String title;       // 일정 제목 또는 과목명
    private String type;        // 일정 유형 (과제, 시험, 공부, 개인)
    private String timeRange;   // 시간 범위 (예: "18:00~19:30")
    private String memo;        // 상세 메모
    private boolean isCompleted; // 완료 여부

    public ScheduleItem(String title, String type, String timeRange, String memo) {
        this.title = title;
        this.type = type;
        this.timeRange = timeRange;
        this.memo = memo;
        this.isCompleted = false;
    }

    // Getter & Setter
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getTimeRange() { return timeRange; }
    public String getMemo() { return memo; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    /**
     * 일정 정렬용: 시작 시간(시 단위)을 정수형으로 반환합니다.
     */
    public int getStartHour() {
        if (timeRange == null || timeRange.isEmpty()) return 99;
        try {
            String cleanStr = timeRange.replace(":", "").replace("-", " ").replace("~", " ").trim();
            String firstPart = cleanStr.split(" ")[0];
            return Integer.parseInt(firstPart);
        } catch (Exception e) {
            return 99;
        }
    }

    /**
     * 시간 중복 검사용: 하루 기준 시작 시간(분 단위)을 계산합니다.
     */
    public int getStartMinuteOfDay() {
        return parseMinute(true);
    }

    /**
     * 시간 중복 검사용: 하루 기준 종료 시간(분 단위)을 계산합니다.
     */
    public int getEndMinuteOfDay() {
        return parseMinute(false);
    }

    private int parseMinute(boolean isStart) {
        if (timeRange == null || timeRange.isEmpty()) return isStart ? 0 : 1439;
        try {
            String[] parts = timeRange.split("~");
            String target = isStart ? parts[0].trim() : (parts.length > 1 ? parts[1].trim() : parts[0].trim());
            String[] hm = target.split(":");
            int h = Integer.parseInt(hm[0].trim());
            int m = hm.length > 1 ? Integer.parseInt(hm[1].trim()) : 0;
            return h * 60 + m;
        } catch (Exception e) {
            return isStart ? 0 : 1439;
        }
    }
}
