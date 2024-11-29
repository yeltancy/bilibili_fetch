package org.example.tool;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.example.model.VideoInfoBilibili;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
    //获取链接的Document对象
    public static Document getDocument(String url, String user_agent, String referer, String cookie) throws IOException {
        Jsoup.connect(url)
                .ignoreContentType(true)
                .header("User-Agent", user_agent);

        if (referer != null && !referer.isEmpty() && cookie != null && !cookie.isEmpty()) {
            return Jsoup.connect(url)
                    .ignoreContentType(true)
                    .header("User-Agent", user_agent)
                    .referrer(referer)
                    .cookie("Cookie", cookie)
                    .get();
        } else if (referer != null && !referer.isEmpty()) {
            return Jsoup.connect(url)
                    .ignoreContentType(true)
                    .header("User-Agent", user_agent)
                    .referrer(referer)
                    .get();
        } else {
            return Jsoup.connect(url)
                    .ignoreContentType(true)
                    .header("User-Agent", user_agent)
                    .get();
        }
    }

    //对爬取信息进行排序
    //keyword: viewCounts, comments, releaseTime, danmaku, like, coin, favorite, share
    //order: true 升序，false 降序
    public static void sortVideoInfos(List<VideoInfoBilibili> videoInfoBilibilis, String sortKeyword, boolean order) {
        Comparator<VideoInfoBilibili> comparator;

        switch (sortKeyword.toLowerCase()) {
            case "viewcounts":
                comparator = Comparator.comparingInt(v -> parseOrDefault(v.getViewCounts(), 0));
                break;
            case "comments":
                comparator = Comparator.comparingInt(v -> parseOrDefault(v.getVideoInfo().getComment(), 0));
                break;
            case "releasetime":
                comparator = Comparator.comparing(v -> v.getVideoInfo().getReleaseTime());
                break;
            case "danmaku":
                comparator = Comparator.comparingInt(v -> parseOrDefault(v.getDanmaku(), 0));
                break;
            case "like":
                comparator = Comparator.comparingInt(v -> parseOrDefault(v.getVideoInfo().getLike(), 0));
                break;
            case "coin":
                comparator = Comparator.comparingInt(v -> parseOrDefault(v.getCoin(), 0));
                break;
            case "favorite":
                comparator = Comparator.comparingInt(v -> parseOrDefault(v.getVideoInfo().getFavorite(), 0));
                break;
            case "share":
                comparator = Comparator.comparingInt(v -> parseOrDefault(v.getVideoInfo().getShare(), 0));
                break;
            default:
                throw new IllegalArgumentException("无效的关键词: " + sortKeyword);
        }

        if (!order) {
            comparator = comparator.reversed();
        }
        Collections.sort(videoInfoBilibilis, comparator);
    }
    //将字符串转换为整数，如果转换失败，则返回默认值
    public static int parseOrDefault(String str, int defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    //获取Document转换成的json数据
    public static JsonObject getJsonResponse(Document document, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(document.html());
        if (!matcher.find()) {
            System.out.println("没有找到 " + regex + " 数据");
            return null;
        }
        String jsonData = matcher.group(1);
        JsonObject jsonResponse = null;
        try {
            jsonResponse = JsonParser.parseString(jsonData).getAsJsonObject();
            return jsonResponse;
        } catch (JsonSyntaxException | IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static JsonObject getJsonResponse(Document document) {
        String jsonData = document.body().text();
        JsonObject jsonResponse = null;
        try {
            jsonResponse = JsonParser.parseString(jsonData).getAsJsonObject();
            return jsonResponse;
        } catch (JsonSyntaxException | IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }

    //选择存放地址，未选择则默认为当前项目的根目录下的store文件夹
    // 只允许选择目录，方便用户选择下载路径
    public static String selectPath(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(null); // 显示文件选择对话框
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedPath = fileChooser.getSelectedFile();
            return selectedPath.getAbsolutePath();
        }
        return "";
    }

}
