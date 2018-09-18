package hfad.com.music;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Handler mHandler = new Handler();

    private static final String TAG = "MainActivity";
    private MusicService.MyBinder mMyBinder;//使用这个变量指示MusicService
//    private MediaService mMediaService;

    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private Button preciousButton;
    private SeekBar mSeekBar;
    private TextView mTextView;

    private TextView mVolume;
    private int maxVolume,currentVolume;

    private AudioManager audioManager;


    //进度条下面的当前进度文字，将毫秒化为m:ss格式
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat time = new SimpleDateFormat("m:ss");
    //“绑定”服务的intent
    Intent MediaServiceIntent;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final SeekBar soundBar = (SeekBar)findViewById(R.id.sound);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mVolume = (TextView) findViewById(R.id.mVolume);

        maxVolume =audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取系统音量最大值
        soundBar.setMax(maxVolume);//拖动条最高值与系统最大声匹配
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        soundBar.setProgress(currentVolume);
        mVolume.setText(currentVolume*100/maxVolume+"%");
        soundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                soundBar.setProgress(currentVolume);
                mVolume.setText(currentVolume*100/maxVolume+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        iniView();
        MediaServiceIntent = new Intent(this, MusicService.class);


        //判断权限够不够，不够就给
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        } else {
            //够了就设置路径等，准备播放
            bindService(MediaServiceIntent, ServiceConnection, BIND_AUTO_CREATE);
        }
    }

    //获取到权限回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[]permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bindService(MediaServiceIntent, ServiceConnection, BIND_AUTO_CREATE);//绑定服务
                } else {
                    Toast.makeText(this, "权限不够获取不到音乐，程序将退出", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }


    private ServiceConnection ServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (MusicService.MyBinder) service;
//            mMediaService = ((MediaService.MyBinder) service).getInstance();
            mSeekBar.setMax(mMyBinder.getProgress());//获取音乐资源最大时间

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //这里很重要，如果不判断是否来自用户操作进度条，会不断执行下面语句块里面的逻辑，然后就会卡顿卡顿
                    if(fromUser){
                        mMyBinder.seekToPosition(seekBar.getProgress());//指定进度条位置
//                    mMediaService.mMediaPlayer.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mHandler.post(mRunnable);

            Log.d(TAG, "Service与Activity已连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    private void iniView() {
        playButton = (Button) findViewById(R.id.play);
        pauseButton = (Button) findViewById(R.id.pause);
        nextButton = (Button) findViewById(R.id.next);
        preciousButton = (Button) findViewById(R.id.precious);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mTextView = (TextView) findViewById(R.id.text1);

        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        preciousButton.setOnClickListener(this);
    }

//j控制点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                mMyBinder.playMusic();
                break;
            case R.id.pause:
                mMyBinder.pauseMusic();
                break;
            case R.id.next:
                mMyBinder.nextMusic();
                break;
            case R.id.precious:
                mMyBinder.preciousMusic();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //我们的handler发送是定时1s发送的，如果不关闭，MediaPlayer release掉了还在获取getCurrentPosition就会爆IllegalStateException错误
        mHandler.removeCallbacks(mRunnable);

        mMyBinder.closeMedia();//退出播放器
        unbindService(ServiceConnection);//绑定服务
    }

   /*
    *更新ui时间
    */
    private Runnable mRunnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            mSeekBar.setProgress(mMyBinder.getPlayPosition());//获取当前播放位置
            mTextView.setText(time.format(mMyBinder.getPlayPosition()) + "s");//显示音乐时间
            mHandler.postDelayed(mRunnable, 1000);//设置延迟提交时间为1秒
        }
    };

}


