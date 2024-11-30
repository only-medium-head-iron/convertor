package org.demacia.receive;

import org.demacia.domain.Context;

/**
 * @author hepenglin
 * @since 2024-08-08 17:18
 **/
public interface ReceiveHandler {
    /**
     * 处理接收数据
     * @param context 上下文
     * @return 返回结果
     */
    Object handle(Context context);
}
