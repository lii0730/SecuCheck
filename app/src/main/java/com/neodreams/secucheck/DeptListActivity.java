package com.neodreams.secucheck;

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
    final int bpp = 4;
    int currPage = 1;
    int totPage = 1;
    ImageButton BtnPrev, BtnNext;

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

        BtnPrev = findViewById(R.id.btnprev);
        BtnNext = findViewById(R.id.btnnext);

        totPage = (int)Math.ceil(Common.DeviceInfo.Departs.size() / (double)bpp);

        if(totPage < 2)
        {
            BtnPrev.setVisibility(View.GONE);
            BtnNext.setVisibility(View.GONE);
        }

        if (Common.DeviceInfo.Departs != null && Common.DeviceInfo.Departs.size() > 0)
            ShowPage(currPage);

//        if (Common.DeviceInfo != null && Common.DeviceInfo.Departs.size() > 0)
//        {
//            for (OBJ_DEPART dept : Common.DeviceInfo.Departs)
//                this.addDeptBtn(dept);
//        }
    }

    private void ShowPage(int page)
    {
        this.LL.removeAllViews();

        BtnPrev.setAlpha(1F);
        BtnNext.setAlpha(1F);

        if(page < 2)
            BtnPrev.setAlpha(0.3F);
        else if(page >= totPage)
            BtnNext.setAlpha(0.3F);

        int si = bpp * (page - 1);
        int ei = si + bpp;

        if(ei > Common.DeviceInfo.Departs.size())
            ei = Common.DeviceInfo.Departs.size();

        for(int i=si; i<ei; i++)
        {
            OBJ_DEPART dept = Common.DeviceInfo.Departs.get(i);

            if(dept != null)
                this.addDeptBtn(dept);
        }
    }

    public void onPrevClicked(View v)
    {
        currPage--;

        if(currPage < 1)
            currPage = 1;

        ShowPage(currPage);
    }

    public void onNextClicked(View v)
    {
        currPage++;

        if(currPage > totPage)
            currPage = totPage;

        ShowPage(currPage);
    }

    private ImageButton addDeptBtn(OBJ_DEPART dept)
    {
        FrameLayout fl = new FrameLayout(this);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.setLayoutParams(llp);

        ImageButton ib = new ImageButton(this);
        ib.setImageResource(R.drawable.i04_btndept);
        ib.setBackgroundColor(0x00000000);
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
