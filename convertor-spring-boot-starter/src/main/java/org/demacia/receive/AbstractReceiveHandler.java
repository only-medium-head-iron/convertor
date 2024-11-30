package org.demacia.receive;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

import lombok.extern.slf4j.Slf4j;
import org.demacia.Convertor;
import org.demacia.domain.Context;
import org.demacia.step.Step;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.LinkedList;

/**
 * @author hepenglin
 * @since 2024-08-08 15:31
 **/
@Slf4j
public abstract class AbstractReceiveHandler implements ReceiveHandler {

    @Resource
    private Convertor convertor;

    private LinkedList<Step> steps;

    /**
     * 重写handle方法，用于处理数据转换的整个流程
     *
     * @param context 上下文对象，包含需要处理的数据和规则ID
     * @return 转换后的对象
     */
    @Override
    public Object handle(Context context) {
        beforeConvert(context);
        Object object = convertor.convert(context.getRuleId(), BeanUtil.beanToMap(context));
        afterConvert(object);
        return object;
    }

    /**
     * 初始化步骤链，将步骤添加到链表中，以确定转换的顺序
     */
    @PostConstruct
    public void init() {
        this.steps = CollUtil.newLinkedList();
    }

    /**
     * 在转换过程开始之前执行一系列步骤
     *
     * @param context 转换上下文，包含可能需要传递给各个步骤的必要信息
     */
    public void beforeConvert(Context context) {
        for (Step step : steps) {
            step.run(context);
        }
    }

    /**
     * 在对象转换完成后执行的操作
     * <p>
     * 此方法旨在提供一个钩子，以便在对象经过转换过程后执行一些操作
     * 它可以被重写以实现特定于子类的后处理逻辑
     *
     * @param object 已转换的对象 可以根据需要对对象进行进一步处理
     */
    public void afterConvert(Object object) {
    }

}