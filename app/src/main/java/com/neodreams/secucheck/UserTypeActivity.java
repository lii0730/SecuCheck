package com.neodreams.secucheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class UserTypeActivity extends BaseActivity  // AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Common.setFullScreen(getWindow().getDecorView());
        setContentView(R.layout.activity_user_type);
//        Common.CurrAct = this;
        Common.CheckOrList = true;

        // 홈버튼 추가
        Common.addHomeBtn(this);
        // 오늘 날짜 추가
        Common.addDateStr(this);
    }

    // 최종퇴사자
    public void onButtonUser1Clicked(View v)
    {
        Common.UserType = Common.USERTYPE_LAST;
        this.goNext();
    }
    // 최종퇴사자 이력
    public void onButtonUser3Clicked(View v)
    {
        Common.UserType = Common.USERTYPE_LAST;
        Common.CheckOrList = false;
        this.goNext();
    }

    // 당직자
    public void onButtonUser2Clicked(View v)
    {
        Common.UserType = Common.USERTYPE_DUTY;
        this.goNext();
    }
    // 당직자 이력
    public void onButtonUser4Clicked(View v)
    {
        Common.UserType = Common.USERTYPE_DUTY;
        Common.CheckOrList = false;
        this.goNext();
    }

    private void goNext()
    {
        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
        startActivity(intent);
        finish();
    }
}
