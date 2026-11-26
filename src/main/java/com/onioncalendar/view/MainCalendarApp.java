package com.onioncalendar.view;

import com.onioncalendar.model.ScheduleItem;
import com.onioncalendar.model.Snapshot;
import com.onioncalendar.service.StartupManager;
import com.onioncalendar.service.TrayManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

/**
 * 캘린더 화면 출력, 네비게이션, 상단바, 설정 패널 등 메인 GUI 창 전체를 담당하는 클래스입니다.
 */
public class MainCalendarApp extends JFrame {

    private static final Color COLOR_NAVY = new Color(44, 62, 103);
    private static final Color COLOR_BG = new Color(243, 245, 249);
    private static final Color COLOR_CARD = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(230, 233, 240);

    // 일정 타입별 범주 색상 정의
    private static final Color BG_TASK = new Color(254, 237, 222);
    private static final Color TEXT_TASK = new Color(194, 97, 24);
    
    private static final Color BG_EXAM = new Color(224, 231, 255);
    private static final Color TEXT_EXAM = new Color(67, 97, 238);

    private static final Color BG_STUDY = new Color(237, 231, 246);
    private static final Color TEXT_STUDY = new Color(124, 77, 255);

    private static final Color BG_PERSONAL = new Color(220, 245, 230);
    private static final Color TEXT_PERSONAL = new Color(46, 125, 50);

    private final Map<LocalDate, List<ScheduleItem>> scheduleMap = new HashMap<>();
    private final Stack<Snapshot> historyStack = new Stack<>();

    private YearMonth currentYearMonth = YearMonth.of(2026, 11);
    private JPanel calendarGridPanel;
    private JLabel currentMonthLabel;

    private final StartupManager startupManager = new StartupManager();
    private final TrayManager trayManager = new TrayManager();
    private OnionWidgetWindow onionWidget;

    private JTabbedPane mainTabbedPane;
    private JTextArea settingsLogArea;

    private int currentProgress = 72;

    public Map<LocalDate, List<ScheduleItem>> getScheduleMap() {
        return scheduleMap;
    }

    public MainCalendarApp() {
        setTitle("CAMPLE - 일정 관리 프로그램");
        setSize(1260, 840);

        // 마우스 툴팁 반응 속도 개선
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

        // X 버튼 누를 시 프로그램 완전 종료 대신 최소화(숨김) 처리
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        initSampleData(); // 초기 데이터 로드

        add(createTopNavBar(), BorderLayout.NORTH);

        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("캘린더", createMainCalendarContent());
        mainTabbedPane.addTab("설정", createSettingsPanel());
        add(mainTabbedPane, BorderLayout.CENTER);

        refreshCalendar();
        initTrayAndStartup();
    }

    private void initTrayAndStartup() {
        onionWidget = new OnionWidgetWindow(() -> {
            setVisible(true);
            toFront();
        });
        onionWidget.setVisible(true);

        trayManager.showTrayIcon(createFallbackIcon(), "CAMPLE 일정 관리", new TrayManager.TrayCallbacks() {
            @Override
            public void onShowCalendar() {
                SwingUtilities.invokeLater(() -> {
                    setVisible(true);
                    toFront();
                });
            }

            @Override
            public void onExit() {
                if (onionWidget != null) onionWidget.dispose();
                trayManager.removeTrayIcon();
                System.exit(0);
            }
        });
    }

