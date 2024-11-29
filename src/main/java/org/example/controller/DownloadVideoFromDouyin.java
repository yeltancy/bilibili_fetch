package org.example.controller;

import org.example.tool.Constants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadVideoFromDouyin {
    public static void main(String[] args) {
        String videoUrl = "https://v3-web.douyinvod.com/7e3e6e00d0f9e5b1ced1bd500cef00f4/67389f74/video/tos/cn/tos-cn-ve-15/90b75fbd006c463ebd562c0030e72bb9/?a=6383&ch=11&cr=3&dr=0&lr=all&cd=0%7C0%7C0%7C3&cv=1&br=1700&bt=1700&cs=0&ds=4&ft=pEaFx4hZffPdh6~kv1jNvAq-antLjrKwNQ_NRkaF8.~CljVhWL6&mime_type=video_mp4&qs=0&rc=NTZkPDozZGdlODdlZTg7N0BpM3VtZmU6ZnB1NjMzNGkzM0AxNC0wMC9jXzQxLmIwYV80YSNobDQxcjRfcF5gLS1kLS9zcw%3D%3D&btag=c0000e00028000&cquery=101n_100B_100D_102u_100o&dy_q=1731753202&l=20241116183321A757EE3DCDC07693D842";
        String saveFilePath = "downloaded_video.mp4";
        try {
            downloadVideoWithProgress(videoUrl, saveFilePath);
            System.out.println("\n视频下载完成！");
        } catch (IOException e) {
            System.out.println("下载失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void downloadVideoWithProgress(String videoUrl, String saveFilePath) throws IOException {
        URL url = new URL(videoUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
        connection.setRequestProperty("Referer", Constants.DOUYIN_REFERER);
        connection.setRequestProperty("Cookie", Constants.DOUYIN_COOKIE);
        int fileSize = connection.getContentLength();
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(saveFilePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            int totalBytesRead = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                if (fileSize > 0) {
                    double progress = (double) totalBytesRead / fileSize * 100;
                    System.out.printf("\r下载进度: %.2f%%", progress);
                }
            }
        } finally {
            connection.disconnect();
        }
    }
}