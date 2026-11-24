package com.onioncalendar.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CharacterPetPanel extends JPanel {
    
    // 캐릭터의 상태 정의
    public enum CharacterState {
        NORMAL,     // 평범한 상태
        URGENT,     // 과제가 임박하거나 진행도가 너무 낮을 때 (활발히/초조하게 움직임)
        DEPRESSED   // 과제가 한동안 없을 때 (우울해함)
    }

    private CharacterState currentState = CharacterState.NORMAL;
    private int xOffset = 0;
    private int yOffset = 0;
    private boolean movingRight = true;
    private Timer animationTimer;

    public CharacterPetPanel() {
        setPreferredSize(new Dimension(200, 200));
        setOpaque(false); // 투명 배경

        // 50ms마다 화면을 갱신하여 애니메이션 효과 생성
        animationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateCharacter();
                repaint();
            }
        });
        animationTimer.start();
    }

    // Controller에서 로직에 따라 캐릭터 상태를 업데이트 해주는 메서드
    public void setCharacterState(CharacterState state) {
        this.currentState = state;
    }

    private void animateCharacter() {
        switch (currentState) {
            case URGENT:
                // 바쁘게 좌우로 빠르게 움직임
                if (movingRight) xOffset += 10;
                else xOffset -= 10;
                
                if (xOffset > 50) movingRight = false;
                if (xOffset < -50) movingRight = true;
                yOffset = (int)(Math.random() * 5); // 덜덜 떠는 효과
                break;
                
            case DEPRESSED:
                // 움직임이 없고 아래로 처짐
                xOffset = 0;
                yOffset = 20; 
                break;
                
            case NORMAL:
            default:
                // 천천히 숨쉬는 듯한 움직임 (상하 이동)
                xOffset = 0;
                yOffset = (int) (Math.sin(System.currentTimeMillis() / 300.0) * 5);
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // TODO: 팀원이 만든 리소스(아스키 아트 또는 이미지)를 불러와서 그립니다.
        // 현재는 임시로 상태를 텍스트와 도형으로 표현합니다.
        
        int centerX = getWidth() / 2 + xOffset;
        int centerY = getHeight() / 2 + yOffset;

        g.setColor(Color.BLACK);
        
        String emotionText = "";
        if (currentState == CharacterState.URGENT) {
            g.setColor(Color.RED);
            emotionText = "과제해!! 빨리!!! (🔥)";
        } else if (currentState == CharacterState.DEPRESSED) {
            g.setColor(Color.GRAY);
            emotionText = "할 일이 없네... (💧)";
        } else {
            g.setColor(Color.ORANGE);
            emotionText = "평-온 (🧅)";
        }

        // 임시 캐릭터 (동그라미)
        g.fillOval(centerX - 25, centerY - 25, 50, 50);
        
        // 텍스트 그리기
        g.setColor(Color.BLACK);
        g.drawString(emotionText, centerX - 40, centerY - 35);
    }
}
