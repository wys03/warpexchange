package com.itranswarp.exchange.message.event;

import org.springframework.lang.Nullable;

import com.itranswarp.exchange.message.AbstractMessage;

/**
 * 抽象事件
 */
public class AbstractEvent extends AbstractMessage {

    /**
     * Message id, set after sequenced.
     * 
     * 事件序列号
     */
    public long sequenceId;

    /**
     * Previous message sequence id.
     * 
     * 前一个事件序列号
     */
    public long previousId;

    /**
     * Unique ID or null if not set.
     * 
     * 唯一ID或null如果未设置
     */
    @Nullable
    public String uniqueId;
}
