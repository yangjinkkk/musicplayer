package com.example.administrator.music2;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {
    private TextView musicStatus, musicTime, musicTotal, LRCtext, LRCtext2, LRCtext3;
    private SeekBar seekBar;
    private Spinner spinner;
    private Button btnPlayOrPause, btnStop, btnQuit, btnBackWard, btnFastWard, btnPre, btnNext;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private static final String[] m = {"列表循环", "随机播放", "单曲循环", "顺序播放"};
    private boolean tag1 = false;
    private boolean tag2 = false;
    private MusicService musicService;
    int i = 0;
    private ArrayAdapter<String> adapter;
    public final static String TAG = "MainActivity";
    private static Vector<timelrc> lrclist;
    private boolean IsLyricExist = false;
    private int lastLine = 0;
    private int current;
    File file = new File("/storage/emulated/0/$MuMu共享文件夹/music/薛之谦 - 刚刚好.lrc");
    final Vector<timelrc> lrcList = ReadLRC(file);
    //快进快退
    private Handler repeatUpdateHandler = new Handler();
    public int mValue;
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;

    private class RptUpdater implements Runnable {
        public void run() {
            if (mAutoIncrement) {
                mValue += 10; //change this value to control how much to forward
                musicService.mediaPlayer.seekTo(musicService.mediaPlayer.getCurrentPosition() + mValue);
                musicService.mediaPlayer.pause();
                repeatUpdateHandler.postDelayed(new RptUpdater(), 50);
            } else if (mAutoDecrement) {
                mValue -= 10; //change this value to control how much to rewind
                musicService.mediaPlayer.seekTo(musicService.mediaPlayer.getCurrentPosition() - mValue);
                musicService.mediaPlayer.pause();
                repeatUpdateHandler.postDelayed(new RptUpdater(), 50);
            }
        }
    }


    //  在Activity中调用 bindService 保持与 Service 的通信
    private void bindServiceConnection() {
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);
        bindService(intent, serviceConnection, this.BIND_AUTO_CREATE);
    }

    //  回调onServiceConnected 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MyBinder) (service)).getService();
            Log.i("musicService", musicService + "");

            musicTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setMax(musicService.mediaPlayer.getDuration());
            musicTotal.setText(time.format(musicService.mediaPlayer.getDuration()));
            // handler.postDelayed(runnable, 200);
            //handler1.post(runnable1);

            Log.i("i", "ttttttttttttt:::99999999sdsadasda");

            handler.postDelayed(runnable, 200);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };


    //  通过 Handler 更新 UI 上的组件状态
    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            musicTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setMax(musicService.mediaPlayer.getDuration());
            musicTotal.setText(time.format(musicService.mediaPlayer.getDuration()));
            handler.postDelayed(runnable, 200);

        }
    };

    //组件绑定
    private void findViewById() {
        musicTime = (TextView) findViewById(R.id.MusicTime);
        musicTotal = (TextView) findViewById(R.id.MusicTotal);
        seekBar = (SeekBar) findViewById(R.id.MusicSeekBar);
        btnPlayOrPause = (Button) findViewById(R.id.BtnPlayorPause);
        btnStop = (Button) findViewById(R.id.BtnStop);
        btnQuit = (Button) findViewById(R.id.BtnQuit);
        musicStatus = (TextView) findViewById(R.id.MusicStatus);
        LRCtext = (TextView) findViewById(R.id.LRCtext);
        LRCtext2 = (TextView) findViewById(R.id.LRCtext2);
        LRCtext3 = (TextView) findViewById(R.id.LRCtext3);
        btnBackWard = (Button) findViewById(R.id.BtnBackWard);
        btnFastWard = (Button) findViewById(R.id.BtnFastWard);
        btnPre = (Button) findViewById(R.id.BtnPre);
        btnNext = (Button) findViewById(R.id.BtnNext);
        spinner = (Spinner) findViewById(R.id.Spinner01);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m);


        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter 添加到spinner中
        spinner.setAdapter(adapter);

        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());

        //设置默认值
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();

        bindServiceConnection();
        myListener();

        //拉动进度条
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    musicService.mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void myListener() {

        //上一首监听
        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String path = musicService.playNew(-1);
                    lrclist = getLrcPath(path);
                } catch (Exception e) {

                }
            }
        });

        //下一首监听
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.i("ff", "dd");
                    String path = musicService.playNew(1);
                    lrclist = getLrcPath(path);
                } catch (Exception e) {

                }
            }

        });
        //快进监听
        btnFastWard.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    mAutoIncrement = true;
                    repeatUpdateHandler.post(new RptUpdater());
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setPressed(false);
                    if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                            && mAutoIncrement) {
                        mAutoIncrement = false;
                    }
                    musicService.mediaPlayer.start();
                    return false;
                }

                return false;
            }

        });
        //快退监听
        btnBackWard.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    mAutoDecrement = true;
                    repeatUpdateHandler.post(new RptUpdater());
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setPressed(false);
                    if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                            && mAutoDecrement) {
                        mAutoDecrement = false;
                    }
                    musicService.mediaPlayer.start();
                    return false;
                }
                return false;
            }


        });
        musicTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //Log.i("i","ytytyt"+TimeToLong(arg0.toString()));
                current = musicService.mediaPlayer.getCurrentPosition();
                i = 0;
                //Log.i("i", "qqqqqqqqqqqqqqqqqq:::" + current);
                //Log.i("i", "eeeeeeeeeeeeee:::" + lrcList.size());
                for (; i < lrclist.size(); i++) {
                    //Log.i("i", "ttttttttttttt:::" + i);
                    if (i == lrclist.size()) {
                        //LRCtext.setText(lrclist.get(i).getLrcString());
                        current = i;
                        break;
                    }
                    if (current < lrclist.get(i).getTimePoint()) {
//                        Log.i("i", "wwwwwwwwwww:::" + lrclist.get
//                                (i).getTimePoint()+"qq"+i);
//                        Log.i("i", "rrrrrrrrrrrrrr:::" + lrclist.get
//                                (i).getLrcString());
                        //LRCtext.setText(lrclist.get(i).getLrcString());
                        current = i;
                        break;
                    }
                }
                LRCtext.setText(lrclist.get(current).getLrcString());
                if (current + 1 < lrclist.size()) {
                    //下一句歌词
                    LRCtext2.setText(lrclist.get(current + 1).getLrcString());
                }
                if (current != 0) {
                    //
                    LRCtext3.setText(lrclist.get(current - 1).getLrcString());
                    LRCtext3.setTextColor(android.graphics.Color.BLUE);
                }
            }


            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });

        //播放/暂停监听
        btnPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService.mediaPlayer != null) {
                    seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
                    seekBar.setMax(musicService.mediaPlayer.getDuration());
                }
                //  由tag的变换来控制事件的调用
                if (musicService.tag != true) {
                    btnPlayOrPause.setText("PAUSE");
                    musicStatus.setText("Playing");
                    musicService.playOrPause();
                    musicService.tag = true;

                    if (tag1 == false) {
                        //animator.start();
                        tag1 = true;
                    } else {
                        // animator.resume();
                    }
                } else {
                    btnPlayOrPause.setText("PLAY");
                    musicStatus.setText("Paused");
                    musicService.playOrPause();
                    //  animator.pause();
                    musicService.tag = false;
                }
                if (tag2 == false) {
                    handler.post(runnable);
                    tag2 = true;
                }
            }
        });
        //停止
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicStatus.setText("Stopped");
                btnPlayOrPause.setText("PLAY");
                musicService.stop();
                //  animator.pause();
                musicService.tag = false;
            }
        });

        //  停止服务时，必须解除绑定，写入btnQuit按钮中
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                unbindService(serviceConnection);
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                stopService(intent);
                try {
                    MainActivity.this.finish();
                } catch (Exception e) {

                }
            }
        });

    }

    //  获取并设置返回键的点击事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            //Log.i("你的血型是：",""+arg2);
            musicService.MODE = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

