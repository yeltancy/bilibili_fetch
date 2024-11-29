package org.example.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.SearchVideoFromWeb;
import org.example.model.VideoInfo;
import org.example.model.VideoInfoDouyin;
import org.example.tool.Constants;
import org.example.tool.Tools;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchVideoFromDouyin implements SearchVideoFromWeb<VideoInfoDouyin> {

    //sort_type: 0:综合排序 1:最多播放 2:最新发布
    //keyword :搜索关键词
    //maxCount :返回视频爬取最大数量
    @Override
    public List<VideoInfoDouyin> searchVideo(String sortType, String keyword, String order, int maxCount) {
        List<VideoInfoDouyin> videoInfoDouyins = new ArrayList<>();
        String aid = getDouYinAid(keyword);
        if (aid == "") {
            System.out.println("aid获取失败");
            return null;
        }
        String BasicURL = getDouyinSearchURL(keyword, aid, 1, 15);
        try {
            Document document = Tools.getDocument(BasicURL, Constants.USER_AGENT, Constants.DOUYIN_REFERER, Constants.DOUYIN_COOKIE);
            JsonObject jsonResponse = Tools.getJsonResponse(document);
            JsonArray dataArray = jsonResponse.getAsJsonArray("data");
            if (dataArray == null) {
                System.out.println("data为空");
                return null;
            }
            //data:aweme_info:author:nickname:作者名
            //data:aweme_info:statistics:点赞数(digg_count),评论数(comment_count),转发数(share_count),收藏数(collect_count)
            //data:aweme_info:desc:标题
            //data:aweme_info:video:big_thumbs:duration:时长
            //data:aweme_info:video:cover:url_list:图片链接
            //data:aweme_info:video:play_addr:url_list:视频链接
            //data:aweme_info:create_time:发布时间
            //data:aweme_info:share_info:share_url:分享链接
            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject data = dataArray.get(i).getAsJsonObject();
                JsonObject awemeInfo = data.getAsJsonObject("aweme_info");
                if (awemeInfo == null) {
                    System.out.println("awemeInfo为空");
                    return null;
                }
                JsonObject author = awemeInfo.getAsJsonObject("author");
                if (author == null) {
                    System.out.println("author为空");
                    return null;
                }
                JsonObject statistics = awemeInfo.getAsJsonObject("statistics");
                if (statistics == null) {
                    System.out.println("statistics为空");
                    return null;
                }
                JsonObject video = awemeInfo.getAsJsonObject("video");
                if (video == null) {
                    System.out.println("video为空");
                    return null;
                }
                JsonObject music = awemeInfo.getAsJsonObject("music");
                if (music == null) {
                    System.out.println("music为空");
                }
                JsonObject cover = video.getAsJsonObject("cover");
                if (cover == null) {
                    System.out.println("cover为空");
                    return null;
                }
                JsonObject playAddr = video.getAsJsonObject("play_addr");
                if (playAddr == null) {
                    System.out.println("playAddr为空");
                    return null;
                }
                JsonObject shareInfo = awemeInfo.getAsJsonObject("share_info");
                if (shareInfo == null) {
                    System.out.println("shareInfo为空");
                    return null;
                }
                VideoInfoDouyin videoInfoDouyin = new VideoInfoDouyin();
                VideoInfo videoInfo = new VideoInfo();
                videoInfo.setAuthor(author.get("nickname").getAsString());
                videoInfo.setLike(statistics.get("digg_count").getAsString());
                videoInfo.setComment(statistics.get("comment_count").getAsString());
                videoInfo.setShare(statistics.get("share_count").getAsString());
                videoInfo.setFavorite(statistics.get("collect_count").getAsString());
                videoInfo.setTitle(awemeInfo.get("desc").getAsString());
                if (music == null) {
                    videoInfo.setDuration("未知");
                } else {
                    videoInfo.setDuration(FormatDuration(music.get("duration").getAsString()));
                }
                videoInfo.setImgURL(cover.get("url_list").getAsJsonArray().get(0).getAsString());
                videoInfo.setVideoURL(playAddr.get("url_list").getAsJsonArray().get(0).getAsString());
                videoInfo.setReleaseTime(FormatReleaseTime(awemeInfo.get("create_time").getAsString()));
                videoInfoDouyin.setShare_url(shareInfo.get("share_url").getAsString());
                videoInfoDouyin.setVideoInfo(videoInfo);
                videoInfoDouyins.add(videoInfoDouyin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoInfoDouyins;
    }

    //获取aid
    public String getDouYinAid(String keyword) {
        try {
            Document document = Tools.getDocument(Constants.DOUYIN_REFERER + keyword, Constants.USER_AGENT, Constants.DOUYIN_REFERER, Constants.DOUYIN_COOKIE);
            String documentContent = document.html();
            Pattern pattern = Pattern.compile("aid:\\s*(\\d+)\\s*,");
            Matcher matcher = pattern.matcher(documentContent);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //构造抖音搜索URL
    public String getDouyinSearchURL(String keyword, String aid, int sortType, int maxCount) {
        StringBuilder stringBuilder = new StringBuilder(Constants.DOUYIN_SEARCH_API)
                .append("device_platform=webapp")
                .append("&aid=").append(aid)
                .append("&search_channel=aweme_general")
                .append("&filter_selected=%7B%22sort_type%22%3A%22").append(sortType).append("%22%2C%22publish_time%22%3A%220%22%7D");
        try {
            stringBuilder.append("&keyword=").append(URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        stringBuilder.append("&search_source=tab_search");
        stringBuilder.append("&is_filter_search=1");
        stringBuilder.append("&count=").append(maxCount);
        return stringBuilder.toString();
    }

    //计算抖音视频发布时间
    //抖音的时间戳转换为日期时间
    public String FormatReleaseTime(String seconds) {
        Instant unixEpoch = Instant.EPOCH;
        Instant newTime = unixEpoch.plus(Duration.ofSeconds(Long.parseLong(seconds)));
        LocalDateTime dateTime = LocalDateTime.ofInstant(newTime, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return formatter.format(dateTime);
    }

    //计算抖音视频时长
    //将抖音的秒数转换为分秒
    public String FormatDuration(String seconds) {
        long second = Long.parseLong(seconds);
        long minute = second / 60;
        second = second % 60;
        return String.format("%02d:%02d", minute, second);
    }

    public static void main(String[] args) {
        SearchVideoFromDouyin searchVideoFromDouyin = new SearchVideoFromDouyin();
        //运行时间
        long startTime = System.currentTimeMillis();
        List<VideoInfoDouyin> videoInfoDouyins = searchVideoFromDouyin.searchVideo("video", "simple", "click", 10);
        for (VideoInfoDouyin videoInfoDouyin : videoInfoDouyins) {
            System.out.println(videoInfoDouyin);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
    }
}
