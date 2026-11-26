package com.onioncalendar.view;

import com.onioncalendar.controller.ScheduleController;
import com.onioncalendar.controller.ShareController;
import com.onioncalendar.model.Friend;
import com.onioncalendar.model.Schedule;
import com.onioncalendar.model.ScheduleType;
import com.onioncalendar.service.FreeTimeFinder;
import com.onioncalendar.service.FreeTimeResult;
import com.onioncalendar.service.TimeSlot;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 공통 공강시간 찾기 화면 (UC7). 진할수록 바쁜 사람이 많다. */
public class FreeTimePanel extends JPanel {
    private static final Color FREE = new Color(220, 245, 230);
    private static final Color[] BUSY = {new Color(205, 214, 236), new Color(130, 150, 200), new Color(44, 62, 103)};

    private final ShareController shareController;
    private final ScheduleController scheduleController;
    private final JPanel friendBoxes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final Map<Friend, JCheckBox> checks = new LinkedHashMap<>();
    private final JLabel weekLabel = new JLabel();
    private final JComboBox<LocalTime> fromCombo = new JComboBox<>(hours(6, 23));
    private final JComboBox<LocalTime> toCombo = new JComboBox<>(hours(7, 24));
    private final JLabel warning = new JLabel();
    private final JLabel countLabel = new JLabel();
    private final Grid gridPanel = new Grid();
    private final DefaultListModel<TimeSlot> recModel = new DefaultListModel<>();
    private final JList<TimeSlot> recList = new JList<>(recModel);
    private final JLabel recNote = new JLabel();
    private LocalDate week = LocalDate.now().with(DayOfWeek.MONDAY);
    private FreeTimeResult result;

