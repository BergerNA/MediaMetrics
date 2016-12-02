package ru.berni.mediametrics.activity.classHelper;

import android.content.Context;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import ru.berni.mediametrics.activity.utilit.NotifierResultPars.ResultPars;
import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.newsParser.Channel;
import ru.berni.mediametrics.newsParser.Parser;


public final class ParserUrl implements Callable<Boolean> {

    private ResultPars resultPars = ResultPars.NULL;

    private static boolean runThread = false;
    private Context context;
    private final String urlForPars;
    private String userTitleChannel = "";

    public ParserUrl(@NonNull final Context context, final String urlForPars, final String titleChannel) {
        this.context = context;
        this.urlForPars = urlForPars;
        resultPars = ResultPars.RUNNING;
        userTitleChannel = titleChannel;
    }

    public static boolean isRunning() {
        return runThread;
    }

    @Override
    public Boolean call() {
        runThread = true;
        final DatabaseHelper db = DatabaseHelper.getInstance(context);
        if (urlForPars == null || urlForPars.equals("")) {
            resultPars = ResultPars.ERR_URL;
            return false;
        }
        final Parser parser = new Parser();
        try {
            final Channel channel = parser.parsChannel(new URL(urlForPars));
            if (channel != null) {
                if(userTitleChannel != null && !userTitleChannel.equals("")) {
                    channel.setTitle(userTitleChannel);
                }
                channel.setId(db.addChannel(channel));
                db.addChannelItems(channel);
                resultPars = ResultPars.RESULT_OK;
            } else {
                resultPars = ResultPars.ERR_CONTENT;
            }
        } catch (final MalformedURLException e) {
            resultPars = ResultPars.ERR_URL;
        } finally {
            stop();
        }
        return true;
    }

    private void stop(){
        context = null;
        runThread = false;
    }

    public ResultPars getResultPars(){
        return resultPars;
    }
}
