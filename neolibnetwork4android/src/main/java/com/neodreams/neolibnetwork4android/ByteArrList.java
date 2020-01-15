package com.neodreams.neolibnetwork4android;

import java.util.ArrayList;
import java.util.Arrays;

public class ByteArrList extends ArrayList<Byte>
{
    public void AddRange(Byte[] arr)
    {
        this.addAll(Arrays.asList(arr));
    }

    public void AddRange(byte[] arr)
    {
        for(byte b : arr)
            this.add(b);
    }

    public void RemoveRange(int f, int t)
    {
        removeRange(f, t);
    }

    public Byte[] GetRange(int index, int len, boolean remove)
    {
        Byte[] rVal = null;

        int toIdx = index + len;

        if(this.size() >= toIdx)
        {
            rVal = new Byte[len];
            this.subList(index, index + len).toArray(rVal);

            if (remove)
                this.removeRange(index, toIdx);
        }

        return  rVal;
    }

    public Byte[] GetRange()
    {
        return this.GetRange(0, this.size(), true);
    }

    public byte[] GetRange2(int index, int len, boolean remove)
    {
        byte[] rVal = null;
        Byte[] tmp = this.GetRange(index, len, remove);

        if (tmp != null && tmp.length > 0)
        {
            rVal = new byte[tmp.length];

            for (int i=0; i<tmp.length; i++)
            {
                rVal[i] = tmp[i].byteValue();
            }
        }

        return rVal;
    }

    public byte[] GetRange2()
    {
        return this.GetRange2(0, this.size(), true);
    }
}
