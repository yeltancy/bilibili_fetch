package org.example.controller;


import com.google.gson.JsonObject;
import org.example.tool.Constants;
import org.example.tool.Tools;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//爬取弹幕到csv文件，文件路径默认为当前项目的根目录下的store文件夹的danmu文件夹下
public class CrawlingDanmuFromBilibili {
    //爬取弹幕到csv文件
    //url:视频bv号链接，如"https://www.bilibili.com/video/BV1saSHYNEzb"
    //targetPath:下载弹幕存放路径，如"C:\\Users\\25220\\Desktop\\其他"，后面不需要加\\
    public void fetchDanmuToCSV(String url,String targetPath) {
        try {
            String danmuURL = getDanMuURL(url);
            Document document = Tools.getDocument(danmuURL, Constants.USER_AGENT, Constants.BILIBILI_REFERER, Constants.BILIBILI_COOKIE);
            String regex = "<d[^>]*>(.*?)</d>";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(document.html());
            List<String> results = new ArrayList<>();
            while (matcher.find()) {
                results.add(matcher.group(1));
            }
            String title = url.split("/")[4];
            String fileName = title + ".csv";
            File file = new File(targetPath, fileName);
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
                for (int i = 0; i < results.size(); i++) {
                    String line = results.get(i) + "\n";
                    writer.write(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDanMuURL(String url) {
        try {
            Document document = Tools.getDocument(url, Constants.USER_AGENT, Constants.BILIBILI_REFERER, Constants.BILIBILI_COOKIE);
            String regex = "window\\.\\__INITIAL_STATE__\\s*=\\s*(\\{.*?\\});";
            JsonObject jsonResponse = Tools.getJsonResponse(document, regex);
            String cid = jsonResponse.get("cid").getAsString();
            return Constants.BILIBILI_DANMU_API + cid + ".xml";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        CrawlingDanmuFromBilibili crawlingDanmuFromBilibili = new CrawlingDanmuFromBilibili();
        String targetPath;
        Path path;
        do {
            targetPath = Tools.selectPath() == "" ? Constants.BASE_PATH + "\\danmu" : Tools.selectPath();
            path = Paths.get(targetPath);
        } while (!Files.exists(path));
        crawlingDanmuFromBilibili.fetchDanmuToCSV("https://www.bilibili.com/video/BV1saSHYNEzb",targetPath);
    }
}