//    public void RefreshLRC(int current)
//    {
//        if (IsLyricExist){
//            for(int i = 0; i < lrclist.size(); i++)
//            {
//                if(current < lrclist.get(i).getTimePoint())
//                    if(i == 0 || current >= lrclist.get(i-1).getTimePoint())
//                    {
//                        Log.d(TAG,"string = "+lrclist.get(i-1).getLrcString());
//                        mediaPlay.setLRCText(lrclist.get(i-1).getLrcString(),lastLine!=(i-1));
//                        lastLine = i-1;
////                      playlrcText.setText(lrclist.get(i-1).getLrcString());
//                    }
//
//            }
//        }
//    }


    public Vector<timelrc> ReadLRC(File f) {
        try {
            if (!f.exists()) {
                Log.d(TAG, "not exit the lrc file");
                IsLyricExist = false;
//              strLRC = main.getResources().getString(R.string.lrcservice_no_lyric_found);
            } else {
                lrclist = new Vector<timelrc>();
                IsLyricExist = true;
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                BufferedReader br = new BufferedReader(new InputStreamReader(is, GetCharset(f)));
                String strTemp = "";
                while ((strTemp = br.readLine()) != null) {
//                  Log.d(TAG,"strTemp = "+strTemp);
                    strTemp = AnalyzeLRC(strTemp);
                }
                br.close();
                is.close();
                Collections.sort(lrclist, new Sort());

                for (int i = 0; i < lrclist.size(); i++) {
                    Log.d(TAG, "time = " + lrclist.get(i).getTimePoint() + "   string = " + lrclist.get(i).getLrcString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lrclist;
    }

    private String AnalyzeLRC(String LRCText) {
        try {
            int pos1 = LRCText.indexOf("[");
            int pos2 = LRCText.indexOf("]");

            if (pos1 == 0 && pos2 != -1) {
                Long time[] = new Long[GetPossiblyTagCount(LRCText)];
                time[0] = TimeToLong(LRCText.substring(pos1 + 1, pos2));
                if (time[0] == -1)
                    return ""; // LRCText
                String strLineRemaining = LRCText;
                int i = 1;
                while (pos1 == 0 && pos2 != -1) {

                    strLineRemaining = strLineRemaining.substring(pos2 + 1);
                    pos1 = strLineRemaining.indexOf("[");
                    pos2 = strLineRemaining.indexOf("]");
                    if (pos2 != -1) {
                        time[i] = TimeToLong(strLineRemaining.substring(pos1 + 1, pos2));
                        if (time[i] == -1)
                            return ""; // LRCText
                        i++;
                    }
                }

                timelrc tl = new timelrc();
                for (int j = 0; j < time.length; j++) {
                    if (time[j] != null) {
//                      Log.d(TAG,"time["+j+"] = "+time[j].intValue()+"    strLineRemaining = "+strLineRemaining);
                        tl.setTimePoint(time[j].intValue());
                        tl.setLrcString(strLineRemaining);

                        lrclist.add(tl);
                        tl = new timelrc();
//                      map.put(time[j], strLineRemaining);
//                      lstTimeStamp.add(time[j]);
                    }
                }
                return strLineRemaining;
            } else
                return "";
        } catch (Exception e) {
            return "";
        }
    }

    private int GetPossiblyTagCount(String Line) {
        String strCount1[] = Line.split("\\Q[\\E");
        String strCount2[] = Line.split("\\Q]\\E");
        if (strCount1.length == 0 && strCount2.length == 0)
            return 1;
        else if (strCount1.length > strCount2.length)
            return strCount1.length;
        else
            return strCount2.length;
    }

    public long TimeToLong(String Time) {
        try {
            String[] s1 = Time.split(":");
            int min = Integer.parseInt(s1[0]);
            String[] s2 = s1[1].split("\\.");
            int sec = Integer.parseInt(s2[0]);
            int mill = 0;
            if (s2.length > 1)
                mill = Integer.parseInt(s2[1]);
            return min * 60 * 1000 + sec * 1000 + mill * 10;
        } catch (Exception e) {
            return -1;
        }
    }

    public String GetCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // ��������BF���µģ�Ҳ����GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // ˫�ֽ�(0xC0-0xDF),(0x80-xBF)Ҳ������GB������
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// Ҳ�п��ܳ��?���Ǽ��ʽ�С
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    private class Sort implements Comparator<timelrc> {
        public Sort() {
        }

        public int compare(timelrc tl1, timelrc tl2) {
            return sortUp(tl1, tl2);
        }

        private int sortUp(timelrc tl1, timelrc tl2) {
            if (tl1.getTimePoint() < tl2.getTimePoint())
                return -1;
            else if (tl1.getTimePoint() > tl2.getTimePoint())
                return 1;
            else
                return 0;
        }
    }


    public static class timelrc {
        private String lrcString;
        private int sleepTime;
        private int timePoint;

        timelrc() {
            lrcString = null;
            sleepTime = 0;
            timePoint = 0;
        }

        public void setLrcString(String lrc) {
            lrcString = lrc;
        }

        public void setSleepTime(int time) {
            sleepTime = time;
        }

        public void setTimePoint(int tPoint) {
            timePoint = tPoint;
        }

        public String getLrcString() {
            return lrcString;
        }

        public int getSleepTime() {
            return sleepTime;
        }

        public int getTimePoint() {
            return timePoint;
        }
    }

    ;

    private Vector<timelrc> getLrcPath(String path) {
        Log.d(TAG, "path = " + path);
        if (path != null) {
            path = path.substring(0, path.lastIndexOf(".")).concat(".lrc");
            File file = new File(path);
            lrclist = ReadLRC(file);
            return lrclist;
        }
        return null;
    }

}

