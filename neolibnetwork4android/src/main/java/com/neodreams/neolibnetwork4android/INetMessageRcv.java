package com.neodreams.neolibnetwork4android;

public interface INetMessageRcv
{
    public void NetConnected();
    public void NetDisconnected();

    public void msgReceived(OBJMSGHeader header, Byte[] body);
}
