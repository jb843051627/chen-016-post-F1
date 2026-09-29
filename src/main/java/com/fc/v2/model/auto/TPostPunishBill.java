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
 * 四开：0受案 1核查 2并签 3签发；件的推进只认 node_no 开次。
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

    /** 当前所在开次 0受案 1核查 2并签 3签发 */
    @TableField("node_no")
    @ApiModelProperty(value = "当前所在开次 0受案 1核查 2并签 3签发")
    private Integer nodeNo;

    /** 同关并签办法 0任一人 1两名点齐 */
    @TableField("sign_mode")
    @ApiModelProperty(value = "同关并签办法 0任一人 1两名点齐")
    private Integer signMode;

    /** 本开应签人数（并签开恒为2，由并签办法点定，不接受手填） */
    @TableField("need_count")
    @ApiModelProperty(value = "本开应签人数")
    private Integer needCount;

    /** 本开已签人数（由有效签迹同一次点算回写） */
    @TableField("sign_count")
    @ApiModelProperty(value = "本开已签人数")
    private Integer signCount;

    /** 报批情形 0在核 1已核讫 2已打回 */
    @TableField("status")
    @ApiModelProperty(value = "报批情形 0在核 1已核讫 2已打回")
    private Integer status;

    /** 涉案企业字号（受案四要件之一，核验只认字号不认人） */
    @TableField("site_no")
    @ApiModelProperty(value = "涉案企业字号")
    private String siteNo;

    /** 企业全称（核查通过时自底档带入的快照） */
    @TableField("site_name")
    @ApiModelProperty(value = "企业全称快照")
    private String siteName;

    /** 涉案网点号（受案四要件之一） */
    @TableField("outlet_no")
    @ApiModelProperty(value = "涉案网点号")
    private String outletNo;

    /** 涉案网点地址（随确认单对外） */
    @TableField("outlet_addr")
    @ApiModelProperty(value = "涉案网点地址")
    private String outletAddr;

    /** 事由（受案四要件之一） */
    @TableField("case_reason")
    @ApiModelProperty(value = "事由")
    private String caseReason;

    /** 来源（受案四要件之一） */
    @TableField("source_channel")
    @ApiModelProperty(value = "来源")
    private String sourceChannel;

    /** 申请人姓名（撞得一个不差也不顶用，核验只认字号） */
    @TableField("applicant_name")
    @ApiModelProperty(value = "申请人姓名（核验不认）")
    private String applicantName;

    /** 本件裁量幅度折到的当季那一格 1..3（核查开落定） */
    @TableField("season_grade")
    @ApiModelProperty(value = "当季裁量格 1..3")
    private Integer seasonGrade;

    /** 责令改正期限（随确认单对外） */
    @TableField("correct_deadline")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "责令改正期限")
    private Date correctDeadline;

    /** 救济途径（随确认单对外） */
    @TableField("relief_channel")
    @ApiModelProperty(value = "救济途径")
    private String reliefChannel;

    /** 轮次：省局退回后先前名目沉底，从核查重新往上累一轮 */
    @TableField("round_no")
    @ApiModelProperty(value = "轮次")
    private Integer roundNo;

    /** 受案年度（当年第二件点名用） */
    @TableField("year_no")
    @ApiModelProperty(value = "受案年度")
    private Integer yearNo;

    /** 同企业同年度受案序（2 即第二件，当场点名） */
    @TableField("serial_in_year")
    @ApiModelProperty(value = "同企业同年度受案序")
    private Integer serialInYear;

    /** 止步/封口情形 0不予立案 1撤案 2核讫封住；NULL为在途 */
    @TableField("end_type")
    @ApiModelProperty(value = "止步/封口情形 0不予立案 1撤案 2核讫封住")
    private Integer endType;

    /** 封口标记 0未封 1章盖到底各栏封死 */
    @TableField("sealed")
    @ApiModelProperty(value = "封口标记 0未封 1已封")
    private Integer sealed;

    /** 省局名录情形 0未报 1已入名录 2省局退回 */
    @TableField("province_status")
    @ApiModelProperty(value = "省局名录情形 0未报 1已入名录 2省局退回")
    private Integer provinceStatus;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(String sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
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

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getYearNo() {
        return yearNo;
    }

    public void setYearNo(Integer yearNo) {
        this.yearNo = yearNo;
    }

    public Integer getSerialInYear() {
        return serialInYear;
    }

    public void setSerialInYear(Integer serialInYear) {
        this.serialInYear = serialInYear;
    }

    public Integer getEndType() {
        return endType;
    }

    public void setEndType(Integer endType) {
        this.endType = endType;
    }

    public Integer getSealed() {
        return sealed;
    }

    public void setSealed(Integer sealed) {
        this.sealed = sealed;
    }

    public Integer getProvinceStatus() {
        return provinceStatus;
    }

    public void setProvinceStatus(Integer provinceStatus) {
        this.provinceStatus = provinceStatus;
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
