package com.fc.v2.model.custom;

/**
 * 并签开点算结果：给人过目的计数与库里存的计数同一次点算给回，
 * 页面不设手工填数的格子，也没有代填的把手。
 *
 * @author fuce
 * @date 2026-09-29
 */
public class SignCountVo {

    /** 核签单主键 */
    private Long billId;

    /** 核签单号 */
    private String billNo;

    /** 当前开次 */
    private Integer nodeNo;

    /** 轮次 */
    private Integer roundNo;

    /** 本开应签笔数（两名点齐=2） */
    private Integer needCount;

    /** 顺着签迹点算出来的已签笔数（给人过目） */
    private Integer viewCount;

    /** 库里存着的已签笔数（与 viewCount 同一次点算回写） */
    private Integer dbCount;

    /** 两数是否接平（接不平就算这一开没满） */
    private Boolean consistent;

    /** 盯程序那笔是否已落 */
    private Boolean programSigned;

    /** 盯裁量那笔是否已落 */
    private Boolean discretionSigned;

    /** 盯程序落名人 */
    private String programSigner;

    /** 盯裁量落名人 */
    private String discretionSigner;

    /** 替人落名、不作数的笔数 */
    private Integer onBehalfCount;

    /** 两笔账是否齐（齐了才见得着签发） */
    private Boolean complete;

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getNeedCount() {
        return needCount;
    }

    public void setNeedCount(Integer needCount) {
        this.needCount = needCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getDbCount() {
        return dbCount;
    }

    public void setDbCount(Integer dbCount) {
        this.dbCount = dbCount;
    }

    public Boolean getConsistent() {
        return consistent;
    }

    public void setConsistent(Boolean consistent) {
        this.consistent = consistent;
    }

    public Boolean getProgramSigned() {
        return programSigned;
    }

    public void setProgramSigned(Boolean programSigned) {
        this.programSigned = programSigned;
    }

    public Boolean getDiscretionSigned() {
        return discretionSigned;
    }

    public void setDiscretionSigned(Boolean discretionSigned) {
        this.discretionSigned = discretionSigned;
    }

    public String getProgramSigner() {
        return programSigner;
    }

    public void setProgramSigner(String programSigner) {
        this.programSigner = programSigner;
    }

    public String getDiscretionSigner() {
        return discretionSigner;
    }

    public void setDiscretionSigner(String discretionSigner) {
        this.discretionSigner = discretionSigner;
    }

    public Integer getOnBehalfCount() {
        return onBehalfCount;
    }

    public void setOnBehalfCount(Integer onBehalfCount) {
        this.onBehalfCount = onBehalfCount;
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }
}
