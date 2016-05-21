package com.macroyau.thingspeakandroid;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.macroyau.thingspeakandroid.model.Channel;
import com.macroyau.thingspeakandroid.model.ChannelFeed;
import com.macroyau.thingspeakandroid.model.empty;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by user on 2016/5/21.
 */
public class User {

    public interface OnRefreshChannelListener {

        /***
         * The specific User's Channel is updated.
         */
        void OnRefreshedChannel(List<Channel> channels);

    }

    private static final String THINGSPEAK_API = "https://api.thingspeak.com";
    private String mUserApiKey;
    private UserService mService;
    private OnRefreshChannelListener mOnRefreshChannelListener;

    public User(String UserApiKey) {
        this.mUserApiKey = UserApiKey;

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(THINGSPEAK_API)
                .setConverter(new GsonConverter(gson))
                .build();

        mService = restAdapter.create(UserService.class);
    }

    public void setOnRefreshChannelListener(OnRefreshChannelListener OnRefreshChannelListener) {
        this.mOnRefreshChannelListener = OnRefreshChannelListener;
    }

    public void refreshMyChannels() {
        mService.listMyChannels(mUserApiKey, new Callback<List<Channel>>() {
            @Override
            public void success(List<Channel> channels, Response response) {
                if(mOnRefreshChannelListener!=null){
                    mOnRefreshChannelListener.OnRefreshedChannel(channels);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
        //        mService.listMyChannels(mUserApiKey, new Callback<List<Channel>>() {
//
//            @Override
//            public void success(List<Channel> channels, Response response) {
//                if (mOnRefreshChannelListener != null && channels.size() != 0) {
//
//                    tempID = new Long[channels.size()];
//                    tempName = new String[channels.size()];
//                    tempWriteKey = new String[channels.size()];
//
//                    for (int i = 0; i < channels.size(); i++) {
//                        tempID[i] = channels.get(i).getId();
//                        tempName[i] = channels.get(i).getName();
//                        tempWriteKey[i] = channels.get(i).getApiKeys().get(0).getApiKey();
//                        final int finalI = i;
//                        new ThingSpeakChannel(channels.get(i).getId()).setChannelFeedUpdateListener(new ThingSpeakChannel.ChannelFeedUpdateListener() {
//                            @Override
//                            public void onChannelFeedUpdated(long channelId, String channelName, ChannelFeed channelFeed) {
//                                setTest(String.valueOf(finalI),finalI);
//                            }
//                        });
//                    }
//                    if(test==null){
//                        Log.d("test is null", "test is null");
//                    }else {
//                        Log.d("test", test[0]);
//                    }
//                    mOnRefreshChannelListener.OnRefreshedChannel(tempID, tempName, tempWriteKey, test);
//                } else {
//                    mOnRefreshChannelListener.OnRefreshedChannel(null, null, null, null);
//                }
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//            }
//        });
    }

    public void createPublicChannel(String name) {

        mService.createChannel(mUserApiKey, name, "true", new Callback<Channel>() {
            @Override
            public void success(Channel channel, Response response) {
                refreshMyChannels();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

    }

    public void editChannel(Long channelId, String name){

        mService.editChannel(mUserApiKey, channelId, name, new Callback<Channel>() {
            @Override
            public void success(Channel channel, Response response) {
                refreshMyChannels();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void resetChannel(Long channelId) {
        mService.resetChannel(mUserApiKey, channelId, new Callback<empty>() {
            @Override
            public void success(empty empty, Response response) {
                refreshMyChannels();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void deleteChannel(Long channelId) {
        mService.deleteChannel(mUserApiKey, channelId, new Callback<Channel>() {
            @Override
            public void success(Channel channel, Response response) {
                refreshMyChannels();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}
