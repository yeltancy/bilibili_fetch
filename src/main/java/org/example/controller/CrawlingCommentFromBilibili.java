package org.example.controller;

import com.google.gson.*;
import org.example.tool.Tools;
import org.example.tool.Constants;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

//爬取评论到csv文件，文件路径默认为当前项目的根目录下的store文件夹的comment文件夹下
public class CrawlingCommentFromBilibili {
    //获取一级评论
    //url:视频bv号链接，如"https://www.bilibili.com/video/BV1svSdYcE7z"
    //paginationStr:评论分页信息,从getWRid获取，无需关心
    public void fetchFirstComment(String url, String paginationStr, String targetPath) throws Exception {
        String oid = getOid(url);
        String wRid = getWRid(oid, paginationStr);
        String params = "oid=" + oid + "&type=1&mode=3&pagination_str=" + URLEncoder.encode(paginationStr, StandardCharsets.UTF_8.toString()) + "&plat=1&web_location=1315875&w_rid=" + wRid + "&wts=" + System.currentTimeMillis() / 1000 + "";
        String commentURL = Constants.BILIBILI_COMMENT_API + "/wbi/main?" + params;
        Document document = Tools.getDocument(commentURL, Constants.USER_AGENT, Constants.BILIBILI_REFERER, Constants.BILIBILI_COOKIE);
        JsonObject jsonResponse = Tools.getJsonResponse(document);
        JsonObject data = jsonResponse.getAsJsonObject("data");
        if (data == null) {
            System.out.println("data为空");
            return;
        }
        JsonArray replies = data.getAsJsonArray("replies");
        if (replies == null || replies.size() == 0) {
            System.out.println("replies为null或者replies.size()为0");
            return;
        }
        List<String> commentList = new ArrayList<>();
        for (int i = 0; i < replies.size(); i++) {
            if (replies.get(i).isJsonNull()) {
                continue;
            }
            JsonObject reply = replies.get(i).getAsJsonObject();
            String rpid = reply.get("rpid").getAsString();
            String uname = reply.getAsJsonObject("member").get("uname").getAsString();
            String message = reply.getAsJsonObject("content").get("message").getAsString();
            // 输出当前评论，检查 uname 和 message
            if (uname != null && !uname.isEmpty() && message != null && !message.isEmpty()) {
                commentList.add(uname + ": " + message);
            } else {
                System.out.println("Received invalid reply data.");
            }
            if (rpid != null && !rpid.isEmpty()) {
                fetchSecondComment(oid, rpid, 1, commentList);
            }
        }
        downloadToCSV(url.split("/")[4], commentList, targetPath);
        JsonObject cursor = data.getAsJsonObject("cursor");
        if (cursor == null) {
            return;
        }
        JsonObject paginationReply = cursor.getAsJsonObject("pagination_reply");
        if (paginationReply == null) {
            return;
        }
        String next_offset = "";
        if (paginationReply.has("next_offset") && !paginationReply.get("next_offset").isJsonNull()) {
            next_offset = paginationReply.get("next_offset").getAsString();
        }

        if (next_offset == null || next_offset.isEmpty()) {
            return;
        }
        next_offset = next_offset.replace("\"", "\\\"");
        paginationStr = String.format("{\"offset\":\"%s\"}", next_offset);
        fetchFirstComment(url, paginationStr, targetPath);
    }

    //获取oid
    public String getOid(String videoURL) throws IOException {
        Document document = Tools.getDocument(videoURL, Constants.USER_AGENT, Constants.BILIBILI_REFERER, Constants.BILIBILI_COOKIE);
        String regex = "window\\.\\__INITIAL_STATE__\\s*=\\s*(\\{.*?\\});";
        JsonObject jsonResponse = Tools.getJsonResponse(document, regex);
        String oid = jsonResponse.get("aid").getAsString();
        return oid;
    }

    //获取wRid
    public String getWRid(String oid, String paginationStr) throws Exception {
        if (paginationStr == null || paginationStr.isEmpty() || paginationStr.equals(Constants.BILIBILI_OFFSET)) {
            paginationStr = URLEncoder.encode(Constants.BILIBILI_OFFSET, StandardCharsets.UTF_8.toString());
        } else {
            paginationStr = URLEncoder.encode(paginationStr, StandardCharsets.UTF_8.toString());
        }
        List<String> params = new ArrayList<>();
        params.add("mode=3");
        params.add("oid=" + oid);
        params.add("pagination_str=" + paginationStr);
        params.add("plat=1");
        params.add("type=1");
        params.add("web_location=1315875");
        params.add("wts=" + System.currentTimeMillis() / 1000);
        String y = String.join("&", params);
        String data = y + Constants.A;
        String wRid = generateMD5(data);
        return wRid;
    }

    //获取二级评论
    private void fetchSecondComment(String oid, String uniqueRoot, int pn, List<String> commentList) {
        String secondCommentURL = Constants.BILIBILI_COMMENT_API + "/reply?oid=" + oid + "&type=1&root=" + uniqueRoot + "&ps=10&pn=" + pn + "&web_location=333.788";
        Document document;
        try {
            document = Tools.getDocument(secondCommentURL, Constants.USER_AGENT, Constants.BILIBILI_REFERER, Constants.BILIBILI_COOKIE);
        } catch (IOException e) {
            return;
        }
        JsonObject jsonResponse = Tools.getJsonResponse(document);
        if (jsonResponse.has("data") && !jsonResponse.get("data").isJsonNull()) {
            JsonObject data = jsonResponse.getAsJsonObject("data");
            int count = data.getAsJsonObject("page").get("count").getAsInt();
            JsonArray replies = data.getAsJsonArray("replies");
            for (int i = 0; i < replies.size(); i++) {
                JsonObject subReply = replies.get(i).getAsJsonObject();
                String username = subReply.getAsJsonObject("member").get("uname").getAsString();
                String message = subReply.getAsJsonObject("content").get("message").getAsString();
                commentList.add(" ".repeat(4) + username + ": " + message);
            }
            if (count > 10) {
                int totalPage = count / 10 + 1;
                if (pn < totalPage) {
                    fetchSecondComment(oid, uniqueRoot, pn + 1, commentList);
                }
            }
        }
    }

    private String generateMD5(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void downloadToCSV(String title, List<String> results, String targetPath) {
        try {
            String fileName = title + ".csv";
            File file = new File(targetPath, fileName);
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8")) {
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

    public static void main(String[] args) throws Exception {
        CrawlingCommentFromBilibili crawling = new CrawlingCommentFromBilibili();
        String targetPath;
        Path path;
        do {
            targetPath = Tools.selectPath() == "" ? Constants.BASE_PATH + "\\comment" : Tools.selectPath();
            path = Paths.get(targetPath);
        } while (!Files.exists(path));
        crawling.fetchFirstComment("https://www.bilibili.com/video/BV16gStYCEp8", Constants.BILIBILI_OFFSET, targetPath);
    }
}