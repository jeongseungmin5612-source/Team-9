package com.onioncalendar.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;

/**
 * 바탕화면 상단에 항상 위에 뜨는 양파 캐릭터 잔소리/진행도 오버레이 위젯 UI 클래스입니다.
 */
public class OnionWidgetWindow extends JWindow {
    private Point initialClick;
    private final JLabel speechBubbleLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel onionImageLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel progressLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel contentPanel;
    private Color currentThemeColor = new Color(235, 165, 65);

    public OnionWidgetWindow(Runnable onClickCallback) {
        setSize(180, 230);
        setBackground(new Color(0, 0, 0, 0)); // 투명 배경

        // 둥근 사각형 커스텀 패널 생성
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 245));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(currentThemeColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BorderLayout(0, 4));
        contentPanel.setBorder(new EmptyBorder(10, 10, 8, 10));

        speechBubbleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        progressLabel.setFont(new Font("맑은 고딕", Font.BOLD, 11));

        onionImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        onionImageLabel.setVerticalAlignment(SwingConstants.CENTER);

        contentPanel.add(speechBubbleLabel, BorderLayout.NORTH);
        contentPanel.add(onionImageLabel, BorderLayout.CENTER);
        contentPanel.add(progressLabel, BorderLayout.SOUTH);

        // 마우스 드래그로 위젯 이동 가능하도록 처리
        contentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
            @Override
            public void mouseClicked(MouseEvent e) { if (onClickCallback != null) onClickCallback.run(); }
        });

        contentPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        add(contentPanel);

        // 화면 우측 하단 배치 및 항상 위에 표시
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width - 210, screenSize.height - 270);
        setAlwaysOnTop(true);

        updateOnionStage(72); // 초기 진행도 72%
    }

    /**
     * 다양한 파일 경로(바탕화면, 프로젝트 리소스 등)에서 양파 이미지 로드
     */
    private Image loadOnionImage(String baseName) {
        String fileName = baseName.endsWith(".png") ? baseName : baseName + ".png";
        
        String userHome = System.getProperty("user.home");
        File desktopFolder = new File(userHome, "Desktop/양파사진/" + fileName);
        if (desktopFolder.exists() && desktopFolder.isFile()) {
            return Toolkit.getDefaultToolkit().getImage(desktopFolder.getAbsolutePath());
        }

        File oneDriveDesktopFolder = new File(userHome, "OneDrive/바탕 화면/양파사진/" + fileName);
        if (oneDriveDesktopFolder.exists() && oneDriveDesktopFolder.isFile()) {
            return Toolkit.getDefaultToolkit().getImage(oneDriveDesktopFolder.getAbsolutePath());
        }

        File oneDriveDesktopEng = new File(userHome, "OneDrive/Desktop/양파사진/" + fileName);
        if (oneDriveDesktopEng.exists() && oneDriveDesktopEng.isFile()) {
            return Toolkit.getDefaultToolkit().getImage(oneDriveDesktopEng.getAbsolutePath());
        }

        URL classUrl = getClass().getResource("/images/" + fileName);
        if (classUrl == null) classUrl = getClass().getResource("/" + fileName);
        if (classUrl != null) {
            return Toolkit.getDefaultToolkit().getImage(classUrl);
        }

        String[] searchPaths = { fileName, "images/" + fileName, "양파사진/" + fileName };
        for (String path : searchPaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                return Toolkit.getDefaultToolkit().getImage(f.getAbsolutePath());
            }
        }

        return null; // 이미지 파일이 없을 경우 null
    }

    /**
     * 공부 진행도(%) 수치에 따른 양파의 대사, 테마 색상, 이미지를 업데이트합니다.
     */
    public void updateOnionStage(int progress) {
        progress = Math.max(0, Math.min(100, progress));

        String dialogue;
        String imgFileName;
        Color themeColor;

        if (progress < 20) {
            dialogue = "<html><center><b>\"자냐? 지금 잠이 오냐??\"</b></center></html>";
            imgFileName = "KakaoTalk_261125_162408_31.png";
            themeColor = new Color(211, 47, 47);
        } else if (progress < 40) {
            dialogue = "<html><center><b>\"빨리 공부해라!\"</b></center></html>";
            imgFileName = "angry_onion.png";
            themeColor = new Color(230, 81, 0);
        } else if (progress < 60) {
            dialogue = "<html><center><b>\"이러다 큰일 난다...\"</b></center></html>";
            imgFileName = "KakaoTalk_261125_162408_21.png";
            themeColor = new Color(245, 124, 0);
        } else if (progress < 80) {
            dialogue = "<html><center><b>\"조금만 더 집중하자!\"</b></center></html>";
            imgFileName = "KakaoTalk_261125_162408_11.png";
            themeColor = new Color(251, 192, 45);
        } else if (progress < 100) {
            dialogue = "<html><center><b>\"잘하고 있어! 화이팅!\"</b></center></html>";
            imgFileName = "KakaoTalk_261125_162353.png";
            themeColor = new Color(56, 142, 60);
        } else {
            dialogue = "<html><center><b>\"완벽해! 이대로만 가자!\"</b></center></html>";
            imgFileName = "KakaoTalk_261125_162408_01.png";
            themeColor = new Color(2, 136, 209);
        }

        this.currentThemeColor = themeColor;

        speechBubbleLabel.setText(dialogue);
        speechBubbleLabel.setForeground(themeColor);

        progressLabel.setText("[ 양파 진행도: " + progress + "% ]");
        progressLabel.setForeground(themeColor);

        Image rawImg = loadOnionImage(imgFileName);

        if (rawImg != null) {
            Image scaled = rawImg.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            onionImageLabel.setIcon(new ImageIcon(scaled));
            onionImageLabel.setText("");
        } else {
            // 이미지 없을 시 대체 아스키 아트 표기
            onionImageLabel.setIcon(null);
            onionImageLabel.setText("<html><pre style='font-family:monospace;font-size:10px;color:" 
                    + String.format("#%06x", themeColor.getRGB() & 0xFFFFFF) + ";'>"
                    + "   /\\_/\\\n"
                    + "  ( o.o )\n"
                    + "   &gt; ^ &lt; \n"
                    + "  [ONION]"
                    + "</pre></html>");
        }

        contentPanel.repaint();
    }
}
