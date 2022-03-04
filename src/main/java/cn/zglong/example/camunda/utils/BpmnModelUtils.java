package cn.zglong.example.camunda.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.CompletionCondition;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Classname BpmnUtils
 * @description: BpmnModel工具类
 * @author: zglong
 * @create: 2020-11-25 17:56
 **/
public class BpmnModelUtils {

    /**
     * 根据流程id获得流程对象.
     *
     * @param bpmnModelInstance bpmnModelInstance
     * @param processId         流程kay
     * @return
     */
    public static Process getProcess(BpmnModelInstance bpmnModelInstance, String processId) {
        Process process = (Process) bpmnModelInstance.getModelElementById(processId);
        if (ObjectUtils.isEmpty(process)) {
            return null;
        } else {
            return process;
        }
    }


    /**
     * 获得流程id.
     *
     * @param bpmnModelInstance bpmnModelInstance
     * @return
     */
    public static String getProcessId(BpmnModelInstance bpmnModelInstance) {
        Collection<Process> processList = bpmnModelInstance.getModelElementsByType(Process.class);
        for (Process process : processList) {
            String processName = process.getId();
            if (StringUtils.isNoneBlank(processName)) {
                return processName;
            }
        }
        return null;
    }


    /**
     * 获得流程名称.
     *
     * @param bpmnModelInstance bpmnModelInstance
     * @return
     */
    public static String getProcessName(BpmnModelInstance bpmnModelInstance) {
        Collection<Process> processList = bpmnModelInstance.getModelElementsByType(Process.class);
        for (Process process : processList) {
            String processName = process.getName();
            if (StringUtils.isNoneBlank(processName)) {
                return processName;
            }
        }
        return null;
    }


    /**
     * 获取流程全局监听.
     *
     * @return
     */
    public static List<Map<String, String>> getProcessListener(Process process) {
        List<CamundaExecutionListener> camundaExecutionListenerList = process.getExtensionElements().getElementsQuery().filterByType(CamundaExecutionListener.class).list();
        List<Map<String, String>> listenerMaps = new ArrayList<>();
        for (CamundaExecutionListener listener : camundaExecutionListenerList) {
            Map<String, String> map = new HashMap<>(4);
            getListenerMap(listenerMaps, listener, map);
        }
        return listenerMaps;
    }


    /**
     * CamundaExecutionListener转map
     *
     * @param listenerMaps
     * @param listener
     * @param map
     */
    private static void getListenerMap(List<Map<String, String>> listenerMaps, CamundaExecutionListener listener, Map<String, String> map) {
        if (!StringUtils.isEmpty(listener.getCamundaClass())) {
            map.put(listener.getCamundaEvent(), listener.getCamundaClass());
        } else if (!StringUtils.isEmpty(listener.getCamundaExpression())) {
            map.put(listener.getCamundaEvent(), listener.getCamundaClass());
        } else if (!StringUtils.isEmpty(listener.getCamundaDelegateExpression())) {
            map.put(listener.getCamundaEvent(), listener.getCamundaDelegateExpression());
        }
        listenerMaps.add(map);
    }


    /**
     * 获取流程全局自定义属性
     *
     * @param process process
     * @return
     */
    public static List<BpmvProperty> getProcessProperty(Process process) {
        ExtensionElements extensionElements = process.getExtensionElements();
        List<CamundaProperties> camundaPropertiesList = extensionElements.getElementsQuery().filterByType(CamundaProperties.class).list();
        if (camundaPropertiesList.size() == 1) {
            return resolveCamundaProperties(camundaPropertiesList.get(0));
        }
        return new ArrayList<BpmvProperty>();
    }


    /**
     * 根据节点id获得用户任务.
     *
     * @param bpmnModelInstance bpmnModelInstance
     * @param actityId          用户id
     * @return
     */
    public static UserTask getUserTask(BpmnModelInstance bpmnModelInstance, String actityId) {
        UserTask userTask = (UserTask) bpmnModelInstance.getModelElementById(actityId);

        if (ObjectUtils.isEmpty(userTask)) {
            return null;
        } else {
            return userTask;
        }

    }


