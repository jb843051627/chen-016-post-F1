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
 * 对外整改确认单对象 t_post_rectify_notice
 * 末一开签发后一键由同一件带出，不另请人抄第二遍；一件只出一份。
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_post_rectify_notice")
@ApiModel(value = "TPostRectifyNotice", description = "对外整改确认单")
public class TPostRectifyNotice implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 整改确认单号 */
    @TableField("notice_no")
    @ApiModelProperty(value = "整改确认单号")
    private String noticeNo;

    /** 出处核签单主键 */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "出处核签单主键")
    private Long billId;

    /** 出处核签单号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "出处核签单号")
    private String billNo;

    /** 出处轮次（省局退回重走后再签发，出新一轮版本；旧版留底可倒查） */
    @TableField("round_no")
    @ApiModelProperty(value = "出处轮次")
    private Integer roundNo;

    /** 企业字号 */
    @TableField("site_no")
    @ApiModelProperty(value = "企业字号")
    private String siteNo;

    /** 企业全称 */
    @TableField("site_name")
    @ApiModelProperty(value = "企业全称")
    private String siteName;

    /** 涉案网点号 */
    @TableField("outlet_no")
    @ApiModelProperty(value = "涉案网点号")
    private String outletNo;

    /** 网点地址 */
    @TableField("outlet_addr")
    @ApiModelProperty(value = "网点地址")
    private String outletAddr;

    /** 事由 */
    @TableField("case_reason")
    @ApiModelProperty(value = "事由")
    private String caseReason;

    /** 当季裁量格 */
    @TableField("season_grade")
    @ApiModelProperty(value = "当季裁量格")
    private Integer seasonGrade;

    /** 责令改正期限 */
    @TableField("correct_deadline")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "责令改正期限")
    private Date correctDeadline;

    /** 救济途径 */
    @TableField("relief_channel")
    @ApiModelProperty(value = "救济途径")
    private String reliefChannel;

    /** 出具时刻（末一开签发同时） */
    @TableField("issue_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "出具时刻")
    private Date issueTime;

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

    public String getNoticeNo() {
        return noticeNo;
    }

    public void setNoticeNo(String noticeNo) {
        this.noticeNo = noticeNo;
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

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
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

    public Integer getSeasonGrade() {
        return seasonGrade;
    }

    public void setSeasonGrade(Integer seasonGrade) {
        this.seasonGrade = seasonGrade;
    }

    public Date getCorrectDeadline() {
        return correctDeadline;
    }

    public void setCorrectDeadline(Date correctDeadline) {
        this.correctDeadline = correctDeadline;
    }

    public String getReliefChannel() {
        return reliefChannel;
    }

    public void setReliefChannel(String reliefChannel) {
        this.reliefChannel = reliefChannel;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
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
