package ru.berni.mediametrics.newsParser;

import java.util.ArrayList;
import java.util.Date;

public class Channel implements RssEntity {

    private ArrayList<Item> listItem = new ArrayList<>();
    private final StringContent title;
    private String url;
    private String description;
    private Date date;
    private long id;

    public Channel(){
        title = new StringContent();
        url = null;
        description = null;
        date = new Date();
    }

    void addItem(final Item item){
        listItem.add(item);
    }

    public ArrayList<Item> getListItem(){
        return listItem;
    }

    public void setListItem(final ArrayList<Item> items){
        listItem = items;
    }

    @Override
    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUrl() {return url;}

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setDate(final Date date) {
        this.date = date;
    }

    public Date getDate() {return date;}

    public void setTitle(final String title) {
        this.title.setContent(title);
    }

    public String getTitle(){
        return title.getContent();
    }

    public void setId(final long id) {this.id = id;}

    public long getId(){
        return id;
    }

    @Override
    public String toString() {
        return title.getContent();
    }
}
