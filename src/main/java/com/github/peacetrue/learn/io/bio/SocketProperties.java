package com.github.peacetrue.learn.io.bio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.net.SocketOptions;

/**
 * 套接字属性。
 *
 * @see SocketOptions
 */
@Data
@Builder
public class SocketProperties {
    /** 主机 */
    private String host;
    /** 监听的端口，有效值范围：0 ~ 65535，0 随机生成 */
    private Integer port;
    /** 连接队列长度，超过时拒绝 */
    private Integer backlog;
    /** 接收缓冲区容量（字节） */
    private Integer receiveBufferSize;
    /** 重用地址 */
    private Boolean reuseAddress;
    /** 连接超时时间（毫秒） */
    private Integer soTimeout;
    /** 性能首选项 */
    private PerformancePreferences performancePreferences;
    /** 是否保持长连接 */
    private Boolean keepalive;
    private Integer tcpNoDelay;
    private Integer oobInline;
}