    private Image createFallbackIcon() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(235, 165, 65));
        g2.fillOval(2, 2, 12, 12);
        g2.dispose();
        return img;
    }

    private void initSampleData() {
        addSchedule(LocalDate.of(2026, 11, 17), new ScheduleItem("동아리 회의", "개인", "18:00~19:30", "동아리방에서 장소 결정"));
        addSchedule(LocalDate.of(2026, 11, 18), new ScheduleItem("운영체제 과제", "과제", "23:59~23:59", "컴파일 테스트 후 제출"));
        addSchedule(LocalDate.of(2026, 11, 19), new ScheduleItem("데이터베이스 과제", "과제", "23:59~23:59", "ER 다이어그램 포함해서 제출"));
        addSchedule(LocalDate.of(2026, 11, 19), new ScheduleItem("스터디", "개인", "19:00~21:00", "알고리즘 3문제 풀이 리뷰"));
        addSchedule(LocalDate.of(2026, 11, 20), new ScheduleItem("팀플 회의", "개인", "14:00~16:00", "중간 발표 PPT 작성"));
        addSchedule(LocalDate.of(2026, 11, 24), new ScheduleItem("SW설계 중간보고서", "과제", "23:59~23:59", "유즈케이스 명세서 보완"));
        addSchedule(LocalDate.of(2026, 11, 30), new ScheduleItem("자료구조 실습", "과제", "23:59~23:59", "C언어 트리를 Java로 변환"));
    }

    public void addSchedule(LocalDate date, ScheduleItem item) {
        scheduleMap.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
    }

    /**
     * 실행 취소(Undo)용 스냅샷 저장
     */
    public void saveSnapshotForUndo() {
        Map<LocalDate, List<ScheduleItem>> snapshotMap = new HashMap<>();
        for (Map.Entry<LocalDate, List<ScheduleItem>> entry : scheduleMap.entrySet()) {
            List<ScheduleItem> newList = new ArrayList<>();
            for (ScheduleItem item : entry.getValue()) {
                newList.add(new ScheduleItem(item.getTitle(), item.getType(), item.getTimeRange(), item.getMemo()));
            }
            snapshotMap.put(entry.getKey(), newList);
        }
        historyStack.push(new Snapshot(snapshotMap));
    }

    /**
     * 마지막 스냅샷 상태로 일정 복원
     */
    private void undoSchedule() {
        if (!historyStack.isEmpty()) {
            scheduleMap.clear();
            scheduleMap.putAll(historyStack.pop().getState());
            refreshCalendar();
            recalculateProgress();
            JOptionPane.showMessageDialog(this, "이전 일정 상태로 되돌렸습니다.", "되돌리기 완료", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "되돌릴 변경 내역이 없습니다.", "알림", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void recalculateProgress() {
        if (onionWidget != null) {
            onionWidget.updateOnionStage(currentProgress);
        }
    }

    private JPanel createTopNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(COLOR_NAVY);
        navBar.setPreferredSize(new Dimension(0, 56));
        navBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        leftPanel.setOpaque(false);

        JLabel brandLabel = new JLabel("CAMPLE");
        brandLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);
        leftPanel.add(brandLabel);

        String[] tabs = {"캘린더", "시간표", "공유", "스케줄러", "설정"};
        for (int i = 0; i < tabs.length; i++) {
            final int tabIndex = i;
            JButton tabBtn = new JButton(tabs[i]);
            tabBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            tabBtn.setFocusPainted(false);
            tabBtn.setBorderPainted(false);
            tabBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (i == 0) {
                tabBtn.setBackground(Color.WHITE);
                tabBtn.setForeground(COLOR_NAVY);
            } else {
                tabBtn.setOpaque(false);
                tabBtn.setContentAreaFilled(false);
                tabBtn.setForeground(new Color(200, 210, 230));
            }

            tabBtn.addActionListener(e -> {
                if (tabIndex == 0) mainTabbedPane.setSelectedIndex(0);
                else if (tabIndex == 4) mainTabbedPane.setSelectedIndex(1);
                else JOptionPane.showMessageDialog(this, tabs[tabIndex] + " 화면으로 이동합니다.");
            });

            leftPanel.add(tabBtn);
        }
        navBar.add(leftPanel, BorderLayout.WEST);

        JButton addBtn = new JButton("+ 일정 추가");
        addBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        addBtn.setBackground(new Color(225, 130, 60));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> openScheduleFormDialog());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 12));
        rightPanel.setOpaque(false);
        rightPanel.add(addBtn);
        navBar.add(rightPanel, BorderLayout.EAST);

        return navBar;
    }

    private JPanel createMainCalendarContent() {
        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftNav.setOpaque(false);

        JButton prevBtn = createNavBtn("< 이전");
        JButton nextBtn = createNavBtn("다음 >");
        currentMonthLabel = new JLabel("", JLabel.LEFT);
        currentMonthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        currentMonthLabel.setForeground(COLOR_NAVY);

        prevBtn.addActionListener(e -> { currentYearMonth = currentYearMonth.minusMonths(1); refreshCalendar(); });
        nextBtn.addActionListener(e -> { currentYearMonth = currentYearMonth.plusMonths(1); refreshCalendar(); });

        leftNav.add(prevBtn);
        leftNav.add(currentMonthLabel);
        leftNav.add(nextBtn);
        headerPanel.add(leftNav, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        JButton undoBtn = new JButton("되돌리기");
        undoBtn.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        undoBtn.setBackground(new Color(108, 117, 125));
        undoBtn.setForeground(Color.WHITE);
        undoBtn.setFocusPainted(false);
        undoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        undoBtn.setPreferredSize(new Dimension(75, 28));
        undoBtn.setMargin(new Insets(2, 4, 2, 4));
        undoBtn.addActionListener(e -> undoSchedule());

        JButton saveBtn = new JButton("저장");
        saveBtn.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(60, 28));
        saveBtn.setMargin(new Insets(2, 4, 2, 4));
        saveBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "모든 일정 변경사항이 성공적으로 저장되었습니다.", "저장 완료", JOptionPane.INFORMATION_MESSAGE));

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("과제", BG_TASK, TEXT_TASK));
        legendPanel.add(createLegendItem("시험", BG_EXAM, TEXT_EXAM));
        legendPanel.add(createLegendItem("공부", BG_STUDY, TEXT_STUDY));
        legendPanel.add(createLegendItem("개인", BG_PERSONAL, TEXT_PERSONAL));

        rightControls.add(undoBtn);
        rightControls.add(saveBtn);
        rightControls.add(Box.createHorizontalStrut(10));
        rightControls.add(legendPanel);

        headerPanel.add(rightControls, BorderLayout.EAST);
        container.add(headerPanel, BorderLayout.NORTH);

        JPanel calendarCard = new JPanel(new BorderLayout(0, 5));
        calendarCard.setBackground(COLOR_CARD);
        calendarCard.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

        JPanel daysHeader = new JPanel(new GridLayout(1, 7));
        daysHeader.setBackground(new Color(248, 250, 254));
        daysHeader.setPreferredSize(new Dimension(0, 30));
        String[] weekDays = {"일", "월", "화", "수", "목", "금", "토"};
        for (String day : weekDays) {
            JLabel lbl = new JLabel(day, JLabel.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            if (day.equals("일")) lbl.setForeground(new Color(220, 53, 69));
            else if (day.equals("토")) lbl.setForeground(TEXT_EXAM);
            else lbl.setForeground(COLOR_NAVY);
            daysHeader.add(lbl);
        }
        calendarCard.add(daysHeader, BorderLayout.NORTH);

        calendarGridPanel = new JPanel(new GridLayout(5, 7, 1, 1));
        calendarGridPanel.setBackground(COLOR_BORDER);
        calendarCard.add(calendarGridPanel, BorderLayout.CENTER);

        container.add(calendarCard, BorderLayout.CENTER);

        JLabel footNote = new JLabel("💡 일정 위에 마우스를 올리면 작성한 메모를 확인할 수 있습니다.");
        footNote.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        footNote.setForeground(Color.GRAY);
        container.add(footNote, BorderLayout.SOUTH);

        return container;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("시작 프로그램 및 백그라운드 감시 설정");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(16, 0, 0, 0));

        JCheckBox chkAutoStart = new JCheckBox("시작 프로그램 등록 (부팅 시 자동 실행 및 트레이 상주)");
        chkAutoStart.setBackground(Color.WHITE);
        chkAutoStart.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

        JLabel desc = new JLabel("<html>체크하면 부팅 시 OS 레지스트리에 자동 실행 등록되어 백그라운드 감시를 시작합니다.<br>"
                + "등록에 실패하면 경고 안내 후 체크가 해제됩니다.</html>");
        desc.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        desc.setForeground(Color.GRAY);

        JPanel testProgressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        testProgressPanel.setBackground(Color.WHITE);
        testProgressPanel.add(new JLabel("테스트용 양파 진행도 조절: "));
        JButton btnDecrease = new JButton("-15%");
        JButton btnIncrease = new JButton("+15%");
        btnDecrease.addActionListener(e -> {
            currentProgress = Math.max(0, currentProgress - 15);
            recalculateProgress();
            settingsLogArea.append("[진행도 변경] 현재 진행도: " + currentProgress + "%\n");
        });
        btnIncrease.addActionListener(e -> {
            currentProgress = Math.min(100, currentProgress + 15);
            recalculateProgress();
            settingsLogArea.append("[진행도 변경] 현재 진행도: " + currentProgress + "%\n");
        });
        testProgressPanel.add(btnDecrease);
        testProgressPanel.add(btnIncrease);

        settingsLogArea = new JTextArea(8, 40);
        settingsLogArea.setEditable(false);
        settingsLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(settingsLogArea);

        chkAutoStart.addActionListener(e -> {
            boolean want = chkAutoStart.isSelected();
            if (want) {
                boolean ok = startupManager.register();
                if (ok) {
                    settingsLogArea.append("[시작 프로그램] Windows 레지스트리 등록 완료\n");
                } else {
                    chkAutoStart.setSelected(false);
                    settingsLogArea.append("[시작 프로그램] 등록 실패 (권한 문제 또는 비Windows)\n");
                    JOptionPane.showMessageDialog(this, "시작 프로그램 등록에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                boolean ok = startupManager.unregister();
                if (ok) {
                    settingsLogArea.append("[시작 프로그램] 등록 해제 완료\n");
                } else {
                    settingsLogArea.append("[시작 프로그램] 등록 해제 실패 또는 미등록 상태\n");
                }
            }
        });

        center.add(chkAutoStart);
        center.add(Box.createVerticalStrut(8));
        center.add(desc);
        center.add(Box.createVerticalStrut(12));
        center.add(testProgressPanel);
        center.add(Box.createVerticalStrut(12));
        center.add(new JLabel("시스템 설정 / 감시 로그:"));
        center.add(Box.createVerticalStrut(4));
        center.add(logScroll);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 현재 선택된 연월(currentYearMonth)과 일정을 기반으로 캘린더 화면을 다시 그립니다.
     */
    public void refreshCalendar() {
        currentMonthLabel.setText(currentYearMonth.getYear() + "년 " + currentYearMonth.getMonthValue() + "월");
        calendarGridPanel.removeAll();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // 1. 월 시작 전 빈 칸 배치
        for (int i = 0; i < dayOfWeekValue; i++) {
            JPanel emptyCell = new JPanel();
            emptyCell.setBackground(new Color(250, 251, 254));
            calendarGridPanel.add(emptyCell);
        }

        // 2. 날짜별 셀 및 등록된 일정 바 배지 렌더링
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            
            JPanel cell = new JPanel(new BorderLayout(2, 2));
            cell.setBackground(COLOR_CARD);
            cell.setBorder(new EmptyBorder(4, 5, 4, 5));

            JLabel dayNum = new JLabel(String.valueOf(day));
            dayNum.setFont(new Font("맑은 고딕", Font.BOLD, 11));
            dayNum.setForeground(COLOR_NAVY);
            cell.add(dayNum, BorderLayout.NORTH);

            JPanel itemListPanel = new JPanel();
            itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));
            itemListPanel.setOpaque(false);

            List<ScheduleItem> items = scheduleMap.getOrDefault(date, Collections.emptyList());
            items.sort(Comparator.comparingInt(ScheduleItem::getStartHour));

            for (ScheduleItem item : items) {
                JPanel badge = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        Color bg = BG_PERSONAL;
                        if ("과제".equals(item.getType())) bg = BG_TASK;
                        else if ("시험".equals(item.getType())) bg = BG_EXAM;
                        else if ("공부".equals(item.getType())) bg = BG_STUDY;
                        
                        g2.setColor(bg);
                        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 4, 4));
                        g2.dispose();
                    }
                };
                badge.setOpaque(false);
                badge.setBorder(new EmptyBorder(1, 4, 1, 4));
                badge.setMaximumSize(new Dimension(155, 18));

                String displayText = "• " + item.getTitle();
                if (item.getTimeRange() != null && !item.getTimeRange().isEmpty()) {
                    displayText += " (" + item.getTimeRange() + ")";
                }

                JLabel itemLbl = new JLabel(displayText);
                itemLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 8));
                
                if ("과제".equals(item.getType())) itemLbl.setForeground(TEXT_TASK);
                else if ("시험".equals(item.getType())) itemLbl.setForeground(TEXT_EXAM);
                else if ("공부".equals(item.getType())) itemLbl.setForeground(TEXT_STUDY);
                else itemLbl.setForeground(TEXT_PERSONAL);

                badge.add(itemLbl, BorderLayout.CENTER);

                // 마우스 호버 시 툴팁(상세정보) 설정
                String memoText = (item.getMemo() == null || item.getMemo().isBlank()) ? "메모 없음" : item.getMemo();
                String toolTipContent = "<html><body style='padding:4px;'>"
                        + "<b>[" + item.getType() + "] " + item.getTitle() + "</b><br>"
                        + "시간: " + (item.getTimeRange() == null ? "미정" : item.getTimeRange()) + "<br>"
                        + "<hr style='border:0.5px solid #cccccc;'>"
                        + "<b>메모:</b> " + memoText
                        + "</body></html>";
                badge.setToolTipText(toolTipContent);
                itemLbl.setToolTipText(toolTipContent);

                itemListPanel.add(badge);
                itemListPanel.add(Box.createVerticalStrut(2));
            }

            cell.add(itemListPanel, BorderLayout.CENTER);
            calendarGridPanel.add(cell);
        }

        // 3. 남은 주차 격자 빈 칸 채우기 (5주/6주 대응)
        int totalCells = dayOfWeekValue + daysInMonth;
        int remainingCells = (totalCells > 35 ? 42 : 35) - totalCells;
        for (int i = 0; i < remainingCells; i++) {
            JPanel emptyCell = new JPanel();
            emptyCell.setBackground(new Color(250, 251, 254));
            calendarGridPanel.add(emptyCell);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }

    private void openScheduleFormDialog() {
        ScheduleFormDialog dialog = new ScheduleFormDialog(this);
        dialog.setVisible(true);
    }

    private JButton createNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        btn.setBackground(Color.WHITE);
        btn.setForeground(COLOR_NAVY);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(65, 28));
        btn.setMargin(new Insets(2, 4, 2, 4));
        return btn;
    }

    private JPanel createLegendItem(String label, Color bg, Color fg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel box = new JLabel("■");
        box.setForeground(fg);
        JLabel txt = new JLabel(label);
        txt.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        txt.setForeground(Color.DARK_GRAY);
        p.add(box);
        p.add(txt);
        return p;
    }
}
