package com.onioncalendar.controller;

import com.onioncalendar.model.ScheduleBook;
import com.onioncalendar.service.StudyPlanner;
import com.onioncalendar.util.ScheduleRepository;
import com.onioncalendar.view.CalendarPanel;
import com.onioncalendar.view.MainFrame;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.nio.file.Path;
import java.nio.file.Paths;

/** 프로그램 조립: 저장소 → 컨트롤러 → 화면. (트레이·시작 프로그램은 정승민 파트에서 연결) */
public class ScheduleApp {
    private final ScheduleController scheduleController;
    private final MainFrame frame;

    public ScheduleApp(Path dataDir) {
        ScheduleRepository repository = new ScheduleRepository(dataDir.resolve("schedules.json"));
        ScheduleBook book = repository.load();
        scheduleController = new ScheduleController(book, repository, new StudyPlanner());

        frame = new MainFrame();
        CalendarPanel calendar = new CalendarPanel(scheduleController);
        frame.addPanel(MainFrame.CALENDAR, calendar);
        frame.addPanel(MainFrame.TIMETABLE, placeholder("시간표 화면 (구현 예정)"));
        frame.addPanel(MainFrame.SHARE, placeholder("공유 화면 (구현 예정)"));
        frame.addPanel(MainFrame.SETTINGS, placeholder("설정 화면 (정승민 파트)"));
        frame.getAddButton().addActionListener(e -> calendar.openNewSchedule());
    }

    public ScheduleApp() { this(Paths.get("data")); }

    public void start() { showCalendar(); }

    public void showCalendar() {
        frame.showPanel(MainFrame.CALENDAR);
        frame.setVisible(true);
        frame.toFront();
    }

    public void exit() {
        frame.dispose();
        System.exit(0);
    }

    public ScheduleController getScheduleController() { return scheduleController; }

    public MainFrame getFrame() { return frame; }

    private static JPanel placeholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(text, JLabel.CENTER);
        l.setFont(com.onioncalendar.view.UiStyle.font(16, false));
        p.add(l, BorderLayout.CENTER);
        return p;
    }
}
