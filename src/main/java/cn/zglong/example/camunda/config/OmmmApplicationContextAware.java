package cn.zglong.example.camunda.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zglong
 * @version 1.0
 * @className OmmmApplicationContextAware
 * @description 获取上下文
 * @date 2021/12/9 23:02
 */
@Component
public class OmmmApplicationContextAware implements ApplicationContextAware {

    private static ApplicationContext applicationContex;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        OmmmApplicationContextAware.applicationContex = applicationContext;
    }

    public int getBeanCount() {
        return applicationContex.getBeanDefinitionCount();
    }

    public String[] getBeanNames() {
        return applicationContex.getBeanDefinitionNames();
    }
}