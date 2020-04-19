package com.example.healthanalytics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MessageService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {


        if (messageEvent.getPath().equals("/my_path")) {

            final String message = new String(messageEvent.getData());
           // Log.i("Heart: ",message);

           Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra("MY_KEY",message);
            sendBroadcast(intent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

}