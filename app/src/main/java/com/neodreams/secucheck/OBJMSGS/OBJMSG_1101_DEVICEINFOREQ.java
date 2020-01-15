package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.ByteArrList;
import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

public class OBJMSG_1101_DEVICEINFOREQ extends OBJMSG
{
    public String IP;
    public String VER;

    public OBJMSG_1101_DEVICEINFOREQ()
    {
        this.OPCode = NetMSGS.OP1101_DEVICEINFOREQ;

        VER = "1";
    }

    @Override
    public byte[] MakeMSG()
    {
        byte[] rVal = null;

        try
        {
            ByteArrList tmp = new ByteArrList();
            tmp.AddRange(NetCommon.StringTobyteArr(this.IP, 15));
            tmp.AddRange(NetCommon.StringTobyteArr(this.VER, 20));

            rVal = tmp.GetRange2();
        }
        catch (Exception ex){}

        return rVal;
    }
}
