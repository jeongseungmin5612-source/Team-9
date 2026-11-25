package com.onioncalendar.view;

import com.onioncalendar.controller.ShareController;
import com.onioncalendar.model.Friend;
import com.onioncalendar.model.Lecture;
import com.onioncalendar.model.Onion;
import com.onioncalendar.model.OnionStage;
import com.onioncalendar.model.Schedule;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** 친구 정보 창 (UC8): 친구의 주간 일정표와 양파. */
public class FriendInfoDialog extends JDialog {
    private final ShareController controller;
    private final JPanel weekGrid = new JPanel(new GridLayout(1, 7, 6, 0));
    private final JLabel weekLabel = new JLabel();
    private LocalDate week = LocalDate.now().with(DayOfWeek.MONDAY);
    private Friend friend;

    public FriendInfoDialog(Window owner, ShareController controller) {
        super(owner, "친구 정보", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
    }

    public void showFriend(Friend f, OnionStage stage) {
        friend = f;
        JPanel root = new JPanel(new BorderLayout(18, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));

        // 머리: 이름, 내보낸 날짜, 주 이동
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel names = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        names.setOpaque(false);
        JLabel name = new JLabel(f.getName());
        name.setFont(UiStyle.font(22, true));
        name.setForeground(UiStyle.TEXT);
        JLabel date = new JLabel(f.getExportedAt() + " 내보냄"
                + (f.isOutdated(LocalDate.now()) ? " · " + Friend.OUTDATED_DAYS + "일 이상 지난 파일" : ""));
        date.setFont(UiStyle.font(13, false));
        date.setForeground(UiStyle.MUTED);
        names.add(name);
        names.add(date);
        head.add(names, BorderLayout.WEST);
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        nav.setOpaque(false);
        JButton prev = UiStyle.squareButton("◀", "이전 주");
        JButton next = UiStyle.squareButton("▶", "다음 주");
        prev.addActionListener(e -> { week = week.minusWeeks(1); renderWeek(); });
        next.addActionListener(e -> { week = week.plusWeeks(1); renderWeek(); });
        weekLabel.setFont(UiStyle.font(14, true));
        nav.add(prev);
        nav.add(weekLabel);
        nav.add(next);
        head.add(nav, BorderLayout.EAST);
        root.add(head, BorderLayout.NORTH);

        weekGrid.setOpaque(false);
        root.add(weekGrid, BorderLayout.CENTER);
        root.add(onionBox(f, stage), BorderLayout.EAST);

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setOpaque(false);
        JButton delete = UiStyle.dangerButton("친구 삭제");
        delete.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "\"" + f.getName() + "\"을(를) 친구 목록에서 지울까요?",
                    "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                controller.deleteFriend(f);
                dispose();
            }
        });
        JButton close = UiStyle.primaryButton("닫기");
        close.addActionListener(e -> dispose());
        buttons.add(delete, BorderLayout.WEST);
        buttons.add(close, BorderLayout.EAST);
        root.add(buttons, BorderLayout.SOUTH);

        renderWeek();
        setContentPane(root);
        setSize(1060, 600);
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private void renderWeek() {
        weekLabel.setText(week.getMonthValue() + "/" + week.getDayOfMonth() + " – "
                + week.plusDays(6).getMonthValue() + "/" + week.plusDays(6).getDayOfMonth());
        weekGrid.removeAll();
        for (int d = 0; d < 7; d++) {
            LocalDate date = week.plusDays(d);
            JPanel col = new JPanel();
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
            col.setBackground(UiStyle.BG);
            col.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
            JLabel h = new JLabel(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN) + " " + date.getDayOfMonth());
            h.setFont(UiStyle.font(13, true));
            h.setForeground(date.getDayOfWeek() == DayOfWeek.SUNDAY ? UiStyle.SUNDAY
                    : date.getDayOfWeek() == DayOfWeek.SATURDAY ? UiStyle.NAVY : UiStyle.TEXT);
            h.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(h);
            col.add(Box.createVerticalStrut(6));
            for (Item it : itemsOn(date)) {
                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBackground(it.lecture ? new Color(224, 231, 255) : Color.WHITE);
                card.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
                Color fg = it.lecture ? UiStyle.NAVY : UiStyle.TEXT;
                JLabel time = new JLabel(it.time);              // 한 줄로 (HTML 줄바꿈 없이)
                time.setFont(UiStyle.font(11, true));
                time.setForeground(fg);
                JLabel text = new JLabel(it.title);
                text.setFont(UiStyle.font(12, false));
                text.setForeground(fg);
                text.setToolTipText(it.title);
                card.add(time);
                card.add(text);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
                col.add(card);
                col.add(Box.createVerticalStrut(5));
            }
            weekGrid.add(col);
        }
        weekGrid.revalidate();
        weekGrid.repaint();
    }

    private List<Item> itemsOn(LocalDate date) {
        List<Item> out = new ArrayList<>();
        for (Lecture l : friend.getLectures()) {
            if (l.getDay() == date.getDayOfWeek()) out.add(new Item(l.getStart(), l.getStart() + "–" + l.getEnd(), l.getSubject(), true));
        }
        for (Schedule s : friend.getSchedules()) {
            if (!s.getDate().equals(date)) continue;
            String time = s.isTimed() ? s.getStart().toLocalTime() + "–" + s.getEnd().toLocalTime() : "~" + s.getEnd().toLocalTime();
            out.add(new Item(s.isTimed() ? s.getStart().toLocalTime() : LocalTime.MAX, time, s.getTitle(), false));
        }
        out.sort(Comparator.comparing(i -> i.sort));
        return out;
    }

    private JPanel onionBox(Friend f, OnionStage stage) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(new Color(0xB7C2D1), 2, 6, 4, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        box.setPreferredSize(new Dimension(240, 10));
        JTextArea art = new JTextArea(Onion.getAsciiArt(stage));   // 그림은 양파 파트(정승민)에서 채움
        art.setEditable(false);
        art.setOpaque(false);
        art.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        art.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel pct = new JLabel(f.getProgress() == Onion.NO_DATA ? "-" : f.getProgress() + "%");
        pct.setFont(UiStyle.font(28, true));
        pct.setForeground(UiStyle.TEXT);
        pct.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel st = new JLabel(stageName(stage) + " · 최근 14일 기준");
        st.setFont(UiStyle.font(13, false));
        st.setForeground(UiStyle.MUTED);
        st.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel bubble = new JLabel(Onion.getBubble(stage));
        bubble.setFont(UiStyle.font(13, true));
        bubble.setForeground(new Color(194, 97, 24));
        bubble.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(bubble);
        box.add(Box.createVerticalStrut(10));
        box.add(art);
        box.add(Box.createVerticalStrut(10));
        box.add(pct);
        box.add(st);
        box.add(Box.createVerticalGlue());
        return box;
    }

    private static String stageName(OnionStage s) {
        switch (s) {
            case FRESH: return "싱싱한 양파";
            case WILTED: return "시든 양파";
            case ROTTEN: return "썩은 양파";
            default: return "진행도 정보 없음";
        }
    }

    private static final class Item {
        final LocalTime sort;
        final String time, title;
        final boolean lecture;
        Item(LocalTime sort, String time, String title, boolean lecture) {
            this.sort = sort; this.time = time; this.title = title; this.lecture = lecture;
        }
    }
}
