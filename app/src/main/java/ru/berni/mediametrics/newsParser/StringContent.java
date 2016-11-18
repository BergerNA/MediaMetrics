package ru.berni.mediametrics.newsParser;

class StringContent {

    enum ContentType {text, html}

    private ContentType contentType = ContentType.text;
    private String content = "";

    StringContent(){}

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(final ContentType type) {
        this.contentType = type;
    }
}
