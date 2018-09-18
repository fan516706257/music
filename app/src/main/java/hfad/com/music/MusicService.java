package hfad.com.music;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;



public class MusicService extends Service {

    private static final String TAG = "MediaService";
    private MyBinder mBinder = new MyBinder();
    //标记当前歌曲的序号
    private int i = 0;
    //歌曲路径
    @SuppressLint("SdCardPath")
    private String[] musicPath = new String[]{
            Environment.getExternalStorageDirectory() + "/Movies/1.mp3",
            Environment.getExternalStorageDirectory() + "/Movies/2.mp3",
            Environment.getExternalStorageDirectory() + "/Movies/3.mp3",

    };
    //初始化MediaPlayer
    public MediaPlayer mMediaPlayer = new MediaPlayer();


    public MusicService() {
        iniMediaPlayerFile(i);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {

//        /**
//         *  获取MediaService.this（方便在ServiceConnection中）
//         *
//         * *//*
//        public MediaService getInstance() {
//            return MediaService.this;
//        }*/
        /**
         * 播放音乐
         */
        public void playMusic() {
            if (!mMediaPlayer.isPlaying()) {
                //如果还没开始播放，就开始
                mMediaPlayer.start();
            }
        }

        /**
         * 暂停播放
         */
        public void pauseMusic() {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }

        /**
         * 重置
         */
        public void resetMusic() {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.reset();
                iniMediaPlayerFile(i);
            }
        }

        /**
         * 关闭播放器
         */
        public void closeMedia() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }

        /**
         * 下一首
         */
        public void nextMusic() {
            if (mMediaPlayer != null && i < 3 && i >= 0) {
                //切换歌曲reset()很重要很重要很重要，没有会报IllegalStateException
                mMediaPlayer.reset();
                iniMediaPlayerFile(i + 1);
                //这里的if只要是为了不让歌曲的序号越界，因为只有4首歌
                if (i == 2) {

                } else {
                    i = i + 1;
                }
                playMusic();
            }
        }

        /**
         * 上一首
         */
        public void preciousMusic() {
            if (mMediaPlayer != null && i < 3 && i > 0) {
                mMediaPlayer.reset();
                iniMediaPlayerFile(i - 1);
                if (i == 1) {

                } else {

                    i = i - 1;
                }
                playMusic();
            }
        }

        public int getProgress() {

            return mMediaPlayer.getDuration();//获取歌曲长度
        }

        public int getPlayPosition() {

            return mMediaPlayer.getCurrentPosition();//获取播放位置
        }

        public void seekToPosition(int msec) {
            mMediaPlayer.seekTo(msec);//播放指定位置
        }



    }


    /**
     * 添加file文件到MediaPlayer对象并且准备播放音频
     */
    private void iniMediaPlayerFile(int dex) {
        //获取文件路径
        try {
            //此处的两个方法需要捕获IO异常
            //设置音频文件到MediaPlayer对象中
            mMediaPlayer.setDataSource(musicPath[dex]);
            //让MediaPlayer对象准备播放
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.d(TAG, "设置资源，准备阶段出错");
            e.printStackTrace();
        }
    }

}
