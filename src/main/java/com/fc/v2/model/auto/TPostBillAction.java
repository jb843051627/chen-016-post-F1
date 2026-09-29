package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 核签单四开签迹凭据对象 t_post_bill_action
 * 每一开过口落一笔名目；并签开两名并排各记各的，同一分钟也算两笔。
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_post_bill_action")
@ApiModel(value = "TPostBillAction", description = "核签单四开签迹凭据")
public class TPostBillAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 核签单主键 */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "核签单主键")
    private Long billId;

    /** 落名时所属轮次 */
    @TableField("round_no")
    @ApiModelProperty(value = "轮次")
    private Integer roundNo;

    /** 开次 0受案 1核查 2并签 3签发 */
    @TableField("node_no")
    @ApiModelProperty(value = "开次 0受案 1核查 2并签 3签发")
    private Integer nodeNo;

    /** 名目角色 0受案登记 1核查 2程序并签 3裁量并签 4签发 5止步 6省局退回 */
    @TableField("role_kind")
    @ApiModelProperty(value = "名目角色 0受案登记 1核查 2程序并签 3裁量并签 4签发 5止步 6省局退回")
    private Integer roleKind;

    /** 落名人（取自登录会话，替人落的不作数） */
    @TableField("signer")
    @ApiModelProperty(value = "落名人")
    private String signer;

    /** 动作名目 RECEIVE/VERIFY_PASS/COSIGN/SIGN_FINISH/NO_CASE/WITHDRAW/PROVINCE_RETURN */
    @TableField("action_code")
    @ApiModelProperty(value = "动作名目")
    private String actionCode;

    /** 该笔结论 0放行 1拦下 2止步 3退回 */
    @TableField("result_code")
    @ApiModelProperty(value = "该笔结论 0放行 1拦下 2止步 3退回")
    private Integer resultCode;

    /** 落名意见/裁量依据 */
    @TableField("opinion")
    @ApiModelProperty(value = "落名意见")
    private String opinion;

    /** 凭据情形 1有效 0省局退回后作废沉底 */
    @TableField("valid_flag")
    @ApiModelProperty(value = "凭据情形 1有效 0作废沉底")
    private Integer validFlag;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Integer getRoleKind() {
        return roleKind;
    }

    public void setRoleKind(Integer roleKind) {
        this.roleKind = roleKind;
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

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
