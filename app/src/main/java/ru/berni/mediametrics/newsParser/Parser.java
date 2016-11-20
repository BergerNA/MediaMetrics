package ru.berni.mediametrics.newsParser;

import android.util.Log;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Parser {

    private final static String LOG_TAG = "Parser";
    private final static char STREAM_END = '\uFFFF';
    private final static String CHARSET = "charset=";
    private final static String ENCODING = "encoding=";
    private final static String XML_TAG_PREFERENCES = "<?xml";
    private final static String CONTENT_TYPE = "Content-Type";

    private final static char TAG_START = '<';
    private final static char TAG_END = '>';

    private final static String DATE_FORMATTER_RSS = "EEE, dd MMM yyyy HH:mm:ss Z";
    private final static String DATE_FORMATTER_ATOM = "yyyy-MM-dd'T'HH:mm:ss";
    private final static int CDATA_LENGTH_SKIP_CHAR = 9;

    private final static String CHANNEL = "channel";
    private final static String ITEM = "item";
    private final static String TITLE = "title";
    private final static String LINK = "link";
    private final static String DESCRIPTION = "description";
    private final static String DATE = "pubDate";

    private final static String FEED = "feed";
    private final static String ENTRY = "entry";
    private final static String PUBLISHED = "published";
    private final static String LAST_BUILD_DATE = "lastBuildDate";
    private final static String SUMMARY = "summary";
    private final static String CONTENT = "content";

    private final static String VALUE_TYPE = "type";
    private final static int VALUE_TYPE_LENGTH = 6;
    private final static String VALUE_REL = "rel";
    private final static int VALUE_REL_LENGTH = 5;
    private final static String VALUE_HREF = "href";
    private final static int VALUE_HREF_LENGTH = 6;

    private String charset = null;
    private String key = "";
    private final StringBuilder stringValue = new StringBuilder();

    private BufferedReader reader;
    private char sym;
    private Channel selectChanel;
    private Item selectItem;

    private void nextSym() {
        try {
            sym = (char) reader.read();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (sym == '\n' || sym == '\r' || sym == '\t') {
            nextSym();
        }
    }

    public Channel parsChannel(final URL urlResourcesForParse) {
        if (urlResourcesForParse == null) {
            return null;
        }
        URLConnection urlConnection = null;
        reader = null;

        try {
            urlConnection = urlResourcesForParse.openConnection();
            urlConnection.connect();

            final String contentType = urlConnection.getHeaderField(CONTENT_TYPE);
            charset = null;
            for (final String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith(CHARSET)) {
                    charset = param.substring(CHARSET.length());
                    break;
                }
            }

            if (charset == null) {
                charset = getCharset(urlConnection.getInputStream());
            }

            if (charset != null) {
                try {
                    final Charset charsetContent = Charset.forName(charset);
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), charsetContent));
                    parsChannel();
                    selectChanel.setUrl(urlConnection.getURL().toString());
                }catch (final IllegalCharsetNameException exc){
                    Log.i(LOG_TAG, "Unknown charset: " + charset);
                }catch (final UnsupportedCharsetException exc){
                    Log.i(LOG_TAG, "Unsupported charset: " + charset);
                }
            }
        } catch (final IOException ignored) {
        } catch (final Exception exc) {
            Log.e(LOG_TAG, "Error when try stream connection with url: " + urlResourcesForParse);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException exc) {
                    Log.e(LOG_TAG, "Error when try close streamReader");
                }
            }
            if (urlConnection != null) {
                ((HttpURLConnection) urlConnection).disconnect();
            }
        }
        return selectChanel;
    }

    private String getCharset(final InputStream inputReader) throws IOException {
        sym = (char) inputReader.read();
        final StringBuilder command = new StringBuilder();
        if (sym == TAG_START) {
            key = "";
            while (sym != TAG_END) {
                if (sym == ' ' || sym == ':' || sym == STREAM_END) {
                    break;
                }
                key += sym;
                sym = (char) inputReader.read();
            }
            if (key.equals(XML_TAG_PREFERENCES)) {
                while (sym != TAG_END) {
                    if(sym == STREAM_END){
                        break;
                    }
                    command.append(sym);
                    sym = (char) inputReader.read();
                }
                String str = command.toString().replace("?", "");
                str = str.replace("\"", "");
                for (final String param : str.split(" ")) {
                    if (param.startsWith(ENCODING)) {
                        charset = param.substring(ENCODING.length(), param.length());
                        break;
                    }
                }
            }
        }
        return charset;
    }

    private void parsChannel() {
        nextSym();
        while (sym != STREAM_END) {
            if (sym == TAG_START) {
                readKey();
                switch (key) {
                    case FEED:
                    case CHANNEL:
                        System.out.println("CHANNEL");
                        selectChanel = new Channel();
                        parsItem(selectChanel);
                        break;
                    default:
                        break;
                }
                key = "";
            }
            nextSym();
        }
    }

    private void parsItem(final RssEntity rssObject) {
        nextSym();
        final char endStream = STREAM_END;
        while (sym != endStream) {
            if (sym == TAG_START) {
                readKey();
                switch (key) {
                    case TITLE:
                        if (sym == ' ') {
                            readKeySetting();
                        }
                        rssObject.setTitle(readValue());
                        break;
                    case ITEM:
                    case ENTRY:
                        System.out.println("ITEM");
                        final Item item = new Item();
                        selectItem = item;
                        parsItem(item);
                        selectChanel.addItem(item);
                        break;
                    case LINK:
                        if (sym == ' ') {
                            final KeySettings keySettings = readKeySetting();
                            if (keySettings.getRel().equals("alternate")) {
                                rssObject.setUrl(keySettings.getHref());
                            }
                        } else {
                            rssObject.setUrl(readValue());
                        }
                        break;
                    case DESCRIPTION:
                    case SUMMARY:
                        if (sym == ' ') {
                            readKeySetting();
                        }
                        rssObject.setDescription(readValue());
                        break;
                    case CONTENT:
                        if (sym == ' ' || sym == ':') {
                            readKeySetting();
                        }
                        selectItem.setContent(readValue());
                        break;
                    case DATE:
                    case LAST_BUILD_DATE:
                        rssObject.setDate(readDate(DATE_FORMATTER_RSS));
                        break;
                    case PUBLISHED:
                        rssObject.setDate(readDate(DATE_FORMATTER_ATOM));
                        break;
                    default:
                        break;
                }
                key = "";
            }
            nextSym();
        }
    }

    private Date readDate(final String dFormat) {
        final DateFormat dateFormat = new SimpleDateFormat(dFormat, Locale.ENGLISH);
        Date date = null;
        try {
            date = dateFormat.parse(readValue());
        } catch (final ParseException exc) {
            Log.e(LOG_TAG, "Error when try pars string date of news");
        }
        key = "";
        return date;
    }

    private void readKey() {
        key = "";
        nextSym();
        while (sym != TAG_END) {
            if (sym == ' ' || sym == ':') {
                // readKeySetting();
                return;
            }
            key += sym;
            nextSym();
        }
    }

    private KeySettings readKeySetting() {
        final StringBuilder strSetting = new StringBuilder();
        final KeySettings keySettings = new KeySettings();
        nextSym();
        while (sym != TAG_END) {
            strSetting.append(sym);
            nextSym();
        }
        for (final String str : strSetting.toString().split(" ")) {
            if (str.contains(VALUE_TYPE)) {
                keySettings.setType(str.substring(VALUE_TYPE_LENGTH, str.length() - 1)); // 6 == "type=".length (-1 т.к. значение ключа заключенно в кавычки - ")
            } else if (str.contains(VALUE_REL)) {
                keySettings.setRel(str.substring(VALUE_REL_LENGTH, str.length() - 1));
            } else if (str.contains(VALUE_HREF)) {
                keySettings.setHref(str.substring(VALUE_HREF_LENGTH, str.length() - 1));
            }
        }
        return keySettings;
    }

    private String readValue() {
        stringValue.setLength(0);
        nextSym();
        while (sym == ' ') {
            nextSym();
        }
        if (sym == TAG_START) {
            for (int i = CDATA_LENGTH_SKIP_CHAR; i > 0; i--) { // пропускаем "<!CDATA["
                if (sym == 'd') { // <description></description>
                    return "none";
                }
                nextSym();
            }
            while (sym != ']' && sym != '[') {
                stringValue.append(sym);
                nextSym();
            }
            return stringValue.toString();
        }
        while (sym != TAG_START) {
            if (sym == '"') {
                stringValue.append('\"');
            } else {
                stringValue.append(sym);
            }
            nextSym();
        }
        return stringValue.toString();
    }
}
