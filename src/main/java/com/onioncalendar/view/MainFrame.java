package com.onioncalendar.view;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

/** 메인 창. 상단 메뉴(캘린더/시간표/공유/설정)와 CardLayout으로 화면을 바꾼다. */
public class MainFrame extends JFrame {
    public static final String CALENDAR = "calendar";
    public static final String TIMETABLE = "timetable";
    public static final String SHARE = "share";
    public static final String SETTINGS = "settings";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final JButton addButton = UiStyle.accentButton("+ 일정 추가");

    public MainFrame() {
        super("CAMPLE - 캘린더");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1180, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UiStyle.BG);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        cards.setBackground(UiStyle.BG);
        add(cards, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiStyle.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        JLabel brand = new JLabel("CAMPLE");
        brand.setFont(UiStyle.font(20, true));
        brand.setForeground(Color.WHITE);
        left.add(brand);
        left.add(Box.createHorizontalStrut(20));
        String[][] menus = {{CALENDAR, "캘린더"}, {TIMETABLE, "시간표"}, {SHARE, "공유"}, {SETTINGS, "설정"}};
        for (String[] m : menus) {
            NavButton b = new NavButton(m[1]);
            b.addActionListener(e -> showPanel(m[0]));
            navButtons.put(m[0], b);
            left.add(b);
            left.add(Box.createHorizontalStrut(4));
        }
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        addButton.setPreferredSize(new Dimension(130, 40));
        right.add(addButton);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /** 메뉴 이름(CALENDAR 등)으로 화면을 등록한다 */
    public void addPanel(String name, Component panel) {
        cards.add(panel, name);
    }

    public void showPanel(String name) {
        cardLayout.show(cards, name);
        navButtons.forEach((k, b) -> b.setSelected(k.equals(name)));
        addButton.setVisible(CALENDAR.equals(name));
    }

    /** 상단 [+ 일정 추가] 버튼 */
    public JButton getAddButton() { return addButton; }

    /** 선택되면 흰 바탕 + 남색 글씨가 되는 상단 메뉴 버튼 */
    private static final class NavButton extends JButton {
        NavButton(String text) {
            super(text);
            setFont(UiStyle.font(15, false));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void setSelected(boolean b) {
            super.setSelected(b);
            setForeground(b ? UiStyle.NAVY : Color.WHITE);
            setFont(UiStyle.font(15, b));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isSelected()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
