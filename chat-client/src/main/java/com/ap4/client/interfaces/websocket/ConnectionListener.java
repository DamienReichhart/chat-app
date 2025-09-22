package com.ap4.client.interfaces.websocket;

// Connection listener interface
public interface ConnectionListener {
    void onConnectionStatusChanged(boolean connected);
}