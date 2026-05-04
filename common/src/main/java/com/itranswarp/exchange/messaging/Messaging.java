package com.itranswarp.exchange.messaging;

public interface Messaging {

    enum Topic {

        /**
         * Topic name: to sequence.
         * 
         * 序列服务使用
         * 用于生成事件序列号
         */
        SEQUENCE(1),

        /**
         * Topic name: to/from trading-engine.
         * 
         * 交易引擎使用
         * 用于处理转账事件
         */
        TRANSFER(1),

        /**
         * Topic name: events to trading-engine.
         * 
         * 交易引擎使用
         * 用于处理交易事件
         */
        TRADE(1),

        /**
         * Topic name: tick to quotation for generate bars.
         * 
         * 行情服务使用
         * 用于生成K线
         */
        TICK(1);

        private final int concurrency;

        Topic(int concurrency) {
            this.concurrency = concurrency;
        }

        public int getConcurrency() {
            return this.concurrency;
        }

        public int getPartitions() {
            return this.concurrency;
        }
    }
}
