package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OBJMSG_1106_USERINFORES extends OBJMSG
{
    public String EMPNO;
    public String Name;
    public String DetpFull;
    public String LevelName;

    public OBJMSG_1106_USERINFORES() { this.OPCode = NetMSGS.OP1106_USERINFORES; }

    @Override
    public boolean Parse(Byte[] msg)
    {
        boolean rVal = false;

        int offset = 0;

        try
        {
            this.EMPNO = NetCommon.ByteArrToString(msg, offset, 8, US_ASCII);
            offset += 8;
            this.Name = NetCommon.ByteArrToString(msg, offset, 10, UTF_8);
            offset += 10;
            this.DetpFull = NetCommon.ByteArrToString(msg, offset, 40, US_ASCII);
            offset += 40;
            this.LevelName = NetCommon.ByteArrToString(msg, offset, 40, UTF_8);

            rVal = true;
        }
        catch (Exception ex){}

        return rVal;
    }
}
