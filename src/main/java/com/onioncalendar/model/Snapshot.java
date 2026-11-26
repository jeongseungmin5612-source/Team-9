package com.onioncalendar.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 되돌리기(Undo) 기능을 제공하기 위해 특정 시점의 전체 일정 데이터를 저장하는 스냅샷 클래스입니다.
 */
public class Snapshot {
    private final Map<LocalDate, List<ScheduleItem>> state;

    public Snapshot(Map<LocalDate, List<ScheduleItem>> state) {
        this.state = state;
    }

    public Map<LocalDate, List<ScheduleItem>> getState() {
        return state;
    }
}
