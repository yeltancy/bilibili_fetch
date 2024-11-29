package org.example.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.tool.Constants;
import org.example.tool.ExecStream;
import org.example.tool.Tools;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//爬取b站视频，保存地址可自己设置
// 单独的音频和视频文件存放在当前项目的根目录下的store文件夹的video文件夹下
public class DownloadVideoFromBilibili {
    //url:视频bv号链接，如"https://www.bilibili.com/video/BV1svSdYcE7z"
    //targetPath:下载视频存放路径，如"C:\\Users\\25220\\Desktop\\其他"，后面不需要加\\
    //单独的音频和视频文件存放在当前项目的根目录下的store文件夹
    //运行完会有一个异常抛出，无影响
    public void downloadVideo(String url, String targetPath) {
        try {
            Document document = Tools.getDocument(url, Constants.USER_AGENT, Constants.BILIBILI_REFERER, Constants.BILIBILI_COOKIE);
            String regex = "window\\.__playinfo__=(.*?)</script>";
            JsonObject jsonResponse = Tools.getJsonResponse(document, regex);
            JsonObject data = jsonResponse.getAsJsonObject("data");
            if (data == null) {
                return;
            }
            JsonObject dash = data.getAsJsonObject("dash");
            if (dash == null) {
                return;
            }
            JsonArray video = dash.getAsJsonArray("video");
            if (video == null) {
                return;
            }
            String videoUrl = video.get(0).getAsJsonObject().get("baseUrl").getAsString();
//                System.out.println(videoUrl);
            JsonArray audio = dash.getAsJsonArray("audio");
            if (audio == null) {
                return;
            }
            String audioUrl = audio.get(0).getAsJsonObject().get("baseUrl").getAsString();
//                System.out.println(audioUrl);
            String title = url.split("/")[4];
            //单独的音频和视频文件存放在当前项目的根目录下的store文件夹的video文件夹下
            String basePath = Constants.BASE_PATH + "\\video";
            Path directoryPath = Paths.get(basePath, title);
            try {
                // 创建目录
                Files.createDirectories(directoryPath);
            } catch (Exception e) {
                System.err.println("创建目录时发生错误: " + e.getMessage());
            }
            String path = directoryPath + "\\" + title;
            Thread videoThread = new Thread(() -> getVideoOrAudio(videoUrl, path, ".mp4"));
            Thread audioThread = new Thread(() -> getVideoOrAudio(audioUrl, path, ".mp3"));
            videoThread.start();
            audioThread.start();
            try {
                videoThread.join();
                audioThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mergeVideoAndAudio(path + ".mp4", path + ".mp3", targetPath + "\\" + title + ".mp4");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载视频或音频
    //baseURL:视频或音频的url
    //path:存放路径
    //suffix:文件后缀,如.mp4、.mp3
    public void getVideoOrAudio(String baseURL, String path, String suffix) {
        try {
            URL url = new URL(baseURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setRequestProperty("Referer", Constants.BILIBILI_REFERER);
            connection.setRequestProperty("Cookie", Constants.BILIBILI_COOKIE);
            int responseCode = connection.getResponseCode();
            int fileSize = connection.getContentLength();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                File targetFile = new File(path + suffix);
                if (!targetFile.exists()) {
                    targetFile.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                int totalBytesRead = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (fileSize > 0) {
                        double progress = (double) totalBytesRead / fileSize * 100;
                        System.out.printf("\r下载进度: %.2f%%", progress);
                    }
                }
                fileOutputStream.close();
                inputStream.close();
            } else {
                System.out.println("下载失败，响应码：" + responseCode);
            }
        } catch (IOException e) {
            //这个报错无影响
            e.printStackTrace();
        }
    }

    public static void mergeVideoAndAudio(String videoPath, String audioPath, String targetPath) {
        String command = Constants.FFMPEG + " -i " + videoPath + " -i " + audioPath + " -codec copy " + targetPath;
//        System.out.println(command);
        Process process = null;
        try {
            //执行本地命令
            process = Runtime.getRuntime().exec(command);
            //因为process的输出流缓冲区很小，会导致程序阻塞，因此自己写个工具类对进程的输出流进行处理
            ExecStream stream = new ExecStream(process.getErrorStream(), "ERROR");
            stream.start();
            ExecStream stream1 = new ExecStream(process.getInputStream(), "STDOUT");
            stream1.start();
            //得到进程运行结束后的返回状态，如果进程未运行完毕则等待知道执行完毕，正确结束返回int型的0
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DownloadVideoFromBilibili downLoadVideoFromBilibili = new DownloadVideoFromBilibili();
        String targetPath;
        Path path;
        do {
            targetPath = Tools.selectPath() == "" ? Constants.BASE_PATH : Tools.selectPath();
            path = Paths.get(targetPath);
        } while (!Files.exists(path));
        downLoadVideoFromBilibili.downloadVideo("https://www.bilibili.com/video/BV1GsSxY4E41/", targetPath);
    }
}
