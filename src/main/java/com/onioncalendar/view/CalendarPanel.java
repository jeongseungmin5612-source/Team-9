package com.onioncalendar.view;

import com.onioncalendar.controller.ScheduleController;
import com.onioncalendar.controller.ScheduleListener;
import com.onioncalendar.model.Schedule;
import com.onioncalendar.model.ScheduleType;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 월간 캘린더 화면 (UC1 캘린더 조회). 날짜 칸을 누르면 날짜 상세 창이 열린다. */
public class CalendarPanel extends JPanel implements ScheduleListener {
    private static final int MAX_CHIPS = 3;

    private final ScheduleController controller;
    private YearMonth currentMonth = YearMonth.now();
    private final JLabel monthLabel = new JLabel();
    private final JPanel grid = new JPanel(new GridLayout(0, 7));   // 행 수는 달마다 5~6줄 자동

    public CalendarPanel(ScheduleController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout());
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel card = UiStyle.card(new BorderLayout(0, 12));
        card.add(buildToolbar(), BorderLayout.NORTH);

        JPanel table = new JPanel(new BorderLayout());
        table.setOpaque(false);
        table.add(buildWeekdayHeader(), BorderLayout.NORTH);
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, UiStyle.BORDER));
        table.add(grid, BorderLayout.CENTER);
        card.add(table, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        showMonth(currentMonth);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nav.setOpaque(false);
        JButton prev = UiStyle.squareButton("◀", "이전 달");
        JButton next = UiStyle.squareButton("▶", "다음 달");
        prev.addActionListener(e -> showMonth(currentMonth.minusMonths(1)));
        next.addActionListener(e -> showMonth(currentMonth.plusMonths(1)));
        monthLabel.setFont(UiStyle.font(22, true));
        monthLabel.setForeground(UiStyle.TEXT);
        monthLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        nav.add(prev);
        nav.add(monthLabel);
        nav.add(next);
        bar.add(nav, BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        legend.setOpaque(false);
        for (ScheduleType t : ScheduleType.values()) legend.add(UiStyle.legend(t));
        JLabel doneNote = new JLabel("<html><s>취소선</s> = 완료</html>");
        doneNote.setFont(UiStyle.font(12, false));
        doneNote.setForeground(new Color(0x4A5565));
        legend.add(doneNote);
        bar.add(legend, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildWeekdayHeader() {
        JPanel row = new JPanel(new GridLayout(1, 7));
        row.setBackground(UiStyle.BG);
        row.setOpaque(true);
        row.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, UiStyle.BORDER));
        String[] names = {"일", "월", "화", "수", "목", "금", "토"};
        for (int i = 0; i < 7; i++) {
            JLabel l = new JLabel(names[i], JLabel.CENTER);
            l.setFont(UiStyle.font(13, true));
            l.setForeground(i == 0 ? UiStyle.SUNDAY : i == 6 ? UiStyle.NAVY : UiStyle.TEXT);
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, UiStyle.BORDER),
                    BorderFactory.createEmptyBorder(8, 0, 8, 0)));
            row.add(l);
        }
        return row;
    }

    @Override
    public void onScheduleChanged() { refresh(); }

    public void refresh() { showMonth(currentMonth); }

    public void showMonth(YearMonth month) {
        currentMonth = month;
        monthLabel.setText(month.getYear() + "년 " + month.getMonthValue() + "월");

        Map<LocalDate, List<Schedule>> byDate = new HashMap<>();
        // 앞뒤 달 칸에도 일정이 보이도록 3달치를 읽는다
        for (YearMonth m : new YearMonth[]{month.minusMonths(1), month, month.plusMonths(1)}) {
            for (Schedule s : controller.loadMonth(m)) {
                byDate.computeIfAbsent(s.getDate(), k -> new ArrayList<>()).add(s);
            }
        }

        grid.removeAll();
        LocalDate first = month.atDay(1);
        int lead = first.getDayOfWeek().getValue() % 7;              // 일요일 시작
        int cells = (lead + month.lengthOfMonth() + 6) / 7 * 7;      // 5줄 또는 6줄
        LocalDate start = first.minusDays(lead);
        for (int i = 0; i < cells; i++) {
            LocalDate d = start.plusDays(i);
            grid.add(new DayCell(d, YearMonth.from(d).equals(month), byDate.getOrDefault(d, List.of())));
        }
        grid.revalidate();
        grid.repaint();
    }

    /** 날짜 상세 창을 열고, 거기서 일정을 고르면 일정 상세 창을 연다 */
    public void openDayDetail(LocalDate date) {
        Schedule picked = new DayDetailDialog(owner(), controller).showDay(date);
        if (picked != null) openScheduleDetail(picked);
    }

    public void openScheduleDetail(Schedule s) {
        new ScheduleDetailDialog(owner(), controller).showSchedule(s);
    }

    /** 상단 [+ 일정 추가] → 새 일정 등록 */
    public void openNewSchedule() {
        ScheduleFormDialog form = new ScheduleFormDialog(owner());
        Schedule s = form.showForm(null);
        if (s == null) return;
        boolean ok = s.getType() == ScheduleType.EXAM
                ? controller.addExam(s, form.getGoal())
                : controller.addSchedule(s);
        if (!ok) {
            JOptionPane.showMessageDialog(this, controller.getLastError(), "저장 실패", JOptionPane.WARNING_MESSAGE);
        } else {
            showMonth(YearMonth.from(s.getDate()));
        }
    }

    private Window owner() { return SwingUtilities.getWindowAncestor(this); }

    /** 날짜 한 칸 */
    private final class DayCell extends JPanel {
        DayCell(LocalDate date, boolean inMonth, List<Schedule> items) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            boolean today = date.equals(LocalDate.now());
            setBackground(today ? UiStyle.TODAY_BG : inMonth ? Color.WHITE : UiStyle.OTHER_MONTH_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, UiStyle.BORDER),
                    BorderFactory.createEmptyBorder(5, 6, 5, 6)));
            setPreferredSize(new Dimension(120, 96));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleName(date + " 상세 보기");

            JLabel num = new JLabel(String.valueOf(date.getDayOfMonth()));
            num.setFont(UiStyle.font(13, today));
            DayOfWeek dow = date.getDayOfWeek();
            num.setForeground(!inMonth ? new Color(0x8A93A2)
                    : dow == DayOfWeek.SUNDAY ? UiStyle.SUNDAY
                    : dow == DayOfWeek.SATURDAY ? UiStyle.NAVY : UiStyle.TEXT);
            num.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(num);
            add(Box.createVerticalStrut(3));

            for (int i = 0; i < items.size() && i < MAX_CHIPS; i++) {
                add(chip(items.get(i)));
                add(Box.createVerticalStrut(2));
            }
            if (items.size() > MAX_CHIPS) {
                JLabel more = new JLabel("+" + (items.size() - MAX_CHIPS) + "개 더");
                more.setFont(UiStyle.font(11, false));
                more.setForeground(UiStyle.MUTED);
                more.setAlignmentX(Component.LEFT_ALIGNMENT);
                add(more);
            }

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { openDayDetail(date); }
            });
        }

        private JLabel chip(Schedule s) {
            Color[] c = UiStyle.typeColors(s.getType());
            String title = s.getType() == ScheduleType.EXAM && !s.getTitle().endsWith("시험")
                    ? s.getTitle() + " 시험" : s.getTitle();
            String text = escape(title);
            JLabel l = new JLabel(s.isDone() ? "<html><s>" + text + "</s></html>" : text);
            l.setFont(UiStyle.font(12, false));
            l.setOpaque(true);
            l.setBackground(c[0]);
            l.setForeground(c[1]);
            l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height));
            l.setToolTipText(s.getTitle());
            return l;
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
