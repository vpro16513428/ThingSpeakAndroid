package com.macroyau.thingspeakandroid.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.User;
import com.macroyau.thingspeakandroid.demo.task.__IEsptouchTask;
import com.macroyau.thingspeakandroid.model.Channel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    int channel_total = 0;
    int Prechannel_total = 0;
    Boolean openPush=false;
    ThingSpeakChannel[] tsChannel = new ThingSpeakChannel[0];
    String User_APIKEY = "";
    String Sensor_IP[] = new String[1];
    Socket socket = null;

    static User mUser;

    private ListView listinput;
    private ArrayAdapter<String> name_adapter;
    private ArrayAdapter<String> ip_adapter;
    private channelListAdapter channelListAdapter;
    private ArrayList<String> item;
    int x = 0 , y = 0 , red_warn = 20 , yellow_warn = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readSavedData();
        listinput = (ListView)findViewById(R.id.channelListView);
        item = new ArrayList<>();

        /*目前只有一個裝置 初始化暫時寫死
                1. SCAN SENSOR 會存取Sensor_IP[0]的值
                2. readSavedData 會存取Sensor_IP[0]的值*/
        Sensor_IP[0] = "192.168.1.101";

        if(User_APIKEY.equals("")){
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            final View v = inflater.inflate(R.layout.user_apikey, null);
            final EditText inputText4 = (EditText)v.findViewById(R.id.edit4);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("User_APIKEY")
                    .setView(v)
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
                                    User_APIKEY = inputText4.getText().toString();
                                    channelListAdapter = new channelListAdapter(MainActivity.this,User_APIKEY,tsChannel,yellow_warn,red_warn);
                                    channelListAdapter.setSensor_IP(Sensor_IP);
                                    listinput.setAdapter(channelListAdapter);
                                    mUser=new User(User_APIKEY);
                                    mUser.setOnRefreshChannelListener(new refreshListner());
                                    Intent intent = new Intent(MainActivity.this,mainService.class);
                                    intent.putExtra("User_APIKEY", User_APIKEY);
                                    startService(intent);
                                    Toast.makeText(MainActivity.this, "mianService start", Toast.LENGTH_SHORT).show();
                                }
                            })
                    .show();
        }else{
            channelListAdapter = new channelListAdapter(this,User_APIKEY,tsChannel,yellow_warn,red_warn);
            channelListAdapter.setSensor_IP(Sensor_IP);
            listinput.setAdapter(channelListAdapter);
            mUser=new User(User_APIKEY);
            mUser.setOnRefreshChannelListener(new refreshListner());
            Intent intent = new Intent(MainActivity.this,mainService.class);
            intent.putExtra("User_APIKEY", User_APIKEY);
            startService(intent);
            Toast.makeText(MainActivity.this, "mianService start", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        writeData();
        stopService(new Intent(MainActivity.this,mainService.class));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }// ActionBar

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                openadd();
                return true;
            case R.id.action_settings:
                opensetting();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }//讓ActionBar按鈕有動作

    private void openadd() { //新增Channel
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.add_activity, null);
        final EditText inputText = (EditText)v.findViewById(R.id.edit);
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("新增")
                .setView(v)
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
                                if (!inputText.getText().toString().equals("")) {
                                    mUser.createPublicChannel(inputText.getText().toString());
                                }
                            }
                        })
                .create().show();
    }

    private void opensetting() {
        final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.setting_activity, null);
        final SeekBar seekbar = (SeekBar) v.findViewById(R.id.seekBar);
        final TextView text3 = (TextView) v.findViewById(R.id.textView3);
        final SeekBar seekbar2 = (SeekBar) v.findViewById(R.id.seekBar2);
        final TextView text4 = (TextView) v.findViewById(R.id.textView4);
        final Button scn_btn = (Button) v.findViewById(R.id.Scan_sensor_button);
        final Switch Push_switch = (Switch) v.findViewById(R.id.Push_switch);
        seekbar.setProgress(yellow_warn);
        seekbar2.setProgress(red_warn);
        text3.setText(yellow_warn + "%");
        text4.setText(red_warn + "%");
        final AlertDialog.Builder setting = new AlertDialog.Builder(this);

        setting.setTitle("設定")
                .setView(v)
                .setPositiveButton("離開",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                channelListAdapter.setYellowWarnValue(yellow_warn);
                                channelListAdapter.setRedWarnValue(red_warn);
                                channelListAdapter.notifyDataSetChanged();
                            }
                        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            public void onProgressChanged(SeekBar seekBar, int progressV, boolean fromUser) {
                progress = progressV;
                text3.setText((progress) + "%");
                yellow_warn = progress;
                if (yellow_warn<1){
                    seekBar.setProgress(1);
                    yellow_warn = 1;
                    text3.setText((progress) + "%");
                }
                channelListAdapter.setYellowWarnValue(yellow_warn);
                channelListAdapter.notifyDataSetChanged();
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekbar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            public void onProgressChanged(SeekBar seekBar2, int progressV, boolean fromUser) {
                Log.d("onProgressChanged","onProgressChanged");
                progress = progressV;
                text4.setText((progress) + "%");
                red_warn = progress;
                if (red_warn<1){
                    seekBar2.setProgress(1);
                    yellow_warn = 1;
                    text4.setText((progress) + "%");
                }
                channelListAdapter.setRedWarnValue(red_warn);
                channelListAdapter.notifyDataSetChanged();
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("onStopTrackingTouch","onStopTrackingTouch");
                if(openPush){
                    Intent intent = new Intent(MainActivity.this,NUTC_FDS_Service.class);
                    intent.putExtra("User_APIKEY", User_APIKEY);
                    intent.putExtra("red_warn", red_warn);
                    startService(intent);
                }
            }
        });

        Push_switch.setChecked(openPush);
        Push_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    openPush=isChecked;
                    Intent intent = new Intent(MainActivity.this,NUTC_FDS_Service.class);
                    intent.putExtra("User_APIKEY", User_APIKEY);
                    intent.putExtra("red_warn", red_warn);
                    startService(intent);
                } else {
                    openPush=isChecked;
                    Intent intent = new Intent(MainActivity.this,NUTC_FDS_Service.class);
                    intent.putExtra("User_APIKEY", User_APIKEY);
                    intent.putExtra("red_warn", red_warn);
                    stopService(intent);
                }
            }
        });

        scn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View scn_v = inflater.inflate(R.layout.esptouch_demo_activity, null);

                final String TAG = "EsptouchDemoActivity";
                final TextView mTvApSsid;
                final EditText mEdtApPassword;
                final Button mBtnConfirm;
                final EspWifiAdminSimple mWifiAdmin;


                mWifiAdmin = new EspWifiAdminSimple(MainActivity.this);
                mTvApSsid = (TextView) scn_v.findViewById(R.id.tvApSssidConnected);
                mEdtApPassword = (EditText) scn_v.findViewById(R.id.edtApPassword);
                mBtnConfirm = (Button) scn_v.findViewById(R.id.btnConfirm);


                String apSsid = mWifiAdmin.getWifiConnectedSsid();
                if (apSsid != null) {
                    mTvApSsid.setText(apSsid);
                } else {
                    mTvApSsid.setText("");
                }

                mBtnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v == mBtnConfirm) {
                            String apSsid = mTvApSsid.getText().toString();
                            String apPassword = mEdtApPassword.getText().toString();
                            String apBssid = mWifiAdmin.getWifiConnectedBssid();
                            Boolean isSsidHidden = false;
                            String isSsidHiddenStr = "NO";
                            String taskResultCountStr = "1";
                            if (isSsidHidden) {
                                isSsidHiddenStr = "YES";
                            }
                            if (__IEsptouchTask.DEBUG) {
                                Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid + ", " + " mEdtApPassword = " + apPassword);
                            }
                            EsptouchAsyncTask3 temp = new EsptouchAsyncTask3();
                            temp.execute(apSsid, apBssid, apPassword, isSsidHiddenStr, taskResultCountStr);
                        }
                    }
                });


                final AlertDialog.Builder scn = new AlertDialog.Builder(MainActivity.this);
                scn.setTitle("ScanSensor")
                        .setView(scn_v)
                        .setPositiveButton("離開",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                scn.create().show();
            }
        });

        setting.create().show();
    }

    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String text = result.getBssid() + " is connected to the wifi";
                Toast.makeText(MainActivity.this, text,
                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    private class refreshListner implements User.OnRefreshChannelListener {

        @Override
        public void OnRefreshedChannel(List<Channel> channels) {
            if(channels.size()!=Prechannel_total){
                Prechannel_total = channels.size();
                channel_total = channels.size(); //把數量轉換成索引值最大值
                tsChannel=new ThingSpeakChannel[channels.size()];
                for(int i=0;i<channels.size();i++){
                    tsChannel[i] = new ThingSpeakChannel(channels.get(i).getId(),channels.get(i).getName(),channels.get(i).getApiKeys().get(0).getApiKey());
                }
            }

            for(int i=0;i<channels.size();i++){
                tsChannel[i].loadChannelFeed();
            }
            channelListAdapter.serItem(tsChannel);
            channelListAdapter.notifyDataSetChanged();
        }
    }

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("尋找裝置");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i("test", "progress dialog is canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "尋找中", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                String taskResultCountStr = params[4];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, MainActivity.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    "確定");
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("裝置掃描成功, bssid = " + resultInList.getBssid() + ",IP位置 = " + resultInList.getInetAddress().getHostAddress() + "\n");
                        Sensor_IP[0] = resultInList.getInetAddress().getHostAddress();
                        channelListAdapter.setSensor_IP(Sensor_IP);
                        Toast.makeText(MainActivity.this, Sensor_IP[0], Toast.LENGTH_LONG).show();
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count) + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage(sb.toString());
                } else {
                    mProgressDialog.setMessage("裝置掃描失敗");
                }
            }
        }
    }

    public void writeData() {
        try {
            FileOutputStream fos = openFileOutput("settings.dat", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            JSONObject data = new JSONObject();
            //User_APIKEY
            data.put("User_APIKEY", User_APIKEY);
            data.put("yellow_warn", yellow_warn);
            data.put("red_warn", red_warn);
            data.put("channel_total", channel_total);
            data.put("Sensor_IP", Sensor_IP[0]);
            data.put("openPush",openPush);
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
            FileInputStream fIn = openFileInput("settings.dat");
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
                yellow_warn = Integer.parseInt(data.getString("yellow_warn"));
                red_warn = Integer.parseInt(data.getString("red_warn"));
                channel_total = Integer.parseInt(data.getString("channel_total"));
                Sensor_IP[0] = data.getString("Sensor_IP");
                openPush = data.getBoolean("openPush");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class mainService extends Service {
        private Handler handler = new Handler();


        @Override
        public void onStart(Intent intent, int startId) {
            super.onStart(intent, startId);
            handler.postDelayed(showTime, 1000);
        }

        @Override
        public void onDestroy() {
            handler.removeCallbacks(showTime);
            super.onDestroy();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private Runnable showTime = new Runnable() {
            public void run() {
                if(mUser!=null){
                    mUser.refreshMyChannels();
                }
                Log.d("main_Service","run to end once time");
                handler.postDelayed(this, 1000);
            }
        };
    }
}

