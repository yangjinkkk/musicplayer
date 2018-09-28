package com.example.administrator.music2;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MusicService extends Service {
    public MediaPlayer mediaPlayer;
    public boolean tag = false;
    //列表循环
    public static final int MENU_RECICLE = 0;
    //随机播放
    public static final int RADOM = 1;
    //单曲循环
    public static final int SINGLE_RECICLE = 2;
    //顺序播放
    public static final int LINE = 3;
    //播放下一首
    public static final int OPER_NEXT = 1;
    //播放上一首
    public static final int OPER_PREVIOUS=-1;
    //保存当前播放模式
    public int MODE = MENU_RECICLE;
    //用于显示播放列表的数据源
    private List<Map<String,Object>> musicList=new ArrayList<>();
    //当前播放的歌曲索引
    private int currIndex=-1;
    //获取歌曲列表
    public List<Map<String,Object>> refeshMusicList(String musicUrl){
        File musicDir=new File(musicUrl);
        if(musicDir!=null&&musicDir.isDirectory()){
            File[] musicFile=musicDir.listFiles(new MusicFilter());
            if(musicFile!=null){
                musicList=new ArrayList<Map<String,Object>>();
                for(int i=0;i<musicFile.length;i++){
                    File currMusic=musicFile[i];
                    //获取当前目录的名称和绝对路径
                    String abPath=currMusic.getAbsolutePath();
                    String musicName=currMusic.getName();
                    Map<String,Object> currMusicMap=new HashMap<>();
                    currMusicMap.put("musicName", musicName);
                    currMusicMap.put("musicAbPath", abPath);
                    musicList.add(currMusicMap);
                }

            }else{
                musicList = new ArrayList<Map<String,Object>>();
            }
        }else{
            musicList = new ArrayList<Map<String,Object>>();
        }
        return musicList;
    }
    //播放音乐
    public String playMusic(int musicPo) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
        Map<String,Object> currMusic=musicList.get(musicPo);
        Log.i("i",(String)currMusic.get("musicAbPath"));
        String musicUrl=(String)currMusic.get("musicAbPath");
        mediaPlayer.reset();
        mediaPlayer.setDataSource(musicUrl);
        mediaPlayer.prepare();
        mediaPlayer.start();
        currIndex=musicPo;
        return musicUrl;
    }
    //按照播放模式播放音乐
    public String playNew(int operCode) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
        musicList = refeshMusicList("/storage/emulated/0/$MuMu共享文件夹/music");
        String path = "";
        for(Map<String,Object> m:musicList){
            Log.i("i",m.get("musicName").toString());
        }
        if(musicList.size()>0){
            Log.i("i",""+MODE);
            switch(MODE){
                case MENU_RECICLE:
                    int newIndex=0;
                    switch(operCode){
                        case OPER_NEXT:
                            newIndex=currIndex+1;
                            if(newIndex>=musicList.size()){
                                newIndex=0;
                            }
                            break;
                        case OPER_PREVIOUS:
                            newIndex=currIndex-1;
                            if(newIndex<0){
                                newIndex=musicList.size()-1;
                            }
                            break;
                    }
                    path = playMusic(newIndex);
                    break;
                case SINGLE_RECICLE:
                    Log.i("88  ",currIndex+"");
                    path = playMusic(currIndex);
                    break;
                case RADOM:
                    Random rd=new Random();
                    int randomIndex=rd.nextInt(musicList.size());
                    path = playMusic(randomIndex);
                    break;
                case LINE:
                    newIndex=0;
                    switch(operCode){
                        case OPER_NEXT:
                            newIndex=currIndex+1;
                            if(newIndex>=musicList.size()){
                                newIndex=0;
                            }
                            break;
                        case OPER_PREVIOUS:
                            newIndex=currIndex-1;
                            if(newIndex<0){
                                newIndex=musicList.size()-1;
                            }
                            break;
                    }
                    path = playMusic(newIndex);
                    break;

            }
        }
        return path;
    }
    //构造函数
    public MusicService() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource("/storage/emulated/0/$MuMu共享文件夹/music/薛之谦 - 刚刚好.mp3");
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            currIndex = 0;
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  通过 Binder 来保持 Activity 和 Service 的通信
    public MyBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    //播放/暂停
    public void playOrPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }
    //停止
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource("/storage/emulated/0/$MuMu共享文件夹/music/薛之谦 - 刚刚好.mp3");
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
//内部类
class MusicFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return (name.endsWith(".mp3"));//返回当前目录所有以.mp3结尾的文件
    }
}