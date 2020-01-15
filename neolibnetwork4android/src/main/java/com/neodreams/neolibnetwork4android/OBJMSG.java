package com.neodreams.neolibnetwork4android;

public class OBJMSG
{
    public OBJMSGHeader Header;

    public int OPCode;

    public int getOPCode()
    {
        if(this.Header != null)
            return this.Header.OPCode;
        else
            return 0;
    }

    public int getBodyLength()
    {
        if(this.Header != null)
            return this.Header.BodyLength;
        else
            return 0;
    }

    public boolean Parse(Byte[] msg)
    {
        return true;
    }

    public byte[] MakeMSG()
    {
        return null;
    }
}