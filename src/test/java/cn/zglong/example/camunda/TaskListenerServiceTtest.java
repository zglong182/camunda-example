package cn.zglong.example.camunda;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationService;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthentication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TaskListenerServiceTtest extends CamundaExampleApplicationTests {

    public static final String ProcessInstallId = "41a6e302-4867-11ec-8964-36de1ad39163";
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IdentityService identityService;
    @Resource
    private ProcessEngine processEngine;

    @Autowired
    private FormService formService;
    @Autowired
    private FilterService filterService;
    @Autowired
    private ManagementService managementService;

    @Test
    public void getAssigneeByTask() {
        String assignee = "admin";
        Task task = taskService.createTaskQuery().taskAssignee(assignee).singleResult();
        Task newTask = taskService.newTask("421d7090-08d4-11ec-b0e5-36de1ad39163");
        newTask.setAssignee("zglong");
        taskService.saveTask(newTask);
        System.out.println(1);
    }

    @Test
    public void rollBackMultiTask() {
        try {
            String userId = "bx6";
            String taskId = "0fe3ac21-0711-11ec-a5c9-00ff0269ba71";
            String processInstanceId = "ed1f2ae0-079f-11ec-ae32-00ff0269ba71";
            String toActId = "Activity_09emoea";
            AuthenticationService authenticationService = new AuthenticationService();
            String engineName = processEngine.getName();
            UserAuthentication authentication = (UserAuthentication) authenticationService
                    .createAuthenticate(engineName, userId, null, null);
            log.info("authentication--------->" + authentication.getName());

            identityService.setAuthenticatedUserId(authentication.getName());
            List<TaskDto> taskList = new ArrayList<TaskDto>();
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);

            /** 单实例节点驳回到多实例节点（串行、并行）使用 begin **/
            // runtimeService
            // .createProcessInstanceModification(processInstanceId)
            // .startBeforeActivity(toActId+"#multiInstanceBody")
            // .execute();
            //
            // taskService.complete(taskId);
            /** 单实例节点驳回到多实例节点使用 end **/

            /** 多实例节点驳回到多实例节点使用（串行、并行） begin **/
            runtimeService.createProcessInstanceModification(processInstanceId)
                    .startBeforeActivity(toActId + "#multiInstanceBody").execute();

            taskService.complete(taskId);

            /** 当前节点其他任务取消 **/
            runtimeService.createProcessInstanceModification(processInstanceId)
                    .cancelAllForActivity(task.getTaskDefinitionKey() + "#multiInstanceBody").execute();
            /** 多实例节点驳回到多实例节点使用 end **/

//            taskList = simpleGetTasks(processInstanceId);

            System.out.println(JSON.toJSONString(taskList));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消A节点并从B节点开始并设置变量
     */
    @Test
    public void test1() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(ProcessInstallId).singleResult();
        runtimeService.createProcessInstanceModification(processInstance.getId())
                //
                .startBeforeActivity("Activity_0hikuah")
                .setVariable("success", "ok")

                .cancelAllForActivity("Activity_1yc4px9")
                .execute();
    }

    /**
     * 取消B节点 并子流程指定节点开始
     */
    @Test
    public void test2() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(ProcessInstallId).singleResult();
        runtimeService.createProcessInstanceModification(processInstance.getId())
                .cancelAllForActivity("Activity_0hikuah")
                .startBeforeActivity("Activity_1aid1uq")
                .startBeforeActivity("Activity_0ro7j41")
                .execute();
    }

    /**
     * 取消A节点 从子流程的开始事件开始
     */
    @Test
    public void test3() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(ProcessInstallId).singleResult();
        runtimeService.createProcessInstanceModification(processInstance.getId())
                .cancelAllForActivity("Activity_0hikuah")
                .startBeforeActivity("Event_0d81u1o")
                .execute();
    }

    /**
     * 取消A节点 从子流程开始
     */
    @Test
    public void test4() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(ProcessInstallId).singleResult();
        runtimeService.createProcessInstanceModification(processInstance.getId())
                .cancelAllForActivity("Activity_0hikuah")
                .startBeforeActivity("Activity_12jvmkt")
                .execute();
    }

    @Test
    public void test5() {
        List<Task> tasks = taskService.createNativeTaskQuery()
                .sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = #{taskName}")
                .parameter("taskName", "aOpenTask")
                .list();

        long count = taskService.createNativeTaskQuery()
                .sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T1, "
                        + managementService.getTableName(VariableInstanceEntity.class) + " V1 WHERE V1.TASK_ID_ = T1.ID_")
                .count();
    }
}
