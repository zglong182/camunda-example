package cn.zglong.example.camunda.global.listener;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;

import java.util.List;

/**
 * @author zglong
 * @version 1.0
 * @className CamundaGlobalListener
 * @description 流程全局事件监听注入
 * @date 2021/11/18 18:19
 */
public class CamundaGlobalListener extends AbstractBpmnParseListener {
    public final static ExecutionListener EXECUTION_LISTENER = new CamundaGlobalListenerDelegate();
    public final static TaskListener TASK_LISTENER = new CamundaGlobalListenerDelegate();

    protected void addEndEventListener(ScopeImpl activity) {
        //activity.addExecutionListener(ExecutionListener.EVENTNAME_END, EXECUTION_LISTENER);
        activity.addListener(ExecutionListener.EVENTNAME_END, EXECUTION_LISTENER);
    }

    protected void addStartEventListener(ScopeImpl activity) {
        //activity.addExecutionListener(ExecutionListener.EVENTNAME_START, EXECUTION_LISTENER);
        activity.addListener(ExecutionListener.EVENTNAME_START, EXECUTION_LISTENER);
    }

    protected void addTakeEventListener(TransitionImpl transition) {
        //transition.addExecutionListener(EXECUTION_LISTENER);
        transition.addListener(ExecutionListener.EVENTNAME_TAKE, EXECUTION_LISTENER);

    }

    protected void addTaskAssignmentListeners(TaskDefinition taskDefinition) {
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, TASK_LISTENER);
    }

    protected void addTaskCreateListeners(TaskDefinition taskDefinition) {
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, TASK_LISTENER);
    }

    protected void addTaskCompleteListeners(TaskDefinition taskDefinition) {
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, TASK_LISTENER);
    }

    protected void addTaskUpdateListeners(TaskDefinition taskDefinition) {
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_UPDATE, TASK_LISTENER);
    }

    protected void addTaskDeleteListeners(TaskDefinition taskDefinition) {
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_DELETE, TASK_LISTENER);
    }

    protected void addTaskTimeoutListeners(TaskDefinition taskDefinition) {
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_TIMEOUT, TASK_LISTENER);
    }

    // BpmnParseListener implementation
    // /

    @Override
    public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
        addStartEventListener(processDefinition);
        addEndEventListener(processDefinition);
    }

    @Override
    public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
        addStartEventListener(startEventActivity);
        addEndEventListener(startEventActivity);
    }

    @Override
    public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
        UserTaskActivityBehavior activityBehavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
        TaskDefinition taskDefinition = activityBehavior.getTaskDefinition();
        addTaskCreateListeners(taskDefinition);
        addTaskAssignmentListeners(taskDefinition);
        addTaskCompleteListeners(taskDefinition);
        addTaskUpdateListeners(taskDefinition);
        addTaskDeleteListeners(taskDefinition);
    }

    @Override
    public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting,
                                                  ActivityImpl timerActivity) {
        // start and end event listener are set by parseBoundaryEvent()
    }

    @Override
    public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting,
                                                  ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
        // start and end event listener are set by parseBoundaryEvent()
    }

    @Override
    public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
    }

    @Override
    public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
        addTakeEventListener(transition);
    }

    @Override
    public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseMultiInstanceLoopCharacteristics(Element activityElement,
                                                      Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
    }

    @Override
    public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
        // start and end event listener are set by parseIntermediateCatchEvent()
    }

    @Override
    public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition,
                                                            ActivityImpl signalActivity) {
        // start and end event listener are set by parseIntermediateCatchEvent()
    }

    @Override
    public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting,
                                                   ActivityImpl signalActivity) {
        // start and end event listener are set by parseBoundaryEvent()
    }

    @Override
    public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {

    }

    @Override
    public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl activity) {
        addStartEventListener(activity);
        addEndEventListener(activity);
    }

    @Override
    public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition,
                                                             ActivityImpl nestedActivity) {
    }

    @Override
    public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting,
                                                    ActivityImpl messageActivity) {
    }

    @Override
    public void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition, boolean interrupting,
                                                       ActivityImpl boundaryEventActivity) {
    }

    @Override
    public void parseBoundaryConditionalEventDefinition(Element element, boolean interrupting,
                                                        ActivityImpl conditionalActivity) {
    }

    @Override
    public void parseIntermediateConditionalEventDefinition(Element conditionalEventDefinition,
                                                            ActivityImpl conditionalActivity) {
    }

    public void parseConditionalStartEventForEventSubprocess(Element element, ActivityImpl conditionalActivity,
                                                             boolean interrupting) {
    }

}