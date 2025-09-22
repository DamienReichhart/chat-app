package com.ap4.client.interfaces.websocket;

import com.ap4.common.dto.WebSocketMessageDTO;
import com.ap4.common.models.Message;

// Listener interface for WebSocket events
public interface WebSocketListener {
    void onConnectionStatusChanged(boolean connected);
    void onMessageReceived(Message message);
    void onError(String errorMessage);
}