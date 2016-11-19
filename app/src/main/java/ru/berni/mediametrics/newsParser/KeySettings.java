package ru.berni.mediametrics.newsParser;

class KeySettings {
    private String rel;
    private String href;
    private StringContent.ContentType type;

    private final static String TEXT = "text";
    private final static String HTML = "html";

    KeySettings() {
        rel = null;
        href = null;
        type = null;
    }

    String getRel() {
        return rel;
    }

    void setRel(final String rel) {
        this.rel = rel;
    }

    String getHref() {
        return href;
    }

    void setHref(final String href) {
        this.href = href;
    }

    public StringContent.ContentType getType() {
        return type;
    }

    void setType(final String type) {
        if (type.equals(HTML)) {
            this.type = StringContent.ContentType.html;
        } else {
            this.type = StringContent.ContentType.text;
        }
    }
}
