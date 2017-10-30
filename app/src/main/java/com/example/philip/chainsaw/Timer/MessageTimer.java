package com.example.philip.chainsaw.Timer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.philip.chainsaw.apis.TinderServiceVolley;
import com.example.philip.chainsaw.interfaces.CallBack;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by admin1 on 16/10/17.
 */

public class MessageTimer extends Service {
    Timer timer= new Timer();
    Context ctx;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ctx=this;
        timer.schedule(new Mytask(),1000*60*2);
    }

    class Mytask extends TimerTask
    {

        @Override
        public void run() {
            SharedPreferences preferences = ctx.getSharedPreferences("savedToken", MODE_PRIVATE);

            TinderServiceVolley.getInstance(getApplicationContext()).getMessages(preferences.getString("Token",""), new CallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    Intent in= new Intent("sendmsgreciver");
                    in.putExtra("response",response.toString());
                    sendBroadcast(in);
                }

                @Override
                public void onFail(String msg) {

                }
            });

        }
    }
}
