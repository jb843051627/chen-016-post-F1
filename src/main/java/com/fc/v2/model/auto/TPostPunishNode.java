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
 * 核签单四开明细分账对象 t_post_punish_node
 *
 * <p>每过一开落一行；省局打回后旧轮次各行 valid_flag=0 作废沉底，从核查重新累，作废与后补不并列。</p>
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_post_punish_node")
@ApiModel(value = "TPostPunishNode", description = "核签单四开明细分账")
public class TPostPunishNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 核签单主键 */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "核签单主键")
    private Long billId;

    /** 核签单号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "核签单号")
    private String billNo;

    /** 开次 0受案 1核查 2并签 3签发 */
    @TableField("node_no")
    @ApiModelProperty(value = "开次 0受案 1核查 2并签 3签发")
    private Integer nodeNo;

    /** 轮次（首趟0；省局打回重走累加） */
    @TableField("round_no")
    @ApiModelProperty(value = "轮次")
    private Integer roundNo;

    /** 本行名目是否作数 1现行 0已作废沉底 */
    @TableField("valid_flag")
    @ApiModelProperty(value = "本行名目是否作数 1现行 0已作废沉底")
    private Integer validFlag;

    /** 本开落名（受案/核查/签发各一记） */
    @TableField("actor_name")
    @ApiModelProperty(value = "本开落名")
    private String actorName;

    /** 并签两笔账分工 1盯程序 2盯裁量（仅并签开） */
    @TableField("sign_role")
    @ApiModelProperty(value = "并签两笔账分工 1盯程序 2盯裁量")
    private Integer signRole;

    /** 本开记事/意见 */
    @TableField("opinion")
    @ApiModelProperty(value = "本开记事/意见")
    private String opinion;

    /** 本开写下的止步名目 1不予立案 2撤案 */
    @TableField("stop_kind")
    @ApiModelProperty(value = "本开写下的止步名目")
    private Integer stopKind;

    /** 本开是否随签发封死 */
    @TableField("sealed")
    @ApiModelProperty(value = "本开是否随签发封死")
    private Integer sealed;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

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

    public Integer getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(Integer validFlag) {
        this.validFlag = validFlag;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public Integer getSignRole() {
        return signRole;
    }

    public void setSignRole(Integer signRole) {
        this.signRole = signRole;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public Integer getStopKind() {
        return stopKind;
    }

    public void setStopKind(Integer stopKind) {
        this.stopKind = stopKind;
    }

    public Integer getSealed() {
        return sealed;
    }

    public void setSealed(Integer sealed) {
        this.sealed = sealed;
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
}