    /**
     * 获取用户任务的自定义属性.
     *
     * @param userTask
     * @return
     */
    public static List<BpmvProperty> getUserTaskProperty(UserTask userTask) {
        if (ObjectUtils.isEmpty(userTask)) {
            return new ArrayList<BpmvProperty>();
        }
        ExtensionElements extensionElements = userTask.getExtensionElements();
        if (!ObjectUtils.isEmpty(extensionElements)) {
            Query<ModelElementInstance> elementsQuery = extensionElements.getElementsQuery();
            List<CamundaProperties> camundaPropertiesList = elementsQuery.filterByType(CamundaProperties.class).list();
            if (camundaPropertiesList.size() == 1) {
                return resolveCamundaProperties(camundaPropertiesList.get(0));
            }
            return new ArrayList<BpmvProperty>();
        }
        return new ArrayList<BpmvProperty>();
    }


    /**
     * 获取下个节点
     *
     * @param bpmnModelInstance
     * @param userTaskId
     * @return
     */
    public static List<UserTask> getNextTask(BpmnModelInstance bpmnModelInstance, String userTaskId) {
        List<UserTask> userTasks = new ArrayList<>();
        UserTask userTask = getUserTask(bpmnModelInstance, userTaskId);
        Collection<SequenceFlow> outgoing = userTask.getOutgoing();
        for (SequenceFlow sequenceFlow : outgoing) {
            // 当前连线的输出
            String targetRef = sequenceFlow.getTarget().getId();
            if (StringUtils.isNotEmpty(targetRef)) {
                if (targetRef.contains("Activity")) {
                    // 输出节点是任务节点时
                    ModelElementInstance bpmnInstance = bpmnModelInstance.getModelElementById(targetRef);
                    if (bpmnInstance instanceof UserTask) {
                        userTasks.add((UserTask) bpmnInstance);
                    }
                } else if (targetRef.contains("Gateway")) {
                    // 输出节点是网关时
                    ModelElementInstance bpmnInstance = bpmnModelInstance.getModelElementById(targetRef);
                    if (bpmnInstance instanceof ExclusiveGateway) {
                        ExclusiveGateway exclusiveGateway = (ExclusiveGateway) bpmnInstance;
                        // 网关的输出
                        Collection<SequenceFlow> sequenceFlows = exclusiveGateway.getOutgoing();
                        for (SequenceFlow flow : sequenceFlows) {
                            FlowNode node = flow.getTarget();
                            // 网关的输出是用户节点时
                            if (node instanceof UserTask && node.getId().contains("Activity")) {
                                userTasks.add((UserTask) node);
                            }
                        }
                    }

                }
            }
        }
        return userTasks;
    }


