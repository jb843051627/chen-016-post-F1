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
 * 并签签迹对象 t_post_punish_sign
 *
 * <p>两名并排落名各记各的，掐同一分钟也算两笔；件已签数只许顺着自己的签迹一段段数出来。
 * 替人落名（on_behalf=1）不论写什么都不作数；省局打回后 valid_flag=0 沉底。</p>
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_post_punish_sign")
@ApiModel(value = "TPostPunishSign", description = "并签签迹")
public class TPostPunishSign implements Serializable {
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

    /** 所属并签开明细行 */
    @TableField("node_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "所属并签开明细行")
    private Long nodeId;

    /** 这笔账盯什么 1程序走没走全 2裁量幅度合不合当季那一格 */
    @TableField("sign_role")
    @ApiModelProperty(value = "这笔账盯什么 1程序 2裁量")
    private Integer signRole;

    /** 落名人 */
    @TableField("signer_name")
    @ApiModelProperty(value = "落名人")
    private String signerName;

    /** 是否替人落名 0本人 1代签（代签不计入点算） */
    @TableField("on_behalf")
    @ApiModelProperty(value = "是否替人落名 0本人 1代签")
    private Integer onBehalf;

    /** 该签迹是否作数 1作数 0作废（随打回沉底） */
    @TableField("valid_flag")
    @ApiModelProperty(value = "该签迹是否作数")
    private Integer validFlag;

    /** 签时轮次 */
    @TableField("round_no")
    @ApiModelProperty(value = "签时轮次")
    private Integer roundNo;

    /** 落名时刻 */
    @TableField("sign_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "落名时刻")
    private Date signTime;

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

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getSignRole() {
        return signRole;
    }

    public void setSignRole(Integer signRole) {
        this.signRole = signRole;
    }

    public String getSignerName() {
        return signerName;
    }

    public void setSignerName(String signerName) {
        this.signerName = signerName;
    }

    public Integer getOnBehalf() {
        return onBehalf;
    }

    public void setOnBehalf(Integer onBehalf) {
        this.onBehalf = onBehalf;
    }

    public Integer getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(Integer validFlag) {
        this.validFlag = validFlag;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Date getSignTime() {
        return signTime;
    }

    public void setSignTime(Date signTime) {
        this.signTime = signTime;
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
}
