package com.neodreams.secucheck;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.neodreams.secucheck.OBJMSGS.NetMSGS;

public class BaseActivity extends AppCompatActivity
{
    public static int MESSAGE_WHAT_TIMER = 1;
    public static int MESSAGE_WHAT_CANCEL = 2;
    public static int LIMIT_TIME = 60000;

    Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == MESSAGE_WHAT_TIMER)
            {
                Log.d("MESSAGE_WHAT_TIMER", " : MESSAGE_WHAT_TIMER ");
                Common.CurrAct = null;
                finish();
            }
            else if (msg.what == NetMSGS.OP1106_USERINFORES)
            {
                RCV(msg.what, msg.arg1);
            }
        }
    };

    protected void resetTimer()
    {
        Log.d("reset timer", "reset timer");
        handlerDelayStop(MESSAGE_WHAT_TIMER);
        handlerDelayStart(MESSAGE_WHAT_TIMER, LIMIT_TIME);
    }

    protected void resetTimer(int time)
    {
        Log.d("reset timer", "reset timer");
        handlerDelayStop(MESSAGE_WHAT_TIMER);
        handlerDelayStart(MESSAGE_WHAT_TIMER, time * 1000);
    }

    public void RCV(int MSG, int code)
    {
    }

    public void RCV2(int MSG, int code)
    {
        mHandler.sendMessage(Message.obtain(mHandler, MSG, code, 0));
    }

    public void handlerDelayStop(int what)
    {
        Log.d("handlerDelayStop", " : handlerDelayStop ");
        mHandler.removeMessages(what);
    }

    public void handlerDelayStart(int what, int time)
    {
        Log.d("handlerDelayStart", " : handlerDelayStart ");
        mHandler.sendEmptyMessageDelayed(what, time);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            Common.CurrAct = this;
            Log.d("TouchEvent", "scroll action");
            resetTimer();

            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {

        handlerDelayStart(MESSAGE_WHAT_TIMER, LIMIT_TIME);
        super.onStart();

    }

    @Override
    protected void onStop() {
        handlerDelayStop(MESSAGE_WHAT_CANCEL);
        super.onStop();

    }
}
