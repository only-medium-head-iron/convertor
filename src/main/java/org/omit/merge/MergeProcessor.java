package org.omit.merge;

import org.omit.rule.RuleMerge;

import java.util.List;
import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-09-22 15:41
 **/
public interface MergeProcessor {

    /**
     * 合并数据
     * @param ruleMerge 合并规则
     * @param mapList 待合并的数据列表
     * @return 合并后的数据列表
     */
    List<Map<String, Object>> merge(RuleMerge ruleMerge, List<Map<String, Object>> mapList);
}
