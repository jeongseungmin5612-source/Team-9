package com.onioncalendar;

import com.onioncalendar.view.MainCalendarApp;
import javax.swing.SwingUtilities;

/**
 * CAMPLE 일정 관리 프로그램의 메인 실행 진입점(Entry Point) 클래스입니다.
 */
public class Main {
    public static void main(String[] args) {
        // Swing UI Thread-Safe 보장을 위한 SwingUtilities.invokeLater 사용
        SwingUtilities.invokeLater(() -> new MainCalendarApp().setVisible(true));
    }
}
