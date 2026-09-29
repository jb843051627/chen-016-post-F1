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
 * 对外整改确认单存根对象 t_post_rectify_confirm
 *
 * <p>字号/网点地址/事由/责令改正期限/救济途径全从同一件核签单带出，不另请一人抄第二遍；
 * 两边摆出的数由同一件给回。</p>
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_post_rectify_confirm")
@ApiModel(value = "TPostRectifyConfirm", description = "对外整改确认单存根")
public class TPostRectifyConfirm implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 整改确认单号 */
    @TableField("confirm_no")
    @ApiModelProperty(value = "整改确认单号")
    private String confirmNo;

    /** 来源核签单主键 */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "来源核签单主键")
    private Long billId;

    /** 来源核签单号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "来源核签单号")
    private String billNo;

    /** 企业字号（同源） */
    @TableField("site_no")
    @ApiModelProperty(value = "企业字号（同源）")
    private String siteNo;

    /** 企业全称（同源） */
    @TableField("site_name")
    @ApiModelProperty(value = "企业全称（同源）")
    private String siteName;

    /** 涉案网点（同源） */
    @TableField("outlet_no")
    @ApiModelProperty(value = "涉案网点（同源）")
    private String outletNo;

    /** 网点地址（同源） */
    @TableField("outlet_addr")
    @ApiModelProperty(value = "网点地址（同源）")
    private String outletAddr;

    /** 事由（同源） */
    @TableField("case_reason")
    @ApiModelProperty(value = "事由（同源）")
    private String caseReason;

    /** 责令改正期限（同源） */
    @TableField("rectify_deadline")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "责令改正期限（同源）")
    private Date rectifyDeadline;

    /** 救济途径（同源） */
    @TableField("relief_way")
    @ApiModelProperty(value = "救济途径（同源）")
    private String reliefWay;

    /** 开具时刻（末一开发出） */
    @TableField("issue_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "开具时刻")
    private Date issueTime;

    /** 所属轮次（省局打回后旧版沉底，只认当前轮） */
    @TableField("round_no")
    @ApiModelProperty(value = "所属轮次")
    private Integer roundNo;

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

    public String getConfirmNo() {
        return confirmNo;
    }

    public void setConfirmNo(String confirmNo) {
        this.confirmNo = confirmNo;
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

    public String getSiteNo() {
        return siteNo;
    }

    public void setSiteNo(String siteNo) {
        this.siteNo = siteNo;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getOutletNo() {
        return outletNo;
    }

    public void setOutletNo(String outletNo) {
        this.outletNo = outletNo;
    }

    public String getOutletAddr() {
        return outletAddr;
    }

    public void setOutletAddr(String outletAddr) {
        this.outletAddr = outletAddr;
    }

    public String getCaseReason() {
        return caseReason;
    }

    public void setCaseReason(String caseReason) {
        this.caseReason = caseReason;
    }

    public Date getRectifyDeadline() {
        return rectifyDeadline;
    }

    public void setRectifyDeadline(Date rectifyDeadline) {
        this.rectifyDeadline = rectifyDeadline;
    }

    public String getReliefWay() {
        return reliefWay;
    }

    public void setReliefWay(String reliefWay) {
        this.reliefWay = reliefWay;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
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
