package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.ByteArrList;
import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

public class OBJMSG_1205_TODAYCHECKLISTREQ extends OBJMSG
{
    public int DeviceSeq = 0;

    public OBJMSG_1205_TODAYCHECKLISTREQ()
    {
        this.OPCode = NetMSGS.OP1205_TODAYCHECKLISTREQ;
    }

    @Override
    public byte[] MakeMSG()
    {
        byte[] rVal = null;

        try
        {
            ByteArrList tmp = new ByteArrList();
            tmp.AddRange(NetCommon.NumberTobyteArr(this.DeviceSeq, 4));

            rVal = tmp.GetRange2();
        }
        catch (Exception ex){}

        return rVal;
    }
}
