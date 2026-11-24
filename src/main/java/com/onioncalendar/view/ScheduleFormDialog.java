package com.onioncalendar.view;

import com.onioncalendar.model.Schedule;
import com.onioncalendar.model.ScheduleType;
import com.onioncalendar.service.StudyGoal;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * ※ 임시 구현 — 일정 등록은 정현국이 구현해 이 클래스를 교체한다.
 * 교체할 때 지켜야 할 것 (CalendarPanel, ScheduleDetailDialog가 이렇게 부른다):
 *   - new ScheduleFormDialog(Window owner)
 *   - Schedule showForm(Schedule s): s가 null이면 새 일정, 아니면 수정. 취소하면 null. 수정이면 같은 id.
 *   - StudyGoal getGoal(): 시험일 때 공부량 입력값, 아니면 null.
 *
 * 일정 등록·수정 창 (UC3, UC4).
 * 유형이 시험이면 공부량 자동 할당 옵션(회독 수·문제풀이 횟수·기간)이 나타난다.
 */
public class ScheduleFormDialog extends JDialog {
    private static final String[] PERIODS = {"벼락치기 (3일)", "2주", "3주", "직접 설정"};
    private static final int[] PERIOD_DAYS = {3, 14, 21, 0};

    private final JComboBox<ScheduleType> typeCombo =
            new JComboBox<>(new ScheduleType[]{ScheduleType.ASSIGNMENT, ScheduleType.EXAM, ScheduleType.PERSONAL});
    private final JTextField titleField = field();
    private final JTextField dateField = field();
    private final JTextField startField = field();
    private final JTextField endField = field();
    private final JTextField readingField = field();
    private final JTextField solvingField = field();
    private final JComboBox<String> periodCombo = new JComboBox<>(PERIODS);
    private final JTextField customStartField = field();

    private final JLabel titleLabel = label("제목");
    private final JLabel dateLabel = label("날짜");
    private final JLabel startLabel = label("시작 시간 (HH:mm)");
    private final JLabel endLabel = label("종료 시간 (HH:mm)");
    private final JLabel examHint = new JLabel();
    private final JPanel startBox = labeled(startLabel, startField);
    private final JPanel examBox = new JPanel();
    private final JPanel customBox = labeled(label("공부 시작 날짜 (yyyy-MM-dd)"), customStartField);
    private final JLabel errorLabel = new JLabel();

    private Schedule editing;
    private Schedule result;
    private StudyGoal goal;

    public ScheduleFormDialog(Window owner) {
        super(owner, "일정 등록", ModalityType.APPLICATION_MODAL);
        typeCombo.setFont(UiStyle.font(14, false));
        periodCombo.setFont(UiStyle.font(14, false));
        typeCombo.addActionListener(e -> updateForType());
        periodCombo.addActionListener(e -> { customBox.setVisible(periodCombo.getSelectedIndex() == 3); repack(); });
    }

    /**
     * 창을 띄운다. s가 null이면 새 일정, 아니면 그 일정을 수정한다.
     * 저장하면 입력값으로 만든 Schedule(수정이면 같은 id), 취소하면 null.
     */
    public Schedule showForm(Schedule s) {
        editing = s;
        setTitle(s == null ? "일정 등록" : "일정 수정");
        fill(s);
        setContentPane(build());
        updateForType();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return result;
    }

    /** 시험일 때 입력한 공부량 옵션. 시험이 아니거나 비워 두었으면 null. */
    public StudyGoal getGoal() { return goal; }

    private void fill(Schedule s) {
        LocalDate today = LocalDate.now();
        if (s == null) {
            typeCombo.setSelectedItem(ScheduleType.ASSIGNMENT);
            dateField.setText(today.toString());
            endField.setText("23:59");
            readingField.setText("3");
            solvingField.setText("1");
            customStartField.setText(today.toString());
            return;
        }
        typeCombo.setSelectedItem(s.getType());
        typeCombo.setEnabled(false);
        titleField.setText(s.getTitle());
        dateField.setText(s.getDate().toString());
        startField.setText(s.getStart() == null ? "" : s.getStart().toLocalTime().toString());
        endField.setText(s.getEnd() == null || (!s.isTimed() && s.getType() != ScheduleType.ASSIGNMENT)
                ? "" : s.getEnd().toLocalTime().toString());
        customStartField.setText(today.toString());
    }

