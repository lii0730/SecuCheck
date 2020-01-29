package com.neodreams.secucheck;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.neodreams.secucheck.OBJMSGS.NetMSGS;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1105_USERINFOREQ;
import com.neodreams.secucheck.OBJMSGS.OBJ_CHECKDATA;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserInfoActivity extends BaseActivity  // AppCompatActivity
{
    TextView[] TxtView;
    char[] empno = new char[8];
    byte currIndex = 0;

    private PopupWindow mPopWin;

    //////////////////////////////////////////////////////////////////////// USB
    private UsbManager mUsbManager;
    private UsbSerialPort mSerialPort;

    public static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private byte[] bf;
    private int idx = 0;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener()
            {
                @Override
                public void onRunError(Exception e) { }

                @Override
                public void onNewData(final byte[] data) {
                    UserInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UserInfoActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };
    //-----//////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////// Alert
    View.OnClickListener clnone = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(Common.cPopupWin != null)
                Common.cPopupWin.dismiss();

            Common.setFullScreen(getWindow().getDecorView());
        }
    };
    View.OnClickListener clclose = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(Common.cPopupWin != null)
                Common.cPopupWin.dismiss();

            finish();
        }
    };
    //-----//////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Common.setFullScreen(getWindow().getDecorView());
        setContentView(R.layout.activity_user_info);
