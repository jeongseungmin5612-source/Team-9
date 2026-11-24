package com.onioncalendar.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 프로그램이 켜져 있는 동안 수업과 일정을 들고 있는 저장소 (메모리). */
public class ScheduleBook {
    private final List<TimeBlock> items = new ArrayList<>();

    public void add(TimeBlock b) { items.add(b); }

    public void remove(Schedule s) { items.remove(s); }

    /** 수업 삭제 (시간표 화면용) */
    public void removeLecture(Lecture l) { items.remove(l); }

    /** 해당 달에 표시할 일정 (수업 제외), 날짜순 */
    public List<Schedule> findByMonth(YearMonth month) {
        List<Schedule> out = new ArrayList<>();
        for (Schedule s : getSchedules()) {
            if (YearMonth.from(s.getDate()).equals(month)) out.add(s);
        }
        out.sort(Comparator.comparing(Schedule::getDate));
        return out;
    }

    /** 해당 날짜의 수업 + 일정, 시간순 (시간 없는 일정은 맨 뒤) */
    public List<TimeBlock> findByDate(LocalDate date) {
        List<TimeBlock> out = new ArrayList<>();
        for (TimeBlock b : items) {
            if (b instanceof Lecture && ((Lecture) b).getDay() == date.getDayOfWeek()) out.add(b);
            if (b instanceof Schedule && ((Schedule) b).getDate().equals(date)) out.add(b);
        }
        out.sort(Comparator.comparing(ScheduleBook::sortTime));
        return out;
    }

    private static LocalTime sortTime(TimeBlock b) {
        if (b instanceof Lecture) return ((Lecture) b).getStart();
        Schedule s = (Schedule) b;
        return s.getStart() != null ? s.getStart().toLocalTime() : LocalTime.MAX;
    }

    /** 이미 등록된 수업 중 겹치는 수업. 없으면 null. 자기 자신은 제외. */
    public Lecture findOverlap(Lecture l) {
        for (TimeBlock b : items) {
            if (b instanceof Lecture && b != l && ((Lecture) b).overlaps(l)) return (Lecture) b;
        }
        return null;
    }

    /** 마감이 내일인 미완료 과제 (강제 알림용) */
    public List<Schedule> findDueTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Schedule> out = new ArrayList<>();
        for (Schedule s : getSchedules()) {
            if (s.getType() == ScheduleType.ASSIGNMENT && !s.isDone()
                    && s.getEnd() != null && s.getEnd().toLocalDate().equals(tomorrow)) out.add(s);
        }
        return out;
    }

    /** 시험(examId)으로 만든 공부 일정 중 아직 안 한 것 삭제 */
    public void removeUndoneStudy(String examId) {
        items.removeIf(b -> b instanceof Schedule
                && ((Schedule) b).getType() == ScheduleType.STUDY
                && examId.equals(((Schedule) b).getExamId())
                && !((Schedule) b).isDone());
    }

    public List<TimeBlock> getAll() { return new ArrayList<>(items); }

    /** 일정만 (수업 제외) */
    public List<Schedule> getSchedules() {
        List<Schedule> out = new ArrayList<>();
        for (TimeBlock b : items) if (b instanceof Schedule) out.add((Schedule) b);
        return out;
    }

    /** 수업만 */
    public List<Lecture> getLectures() {
        List<Lecture> out = new ArrayList<>();
        for (TimeBlock b : items) if (b instanceof Lecture) out.add((Lecture) b);
        return out;
    }
}
