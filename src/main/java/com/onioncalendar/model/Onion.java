package com.onioncalendar.model;

import java.time.LocalDate;
import java.util.List;

/**
 * 진행도 계산과 양파 단계 판정.
 * calcProgress, getStage: 일정 파트(이로운) 구현.
 * getBubble, getAsciiArt: 양파 파트(정승민)가 채울 자리 (지금은 임시 문구).
 */
public class Onion {
    public static final int FRESH_RATE = 80;
    public static final int WILTED_RATE = 50;
    /** 최근 며칠을 셀지 (오늘 포함) */
    public static final int WINDOW_DAYS = 14;
    /** 셀 일정이 없을 때 calcProgress가 돌려주는 값 */
    public static final int NO_DATA = -1;

    private Onion() { }

    /**
     * 최근 14일 과제·공부 일정의 완료 비율(0~100).
     * 어제까지 일정은 모두 세고, 오늘 일정은 완료한 것만 센다. 셀 일정이 없으면 NO_DATA.
     */
    public static int calcProgress(List<Schedule> items, LocalDate today) {
        int total = 0, done = 0;
        for (Schedule s : items) {
            if (!isCounted(s, today)) continue;
            total++;
            if (s.isDone()) done++;
        }
        if (total == 0) return NO_DATA;
        return (int) Math.round(done * 100.0 / total);
    }

    /** calcProgress가 세는 일정인가 */
    public static boolean isCounted(Schedule s, LocalDate today) {
        if (s.getType() != ScheduleType.ASSIGNMENT && s.getType() != ScheduleType.STUDY) return false;
        LocalDate d = s.getDate();
        LocalDate from = today.minusDays(WINDOW_DAYS - 1);
        if (d.isBefore(from) || d.isAfter(today)) return false;
        return d.isBefore(today) || s.isDone();
    }

    public static OnionStage getStage(int progress) {
        if (progress == NO_DATA) return OnionStage.DEFAULT;
        if (progress >= FRESH_RATE) return OnionStage.FRESH;
        if (progress >= WILTED_RATE) return OnionStage.WILTED;
        return OnionStage.ROTTEN;
    }

    /** TODO(정승민): 단계별 말풍선 */
    public static String getBubble(OnionStage stage) {
        switch (stage) {
            case FRESH: return "잘하고 있어!";
            case WILTED: return "조금 밀렸다!";
            case ROTTEN: return "이게 스케줄이냐?";
            default: return "일정을 등록해라!";
        }
    }

    /** TODO(정승민): 단계별 아스키 아트 */
    public static String getAsciiArt(OnionStage stage) {
        return "  (onion: " + stage + ")";
    }
}
