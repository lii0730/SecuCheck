package com.neodreams.secucheck;

import android.os.Message;

import com.neodreams.neolibnetwork4android.INetMessageRcv;
import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.NetworkClient;
import com.neodreams.neolibnetwork4android.OBJMSG;
import com.neodreams.neolibnetwork4android.OBJMSGHeader;
import com.neodreams.secucheck.OBJMSGS.NetMSGS;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1101_DEVICEINFOREQ;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1102_DEVICEINFORES;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1103_DEVICEONTIMECHANGE;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1106_USERINFORES;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1202_CHECKLISTRES;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1206_TODAYCHECKLISTRES;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_1301_IMGCHANGEEVT;
import com.neodreams.secucheck.OBJMSGS.OBJMSG_F002_HOLIDAYLIST;
import com.neodreams.secucheck.OBJMSGS.OBJ_CHECKDATA;
import com.neodreams.secucheck.OBJMSGS.OBJ_DEPART;

public class NetMNG implements INetMessageRcv
{
    public NetworkClient Client = null;
    
    public NetMNG()
    {
    }

    public void Init()
    {
        if(Client != null)
        {
            Client.Close();
            Client = null;
        }

        // 통신 초기화
        Client = new NetworkClient(ConfigActivity.SERVERIP, ConfigActivity.SERVERPORT);
        Client.AddHandler(this);
        Client.start();
    }

    public void CheckClient()
    {
        Thread.State ts = Client.getState();

        if(ts != Thread.State.RUNNABLE && ts != Thread.State.TIMED_WAITING)
            Init();

        if(!Client.getConnected())
            Init();
    }

    @Override
    public void NetConnected()
    {
        OBJMSG_1101_DEVICEINFOREQ msg = new OBJMSG_1101_DEVICEINFOREQ();
        msg.IP = NetCommon.getIPAddress();

        Client.Send(msg);
    }

    @Override
    public void NetDisconnected()
    {
        Init();
    }

