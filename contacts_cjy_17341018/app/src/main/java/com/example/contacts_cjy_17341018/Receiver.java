package com.example.contacts_cjy_17341018;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class Receiver extends BroadcastReceiver {
    private Handler handler;

    Receiver(Handler handler) {
        this.handler = handler;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        handler.sendEmptyMessage(0);
    }
}
