package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.ByteArrList;
import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

public class OBJMSG_1201_CHECKLISTREQ extends OBJMSG
{
    public String DepartCode;
    public byte UserType;
//    public int StartDate;
//    public int EndDate;

    public OBJMSG_1201_CHECKLISTREQ()
    {
        this.OPCode = NetMSGS.OP1201_CHECKLISTREQ;
    }

    @Override
    public byte[] MakeMSG()
    {
        byte[] rVal = null;

        try
        {
            ByteArrList tmp = new ByteArrList();
            tmp.AddRange(NetCommon.StringTobyteArr(this.DepartCode, 4));
            tmp.add(this.UserType);
//            tmp.AddRange(NetCommon.NumberTobyteArr(this.StartDate, 4));
//            tmp.AddRange(NetCommon.NumberTobyteArr(this.EndDate, 4));

            rVal = tmp.GetRange2();
        }
        catch (Exception ex){}

        return rVal;
    }
}
