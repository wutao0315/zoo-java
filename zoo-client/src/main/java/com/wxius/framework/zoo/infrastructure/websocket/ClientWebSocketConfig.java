package com.wxius.framework.zoo.infrastructure.websocket;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.wxius.framework.zoo.Constants;
import com.wxius.framework.zoo.ZooOptions;
import com.wxius.framework.zoo.util.Signature;

import javax.websocket.ClientEndpointConfig;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientWebSocketConfig extends ClientEndpointConfig.Configurator {

    private static final Gson GSON = new Gson();
    private static final String ENCODING = "UTF-8";
    private final ZooOptions m_options;

    public ClientWebSocketConfig(ZooOptions options) {
        this.m_options = options;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        if (m_options.isSecure())
        {
            try {
                String encoded = Signature.genSign(m_options.getAccessKey(), m_options.getSecretKey());
                headers.put("Authorization", Collections.singletonList("api-zoo " + encoded));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        headers.put("ClientVersion", Collections.singletonList(Constants.ClientVersion));
        headers.put("ClientId", Collections.singletonList(m_options.getClientId()));
        headers.put("ClientName", Collections.singletonList(m_options.getClientName()));
        headers.put("GroupName", Collections.singletonList(m_options.getGroupName()));
        Map<String,String> metadata = new HashMap<>(m_options.getMetadata());
        if (m_options.getPublish().size() > 0)
        {
            metadata.put(Constants.LABEL_MODULE, Constants.LABEL_MODULE_NAMING);
        }
        String metadataJson = GSON.toJson(metadata);
        byte[] metadataBytes = new byte[0];
        try {
            metadataBytes = metadataJson.getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String metadataBase64 = BaseEncoding.base64().encode(metadataBytes);
        headers.put("Metadata", Collections.singletonList(metadataBase64));
    }
}
