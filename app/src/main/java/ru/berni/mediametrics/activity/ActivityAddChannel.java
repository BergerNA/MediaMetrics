package ru.berni.mediametrics.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ru.berni.mediametrics.dataBase.DatabaseHelper;
import ru.berni.mediametrics.R;

public class ActivityAddChannel extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_channel);
        final Button buttonAddChannel = (Button) findViewById(R.id.add_channel_button_add);
        buttonAddChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addChannel();
            }
        });

        final Button buttonClearDB = (Button) findViewById(R.id.add_channel_button_clearDB);
        buttonClearDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                buttonClearDB();
            }
        });
    }

    private void addChannel() {
        final EditText editText = (EditText) findViewById(R.id.add_channel_editText_url);
        final String url = editText.getText().toString();
        final Intent intent = new Intent(getApplicationContext(), ServiceParseURL.class);
        intent.putExtra(ServiceParseURL.EXTRA_MESSAGE_URL, url);
        startService(intent);
    }

    private void buttonClearDB() {
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        databaseHelper.clearDB();
    }
}
