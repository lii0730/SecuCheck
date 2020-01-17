package com.neodreams.secucheck;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.neodreams.secucheck.OBJMSGS.OBJMSG_1203_CHECKDATA;
import com.neodreams.secucheck.OBJMSGS.OBJ_CHECKDATA;
import com.neodreams.secucheck.OBJMSGS.OBJ_DEPART;

import org.w3c.dom.Text;

import java.util.Date;

public class SecuCheckActivity extends BaseActivity  // AppCompatActivity
{
    RadioGroup[] RG;

    public String CheckNote = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Common.setFullScreen(getWindow().getDecorView());
        setContentView(R.layout.activity_secu_check);
        Common.CurrAct = this;

        // 홈버튼 추가
        Common.addHomeBtn(this);
        // 오늘 날짜 추가
        Common.addDateStr(this);

        // 부서 이름 넣기
        if (Common.CurrDept != null)
        {
            TextView tv = findViewById(R.id.textViewDeptName);
            tv.setText(Common.CurrDept.DepartName);
        }

        // 점검자 이름 넣기
        if (Common.User != null)
        {
            TextView tv = findViewById(R.id.textViewUserName);
            tv.setText(Common.User.Name);
        }

        RadioGroup rd1 = findViewById(R.id.radiog1);
        RadioGroup rd2 = findViewById(R.id.radiog2);
        RadioGroup rd3 = findViewById(R.id.radiog3);
        RadioGroup rd4 = findViewById(R.id.radiog4);
        RadioGroup rd5 = findViewById(R.id.radiog5);

        this.RG = new RadioGroup[] {rd1, rd2, rd3, rd4, rd5};
    }

    // 특이사항 부분 눌렀을 때 처리
    public void onButtonNoteClicked(View v)
    {
        resetTimer(180);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("특이사항");
        alert.setMessage("특이사항을 입력해 주세요.");

        final EditText note = new EditText(this);
        note.setText(CheckNote);
        alert.setView(note);

        alert.setPositiveButton("확인", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                resetTimer();
                CheckNote = note.getText().toString();

                TextView tv = findViewById(R.id.txtCheckNote);
                tv.setText(CheckNote);

                Common.setFullScreen(getWindow().getDecorView());
            }
        });

        alert.show();
    }

    public void onNMVBtnClicked(View v)
    {
        resetTimer();
    }

    // 확인 버튼을 눌렀을 때 처리
    public void onButtonCommitClicked(View v)
    {
        resetTimer();
        byte[] data = new byte[5];
        boolean check = true;

        for (int i=0; i<5; i++)
        {
            int bid = this.RG[i].getCheckedRadioButtonId();

            if (bid > 0)
            {
                RadioButton rb = findViewById(bid);
                byte tmp = Byte.parseByte(rb.getTag().toString());

                data[i] = tmp;
            }
            else
            {
                Toast.makeText(this, "모든 항목을 확인해 주세요!!", Toast.LENGTH_LONG).show();
                check = false;
                break;
            }
        }

        if (check)
        {
            OBJ_CHECKDATA checkdata = new OBJ_CHECKDATA();
            checkdata.DeviceSeq = Common.DeviceInfo.DeviceSeq;
            checkdata.DepartCode = Common.CurrDept.DepartCode;
            checkdata.EMPNO = Common.User.EMPNO;
            checkdata.UserType = Common.UserType;

            checkdata.Data1 = data[0];
            checkdata.Data2 = data[1];
            checkdata.Data3 = data[2];
            checkdata.Data4 = data[3];
            checkdata.Data5 = data[4];

            checkdata.CheckNote = CheckNote;
            checkdata.CheckTime = new Date(System.currentTimeMillis());

            // 서버로 전송
            OBJMSG_1203_CHECKDATA msg = new OBJMSG_1203_CHECKDATA();
            msg.Data = checkdata;
            Common.Send(msg);

            // 부서 완료 처리
            if (Common.UserType == Common.USERTYPE_LAST)
            {
                checkdata.UserName = Common.User.Name;

                Common.CheckDataByLast.put(Common.CurrDept.DepartCode, checkdata);
            }
            else
            {
                for (OBJ_DEPART dept : Common.DeviceInfo.Departs)
                {
                    if (dept.DepartCode.equals(Common.CurrDept.DepartCode))
                    {
                        dept.Checked = true;
                        break;
                    }
                }
            }

            ComProc();
        }
    }

    private void ComProc()
    {
        boolean comp = Common.UserType == Common.USERTYPE_LAST; // 최종퇴사자

        if (Common.UserType == Common.USERTYPE_DUTY) // usertype = 2. 당직자
        {
            comp = true;
            for (OBJ_DEPART dept : Common.DeviceInfo.Departs)
            {
                if (!dept.Checked)
                {
                    comp = false;
                    break;
                }
            }
        }

        if(comp)
        {
            FrameLayout fl = findViewById(R.id.compframe);
            fl.setVisibility(View.VISIBLE);

            resetTimer(2);
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), DeptListActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
