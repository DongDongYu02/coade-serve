package cn.dong.coade.modules.cmt;

import cn.dong.nexus.common.utils.FfmpegUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Main {
    static void main() throws IOException, InterruptedException {
        String ffmpegPath = "C:\\Users\\Administrator\\AppData\\Local\\Microsoft\\WinGet\\Links\\ffmpeg.exe";
        String videoPath = "C:\\Users\\Administrator\\Desktop\\image\\1.mp4";

        String outputDir = "D:\\test\\frames";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String outputPattern = outputDir + "\\frame_%03d.png";

        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-y",              // 已存在文件直接覆盖
                "-nostdin",        // 不等待控制台输入
                "-i", videoPath,
                "-vf", "fps=5",
                outputPattern
        );

        pb.redirectErrorStream(true); // 合并 stdout/stderr

        Process process = pb.start();

        // 必须读取输出流，否则可能阻塞
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        System.out.println("FFmpeg 退出码: " + exitCode);
    }
}
