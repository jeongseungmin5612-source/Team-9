package com.onioncalendar.view;

import com.onioncalendar.controller.ScheduleController;
import com.onioncalendar.model.Lecture;
import com.onioncalendar.model.Schedule;
import com.onioncalendar.model.ScheduleType;
import com.onioncalendar.model.TimeBlock;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/** 날짜 상세 창 (UC1 5단계). 그날의 수업과 일정을 시간순으로 보여준다. */
public class DayDetailDialog extends JDialog {
    private final ScheduleController controller;
    private Schedule picked;

    public DayDetailDialog(Window owner, ScheduleController controller) {
        super(owner, "날짜 상세", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
    }

    /** 창을 띄우고, 사용자가 누른 일정을 돌려준다. 그냥 닫으면 null. */
    public Schedule showDay(LocalDate date) {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(date.getMonthValue() + "월 " + date.getDayOfMonth() + "일 ("
                + date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN) + ")");
        title.setFont(UiStyle.font(20, true));
        title.setForeground(UiStyle.TEXT);
        JLabel hint = new JLabel("시간순 정렬 · 일정을 누르면 일정 상세 창이 열립니다.");
        hint.setFont(UiStyle.font(12, false));
        hint.setForeground(UiStyle.MUTED);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(title);
        head.add(Box.createVerticalStrut(4));
        head.add(hint);
        root.add(head, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        List<TimeBlock> items = controller.loadDay(date);
        if (items.isEmpty()) {
            JLabel empty = new JLabel("등록된 일정이 없습니다.");
            empty.setFont(UiStyle.font(14, false));
            empty.setForeground(UiStyle.MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(empty);
        }
        for (TimeBlock b : items) {
            list.add(row(b));
            list.add(Box.createVerticalStrut(8));
        }
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        root.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        JButton close = UiStyle.secondaryButton("닫기");
        close.addActionListener(e -> dispose());
        bottom.add(close);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(460, 520);
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return picked;
    }

    private JPanel row(TimeBlock b) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(true);
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        String time, tag, text;
        Color bg, fg;
        if (b instanceof Lecture) {
            Lecture l = (Lecture) b;
            time = l.getStart() + "–" + l.getEnd();
            tag = "수업";
            text = l.getSubject() + (l.getRoom() == null || l.getRoom().isBlank() ? "" : "  " + l.getRoom());
            bg = UiStyle.LECTURE_BG;
            fg = UiStyle.TEXT;
        } else {
            Schedule s = (Schedule) b;
            time = s.isTimed() ? s.getStart().toLocalTime() + "–" + s.getEnd().toLocalTime()
                    : s.getType() == ScheduleType.ASSIGNMENT ? "마감 " + s.getEnd().toLocalTime() : "하루 중";
            tag = s.getType().getLabel();
            text = s.isDone() ? "<html><s>" + s.getTitle() + "</s> (완료)</html>" : s.getTitle();
            Color[] c = UiStyle.typeColors(s.getType());
            bg = c[0];
            fg = c[1];
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { picked = s; dispose(); }
            });
        }
        row.setBackground(bg);
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(UiStyle.font(13, false));
        timeLabel.setForeground(fg);
        timeLabel.setPreferredSize(new Dimension(96, 20));
        JLabel tagLabel = new JLabel(tag);
        tagLabel.setFont(UiStyle.font(12, true));
        tagLabel.setForeground(fg);
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(UiStyle.font(14, false));
        textLabel.setForeground(fg);

        JPanel leftPart = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPart.setOpaque(false);
        leftPart.add(timeLabel);
        leftPart.add(tagLabel);
        row.add(leftPart, BorderLayout.WEST);
        row.add(textLabel, BorderLayout.CENTER);
        return row;
    }
}
