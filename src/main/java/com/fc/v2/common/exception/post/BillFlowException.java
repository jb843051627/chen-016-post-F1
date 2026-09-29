package com.fc.v2.common.exception.post;

/**
 * 核签单四开流转业务异常：开次不符、要件不齐、签迹不满等一律拦下。
 * 由全局 RuntimeException 出口转成 AjaxResult.error。
 *
 * @author fuce
 * @date 2026-09-29
 */
public class BillFlowException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BillFlowException(String message) {
        super(message);
    }
}
