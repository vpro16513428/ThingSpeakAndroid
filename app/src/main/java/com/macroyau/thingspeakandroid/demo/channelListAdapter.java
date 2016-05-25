package com.macroyau.thingspeakandroid.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by vpro16513428 on 2016/5/22.
 */
public class channelListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater li;
    private int yellow_warn=0;
    private int red_warn=0;
    private User mUser;
    int IP_selected_pos=0;
    Socket socket = null;

    // 清單的資料，常用一個可變動的陣列或是集合來儲存，在此以JSONArray為例
    ThingSpeakChannel[] mItem;
    String mSensor_IP[];

    // 紀錄getView重新排版(inflate)的次數．此為研究觀察用，在實際應用時不需紀錄次數


    public channelListAdapter(Context context, String User_APIKEY,ThingSpeakChannel[] item, int yellow_warn, int red_warn) {
        this.mContext = context;
        this.mItem = item;
        this.yellow_warn=yellow_warn;
        this.red_warn=red_warn;
        this.li = LayoutInflater.from(context);
        mUser=new User(User_APIKEY);
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

    public void setSensor_IP(String[] Sensor_IP){
        this.mSensor_IP=Sensor_IP;
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
            holder.channelMainBtn.setOnClickListener(new channelMainBtnOnClickListener(position));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(mItem[position].getChannelPercent()!=null){ //防止APP剛開 初始化的狀態出錯
            if(mItem[position].getChannelname().length()<7){
                holder.tv_channelName.setText(mItem[position].getChannelname());
            }else{
                holder.tv_channelName.setText(mItem[position].getChannelname().substring(0,6)+"...");
            }
            holder.tv_channelPercent.setText(mItem[position].getChannelPercent()+"%");

            float itemPercent= Float.parseFloat(mItem[position].getChannelPercent());
            convertView.setBackgroundResource(R.drawable.channel_corner_green);
            if (itemPercent<=yellow_warn){
                convertView.setBackgroundResource(R.drawable.channel_corner_yellow);

            }
            if (itemPercent<=red_warn){
                convertView.setBackgroundResource(R.drawable.channel_corner_red);
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

    public class channelMainBtnOnClickListener implements View.OnClickListener {

        int pos;

        public channelMainBtnOnClickListener(int pos){
            this.pos=pos;
        }

        @Override
        public void onClick(View v) {
            PopupMenu channelOption = new PopupMenu(mContext,v);
            channelOption.getMenuInflater().inflate(R.menu.channel,channelOption.getMenu());
            channelOption.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    LayoutInflater inflater = LayoutInflater.from(mContext);

                    switch (item.getItemId()){
                        case R.id.action_edit:
                            final View edit_v = inflater.inflate(R.layout.edit_activity, null);
                            final EditText inputText2 = (EditText)edit_v.findViewById(R.id.edit2);
                            new AlertDialog.Builder(mContext)
                                    .setTitle(mItem[pos].getChannelname())
                                    .setView(edit_v)
                                    .setPositiveButton("取消",
                                            new  DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which){
                                                    dialog.cancel();
                                                }
                                            })
                                    .setNegativeButton("確定",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (!inputText2.getText().toString().equals("")){
                                                        mItem[pos].setChannelname(inputText2.getText().toString());
                                                        mUser.editChannel(mItem[pos].getChannelId(),inputText2.getText().toString());
                                                    }
                                                }
                                            })
                                    .show();
                            break;
                        case R.id.action_delete:
                            new AlertDialog.Builder(mContext)
                                    .setTitle("確認刪除")
                                    .setMessage(mItem[pos].getChannelname())
                                    .setPositiveButton("取消",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setNegativeButton("確定",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    mUser.deleteChannel(mItem[pos].getChannelId());
                                                }
                                            })
                                    .show();
                            break;
                        case R.id.action_reset:
                            new AlertDialog.Builder(mContext)
                                    .setTitle("確認重置")
                                    .setMessage(mItem[pos].getChannelname())
                                    .setPositiveButton("取消",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            })
                                    .setNegativeButton("確定",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    mUser.resetChannel(mItem[pos].getChannelId());
                                                }
                                            })
                                    .show();
                            break;
                        case R.id.action_set_sensor:
                            final String[] str_IP = new String[mSensor_IP.length+1];
                            str_IP[0] = "選擇指派的裝置 IP：";
                            for (int i=1; i < mSensor_IP.length+1; i++){
                                str_IP[i] = mSensor_IP[i-1];
                            }
                            final View set_v = inflater.inflate(R.layout.socket_activity, null);
                            final Spinner IP_spinner = (Spinner)set_v.findViewById(R.id.IP_spinner);
                            final Button buttonConnect = (Button) set_v.findViewById(R.id.connect);

                            IP_spinner.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_dropdown_item, str_IP));
                            IP_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void  onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                                    IP_selected_pos = position;
                                    Log.d("IP_selected_pos", String.valueOf(IP_selected_pos));
                                }
                                @Override
                                public void  onNothingSelected(AdapterView<?> arg0) {
                                }
                            });

                            buttonConnect.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (v == buttonConnect && IP_selected_pos!=0) {
                                        Sensor_connect_Task connect_Task = new Sensor_connect_Task(str_IP[IP_selected_pos], 18266 ,mItem[pos].getWirteKey());
                                        connect_Task.execute();
                                    }
                                    else{
                                        new AlertDialog.Builder(mContext)
                                                .setTitle("錯誤")
                                                .setMessage("請選擇IP")
                                                .show();
                                    }
                                }
                            });

                            final AlertDialog.Builder set = new AlertDialog.Builder(mContext);
                            set.setTitle("指派裝置")
                                    .setView(set_v)
                                    .setPositiveButton("離開",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                            set.create().show();
                            break;
                    }
                    return true;
                }
            });
            channelOption.show();
        }

    }

    public class Sensor_connect_Task extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        String toSend="";

        Sensor_connect_Task(String addr, int port, String toSend) {
            dstAddress = addr;
            dstPort = port;
            this.toSend=toSend;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                Log.d("connectTask", "connectTask");
                socket = null;
                socket = new Socket(dstAddress, dstPort);

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Sensor_send_Task send_task = new Sensor_send_Task();
            send_task.execute("Channel_APIKEY:"+toSend);
        }

    }

    public class Sensor_send_Task extends AsyncTask<String, Void, Void> {
        String response = "";

        @Override
        protected Void doInBackground(String... arg0) {
            try {
                Log.d("sendTask", "sendTask");
                byte[] data = arg0[0].getBytes("UTF-8");
                OutputStream os = socket.getOutputStream();
                os.write(data);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();

    /*
     * notice:
     * inputStream.read() will block if no data return
     */
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }
                Log.d("response",response);

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }

}