package cn.zglong.example.camunda.utils;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class BpmnTaskUtil {
	
	public static Object cloneObject(Object obj) {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Object rtn = null;

        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteOut);
            out.writeObject(obj);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            in = new ObjectInputStream(byteIn);
            rtn = in.readObject();
        } catch (IOException var16) {
            log.error("clone object failed {}.", var16.getMessage());
        } catch (ClassNotFoundException var17) {
            log.error("class not found {}.", var17.getMessage());
        } finally {
            try {
                if (null != out) {
                    out.close();
                }

                if (null != in) {
                    in.close();
                }
            } catch (IOException var15) {
            }

        }

        return rtn;
    }
	
	/**
	 * 将节点之后的节点删除然后指向新的节点。 
	 * @param actDefId			流程定义ID
	 * @param nodeId			流程节点ID
	 * @param aryDestination	需要跳转的节点
	 * @return Map<String,Object> 返回节点和需要恢复节点的集合。
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> prepare(String actDefId, String nodeId, String[] aryDestination){
		Map<String,Object> map=new HashMap<String, Object>();


		RepositoryService repositoryService=BeanUtils.getBean(RepositoryService.class);
		
		//修改流程定义
		ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(actDefId);

		ActivityImpl curAct= processDefinition.findActivity(nodeId);
		List<PvmTransition> outTrans= curAct.getOutgoingTransitions();
		//原来用activiti的是可以直接clone的 但是camunda不行
		//List<PvmTransition> cloneOutTrans=(List<PvmTransition>) cloneObject(outTrans);
		//map.put("outTrans", outTrans);
		
		/**
		 * 解决通过选择自由跳转指向同步节点导致的流程终止的问题。
		 * 在目标节点中删除指向自己的流转。
		 */
		List<Map<String,Object>> items = new ArrayList<>();
		List<PvmTransition> cloneOutTrans = new ArrayList<>();
		for(Iterator<PvmTransition> it = outTrans.iterator(); it.hasNext();){
			PvmTransition transition=it.next();
			cloneOutTrans.add(transition);
			PvmActivity activity= transition.getDestination();
			List<PvmTransition> inTrans= activity.getIncomingTransitions();
			for(Iterator<PvmTransition> itIn=inTrans.iterator();itIn.hasNext();){
				PvmTransition inTransition=itIn.next();
				if(inTransition.getSource().getId().equals(curAct.getId())){
					Map<String,Object> item = new HashMap<>();
					item.put(activity.getId(),inTransition);
					items.add(item);
					itIn.remove();
				}
			}
		}
		//所以改了一种方式去实现
		map.put("outTrans", cloneOutTrans);
		map.put("items", items);
		
		curAct.getOutgoingTransitions().clear();
		
		if(aryDestination!=null && aryDestination.length>0){
			for(String dest:aryDestination){
				//创建一个连接
				ActivityImpl destAct= processDefinition.findActivity(dest);
				TransitionImpl transitionImpl = curAct.createOutgoingTransition();
				transitionImpl.setDestination(destAct);
			}
		}
		
		map.put("activity", curAct);
		
		return map;
		
	}
	
	/**
	 * 将临时节点清除掉，加回原来的节点。
	 * @param map 
	 * void
	 */
	@SuppressWarnings("unchecked")
	public static void restore(Map<String,Object> map){
		ActivityImpl curAct=(ActivityImpl) map.get("activity");
		List<PvmTransition> outTrans=(List<PvmTransition>) map.get("outTrans");
		//这一段原来是没有的 st
		List<Map<String, Object>> items = (List<Map<String, Object>>)map.get("items");
		for(Iterator<PvmTransition> it=outTrans.iterator();it.hasNext();) {
			PvmTransition transition = it.next();
			PvmActivity activity = transition.getDestination();
			for (Iterator<Map<String, Object>> item = items.iterator(); item.hasNext();) {
				Map<String, Object> next = item.next();
				for (Iterator<String> keys =next.keySet().iterator();keys.hasNext();) {
					String key = keys.next();
					if (activity.getId().equals(key)){
						activity.getIncomingTransitions().add((PvmTransition) next.get(key));
					}
				}
			}
		}
		// ed
		curAct.getOutgoingTransitions().clear();
		curAct.getOutgoingTransitions().addAll(outTrans);
	}

}
