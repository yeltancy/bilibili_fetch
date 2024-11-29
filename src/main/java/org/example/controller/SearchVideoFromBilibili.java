package org.example.controller;

import com.google.gson.JsonObject;
import org.example.SearchVideoFromWeb;
import org.example.model.VideoInfo;
import org.example.model.VideoInfoBilibili;
import org.example.tool.Constants;
import org.example.tool.Tools;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//搜索视频
public class SearchVideoFromBilibili implements SearchVideoFromWeb<VideoInfoBilibili> {
    //type :综合（all）、视频（video）、番剧（bangumi）、影视（pgc）、专栏（article）、直播（live）、用户（upuser）
    //keyword :搜索关键词
    //order :综合排序（null）、最多播放（click）、最新发布（pubdate）、最多弹幕（dm）、最多收藏（stow）
    //maxCount :返回视频爬取最大数量
    //sortKeyword :排序关键词(大小写不重要) viewCounts, comments, releaseTime, danmaku, like, coin, favorite, share
    //sortOrder :排序顺序 true 升序，false 降序
    public List<VideoInfoBilibili> getSortedVideo(String type, String keyword, String order, int maxCount, String sortKeyword, boolean sortOrder) {
        List<VideoInfoBilibili> videoInfoBilibilis = searchVideo(type, keyword, order, maxCount);
//        Tools.sortVideoInfos(videoInfoBilibilis, sortKeyword, sortOrder);
        return videoInfoBilibilis;
    }

    @Override
    public List<VideoInfoBilibili> searchVideo(String type, String keyword, String order, int maxCount) {
        String BasicURL = getBilibiliSearchURL(type, keyword, order);
        List<VideoInfoBilibili> videoInfoBilibilis = new ArrayList<>();
        try {
            Document document = Tools.getDocument(BasicURL, Constants.USER_AGENT, Constants.BILIBILI_REFERER, Constants.BILIBILI_COOKIE);
            Elements URLElements = document.select("div.bili-video-card__wrap.__scale-wrap");
            Elements imgElements = document.select("div.bili-video-card__image--wrap img");
            if (imgElements.isEmpty()) {
                System.out.println("没有找到图片。");
                return null;
            }
            if (URLElements.isEmpty()) {
                System.out.println("没有找到视频。");
                return null;
            }
            videoInfoBilibilis.addAll(IntStream.range(0, Math.min(URLElements.size(), maxCount))
                    .parallel()
                    .mapToObj(i -> {
                        Element URLElement = URLElements.get(i);
                        Element imgElement = imgElements.get(i);
                        Elements links = URLElement.select("a[href]");
                        if (links.isEmpty()) {
                            System.out.println("没有找到链接。");
                            return null;
                        }
                        if (links.size() < 3) {
                            System.out.println("links里面的元素少于3");
                            return null;
                        }
                        String[] linkTexts = links.get(0).text().split(" ");
                        StringBuilder videoUrlBuilder = new StringBuilder("https://")
                                .append(links.get(0).attr("href").substring(2));
                        String[] authorInfo = links.get(2).text().split(" · ");
                        StringBuilder imgUrlBuilder = new StringBuilder("https://")
                                .append(imgElement.attr("src").substring(2));

                        VideoInfo videoInfo = new VideoInfo();
                        videoInfo.setComment(linkTexts[1]);
                        videoInfo.setDuration(linkTexts[2]);
                        videoInfo.setTitle(links.get(1).text());
                        videoInfo.setAuthor(authorInfo[0]);
                        videoInfo.setReleaseTime(formatReleaseTime(authorInfo[1]));
                        videoInfo.setImgURL(imgUrlBuilder.toString());
                        videoInfo.setVideoURL(videoUrlBuilder.toString());

                        VideoInfoBilibili videoInfoBilibili = new VideoInfoBilibili();
                        videoInfoBilibili.setViewCounts(linkTexts[0]);
                        videoInfoBilibili.setVideoInfo(videoInfo);
                        getExtraAttrs(videoInfoBilibili);
                        return videoInfoBilibili;
                    })
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoInfoBilibilis;
    }

    //构造搜索URL
    public String getBilibiliSearchURL(String type, String keyword, String order) {
        StringBuilder urlBuilder = null;
        try {
            urlBuilder = new StringBuilder(Constants.BILIBILI_SEARCH_API)
                    .append(type)
                    .append("?keyword=")
                    .append(URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString()))
                    .append("&search_source=1&order=")
                    .append(order);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return urlBuilder.toString();
    }

    //格式化发布时间
    public String formatReleaseTime(String releaseTime) {
        String originalDate = "2019-4-6";
        LocalDate date = LocalDate.parse(originalDate, DateTimeFormatter.ofPattern("yyyy-M-d"));
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = date.format(targetFormatter);
        return formattedDate;
    }

    //获取额外属性 like,coin,danmaku,dislike,favorite,share
    public void getExtraAttrs(VideoInfoBilibili videoInfoBilibili) {
        try {
            Document document = Tools.getDocument(videoInfoBilibili.getVideoInfo().getVideoURL(), Constants.USER_AGENT, Constants.BILIBILI_REFERER, Constants.BILIBILI_COOKIE);
            String regex = "window\\.\\__INITIAL_STATE__\\s*=\\s*(\\{.*?\\});";
            JsonObject jsonResponse = Tools.getJsonResponse(document, regex);
            if (jsonResponse == null) {
                System.out.println("jsonResponse为空");
                return;
            }
            String aid = jsonResponse.get("aid").getAsString();
            if (aid == null || aid.isEmpty()) {
                System.out.println("aid为空");
                return;
            }
            String cid = jsonResponse.get("cid").getAsString();
            if (cid == null || cid.isEmpty()) {
                System.out.println("cid为空");
                return;
            }
            JsonObject videoData = jsonResponse.getAsJsonObject("videoData");
            if (videoData == null) {
                System.out.println("videoData为空");
                return;
            }
            JsonObject stat = videoData.getAsJsonObject("stat");
            if (stat == null) {
                System.out.println("stat为空");
                return;
            }
            videoInfoBilibili.setAid(aid);
            videoInfoBilibili.setCid(cid);
            videoInfoBilibili.getVideoInfo().setLike(stat.get("like").getAsString());
            videoInfoBilibili.setCoin(stat.get("coin").getAsString() == null ? "未知" : stat.get("coin").getAsString());
            videoInfoBilibili.setDanmaku(stat.get("danmaku").getAsString() == null ? "未知" : stat.get("danmaku").getAsString());
            videoInfoBilibili.getVideoInfo().setFavorite(stat.get("favorite").getAsString() == null ? "未知" : stat.get("favorite").getAsString());
            videoInfoBilibili.getVideoInfo().setShare(stat.get("share").getAsString() == null ? "未知" : stat.get("share").getAsString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) {
        SearchVideoFromBilibili searchVideoFromBilibili = new SearchVideoFromBilibili();
        //运行时间
        long startTime = System.currentTimeMillis();
        List<VideoInfoBilibili> videoInfoBilibilis = searchVideoFromBilibili.getSortedVideo("video", "simple", "click", 10, "viewCounts", false);
        if (videoInfoBilibilis == null) {
            System.out.println("出错了。");
            return;
        }
        for (int i = 0; i < videoInfoBilibilis.size(); i++) {
            System.out.println(videoInfoBilibilis.get(i));
        }
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
    }
}
