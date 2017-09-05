package com.weituitu.motan;

/**
 * @描述:
 * @作者:liuguozhu
 * @创建:2017/8/28-下午8:08
 * @版本:v1.0
 */

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class BraveContextAware implements ApplicationContextAware {


    private static ApplicationContext applicationContext = null;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(final ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

}

