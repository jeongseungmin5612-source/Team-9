package com.onioncalendar.view;

import com.onioncalendar.model.Lecture;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 수업 입력 창 (UC2). 같은 창을 여러 번 띄울 수 있고, 다시 띄우면 입력값이 유지된다. */
public class LectureFormDialog extends JDialog {
    private static final DayOfWeek[] DAYS = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};

    private final JTextField subjectField = field();
    private final JComboBox<DayOfWeek> dayCombo = new JComboBox<>(DAYS);
    private final JComboBox<LocalTime> startCombo = new JComboBox<>(times());
    private final JComboBox<LocalTime> endCombo = new JComboBox<>(times());
    private final JTextField roomField = field();
    private final JLabel warning = new JLabel();

    private boolean built;
    private boolean deleteRequested;
    private Lecture result;

    public LectureFormDialog(Window owner) {
        super(owner, "수업 입력", ModalityType.APPLICATION_MODAL);
        dayCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                String text = ((DayOfWeek) value).getDisplayName(TextStyle.FULL, Locale.KOREAN);
                return super.getListCellRendererComponent(list, text, index, selected, focus);
            }
        });
        for (JComboBox<?> c : List.of(dayCombo, startCombo, endCombo)) c.setFont(UiStyle.font(14, false));
    }

    /** 창을 띄운다. l이 null이면 새 수업. 저장하면 새 Lecture, 취소·삭제면 null. */
    public Lecture showForm(Lecture l) {
        result = null;
        deleteRequested = false;
        if (!built) {
            fill(l);
            setContentPane(build(l != null));
            built = true;
            pack();
            setSize(Math.max(getWidth(), 420), getHeight());
            setLocationRelativeTo(getOwner());
        }
        setVisible(true);
        return result;
    }

    /** 컨트롤러가 거절했을 때(겹침 등) 이유를 보여준다. 다음 showForm에서 입력값은 그대로다. */
    public void showWarning(String msg) {
        warning.setText(msg);
        warning.setVisible(true);
        pack();
        setSize(Math.max(getWidth(), 420), getHeight());
    }

    /** 사용자가 [삭제]를 눌렀는가 */
    public boolean isDeleteRequested() { return deleteRequested; }

    private void fill(Lecture l) {
        if (l == null) {
            dayCombo.setSelectedItem(DayOfWeek.MONDAY);
            startCombo.setSelectedItem(LocalTime.of(9, 0));
            endCombo.setSelectedItem(LocalTime.of(10, 30));
            return;
        }
        subjectField.setText(l.getSubject());
        dayCombo.setSelectedItem(l.getDay());
        startCombo.setSelectedItem(l.getStart());
        endCombo.setSelectedItem(l.getEnd());
        roomField.setText(l.getRoom() == null ? "" : l.getRoom());
    }

    private JPanel build(boolean editing) {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        add(form, labeled("과목명", subjectField));
        add(form, labeled("요일", dayCombo));
        JPanel times = new JPanel(new GridLayout(1, 2, 10, 0));
        times.setOpaque(false);
        times.add(labeled("시작 (30분 단위)", startCombo));
        times.add(labeled("종료", endCombo));
        add(form, times);
        add(form, labeled("강의실 (선택)", roomField));

        warning.setOpaque(true);
        warning.setBackground(UiStyle.ERROR_BG);
        warning.setForeground(UiStyle.ERROR_FG);
        warning.setFont(UiStyle.font(13, false));
        warning.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        warning.setAlignmentX(Component.LEFT_ALIGNMENT);
        warning.setVisible(false);
        form.add(warning);
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setOpaque(false);
        if (editing) {
            JButton delete = UiStyle.dangerButton("삭제");
            delete.addActionListener(e -> { deleteRequested = true; result = null; setVisible(false); });
            buttons.add(delete, BorderLayout.WEST);
        }
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton cancel = UiStyle.secondaryButton("취소");
        cancel.addActionListener(e -> { result = null; setVisible(false); });
        JButton save = UiStyle.primaryButton("저장");
        save.addActionListener(e -> onSave());
        right.add(cancel);
        right.add(save);
        buttons.add(right, BorderLayout.EAST);
        root.add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        return root;
    }

    private void onSave() {
        String subject = subjectField.getText().trim();
        LocalTime s = (LocalTime) startCombo.getSelectedItem(), e = (LocalTime) endCombo.getSelectedItem();
        if (subject.isEmpty()) { showWarning("과목명을 입력하세요."); return; }
        if (!s.isBefore(e)) { showWarning("시작 시간이 종료 시간보다 빨라야 합니다."); return; }
        String room = roomField.getText().trim();
        result = new Lecture(subject, (DayOfWeek) dayCombo.getSelectedItem(), s, e, room.isEmpty() ? null : room);
        warning.setVisible(false);
        setVisible(false);
    }

    private static LocalTime[] times() {
        List<LocalTime> out = new ArrayList<>();
        for (int m = 7 * 60; m <= 23 * 60; m += 30) out.add(LocalTime.of(m / 60, m % 60));
        return out.toArray(new LocalTime[0]);
    }

    private static JTextField field() {
        JTextField f = new JTextField();
        f.setFont(UiStyle.font(14, false));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB7C2D1)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private static JPanel labeled(String text, JComponent c) {
        JLabel l = new JLabel(text);
        l.setFont(UiStyle.font(13, true));
        l.setForeground(new Color(0x4A5565));
        l.setLabelFor(c);
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(l, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private static void add(JPanel parent, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        parent.add(c);
        parent.add(Box.createVerticalStrut(10));
    }
}
