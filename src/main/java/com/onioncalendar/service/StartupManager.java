package com.onioncalendar.service;

import com.onioncalendar.Main;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Windows 시작 프로그램 레지스트리(Run Key) 등록 및 해제를 관리하는 서비스 클래스입니다.
 */
public class StartupManager {
    private static final String RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String APP_NAME = "CAMPLE_OnionCalendar";

    /**
     * Windows 레지스트리에 프로그램을 시작 프로그램으로 등록합니다.
     */
    public boolean register() {
        try {
            String launchCommand = buildLaunchCommand();
            if (launchCommand == null) return false;

            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("win")) {
                ProcessBuilder pb = new ProcessBuilder(
                        "reg", "add", RUN_KEY, "/v", APP_NAME, "/t", "REG_SZ", "/d", launchCommand, "/f"
                );
                Process process = pb.start();
                return process.waitFor() == 0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Windows 레지스트리에서 시작 프로그램 등록을 해제합니다.
     */
    public boolean unregister() {
        try {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("win")) {
                ProcessBuilder pb = new ProcessBuilder(
                        "reg", "delete", RUN_KEY, "/v", APP_NAME, "/f"
                );
                Process process = pb.start();
                return process.waitFor() == 0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 현재 실행 중인 프로그램(JAR 또는 Class) 경로를 기반으로 실행 명령어를 생성합니다.
     */
    private String buildLaunchCommand() throws URISyntaxException {
        File location = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String path = location.getAbsolutePath();
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe";
        File javawFile = new File(javaBin);
        if (!javawFile.exists()) {
            javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe";
        }

        if (path.endsWith(".jar")) {
            return "\"" + javaBin + "\" -jar \"" + path + "\"";
        }
        return "\"" + javaBin + "\" -cp \"" + path + "\" com.onioncalendar.Main";
    }
}
