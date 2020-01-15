package com.neodreams.secucheck;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.neodreams.secucheck.OBJMSGS.OBJ_DEPART;

public class DeptListActivity extends BaseActivity  // AppCompatActivity
{
    LinearLayout LL;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Common.setFullScreen(getWindow().getDecorView());
        setContentView(R.layout.activity_dept_list);
        Common.CurrAct = this;

        this.LL = findViewById(R.id.listLayout);

        // 홈버튼 추가
        Common.addHomeBtn(this);
        // 오늘 날짜 추가
        Common.addDateStr(this);

        if (Common.DeviceInfo != null && Common.DeviceInfo.Departs.size() > 0)
        {
            for (OBJ_DEPART dept : Common.DeviceInfo.Departs)
                this.addDeptBtn(dept);
        }
    }

    private ImageButton addDeptBtn(OBJ_DEPART dept)
    {
        FrameLayout fl = new FrameLayout(this);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.setLayoutParams(llp);

        ImageButton ib = new ImageButton(this);
        ib.setImageResource(R.drawable.i04_btndept);
        ib.setBackgroundColor(0x00000000);
//        ib.setTag(dept.DepartCode);
        ib.setTag(dept);

        TextView tv = new TextView(this);
        tv.setTextSize(100);
        tv.setText(dept.DepartName);

        if (dept.Checked)
        {
            ib.setAlpha(0.7f);
            tv.setTextColor(0xFF909090);
        }
        else
        {
            ib.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //String tag = view.getTag().toString();
                    Common.CurrDept = (OBJ_DEPART) view.getTag();

                    Intent intent = new Intent(getApplicationContext(), SecuCheckActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            tv.setTextColor(0xFFFFFFFF);
        }

        FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(1278, 203);
        ib.setLayoutParams(ilp);

        fl.addView(ib);

        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.gravity = Gravity.CENTER;
        tv.setLayoutParams(tlp);

        fl.addView(tv);

        this.LL.addView(fl);

        return null;
    }

    // 버튼을 눌렀을 때 처리
    public void onButtonClicked(View v)
    {
    }
}
