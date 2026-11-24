package com.onioncalendar.view;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ProgressOnionPanel extends JPanel {
    
    // TODO: 추후 Controller에서 실제 과제 진행도를 계산하여 이 값을 업데이트해야 합니다.
    // 현재는 화면 테스트를 위해 임시로 50.0%로 고정해 두었습니다.
    private double progressPercentage = 50.0; 

    public ProgressOnionPanel() {
        // 패널 기본 크기 설정
        setPreferredSize(new Dimension(300, 300));
    }

    // Controller에서 호출할 진행도 업데이트 메서드
    public void updateProgress(double percentage) {
        this.progressPercentage = Math.max(0.0, Math.min(100.0, percentage));
        repaint(); // 화면 다시 그리기
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        int width = getWidth();
        int height = getHeight();

        // 배경색 설정 (원하는 색상으로 변경 가능)
        g2d.setColor(new Color(240, 248, 255));
        g2d.fillRect(0, 0, width, height);

        // 진행도를 6단계로 계산 (100 / 6 ≒ 16.666)
        // 0단계(0)부터 5단계(5)까지 인덱스 생성
        int stageIndex = (int) (progressPercentage / 16.6666);
        if (stageIndex >= 6) {
            stageIndex = 5; // 100%일 때 인덱스 초과 방지
        }

        // 이미지 파일명 설정 (onion_stage1.png ~ onion_stage6.png)
        int currentStage = stageIndex + 1;
        String imageName = "onion_stage" + currentStage + ".png";
        String imagePath = "src/main/resources/images/" + imageName;

        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            // 이미지가 존재하면 화면 중앙에 그리기
            ImageIcon icon = new ImageIcon(imagePath);
            Image img = icon.getImage();
            
            // 이미지 크기 조절 (예: 200x200)
            int imgW = 200;
            int imgH = 200;
            int x = (width - imgW) / 2;
            int y = (height - imgH) / 2;
            
            g2d.drawImage(img, x, y, imgW, imgH, this);
        } else {
            // 이미지가 없을 때를 대비한 임시 텍스트 표시 (테스트 확인용)
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            g2d.drawString("이미지 파일 없음: " + imageName, width / 2 - 80, height / 2 - 20);
            g2d.drawString("현재 진행도: " + progressPercentage + "%", width / 2 - 60, height / 2 + 10);
            g2d.drawString("현재 단계: " + currentStage + " / 6 단계", width / 2 - 60, height / 2 + 40);
        }
    }
}