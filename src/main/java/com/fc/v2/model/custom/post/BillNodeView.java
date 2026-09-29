package com.fc.v2.model.custom.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 一开的复核视图：本开应到/实到名目、是否走满。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ApiModel(value = "BillNodeView", description = "一开复核视图")
public class BillNodeView {

    @ApiModelProperty(value = "开次 0受案 1核查 2并签 3签发")
    private Integer nodeNo;

    @ApiModelProperty(value = "开次名")
    private String nodeName;

    @ApiModelProperty(value = "本开应到名目数")
    private int needCount;

    @ApiModelProperty(value = "本开当前轮次有效名目数（点算签迹）")
    private int signedCount;

    @ApiModelProperty(value = "本开是否已走满")
    private boolean passed;

    @ApiModelProperty(value = "本开有效签迹")
    private List<BillActionView> actions = new ArrayList<>();

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getNeedCount() {
        return needCount;
    }

    public void setNeedCount(int needCount) {
        this.needCount = needCount;
    }

    public int getSignedCount() {
        return signedCount;
    }

    public void setSignedCount(int signedCount) {
        this.signedCount = signedCount;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<BillActionView> getActions() {
        return actions;
    }

    public void setActions(List<BillActionView> actions) {
        this.actions = actions;
    }
}
