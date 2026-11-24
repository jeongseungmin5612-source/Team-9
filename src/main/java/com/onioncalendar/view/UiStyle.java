package com.onioncalendar.view;

import com.onioncalendar.model.ScheduleType;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.List;

/** 화면 공통 색상·글꼴·컴포넌트 (GUI 초안의 스타일). */
public final class UiStyle {
    public static final Color NAVY = new Color(44, 62, 103);
    public static final Color ORANGE = new Color(194, 97, 24);       // 과제 글자색과 같은 주황
    public static final Color BG = new Color(243, 245, 249);
    public static final Color BORDER = new Color(230, 233, 240);
    public static final Color TEXT = new Color(0x1F2733);
    public static final Color MUTED = new Color(0x5A6475);
    public static final Color SUNDAY = new Color(0xB3261E);
    public static final Color TODAY_BG = new Color(235, 239, 250);
    public static final Color OTHER_MONTH_BG = new Color(0xF7F8FA);
    public static final Color ERROR_BG = new Color(0xFBEBEB);
    public static final Color ERROR_FG = new Color(0x8A1F1F);
    public static final Color LECTURE_BG = new Color(0xF2F4F7);

    private static final String FAMILY = pickFamily();

    private UiStyle() { }

    private static String pickFamily() {
        List<String> installed = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String f : new String[]{"맑은 고딕", "Malgun Gothic", "Noto Sans CJK KR", "Apple SD Gothic Neo"}) {
            if (installed.contains(f)) return f;
        }
        return Font.DIALOG;
    }

    public static Font font(int size, boolean bold) {
        return new Font(FAMILY, bold ? Font.BOLD : Font.PLAIN, size);
    }

    /** 유형별 [배경, 글자, 테두리] 색 */
    public static Color[] typeColors(ScheduleType t) {
        switch (t) {
            // 팀 공통 팔레트 (현국 MainCalendarApp 기준): [배경, 글자, 테두리=글자]
            case ASSIGNMENT: return new Color[]{new Color(254, 237, 222), new Color(194, 97, 24), new Color(194, 97, 24)};
            case EXAM: return new Color[]{new Color(224, 231, 255), new Color(67, 97, 238), new Color(67, 97, 238)};
            case STUDY: return new Color[]{new Color(237, 231, 246), new Color(124, 77, 255), new Color(124, 77, 255)};
            default: return new Color[]{new Color(220, 245, 230), new Color(46, 125, 50), new Color(46, 125, 50)};
        }
    }

    public static JButton primaryButton(String text) {
        return button(text, NAVY, Color.WHITE, NAVY);
    }

    public static JButton accentButton(String text) {
        return button(text, ORANGE, Color.WHITE, ORANGE);
    }

    public static JButton secondaryButton(String text) {
        return button(text, Color.WHITE, NAVY, NAVY);
    }

    public static JButton dangerButton(String text) {
        return button(text, Color.WHITE, new Color(0xA12A2A), new Color(0xC9A0A0));
    }

    /** L&F와 상관없이 색이 그대로 보이도록 직접 그리는 둥근 버튼 */
    public static JButton button(String text, Color bg, Color fg, Color border) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed() ? bg.darker() : getModel().isRollover() ? blend(bg) : bg;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(font(14, true));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** 정사각형 아이콘 버튼 (◀ ▶) — 글자가 잘려 "..."로 보이지 않게 크기를 고정 */
    public static JButton squareButton(String text, String tooltip) {
        JButton b = button(text, Color.WHITE, NAVY, BORDER);
        b.setBorder(BorderFactory.createEmptyBorder());
        Dimension d = new Dimension(40, 40);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);
        b.setToolTipText(tooltip);
        b.getAccessibleContext().setAccessibleName(tooltip);
        return b;
    }

    private static Color blend(Color c) {
        return new Color(Math.max(0, c.getRed() - 12), Math.max(0, c.getGreen() - 12), Math.max(0, c.getBlue() - 12));
    }

    /** 흰 바탕 둥근 카드 */
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        return p;
    }

    /** 유형 색 작은 사각형 + 이름 (범례) */
    public static JLabel legend(ScheduleType t) {
        Color[] c = typeColors(t);
        JLabel l = new JLabel(t.getLabel());
        l.setFont(font(12, false));
        l.setForeground(new Color(0x4A5565));
        l.setIcon(new SwatchIcon(c[0], c[2]));
        l.setIconTextGap(5);
        return l;
    }

    /** 범례용 색 사각형 아이콘 */
    static final class SwatchIcon implements javax.swing.Icon {
        private final Color fill, line;
        SwatchIcon(Color fill, Color line) { this.fill = fill; this.line = line; }
        @Override public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
            g.setColor(fill); g.fillRect(x, y, 11, 11);
            g.setColor(line); g.drawRect(x, y, 11, 11);
        }
        @Override public int getIconWidth() { return 12; }
        @Override public int getIconHeight() { return 12; }
    }
}
