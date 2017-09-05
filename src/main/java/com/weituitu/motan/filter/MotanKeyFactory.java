package com.weituitu.motan.filter;

import brave.propagation.Propagation;

/**
 * @描述:
 * @作者:liuguozhu
 * @创建:2017/9/1-下午3:01
 * @版本:v1.0
 */
enum MotanKeyFactory implements Propagation.KeyFactory<String> {
    INSTANCE;


    public String create(String name) {
        return name;
    }
}
