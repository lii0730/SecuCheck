package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class OBJMSG_1301_IMGCHANGEEVT extends OBJMSG
{
    public int DeviceSeq;
    public String FilePath;

    public OBJMSG_1301_IMGCHANGEEVT() { this.OPCode = NetMSGS.OP1301_IMAGECHANGEEVT; }

    @Override
    public boolean Parse(Byte[] msg)
    {
        boolean rVal = false;

        int offset = 0;

        try
        {
            this.DeviceSeq = NetCommon.ByteArrToInt(msg, offset, 4);
            offset += 4;
            this.FilePath = NetCommon.ByteArrToString(msg, offset, 100, US_ASCII);

            rVal = true;
        }
        catch (Exception ex){}

        return rVal;
    }
}
