package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.ByteArrList;
import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

public class OBJMSG_1105_USERINFOREQ extends OBJMSG
{
    public byte DataType;
    public String Data;

    public OBJMSG_1105_USERINFOREQ() { this.OPCode = NetMSGS.OP1105_USERINFOREQ; }

    @Override
    public byte[] MakeMSG()
    {
        byte[] rVal = null;

        try
        {
            ByteArrList tmp = new ByteArrList();
            tmp.add(this.DataType);
            tmp.AddRange(NetCommon.StringTobyteArr(this.Data, 10));

            rVal = tmp.GetRange2();
        }
        catch (Exception ex){}

        return rVal;
    }
}
