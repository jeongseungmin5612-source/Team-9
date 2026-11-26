package com.onioncalendar.view;

import com.onioncalendar.controller.ScheduleController;
import com.onioncalendar.controller.ScheduleListener;
import com.onioncalendar.model.Lecture;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 주간 시간표 화면 (UC2 시간표 등록). 수업 블록을 누르면 수정·삭제 창이 열린다. */
public class TimetablePanel extends JPanel implements ScheduleListener {
    private static final DayOfWeek[] DAYS = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};
    private static final String[] DAY_NAMES = {"월", "화", "수", "목", "금", "토"};
    /** 과목마다 돌아가며 쓰는 블록 색 [배경, 글자] */
    private static final Color[][] PALETTE = {
            {new Color(224, 231, 255), new Color(44, 62, 103)},
            {new Color(254, 237, 222), new Color(140, 70, 18)},
            {new Color(220, 245, 230), new Color(33, 95, 38)},
            {new Color(237, 231, 246), new Color(85, 50, 170)},
            {new Color(250, 240, 205), new Color(110, 85, 10)},
            {new Color(225, 242, 245), new Color(20, 90, 100)}};

    private final ScheduleController controller;
    private final Grid grid = new Grid();

    public TimetablePanel(ScheduleController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout());
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel card = UiStyle.card(new BorderLayout(0, 12));
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("주간 시간표");
        title.setFont(UiStyle.font(22, true));
        title.setForeground(UiStyle.TEXT);
        JLabel hint = new JLabel("매주 같은 요일·시간에 반복 · 수업 블록을 누르면 수정/삭제");
        hint.setFont(UiStyle.font(12, false));
        hint.setForeground(UiStyle.MUTED);
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(hint);
        bar.add(titles, BorderLayout.WEST);
        JButton add = UiStyle.accentButton("+ 수업 추가");
        add.addActionListener(e -> openLectureForm(null));
        JPanel addWrap = new JPanel(new BorderLayout());
        addWrap.setOpaque(false);
        addWrap.add(add, BorderLayout.NORTH);
        bar.add(addWrap, BorderLayout.EAST);
        card.add(bar, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    @Override
    public void onScheduleChanged() { refresh(); }

    public void refresh() { grid.repaint(); }

    /** l이 null이면 새 수업, 아니면 수정 */
    public void openLectureForm(Lecture l) {
        LectureFormDialog form = new LectureFormDialog(SwingUtilities.getWindowAncestor(this));
        try {
            loop(form, l);
        } finally {
            form.dispose();
        }
    }

    private void loop(LectureFormDialog form, Lecture l) {
        while (true) {
            Lecture result = form.showForm(l);
            if (form.isDeleteRequested()) {
                int ok = JOptionPane.showConfirmDialog(this, "\"" + l.getSubject() + "\" 수업을 삭제할까요?",
                        "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) controller.deleteLecture(l);
                return;
            }
            if (result == null) return;
            boolean ok = l == null ? controller.addLecture(result) : controller.updateLecture(l, result);
            if (ok) return;
            form.showWarning(controller.getLastError());   // 겹침 등: 입력값을 유지한 채 다시 연다
        }
    }

    /** 요일 × 30분 격자에 수업 블록을 직접 그린다 */
    private final class Grid extends JPanel {
        private static final int HEADER = 32, TIME_COL = 56, ROW = 22;
        private final Map<Rectangle, Lecture> hit = new LinkedHashMap<>();

        Grid() {
            setOpaque(false);
            setPreferredSize(new Dimension(800, HEADER + ROW * 26));
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    for (Map.Entry<Rectangle, Lecture> en : hit.entrySet()) {
                        if (en.getKey().contains(e.getPoint())) { openLectureForm(en.getValue()); return; }
                    }
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    boolean over = hit.keySet().stream().anyMatch(r -> r.contains(e.getPoint()));
                    setCursor(Cursor.getPredefinedCursor(over ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            List<Lecture> lectures = controller.getLectures();

            // 표시 범위: 기본 09:00~18:00, 수업이 밖에 있으면 넓힌다
            int startMin = 9 * 60, endMin = 18 * 60;
            for (Lecture l : lectures) {
                startMin = Math.min(startMin, l.getStart().getHour() * 60);
                endMin = Math.max(endMin, l.getEnd().getHour() * 60 + (l.getEnd().getMinute() > 0 ? 60 : 0));
            }
            int rows = (endMin - startMin) / 30;
            int w = getWidth(), colW = (w - TIME_COL) / DAYS.length;
            int rowH = Math.max(ROW, (getHeight() - HEADER) / Math.max(rows, 1));

            g.setFont(UiStyle.font(14, true));
            for (int c = 0; c < DAYS.length; c++) {
                g.setColor(c == 5 ? UiStyle.NAVY : UiStyle.TEXT);
                drawCentered(g, DAY_NAMES[c], TIME_COL + c * colW, 0, colW, HEADER);
            }
            g.setFont(UiStyle.font(12, false));
            for (int r = 0; r <= rows; r++) {
                int y = HEADER + r * rowH;
                g.setColor(r % 2 == 0 ? UiStyle.BORDER : new Color(244, 245, 248));
                g.drawLine(TIME_COL, y, TIME_COL + colW * DAYS.length, y);
                if (r % 2 == 0 && r < rows) {
                    int min = startMin + r * 30;
                    g.setColor(UiStyle.MUTED);
                    g.drawString(String.format("%02d:00", min / 60), 6, y + 14);
                }
            }
            g.setColor(UiStyle.BORDER);
            for (int c = 0; c <= DAYS.length; c++) {
                int x = TIME_COL + c * colW;
                g.drawLine(x, HEADER, x, HEADER + rows * rowH);
            }

            hit.clear();
            Map<String, Color[]> colors = new LinkedHashMap<>();
            for (Lecture l : lectures) {
                int c = java.util.Arrays.asList(DAYS).indexOf(l.getDay());
                if (c < 0) continue;   // 일요일 수업은 표시 안 함
                Color[] pc = colors.computeIfAbsent(l.getSubject(), k -> PALETTE[colors.size() % PALETTE.length]);
                int y1 = HEADER + (minutes(l.getStart()) - startMin) * rowH / 30;
                int y2 = HEADER + (minutes(l.getEnd()) - startMin) * rowH / 30;
                Rectangle r = new Rectangle(TIME_COL + c * colW + 3, y1 + 2, colW - 6, y2 - y1 - 4);
                hit.put(r, l);
                g.setColor(pc[0]);
                g.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g.setColor(pc[1]);
                g.setStroke(new BasicStroke(3f));
                g.drawLine(r.x + 1, r.y + 4, r.x + 1, r.y + r.height - 4);
                g.setFont(UiStyle.font(13, true));
                g.drawString(fit(g, l.getSubject(), r.width - 12), r.x + 9, r.y + 18);
                g.setFont(UiStyle.font(12, false));
                String sub = l.getStart() + "–" + l.getEnd()
                        + (l.getRoom() == null || l.getRoom().isBlank() ? "" : " · " + l.getRoom());
                if (r.height > 34) g.drawString(fit(g, sub, r.width - 12), r.x + 9, r.y + 34);
            }
            g.dispose();
        }

        private int minutes(LocalTime t) { return t.getHour() * 60 + t.getMinute(); }

        private void drawCentered(Graphics2D g, String s, int x, int y, int w, int h) {
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, x + (w - fm.stringWidth(s)) / 2, y + (h + fm.getAscent()) / 2 - 3);
        }

        private String fit(Graphics2D g, String s, int max) {
            FontMetrics fm = g.getFontMetrics();
            if (fm.stringWidth(s) <= max) return s;
            while (s.length() > 1 && fm.stringWidth(s + "…") > max) s = s.substring(0, s.length() - 1);
            return s + "…";
        }
    }
}
