package ru.berni.mediametrics.newsParser;

public abstract class RssUpdateListener{
    public enum EntityType {
        CHANNEL, ITEM
    }
    private EntityType entityType;

    protected RssUpdateListener(final EntityType entityType){
        this.entityType = entityType;
    }

    public abstract void onUpdate();

    public EntityType getType(){return entityType;}
}
