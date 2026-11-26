package com.onioncalendar.view;

import com.onioncalendar.controller.ScheduleController;
import com.onioncalendar.model.Schedule;
import com.onioncalendar.model.ScheduleType;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.format.TextStyle;
import java.util.Locale;

/** 일정 상세 창 (UC4). 완료 체크, 수정, 삭제. */
public class ScheduleDetailDialog extends JDialog {
    private final ScheduleController controller;
    private final JCheckBox doneCheck = new JCheckBox("완료");

    public ScheduleDetailDialog(Window owner, ScheduleController controller) {
        super(owner, "일정 상세", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
    }

    public void showSchedule(Schedule s) {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        Color[] c = UiStyle.typeColors(s.getType());
        JLabel tag = new JLabel(s.getType().getLabel());
        tag.setFont(UiStyle.font(13, true));
        tag.setOpaque(true);
        tag.setBackground(c[0]);
        tag.setForeground(c[1]);
        tag.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(tag);
        body.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel(s.getTitle());
        title.setFont(UiStyle.font(20, true));
        title.setForeground(UiStyle.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(title);
        body.add(Box.createVerticalStrut(12));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        addInfo(info, "날짜", s.getDate().getYear() + "년 " + s.getDate().getMonthValue() + "월 "
                + s.getDate().getDayOfMonth() + "일 ("
                + s.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN) + ")");
        if (s.isTimed()) addInfo(info, "시간", s.getStart().toLocalTime() + " ~ " + s.getEnd().toLocalTime());
        else if (s.getType() == ScheduleType.ASSIGNMENT) addInfo(info, "마감", s.getEnd().toLocalTime().toString());
        body.add(info);
        body.add(Box.createVerticalStrut(14));

        doneCheck.setSelected(s.isDone());
        doneCheck.setFont(UiStyle.font(15, false));
        doneCheck.setOpaque(false);
        doneCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        doneCheck.addActionListener(e -> controller.setDone(s, doneCheck.isSelected()));
        body.add(doneCheck);
        body.add(Box.createVerticalStrut(6));
        JLabel note = new JLabel("<html>완료를 바꾸면 양파 진행도에 바로 반영됩니다."
                + (s.getType() == ScheduleType.EXAM ? "<br>시험을 삭제하면 아직 안 한 공부 일정도 함께 지워집니다." : "")
                + "</html>");
        note.setFont(UiStyle.font(12, false));
        note.setForeground(UiStyle.MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(note);
        root.add(body, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setOpaque(false);
        JButton delete = UiStyle.dangerButton("삭제");
        delete.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "\"" + s.getTitle() + "\" 일정을 삭제할까요?",
                    "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                controller.deleteSchedule(s);
                dispose();
            }
        });
        buttons.add(delete, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton edit = UiStyle.secondaryButton("수정");
        edit.setEnabled(s.getType() != ScheduleType.STUDY);
        edit.setToolTipText(s.getType() == ScheduleType.STUDY ? "공부 일정은 시험을 수정해 다시 배치합니다" : null);
        edit.addActionListener(e -> {
            ScheduleFormDialog form = new ScheduleFormDialog(this);
            Schedule updated = form.showForm(s);
            if (updated == null) return;
            if (controller.updateSchedule(updated, form.getGoal())) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, controller.getLastError(), "수정 실패", JOptionPane.WARNING_MESSAGE);
            }
        });
        JButton close = UiStyle.primaryButton("닫기");
        close.addActionListener(e -> dispose());
        right.add(edit);
        right.add(close);
        buttons.add(right, BorderLayout.EAST);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setSize(Math.max(getWidth(), 420), getHeight());
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private void addInfo(JPanel p, String k, String v) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel kl = new JLabel(k);
        kl.setFont(UiStyle.font(14, false));
        kl.setForeground(UiStyle.MUTED);
        kl.setPreferredSize(new Dimension(56, 22));
        JLabel vl = new JLabel(v);
        vl.setFont(UiStyle.font(14, false));
        vl.setForeground(UiStyle.TEXT);
        row.add(kl);
        row.add(vl);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        p.add(row);
    }
}
