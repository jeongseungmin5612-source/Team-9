package com.onioncalendar.service;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 작업 표시줄 우측 시스템 트레이(System Tray) 아이콘 생성 및 이벤트를 관리하는 클래스입니다.
 */
public class TrayManager {
    private TrayIcon trayIcon;

    public interface TrayCallbacks {
        void onShowCalendar();
        void onExit();
    }

    /**
     * 시스템 트레이 아이콘을 등록하고 우클릭 팝업 메뉴 및 더블클릭 이벤트를 연결합니다.
     */
    public boolean showTrayIcon(Image iconImage, String tooltip, TrayCallbacks callbacks) {
        if (!SystemTray.isSupported() || iconImage == null) return false;

        SystemTray tray = SystemTray.getSystemTray();
        if (trayIcon != null) tray.remove(trayIcon);

        // 트레이 메뉴 설정
        PopupMenu popup = new PopupMenu();
        MenuItem openItem = new MenuItem("캘린더 열기");
        openItem.addActionListener(e -> { if (callbacks != null) callbacks.onShowCalendar(); });

        MenuItem exitItem = new MenuItem("종료");
        exitItem.addActionListener(e -> { if (callbacks != null) callbacks.onExit(); });

        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);

        // 트레이 아이콘 생성 및 쥐 클릭 동작 정의
        trayIcon = new TrayIcon(iconImage, tooltip, popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 더블 클릭 시 메인 창 열기
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    if (callbacks != null) callbacks.onShowCalendar();
                }
            }
        });

        try {
            tray.add(trayIcon);
            return true;
        } catch (AWTException e) {
            return false;
        }
    }

    /**
     * 등록된 트레이 아이콘을 제거합니다.
     */
    public void removeTrayIcon() {
        if (SystemTray.isSupported() && trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }
}
