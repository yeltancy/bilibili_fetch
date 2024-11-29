package org.example.model;

public class VideoInfo {
    private String title;//标题
    private String videoURL;//视频链接
    private String duration;//时长
    private String author;//作者
    private String releaseTime;//发布时间
    private String imgURL;//图片链接
    private String comment;//评论数
    private String like;//点赞数
    private String share;//转发人数
    private String favorite;//收藏人数

    public VideoInfo() {
    }

    public VideoInfo(String title, String videoURL, String duration, String author, String releaseTime, String imgURL, String comment, String like, String share, String favorite) {
        this.title = title;
        this.videoURL = videoURL;
        this.duration = duration;
        this.author = author;
        this.releaseTime = releaseTime;
        this.imgURL = imgURL;
        this.comment = comment;
        this.like = like;
        this.share = share;
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "title='" + title + '\'' +
                ", videoURL='" + videoURL + '\'' +
                ", duration='" + duration + '\'' +
                ", author='" + author + '\'' +
                ", releaseTime='" + releaseTime + '\'' +
                ", imgURL='" + imgURL + '\'' +
                ", comment='" + comment + '\'' +
                ", like='" + like + '\'' +
                ", share='" + share + '\'' +
                ", favorite='" + favorite + '\'' +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public String getFavorite() {
        return favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }

}
