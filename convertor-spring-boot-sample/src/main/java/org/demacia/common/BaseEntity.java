package org.demacia.common;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author hepenglin
 * @since 2025/12/29 14:23
 **/
@Data
public class BaseEntity implements Serializable {

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建人
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private Long updateBy;

    /**
     * 更新人
     */
    private String updateName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志
     */
    @Column(isLogicDelete = true)
    private Boolean deleted;
}
