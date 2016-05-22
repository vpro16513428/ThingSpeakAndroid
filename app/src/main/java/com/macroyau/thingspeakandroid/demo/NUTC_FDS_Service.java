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
import com.macroyau.thingspeakandroid.model.ChannelFeed;

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
    String[][] Channel_Info;
    String User_APIKEY="";
    int red_warn = 0;
    int channel_total=-1;
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
            handler.postDelayed(this, 1000);
            Log.d("Push_Service","run to end once time");
        }
    };

    private class refreshListner implements User.OnRefreshChannelListener {

        @Override
        public void OnRefreshedChannel(List<Channel> channels) {
            Channel_Info=new String[channels.size()][6];
            channel_total=channels.size()-1; //把數量轉換成索引值最大值

            for(int i=0;i<channels.size();i++){
                //0 IP
                //1 ID
                //2 name
                //3 APIKEY
                //4 percent
                //5 push_flag
                Channel_Info[i][2]=channels.get(i).getName();
                ThingSpeakChannel tsChannel =new ThingSpeakChannel(channels.get(i).getId(),channels.get(i).getName(),channels.get(i).getApiKeys().get(0).getApiKey());
                tsChannel.setChannelFeedUpdateListener(new feedUpdateListener(i));
            }
        }
    }

    private class feedUpdateListener implements ThingSpeakChannel.ChannelFeedUpdateListener{

        int pos;

        feedUpdateListener(int pos){
            this.pos=pos;
        }

        @Override
        public void onChannelFeedUpdated(long channelId, String channelName, ChannelFeed channelFeed) {
            if(channelFeed.getFeeds().size()!=0){
                float tempMax=0,tempLast=0,Percnet=0;
                tempLast= Float.parseFloat(channelFeed.getFeeds().get(channelFeed.getFeeds().size()-1).getField1());
                for(int i =0;i<channelFeed.getFeeds().size();i++){
                    if(tempMax < Float.parseFloat(channelFeed.getFeeds().get(i).getField1())){
                        tempMax = Float.parseFloat(channelFeed.getFeeds().get(i).getField1());
                    }
                }
                Percnet=tempLast/tempMax;
                Channel_Info[pos][4]= String.valueOf(Percnet);
            }else{
                Channel_Info[pos][4]="0.0";
            }
            for (int i=0 ;i<=channel_total; i++){
                if(Channel_Info[i][5]==null){ //flag初始化
                    Channel_Info[i][5]="0";
                }
                if (Float.parseFloat(Channel_Info[i][4])<red_warn && Channel_Info[i][5].equals("0")){
                    final int notifyID = Integer.valueOf(Channel_Info[i][1]); // 通知的識別號碼
                    // 建立震動效果，陣列中元素依序為停止、震動的時間，單位是毫秒
                    long[] vibrate_effect = {500, 500, 500, 500};
                    final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 通知音效的URI，在這裡使用系統內建的通知音效
                    final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
                    final Notification notification = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("液難忘_FDS")
                            .setVibrate(vibrate_effect)
                            .setSound(soundUri)
                            .setContentText(Channel_Info[i][2]+"只剩下"+Channel_Info[i][4]+"%囉!!").build(); // 建立通知
                    //notification.defaults=Notification.DEFAULT_ALL;
                    //notification.flags = Notification.FLAG_AUTO_CANCEL;
                    notificationManager.notify(notifyID, notification); // 發送通知
                    Channel_Info[i][5] = String.valueOf(Integer.parseInt(Channel_Info[i][5])+1);
                }
            }
        }
    }
}
