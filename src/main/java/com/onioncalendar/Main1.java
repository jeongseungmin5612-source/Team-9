package com.onioncalendar;

import com.onioncalendar.view.MainCalendarApp;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainCalendarApp().setVisible(true));
    }
}
