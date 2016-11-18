package ru.berni.mediametrics.newsParser;

import java.util.Date;

interface RssEntity {
    void setUrl(String url);
    void setDescription(String description);
    void setDate(Date date);
    void setTitle(String title);
}