    private JPanel build() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        add(form, labeled(label("유형"), typeCombo));
        add(form, labeled(titleLabel, titleField));
        add(form, row(labeled(dateLabel, dateField), Box.createGlue()));
        add(form, row(startBox, labeled(endLabel, endField)));

        examBox.setLayout(new BoxLayout(examBox, BoxLayout.Y_AXIS));
        examBox.setBackground(new Color(0xF8F3FC));
        examBox.setOpaque(true);
        examBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0xC9B6DD)),
                        "공부량 자동 할당 옵션", 0, 0, UiStyle.font(13, true), new Color(0x4B2A6B)),
                BorderFactory.createEmptyBorder(6, 10, 10, 10)));
        examBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(examBox, row(labeled(label("총 회독 수 (1~20)"), readingField),
                labeled(label("문제풀이 횟수 (0~10)"), solvingField)));
        add(examBox, labeled(label("기간 설정"), periodCombo));
        add(examBox, customBox);
        examHint.setFont(UiStyle.font(12, false));
        examHint.setForeground(new Color(0x4B2A6B));
        examHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        examBox.add(examHint);
        customBox.setVisible(false);
        form.add(examBox);
        form.add(Box.createVerticalStrut(10));

        errorLabel.setOpaque(true);
        errorLabel.setBackground(UiStyle.ERROR_BG);
        errorLabel.setForeground(UiStyle.ERROR_FG);
        errorLabel.setFont(UiStyle.font(13, false));
        errorLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setVisible(false);                 // 오류가 있을 때만 보인다
        form.add(errorLabel);
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = UiStyle.secondaryButton("취소");
        cancel.addActionListener(e -> { result = null; goal = null; dispose(); });
        JButton save = UiStyle.primaryButton("저장");
        save.addActionListener(e -> onSave());
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        return root;
    }

    private void updateForType() {
        ScheduleType t = (ScheduleType) typeCombo.getSelectedItem();
        boolean exam = t == ScheduleType.EXAM, hw = t == ScheduleType.ASSIGNMENT;
        titleLabel.setText(exam ? "과목명" : "제목");
        dateLabel.setText((exam ? "시험 날짜" : hw ? "마감 날짜" : "날짜") + " (yyyy-MM-dd)");
        startBox.setVisible(!hw);
        endLabel.setText(hw ? "마감 시각 (HH:mm)" : exam ? "종료 시간 (HH:mm)" : "종료 시간 (HH:mm, 비우면 하루 일정)");
        startLabel.setText(exam ? "시작 시간 (HH:mm)" : "시작 시간 (HH:mm, 선택)");
        examBox.setVisible(exam);
        if (editing != null && exam) {
            readingField.setText("");
            solvingField.setText("");
            examHint.setText("<html>비워 두면 기존 공부 일정을 그대로 둡니다.<br>입력하면 아직 안 한 공부 일정을 지우고 다시 배치합니다.</html>");
        } else {
            examHint.setText("<html>저장하면 시험 전날까지 회독은 앞쪽, 문제풀이는<br>시험일 가까이에 공부 일정으로 배치됩니다.</html>");
        }
        if (editing == null) {                       // 새 일정: 유형에 맞는 기본 시간
            if (hw) { startField.setText(""); endField.setText("23:59"); }
            else if ("23:59".equals(endField.getText())) endField.setText("");
        }
        errorLabel.setVisible(false);
        repack();
    }

    /** 내용에 맞게 높이를 맞추되 너비는 460 이상 유지 */
    private void repack() {
        pack();
        setSize(Math.max(getWidth(), 460), getHeight());
    }

    private void onSave() {
        try {
            ScheduleType type = (ScheduleType) typeCombo.getSelectedItem();
            String title = titleField.getText().trim();
            if (title.isEmpty()) throw new FormError(type == ScheduleType.EXAM ? "과목명을 입력하세요." : "제목을 입력하세요.");
            LocalDate date = parseDate(dateField.getText(), "날짜");
            LocalTime start = parseTimeOrNull(startField.getText(), "시작 시간");
            LocalTime end = parseTimeOrNull(endField.getText(), "종료 시간");

            LocalDateTime s, e;
            if (type == ScheduleType.ASSIGNMENT) {
                if (end == null) throw new FormError("마감 시각을 입력하세요.");
                s = null;
                e = date.atTime(end);
            } else if (type == ScheduleType.EXAM) {
                if (start == null || end == null) throw new FormError("시험 시작·종료 시간을 입력하세요.");
                if (editing == null && date.isBefore(LocalDate.now())) throw new FormError("시험 날짜가 오늘보다 이전입니다.");
                s = date.atTime(start);
                e = date.atTime(end);
            } else {
                if ((start == null) != (end == null)) throw new FormError("시작·종료 시간을 둘 다 입력하거나 둘 다 비워 두세요.");
                s = start == null ? null : date.atTime(start);
                e = end == null ? date.atTime(23, 59) : date.atTime(end);
            }
            if (s != null && !s.isBefore(e)) throw new FormError("시작 시간이 종료 시간보다 빨라야 합니다.");

            goal = type == ScheduleType.EXAM ? readGoal(date) : null;
            result = editing == null
                    ? new Schedule(type, title, s, e)
                    : new Schedule(editing.getId(), type, title, s, e, editing.isDone(), editing.getExamId());
            dispose();
        } catch (FormError ex) {
            errorLabel.setText(ex.getMessage());
            errorLabel.setVisible(true);
            repack();
        }
    }

    /** 공부량 입력값 → StudyGoal. 수정 중에 비워 두었으면 null. */
    private StudyGoal readGoal(LocalDate examDate) {
        if (editing != null && readingField.getText().isBlank() && solvingField.getText().isBlank()) return null;
        int readings = parseInt(readingField.getText(), "총 회독 수", 1, 20);
        int solvings = parseInt(solvingField.getText(), "문제풀이 횟수", 0, 10);
        int idx = periodCombo.getSelectedIndex();
        LocalDate startDate = idx == 3
                ? parseDate(customStartField.getText(), "공부 시작 날짜")
                : examDate.minusDays(PERIOD_DAYS[idx]);
        if (!startDate.isBefore(examDate)) throw new FormError("공부 시작 날짜는 시험 날짜보다 앞이어야 합니다.");
        if (!LocalDate.now().isBefore(examDate)) throw new FormError("오늘이 시험 날이라 공부 일정을 배치할 수 없습니다.");
        return new StudyGoal(readings, solvings, startDate);
    }

    private static LocalDate parseDate(String text, String name) {
        try { return LocalDate.parse(text.trim()); }
        catch (DateTimeParseException ex) { throw new FormError(name + "를 2026-11-24 형식으로 입력하세요."); }
    }

    private static LocalTime parseTimeOrNull(String text, String name) {
        if (text.isBlank()) return null;
        try { return LocalTime.parse(text.trim().length() == 4 ? "0" + text.trim() : text.trim()); }
        catch (DateTimeParseException ex) { throw new FormError(name + "을 09:30 형식으로 입력하세요."); }
    }

    private static int parseInt(String text, String name, int min, int max) {
        try {
            int v = Integer.parseInt(text.trim());
            if (v < min || v > max) throw new FormError(name + "는 " + min + "~" + max + " 사이로 입력하세요.");
            return v;
        } catch (NumberFormatException ex) {
            throw new FormError(name + "를 숫자로 입력하세요.");
        }
    }

    // ---- 레이아웃 도우미 ----

    private static JTextField field() {
        JTextField f = new JTextField();
        f.setFont(UiStyle.font(14, false));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB7C2D1)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UiStyle.font(13, true));
        l.setForeground(new Color(0x4A5565));
        return l;
    }

    private static JPanel labeled(JLabel l, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        l.setLabelFor(c);
        p.add(l, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private static JPanel row(Component a, Component b) {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
        p.setOpaque(false);
        p.add(a);
        p.add(b);
        return p;
    }

    private static void add(JPanel parent, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        parent.add(c);
        parent.add(Box.createVerticalStrut(10));
    }

    /** 입력 오류 (화면 경고 문구로 표시) */
    private static final class FormError extends RuntimeException {
        FormError(String msg) { super(msg); }
    }
}
