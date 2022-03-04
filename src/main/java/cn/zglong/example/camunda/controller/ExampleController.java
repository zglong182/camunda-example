package cn.zglong.example.camunda.controller;

import cn.zglong.example.camunda.dto.MessageDTO;
import cn.zglong.example.camunda.utils.BpmnTaskUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/operate")
@Slf4j
@Api(tags = "流程操作")
public class ExampleController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;

    @GetMapping("/installId")
    @ApiOperation("根据实例id操作")
    public void install(String installId, String activityKey) {
        log.info("installId: {},activityKey: {}", installId, activityKey);

    }

    public void start() {
        List<String> userList = Arrays.asList("admin");
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("userList", userList);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("addAndaub", variables);
        log.info("instance: {}", instance.getProcessInstanceId());
    }

    /**
     * 多实例加签
     *
     * @param processInstanceId
     * @param taskDefKey        任务节点id
     */
    public void addAssignee(String processInstanceId, String taskDefKey) {
        runtimeService.createProcessInstanceModification(processInstanceId)
                .startBeforeActivity(taskDefKey).setVariable("user", "admin").execute();
    }

    /**
     * 多实例减签
     *
     * @param processInstanceId
     * @param activityInstanceId 任务节点实例id
     */
    public void removeAssignee(String processInstanceId, String activityInstanceId) {
        runtimeService.createProcessInstanceModification(processInstanceId)
                .cancelActivityInstance(activityInstanceId).execute();
    }

    /**
     * 协办(产生taskId的子任务)
     * 子任务完成与否对task没有影响
     * task完成子任务自动完成
     *
     * @param taskId
     */
    public void add(String taskId) {
        Task task = taskService.newTask();
        task.setName("请协助我");
        task.setAssignee("zglong");
        task.setParentTaskId(taskId);
        taskService.saveTask(task);
        // 能不能实现同时委派多人
//        task.setDelegationState();
    }

    /**
     * 查询已阅
     *
     * @param taskId
     */
    public void getHisReview(String taskId) {
        List<HistoricIdentityLinkLog> linkHisTask = historyService.createHistoricIdentityLinkLogQuery()
                .processDefinitionKey(null).processDefinitionId(null)
                .taskId(taskId).type(IdentityLinkType.CANDIDATE).list();
    }

    /**
     * 查询未阅
     *
     * @param taskId
     */
    public void getReview(String taskId) {
        List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
    }

    /**
     * 添加传阅
     *
     * @param taskId
     */
    public void addReview(String taskId) {
        taskService.addUserIdentityLink(taskId, "manager", IdentityLinkType.CANDIDATE);
    }

    /**
     * 转办/分配(manager提交后继续向后流转)
     *
     * @param taskId
     */
    public void setAssignee(String taskId) {
        taskService.createTaskQuery().taskId(taskId).singleResult().setAssignee("manager");
    }

    /**
     * 完成任务
     *
     * @param taskId
     */
    public void co(String taskId) {
        taskService.complete(taskId);
    }

    /**
     * 委派任务
     *
     * @param taskId
     */
    public void delegateTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 以下两种结果应该一样
        // 第一种
        // task.setOwner(task.getAssignee());
        // task.setAssignee("zglong");
        // task.setDelegationState(DelegationState.PENDING);

        // 第二种
        taskService.delegateTask(taskId, "zglong");
    }

    /**
     * (委派者)完成委派任务
     */
    public void resolveTask(String taskId) {
        // DelegationState为空则表示未被委派
        // DelegationState.PENDING 表示委派任务
        // DelegationState.RESOLVED 表示委派人已完成任务
        taskService.resolveTask(taskId);
    }


    @PostMapping("/jump")
    @ApiOperation("提交跳转")
    public void jumpTask(@RequestParam String taskId, String defId, String nodeId, String[] aryDestination) {
        Map<String, Object> prepare = BpmnTaskUtil.prepare(defId, nodeId, aryDestination);
        taskService.complete(taskId);
        BpmnTaskUtil.restore(prepare);

    }

    @PostMapping("/turn-to-do")
    @ApiOperation("转办")
    public void turnToDo(String taskId, String newUser) {

        // 验证转办是直接修改处理人 还是产生新的任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String oldUser = task.getAssignee();
        task.setAssignee(newUser);
        taskService.saveTask(task);
        String comment = oldUser + "将流程转办给" + newUser + "处理";
        taskService.createComment(taskId, task.getProcessInstanceId(), comment);
    }

    @PostMapping("/jump2")
    @ApiOperation("不提交跳转")
    public void notCompleteJumpTask(String taskId, String defId, String nodeId, String[] aryDestination) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);
        String activityInstanceId = activityInstance.getId();
        String activityInstanceId2 = "";
        taskService.createComment(taskId, processInstanceId, "往回跳转流程");
        for (ActivityInstance childActivityInstance : activityInstance.getChildActivityInstances()) {
            if (childActivityInstance.getActivityId().equals(nodeId)) {
                activityInstanceId = childActivityInstance.getId();
            } else {
                activityInstanceId2 = childActivityInstance.getActivityId();

            }
        }
        runtimeService
                .createProcessInstanceModification(processInstanceId)
                .cancelActivityInstance(activityInstanceId)
                .startBeforeActivity(nodeId)
                .startBeforeActivity(activityInstanceId2)
                .execute();
    }

    @PostMapping("/endorsement")
    @ApiOperation("加签")
    public void notCompleteJumpTask(String taskId, String activityId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        runtimeService
                .createProcessInstanceModification(processInstanceId)
                .startBeforeActivity(activityId)
                .execute();
    }

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/service")
    public String getService() {
        return appName;
    }

    @PostMapping("/add")
    @ApiOperation("test")
    public MessageDTO addMessage(@RequestBody MessageDTO messageDto) {
        System.out.println(messageDto.getName() + "A");
        return messageDto;
    }
}