package org.demacia.config;

import com.mybatisflex.annotation.InsertListener;
import org.demacia.common.BaseEntity;

/**
 * @author hepenglin
 * @since 2025/12/29 14:55
 **/
public class MybatisInsertListener implements InsertListener {

    @Override
    public void onInsert(Object o) {
        Object username = "";
        if (o instanceof BaseEntity) {
            BaseEntity entity = (BaseEntity) o;
            entity.setUpdateBy(username.toString());
        }
    }
}