    /**
     * 获取下个节点处理人
     */
//    public static List<TaskVariableDto> getNextNodeAssignee(BpmnModelInstance bpmnModelInstance, String userTaskId) {
//        List<TaskVariableDto> taskVariableDtos = new ArrayList<>();
//        UserTask userTask = getUserTask(bpmnModelInstance, userTaskId);
//        // 获取当前节点的输出
//        Collection<SequenceFlow> outgoing = userTask.getOutgoing();
//        for (SequenceFlow sequenceFlow : outgoing) {
//            // 当前连线的输出
//            String targetRef = sequenceFlow.getTarget().getId();
//            if (StringUtils.isNotEmpty(targetRef)) {
//                if (targetRef.contains("Activity")) {
//                    // 输出节点是任务节点时
//                    ModelElementInstance bpmnInstance = bpmnModelInstance.getModelElementById(targetRef);
//                    if (bpmnInstance instanceof UserTask) {
//                        TaskVariableDto taskVariableDto = getNodeAssignee(bpmnModelInstance, targetRef);
//                        if (StringUtils.isNotNull(taskVariableDto)) {
//                            taskVariableDtos.add(taskVariableDto);
//                        }
//                    }
//                } else if (targetRef.contains("Gateway")) {
//                    // 输出节点是网关时
//                    ModelElementInstance bpmnInstance = bpmnModelInstance.getModelElementById(targetRef);
//                    if (bpmnInstance instanceof ExclusiveGateway) {
//                        ExclusiveGateway exclusiveGateway = (ExclusiveGateway) bpmnInstance;
//                        // 网关的输出
//                        Collection<SequenceFlow> sequenceFlows = exclusiveGateway.getOutgoing();
//                        for (SequenceFlow flow : sequenceFlows) {
//                            FlowNode node = flow.getTarget();
//                            // 网关的输出是用户节点时
//                            if (node instanceof UserTask && node.getId().contains("Activity")) {
//                                TaskVariableDto taskVariableDto = getNodeAssignee(bpmnModelInstance, node.getId());
//                                if (StringUtils.isNotNull(taskVariableDto)) {
//                                    taskVariableDto.setIncoming(flow.getName());
//                                    taskVariableDtos.add(taskVariableDto);
//                                }
//                            }
//                        }
//                    }
//
//                }
//            }
//        }
//        return taskVariableDtos;
//    }


//    public static TaskVariableDto getNodeAssignee(BpmnModelInstance bpmnModelInstance, String userTaskId) {
//        TaskVariableDto taskVariableDto = new TaskVariableDto();
//        ModelElementInstance instance = bpmnModelInstance.getModelElementById(userTaskId);
//        if (instance instanceof UserTask) {
//            UserTask userTask = (UserTask) instance;
//            String camundaAssignee = userTask.getCamundaAssignee();
//            // Assignee为空时
//            if (StringUtils.isEmpty(camundaAssignee)) {
//                return null;
//            }
//            camundaAssignee = camundaAssignee.trim();
//            // Assignee不包含变量时
//            if (!(camundaAssignee.contains("${") && camundaAssignee.contains("}"))) {
//                return null;
//            }
//            camundaAssignee = getTag(camundaAssignee);
//            // 节点名称
//            taskVariableDto.setTitle(userTask.getName());
//            // 人员变量名称
//            taskVariableDto.setTag(camundaAssignee);
//            // 多实例
//            Collection<MultiInstanceLoopCharacteristics> multiInstanceLoopCharacteristics
//                    = userTask.getChildElementsByType(MultiInstanceLoopCharacteristics.class);
//            taskVariableDto.setMulti(multiInstanceLoopCharacteristics.size() > 0);
//            if (taskVariableDto.isMulti()) {
//                for (MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristic : multiInstanceLoopCharacteristics) {
//                    // 多实例人员集合
//                    String camundaCollection = multiInstanceLoopCharacteristic.getCamundaCollection();
//                    taskVariableDto.setTagList(camundaCollection);
//
//                    // 会签规则
//                    CompletionCondition completionCondition = multiInstanceLoopCharacteristic.getCompletionCondition();
//                    if (!org.springframework.util.StringUtils.isEmpty(completionCondition)) {
//                        taskVariableDto.setCountersigningRules(getTag(completionCondition.getTextContent()));
//                    }
//                    // 循环次数
//                    LoopCardinality loopCardinality = multiInstanceLoopCharacteristic.getLoopCardinality();
//                    if (StringUtils.isNotNull(loopCardinality)) {
//                        String count = loopCardinality.getTextContent();
//                        if (StringUtils.isNotEmpty(count)) {
//                            if (camundaAssignee.contains("${") && camundaAssignee.contains("}")) {
//                                taskVariableDto.setFrequency(getTag(count));
//                            }
//                        }
//                    }
//                }
//            }
//            return taskVariableDto;
//        } else {
//            return null;
//        }
//    }


    /**
     * 获得用户节点的Listener.
     *
     * @param userTask
     */
    public static List<Map<String, String>> getTaskUserListener(UserTask userTask) {
        List<Map<String, String>> listenerMaps = new ArrayList<>();
        if (ObjectUtils.isEmpty(userTask)) {
            return listenerMaps;
        }
        Query<ModelElementInstance> elementsQuery = userTask.getExtensionElements().getElementsQuery();

        List<CamundaTaskListener> listenerList = elementsQuery.filterByType(CamundaTaskListener.class).list();
        for (CamundaTaskListener listener : listenerList) {
            Map<String, String> map = new HashMap<>(4);
            if (!StringUtils.isEmpty(listener.getCamundaClass())) {
                map.put(listener.getCamundaEvent(), listener.getCamundaClass());
            } else if (!StringUtils.isEmpty(listener.getCamundaExpression())) {
                map.put(listener.getCamundaEvent(), listener.getCamundaClass());
            } else if (!StringUtils.isEmpty(listener.getCamundaDelegateExpression())) {
                map.put(listener.getCamundaEvent(), listener.getCamundaDelegateExpression());
            }
            listenerMaps.add(map);
        }
        return listenerMaps;
    }


