package com.onioncalendar.view;

import com.onioncalendar.controller.ShareController;
import com.onioncalendar.model.Friend;
import com.onioncalendar.model.Onion;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

/** 일정 공유 화면 (UC5 내보내기, UC6 가져오기, UC8 친구 정보 조회). */
public class SharePanel extends JPanel {
    private static final Preferences PREFS = Preferences.userNodeForPackage(SharePanel.class);

    private final ShareController controller;
    private final DefaultListModel<Friend> model = new DefaultListModel<>();
    private final JList<Friend> friendList = new JList<>(model);
    private final JLabel emptyLabel = new JLabel("<html>아직 가져온 친구가 없습니다.<br>[가져오기]로 친구가 보낸 파일을 불러오세요.</html>");

    /** @param openFreeTime 공통 공강시간 화면을 여는 동작 */
    public SharePanel(ShareController controller, Runnable openFreeTime) {
        this.controller = controller;
        controller.setReplaceConfirmer(this::confirmReplace);
        setLayout(new BorderLayout(20, 0));
        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 왼쪽: 친구 목록
        JPanel listCard = UiStyle.card(new BorderLayout(0, 10));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("친구 목록");
        title.setFont(UiStyle.font(22, true));
        title.setForeground(UiStyle.TEXT);
        JLabel hint = new JLabel("친구를 누르면 일정표와 양파를 볼 수 있어요");
        hint.setFont(UiStyle.font(12, false));
        hint.setForeground(UiStyle.MUTED);
        head.add(title, BorderLayout.WEST);
        head.add(hint, BorderLayout.EAST);
        listCard.add(head, BorderLayout.NORTH);

        friendList.setCellRenderer(new FriendRenderer());
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setFixedCellHeight(58);
        friendList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        friendList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int i = friendList.locationToIndex(e.getPoint());
                if (i >= 0 && friendList.getCellBounds(i, i).contains(e.getPoint())) {
                    openFriendInfo(model.get(i));
                    friendList.clearSelection();
                }
            }
        });
        JScrollPane scroll = new JScrollPane(friendList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(columnHeader(), BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        emptyLabel.setFont(UiStyle.font(14, false));
        emptyLabel.setForeground(UiStyle.MUTED);
        emptyLabel.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
        center.add(emptyLabel, BorderLayout.SOUTH);
        listCard.add(center, BorderLayout.CENTER);
        add(listCard, BorderLayout.CENTER);

        // 오른쪽: 공강 찾기 + 파일 주고받기
        JPanel side = new JPanel();
        side.setOpaque(false);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(300, 10));

        JPanel freeCard = sideCard("팀플 시간 맞추기", "나와 고른 친구들이 모두 비는 시간을 찾아줘요.");
        JButton freeBtn = UiStyle.accentButton("공통 공강시간 찾기");
        freeBtn.addActionListener(e -> openFreeTime.run());
        freeCard.add(stretch(freeBtn));
        side.add(freeCard);
        side.add(Box.createVerticalStrut(16));

        JPanel fileCard = sideCard("파일 주고받기", null);
        JButton exportBtn = UiStyle.primaryButton("내보내기");
        exportBtn.addActionListener(e -> openExport());
        JButton importBtn = UiStyle.secondaryButton("가져오기");
        importBtn.addActionListener(e -> openImport());
        fileCard.add(stretch(exportBtn));
        fileCard.add(Box.createVerticalStrut(8));
        fileCard.add(stretch(importBtn));
        fileCard.add(Box.createVerticalStrut(8));
        JLabel note = new JLabel("<html>내보낸 JSON 파일은 메신저로 직접 전달합니다.<br>같은 이름의 친구를 다시 가져오면 덮어쓸지 묻습니다.</html>");
        note.setFont(UiStyle.font(12, false));
        note.setForeground(UiStyle.MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileCard.add(note);
        side.add(fileCard);
        side.add(Box.createVerticalGlue());
        add(side, BorderLayout.EAST);

        refreshFriends();
    }

    public void refreshFriends() {
        model.clear();
        for (Friend f : controller.getFriends()) model.addElement(f);
        emptyLabel.setVisible(model.isEmpty());
    }

    /** 같은 이름의 친구가 이미 있을 때 덮어쓸지 묻는다 */
    public boolean confirmReplace(String name) {
        return JOptionPane.showConfirmDialog(this, "\"" + name + "\"의 일정이 이미 있습니다. 새 파일로 바꿀까요?",
                "덮어쓰기 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void openExport() {
        ExportDialog dialog = new ExportDialog(SwingUtilities.getWindowAncestor(this), PREFS.get("lastName", ""));
        if (!dialog.showDialog()) return;
        String name = dialog.getUserName();
        PREFS.put("lastName", name);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("내보낼 파일 저장");
        chooser.setFileFilter(new FileNameExtensionFilter("일정 공유 파일 (*.json)", "json"));
        chooser.setSelectedFile(new File(name + "_일정_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".json"));
        while (true) {
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".json")) file = new File(file.getPath() + ".json");
            if (file.exists() && JOptionPane.showConfirmDialog(this, file.getName() + " 파일이 이미 있습니다. 덮어쓸까요?",
                    "덮어쓰기 확인", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) continue;
            if (controller.exportSchedule(name, dialog.isTitleIncluded(), file.toPath())) {
                JOptionPane.showMessageDialog(this, "내보냈습니다.\n" + file.getPath(), "내보내기 완료",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, controller.getLastError(), "내보내기 실패", JOptionPane.WARNING_MESSAGE);
            if (controller.getLastError().startsWith("내보낼")) return;   // 일정이 없으면 다시 고를 필요 없음
        }
    }

    public void openImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("친구 일정 파일 가져오기");
        chooser.setFileFilter(new FileNameExtensionFilter("일정 공유 파일 (*.json)", "json"));
        while (true) {
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            Path path = chooser.getSelectedFile().toPath();
            if (controller.importFriend(path)) {
                refreshFriends();
                JOptionPane.showMessageDialog(this, "친구 일정을 가져왔습니다.", "가져오기 완료",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (controller.getLastError().isEmpty()) return;   // 덮어쓰기를 취소함
            JOptionPane.showMessageDialog(this, controller.getLastError(), "가져오기 실패", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void openFriendInfo(Friend f) {
        new FriendInfoDialog(SwingUtilities.getWindowAncestor(this), controller)
                .showFriend(f, Onion.getStage(f.getProgress()));
        refreshFriends();   // 삭제했을 수 있음
    }

    private JPanel columnHeader() {
        JPanel p = new JPanel(new GridLayout(1, 3));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiStyle.BORDER),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        for (String s : new String[]{"이름", "내보낸 날짜", "진행도"}) {
            JLabel l = new JLabel(s);
            l.setFont(UiStyle.font(13, true));
            l.setForeground(UiStyle.MUTED);
            p.add(l);
        }
        return p;
    }

    private JPanel sideCard(String title, String desc) {
        JPanel card = UiStyle.card(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel(title);
        t.setFont(UiStyle.font(17, true));
        t.setForeground(UiStyle.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(t);
        card.add(Box.createVerticalStrut(6));
        if (desc != null) {
            JLabel d = new JLabel(desc);
            d.setFont(UiStyle.font(12, false));
            d.setForeground(UiStyle.MUTED);
            d.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(d);
            card.add(Box.createVerticalStrut(10));
        }
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private static JPanel stretch(JButton b) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(b, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setPreferredSize(new Dimension(200, 44));
        return p;
    }

    /** 친구 한 줄: 이름 / 내보낸 날짜(+7일 지남) / 진행도 */
    private static final class FriendRenderer implements ListCellRenderer<Friend> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Friend> list, Friend f, int index,
                                                      boolean selected, boolean focus) {
            JPanel row = new JPanel(new GridLayout(1, 3));
            row.setBackground(selected ? UiStyle.TODAY_BG : Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UiStyle.BORDER),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)));
            JLabel name = new JLabel(f.getName());
            name.setFont(UiStyle.font(16, true));
            name.setForeground(UiStyle.TEXT);
            JPanel date = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            date.setOpaque(false);
            JLabel d = new JLabel(f.getExportedAt().toString());
            d.setFont(UiStyle.font(14, false));
            d.setForeground(UiStyle.TEXT);
            date.add(d);
            if (f.isOutdated(LocalDate.now())) {
                JLabel badge = new JLabel(Friend.OUTDATED_DAYS + "일 이상 지남");
                badge.setFont(UiStyle.font(12, false));
                badge.setOpaque(true);
                Color[] c = UiStyle.typeColors(com.onioncalendar.model.ScheduleType.ASSIGNMENT);
                badge.setBackground(c[0]);
                badge.setForeground(c[1]);
                badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                date.add(Box.createHorizontalStrut(8));
                date.add(badge);
            }
            JLabel prog = new JLabel(f.getProgress() == Onion.NO_DATA ? "일정 없음" : f.getProgress() + "%");
            prog.setFont(UiStyle.font(14, false));
            prog.setForeground(UiStyle.TEXT);
            row.add(name);
            JPanel mid = new JPanel(new java.awt.GridBagLayout());
            mid.setOpaque(false);
            mid.add(date);
            JPanel midLeft = new JPanel(new BorderLayout());
            midLeft.setOpaque(false);
            midLeft.add(mid, BorderLayout.WEST);
            row.add(midLeft);
            row.add(prog);
            row.getAccessibleContext().setAccessibleName(f.getName() + " 정보 보기");
            return row;
        }
    }
}
