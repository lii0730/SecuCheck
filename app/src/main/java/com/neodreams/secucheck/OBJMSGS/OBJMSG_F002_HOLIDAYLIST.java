package com.neodreams.secucheck.OBJMSGS;

import com.neodreams.neolibnetwork4android.NetCommon;
import com.neodreams.neolibnetwork4android.OBJMSG;

import java.util.ArrayList;

public class OBJMSG_F002_HOLIDAYLIST extends OBJMSG
{
    public ArrayList<Integer> Holidays = null;

    public OBJMSG_F002_HOLIDAYLIST()
    {
        this.OPCode = NetMSGS.OPF002_HOLIDAYLIST;
    }

    @Override
    public boolean Parse(Byte[] msg)
    {
        boolean rVal = false;

        int offset = 0;

        try
        {
            int cnt = NetCommon.ByteArrToInt(msg, offset, 2);
            offset += 2;

            if (cnt > 0)
            {
                int today = NetCommon.GetTodayNum();
                Holidays = new ArrayList<Integer>();

                for (int i=0; i<cnt; i++)
                {
                    int tmp = NetCommon.ByteArrToInt(msg, offset, 4);
                    offset += 4;

                    if (tmp >= today)
                        Holidays.add(tmp);
                }
            }

            rVal = true;
        }
        catch (Exception ex){}

        return rVal;
    }
}
