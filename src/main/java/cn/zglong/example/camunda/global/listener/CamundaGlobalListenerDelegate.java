package cn.zglong.example.camunda.global.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;

/**
 * @author zglong
 * @version 1.0
 * @className CamundaGlobalListenerDelegate
 * @description 流程全局事件处理器
 * @date 2021/11/18 18:20
 */
@Slf4j
public class CamundaGlobalListenerDelegate implements ExecutionListener, TaskListener {

    @Override
    public void notify(DelegateTask task) {

        log.info("delegateTask------------------> " + task.getName() + "----------->" + task.getEventName() + "----->" +
                task.getBpmnModelElementInstance().getElementType().getTypeName() + "--->" + JSON.toJSONString(((TaskEntity) task).getPersistentState(), SerializerFeature.DisableCircularReferenceDetect));
    }


    @Override
    public void notify(DelegateExecution execution) throws Exception {

//        log.info("execution------------------> " + execution.getCurrentActivityName() +
//                "----------->" + execution.getEventName() +
//                "----->" +
//                execution.getBpmnModelElementInstance().getElementType().getTypeName() +
//                "---->" + JSON.toJSONString(((ExecutionEntity) execution).getPersistentState(),
//                SerializerFeature.DisableCircularReferenceDetect)
//        );

    }

}