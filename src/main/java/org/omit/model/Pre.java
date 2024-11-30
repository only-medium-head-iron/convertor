package org.omit.model;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024-08-20 17:20
 **/
@Data
public class Pre {
    /**
     * 外部编号。
     * <p>
     * 外部编号。
     */
    private String outerNo;

    /**
     * OMS仓库或货主。
     * <p>
     * 该代码用于存储与订单相关的其他标识信息。
     */
    private String selfCode;

    /**
     * OMS仓库或货主。
     * <p>
     * 该值用于存储与OMS仓库或货主。
     */
    private String selfValue;

    /**
     * 其他系统仓库或货主。
     * <p>
     * 该代码用于存储OMS仓库或货主。
     */
    private String otherCode;

    /**
     * 其他系统仓库或货主。
     * <p>
     * 该值用于存储其他系统仓库或货主。
     */
    private String otherValue;
}
