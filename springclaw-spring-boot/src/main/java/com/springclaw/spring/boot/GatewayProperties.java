package com.springclaw.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gateway server configuration.
 */
@ConfigurationProperties(prefix = "springclaw.gateway")
public class GatewayProperties {

    /** Server port (default 8080). */
    private int port = 8080;

    /** Bind address (default "localhost"). */
    private String bind = "localhost";

    /** Whether to enable the built-in web channel. */
    private boolean webChannelEnabled = true;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBind() {
        return bind;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public boolean isWebChannelEnabled() {
        return webChannelEnabled;
    }

    public void setWebChannelEnabled(boolean webChannelEnabled) {
        this.webChannelEnabled = webChannelEnabled;
    }
}
