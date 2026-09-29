package com.fc.v2.model.custom.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 并签开点算回数：页面计数与库内计数由同一次点算给回，
 * needCount 与 signCount 接不平即视为本开未满。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ApiModel(value = "CosignCountVo", description = "并签开点算回数")
public class CosignCountVo {

    @ApiModelProperty(value = "核签单主键")
    private Long billId;

    @ApiModelProperty(value = "当前轮次")
    private Integer roundNo;

    @ApiModelProperty(value = "本开应签人数（恒为2）")
    private Integer needCount;

    @ApiModelProperty(value = "本开已落名数（有效签迹点算）")
    private Integer signCount;

    @ApiModelProperty(value = "程序并签是否已落名")
    private boolean programSigned;

    @ApiModelProperty(value = "裁量并签是否已落名")
    private boolean discretionSigned;

    @ApiModelProperty(value = "本开是否已满（同一次点算得出）")
    private boolean full;

    public CosignCountVo() {
    }

    public CosignCountVo(Long billId, Integer roundNo, int needCount, int signCount,
                         boolean programSigned, boolean discretionSigned) {
        this.billId = billId;
        this.roundNo = roundNo;
        this.needCount = needCount;
        this.signCount = signCount;
        this.programSigned = programSigned;
        this.discretionSigned = discretionSigned;
        this.full = signCount == needCount;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
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

    public Integer getSignCount() {
        return signCount;
    }

    public void setSignCount(Integer signCount) {
        this.signCount = signCount;
    }

    public boolean isProgramSigned() {
        return programSigned;
    }

    public void setProgramSigned(boolean programSigned) {
        this.programSigned = programSigned;
    }

    public boolean isDiscretionSigned() {
        return discretionSigned;
    }

    public void setDiscretionSigned(boolean discretionSigned) {
        this.discretionSigned = discretionSigned;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }
}
