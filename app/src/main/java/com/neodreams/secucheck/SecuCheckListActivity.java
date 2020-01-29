package com.neodreams.secucheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.neodreams.secucheck.OBJMSGS.OBJMSG_1201_CHECKLISTREQ;
import com.neodreams.secucheck.OBJMSGS.OBJ_CHECKDATA;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

public class SecuCheckListActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Common.setFullScreen(getWindow().getDecorView());
        setContentView(R.layout.activity_secu_check_list);
//        Common.CurrAct = this;

        // 홈버튼 추가
        Common.addHomeBtn(this);
//        ShowList();

        // 부서 이름 넣기
        if (Common.CurrDept != null)
        {
            TextView tv = findViewById(R.id.textViewDeptName);
            tv.setText(Common.CurrDept.DepartName);
        }

        if (Common.UserType == Common.USERTYPE_LAST)
        {
            ImageButton ib = findViewById(R.id.btnback);
            ib.setVisibility(View.GONE);
        }

        Common.CheckData4List = null;

        OBJMSG_1201_CHECKLISTREQ smsg = new OBJMSG_1201_CHECKLISTREQ();
        smsg.DepartCode = Common.CurrDept.DepartCode;
        smsg.UserType = Common.UserType;

        Common.Send(smsg);
    }

    @Override
    public void RCV(int MSG, int code)
    {
        ShowList();
    }

    public void ShowList()
    {
        if(Common.CheckData4List == null || Common.CheckData4List.CheckList.size() < 1)
        {
            TextView tv = findViewById(R.id.txtnolist);
            tv.setVisibility(View.VISIBLE);
        }
        else
        {
            TableLayout tl = findViewById(R.id.listctrl);

            int colWidth[] = new int[] {220, 365, 168, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98};

            int idx = 0;

            //for(int i=0; i<5; i++)
            for (OBJ_CHECKDATA data : Common.CheckData4List.CheckList)
            {
                idx++;
                if(idx > 5)
                    break;

                TableRow tr = new TableRow(this);

                for(int j=0; j<colWidth.length; j++)
                {
                    FrameLayout fl = new FrameLayout(this);
                    fl.setLayoutParams(new TableRow.LayoutParams(colWidth[j], 114));

                    FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tlp.gravity = Gravity.CENTER;
                    TextView tv = new TextView(this);
                    tv.setLayoutParams(tlp);
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    tv.setTextSize(COMPLEX_UNIT_PX, 38);

                    String tmp = "";
                    switch (j)
                    {
                        case 0:
                            SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");
                            tmp = transFormat.format(data.CheckTime);
                            break;
                        case 1:
                            tmp = Common.CurrDept.DepartName;
                            break;
                        case 2:
                            tmp = data.UserName;
                            break;
                        case 3:
                            if(data.Data1 == 1)
                                tmp = "O";
                            break;
                        case 4:
                            if(data.Data1 == 2)
                                tmp = "O";
                            break;
                        case 5:
                            if(data.Data2 == 1)
                                tmp = "O";
                            break;
                        case 6:
                            if(data.Data2 == 2)
                                tmp = "O";
                            break;
                        case 7:
                            if(data.Data3 == 1)
                                tmp = "O";
                            break;
                        case 8:
                            if(data.Data3 == 2)
                                tmp = "O";
                            break;
                        case 9:
                            if(data.Data4 == 1)
                                tmp = "O";
                            break;
                        case 10:
                            if(data.Data4 == 2)
                                tmp = "O";
                            break;
                        case 11:
                            if(data.Data5 == 1)
                                tmp = "O";
                            break;
                        case 12:
                            if(data.Data5 == 2)
                                tmp = "O";
                            break;
                    }

                    tv.setText(tmp);

                    fl.addView(tv);
                    tr.addView(fl);
                }

                tl.addView(tr);
            }
        }
    }

    public void onBackClicked(View v)
    {
        if(Common.UserType == Common.USERTYPE_DUTY)
        {
            Intent intent = new Intent(getApplicationContext(), DeptListActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
