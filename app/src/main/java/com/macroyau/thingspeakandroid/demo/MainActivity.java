package com.macroyau.thingspeakandroid.demo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.User;
import com.macroyau.thingspeakandroid.model.Channel;
import com.macroyau.thingspeakandroid.model.ChannelFeed;

import java.util.List;




public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        User user =new User("RO28U1DJSWQB8Q3L");

        user.setOnRefreshChannelListener(new User.OnRefreshChannelListener() {
            @Override
            public void OnRefreshedChannel(List<Channel> channels) {
                for(int i = 0 ; i<channels.size();i++){
                    Log.d("ID", String.valueOf(channels.get(i).getId()));
                    Log.d("name",channels.get(i).getName());
                    Log.d("WriteAPIKey",channels.get(i).getApiKeys().get(0).getApiKey());
                }
                ThingSpeakChannel tsChannel = new ThingSpeakChannel(channels.get(0).getId());
                tsChannel.setChannelFeedUpdateListener(new ThingSpeakChannel.ChannelFeedUpdateListener() {
                    @Override
                    public void onChannelFeedUpdated(long channelId, String channelName, ChannelFeed channelFeed) {
                        float tempMax=0,tempLast=0,tempPercent=0;
                        tempLast= Float.parseFloat(channelFeed.getFeeds().get(channelFeed.getFeeds().size()-1).getField1());
                        for(int i = 0 ; i<channelFeed.getFeeds().size();i++){
                            if(tempMax< Float.parseFloat(channelFeed.getFeeds().get(i).getField1())){
                                tempMax= Float.parseFloat(channelFeed.getFeeds().get(i).getField1());
                            }
                        }
                        tempPercent=tempLast/tempMax;
                        //更新UI
                    }
                });
            }
        });
        user.refreshMyChannels();

        user.createPublicChannel("qq");
        user.editChannel(96545L,"AA");
        user.resetChannel(96545L);
        user.deleteChannel(96545L);

    }

}
