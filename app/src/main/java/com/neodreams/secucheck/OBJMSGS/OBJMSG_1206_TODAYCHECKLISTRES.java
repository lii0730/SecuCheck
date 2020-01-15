package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OBJMSG_1206_TODAYCHECKLISTRES extends OBJMSG
{
    public int DeviceSeq = 0;
    public ArrayList<OBJ_CHECKDATA> CheckList;

    public OBJMSG_1206_TODAYCHECKLISTRES()
    {
        this.OPCode = NetMSGS.OP1206_TODAYCHECKLISTRES;
        this.CheckList = new ArrayList<OBJ_CHECKDATA>();
    }

    @Override
    public boolean Parse(Byte[] msg)
    {
        boolean rVal = false;

        int offset = 0;

        try
        {
            this.DeviceSeq = NetCommon.ByteArrToInt(msg, offset);
            offset += 4;

            int cnt = msg[offset++];

            for (int i=0; i<cnt; i++)
            {
                OBJ_CHECKDATA tmp = new OBJ_CHECKDATA();

                tmp.CheckTime = NetCommon.ByteArrToDate(msg, offset);
                offset += 4;
                tmp.DepartCode = NetCommon.ByteArrToString(msg, offset, 4, US_ASCII);
                offset += 4;
                tmp.EMPNO = NetCommon.ByteArrToString(msg, offset, 8, US_ASCII);
                offset += 8;
                tmp.UserType = msg[offset++];
                tmp.UserName = NetCommon.ByteArrToString(msg, offset, 10, UTF_8);
                offset += 10;

                tmp.Data1 = msg[offset++];
                tmp.Data2 = msg[offset++];
                tmp.Data3 = msg[offset++];
                tmp.Data4 = msg[offset++];
                tmp.Data5 = msg[offset++];

                this.CheckList.add(tmp);
            }

            rVal = true;
        }
        catch (Exception ex){}

        return rVal;
    }
}