    @Override
    public void msgReceived(OBJMSGHeader header, Byte[] body)
    {
        switch (header.OPCode)
        {
            // 단말 정보
            case NetMSGS.OP1102_DEVICEINFORES:
            {
                OBJMSG_1102_DEVICEINFORES omsg = new OBJMSG_1102_DEVICEINFORES();
                if(omsg.Parse(body))
                {
                    Common.DeviceInfo.Version = omsg.Version;
                    Common.DeviceInfo.DeviceSeq = omsg.DeviceSeq;
                    Common.DeviceInfo.OnTime = omsg.OnTime;
                    Common.DeviceInfo.OffTime = omsg.OffTime;

                    Common.DeviceInfo.Departs = omsg.Departs;

//                    int size = omsg.Departs.size();
//                    if(size > 0)
//                    {
//                        for(OBJ_DEPART dept : omsg.Departs)
//                        {
//                            if(!Common.CheckDataByLast.containsKey(dept.DepartCode))
//                                Common.CheckDataByLast.put(dept.DepartCode, null);
//                        }
//
//                        OBJMSG_1205_TODAYCHECKLISTREQ smsg = new OBJMSG_1205_TODAYCHECKLISTREQ();
//                        smsg.DeviceSeq = omsg.DeviceSeq;
//                        Client.Send(smsg);
//                    }

                    if(Common.MainAct != null)
                    {
                        Common.MainAct.mHandler.sendMessage(Message.obtain(Common.MainAct.mHandler, NetMSGS.OP1102_DEVICEINFORES));
                    }
                }
            }
            break;
            // 단말 운영 시간 변경
            case NetMSGS.OP1103_DEVICEONTIMECHANGE:
            {
                OBJMSG_1103_DEVICEONTIMECHANGE omsg = new OBJMSG_1103_DEVICEONTIMECHANGE();
                if(omsg.Parse(body))
                {
                    if(Common.DeviceInfo.DeviceSeq == omsg.DeviceSeq)
                    {
                        Common.DeviceInfo.OnTime = omsg.OnTime;
                        Common.DeviceInfo.OffTime = omsg.OffTime;
                    }
                }
            }
            break;
            // 보안점검 목록 요청 응답
            case NetMSGS.OP1202_CHECKLISTRES:
            {
                OBJMSG_1202_CHECKLISTRES omsg = new OBJMSG_1202_CHECKLISTRES();
                if(omsg.Parse(body))
                {
                    Common.CheckData4List = omsg;

                    if(Common.CurrAct != null)
                        Common.CurrAct.RCV2(NetMSGS.OP1202_CHECKLISTRES, 0);
                }
            }
            break;
            // 금일 보안점검 목록 요청 응답
            case NetMSGS.OP1206_TODAYCHECKLISTRES:
            {
                OBJMSG_1206_TODAYCHECKLISTRES omsg = new OBJMSG_1206_TODAYCHECKLISTRES();
                if(omsg.Parse(body))
                {
                    if(omsg.DeviceSeq == Common.DeviceInfo.DeviceSeq)
                    {
                        for(OBJ_CHECKDATA data : omsg.CheckList)
                        {
                            if(data.UserType == Common.USERTYPE_LAST)
                            {
                                if(Common.CheckDataByLast.containsKey(data.DepartCode))
                                {
                                    Common.CheckDataByLast.put(data.DepartCode, data);
                                }
                            }
                        }
                    }
                }
            }
            break;
            // 사용자 정보
            case NetMSGS.OP1106_USERINFORES:
            {
                OBJMSG_1106_USERINFORES omsg = new OBJMSG_1106_USERINFORES();
                boolean check = omsg.Parse(body);
                int code = Common.CODE_ERROR;

                if(check)
                {
                    if(omsg.EMPNO.equals("X"))
                    {
                        code = Common.CODE_USERINFO_NOTFOUND;
                    }
                    else
                    {
                        if (Common.UserType == Common.USERTYPE_LAST)
                        {
                            int len = omsg.DetpFull.length();
                            int loop = len / 4;
                            boolean found = false;

                            for(int i=loop-1; i>=0; i--)
                            {
                                int si = 4 * i;
                                int ei = si + 4;

                                String tmpCode = omsg.DetpFull.substring(si, ei);

                                for(OBJ_DEPART dept : Common.DeviceInfo.Departs)
                                {
                                    if(dept.DepartCode.equals(tmpCode))
                                    {
                                        found = true;
                                        //Common.CurrDept = tmpCode;
                                        Common.CurrDept = dept;
                                        break;
                                    }
                                }

                                if(found)
                                    break;
                            }

                            if(found)
                            {
                                Common.User = omsg;
                                code = Common.CODE_OK;
                            }
                            else
                            {
                                Common.User = null;
                                code = Common.CODE_USERINFO_NOMATCHDEPT;
                            }
                        }
                        else if (Common.UserType == Common.USERTYPE_DUTY)
                        {
                            Common.User = omsg;
                            code = Common.CODE_OK;
                        }
                    }
                }

                if(Common.CurrAct != null)
                    Common.CurrAct.RCV2(NetMSGS.OP1106_USERINFORES, code);
            }
            break;
            // 휴일 정보
            case NetMSGS.OPF002_HOLIDAYLIST:
            {
                OBJMSG_F002_HOLIDAYLIST omsg = new OBJMSG_F002_HOLIDAYLIST();
                if(omsg.Parse(body))
                {
                    Common.Holidays = omsg.Holidays;
                }
            }
            break;
            // 조직도 이미지 변경
            case NetMSGS.OP1301_IMAGECHANGEEVT:
            {
                OBJMSG_1301_IMGCHANGEEVT omsg = new OBJMSG_1301_IMGCHANGEEVT();
                if(omsg.Parse(body))
                {
                    if(Common.DeviceInfo.DeviceSeq == omsg.DeviceSeq)
                    {
                        if(Common.MainAct != null)
                        {
                            Common.MainAct.mHandler.sendMessage(Message.obtain(Common.MainAct.mHandler, NetMSGS.OP1102_DEVICEINFORES));
                        }
                    }
                }
            }
            break;
        }
    }

    public void Send(OBJMSG msg)
    {
        Client.Send(msg);
    }
}
