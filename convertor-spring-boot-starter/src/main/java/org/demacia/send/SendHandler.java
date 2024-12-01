package org.demacia.send;

import org.demacia.domain.Context;

/**
 * @author hepenglin
 * @since 2024-08-08 17:18
 **/
public interface SendHandler {

    /**
     * 发送处理
     * @param context 上下文
     */
    void handle(Context context);
}
