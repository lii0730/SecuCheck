package com.neodreams.neolibnetwork4android;

import java.nio.Buffer;
import java.util.Date;

public class OBJMSGHeader
{
    public int PacketLength = 0;
    public int UserCode = 0;
    public int OPCode = 0;
    public int PacketSeq = 0;
    public int TotalPacket = 0;
    public int CurrentPacket = 0;
    public Date Timestamp;
    public int BodyLength = 0;
    public boolean HasError = false;

    public OBJMSGHeader(){}

    public OBJMSGHeader(int opcode)
    {
        this.OPCode = opcode;
        this.PacketSeq = 0;
        this.TotalPacket = 0;
        this.CurrentPacket = 0;
    }

    public OBJMSGHeader(Byte[] header)
    {
        if (header.length == NetCommon.MESSAGE_HEADER_SIZE)
        {
            this.Parse(header);
        }
        else
        {
            this.HasError= true;
        }
    }

    protected void Parse(Byte[] header)
    {
        try
        {
            if (header[0] != NetCommon.PREAMBLE[0] || header[1] != NetCommon.PREAMBLE[1] || header[2] != NetCommon.PREAMBLE[2])
            {
                this.HasError = true;
                return;
            }

            int offset = 4;

            // packet length
            this.PacketLength = NetCommon.ByteArrToInt(header, offset);
            offset += 4;

            // user code
            this.UserCode = NetCommon.ByteArrToInt(header, offset);
            offset += 4;

            // OPCode
            this.OPCode = NetCommon.ByteArrToInt(header, offset, 2);
            offset += 2;

            // packet seq
            this.PacketSeq = NetCommon.ByteArrToInt(header, offset);
            offset += 4;

            // total packet
            this.TotalPacket = NetCommon.ByteArrToInt(header, offset, 2);
            offset += 2;

            // curr packet
            this.CurrentPacket = NetCommon.ByteArrToInt(header, offset, 2);
            offset += 2;

            this.Timestamp = NetCommon.ByteArrToUnixTime(header, offset);

            this.BodyLength = this.PacketLength - NetCommon.MESSAGE_HEADERnTAIL;

            if (this.PacketLength < NetCommon.MESSAGE_HEADERnTAIL || this.OPCode < 0)
                this.HasError = true;
        }
        catch (Exception ex)
        {
            this.HasError = true;
        }
    }
}
