package com.macroyau.thingspeakandroid.demo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.macroyau.thingspeakandroid.ThingSpeakChannel;

/**
 * Created by user on 2016/5/22.
 */
public class channelListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater li;
    private int yellow_warn=0;
    private int red_warn=0;

    // 清單的資料，常用一個可變動的陣列或是集合來儲存，在此以JSONArray為例
    ThingSpeakChannel[] mItem;

    // 紀錄getView重新排版(inflate)的次數．此為研究觀察用，在實際應用時不需紀錄次數


    public channelListAdapter(Context context, ThingSpeakChannel[] item, int yellow_warn, int red_warn) {
        this.mContext = context;
        this.mItem = item;
        this.yellow_warn=yellow_warn;
        this.red_warn=red_warn;
        this.li = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if(mItem!=null){
            return mItem.length;
        }else {
            return 0;
        }

    }

    @Override
    public Object getItem(int position) {
       return mItem[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void serItem(ThingSpeakChannel[] item){
        this.mItem=item;
    }

    private static class ViewHolder {
        TextView tv_channelName;
        TextView tv_channelPercent;
        Button channelMainBtn;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = li.inflate(R.layout.channel, parent, false);
            holder = new ViewHolder();
            holder.tv_channelName = (TextView) convertView.findViewById(R.id.tv_channelName);
            holder.tv_channelPercent = (TextView) convertView.findViewById(R.id.tv_channelPercent);
            holder.channelMainBtn = (Button) convertView.findViewById(R.id.channelMainBtn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(mItem[position].getChannelPercent()!=null){ //防止APP剛開 初始化的狀態出錯
            holder.tv_channelName.setText(mItem[position].getChannelname());
            holder.tv_channelPercent.setText(mItem[position].getChannelPercent()+"%");
            float itemPercent= Float.parseFloat(mItem[position].getChannelPercent());
            convertView.setBackgroundColor(Color.rgb(0,255,0));
            if (itemPercent<=yellow_warn){
                convertView.setBackgroundColor(Color.rgb(255,200,140));

            }
            if (itemPercent<=red_warn){
                convertView.setBackgroundColor(Color.rgb(255,0,0));
            }
        }
        return convertView;
    }

    public void setYellowWarnValue(int value){
        yellow_warn=value;
    }

    public void setRedWarnValue(int value){
        red_warn=value;
    }

}