package com.neodreams.secucheck.OBJMSGS;

import android.util.Log;

import com.neodreams.neolibnetwork4android.ByteArrList;
import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

public class OBJMSG_1203_CHECKDATA extends OBJMSG
{
    public OBJ_CHECKDATA Data;

    public OBJMSG_1203_CHECKDATA() { this.OPCode = NetMSGS.OP1203_CHECKDATA; }

    @Override
    public byte[] MakeMSG()
    {
        byte[] rVal = null;

        try
        {
            ByteArrList tmp = new ByteArrList();
            tmp.AddRange(NetCommon.NumberTobyteArr(this.Data.DeviceSeq, 4));
            tmp.AddRange(NetCommon.StringTobyteArr(this.Data.DepartCode, 4));
            tmp.AddRange(NetCommon.NowTobyteArr(4));
            tmp.AddRange(NetCommon.StringTobyteArr(this.Data.EMPNO, 8));
            tmp.add(this.Data.UserType);
            tmp.add(this.Data.Data1);
            tmp.add(this.Data.Data2);
            tmp.add(this.Data.Data3);
            tmp.add(this.Data.Data4);
            tmp.add(this.Data.Data5);

            tmp.AddRange(NetCommon.StringTobyteArr(this.Data.CheckNote, 100));

            rVal = tmp.GetRange2();
        }
        catch (Exception ex)
        {
            Log.d("ERROR >>> ", ex.getMessage());
        }

        return rVal;
    }
}
