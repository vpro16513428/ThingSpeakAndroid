package com.macroyau.thingspeakandroid.demo;

/**
 * Created by vpro16513428 on 2016/5/20.
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            List<Object> data = new ArrayList<>();
            data.add(0,User_APIKEY);
            data.add(1,red_warn);
            data.add(2,channel_total);
            data.add(3,Prechannel_total);
            data.add(4,pushtag);
            data.add(5,tsChannel);

            oos.writeObject(data);
            oos.flush();
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readSavedData() {
        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fis = openFileInput("service.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<Object> data = (List<Object>) ois.readObject();

            User_APIKEY = (String) data.get(0);
            red_warn = (int) data.get(1);
            channel_total = (int) data.get(2);
            Prechannel_total = (int) data.get(3);
            pushtag = (int[]) data.get(4);
            tsChannel = (ThingSpeakChannel[]) data.get(5);

            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        readSavedData();
        if(intent!=null){
            User_APIKEY = intent.getStringExtra("User_APIKEY");
            red_warn = intent.getIntExtra("red_warn",20);
        }
        mUser = new User(User_APIKEY);
        mUser.setOnRefreshChannelListener(new refreshListner());
        handler.postDelayed(showTime, 1000);
    }

    @Override
    public void onDestroy() {
        writeData();
        handler.removeCallbacks(showTime);
        super.onDestroy();
    }

    public void setRed_warn(int value){
        red_warn=value;
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
                                .setContentTitle("容量過低")
                                .setVibrate(vibrate_effect)
                                .setSound(soundUri)
                                .setContentText(tsChannel[i].getChannelname()+"只剩下"+tsChannel[i].getChannelPercent()+"%囉!!").build(); // 建立通知
                        //notification.defaults=Notification.DEFAULT_ALL;
                        //notification.flags = Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(notifyID, notification); // 發送通知
                        pushtag[i]=1;
                    }
                    if(Float.parseFloat(tsChannel[i].getChannelPercent()) > red_warn && !tsChannel[i].getChannelPercent().equals(tsChannel[i].getChannelPercentPre()) && pushtag[i]==0 && !tsChannel[i].getChannelPercent().equals("0.0")){
                        final int notifyID = Integer.valueOf(String.valueOf(tsChannel[i].getChannelId())); // 通知的識別號碼
                        // 建立震動效果，陣列中元素依序為停止、震動的時間，單位是毫秒
                        long[] vibrate_effect = {500, 500, 500, 500};
                        final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 通知音效的URI，在這裡使用系統內建的通知音效
                        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
                        final Notification notification = new Notification.Builder(getApplicationContext())
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("剩餘量更新")
                                .setVibrate(vibrate_effect)
                                .setSound(soundUri)
                                .setContentText(tsChannel[i].getChannelname()+"剩下："+tsChannel[i].getChannelPercent()+"%").build(); // 建立通知
                        //notification.defaults=Notification.DEFAULT_ALL;
                        //notification.flags = Notification.FLAG_AUTO_CANCEL;
                        notificationManager.notify(notifyID, notification); // 發送通知
                        tsChannel[i].setChannelPercnetPre(tsChannel[i].getChannelPercent());
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

                for (int i = 0; i < Prechannel_total; i++) {
                    tsChannel[i] = new ThingSpeakChannel(channels.get(i).getId(), channels.get(i).getName(), channels.get(i).getApiKeys().get(0).getApiKey());
                    tsChannel[i].loadChannelFeed();
                }

            }

            if(tsChannel.length!=0){
                for (int i = 0; i < channels.size(); i++) {
                    tsChannel[i].loadChannelFeed();
                }
            }
        }
    }
}
