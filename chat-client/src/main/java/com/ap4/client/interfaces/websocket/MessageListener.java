package com.ap4.client.interfaces.websocket;

import com.ap4.common.models.Message;

// Message listener interface
public interface MessageListener {
    void onMessageReceived(Message message);
}