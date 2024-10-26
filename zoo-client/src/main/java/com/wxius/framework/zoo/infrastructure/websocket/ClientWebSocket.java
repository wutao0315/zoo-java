package com.wxius.framework.zoo.infrastructure.websocket;

import com.wxius.framework.zoo.ZooOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import javax.websocket.*;

public class ClientWebSocket extends Endpoint {
    private static final Logger logger = LoggerFactory.getLogger(ClientWebSocket.class);

    public boolean isOpened() {
        return opened;
    }

    private boolean opened;

    private Session userSession = null;
    private MessageHandler messageHandler;

    private final WebSocketContainer webSocketContainer;

    private final ClientWebSocket _this;

    private final ZooOptions m_options;


    public ClientWebSocket(ZooOptions options) {
        this._this = this;
        this.m_options = options;
        webSocketContainer = ContainerProvider.getWebSocketContainer();
    }

    public void connect(String server) {
        server = server.toLowerCase();
        if (!server.endsWith("/")) {
            server += "/";
        }
        server += "ws";
        if (server.startsWith("http://")) {
            server = server.replace("http://", "ws://");
        }
        if (server.startsWith("https://")) {
            server = server.replace("https://", "wss://");
        }
        ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
        configBuilder.configurator(new ClientWebSocketConfig(m_options));
        ClientEndpointConfig clientConfig = configBuilder.build();
        try {
            webSocketContainer.connectToServer(this, clientConfig, new URI(server));
            logger.info(String.format("websocket client connect to %s successful , appId: %s", server, this.m_options.getAccessKey()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        if (opened) {
            if (userSession != null) {
                try {
                    userSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "close by server force"));
                } catch (IOException e) {
                    logger.error("try to close websocket client error", e);
                }
            }
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        logger.info("opening websocket");
        this.opened = true;
        this.userSession = session;
        this.userSession.addMessageHandler(new javax.websocket.MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String s) {
                _this.onMessage(s);
            }
        });
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason      the reason for connection close
     */
    @Override
    public void onClose(Session userSession, CloseReason reason) {
        logger.info("closing websocket");
        this.opened = false;
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    public void onMessage(String message) {
        logger.info("receive message " + message);
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        if (this.userSession != null) {
            this.userSession.getAsyncRemote().sendText(message);
        }
    }

    /**
     * Message handler.
     */
    public interface MessageHandler {

        void handleMessage(String message);
    }
}
