package com.macroyau.thingspeakandroid.demo;

/**
 * Created by user on 2016/5/20.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.User;
import com.macroyau.thingspeakandroid.model.Channel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

//繼承android.app.Service
public class NUTC_FDS_Service extends Service {
    private Handler handler = new Handler();
    ThingSpeakChannel[] tsChannel = new ThingSpeakChannel[0];
    int[] pushtag=new int[0];
    String User_APIKEY="";
    int red_warn = 0;
    int channel_total=0;
    int Prechannel_total=0;
    private User mUser;


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        writeData();
        super.onTaskRemoved(rootIntent);
    }

    public void writeData() {
        try {
            FileOutputStream fos = openFileOutput("service.dat", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            JSONObject data = new JSONObject();

            data.put("User_APIKEY", User_APIKEY);
            data.put("red_warn", red_warn);

            osw.write(data.toString());
            osw.flush();
            osw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readSavedData() {
        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = openFileInput("service.dat");
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }

            isr.close();
            Log.d("datax",datax.toString());
            if(datax.toString()!=""){
                JSONObject data = null;
                data = new JSONObject(datax.toString());
                User_APIKEY = data.getString("User_APIKEY");
                red_warn = data.getInt("red_warn");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        readSavedData();
        mUser=new User(User_APIKEY);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if(intent!=null) {
            User_APIKEY = intent.getStringExtra("User_APIKEY").toString();
            red_warn = intent.getIntExtra("red_warn", red_warn);
            mUser = new User(User_APIKEY);
            mUser.setOnRefreshChannelListener(new refreshListner());
        }
        handler.postDelayed(showTime, 1000);
    }

    @Override
    public void onDestroy() {
        writeData();
        handler.removeCallbacks(showTime);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Runnable showTime = new Runnable() {
        public void run() {
            mUser.refreshMyChannels();
            for(int i =0;i<channel_total;i++){
                if (tsChannel[i].getChannelPercent()!=null){
                    if(tsChannel[i].getChannelPercent().equals("0.0")){
                        pushtag[i]=0;
                    }
                    if(Float.parseFloat(tsChannel[i].getChannelPercent())<red_warn && pushtag[i]==0 && !tsChannel[i].getChannelPercent().equals("0.0")){
                        final int notifyID = Integer.valueOf(String.valueOf(tsChannel[i].getChannelId())); // 通知的識別號碼
                        // 建立震動效果，陣列中元素依序為停止、震動的時間，單位是毫秒
                        long[] vibrate_effect = {500, 500, 500, 500};
                        final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 通知音效的URI，在這裡使用系統內建的通知音效
                        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
                        final Notification notification = new Notification.Builder(getApplicationContext())
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("液難忘_FDS")
                                .setVibrate(vibrate_effect)
                                .setSound(soundUri)
                                .setContentText(tsChannel[i].getChannelname()+"只剩下"+tsChannel[i].getChannelPercent()+"%囉!!").build(); // 建立通知
                        //notification.defaults=Notification.DEFAULT_ALL;
                        //notification.flags = Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(notifyID, notification); // 發送通知
                        pushtag[i]=1;
                    }
                }
            }
            handler.postDelayed(this, 1000);
            Log.d("Push_Service","run to end once time");
        }
    };

    private class refreshListner implements User.OnRefreshChannelListener {

        @Override
        public void OnRefreshedChannel(List<Channel> channels) {
            if (channels.size() != Prechannel_total) {
                Prechannel_total = channels.size();
                channel_total = channels.size(); //把數量轉換成索引值最大值
                tsChannel = new ThingSpeakChannel[channels.size()];
                pushtag=new int[channels.size()];
                for (int i = 0; i < channels.size(); i++) {
                    tsChannel[i] = new ThingSpeakChannel(channels.get(i).getId(), channels.get(i).getName(), channels.get(i).getApiKeys().get(0).getApiKey());
                }
            }

            for (int i = 0; i < channels.size(); i++) {
                tsChannel[i].loadChannelFeed();
            }
        }
    }
}
