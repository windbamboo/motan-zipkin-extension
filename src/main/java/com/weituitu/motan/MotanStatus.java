package com.weituitu.motan;

/**
 * @描述:
 * @作者:liuguozhu
 * @创建:2017/8/28-下午8:02
 * @版本:v1.0
 */
public enum MotanStatus {

    /**
     * 成功
     */
    success,

    /**
     * 失败
     */
    fail,

    /**
     * 异常
     */
    exception;

    public static MotanStatus parse(Object s) {
        try {
            if (s == null) {
                return fail;
            }
            return MotanStatus.valueOf(s.toString());
        } catch (Exception e) {
        }
        return fail;
    }
}
