package org.demacia.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;
import org.demacia.constant.Const;

import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-08-16 17:27
 **/
@Slf4j
public class MessageFormatter {

    public static String determineMsgFormat(Map<String, Object> map, String msgFormat) {
        log.info("determineMsgFormat: map={}, msgFormat={}", map, msgFormat);
        if (MapUtil.isEmpty(map)) {
            return "";
        }
        String msg = "";
        if (StrUtil.equalsIgnoreCase(msgFormat, Const.MsgFormat.XML)) {
            String rootName = map.keySet().iterator().next();
            Map<String, Object> toMap = BeanUtil.beanToMap(map.get(rootName));
            msg = XmlUtil.mapToXmlStr(toMap, rootName);
        } else {
            msg = JSONUtil.toJsonStr(map);
        }
        return msg;
    }

    public static String determineMsgFormat(Object object, String messageFormat) {
        log.info("determineMsgFormat: object={}, messageFormat={}", object, messageFormat);
        if (ObjectUtil.isEmpty(object)) {
            return "";
        }
        String msg = "";
        if (Const.MessageFormat.XML.equals(messageFormat)) {
            XmlMapper xmlMapper = new XmlMapper();
            try {
                msg = xmlMapper.writeValueAsString(object);
            } catch (Exception e) {
                log.error("determineMsgFormat failure: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            msg = JSONUtil.toJsonStr(object);
        }
        return msg;
    }

}
