package com.onioncalendar.service;

import com.onioncalendar.model.TimeBlock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 여러 사람의 수업·일정을 겹쳐 30분 칸마다 바쁜 인원을 세고 만날 시간을 추천한다 (UC7). */
public class FreeTimeFinder {
    public static final int SLOT_MINUTES = 30;
    /** 추천 최대 개수 */
    public static final int MAX_RECOMMEND = 5;

    /**
     * @param blocks 사람마다의 수업·일정 목록 (나 포함)
     * @param week   조회할 주의 월요일
     * @param from   하루 중 시작 시각 (30분 단위)
     * @param to     하루 중 끝 시각
     */
    public FreeTimeResult findFreeTime(List<List<TimeBlock>> blocks, LocalDate week, LocalTime from, LocalTime to) {
        if (blocks.isEmpty() || !from.isBefore(to)) throw new IllegalArgumentException("인원 또는 시간 범위가 잘못됨");
        int slots = (int) (java.time.Duration.between(from, to).toMinutes() / SLOT_MINUTES);
        int[][] busy = new int[7][slots];
        for (int d = 0; d < 7; d++) {
            LocalDate date = week.plusDays(d);
            for (int s = 0; s < slots; s++) {
                LocalDateTime a = date.atTime(from).plusMinutes((long) s * SLOT_MINUTES);
                LocalDateTime b = a.plusMinutes(SLOT_MINUTES);
                for (List<TimeBlock> person : blocks) {
                    for (TimeBlock tb : person) {
                        if (tb.occupies(a, b)) { busy[d][s]++; break; }   // 한 사람은 한 칸에 한 번만
                    }
                }
            }
        }
        List<TimeSlot> rec = runs(busy, week, from, 0, 2);        // 모두 비는 1시간 이상
        if (rec.isEmpty()) {
            int min = Integer.MAX_VALUE;
            for (int d = 0; d < 7; d++) {
                for (int s = 0; s < slots; s++) {
                    if (!isPast(week.plusDays(d).atTime(from).plusMinutes((long) s * SLOT_MINUTES))) {
                        min = Math.min(min, busy[d][s]);
                    }
                }
            }
            if (min == Integer.MAX_VALUE) min = 0;   // 주 전체가 지난 경우
            rec = runs(busy, week, from, min, 1);                 // 없으면 바쁜 인원이 가장 적은 칸
        }
        rec.sort(Comparator.comparing(TimeSlot::start));
        if (rec.size() > MAX_RECOMMEND) rec = new ArrayList<>(rec.subList(0, MAX_RECOMMEND));
        return new FreeTimeResult(busy, blocks.size(), week, from, rec);
    }

    /** 이미 지난 칸은 추천하지 않는다 */
    private boolean isPast(LocalDateTime slotStart) {
        return slotStart.isBefore(LocalDateTime.now());
    }

    /** 값이 target인 칸이 minLen 이상 이어지는 구간들 (지난 칸 제외) */
    private List<TimeSlot> runs(int[][] busy, LocalDate week, LocalTime from, int target, int minLen) {
        List<TimeSlot> out = new ArrayList<>();
        for (int d = 0; d < 7; d++) {
            int start = -1;
            for (int s = 0; s <= busy[d].length; s++) {
                boolean hit = s < busy[d].length && busy[d][s] == target
                        && !isPast(week.plusDays(d).atTime(from).plusMinutes((long) s * SLOT_MINUTES));
                if (hit && start < 0) start = s;
                if (!hit && start >= 0) {
                    if (s - start >= minLen) {
                        LocalDateTime a = week.plusDays(d).atTime(from).plusMinutes((long) start * SLOT_MINUTES);
                        out.add(new TimeSlot(a, a.plusMinutes((long) (s - start) * SLOT_MINUTES), target));
                    }
                    start = -1;
                }
            }
        }
        return out;
    }
}
