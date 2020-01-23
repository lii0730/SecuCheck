package com.neodreams.secucheck;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.neodreams.secucheck.OBJMSGS.NetMSGS;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1205_TODAYCHECKLISTREQ;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
    DevicePolicyManager devicePolicyManager;
    private Timer timer;
    public boolean isOn = false;
    public int ConfCnt = 0;

    public Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == NetMSGS.OP1102_DEVICEINFORES)
            {
                Calendar now = Calendar.getInstance();
                int currD = now.get(Calendar.DAY_OF_MONTH);

                Common.ResetCommon4NewDay(currD);
                DownloadImg();

                // 금일 보안점검 내용 요청
                if (Common.DeviceInfo.Departs != null && Common.DeviceInfo.Departs.size() > 0)
                {
                    OBJMSG_1205_TODAYCHECKLISTREQ smsg = new OBJMSG_1205_TODAYCHECKLISTREQ();
                    smsg.DeviceSeq = Common.DeviceInfo.DeviceSeq;
                    Common.Send(smsg);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Common.MainAct = this;

        // 초기화
        this.init();

        Common.netmng = new NetMNG();
        Common.netmng.Init();
    }

    // 초기화
    private void init()
    {
        // 설정
        SharedPreferences SP = getSharedPreferences("config", MODE_PRIVATE);
        ConfigActivity.SERVERIP = SP.getString("SERVER_IP", "");
        ConfigActivity.SERVERPORT = SP.getInt("SERVER_PORT", 19801);
        ConfigActivity.HTTPPORT = SP.getString("HTTPPORT", "8880");

        // 디바이스 메니저 초기화 및 관리자 권한 확인
        devicePolicyManager = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(getApplicationContext(), DeviceAdminReceiver.class);
        if(!devicePolicyManager.isAdminActive(componentName))
        {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            startActivityForResult(intent, 0);
        }

        // 조직도 표출
        ShowImg();

        // 상태 초기화
        Common.InitCommon();

        // 타이머 초기화
        timer = new Timer();
        timer.schedule(timerTask, 0, 6000);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        turnonScreen();
        Common.setFullScreen(getWindow().getDecorView());
        Common.ResetCommon();

        Common.netmng.CheckClient();
    }

    @Override
    protected void onDestroy()
    {
        timer.cancel();
        super.onDestroy();
    }

    // 화면 켜기
    private void turnonScreen()
    {
        // 화면 켜기
        Window win = getWindow();
        KeyguardManager kg = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if(kg != null)
        {
            win.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

            if (Build.VERSION.SDK_INT >= 27)
            {
                this.setShowWhenLocked(true);
                this.setTurnScreenOn(true);
                kg.requestDismissKeyguard(this, null);
            }
            else if (Build.VERSION.SDK_INT == 26)
            {
                win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                kg.requestDismissKeyguard(this, null);
            }
            else
            {
                PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "onofftest:waketag");
                wl.acquire();

                win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                win.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

                wl.release();
            }
        }

        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Timer.
    // 특정 시간에 단말의 화면을 켜거나 끄기 위함.
    private TimerTask timerTask = new TimerTask()
    {
        @Override
        public void run()
        {
            Message msg = handler.obtainMessage();
            handler.sendMessage(msg);
        }

        final Handler handler = new Handler()
        {
            public  void  handleMessage(Message msg)
            {
                Calendar now = Calendar.getInstance();
                int currD = now.get(Calendar.DAY_OF_MONTH);
                Common.ResetCommon4NewDay(currD);

                int currH = now.get(Calendar.HOUR_OF_DAY);
                int currM = now.get(Calendar.MINUTE);

                int nowTime = (currH * 100) + currM;

                if(Common.DeviceInfo != null)
                {
                    if (nowTime >= Common.DeviceInfo.OnTime && nowTime <= Common.DeviceInfo.OffTime)
                        ScreenCtrl(true);
                    else
                        ScreenCtrl(false);
                }

                Common.SendStatus();
                ConfCnt = 0;
            }
        };
    };

    // on=true : 단말 화면 켜기, on=false : 단말 화면 끄기
    public void ScreenCtrl(boolean on)
    {
        FrameLayout fl = findViewById(R.id.blackframe);

        if(on)
            fl.setVisibility(View.GONE);
        else
            fl.setVisibility(View.VISIBLE);

        isOn = on;
        Common.setFullScreen(getWindow().getDecorView());
    }

    // 설정 버튼 클릭
    public void onButtonConfClicked(View v)
    {
        ConfCnt++;

        if(ConfCnt > 4)
        {
            Toast.makeText(getApplicationContext(), "config", Toast.LENGTH_LONG).show();
            //Config.Save();

            Intent it = new Intent(this, ConfigActivity.class);
            startActivity(it);
        }
    }

    // 보안점검 버튼 클릭
    public void onButtonSecucheckClicked(View v)
    {
        //Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
        Intent intent = new Intent(getApplicationContext(), UserTypeActivity.class);
        startActivity(intent);
    }

    // 조직도 표출
    public void ShowImg()
    {
        File file = new File(getFilesDir(), "orgimg");

        if(file.exists())
        {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            ImageView iv = findViewById(R.id.statusImgViewer);
            iv.setImageBitmap(bitmap);
        }
    }

    // 조직도 다운로드
    public void DownloadImg()
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    String path = "http://" + ConfigActivity.SERVERIP + ":" + ConfigActivity.HTTPPORT + "/contentsFile/ORGCHART/";
                    path += String.valueOf(Common.DeviceInfo.DeviceSeq);

                    URL url = new URL(path);

                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream is = conn.getInputStream();

                    File file = new File(getFilesDir(), "orgimg");
                    OutputStream os = new FileOutputStream(file);

                    byte[] bf = new byte[2048];
                    int len = 0;
                    while((len=is.read(bf)) > 0)
                    {
                        os.write(bf, 0, len);
                    }

                    os.close();
                    is.close();
                }
                catch (MalformedURLException mue)
                {
                    mue.printStackTrace();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }

            }
        };
        t.start();

        try
        {
            t.join();

            ShowImg();
        }
        catch (InterruptedException ie)
        {
            ie.printStackTrace();
        }
    }
}