    /**
     * 获取用户节点(网关)的路由选项.
     *
     * @param userTask 网关的前一个用户节点
     */
//    public static List<RouteDto> getUserTaskopinionOption(BpmnModelInstance bpmnModelInstance, UserTask userTask) {
//        List<RouteDto> routeDtoList = new ArrayList<>();
//        if (StringUtils.isNull(bpmnModelInstance)) {
//            return routeDtoList;
//        }
//        if (StringUtils.isNull(userTask)) {
//            return routeDtoList;
//        }
//        for (SequenceFlow sequenceFlow : userTask.getOutgoing()) {
//            String targetId = sequenceFlow.getTarget().getId();
//            if (targetId.contains("Gateway")) {
//                String gatewayId = targetId;
////                ExclusiveGateway exclusiveGateway = getGateway(bpmnModelInstance, gatewayId);
//                routeDtoList = getGatewayOutgoing(bpmnModelInstance.getModelElementById(gatewayId));
//
//            }
//        }
//        return routeDtoList;
//    }


    public static MultiInstanceLoopCharacteristics getUserMultiInstance(UserTask userTask) {
        if (ObjectUtils.isEmpty(userTask)) {
            return null;
        }

        // 多实例
        Collection<MultiInstanceLoopCharacteristics> collection
                = userTask.getChildElementsByType(MultiInstanceLoopCharacteristics.class);
        for (MultiInstanceLoopCharacteristics multiInstance : collection) {
            // 获得集合变量
            String camundaCollection = multiInstance.getCamundaCollection();
            // 元素变量
            String camundaElementVariable = multiInstance.getCamundaElementVariable();
            // 获得完成条件
            CompletionCondition completionCondition = multiInstance.getCompletionCondition();
            String completionConditions = completionCondition.getTextContent();
        }
        if (collection.size() == 1) {
            return ((List<MultiInstanceLoopCharacteristics>) collection).get(0);
        }
        return null;
    }


    /**
     * 根据flowId获取SequenceFlow.
     *
     * @param bpmnModelInstance
     * @param flowId            flowId
     * @return
     */
    public static SequenceFlow getSequenceFlow(BpmnModelInstance bpmnModelInstance, String flowId) {
        SequenceFlow sequenceFlow = (SequenceFlow) bpmnModelInstance.getModelElementById(flowId);

        if (ObjectUtils.isEmpty(sequenceFlow)) {
            throw null;
        } else {
            return sequenceFlow;
        }
    }


    /**
     * 获得流程连线的监听
     *
     * @param sequenceFlow
     * @return
     */
    public static List<Map<String, String>> getSequenceFlowListener(SequenceFlow sequenceFlow) {
        List<CamundaExecutionListener> camundaExecutionListenerList = sequenceFlow.getExtensionElements().getElementsQuery().filterByType(CamundaExecutionListener.class).list();

        List<Map<String, String>> listenerMaps = new ArrayList<>();

        for (CamundaExecutionListener listener : camundaExecutionListenerList) {
            Map<String, String> map = new HashMap<>(4);
            getListenerMap(listenerMaps, listener, map);
        }
        return listenerMaps;
    }


    /**
     * 获得流程连线的自定义属性
     *
     * @param sequenceFlow
     * @return
     */
    public static List<BpmvProperty> getSequenceFlowProperty(SequenceFlow sequenceFlow) {
        List<CamundaProperties> camundaPropertiesList = sequenceFlow.getExtensionElements()
                .getElementsQuery().filterByType(CamundaProperties.class).list();
        if (camundaPropertiesList.size() == 1) {
            return resolveCamundaProperties(camundaPropertiesList.get(0));
        }
        return new ArrayList<BpmvProperty>();

    }


