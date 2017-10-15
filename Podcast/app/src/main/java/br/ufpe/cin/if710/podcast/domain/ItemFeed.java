package br.ufpe.cin.if710.podcast.domain;

import java.io.Serializable;

public class ItemFeed implements Serializable {
    private final int id;
    private final String title;
    private final String link;
    private final String pubDate;
    private final String description;
    private final String downloadLink;
    private String downloadUri;
    private long downloadID;

    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink) {
        this.id = -1;
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
    }

    public ItemFeed(int id, String title, String link, String pubDate, String description, String downloadLink) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public long getDownloadID() {
        return downloadID;
    }

    public void setDownloadUri(String uri) {
        this.downloadUri = uri;
    }

    public void setDownloadID(long id) {
        this.downloadID = id;
    }

    @Override
    public String toString() {
        return title;
    }
}