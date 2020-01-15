package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OBJMSG_1102_DEVICEINFORES extends OBJMSG
{
    public byte Version;
    public int DeviceSeq;
    public int OnTime;
    public int OffTime;

    public ArrayList<OBJ_DEPART> Departs;

    public OBJMSG_1102_DEVICEINFORES()
    {
        this.OPCode = NetMSGS.OP1102_DEVICEINFORES;

        Departs = new ArrayList<OBJ_DEPART>();
    }

    @Override
    public boolean Parse(Byte[] msg)
    {
        boolean rVal = false;

        int offset = 0;

        try
        {
            this.Version = msg[offset++].byteValue();
            this.DeviceSeq = NetCommon.ByteArrToInt(msg, offset, 4);
            offset += 4;
            this.OnTime = NetCommon.ByteArrToInt(msg, offset, 2);
            offset += 2;
            this.OffTime = NetCommon.ByteArrToInt(msg, offset, 2);
            offset += 2;

            int cnt = (int)msg[offset++];

            for (int i=0; i<cnt; i++)
            {
                OBJ_DEPART dept = new OBJ_DEPART();

                dept.DepartCode = NetCommon.ByteArrToString(msg, offset, 4, US_ASCII);
                offset += 4;
                dept.DepartName = NetCommon.ByteArrToString(msg, offset, 100, UTF_8);
                offset += 100;

                this.Departs.add(dept);
            }

            rVal = true;
        }
        catch (Exception ex){}

        return rVal;
    }
}
