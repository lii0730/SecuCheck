package com.neodreams.neolibnetwork4android;

import android.net.Network;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class NetworkClient extends Thread
{
    Socket socket;
    String Host;
    int Port;

    InputStream is = null ;
    OutputStream os = null ;

    ByteArrList messageBuffer;
    boolean NeedHeader = true;
    int CurrAvailable = 0;

    OBJMSGHeader CurrHeader = null;
    ByteArrList CurrMessageBuffer;

    static int PacketSeq = 0;

    byte[] SendMsg;

    ArrayList<INetMessageRcv> rcvs;

    public boolean loop = true;

    public NetworkClient()
    {
        this("", 0);
    }

    public NetworkClient(String host, int port)
    {
        this.Host = host;
        this.Port = port;

        Reset();

        rcvs = new ArrayList<INetMessageRcv>();
    }

    public boolean getConnected()
    {
        boolean rVal = false;

        if(socket != null)
        {
            rVal = socket.isConnected() && !socket.isClosed();
        }

        return  rVal;
    }

    public void Reset()
    {
        socket = new Socket();
        messageBuffer = new ByteArrList();
        CurrMessageBuffer = new ByteArrList();
        CurrAvailable = 0;
    }

    public void SetHost(String host, int port)
    {
        this.Host = host;
        this.Port = port;
    }

    public void AddHandler(INetMessageRcv rcv)
    {
        this.rcvs.add(rcv);
    }

    public void RemoveHandler(INetMessageRcv rcv)
    {
        this.rcvs.remove(rcv);
    }

    public void RaiseNetMessageRcv()
    {
        if (this.rcvs.size() > 0)
        {
//            byte[] body = this.CurrMessageBuffer.GetRange2();
            Byte[] body = this.CurrMessageBuffer.GetRange();

            for (INetMessageRcv rcv : this.rcvs)
                rcv.msgReceived(this.CurrHeader, body);
        }
    }

    public void RaiseConnected()
    {
        if (this.rcvs.size() > 0)
        {
            for (INetMessageRcv rcv : this.rcvs)
                rcv.NetConnected();
        }
    }

    public void RaiseDisconnected()
    {
        if (this.rcvs.size() > 0)
        {
            for (INetMessageRcv rcv : this.rcvs)
                rcv.NetDisconnected();
        }
    }

    public  void  Send(byte[] msg)
    {
        SendMsg = msg;

        new Thread()
        {
            public void run()
            {
                try
                {
                    os = socket.getOutputStream();
                    os.write(SendMsg, 0/*off*/, SendMsg.length);

                    SendMsg = null;
                }
                catch(IOException iex)
                {
                    int a=0;
                }
                catch (Exception ex)
                {
                    int b=0;
                }
            }
        }.start();
    }

    public void Send(OBJMSG msg)
    {
//        Send(msg.MakeMSG());

        byte[] msgarr = MakeMsg(msg, ++PacketSeq, 1, 1);

        if(msgarr != null)
            Send(msgarr);
    }

    private byte[] MakeMsg(OBJMSG msg, int packetID, int totPacket, int currPacket)
    {
        byte[] rVal = null;
        byte[] body = msg.MakeMSG();

        if(body !=null)
        {
            int size = body.length + NetCommon.MESSAGE_HEADERnTAIL;
            int pLen = NetCommon.PREAMBLE.length;
            int offset = 0;

            rVal = new byte[size];

            try
            {
                // preamble
                System.arraycopy(NetCommon.PREAMBLE, 0, rVal, offset, pLen);
                offset += pLen;

                // version
                rVal[offset++] = 0x01;

                // packet length header + body + tail
                System.arraycopy(NetCommon.NumberTobyteArr(size, 4), 0, rVal, offset, 4);
                offset += 4;

                // user code
                System.arraycopy(NetCommon.NumberTobyteArr(1, 4), 0, rVal, offset, 4);
                offset += 4;

                // OP code
                System.arraycopy(NetCommon.NumberTobyteArr(msg.OPCode, 2), 0, rVal, offset, 2);
                offset += 2;

                // packet seq
                System.arraycopy(NetCommon.NumberTobyteArr(packetID, 4), 0, rVal, offset, 4);
                offset += 4;

                // total packet
                System.arraycopy(NetCommon.NumberTobyteArr(totPacket, 2), 0, rVal, offset, 2);
                offset += 2;

                // current packet
                System.arraycopy(NetCommon.NumberTobyteArr(currPacket, 2), 0, rVal, offset, 2);
                offset += 2;

                // timestamp
                System.arraycopy(NetCommon.NowTobyteArr(8), 0, rVal, offset, 8);
                offset += 8;

                /////////////
                // body
                System.arraycopy(body, 0, rVal, offset, body.length);
                offset += body.length;

                ////////////
                // tail
                // check sum 사용안함
                rVal[offset++] = 0x00;
                // end
                for (int i=1; i<=pLen; i++)
                {
                    rVal[offset++] = NetCommon.PREAMBLE[pLen-i];
                }
            }
            catch (Exception ex)
            {
                rVal = null;
            }
        }

        return rVal;
    }

    public void Close()
    {
        this.loop = false;

        try
        {
            // 소켓 종료.
            if (is != null)
                is.close() ;

            if (os != null)
                os.close() ;

            if (socket != null)
                socket.close() ;
        }
        catch (Exception e)
        {
            Log.d("ERROR >>> ", e.getMessage());
        }
    }

    protected int SearchPreamble()
    {
        int rVal = -1;
        int index = 0;

        try
        {
            do
            {
                index = messageBuffer.indexOf(NetCommon.PREAMBLE[0]);

                if (index > -1 && index < (messageBuffer.size() - 2))
                {
                    if (messageBuffer.get(index) == NetCommon.PREAMBLE[0] && messageBuffer.get(index+1) == NetCommon.PREAMBLE[1] && messageBuffer.get(index+2) == NetCommon.PREAMBLE[2])
                        rVal = index;
                    else
                        messageBuffer.RemoveRange(0, index++);
                }
            } while (rVal == -1 && index < messageBuffer.size());
        }
        catch (Exception ex)
        {
        }

        return rVal;
    }

    public void run()
    {                // 데이터 수신.
        byte[] bufRcv = new byte[102400] ;
        int size ;

        while (this.loop)
        {
            SocketAddress addr = new InetSocketAddress(this.Host, this.Port);

            try
            {
                try
                {
                    if (!socket.isConnected() || socket.isClosed())
                    {
                        socket.connect(addr, 10000);

                        if (socket.isConnected())
                            RaiseConnected();
                    }

                    is = socket.getInputStream();
                    this.NeedHeader = true;

                    while (this.loop)
                    {
                        size = is.read(bufRcv);

                        if (size > 0)
                        {
                            byte[] tmp = new byte[size];
                            System.arraycopy(bufRcv, 0, tmp, 0, size);

                            messageBuffer.AddRange(tmp);

                            do
                            {
                                try
                                {
                                    if (this.NeedHeader && messageBuffer.size() > NetCommon.MESSAGE_HEADER_SIZE)
                                    {
                                        // preamble check
                                        if (messageBuffer.get(0) == NetCommon.PREAMBLE[0] && messageBuffer.get(1) == NetCommon.PREAMBLE[1] && messageBuffer.get(2) == NetCommon.PREAMBLE[2])
                                        {
                                            // header 가져오기..
                                            OBJMSGHeader Header = new OBJMSGHeader(messageBuffer.GetRange(0, NetCommon.MESSAGE_HEADER_SIZE, true));

                                            // header 분석 중 오류 발생 시 데이터 처리 안함..
                                            if (!Header.HasError)
                                            {
                                                this.CurrAvailable = Header.BodyLength + NetCommon.MESSAGE_TAIL_SIZE;
                                                this.NeedHeader = false;

                                                this.CurrHeader = Header;
                                                this.CurrMessageBuffer.clear();
                                            }
                                        } else // 버퍼의 시작이 preamble과 다를 경우..
                                        {
                                            int index = this.SearchPreamble();

                                            if (index == -1)
                                            {
                                                if (messageBuffer.size() > 2)
                                                    messageBuffer.RemoveRange(0, messageBuffer.size() - 2);
                                            } else
                                            {
                                                messageBuffer.RemoveRange(0, index);
                                            }
                                        }

                                    }

                                    if (!this.NeedHeader)
                                    {
                                        // message body 수신
                                        if (this.messageBuffer.size() > 0 && this.CurrAvailable > 0)
                                        {
                                            int msgReadSize = size < this.CurrAvailable ? size : this.CurrAvailable;

                                            Byte[] tmpBody = messageBuffer.GetRange(0, msgReadSize, true);

                                            if (tmpBody != null)
                                            {
                                                this.CurrMessageBuffer.AddRange(tmpBody);
                                                this.CurrAvailable -= msgReadSize;
                                            } else
                                            {
                                                this.NeedHeader = true;
                                            }
                                        }

                                        // message 수신 완료 처리
                                        if (this.CurrAvailable == 0)
                                        {
                                            Byte[] tail = this.CurrMessageBuffer.GetRange(this.CurrMessageBuffer.size() - NetCommon.MESSAGE_TAIL_SIZE, NetCommon.MESSAGE_TAIL_SIZE, true);

                                            if (tail[3] == NetCommon.PREAMBLE[0] && tail[2] == NetCommon.PREAMBLE[1] && tail[1] == NetCommon.PREAMBLE[2])
                                            {
                                                // 수신 이벤트
                                                this.RaiseNetMessageRcv();
                                            } else
                                            {
                                                // ERROR
                                                Log.d("ERROR >>> ", "no match tail");
                                            }

                                            // 지금 처리중인 메시지 버퍼 비우기..
                                            this.CurrMessageBuffer.clear();
                                            // 다시 헤더 읽을 준비...
                                            this.NeedHeader = true;
                                        }
                                    }
                                }
                                catch (Exception ex)
                                {
                                    // ERROR
                                    Log.d("ERROR >>> ", ex.getMessage());
                                    this.NeedHeader = true;
                                }
                            } while (messageBuffer.size() > NetCommon.MESSAGE_HEADER_SIZE);
                        } else
                        {
                            socket.close();
                            Reset();
                            Thread.sleep(2000);
                            break;
                        }
                    }
                }
                catch (IOException ioex)
                {
                    // ERROR
                    Log.d("ERROR >>> ", ioex.getMessage());
                    socket.close();
                    Reset();
                    Thread.sleep(2000);
                }
                catch (Exception ex)
                {
                    // ERROR
                    Log.d("ERROR >>> ", ex.getMessage());
                    socket.close();
                    Reset();
                    Thread.sleep(2000);
                }
            }
            catch (Exception oex)
            {
                Log.d("ERROR >>> ", oex.getMessage());
            }
        }
    }
}
