package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

public class OBJMSG_1103_DEVICEONTIMECHANGE extends OBJMSG
{
    public int DeviceSeq;
    public int OnTime;
    public int OffTime;

    public OBJMSG_1103_DEVICEONTIMECHANGE() { this.OPCode = NetMSGS.OP1103_DEVICEONTIMECHANGE; }

    @Override
    public boolean Parse(Byte[] msg)
    {
        boolean rVal = false;

        int offset = 0;

        try
        {
            this.DeviceSeq = NetCommon.ByteArrToInt(msg, offset, 4);
            offset += 4;
            this.OnTime = NetCommon.ByteArrToInt(msg, offset, 2);
            offset += 2;
            this.OffTime = NetCommon.ByteArrToInt(msg, offset, 2);

            rVal = true;
        }
        catch (Exception ex){}

        return rVal;
    }
}
