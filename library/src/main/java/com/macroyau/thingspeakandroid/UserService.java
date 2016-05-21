package com.macroyau.thingspeakandroid;

import com.macroyau.thingspeakandroid.model.Channel;
import com.macroyau.thingspeakandroid.model.empty;

import java.util.List;

import retrofit.Callback;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;


/**
 * Created by user on 2016/5/21.
 */
public interface UserService {

    @GET("/channels.json")
    void listMyChannels(@Query("api_key") String apiKey, Callback<List<Channel>> callback);

    @POST("/channels.json")
    void createChannel(@Query("api_key") String apiKey,@Query("name") String name,@Query("public_flag") String public_flag,Callback<Channel> callback);

    @PUT("/channels/{id}.json")
    void editChannel(@Query("api_key") String apiKey, @Path("id") Long channelId, @Query("name") String name,Callback<Channel> callback);

    @DELETE("/channels/{id}/feeds.json")
    void resetChannel(@Query("api_key") String apiKey, @Path("id") Long channelId, Callback<empty> callback);

    @DELETE("/channels/{id}.json")
    void deleteChannel(@Query("api_key") String apiKey, @Path("id") Long channelId, Callback<Channel> callback);

}