//        Common.CurrAct = this;

        initUSB();

        TextView txt1 = findViewById(R.id.textNo1);
        TextView txt2 = findViewById(R.id.textNo2);
        TextView txt3 = findViewById(R.id.textNo3);
        TextView txt4 = findViewById(R.id.textNo4);
        TextView txt5 = findViewById(R.id.textNo5);
        TextView txt6 = findViewById(R.id.textNo6);
        TextView txt7 = findViewById(R.id.textNo7);
        TextView txt8 = findViewById(R.id.textNo8);

        TxtView = new TextView[] {txt1, txt2, txt3, txt4, txt5, txt6, txt7, txt8};

        // 홈버튼 추가
        Common.addHomeBtn(this);
        // 오늘 날짜 추가
        Common.addDateStr(this);

        /// USB
        bf = new byte[1000];
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (Common.SerialPort != null) {
            try {
                Common.SerialPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            Common.SerialPort = null;
        }
        finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
//        Common.CurrAct = this;

        if (Common.SerialPort == null)
        {
            Toast.makeText(this, "No serial device.", Toast.LENGTH_LONG);
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(Common.SerialPort.getDriver().getDevice());
            if (connection == null)
            {
                return;
            }

            try
            {
                Common.SerialPort.open(connection);
                Common.SerialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            }
            catch (IOException e)
            {
                Toast.makeText(this, "Error opening device: " + e.getMessage(), Toast.LENGTH_LONG);
                try
                {
                    Common.SerialPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                Common.SerialPort = null;
                return;
            }
//            mTitleTextView.setText("Serial device: " + Common.SerialPort.getClass().getSimpleName());
        }
        onDeviceStateChange();
    }

    private void stopIoManager()
    {
        if (mSerialIoManager != null)
        {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager()
    {
        if (Common.SerialPort != null)
        {
            mSerialIoManager = new SerialInputOutputManager(Common.SerialPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data)
    {
        if(data[data.length-1] == 0x0A)
        {
            System.arraycopy(data, 0, bf, idx, data.length);
            idx += data.length;

            final String message = "Read " + idx + " bytes: " + HexDump.dumpHexString(bf, 0, idx) + "\n\n";
            Log.d("RFID Reader >> ", message);

            int len = idx > 8 ? 8 : idx;
            char[] tmp = new char[len];

            for(int i=0; i<len; i++)
                tmp[i] = (char)bf[i];

            String empno = String.valueOf(tmp).trim();

            SendUserInfoReq(empno);

            idx = 0;
        }
        else
        {
            System.arraycopy(data, 0, bf, idx, data.length);
            idx += data.length;
        }
    }

    // 확인 버튼을 눌렀을 때 처리
    public void onButtonCommitClicked(View v)
    {
        if (currIndex < 8)
        {
            Common.showAlert(this, "알림", "사원증을 태깅하거나, 사번을 입력해 주세요!!", null, clnone, null);
        }
        else
        {
            SendUserInfoReq(String.valueOf(empno));
        }
    }

    public void goNext()
    {
        if(Common.CheckOrList)
        {
            Intent intent = new Intent(getApplicationContext(), SecuCheckActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), SecuCheckListActivity.class);
            startActivity(intent);
        }
        finish();
    }

    // 사원정보 요청 전송
    private void SendUserInfoReq(String data)
    {
        Common.CurrAct = this;

        OBJMSG_1105_USERINFOREQ smsg = new OBJMSG_1105_USERINFOREQ();
        smsg.DataType = 1;
        smsg.Data = data;

        Common.Send(smsg);
    }

    @Override
    public void RCV(int msg, int code)
    {
        if(msg == NetMSGS.OP1106_USERINFORES)
        {
            if(code == Common.CODE_ERROR)
            {
                Common.showAlert(this, "오류", "사원정보 조회중 오류가 발생 했습니다.", null, clclose, null);
            }
            else
            {
                if (code == Common.CODE_OK)
                {
                    if (Common.UserType == Common.USERTYPE_LAST)
                    {
                        OBJ_CHECKDATA data = null;
                        if(Common.CheckDataByLast.containsKey(Common.CurrDept.DepartCode))
                            data = Common.CheckDataByLast.get(Common.CurrDept.DepartCode);

                        if (data == null)
                        {
                            goNext();
                        }
                        else
                        {
                            if(Common.CheckOrList)
                            {
                                SimpleDateFormat dateF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String body = "'" + Common.CurrDept.DepartName + "'는 이미 보안점검을 실행했습니다.\r\n다시 하시겠습니까?\n\n"
                                        + "       점검시간 : " + dateF.format(data.CheckTime)
                                        + "\r\n       점 검 자 : "
                                        + data.UserName;

                                View.OnClickListener clre = new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        if(Common.cPopupWin != null)
                                            Common.cPopupWin.dismiss();

                                        goNext();
                                    }
                                };

                                Common.showAlert(this, "보안점검 재실시 확인", body, clclose, null, clre);
                            }
                            else
                            {
                                goNext();
                            }
                        }
                    }
                    else if (Common.UserType == Common.USERTYPE_DUTY)
                    {
                        Intent intent = new Intent(getApplicationContext(), DeptListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                else if(code == Common.CODE_USERINFO_NOTFOUND)
                {
                    Common.showAlert(this, "오류", "사원정보를 찾을 수 없습니다. 다시 시도해 주세요.", null, clnone, null);
                }
                else if(code == Common.CODE_USERINFO_NOMATCHDEPT)
                {
                    Common.showAlert(this, "오류", "해당 단말에 등록된 부서의 사원이 아닙니다.", null, clclose, null);
                }
            }
        }
    }

    // 숫자 버튼을 눌렀을 때 처리
    public void onButtonNoClicked(View v)
    {
        String no = v.getTag().toString();

        switch (no)
        {
            case "1":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "2":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "3":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "4":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "5":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "6":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "7":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "8":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "9":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "0":
                if (currIndex < 8)
                    empno[currIndex] = no.charAt(0);
                currIndex++;
                break;
            case "X":
                currIndex--;
                break;
        }

        if (currIndex < 0)
            currIndex = 0;
        else if (currIndex > 8)
            currIndex = 8;

        this.ShowEmpno();
    }

    // 숫자 화면 표시
    public void ShowEmpno()
    {
        for (int i=0; i<8; i++)
        {
            if (i < currIndex)
                TxtView[i].setText(String.valueOf(empno[i]));
            else
                TxtView[i].setText("");
        }
    }

    // USB 초기화
    private void initUSB()
    {
        final Context context = this;

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

        final List<UsbSerialPort> ports = new ArrayList<UsbSerialPort>();
        for (final UsbSerialDriver driver : drivers)
        {
            final List<UsbSerialPort> driverports = driver.getPorts();
            ports.addAll(driverports);
        }

        if(ports.size() > 0)
        {
            mSerialPort = ports.get(0);

            UsbDevice device = mSerialPort.getDriver().getDevice();
            if (!mUsbManager.hasPermission(device))
            {
                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
                mUsbManager.requestPermission(device, usbPermissionIntent);
            }
            else
            {
                Common.SerialPort = mSerialPort;
            }
        }
    }

}
