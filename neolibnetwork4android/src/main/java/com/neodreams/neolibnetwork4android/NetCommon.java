package com.neodreams.neolibnetwork4android;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NetCommon
{
    // 메시지 헤더의 길이
    public static final int MESSAGE_HEADER_SIZE = 30;
    // 메시지 시작점
    public static final byte[] PREAMBLE = new byte[] { (byte)'E', (byte)'M', (byte)'P' };
    // 메시지 테일의 길이
    public static final int MESSAGE_TAIL_SIZE = 4;

    // 전송 packet ID
    public static volatile int PacketID = 1;

    public static final int MESSAGE_HEADERnTAIL = MESSAGE_HEADER_SIZE + MESSAGE_TAIL_SIZE;

    //////////////////////////////////////////////////////////////////////////////////

    public static int ByteArrToInt(Byte[] arr, int offset, int len, ByteOrder order)
    {
        int rVal = 0;
        int intSize = Integer.SIZE/8;
        int sgap = intSize - len;
        int tidx = offset + len;

        if(arr.length >= tidx)
        {
            ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
            buff.order(order);

            if(order == ByteOrder.BIG_ENDIAN)
            {
                for(int i=0; i<intSize; i++)
                {
                    if(i < sgap)
                        buff.put((byte)0x00);
                    else
                        buff.put(arr[offset++]);
                }
            }
            else
            {
                for(int i=0; i<intSize; i++)
                {
                    if (i < len)
                        buff.put(arr[offset++]);
                    else
                        buff.put((byte) 0x00);
                }
            }

            buff.flip();
            rVal = buff.getInt();
        }

        return rVal;
    }

    public static int ByteArrToInt(Byte[] arr, int offset, int len)
    {
        return NetCommon.ByteArrToInt(arr, offset, len, ByteOrder.BIG_ENDIAN);
    }

    public static int ByteArrToInt(Byte[] arr, int offset)
    {
        return NetCommon.ByteArrToInt(arr, offset, Integer.SIZE/8, ByteOrder.BIG_ENDIAN);
    }

    public static Date ByteArrToUnixTime(Byte[] arr, int offset)
    {
        Date rVal = null;

        if(arr.length >= offset + 8)
        {
            int intSize = Integer.SIZE/8;

            ByteBuffer timeBf =  ByteBuffer.allocate(intSize);
            ByteBuffer msBf =  ByteBuffer.allocate(intSize);

            for (int i=0; i<4; i++)
                timeBf.put((byte)arr[offset++]);
            for (int i=0; i<4; i++)
                msBf.put((byte)arr[offset++]);

            int intT = 0;
            int intMS = 0;

            timeBf.flip();
            msBf.flip();

            intT = timeBf.getInt();
            intMS = msBf.getInt();

            long timestamp = ((long)intT*1000) + (intMS/1000);

            Date test = new Date();

            rVal = new Date(timestamp);
        }

        return  rVal;
    }

    public static Date ByteArrToDate(Byte[] arr, int offset)
    {
        Date rVal = null;

        if(arr.length >= offset + 4)
        {
            int intSize = Integer.SIZE/8;

            ByteBuffer timeBf =  ByteBuffer.allocate(intSize);

            for (int i=0; i<4; i++)
                timeBf.put((byte)arr[offset++]);

            int intT = 0;

            timeBf.flip();

            intT = timeBf.getInt();

            long timestamp = ((long)intT*1000);

            Date test = new Date();

            rVal = new Date(timestamp);
        }

        return  rVal;
    }

    public static String ByteArrToString(Byte[] arr, int offset, int len, Charset cset)
    {
        String rVal = "";

        if (arr.length >= offset + len)
        {
            Byte[] tmp = new Byte[len];
            System.arraycopy(arr, offset, tmp, 0, len);

            byte[] tmp2 = new byte[len];

            for (int i=0; i<len; i++)
                tmp2[i] = tmp[i].byteValue();

            rVal = new String(tmp2, 0, len, cset);
        }

        return rVal.trim();
    }

    //////////////////////////////////////////////////////////////////////////////////

    public static byte[] NowTobyteArr(int len)
    {
        byte[] rVal = new byte[len];

        if(len >= 4)
        {
            long unixTime = System.currentTimeMillis() / 1000L;
            System.arraycopy(NumberTobyteArr(unixTime, 4), 0, rVal, 0, 4);

            if(len == 8)
            {
                long ms = System.currentTimeMillis() % 1000L;
                System.arraycopy(NumberTobyteArr(ms, 4), 0, rVal, 4, 4);
            }
        }

        return rVal;
    }

    public static byte[] StringTobyteArr(String str, int len)
    {
        byte[] rVal = new byte[len];
        byte[] tmp = str.getBytes();
        int cpLen = tmp.length < len ? tmp.length : len;
        System.arraycopy(tmp, 0, rVal, 0, cpLen);

        return rVal;
    }

    public static byte[] NumberTobyteArr(short num, int len)
    {
        return NumberTobyteArr((long)num, len);
    }

    public static byte[] NumberTobyteArr(int num, int len)
    {
        return NumberTobyteArr((long)num, len);
    }

    public static byte[] NumberTobyteArr(long num, int len)
    {
        byte[] rVal = new byte[len];
        int tmp = len - 1;

        for (int i=0; i<len; i++)
        {
            rVal[i] = (byte)((num >>> 8*(tmp-i)) & 0xFF);
        }

        return rVal;
    }

    //////////////////////////////////////////////////////////////////////////////////

    public static int GetTodayNum()
    {
        Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH);
        int d = now.get(Calendar.DAY_OF_MONTH);

        return (y * 10000) + ((m+1) * 100) + d;
    }

    public static String getIPAddress()
    {
        try
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces)
            {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs)
                {
                    if (!addr.isLoopbackAddress())
                    {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (isIPv4)
                            return sAddr;
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
}
