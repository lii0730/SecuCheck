package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.ByteArrList;
import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

public class OBJMSG_FF00_DEVICESTATUS extends OBJMSG
{
    public int DeviceSeq;
    public byte CPU;
    public byte MEM;
    public byte HDD;

    public OBJMSG_FF00_DEVICESTATUS()
    {
        this.OPCode = NetMSGS.OPFF00_DEVICESTATUS;
    }

    @Override
    public byte[] MakeMSG()
    {
        byte[] rVal = null;

        try
        {
            ByteArrList tmp = new ByteArrList();
            tmp.AddRange(NetCommon.NumberTobyteArr(this.DeviceSeq, 4));
            tmp.add(this.CPU);
            tmp.add(this.MEM);
            tmp.add(this.HDD);

            rVal = tmp.GetRange2();
        }
        catch (Exception ex){}

        return rVal;
    }
}
