package org.omit.send;

import com.wtbh.wcoms.module.cim.convert.model.Context;

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