    public FreeTimePanel(ShareController shareController, ScheduleController scheduleController, Runnable back) {
        this.shareController = shareController;
        this.scheduleController = scheduleController;
        setLayout(new BorderLayout(0, 14));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));

        // 맨 위: 돌아가기 + 제목
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton backBtn = UiStyle.secondaryButton("◀ 공유 화면");
        backBtn.addActionListener(e -> back.run());
        JLabel title = new JLabel("공통 공강시간 찾기");
        title.setFont(UiStyle.font(20, true));
        title.setForeground(UiStyle.TEXT);
        top.add(backBtn);
        top.add(title);

        // 조건 카드
        JPanel cond = UiStyle.card(new FlowLayout(FlowLayout.LEFT, 18, 4));
        friendBoxes.setOpaque(false);
        cond.add(labeled("함께 만날 친구", friendBoxes));
        JPanel weekNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        weekNav.setOpaque(false);
        JButton prev = UiStyle.squareButton("◀", "이전 주");
        JButton next = UiStyle.squareButton("▶", "다음 주");
        prev.addActionListener(e -> { week = week.minusWeeks(1); onFind(); });
        next.addActionListener(e -> { week = week.plusWeeks(1); onFind(); });
        weekLabel.setFont(UiStyle.font(15, true));
        weekNav.add(prev);
        weekNav.add(weekLabel);
        weekNav.add(next);
        cond.add(labeled("조회할 주", weekNav));
        JPanel range = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        range.setOpaque(false);
        fromCombo.setSelectedItem(LocalTime.of(9, 0));
        toCombo.setSelectedItem(LocalTime.of(22, 0));
        fromCombo.setFont(UiStyle.font(14, false));
        toCombo.setFont(UiStyle.font(14, false));
        range.add(fromCombo);
        range.add(new JLabel("~"));
        range.add(toCombo);
        cond.add(labeled("시간 범위", range));
        JButton find = UiStyle.accentButton("찾기");
        find.setPreferredSize(new Dimension(90, 44));
        find.addActionListener(e -> onFind());
        cond.add(find);

        warning.setOpaque(true);
        warning.setFont(UiStyle.font(13, false));
        warning.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        warning.setVisible(false);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        for (JPanel p : new JPanel[]{top, cond}) {
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            north.add(p);
            north.add(Box.createVerticalStrut(12));
        }
        warning.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(warning);
        add(north, BorderLayout.NORTH);

        // 격자 카드
        JPanel gridCard = UiStyle.card(new BorderLayout(0, 8));
        JPanel legend = new JPanel(new BorderLayout());
        legend.setOpaque(false);
        countLabel.setFont(UiStyle.font(13, false));
        countLabel.setForeground(new Color(0x4A5565));
        legend.add(countLabel, BorderLayout.WEST);
        legend.add(legendSwatches(), BorderLayout.EAST);
        gridCard.add(legend, BorderLayout.NORTH);
        gridCard.add(gridPanel, BorderLayout.CENTER);
        add(gridCard, BorderLayout.CENTER);

        // 추천 카드
        JPanel recCard = UiStyle.card(new BorderLayout(0, 10));
        recCard.setPreferredSize(new Dimension(280, 10));
        JPanel recHead = new JPanel();
        recHead.setOpaque(false);
        recHead.setLayout(new BoxLayout(recHead, BoxLayout.Y_AXIS));
        JLabel recTitle = new JLabel("추천 시간");
        recTitle.setFont(UiStyle.font(17, true));
        recNote.setFont(UiStyle.font(12, false));
        recNote.setForeground(UiStyle.MUTED);
        recHead.add(recTitle);
        recHead.add(Box.createVerticalStrut(4));
        recHead.add(recNote);
        recCard.add(recHead, BorderLayout.NORTH);
        recList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recList.setFixedCellHeight(42);
        recList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, slotText((TimeSlot) value), index, sel, focus);
                l.setFont(UiStyle.font(14, sel));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 4, 0, Color.WHITE),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                l.setBackground(sel ? new Color(254, 237, 222) : FREE);
                l.setForeground(sel ? new Color(140, 70, 18) : new Color(30, 80, 35));
                return l;
            }
        });
        recList.addListSelectionListener(e -> gridPanel.repaint());
        JScrollPane recScroll = new JScrollPane(recList);
        recScroll.setBorder(BorderFactory.createEmptyBorder());
        recCard.add(recScroll, BorderLayout.CENTER);
        JPanel recBottom = new JPanel();
        recBottom.setOpaque(false);
        recBottom.setLayout(new BoxLayout(recBottom, BoxLayout.Y_AXIS));
        JButton addBtn = UiStyle.primaryButton("일정으로 추가");
        addBtn.addActionListener(e -> addAsSchedule());
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        recBottom.add(addBtn);
        recCard.add(recBottom, BorderLayout.SOUTH);
        add(recCard, BorderLayout.EAST);
    }

    /** 화면에 들어올 때마다 친구 목록을 다시 읽고 계산한다 */
    public void refreshFriends() {
        Map<String, Boolean> before = new LinkedHashMap<>();
        checks.forEach((f, c) -> before.put(f.getName(), c.isSelected()));
        checks.clear();
        friendBoxes.removeAll();
        JCheckBox me = new JCheckBox("나 (항상 포함)", true);
        me.setEnabled(false);
        me.setOpaque(false);
        me.setFont(UiStyle.font(14, false));
        friendBoxes.add(me);
        for (Friend f : shareController.getFriends()) {
            JCheckBox c = new JCheckBox(f.getName(), before.getOrDefault(f.getName(), true));
            c.setOpaque(false);
            c.setFont(UiStyle.font(14, false));
            c.addActionListener(e -> onFind());
            checks.put(f, c);
            friendBoxes.add(c);
        }
        friendBoxes.revalidate();
        onFind();
    }

    /** 조건으로 계산해 화면에 그린다 */
    public void onFind() {
        weekLabel.setText(week.getMonthValue() + "월 " + week.getDayOfMonth() + "일 – "
                + week.plusDays(6).getMonthValue() + "월 " + week.plusDays(6).getDayOfMonth() + "일");
        List<Friend> picked = new ArrayList<>();
        checks.forEach((f, c) -> { if (c.isSelected()) picked.add(f); });
        LocalTime from = (LocalTime) fromCombo.getSelectedItem(), to = (LocalTime) toCombo.getSelectedItem();
        if (to.equals(LocalTime.MIDNIGHT)) to = LocalTime.of(23, 59);

        if (picked.isEmpty()) { showWarning("함께 만날 친구를 1명 이상 고르세요.", true); showResult(null); return; }
        if (!from.isBefore(to)) { showWarning("시작 시각이 끝 시각보다 빨라야 합니다.", true); showResult(null); return; }
        List<String> stale = new ArrayList<>();
        for (Friend f : picked) if (f.isOutdated(LocalDate.now())) stale.add(f.getName());
        if (!stale.isEmpty()) {
            showWarning(String.join(", ", stale) + "의 파일은 내보낸 지 " + Friend.OUTDATED_DAYS
                    + "일 이상 지났어요. 최신 파일을 다시 받아 주세요.", false);
        } else {
            warning.setVisible(false);
        }
        showResult(shareController.findCommonFreeTime(picked, week, from, to));
    }

    public void showResult(FreeTimeResult r) {
        result = r;
        recModel.clear();
        if (r == null) {
            countLabel.setText("");
            recNote.setText("");
        } else {
            countLabel.setText("선택 인원 " + r.getMemberCount() + "명 · " + FreeTimeFinder.SLOT_MINUTES + "분 단위");
            for (TimeSlot t : r.getRecommended()) recModel.addElement(t);
            if (!recModel.isEmpty()) recList.setSelectedIndex(0);
            recNote.setText(r.getRecommended().isEmpty() ? "이 주에는 추천할 시간이 없어요."
                    : r.isAllFree() ? "모두 1시간 이상 비는 시간"
                    : "<html>모두 비는 시간이 없어, 바쁜 사람이 가장 적은<br>(" + r.getRecommended().get(0).busy() + "명) 시간을 추천해요.</html>");
        }
        gridPanel.repaint();
    }

    private void addAsSchedule() {
        TimeSlot t = recList.getSelectedValue();
        if (t == null) { JOptionPane.showMessageDialog(this, "추천 시간을 먼저 고르세요."); return; }
        String title = (String) JOptionPane.showInputDialog(this, slotText(t) + "\n일정 제목을 입력하세요.",
                "일정으로 추가", JOptionPane.PLAIN_MESSAGE, null, null, "팀플 회의");
        if (title == null) return;
        Schedule s = new Schedule(ScheduleType.PERSONAL, title.trim(), t.start(), t.end());
        if (scheduleController.addSchedule(s)) {
            JOptionPane.showMessageDialog(this, "캘린더에 추가했습니다.");
            onFind();   // 내 일정이 늘었으니 다시 계산
        } else {
            JOptionPane.showMessageDialog(this, scheduleController.getLastError(), "추가 실패", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showWarning(String msg, boolean error) {
        warning.setText(msg);
        warning.setBackground(error ? UiStyle.ERROR_BG : new Color(254, 237, 222));
        warning.setForeground(error ? UiStyle.ERROR_FG : new Color(140, 70, 18));
        warning.setVisible(true);
    }

    private static String slotText(TimeSlot t) {
        return t.start().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN) + " "
                + t.start().getMonthValue() + "/" + t.start().getDayOfMonth() + " · "
                + t.start().toLocalTime() + "–" + t.end().toLocalTime()
                + (t.busy() > 0 ? " (" + t.busy() + "명 바쁨)" : "");
    }

    private static LocalTime[] hours(int from, int to) {
        List<LocalTime> out = new ArrayList<>();
        for (int h = from; h <= to; h++) out.add(h == 24 ? LocalTime.MIDNIGHT : LocalTime.of(h, 0));
        return out.toArray(new LocalTime[0]);
    }

    private static JPanel labeled(String text, Component c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(UiStyle.font(13, true));
        l.setForeground(new Color(0x4A5565));
        p.add(l, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JPanel legendSwatches() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        p.setOpaque(false);
        p.add(swatch(FREE, "모두 가능"));
        JLabel few = new JLabel("  바쁜 사람 적음");
        few.setFont(UiStyle.font(12, false));
        p.add(few);
        for (Color c : BUSY) p.add(swatch(c, null));
        JLabel many = new JLabel("많음");
        many.setFont(UiStyle.font(12, false));
        p.add(many);
        return p;
    }

    private static JLabel swatch(Color c, String text) {
        JLabel l = new JLabel(text);
        l.setFont(UiStyle.font(12, false));
        l.setIcon(new UiStyle.SwatchIcon(c, c.darker()));
        return l;
    }

    /** 요일 × 30분 색 격자 */
    private final class Grid extends JPanel {
        Grid() { setOpaque(false); setPreferredSize(new Dimension(600, 460)); }

        @Override
        protected void paintComponent(Graphics g0) {
            if (result == null) return;
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int slots = result.getSlotCount(), head = 26, timeW = 50;
            int colW = (getWidth() - timeW) / 7;
            int rowH = Math.max(8, (getHeight() - head) / Math.max(slots, 1));
            g.setFont(UiStyle.font(13, true));
            FontMetrics fm = g.getFontMetrics();
            for (int d = 0; d < 7; d++) {
                LocalDate date = result.getWeek().plusDays(d);
                String s = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN) + " " + date.getDayOfMonth();
                g.setColor(d == 6 ? UiStyle.SUNDAY : d == 5 ? UiStyle.NAVY : UiStyle.TEXT);
                g.drawString(s, timeW + d * colW + (colW - fm.stringWidth(s)) / 2, 18);
            }
            TimeSlot sel = recList.getSelectedValue();
            int n = result.getMemberCount();
            g.setFont(UiStyle.font(11, false));
            for (int s = 0; s < slots; s++) {
                int y = head + s * rowH;
                LocalTime t = result.getFrom().plusMinutes((long) s * FreeTimeFinder.SLOT_MINUTES);
                if (t.getMinute() == 0) {
                    g.setColor(UiStyle.MUTED);
                    g.drawString(t.toString(), 4, y + 11);
                }
                for (int d = 0; d < 7; d++) {
                    int c = result.getBusyCount(d, s);
                    double ratio = (double) c / n;
                    g.setColor(c == 0 ? FREE : ratio <= 0.34 ? BUSY[0] : ratio <= 0.67 ? BUSY[1] : BUSY[2]);
                    int x = timeW + d * colW + 2;
                    g.fillRect(x, y, colW - 4, rowH - 1);
                    if (sel != null) {
                        var cellStart = result.getWeek().plusDays(d).atTime(t);
                        if (!cellStart.isBefore(sel.start()) && cellStart.isBefore(sel.end())) {
                            g.setColor(new Color(194, 97, 24));
                            g.drawRect(x, y, colW - 5, rowH - 2);
                            g.drawRect(x + 1, y, colW - 7, rowH - 2);
                        }
                    }
                }
            }
            g.dispose();
        }
    }
}
