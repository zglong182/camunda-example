package cn.zglong.example.camunda.controller

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.delegate.DelegateTask;

//def task = new DelegateTask();

print '----------------------------------------- >>>>>>>>>>>> get taskName'
def taskService = task.getProcessEngine().getTaskService();
def taskName = taskService.createTaskQuery().taskDefinitionKey(task.getTaskDefinitionKey()).singleResult().getName();
print(taskName);

print '----------------------------------------- >>>>>>>>>>>> get businessKey'
def runtimeService = task.getProcessEngine().getRuntimeService();
def businessKey = runtimeService.createProcessInstanceQuery().activityIdIn(task.getTaskDefinitionKey()).singleResult().getBusinessKey();
println(businessKey);
print '----------------------------------------- >>>>>>>>>>>> set Variable'
def userList = new ArrayList<String>()
userList.add("admin")
println(userList.toString())
task.setVariable("userList", userList)

