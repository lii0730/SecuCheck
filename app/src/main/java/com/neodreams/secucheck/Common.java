package com.neodreams.secucheck;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.neodreams.neolibnetwork4android.OBJMSG;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1102_DEVICEINFORES;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1106_USERINFORES;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1202_CHECKLISTRES;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_FF00_DEVICESTATUS;
import com.neodreams.secucheck.OBJMSGS.OBJ_CHECKDATA;
import com.neodreams.secucheck.OBJMSGS.OBJ_DEPART;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

public class Common
{
    // USB. 아두이노.
    public static UsbSerialPort SerialPort;
    // 네트워크
    public static NetMNG netmng;

    // 코드
    public static byte USERTYPE_NONE = 0;
    public static byte USERTYPE_LAST = 1;       // 최종 퇴사자
    public static byte USERTYPE_DUTY = 2;       // 당직자

    public static byte CODE_OK = 1;
    public static byte CODE_ERROR = 0x7F;

    public static byte CODE_USERINFO_NOTFOUND = 2;      // 일치하는 사용자 없음.
    public static byte CODE_USERINFO_NOMATCHDEPT = 3;   // 단말에 설정된 부서의 직원이 아님.

    // 단말 정보
    public static OBJMSG_1102_DEVICEINFORES DeviceInfo = null;
    // 휴일 정보
    public static ArrayList<Integer> Holidays = null;

    // 보안점검에 필요한 데이터
    public static byte UserType = Common.USERTYPE_NONE;     // 사용자 타입
    public static boolean CheckOrList = true;               // true : 보안점검, flase : 목록확인
    public static OBJMSG_1106_USERINFORES User = null;      // 사용자 정보
    public static OBJ_DEPART CurrDept = null;               // 점검중인 부서

    // 금일 최종퇴사자 보안점검 목록
    public static HashMap<String, OBJ_CHECKDATA> CheckDataByLast;

    // 보안점검 이력
    public static OBJMSG_1202_CHECKLISTRES CheckData4List;

    // 첫화면
    public static MainActivity MainAct;
    // 현재 화면
    public static BaseActivity CurrAct;

    public static int DataDay = 0;

    public static void InitCommon()
    {
        Common.DeviceInfo = new OBJMSG_1102_DEVICEINFORES();
        Common.DeviceInfo.OnTime = 800;
        Common.DeviceInfo.OffTime = 2359;

        Common.CheckDataByLast = new HashMap<String, OBJ_CHECKDATA>();

        Common.ResetCommon();
    }

    public static void ResetCommon()
    {
        Common.UserType = Common.USERTYPE_NONE;
        Common.User = null;
        Common.CurrAct = null;
        Common.CurrDept = null;
    }

    public static void ResetCommon4NewDay(int day)
    {
        if (Common.DataDay != day)
        {
            if (Common.DeviceInfo != null && Common.DeviceInfo.Departs.size() > 0)
            {
                Common.CheckDataByLast.clear();

                for (OBJ_DEPART dept : Common.DeviceInfo.Departs)
                {
                    dept.Checked = false;

                    Common.CheckDataByLast.put(dept.DepartCode, null);
                }

                Common.DataDay = day;
            }
        }
    }

    // 메시지 서버 전송
    public static void Send(OBJMSG msg)
    {
        Common.netmng.Send(msg);
    }

    // 상태 전송
    public static void SendStatus()
    {
        try
        {
            if(DeviceInfo.DeviceSeq > 0)
            {
                OBJMSG_FF00_DEVICESTATUS msg = new OBJMSG_FF00_DEVICESTATUS();
                msg.DeviceSeq = DeviceInfo.DeviceSeq;
                msg.CPU = 1;
                msg.MEM = 2;
                msg.HDD = 3;

                Common.netmng.Send(msg);
            }
        }
        catch (Exception ex)
        {
            Log.d("Send status ERROR >> ", ex.getMessage());
        }
    }

    // 오늘 날짜 리턴. yyyy년 MM월 dd일
    public static String GetTodayStr()
    {
        Date from = new Date();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        String to = transFormat.format(from);

        return to;
    }

    /////////////////////////////////////// UI
    // 화면에 오늘 날짜 추가
    public static void addDateStr(Activity ac)
    {
        TextView rVal = new TextView(ac);
        rVal.setTextSize(COMPLEX_UNIT_PX, 34);
        rVal.setTextColor(0xFFFFFFFF);
        rVal.setText(Common.GetTodayStr());

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.leftMargin = 508;
        lp.topMargin = 58;

        rVal.setLayoutParams(lp);

        ConstraintLayout cl = ac.findViewById(R.id.mainLayout);
        cl.addView(rVal);
    }

    // 화면에 홈 버튼 추가
    public static void addHomeBtn(Activity ac)
    {
        ImageButton ib = new ImageButton(ac);
        ib.setImageResource(R.drawable.btn_home);
        ib.setBackgroundColor(0x00000000);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(171, 171);
        lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;

        ib.setLayoutParams(lp);

        ConstraintLayout cl = ac.findViewById(R.id.mainLayout);
        cl.addView(ib);

        ib.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (Common.CurrAct != null)
                        {
                            Common.CurrAct.finish();
                            Common.CurrAct = null;
                        }
                    }
                }
        );
    }

    // full screen 만들기
    public static void setFullScreen(View view)
    {
        int uiOptions = view.getSystemUiVisibility();
        int newUiOptions = uiOptions;

        newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        view.setSystemUiVisibility(newUiOptions);
    }

    // alert
    public static PopupWindow cPopupWin;
    public static void showAlert(Activity act, String title, String body, View.OnClickListener clcc, View.OnClickListener clok, View.OnClickListener clre)
    {
        View pop = act.getLayoutInflater().inflate(R.layout.popupactivity_alert, null);
        Common.cPopupWin = new PopupWindow(pop, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        Common.cPopupWin.setFocusable(true);
        Common.cPopupWin.showAtLocation(pop, Gravity.CENTER, 0, 0);

        TextView txttitle = pop.findViewById(R.id.txt_title);
        txttitle.setText(title);
        TextView txtbody = pop.findViewById(R.id.txt_body);
        txtbody.setText(body);

        ImageButton btncc = pop.findViewById(R.id.btncc);
        ImageButton btnok = pop.findViewById(R.id.btnok);
        ImageButton btnre = pop.findViewById(R.id.btnre);

        if(clcc == null)
            btncc.setVisibility(View.GONE);
        else
            btncc.setOnClickListener(clcc);
        if(clok == null)
            btnok.setVisibility(View.GONE);
        else
            btnok.setOnClickListener(clok);
        if(clre == null)
            btnre.setVisibility(View.GONE);
        else
            btnre.setOnClickListener(clre);
    }
}
