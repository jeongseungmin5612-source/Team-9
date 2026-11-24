package com.onioncalendar.service;

import com.onioncalendar.util.SoundUtil;
import com.onioncalendar.view.AlertView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class GameProcessMonitorService {
    private Timer timer;
    private final String[] gameProcesses = {"LeagueClient.exe", "Valorant.exe", "Overwatch.exe", "steam.exe"}; // 예시 게임들

    public void startMonitoring() {
        timer = new Timer(true);
        // 1분마다 프로세스 검사
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isAssignmentDueTomorrow() && isGameRunning()) {
                    SoundUtil.playAlertSound();
                    AlertView.showGameAlert();
                }
            }
        }, 0, 60000);
    }

    private boolean isGameRunning() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("tasklist");
            Process process = processBuilder.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                for (String game : gameProcesses) {
                    if (line.toLowerCase().contains(game.toLowerCase())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    // 팀원의 Model(과제 데이터)과 합칠 때 이 부분을 실제 로직으로 변경해야 합니다.
    private boolean isAssignmentDueTomorrow() {
        // TODO: Controller나 Repository에서 마감일이 1일 남은 과제가 있는지 체크
        return true; // 테스트용 임시 반환값
    }
}
