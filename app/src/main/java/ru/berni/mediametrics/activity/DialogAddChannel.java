package ru.berni.mediametrics.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.berni.mediametrics.R;
import ru.berni.mediametrics.activity.utilit.NotifierResultPars;
import ru.berni.mediametrics.newsParser.Channel;

public class DialogAddChannel extends BaseDialogBind implements DialogInterface.OnClickListener{

    @Override
    public void onClick(final DialogInterface dialog, final int which) {

    }

    public interface SendToActivity {
        void onUpdate();
    }

    enum ChannelDialogType {
        ADD_CHANNEL,
        CREATE_CHANNEL
    }

    private SendToActivity sendToActivity;

    final private static String KEY_TYPE_DIALOG = "type";
    final private static String KEY_CHANNEL_ID = "id";
    final private static String KEY_CHANNEL_URL = "url";
    final private static String KEY_CHANNEL_TITLE = "title";

    final private static String urlPatternHttp = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
    final private static String urlPatternNotHttp = "[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";

    final private static String HTTP = "http://";

    private Button positiveButton;
    private Button negativeButton;
    private String titleDialog = "";
    private EditText editorTextTitle;
    private EditText editorTextUrl;

    private String title;
    private String url;
    private long id;
    private ChannelDialogType dialogType;

    ListenerParsUrl parsUrlListener = new ListenerParsUrl() {
        @Override
        public void onUpdate(final NotifierResultPars.ResultPars resultPars) {
            negativeButton.setText(resultPars.toString());
            if(resultPars == NotifierResultPars.ResultPars.RESULT_OK){
                if(sendToActivity != null) {
                    sendToActivity.onUpdate();
                }
            }
        }
    };

    static DialogAddChannel newInstance(final ChannelDialogType dialogType, final Channel channel) {
        final DialogAddChannel newDialog = new DialogAddChannel();
        final Bundle args = new Bundle();
        args.putSerializable(KEY_TYPE_DIALOG, dialogType);
        switch (dialogType) {
            case ADD_CHANNEL:
                break;
            case CREATE_CHANNEL:
                if (channel != null) {
                    args.putString(KEY_CHANNEL_TITLE, channel.getTitle());
                    args.putLong(KEY_CHANNEL_ID, channel.getId());
                    args.putString(KEY_CHANNEL_URL, channel.getUrl());
                }
                break;
            default:
                break;
        }
        newDialog.setArguments(args);
        return newDialog;
    }

    private void getExtras(final Bundle args) {
        if (args == null) {
            return;
        }
        dialogType = (ChannelDialogType) args.get(KEY_TYPE_DIALOG);
        if (dialogType == null) {
            return;
        }
        switch (dialogType) {
            case ADD_CHANNEL:
                titleDialog = getString(R.string.dialog_channel_settings_title_add);
                break;
            case CREATE_CHANNEL:
                title = args.getString(KEY_CHANNEL_TITLE);
                id = args.getLong(KEY_CHANNEL_ID);
                url = args.getString(KEY_CHANNEL_URL);
                titleDialog = getString(R.string.dialog_channel_settings_title_create);
                break;
            default:
        }
    }

    @Override
    void doWhenConnectedServices() {
        addParsListener(parsUrlListener);
    }

    @Override
    void onOnCreate() {
        getExtras(getArguments());
    }

    @Override
    void onOnStop() {

    }

    @Override
    public void onAttach(final Context context) {
        sendToActivity = (SendToActivity) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        sendToActivity = null;
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final View form = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_add_channel, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return (builder.setTitle(titleDialog).setView(form)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null).create());
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            negativeButton = dialog.getButton(Dialog.BUTTON_NEGATIVE);
            editorTextTitle = (EditText) dialog.findViewById(R.id.dialog_add_channel_editText_title);
            editorTextUrl = (EditText) dialog.findViewById(R.id.dialog_add_channel_editText_link);
        }
        negativeButton.setText(R.string.cancel);
        switch (dialogType) {
            case ADD_CHANNEL:
                positiveButton.setText(R.string.add);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        addChannel();
                    }
                });
                break;
            case CREATE_CHANNEL:
                positiveButton.setText(R.string.change);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        correctChannel();
                    }
                });
                editorTextUrl.setText(url);
                editorTextUrl.setEnabled(false);
                editorTextTitle.setText(Html.fromHtml(title));
                break;
            default:
        }
    }

    private void addChannel() {
        url = editorTextUrl.getText().toString();
        if (urlIsValid(url)) {
            if(isOnline()) {
                if (getExchange() != null) {
                    title = editorTextTitle.getText().toString();
                    getExchange().parsUrl(url, title);
                }
            }
        }
    }

    private void correctChannel(){
        final String newTitle = editorTextTitle.getText().toString();
        if(!newTitle.equals(title)){
            if(getExchange() != null){
                title = newTitle;
                getExchange().updateChannelTitle(id, newTitle);
                sendToActivity.onUpdate();
            }
        }
    }

    private boolean urlIsValid(final String url) {
        if (url.length() > 0) {

            if (url.matches(urlPatternHttp)) {
                setErrMessage("");
                return true;
            } else if (url.matches(urlPatternNotHttp)) {
                this.url = HTTP + url;
                setErrMessage("");
                return true;
            } else {
                setErrMessage(getString(R.string.err_valid_url));
            }
        } else {
            setErrMessage(getString(R.string.err_null_url));
        }
        return false;
    }

private boolean isOnline(){
    if(getExchange() != null){
        if(getExchange().isOnline()){
            setErrMessage("");
            return true;
        }
        setErrMessage(getString(R.string.err_internet_connection));
    }else {
        setErrMessage(getString(R.string.err_check_internet_connection));
    }
    return false;
}

    private void setErrMessage(final String errMessage) {
        final TextView messageErr = (TextView) getDialog().findViewById(R.id.dialog_add_channel_textView_message);
        messageErr.setText(errMessage);
    }
}
