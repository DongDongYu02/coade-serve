package cn.dong.nexus.common.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FfmpegUtil {

    private final static String FFMPEG_PATH = "C:\\Users\\Administrator\\AppData\\Local\\Microsoft\\WinGet\\Links\\ffmpeg.exe";

    public static String extractFrame(String videoPath, int second, String outputImagePath) {
        try {
            String time = formatSecond(second);

            ProcessBuilder pb = new ProcessBuilder(
                    FFMPEG_PATH,
                    "-y",
                    "-ss", time,
                    "-i", videoPath,
                    "-frames:v", "1",
                    outputImagePath
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("ffmpeg 抽帧失败，exitCode=" + exitCode);
            }
            return outputImagePath;
        } catch (Exception e) {
            throw new RuntimeException("抽帧异常", e);
        }
    }

    private static String formatSecond(int totalSeconds) {
        int hh = totalSeconds / 3600;
        int mm = (totalSeconds % 3600) / 60;
        int ss = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

}
