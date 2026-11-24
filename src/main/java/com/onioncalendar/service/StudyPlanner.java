package com.onioncalendar.service;

import com.onioncalendar.model.Schedule;
import com.onioncalendar.model.ScheduleType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 시험 하나에 대한 공부 일정 목록을 만든다 (저장은 ScheduleController가 한다).
 *
 * <p>※ 정현국 파트. 일정 등록 화면을 먼저 돌려보기 위한 <b>임시 구현</b>이며,
 * 현국 구현으로 교체한다. 메서드 이름과 매개변수는 클래스 다이어그램 그대로다.
 *
 * <p>임시 규칙: 시작일(오늘 이전이면 오늘)부터 시험 전날까지, 회독 → 문제풀이 순서로
 * 총 횟수를 날짜에 고르게 나눈다. 하루에 여러 회차가 몰리면 "1~3회독"처럼 묶는다.
 */
public class StudyPlanner {

    public List<Schedule> allocate(Schedule exam, StudyGoal goal) {
        List<Schedule> out = new ArrayList<>();
        LocalDate examDay = exam.getDate();
        LocalDate from = goal.startDate().isBefore(LocalDate.now()) ? LocalDate.now() : goal.startDate();
        int days = (int) (examDay.toEpochDay() - from.toEpochDay()); // 시험 전날까지
        int total = goal.readings() + goal.solvings();
        if (days <= 0 || total <= 0) return out;

        // 회차 k(0..total-1)를 날짜 floor(k * days / total)에 배정
        int k = 0;
        for (int d = 0; d < days && k < total; d++) {
            int lastExclusive = (int) Math.ceil((double) (d + 1) * total / days);
            int firstK = k;
            while (k < total && k < lastExclusive) k++;
            if (k == firstK) continue;
            LocalDate date = from.plusDays(d);
            addRange(out, exam, date, firstK, k, goal.readings());
        }
        return out;
    }

    /** 회차 [a, b)를 회독/문제풀이로 나눠 일정으로 만든다 */
    private void addRange(List<Schedule> out, Schedule exam, LocalDate date, int a, int b, int readings) {
        int ra = a, rb = Math.min(b, readings);
        if (ra < rb) out.add(study(exam, date, label(exam.getTitle(), ra + 1, rb, "회독")));
        int sa = Math.max(a, readings), sb = b;
        if (sa < sb) out.add(study(exam, date,
                label(exam.getTitle() + " 문제풀이", sa - readings + 1, sb - readings, "회")));
    }

    private String label(String base, int from, int to, String unit) {
        return base + " " + (from == to ? from + unit : from + "~" + to + unit);
    }

    private Schedule study(Schedule exam, LocalDate date, String title) {
        Schedule s = new Schedule(ScheduleType.STUDY, title, null, date.atTime(LocalTime.of(23, 59)));
        s.setExamId(exam.getId());
        return s;
    }
}
