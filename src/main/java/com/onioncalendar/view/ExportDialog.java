package com.onioncalendar.view;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

/** 내보내기 옵션 창 (UC5): 친구에게 보일 이름, 일정 제목 포함 여부. */
public class ExportDialog extends JDialog {
    private final JTextField nameField = new JTextField();
    private final JCheckBox titleCheck = new JCheckBox("일정 제목 포함");
    private final JLabel warning = new JLabel();
    private boolean confirmed;

    public ExportDialog(Window owner, String lastName) {
        super(owner, "내 일정 내보내기", ModalityType.APPLICATION_MODAL);
        nameField.setText(lastName == null ? "" : lastName);
    }

    /** [저장]을 누르면 true, 취소하면 false */
    public boolean showDialog() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel("친구에게 보일 이름 (최대 10자)");
        nameLabel.setFont(UiStyle.font(13, true));
        nameLabel.setForeground(new Color(0x4A5565));
        nameLabel.setLabelFor(nameField);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(nameLabel);
        body.add(Box.createVerticalStrut(4));
        nameField.setFont(UiStyle.font(14, false));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB7C2D1)), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));
        body.add(nameField);
        body.add(Box.createVerticalStrut(14));

        titleCheck.setFont(UiStyle.font(15, false));
        titleCheck.setOpaque(false);
        titleCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(titleCheck);
        JLabel hint = new JLabel("<html>끄면 친구에게는 일정이 \"일정 있음\", 수업이 \"수업\"으로만 보이고<br>"
                + "날짜와 시간만 전달됩니다.</html>");
        hint.setFont(UiStyle.font(12, false));
        hint.setForeground(UiStyle.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(hint);
        body.add(Box.createVerticalStrut(12));

        JLabel info = new JLabel("<html>함께 저장되는 것: 시간표 · 일정 · 양파 진행도 · 내보낸 날짜<br>"
                + "[저장]을 누르면 저장 위치를 고르는 창이 열립니다.</html>");
        info.setFont(UiStyle.font(12, false));
        info.setForeground(new Color(0x4A5565));
        info.setOpaque(true);
        info.setBackground(UiStyle.BG);
        info.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(info);
        body.add(Box.createVerticalStrut(10));

        warning.setOpaque(true);
        warning.setBackground(UiStyle.ERROR_BG);
        warning.setForeground(UiStyle.ERROR_FG);
        warning.setFont(UiStyle.font(13, false));
        warning.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        warning.setAlignmentX(Component.LEFT_ALIGNMENT);
        warning.setVisible(false);
        body.add(warning);
        root.add(body, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = UiStyle.secondaryButton("취소");
        cancel.addActionListener(e -> { confirmed = false; dispose(); });
        JButton save = UiStyle.primaryButton("저장");
        save.addActionListener(e -> {
            String n = getUserName();
            if (n.isEmpty() || n.length() > 10) {
                warning.setText("이름은 1~10자로 입력하세요.");
                warning.setVisible(true);
                pack();
                return;
            }
            confirmed = true;
            dispose();
        });
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);

        setContentPane(root);
        pack();
        setSize(Math.max(getWidth(), 440), getHeight());
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return confirmed;
    }

    public String getUserName() { return nameField.getText().trim(); }

    public boolean isTitleIncluded() { return titleCheck.isSelected(); }
}
