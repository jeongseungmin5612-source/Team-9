package com.onioncalendar.view;

import com.onioncalendar.model.ScheduleItem;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

/**
 * 신규 일정을 등록하고, '시험' 유형 선택 시 공부 일정(회독, 문제풀이)을 자동 배치하는 폼 다이얼로그입니다.
 */
public class ScheduleFormDialog extends JDialog {

    private String selectedType = "시험";
    private JTextField subjectField;
    
    private JTextField dateDisplayField;
    private LocalDate currentDateSelection = LocalDate.of(2026, 11, 27);

    private JSpinner startHourSpinner;
    private JSpinner startMinSpinner;
    private JSpinner endHourSpinner;
    private JSpinner endMinSpinner;

    private JTextField memoField;
    
    // 공부 자동 할당 옵션 필드들
    private JPanel studyAllocationPanel;
    private JTextField totalReadField;  
    private JTextField totalSolveField; 
    private JSpinner studyHoursPerDaySpinner;
    private JComboBox<String> preferredTimeCombo;
    
    private JComboBox<String> periodCombo;
    private JTextField customDaysField;
    private JLabel customDaysSuffixLabel;

    private JLabel errorLabel;
    private final MainCalendarApp parentApp;

    private static final Color COLOR_NAVY = new Color(44, 62, 103);
    private static final Color COLOR_BORDER = new Color(230, 233, 240);

    public ScheduleFormDialog(MainCalendarApp parent) {
        super(parent, "일정 등록", true);
        this.parentApp = parent;
        setSize(440, 670);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createDialogHeader(), BorderLayout.NORTH);
        add(createDialogForm(), BorderLayout.CENTER);
        add(createDialogFooter(), BorderLayout.SOUTH);

