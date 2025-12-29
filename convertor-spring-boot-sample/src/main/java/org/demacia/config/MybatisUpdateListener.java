package org.demacia.config;

import com.mybatisflex.annotation.UpdateListener;
import org.demacia.common.BaseEntity;

/**
 * @author hepenglin
 * @since 2025/12/29 14:55
 **/
public class MybatisUpdateListener implements UpdateListener {

    @Override
    public void onUpdate(Object o) {
        Object username = "";
        if (o instanceof BaseEntity) {
            BaseEntity entity = (BaseEntity) o;
            entity.setUpdateBy(username.toString());
        }
    }
}
