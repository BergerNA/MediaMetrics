package ru.berni.mediametrics.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import java.net.MalformedURLException;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.R;

public class ActivityAddChannel extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_channel);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void buttonAddChannel_onClick(final View view) throws MalformedURLException {
        final EditText editText = (EditText) findViewById(R.id.editText_addChannel);
        final String url = editText.getText().toString();
        final Intent intent = new Intent(getApplicationContext(), ServiceParseURL.class);
        intent.putExtra(ServiceParseURL.EXTRA_MESSAGE_URL, url);
        startService(intent);
    }

    public void buttonClearDB_onClick(final View view) {
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        databaseHelper.clearDB();
    }
}
