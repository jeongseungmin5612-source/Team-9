package com.onioncalendar.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 친구가 보낸 공유 파일 한 개 (내보낼 때는 "나"도 이 형태로 파일에 쓴다).
 * progress가 Onion.NO_DATA(-1)이면 진행도 정보 없음.
 */
public class Friend {
    /** 이 날수 이상 지난 파일은 오래된 것으로 본다 (UC7 6-1) */
    public static final int OUTDATED_DAYS = 7;

    private final String name;
    private final int progress;
    private final LocalDate exportedAt;
    private final List<Lecture> lectures;
    private final List<Schedule> schedules;

    public Friend(String name, int progress, LocalDate exportedAt, List<Lecture> lectures, List<Schedule> schedules) {
        this.name = name;
        this.progress = progress;
        this.exportedAt = exportedAt;
        this.lectures = new ArrayList<>(lectures);
        this.schedules = new ArrayList<>(schedules);
    }

    public boolean isOutdated(LocalDate today) {
        return !exportedAt.plusDays(OUTDATED_DAYS).isAfter(today);
    }

    /** 공강 계산용: 수업 + 일정 */
    public List<TimeBlock> getBlocks() {
        List<TimeBlock> out = new ArrayList<>(lectures);
        out.addAll(schedules);
        return out;
    }

    public String getName() { return name; }
    public int getProgress() { return progress; }
    public LocalDate getExportedAt() { return exportedAt; }
    public List<Lecture> getLectures() { return new ArrayList<>(lectures); }
    public List<Schedule> getSchedules() { return new ArrayList<>(schedules); }

    @Override
    public String toString() { return name; }
}
