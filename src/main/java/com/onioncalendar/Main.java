package com.onioncalendar;

import com.onioncalendar.controller.ScheduleApp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());   // 윈도우 기본 모양
        } catch (Exception ignored) {
            // 실패하면 자바 기본 모양으로 실행
        }
        SwingUtilities.invokeLater(() -> new ScheduleApp().start());
    }
}
