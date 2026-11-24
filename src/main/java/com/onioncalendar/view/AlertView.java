package com.onioncalendar.view;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class AlertView {
    public static void showGameAlert() {
        // 1. 화난 양파 이미지 경로 설정 
        String imagePath = "src/main/resources/images/angry_onion.png";
        ImageIcon onionIcon = null;

        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            // 2. 원본 이미지가 너무 클 수 있으므로 알림창에 맞게 크기 조절 (예: 150x150)
            ImageIcon originalIcon = new ImageIcon(imagePath);
            Image img = originalIcon.getImage();
            Image resizedImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            onionIcon = new ImageIcon(resizedImg);
        } else {
            System.out.println("경고: 화난 양파 이미지를 찾을 수 없습니다. 경로를 확인해주세요.");
        }

        // 3. 알림창 내부에 들어갈 패널 생성 (위아래로 배치하기 위해 BoxLayout 사용)
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // 창 위에 화난 양파 배치
        if (onionIcon != null) {
            JLabel imageLabel = new JLabel(onionIcon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // 가운데 정렬
            panel.add(imageLabel);
            panel.add(Box.createVerticalStrut(15)); // 이미지와 텍스트 사이의 여백 (15px)
        }

        // 경고 문구 추가
        JLabel textLabel1 = new JLabel("과제 제출이 하루 남았습니다");
        JLabel textLabel2 = new JLabel("게임을 끄고 당장 과제를 시작하세요!");
        
        textLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 텍스트 스타일 설정 (빨간색 굵은 글씨로 강조)
        textLabel1.setForeground(Color.RED);
        textLabel1.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        textLabel2.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        panel.add(textLabel1);
        panel.add(Box.createVerticalStrut(5));
        panel.add(textLabel2);

        // 4. 알림창 띄우기
        // JOptionPane.PLAIN_MESSAGE를 사용하면 자바 기본 경고 아이콘(노란색 삼각형)이 사라져서 양파가 더 돋보입니다.
        JOptionPane.showMessageDialog(
            null, 
            panel, 
            "경고", 
            JOptionPane.PLAIN_MESSAGE
        );
    }
}