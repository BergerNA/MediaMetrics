package ru.berni.mediametrics.dataBase;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Item;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Database info
    private static final String DB_NAME = "DBMediaMetrics";
    private static final int DB_VERSION = 1;

    //Table Names
    private static final String TABLE_CHANNELS = "CHANNEL";
    private static final String TABLE_ITEMS = "ITEM";

    // Channel table Columns
    private static final String KEY_CHANNEL_ID = "_id";
    private static final String KEY_CHANNEL_TITLE = "title";
    private static final String KEY_CHANNEL_URL = "url";
    private static final String KEY_CHANNEL_LAST_DATE_UPDATE = "lastDateUpdate";

    // Items table Columns
    private static final String KEY_ITEM_ID = "_id";
    private static final String KEY_ITEM_TITLE = "title";
    private static final String KEY_ITEM_URL = "url";
    private static final String KEY_ITEM_DATE = "pubDate";
    private static final String KEY_ITEM_DESCRIPTION = "description";
    private static final String KEY_ITEM_CONTENT = "content";
    private static final String KEY_ITEM_IS_READ = "isRead";
    private static final String KEY_ITEM_IMAGE_PATH = "imagePath";
    private static final String KEY_ITEM_FK_ID_CHANNEL = "fk_id_Channel";

    // Result of a failed database transaction
    private final static long DB_FAILED_TRANSACTION = -1;

    private static final String LOG_TAG = "DatabaseHelper:";

    private static volatile DatabaseHelper dbInstance;

    public static DatabaseHelper getInstance(final Context context) {
        DatabaseHelper localDBInstance = dbInstance;
        if(localDBInstance == null){
            synchronized (DatabaseHelper.class){
                localDBInstance = dbInstance;
                if(localDBInstance == null){
                    dbInstance = localDBInstance = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return localDBInstance;
    }

    private DatabaseHelper(final Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(final SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        final String CREATE_CHANNELS_TABLE = "CREATE TABLE " + TABLE_CHANNELS +
                "(" +
                KEY_CHANNEL_ID + " INTEGER PRIMARY KEY," +
                KEY_CHANNEL_TITLE + " TEXT NOT NULL," +
                KEY_CHANNEL_URL + " TEXT NOT NULL UNIQUE," +
                KEY_CHANNEL_LAST_DATE_UPDATE + " NUMERIC NOT NULL " +
                ")";

        final String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS +
                "(" +
                KEY_ITEM_ID + " INTEGER PRIMARY KEY," +
                KEY_ITEM_TITLE + " TEXT NOT NULL," +
                KEY_ITEM_URL + " TEXT NOT NULL UNIQUE," +
                KEY_ITEM_DATE + " NUMERIC NOT NULL," +
                KEY_ITEM_DESCRIPTION + " TEXT NOT NULL," +
                KEY_ITEM_CONTENT + " TEXT," +
                KEY_ITEM_IS_READ + " NUMERIC NOT NULL," +
                KEY_ITEM_IMAGE_PATH + " TEXT," +
                KEY_ITEM_FK_ID_CHANNEL + " INTEGER NOT NULL," +
                " FOREIGN KEY (" + KEY_ITEM_FK_ID_CHANNEL + ")" +
                " REFERENCES " + TABLE_CHANNELS + "(" + KEY_CHANNEL_ID + ")" +
                ")";
        db.execSQL(CREATE_CHANNELS_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANNELS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            onCreate(db);
        }
    }

    public long addChannel(final Channel channel) {
        boolean result = channel.getTitle() == null;
        result &= channel.getDate() == null;
        result &= channel.getUrl() == null;
        if (result) {
            return DB_FAILED_TRANSACTION;
        }
        final SQLiteDatabase db = this.getWritableDatabase();
        long channelId = DB_FAILED_TRANSACTION;
        try {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_CHANNEL_TITLE, channel.getTitle());
            contentValues.put(KEY_CHANNEL_URL, channel.getUrl());
            contentValues.put(KEY_CHANNEL_LAST_DATE_UPDATE, channel.getDate().getTime());
            channelId = db.insertOrThrow(TABLE_CHANNELS, null, contentValues);
            channel.setId(channelId);
        } catch (final SQLiteException exc) {
            if (exc.getMessage().contains("UNIQUE")){
                channelId = getChannelId(channel);
            }
            Log.d(LOG_TAG, "Error while add channel to database.");
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return channelId;
    }

    public void addChannelItems(final Channel channel) {
        if (channel.getListItem() == null) {
            return;
        }
        final SQLiteDatabase db = this.getWritableDatabase();
        for (final Item item : channel.getListItem()) {
            boolean error = item.getTitle() == null;
            error &= item.getDate() == null;
            error &= item.getUrl() == null;
            error &= item.getDescription() == null;
            error &= item.getFkIdChannel() == DB_FAILED_TRANSACTION;

            if (error) {
                continue;
            }

            final long itemId;
            try {
                final ContentValues contentValues = new ContentValues();
                contentValues.put(KEY_ITEM_TITLE, item.getTitle());
                contentValues.put(KEY_ITEM_URL, item.getUrl());
                contentValues.put(KEY_ITEM_DATE, item.getDate().getTime());
                contentValues.put(KEY_ITEM_DESCRIPTION, item.getDescription());
                contentValues.put(KEY_ITEM_CONTENT, item.getContent());
                contentValues.put(KEY_ITEM_IMAGE_PATH, item.getImagePath());
                contentValues.put(KEY_ITEM_IS_READ, item.getIsRead() ? 1 : 0);
                contentValues.put(KEY_ITEM_FK_ID_CHANNEL, channel.getId());

                itemId = db.insertOrThrow(TABLE_ITEMS, null, contentValues);
                item.setId(itemId);
            } catch (final SQLiteException exc) {
                Log.d(LOG_TAG, "Error while add item to database.");
            }
        }
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public long addItem(final Item item) {
        boolean error = item.getTitle() == null;
        error &= item.getDate() == null;
        error &= item.getUrl() == null;
        error &= item.getDescription() == null;
        error &= item.getFkIdChannel() == DB_FAILED_TRANSACTION;

        if (error) {
            return DB_FAILED_TRANSACTION;
        }
        final SQLiteDatabase db = this.getWritableDatabase();
        long itemId = DB_FAILED_TRANSACTION;
        try {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_ITEM_TITLE, item.getTitle());
            contentValues.put(KEY_ITEM_URL, item.getUrl());
            contentValues.put(KEY_ITEM_DATE, item.getDate().getTime());
            contentValues.put(KEY_ITEM_DESCRIPTION, item.getDescription());
            contentValues.put(KEY_ITEM_CONTENT, item.getContent());
            contentValues.put(KEY_ITEM_IMAGE_PATH, item.getImagePath());
            contentValues.put(KEY_ITEM_IS_READ, item.getIsRead() ? 1 : 0);
            contentValues.put(KEY_ITEM_FK_ID_CHANNEL, item.getFkIdChannel());

            itemId = db.insertOrThrow(TABLE_ITEMS, null, contentValues);
            item.setId(itemId);
        } catch (final SQLiteException exc) {
            Log.d(LOG_TAG, "Error while add item to database.");
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return itemId;
    }

    public ArrayList<Channel> getAllChannel() {
        final ArrayList<Channel> listChannel = new ArrayList<>();

        final String CHANNELS_SELECT_QUERY = "SELECT * FROM " + TABLE_CHANNELS;
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery(CHANNELS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    final Channel channel = new Channel();
                    setChannelFromDB(cursor, channel);
                    listChannel.add(channel);
                } while (cursor.moveToNext());
            }
        } catch (final Exception exc) {
            Log.d(LOG_TAG, "Error while trying to get channels from database.");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return listChannel;
    }

    private long getChannelId(final Channel channel) {
        if (channel == null){
            return DB_FAILED_TRANSACTION;
        }
        long result = DB_FAILED_TRANSACTION;
        final String ITEM_SELECT_CHANNEL_URL = String.format("SELECT %s FROM %s WHERE %s = \"%s\"",
                KEY_CHANNEL_ID, TABLE_CHANNELS, KEY_CHANNEL_URL, channel.getUrl());
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery(ITEM_SELECT_CHANNEL_URL, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    result = cursor.getLong(cursor.getColumnIndex(KEY_CHANNEL_ID));
                } while (cursor.moveToNext());
            }
        } catch (final Exception exc) {
            Log.d(LOG_TAG, "Error while trying to get channels from database.");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    public ArrayList<Item> getAllItem() {
        final ArrayList<Item> listItem = new ArrayList<>();

        final String ITEMS_SELECT_QUERY = "SELECT * FROM " + TABLE_ITEMS + " ORDER BY " + KEY_ITEM_DATE + " DESC";
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery(ITEMS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    final Item item = new Item();
                    setItemFromDB(cursor, item);
                    listItem.add(item);
                } while (cursor.moveToNext());
            }
        } catch (final Exception exc) {
            Log.d(LOG_TAG, "Error while trying to get items from database.");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return listItem;
    }

    public ArrayList<Item> getItem(final Channel channel) {
        if (channel == null){
            return null;
        }
        final ArrayList<Item> listItem = new ArrayList<>();
        final String ITEM_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = %s ORDER BY %s DESC",
                TABLE_ITEMS, KEY_ITEM_FK_ID_CHANNEL, channel.getId(), KEY_ITEM_DATE);
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery(ITEM_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    final Item item = new Item();
                    setItemFromDB(cursor, item);
                    listItem.add(item);
                } while (cursor.moveToNext());
            }
        } catch (final Throwable exc) {
            Log.d(LOG_TAG, "Error while trying to get items from database.");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return listItem;
    }

    public Item getItem(final long idItem) {
        final String ITEM_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = %s",
                TABLE_ITEMS, KEY_ITEM_ID, idItem);
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.rawQuery(ITEM_SELECT_QUERY, null);
        final Item item = new Item();
        try {
            if (cursor.moveToFirst()) {
                setItemFromDB(cursor, item);
            }
        } catch (final Throwable exc) {
            Log.d(LOG_TAG, "Error while trying to get items from database.");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return item;
    }

    public void clearDB() {
        final SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TABLE_ITEMS, null, null);
            db.delete(TABLE_CHANNELS, null, null);
        } catch (final Exception exc) {
            Log.d(LOG_TAG, "Error while trying to delete all channels and items.");
        }
    }

    private void setItemFromDB(final Cursor cursor, final Item item){
        item.setId(cursor.getLong(cursor.getColumnIndex(KEY_ITEM_ID)));
        final long secTime = cursor.getLong(cursor.getColumnIndex(KEY_ITEM_DATE));
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        final Date dateRealize = new Date(secTime);
        item.setDate(dateRealize);
        item.setUrl(cursor.getString(cursor.getColumnIndex(KEY_ITEM_URL)));
        item.setTitle(cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)));
        item.setFkIdChannel(cursor.getLong(cursor.getColumnIndex(KEY_ITEM_FK_ID_CHANNEL)));
        item.setContent(cursor.getString(cursor.getColumnIndex(KEY_ITEM_CONTENT)));
        item.setDescription(cursor.getString(cursor.getColumnIndex(KEY_ITEM_DESCRIPTION)));
        item.setIsRead(cursor.getInt(cursor.getColumnIndex(KEY_ITEM_IS_READ)));
        item.setImagePath(cursor.getString(cursor.getColumnIndex(KEY_ITEM_IMAGE_PATH)));
    }

    private void setChannelFromDB(final Cursor cursor,final Channel channel){
        channel.setId(cursor.getLong(cursor.getColumnIndex(KEY_CHANNEL_ID)));
        channel.setDate(new Date(cursor.getLong(cursor.getColumnIndex(KEY_CHANNEL_LAST_DATE_UPDATE))));
        channel.setTitle(cursor.getString(cursor.getColumnIndex(KEY_CHANNEL_TITLE)));
        channel.setUrl(cursor.getString(cursor.getColumnIndex(KEY_CHANNEL_URL)));
    }
}
