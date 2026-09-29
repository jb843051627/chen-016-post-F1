package com.fc.v2.model.custom.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * 签迹一笔：倒序复核时每一开过口落过的名目。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ApiModel(value = "BillActionView", description = "签迹一笔")
public class BillActionView {

    @ApiModelProperty(value = "签迹主键")
    private Long id;

    @ApiModelProperty(value = "轮次")
    private Integer roundNo;

    @ApiModelProperty(value = "开次 0受案 1核查 2并签 3签发")
    private Integer nodeNo;

    @ApiModelProperty(value = "开次名")
    private String nodeName;

    @ApiModelProperty(value = "名目角色")
    private Integer roleKind;

    @ApiModelProperty(value = "角色名")
    private String roleName;

    @ApiModelProperty(value = "落名人")
    private String signer;

    @ApiModelProperty(value = "动作名目")
    private String actionCode;

    @ApiModelProperty(value = "该笔结论")
    private Integer resultCode;

    @ApiModelProperty(value = "落名意见")
    private String opinion;

    @ApiModelProperty(value = "凭据情形 1有效 0作废沉底")
    private Integer validFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "落名时刻")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

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

    public Integer getRoleKind() {
        return roleKind;
    }

    public void setRoleKind(Integer roleKind) {
        this.roleKind = roleKind;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public Integer getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(Integer validFlag) {
        this.validFlag = validFlag;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
