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
 * 违法行为查处逐级核签单对象 t_post_punish_bill
 *
 * <p>四开台账：0受案 1核查 2并签 3签发。件的推进只认开次，抢步不算过成。</p>
 *
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_post_punish_bill")
@ApiModel(value = "TPostPunishBill", description = "违法行为查处逐级核签单")
public class TPostPunishBill implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 查处核签单号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "查处核签单号")
    private String billNo;

    /** 当前所在开次 0..3（0受案 1核查 2并签 3签发） */
    @TableField("node_no")
    @ApiModelProperty(value = "当前所在开次 0..3（0受案 1核查 2并签 3签发）")
    private Integer nodeNo;

    /** 同关并签办法 0任一人 1两名点齐 */
    @TableField("sign_mode")
    @ApiModelProperty(value = "同关并签办法 0任一人 1两名点齐")
    private Integer signMode;

    /** 本开应签人数（由签迹点算口径带出，不设手填） */
    @TableField("need_count")
    @ApiModelProperty(value = "本开应签人数")
    private Integer needCount;

    /** 本开已签人数（与给人过目计数同一次点算回写） */
    @TableField("sign_count")
    @ApiModelProperty(value = "本开已签人数")
    private Integer signCount;

    /** 企业字号（在册底档字号） */
    @TableField("site_no")
    @ApiModelProperty(value = "企业字号（在册底档字号）")
    private String siteNo;

    /** 企业全称（受案时自底档带住） */
    @TableField("site_name")
    @ApiModelProperty(value = "企业全称")
    private String siteName;

    /** 涉案网点（末端网点备案建档卡号） */
    @TableField("outlet_no")
    @ApiModelProperty(value = "涉案网点（末端网点备案建档卡号）")
    private String outletNo;

    /** 涉案网点地址 */
    @TableField("outlet_addr")
    @ApiModelProperty(value = "涉案网点地址")
    private String outletAddr;

    /** 事由 */
    @TableField("case_reason")
    @ApiModelProperty(value = "事由")
    private String caseReason;

    /** 来源 */
    @TableField("case_source")
    @ApiModelProperty(value = "来源")
    private String caseSource;

    /** 在册底档主键（只认字号对底档，不认申请人姓名） */
    @TableField("site_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "在册底档主键")
    private Long siteId;

    /** 申请人姓名（仅留痕，不作核对凭据） */
    @TableField("applicant_name")
    @ApiModelProperty(value = "申请人姓名（仅留痕，不作核对凭据）")
    private String applicantName;

    /** 本件裁量所依当季判据线代号 */
    @TableField("rule_code")
    @ApiModelProperty(value = "本件裁量所依当季判据线代号")
    private String ruleCode;

    /** 裁量幅度所定格次（当季那一格） */
    @TableField("grade_level")
    @ApiModelProperty(value = "裁量幅度所定格次")
    private Integer gradeLevel;

    /** 压件未结整改事务卡号（卡着则件停在核查） */
    @TableField("blocked_by")
    @ApiModelProperty(value = "压件未结整改事务卡号")
    private String blockedBy;

    /** 核查开现场材料数值（当季判据定格输入，留作倒计复核） */
    @TableField("verify_input")
    @ApiModelProperty(value = "核查开现场材料数值")
    private java.math.BigDecimal verifyInput;

    /** 止步名目 1不予立案 2撤案（在哪开写下便止在哪开） */
    @TableField("stop_kind")
    @ApiModelProperty(value = "止步名目 1不予立案 2撤案")
    private Integer stopKind;

    /** 章是否盖到底 0未盖 1已盖（各栏当即封死） */
    @TableField("sealed")
    @ApiModelProperty(value = "章是否盖到底 0未盖 1已盖")
    private Integer sealed;

    /** 省局打回后重新累的轮次（沉底名目不与后补并列） */
    @TableField("bounced_round")
    @ApiModelProperty(value = "省局打回后重新累的轮次")
    private Integer bouncedRound;

    /** 报批情形 0在核 1已核讫 2已打回 */
    @TableField("status")
    @ApiModelProperty(value = "报批情形 0在核 1已核讫 2已打回")
    private Integer status;

    /** 责令改正期限（对外确认单同件带出） */
    @TableField("rectify_deadline")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "责令改正期限")
    private Date rectifyDeadline;

    /** 救济途径（对外确认单同件带出） */
    @TableField("relief_way")
    @ApiModelProperty(value = "救济途径")
    private String reliefWay;

    /** 已开出去的整改确认单号（同件存根） */
    @TableField("out_confirm_no")
    @ApiModelProperty(value = "已开出去的整改确认单号")
    private String outConfirmNo;

    /** 同一企业当年第几件（第二件起当场点名） */
    @TableField("year_no")
    @ApiModelProperty(value = "同一企业当年第几件")
    private Integer yearNo;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
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

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getSignMode() {
        return signMode;
    }

    public void setSignMode(Integer signMode) {
        this.signMode = signMode;
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

    public String getCaseSource() {
        return caseSource;
    }

    public void setCaseSource(String caseSource) {
        this.caseSource = caseSource;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public Integer getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(Integer gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(String blockedBy) {
        this.blockedBy = blockedBy;
    }

    public java.math.BigDecimal getVerifyInput() {
        return verifyInput;
    }

    public void setVerifyInput(java.math.BigDecimal verifyInput) {
        this.verifyInput = verifyInput;
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

    public Integer getBouncedRound() {
        return bouncedRound;
    }

    public void setBouncedRound(Integer bouncedRound) {
        this.bouncedRound = bouncedRound;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getOutConfirmNo() {
        return outConfirmNo;
    }

    public void setOutConfirmNo(String outConfirmNo) {
        this.outConfirmNo = outConfirmNo;
    }

    public Integer getYearNo() {
        return yearNo;
    }

    public void setYearNo(Integer yearNo) {
        this.yearNo = yearNo;
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
