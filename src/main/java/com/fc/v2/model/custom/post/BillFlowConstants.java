package com.fc.v2.model.custom.post;

/**
 * 查处核签单四开口径常量。
 * 件的推进只认开次：前一开不放行，后一开不接件。
 *
 * @author fuce
 * @date 2026-09-29
 */
public final class BillFlowConstants {

    private BillFlowConstants() {
    }

    /** 开次：受案 */
    public static final int NODE_RECEIVE = 0;
    /** 开次：核查 */
    public static final int NODE_VERIFY = 1;
    /** 开次：并签 */
    public static final int NODE_COSIGN = 2;
    /** 开次：签发 */
    public static final int NODE_ISSUE = 3;

    /** 名目角色：受案登记 */
    public static final int ROLE_RECEIVE = 0;
    /** 名目角色：核查 */
    public static final int ROLE_VERIFY = 1;
    /** 名目角色：程序并签（盯程序走没走全） */
    public static final int ROLE_PROGRAM = 2;
    /** 名目角色：裁量并签（盯裁量幅度合不合当季那一格） */
    public static final int ROLE_DISCRETION = 3;
    /** 名目角色：签发 */
    public static final int ROLE_ISSUE = 4;
    /** 名目角色：止步（不予立案/撤案） */
    public static final int ROLE_STOP = 5;
    /** 名目角色：省局退回 */
    public static final int ROLE_PROVINCE_RETURN = 6;

    public static final String ACTION_RECEIVE = "RECEIVE";
    public static final String ACTION_VERIFY_PASS = "VERIFY_PASS";
    public static final String ACTION_COSIGN = "COSIGN";
    public static final String ACTION_SIGN_FINISH = "SIGN_FINISH";
    public static final String ACTION_NO_CASE = "NO_CASE";
    public static final String ACTION_WITHDRAW = "WITHDRAW";
    public static final String ACTION_PROVINCE_RETURN = "PROVINCE_RETURN";

    /** 该笔结论：放行 */
    public static final int RESULT_PASS = 0;
    /** 该笔结论：拦下 */
    public static final int RESULT_BLOCK = 1;
    /** 该笔结论：止步 */
    public static final int RESULT_STOP = 2;
    /** 该笔结论：退回 */
    public static final int RESULT_RETURN = 3;

    /** 并签办法：两名点齐 */
    public static final int MODE_AND = 1;
    /** 并签开应到人数 */
    public static final int COSIGN_NEED = 2;

    /** 报批情形：在核 */
    public static final int BILL_RUNNING = 0;
    /** 报批情形：已核讫 */
    public static final int BILL_PASSED = 1;
    /** 报批情形：已打回 */
    public static final int BILL_REJECTED = 2;

    /** 止步情形：不予立案 */
    public static final int END_NO_CASE = 0;
    /** 止步情形：撤案 */
    public static final int END_WITHDRAW = 1;
    /** 止步情形：核讫封住 */
    public static final int END_SEALED = 2;

    /** 省局名录：未报 */
    public static final int PROVINCE_NONE = 0;
    /** 省局名录：已入名录 */
    public static final int PROVINCE_SYNCED = 1;
    /** 省局名录：省局退回 */
    public static final int PROVINCE_RETURNED = 2;

    /** 签迹凭据：有效 */
    public static final int VALID = 1;
    /** 签迹凭据：省局退回后作废沉底 */
    public static final int SUNK = 0;
}
