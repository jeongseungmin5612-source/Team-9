package com.onioncalendar.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** 공강 계산 결과: 요일×30분 칸별 바쁜 인원과 추천 구간. */
public class FreeTimeResult {
    private final int[][] busyCount;          // [요일 0=월 ~ 6=일][칸]
    private final int memberCount;
    private final LocalDate week;             // 그 주의 월요일
    private final LocalTime from;
    private final List<TimeSlot> recommended;

    public FreeTimeResult(int[][] busyCount, int memberCount, LocalDate week, LocalTime from, List<TimeSlot> recommended) {
        this.busyCount = busyCount;
        this.memberCount = memberCount;
        this.week = week;
        this.from = from;
        this.recommended = List.copyOf(recommended);
    }

    public int getBusyCount(int day, int slot) { return busyCount[day][slot]; }

    public int getSlotCount() { return busyCount.length == 0 ? 0 : busyCount[0].length; }

    public int getMemberCount() { return memberCount; }

    public LocalDate getWeek() { return week; }

    public LocalTime getFrom() { return from; }

    /** 추천 구간이 모두 비는 시간인가 (false면 바쁜 인원이 가장 적은 시간) */
    public boolean isAllFree() { return !recommended.isEmpty() && recommended.get(0).busy() == 0; }

    public List<TimeSlot> getRecommended() { return recommended; }
}
