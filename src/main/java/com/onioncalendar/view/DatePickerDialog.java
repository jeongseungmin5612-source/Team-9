package com.onioncalendar.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 일정 등록 시 편리한 날짜 선택을 돕는 미니 캘린더 팝업 다이얼로그 클래스입니다.
 */
public class DatePickerDialog extends JDialog {
    private LocalDate selectedDate;
    private YearMonth viewYearMonth;
    private JLabel monthTitleLabel;
    private JPanel calendarGrid;

    private static final Color COLOR_NAVY = new Color(44, 62, 103);
    private static final Color TEXT_EXAM = new Color(67, 97, 238);

    public DatePickerDialog(JDialog parent, LocalDate initialDate) {
        super(parent, "날짜 선택", true);
        this.selectedDate = initialDate;
        this.viewYearMonth = YearMonth.from(initialDate);

        setSize(300, 320);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 5));

        // 상단 달 이동 네비게이션
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_NAVY);
        header.setBorder(new EmptyBorder(6, 10, 6, 10));

        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        setNavBtnStyle(prevBtn);
        setNavBtnStyle(nextBtn);

        monthTitleLabel = new JLabel("", JLabel.CENTER);
        monthTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        monthTitleLabel.setForeground(Color.WHITE);

        prevBtn.addActionListener(e -> { viewYearMonth = viewYearMonth.minusMonths(1); renderCalendar(); });
        nextBtn.addActionListener(e -> { viewYearMonth = viewYearMonth.plusMonths(1); renderCalendar(); });

        header.add(prevBtn, BorderLayout.WEST);
        header.add(monthTitleLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 요일 헤더 및 일자 격자
        JPanel body = new JPanel(new BorderLayout(0, 2));
        body.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel weekHeader = new JPanel(new GridLayout(1, 7));
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (String d : days) {
            JLabel l = new JLabel(d, JLabel.CENTER);
            l.setFont(new Font("맑은 고딕", Font.BOLD, 11));
            if (d.equals("일")) l.setForeground(Color.RED);
            else if (d.equals("토")) l.setForeground(TEXT_EXAM);
            weekHeader.add(l);
        }
        body.add(weekHeader, BorderLayout.NORTH);

        calendarGrid = new JPanel(new GridLayout(6, 7, 2, 2));
        body.add(calendarGrid, BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        renderCalendar();
    }

    private void setNavBtnStyle(JButton btn) {
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        btn.setBackground(Color.WHITE);
        btn.setForeground(COLOR_NAVY);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(30, 22));
    }

    /**
     * 선택된 달의 일자 버튼들을 그리드에 새로 배치합니다.
     */
    private void renderCalendar() {
        monthTitleLabel.setText(viewYearMonth.getYear() + "년 " + viewYearMonth.getMonthValue() + "월");
        calendarGrid.removeAll();

        LocalDate first = viewYearMonth.atDay(1);
        int startDayOfWeek = first.getDayOfWeek().getValue() % 7;
        int daysInMonth = viewYearMonth.lengthOfMonth();

        for (int i = 0; i < startDayOfWeek; i++) {
            calendarGrid.add(new JLabel(""));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate thisDate = viewYearMonth.atDay(day);
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            dayBtn.setFocusPainted(false);
            dayBtn.setMargin(new Insets(2, 2, 2, 2));

            if (thisDate.equals(selectedDate)) {
                dayBtn.setBackground(COLOR_NAVY);
                dayBtn.setForeground(Color.WHITE);
            } else {
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setForeground(Color.BLACK);
            }

            dayBtn.addActionListener(e -> {
                selectedDate = thisDate;
                dispose();
            });

            calendarGrid.add(dayBtn);
        }

        int remaining = 42 - (startDayOfWeek + daysInMonth);
        for (int i = 0; i < remaining; i++) {
            calendarGrid.add(new JLabel(""));
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }
}
