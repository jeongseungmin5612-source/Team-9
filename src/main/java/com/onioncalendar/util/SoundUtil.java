package com.onioncalendar.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class SoundUtil {
    
    // 재생할 오디오 파일명 목록
    private static final String[] SOUND_FILES = {
            "siren.wav",
            "soundbyman.wav",
            "whilhelm_scream.wav"
    };
    
    private static final Random random = new Random();

    // 오디오 재생 메서드
    public static void playAlertSound() {
        try {
            int randomIndex = random.nextInt(SOUND_FILES.length);
            String selectedSound = SOUND_FILES[randomIndex];
            
            // 랜덤으로 선택된 파일 경로 생성
            File audioFile = new File("src/main/resources/audio/" + selectedSound);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}