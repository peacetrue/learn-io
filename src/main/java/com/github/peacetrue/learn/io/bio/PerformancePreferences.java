package com.github.peacetrue.learn.io.bio;

import lombok.Builder;
import lombok.Data;

/**
 * 性能首选项，设置选项的优先级。
 *
 * @author peace
 */
@Data
@Builder
public class PerformancePreferences {
    /** 最少连接时间 */
    private int connectionTime;
    /** 最小延迟 */
    private int latency;
    /** 最高宽带 */
    private int bandwidth;
}
