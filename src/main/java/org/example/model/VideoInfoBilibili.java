package org.example.model;

public class VideoInfoBilibili {
    private VideoInfo videoInfo;
    private String coin;//投硬币枚数
    private String danmaku;//弹幕量
    private String viewCounts;//播放量
    private String aid;//aid
    private String cid;//cid

    public VideoInfoBilibili() {
    }

    public VideoInfoBilibili(VideoInfo videoInfo, String coin, String danmaku, String viewCounts, String aid, String cid) {
        this.videoInfo = videoInfo;
        this.coin = coin;
        this.danmaku = danmaku;
        this.viewCounts = viewCounts;
        this.aid = aid;
        this.cid = cid;
    }
    @Override
    public String toString() {
        return "VideoInfo{" +
                "viewCounts='" + viewCounts + '\'' +
                ", comments='" + videoInfo.getComment() + '\'' +
                ", duration='" + videoInfo.getDuration() + '\'' +
                ", VideoURL='" + videoInfo.getVideoURL() + '\'' +
                ", title='" + videoInfo.getTitle() + '\'' +
                ", author='" + videoInfo.getAuthor() + '\'' +
                ", releaseTime='" + videoInfo.getReleaseTime() + '\'' +
                ", imgURL='" + videoInfo.getImgURL() + '\'' +
                ", danmaku='" + danmaku + '\'' +
                ", like='" + videoInfo.getLike() + '\'' +
                ", coin='" + coin + '\'' +
                ", favorite='" + videoInfo.getFavorite() + '\'' +
                ", share='" + videoInfo.getShare() + '\'' +
                ", aid='" + aid + '\'' +
                ", cid='" + cid + '\'' +
                '}';
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getDanmaku() {
        return danmaku;
    }

    public void setDanmaku(String danmaku) {
        this.danmaku = danmaku;
    }

    public String getViewCounts() {
        return viewCounts;
    }

    public void setViewCounts(String viewCounts) {
        this.viewCounts = viewCounts;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }
}