    /**
     * 获取连线的名称-表达式.
     *
     * @param sequenceFlow
     * @return
     */
//    public static RouteDto getSequenceFlowExpression(SequenceFlow sequenceFlow) {
//        Map<String, String> map = new HashMap<>(4);
//        ConditionExpression conditionExpression = sequenceFlow.getConditionExpression();
//        if (StringUtils.isNull(conditionExpression)) {
//            return null;
//        }
//        String textContent = conditionExpression.getTextContent();
//
//        map.put(sequenceFlow.getName(), textContent);
//        // flag == 'false'
//        textContent = textContent.substring(2, textContent.length() - 1);
//        String[] tagValue = textContent.split("==");
//        RouteDto routeDto = new RouteDto();
//        if (org.xhdmp.common.webmvc.util.StringUtils.isNotEmpty(sequenceFlow.getName()) && tagValue.length == 2) {
//            if (tagValue[1].contains("'")) {
//                routeDto.setTitle(sequenceFlow.getName());
//                routeDto.setValue(tagValue[1].trim().substring(1, tagValue[1].trim().length() - 1));
//                routeDto.setTag(tagValue[0].trim());
//            }
//            return routeDto;
//        } else {
//            return null;
//        }
//
//    }


    /**
     * 根据id获得网关.
     *
     * @param bpmnModelInstance
     * @param gatewayId         网关id
     * @return
     */
    public static ExclusiveGateway getGateway(BpmnModelInstance bpmnModelInstance, String gatewayId) {
        ExclusiveGateway exclusiveGateway = (ExclusiveGateway) bpmnModelInstance.getModelElementById(gatewayId);
        if (ObjectUtils.isEmpty(exclusiveGateway)) {
            return null;
        } else {
            return exclusiveGateway;
        }
    }


    /**
     * 获取网关的路由选项.
     *
     * @param t 网关泛型
     * @return
     */
//    public static <T> List<RouteDto> getGatewayOutgoing(T t) {
//        List<RouteDto> routeDtoList = new ArrayList<>();
//        if (t == null) {
//            return routeDtoList;
//        }
//        if (t instanceof ExclusiveGateway) {
//            getExclusiveGatewayOutgoing((ExclusiveGateway) t, routeDtoList);
//        } else if (t instanceof ParallelGateway) {
//            getParallelGatewayOutgoing((ParallelGateway) t, routeDtoList);
//        }
//        return routeDtoList;
//    }


//    private static void getExclusiveGatewayOutgoing(ExclusiveGateway exclusiveGateway, List<RouteDto> routeDtoList) {
//        for (SequenceFlow sequenceFlow : exclusiveGateway.getOutgoing()) {
//            RouteDto routeDto = getSequenceFlowExpression(sequenceFlow);
//            if (StringUtils.isNotNull(routeDto)) {
//                routeDtoList.add(routeDto);
//            }
//        }
//    }


//    private static void getParallelGatewayOutgoing(ParallelGateway parallelGateway, List<RouteDto> routeDtoList) {
//        for (SequenceFlow sequenceFlow : parallelGateway.getOutgoing()) {
//            RouteDto routeDto = getSequenceFlowExpression(sequenceFlow);
//            if (StringUtils.isNotNull(routeDto)) {
//                routeDtoList.add(routeDto);
//            }
//        }
//    }


    /**
     * 解析CamundaProperties.
     *
     * @param camundaProperties CamundaProperties.class
     * @return List<CamundaCustomizeProperty>
     */
    public static List<BpmvProperty> resolveCamundaProperties(CamundaProperties camundaProperties) {
//        if (StringUtils.isNull(camundaProperties)) {
//            throw new DMPException("不存在自定义属性");
//        }
        List<BpmvProperty> camundaCustomizeProperties = new ArrayList<>();
        camundaProperties.getCamundaProperties().forEach(camundaProperty -> {
            BpmvProperty bpmvProperty = new BpmvProperty();
            bpmvProperty.setCode(camundaProperty.getCamundaId());
            bpmvProperty.setName(camundaProperty.getCamundaName());
            bpmvProperty.setValue(camundaProperty.getCamundaValue());
            camundaCustomizeProperties.add(bpmvProperty);
        });
        return camundaCustomizeProperties;
    }


    /**
     * ${user} 获得user.
     *
     * @param text
     * @return
     */
    public static String getTag(String text) {
        text = text.trim();
        text = text.substring(2, text.length() - 1);
        return text;
    }
}

