package ru.berni.mediametrics.newsParser;

import java.util.Date;

public class Item implements RssEntity{

    private long id;
    private final StringContent title;
    private String url;
    private Date date;
    private final StringContent description;
    private final StringContent content;
    private String imagePath;
    private boolean isRead;
    private long fkIdChannel;

    public Item() {
        title = new StringContent();
        description = new StringContent();
        url = null;
        content = new StringContent();
        imagePath = null;
        isRead = false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(final Object obj) {
        return false;
    }

    @Override
    public String toString() {
        return title.getContent();
    }

    @Override
    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void setDescription(final String description) {
        this.description.setContent(description);
    }

    public String getDescription() {
        return description.getContent();
    }

    public void setContent(final String content) {
        this.content.setContent(content);
    }

    public String getContent() {
        return content.getContent();
    }

    @Override
    public void setDate(final Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setImagePath(final String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(final boolean read) {
        isRead = read;
    }

    public void setIsRead(final int read) {
        isRead = read == 1;
    }

    public void setTitle(final String title) {
        this.title.setContent(title);
    }

    public String getTitle() {
        return title.getContent();
    }

    public void setId(final long id) {this.id = id;}

    public long getId() { return id;}

    public void setFkIdChannel(final long fkIdChannel) {
        this.fkIdChannel = fkIdChannel;
    }

    public long getFkIdChannel() {
        return fkIdChannel;
    }
}
