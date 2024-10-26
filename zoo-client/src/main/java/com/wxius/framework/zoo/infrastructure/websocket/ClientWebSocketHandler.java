package com.wxius.framework.zoo.infrastructure.websocket;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wxius.framework.zoo.api.ConnectReset;
import com.wxius.framework.zoo.api.Notification;
import com.wxius.framework.zoo.api.NotificationMessage;
import com.wxius.framework.zoo.api.NotificationServerList;
import com.wxius.framework.zoo.infrastructure.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClientWebSocketHandler implements ClientWebSocket.MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientWebSocketHandler.class);

    private static final Gson GSON = new Gson();
    private final WebSocketService webSocketService;

    public ClientWebSocketHandler(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Override
    public void handleMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }



        InvocationMessage invocationMessage = GSON.fromJson(message, InvocationMessage.class);
        if (invocationMessage == null) {
            return;
        }

        switch (invocationMessage.getMethod())
        {
            case "notificationServerList":
                NotificationServerList data = GSON.fromJson(invocationMessage.getData(), NotificationServerList.class);
                webSocketService.notificationServerList(data);
                break;
            case "notification":
                List<Notification> data1 = GSON.fromJson(invocationMessage.getData(), TypeToken.getParameterized(List.class, Notification.class).getType());
                webSocketService.notification(data1);
                break;
            case "notificationMessage":
                List<NotificationMessage> data2 = GSON.fromJson(invocationMessage.getData(), TypeToken.getParameterized(List.class, NotificationMessage.class).getType());
                webSocketService.notificationMessage(data2);
                break;
            case "offline":
                webSocketService.offline();
                break;
            case "reset":
                ConnectReset data4 = GSON.fromJson(invocationMessage.getData(), ConnectReset.class);
                webSocketService.reset(data4);
                break;
            case "detection":
                webSocketService.detection();
                break;
            default:break;
        }

    }
}
