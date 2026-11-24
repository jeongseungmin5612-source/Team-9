package com.onioncalendar.controller;

import com.onioncalendar.model.Lecture;
import com.onioncalendar.model.Onion;
import com.onioncalendar.model.OnionStatus;
import com.onioncalendar.model.Schedule;
import com.onioncalendar.model.ScheduleBook;
import com.onioncalendar.model.ScheduleType;
import com.onioncalendar.model.TimeBlock;
import com.onioncalendar.service.StudyGoal;
import com.onioncalendar.service.StudyPlanner;
import com.onioncalendar.util.ScheduleRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** 캘린더·일정 화면과 데이터(ScheduleBook)를 잇는 컨트롤러. 바뀔 때마다 파일에 저장한다. */
public class ScheduleController {
    private final ScheduleBook book;
    private final ScheduleRepository repository;
    private final StudyPlanner planner;
    private final List<ScheduleListener> listeners = new ArrayList<>();
    private String lastError = "";

    public ScheduleController(ScheduleBook book, ScheduleRepository repository, StudyPlanner planner) {
        this.book = book;
        this.repository = repository;
        this.planner = planner;
    }

    public List<Schedule> loadMonth(YearMonth month) { return book.findByMonth(month); }

    public List<TimeBlock> loadDay(LocalDate date) { return book.findByDate(date); }

    public boolean addLecture(Lecture l) {
        if (!validate(l)) return false;
        Lecture overlap = book.findOverlap(l);
        if (overlap != null) {
            lastError = "\"" + overlap.getSubject() + "\" 수업과 시간이 겹칩니다.";
            return false;
        }
        book.add(l);
        saveAndNotify();
        return true;
    }

    /** 과제·개인 일정 추가. 시험은 addExam을 쓴다. */
    public boolean addSchedule(Schedule s) {
        if (s.getType() == ScheduleType.EXAM || s.getType() == ScheduleType.STUDY) {
            throw new IllegalArgumentException("시험은 addExam, 공부 일정은 StudyPlanner로 추가");
        }
        if (!validate(s)) return false;
        s.setDone(false);
        book.add(s);
        saveAndNotify();
        return true;
    }

    /** 시험 추가 + 공부 일정 자동 할당. goal이 null이면 시험만 추가. */
    public boolean addExam(Schedule exam, StudyGoal goal) {
        if (exam.getType() != ScheduleType.EXAM) throw new IllegalArgumentException("시험이 아님");
        if (!validate(exam)) return false;
        book.add(exam);
        if (goal != null) for (Schedule st : planner.allocate(exam, goal)) book.add(st);
        saveAndNotify();
        return true;
    }

    public void setDone(Schedule s, boolean done) {
        s.setDone(done);
        saveAndNotify();
    }

    /**
     * 수정한 내용을 반영한다. updated는 원래 일정과 id가 같은 새 객체다.
     * 시험이고 goal이 있으면 안 한 공부 일정을 지우고 다시 할당한다. (UC4 2-3)
     */
    public boolean updateSchedule(Schedule updated, StudyGoal goal) {
        if (!validate(updated)) return false;
        Schedule old = findById(updated.getId());
        if (old == null) { lastError = "수정할 일정을 찾을 수 없습니다."; return false; }
        updated.setDone(old.isDone());
        book.remove(old);
        book.add(updated);
        if (updated.getType() == ScheduleType.EXAM && goal != null) {
            book.removeUndoneStudy(updated.getId());
            for (Schedule st : planner.allocate(updated, goal)) book.add(st);
        }
        saveAndNotify();
        return true;
    }

    /** 삭제. 시험이면 그 시험으로 만든 안 한 공부 일정도 함께 지운다. (UC4 2-4) */
    public void deleteSchedule(Schedule s) {
        book.remove(s);
        if (s.getType() == ScheduleType.EXAM) book.removeUndoneStudy(s.getId());
        saveAndNotify();
    }

    public OnionStatus getOnionStatus() {
        LocalDate today = LocalDate.now();
        List<Schedule> all = book.getSchedules();
        int progress = Onion.calcProgress(all, today);
        int doneCount = 0;
        for (Schedule s : all) if (Onion.isCounted(s, today) && s.isDone()) doneCount++;
        return new OnionStatus(progress, Onion.getStage(progress), doneCount);
    }

    public void addListener(ScheduleListener l) { listeners.add(l); }

    /** 마지막 검증 실패 이유 (화면 경고 문구용) */
    public String getLastError() { return lastError; }

    private Schedule findById(String id) {
        for (Schedule s : book.getSchedules()) if (s.getId().equals(id)) return s;
        return null;
    }

    private boolean validate(TimeBlock b) {
        if (b.getTitle() == null || b.getTitle().isBlank()) {
            lastError = "제목을 입력하세요.";
            return false;
        }
        if (b instanceof Lecture) {
            Lecture l = (Lecture) b;
            if (!l.getStart().isBefore(l.getEnd())) { lastError = "시작 시간이 종료 시간보다 빨라야 합니다."; return false; }
            if (l.getStart().getMinute() % 30 != 0 || l.getEnd().getMinute() % 30 != 0) {
                lastError = "시간은 30분 단위로 입력하세요.";
                return false;
            }
            return true;
        }
        Schedule s = (Schedule) b;
        if (s.getEnd() == null) { lastError = "날짜와 시간을 입력하세요."; return false; }
        if (s.getStart() != null && !s.getStart().isBefore(s.getEnd())) {
            lastError = "시작 시간이 종료 시간보다 빨라야 합니다.";
            return false;
        }
        return true;
    }

    private void saveAndNotify() {
        repository.save(book);
        notifyChanged();
    }

    private void notifyChanged() {
        for (ScheduleListener l : listeners) l.onScheduleChanged();
    }
}
