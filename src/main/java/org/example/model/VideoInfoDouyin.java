package org.example.model;
public class VideoInfoDouyin {
    private VideoInfo videoInfo;
    private String share_url;

    public VideoInfoDouyin() {
    }

    public VideoInfoDouyin(VideoInfo videoInfo, String share_url) {
        this.videoInfo = videoInfo;
        this.share_url = share_url;
    }

    public String getShare_url() {
        return share_url;
    }

    public void setShare_url(String share_url) {
        this.share_url = share_url;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }
    @Override
    public String toString() {
        return "VideoInfo{" +
                "comments='" + videoInfo.getComment() + '\'' +
                ", duration='" + videoInfo.getDuration() + '\'' +
                ", VideoURL='" + videoInfo.getVideoURL() + '\'' +
                ", title='" + videoInfo.getTitle() + '\'' +
                ", author='" + videoInfo.getAuthor() + '\'' +
                ", releaseTime='" + videoInfo.getReleaseTime() + '\'' +
                ", imgURL='" + videoInfo.getImgURL() + '\'' +
                ", like='" + videoInfo.getLike() + '\'' +
                ", favorite='" + videoInfo.getFavorite() + '\'' +
                ", share='" + videoInfo.getShare() + '\'' +
                ", share_url='" + share_url + '\'' +
                '}';
    }
}
