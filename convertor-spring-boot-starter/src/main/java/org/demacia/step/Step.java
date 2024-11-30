package org.demacia.step;

import org.demacia.domain.Context;

/**
 * @author 何朋林
 * @since 2024/9/26 20:26
 */
public interface Step {

    /**
     * 执行
     * @param context 上下文
     */
    void run(Context context);

}