        updateTypeUI();
    }

    private JPanel createDialogHeader() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        panel.setBackground(COLOR_NAVY);
        JLabel titleLabel = new JLabel("일정 등록");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel);
        return panel;
    }

    private JPanel createDialogForm() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. 유형 선택 (과제/시험/개인)
        formPanel.add(createLabel("유형"));
        formPanel.add(Box.createVerticalStrut(5));

        JPanel typeBtnPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        typeBtnPanel.setOpaque(false);

        JButton taskBtn = createTypeBtn("과제");
        JButton examBtn = createTypeBtn("시험");
        JButton personalBtn = createTypeBtn("개인");

        taskBtn.addActionListener(e -> { selectedType = "과제"; updateTypeUI(); });
        examBtn.addActionListener(e -> { selectedType = "시험"; updateTypeUI(); });
        personalBtn.addActionListener(e -> { selectedType = "개인"; updateTypeUI(); });

        typeBtnPanel.add(taskBtn);
        typeBtnPanel.add(examBtn);
        typeBtnPanel.add(personalBtn);
        formPanel.add(typeBtnPanel);
        formPanel.add(Box.createVerticalStrut(10));

        // 2. 제목 입력
        formPanel.add(createLabel("과목명 / 제목"));
        formPanel.add(Box.createVerticalStrut(4));
        subjectField = createTextField("알고리즘");
        formPanel.add(subjectField);
        formPanel.add(Box.createVerticalStrut(10));

        // 3. 날짜 및 시간 선택 카드
        JPanel dateTimeCard = new JPanel();
        dateTimeCard.setLayout(new BoxLayout(dateTimeCard, BoxLayout.Y_AXIS));
        dateTimeCard.setBackground(new Color(248, 250, 254));
        dateTimeCard.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(10, 12, 10, 12)));

        JPanel dateRow = new JPanel(new BorderLayout(8, 0));
        dateRow.setOpaque(false);
        
        JLabel dateLbl = createLabel("📅 날짜 선택");
        
        dateDisplayField = new JTextField(currentDateSelection.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateDisplayField.setEditable(false);
        dateDisplayField.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        dateDisplayField.setHorizontalAlignment(JTextField.CENTER);

        JButton openPickerBtn = new JButton("📅 달력에서 선택");
        openPickerBtn.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        openPickerBtn.setBackground(COLOR_NAVY);
        openPickerBtn.setForeground(Color.WHITE);
        openPickerBtn.setFocusPainted(false);
        openPickerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        openPickerBtn.addActionListener(e -> {
            DatePickerDialog picker = new DatePickerDialog(this, currentDateSelection);
            picker.setVisible(true);
            currentDateSelection = picker.getSelectedDate();
            dateDisplayField.setText(currentDateSelection.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        });

        JPanel rightDateGroup = new JPanel(new BorderLayout(5, 0));
        rightDateGroup.setOpaque(false);
        rightDateGroup.add(dateDisplayField, BorderLayout.CENTER);
        rightDateGroup.add(openPickerBtn, BorderLayout.EAST);

        dateRow.add(dateLbl, BorderLayout.WEST);
        dateRow.add(rightDateGroup, BorderLayout.CENTER);

        dateTimeCard.add(dateRow);
        dateTimeCard.add(Box.createVerticalStrut(8));
        dateTimeCard.add(new JSeparator());
        dateTimeCard.add(Box.createVerticalStrut(8));

        // 시작 / 종료 시간 선택 Spinner
        JPanel timeGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        timeGrid.setOpaque(false);

        JPanel startGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        startGroup.setOpaque(false);
        startGroup.add(new JLabel("시작:"));
        startHourSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 23, 1));
        startMinSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 55, 5));
        setCompactSpinner(startHourSpinner);
        setCompactSpinner(startMinSpinner);
        startGroup.add(startHourSpinner);
        startGroup.add(new JLabel(":"));
        startGroup.add(startMinSpinner);

        JPanel endGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        endGroup.setOpaque(false);
        endGroup.add(new JLabel("종료:"));
        endHourSpinner = new JSpinner(new SpinnerNumberModel(11, 0, 23, 1));
        endMinSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 55, 5));
        setCompactSpinner(endHourSpinner);
        setCompactSpinner(endMinSpinner);
        endGroup.add(endHourSpinner);
        endGroup.add(new JLabel(":"));
        endGroup.add(endMinSpinner);

        timeGrid.add(startGroup);
        timeGrid.add(endGroup);

        dateTimeCard.add(timeGrid);
        formPanel.add(dateTimeCard);
        formPanel.add(Box.createVerticalStrut(10));

        // 4. 시험 대비 자동 할당 옵션 패널
        studyAllocationPanel = new JPanel();
        studyAllocationPanel.setLayout(new BoxLayout(studyAllocationPanel, BoxLayout.Y_AXIS));
        studyAllocationPanel.setOpaque(false);
        studyAllocationPanel.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder(), "📖 공부량 자동 할당 옵션"));

        JPanel readSolveRow = new JPanel(new GridLayout(1, 2, 8, 0));
        readSolveRow.setOpaque(false);

        JPanel readCol = new JPanel(new BorderLayout(0, 4));
        readCol.setOpaque(false);
        readCol.add(createLabel("총 회독 수 (회)"), BorderLayout.NORTH);
        totalReadField = createTextField("9");
        readCol.add(totalReadField, BorderLayout.CENTER);

        JPanel solveCol = new JPanel(new BorderLayout(0, 4));
        solveCol.setOpaque(false);
        solveCol.add(createLabel("문제풀이 횟수 (회)"), BorderLayout.NORTH);
        totalSolveField = createTextField("2");
        solveCol.add(totalSolveField, BorderLayout.CENTER);

        readSolveRow.add(readCol);
        readSolveRow.add(solveCol);
        studyAllocationPanel.add(readSolveRow);
        studyAllocationPanel.add(Box.createVerticalStrut(6));

        JPanel customTimeRow = new JPanel(new GridLayout(1, 2, 8, 0));
        customTimeRow.setOpaque(false);

        JPanel hourCol = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        hourCol.setOpaque(false);
        hourCol.add(createLabel("하루 공부:"));
        studyHoursPerDaySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 8, 1));
        setCompactSpinner(studyHoursPerDaySpinner);
        hourCol.add(studyHoursPerDaySpinner);
        hourCol.add(new JLabel("시간"));

        JPanel prefCol = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        prefCol.setOpaque(false);
        prefCol.add(createLabel("선호시간:"));
        String[] prefTimes = {"아침 (09시~)", "오후 (13시~)", "저녁 (19시~)"};
        preferredTimeCombo = new JComboBox<>(prefTimes);
        preferredTimeCombo.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        prefCol.add(preferredTimeCombo);

        customTimeRow.add(hourCol);
        customTimeRow.add(prefCol);
        studyAllocationPanel.add(customTimeRow);
        studyAllocationPanel.add(Box.createVerticalStrut(6));

        studyAllocationPanel.add(createLabel("기간 설정"));
        studyAllocationPanel.add(Box.createVerticalStrut(4));

        JPanel periodRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        periodRow.setOpaque(false);

        String[] periods = {"벼락치기 (3일)", "2 주 (14일)", "3 주 (21일)", "직접 설정"};
        periodCombo = new JComboBox<>(periods);
        periodCombo.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        periodCombo.setPreferredSize(new Dimension(130, 24));

        customDaysField = new JTextField("5");
        customDaysField.setPreferredSize(new Dimension(45, 24));
        customDaysField.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        customDaysField.setHorizontalAlignment(JTextField.CENTER);
        customDaysField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(2, 4, 2, 4)));
        customDaysField.setEnabled(false);

        customDaysSuffixLabel = new JLabel("일 전부터 준비");
        customDaysSuffixLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        customDaysSuffixLabel.setForeground(Color.GRAY);

        periodCombo.addActionListener(e -> {
            boolean isCustom = (periodCombo.getSelectedIndex() == 3);
            customDaysField.setEnabled(isCustom);
            customDaysSuffixLabel.setForeground(isCustom ? COLOR_NAVY : Color.GRAY);
        });

        periodRow.add(periodCombo);
        periodRow.add(customDaysField);
        periodRow.add(customDaysSuffixLabel);

        studyAllocationPanel.add(periodRow);

        formPanel.add(studyAllocationPanel);
        formPanel.add(Box.createVerticalStrut(10));

        // 5. 메모 작성란
        formPanel.add(createLabel("메모 (선택)"));
        formPanel.add(Box.createVerticalStrut(4));
        memoField = createTextField("");
        formPanel.add(memoField);
        formPanel.add(Box.createVerticalStrut(8));

        // 오류 메세지 표시 레이블
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(new Color(253, 237, 237));
        errorPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
        errorLabel = new JLabel(" ", JLabel.LEFT);
        errorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        errorLabel.setForeground(new Color(211, 47, 47));
        errorPanel.add(errorLabel, BorderLayout.CENTER);

        formPanel.add(errorPanel);
        return formPanel;
    }

    private void setCompactSpinner(JSpinner spinner) {
        spinner.setPreferredSize(new Dimension(42, 24));
        spinner.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
    }

    private JPanel createDialogFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton cancelBtn = new JButton("취소");
        cancelBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setForeground(Color.DARK_GRAY);
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("저장");
        saveBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        saveBtn.setBackground(COLOR_NAVY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> onSaveSchedule());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        return footer;
    }

    private void updateTypeUI() {
        studyAllocationPanel.setVisible("시험".equals(selectedType));
        revalidate();
        repaint();
    }

    /**
     * 일정 시간이 겹치지 않도록 적절한 미사용 시간대를 검색합니다.
     */
    private String findNonOverlappingTime(LocalDate date, int requiredHours, int startPreferenceHour) {
        List<ScheduleItem> existingItems = parentApp.getScheduleMap().getOrDefault(date, Collections.emptyList());
        
        int candidateStartMin = startPreferenceHour * 60;
        int durationMin = requiredHours * 60;

        while (candidateStartMin + durationMin <= 22 * 60) {
            int candidateEndMin = candidateStartMin + durationMin;
            boolean isOverlap = false;

            for (ScheduleItem item : existingItems) {
                int itemStart = item.getStartMinuteOfDay();
                int itemEnd = item.getEndMinuteOfDay();

                if (candidateStartMin < itemEnd && candidateEndMin > itemStart) {
                    isOverlap = true;
                    candidateStartMin = Math.max(candidateStartMin + 60, itemEnd);
                    break;
                }
            }

            if (!isOverlap) {
                int startH = candidateStartMin / 60;
                int startM = candidateStartMin % 60;
                int endH = candidateEndMin / 60;
                int endM = candidateEndMin % 60;
                return String.format("%02d:%02d~%02d:%02d", startH, startM, endH, endM);
            }
        }

        return String.format("%02d:00~%02d:00", startPreferenceHour, startPreferenceHour + requiredHours);
    }

    /**
     * 일정 저장 처리 및 자동 분할 알고리즘을 수행합니다.
     */
    private void onSaveSchedule() {
        String subject = subjectField.getText().trim();
        if (subject.isEmpty()) {
            errorLabel.setText("검증 실패: 과목명/제목을 입력하세요.");
            return;
        }

        LocalDate examDate = currentDateSelection;

        int sHour = (Integer) startHourSpinner.getValue();
        int sMin = (Integer) startMinSpinner.getValue();
        int eHour = (Integer) endHourSpinner.getValue();
        int eMin = (Integer) endMinSpinner.getValue();

        String timeStr = String.format("%02d:%02d~%02d:%02d", sHour, sMin, eHour, eMin);

        // 되돌리기를 위한 현재 상태 스냅샷 저장
        parentApp.saveSnapshotForUndo();

        // 메인 일정 추가
        parentApp.addSchedule(examDate, new ScheduleItem(subject + ("시험".equals(selectedType) ? " 시험" : ""), selectedType, timeStr, memoField.getText().trim()));

        // '시험' 선택 시 공부 일정 자동 배치 알고리즘 동작
        if ("시험".equals(selectedType)) {
            try {
                int totalReads = Integer.parseInt(totalReadField.getText().trim());
                int totalSolves = Integer.parseInt(totalSolveField.getText().trim());
                int studyHours = (Integer) studyHoursPerDaySpinner.getValue();

                int prefStartHour = 9;
                int prefIdx = preferredTimeCombo.getSelectedIndex();
                if (prefIdx == 1) prefStartHour = 13;
                else if (prefIdx == 2) prefStartHour = 19;

                int periodDays = 5;
                int comboIdx = periodCombo.getSelectedIndex();
                if (comboIdx == 0) periodDays = 3;
                else if (comboIdx == 1) periodDays = 14;
                else if (comboIdx == 2) periodDays = 21;
                else if (comboIdx == 3) {
                    String inputVal = customDaysField.getText().trim();
                    if (inputVal.isEmpty()) {
                        errorLabel.setText("검증 실패: 직접 설정할 일수를 입력하세요.");
                        return;
                    }
                    periodDays = Integer.parseInt(inputVal);
                    if (periodDays <= 0) {
                        errorLabel.setText("검증 실패: 일수는 1 이상의 양수를 입력하세요.");
                        return;
                    }
                }

                LocalDate startDate = examDate.minusDays(periodDays);
                long totalDaysAvailable = ChronoUnit.DAYS.between(startDate, examDate);

                if (totalDaysAvailable > 0) {
                    int chunkSize = 3; 
                    int taskCount = (int) Math.ceil((double) totalReads / chunkSize);
                    double step = (double) (totalDaysAvailable - 1) / Math.max(1, taskCount);

                    // 1) 회독 일정 며칠 간격 자동 할당
                    int currentRead = 1;
                    for (int i = 0; i < taskCount && currentRead <= totalReads; i++) {
                        long dayOffset = Math.round(i * step);
                        LocalDate targetDate = startDate.plusDays(dayOffset);

                        if (targetDate.isEqual(examDate) || targetDate.isAfter(examDate)) {
                            targetDate = examDate.minusDays(1);
                        }

                        int nextRead = Math.min(totalReads, currentRead + chunkSize - 1);
                        String readTitle = subject + " " + currentRead + "~" + nextRead + "회독";
                        
                        String safeTimeRange = findNonOverlappingTime(targetDate, studyHours, prefStartHour);
                        parentApp.addSchedule(targetDate, new ScheduleItem(readTitle, "공부", safeTimeRange, "시험 대비 자동 배치 일정"));

                        currentRead = nextRead + 1;
                    }

                    // 2) 시험 전날 문제풀이 일정 할당
                    LocalDate solveDate = examDate.minusDays(1);
                    String safeSolveTime = findNonOverlappingTime(solveDate, Math.max(2, studyHours), 14);
                    String solveTitle = subject + " 문제풀이 1~" + totalSolves + "회";
                    parentApp.addSchedule(solveDate, new ScheduleItem(solveTitle, "공부", safeSolveTime, "시험 전날 문제풀이 일정"));
                }
            } catch (NumberFormatException nfe) {
                errorLabel.setText("검증 실패: 회독 수, 문제풀이, 직접 설정 일수는 숫자로만 입력하세요.");
                return;
            }
        }

        parentApp.refreshCalendar();
        parentApp.recalculateProgress();
        dispose();
    }

    private JButton createTypeBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        btn.setBackground(Color.WHITE);
        btn.setForeground(COLOR_NAVY);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        lbl.setForeground(COLOR_NAVY);
        return lbl;
    }

    private JTextField createTextField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        tf.setPreferredSize(new Dimension(200, 26));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(2, 6, 2, 6)));
        return tf;
    }
}
