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
 * 省局核签名录对象 t_post_province_roster
 * 签发盖到底同步一份；省局退回则名下移出（status=1）。
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_post_province_roster")
@ApiModel(value = "TPostProvinceRoster", description = "省局核签名录")
public class TPostProvinceRoster implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 查处核签单号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "查处核签单号")
    private String billNo;

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

    /** 涉案网点地址 */
    @TableField("outlet_addr")
    @ApiModelProperty(value = "涉案网点地址")
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

    /** 省局退回措辞 */
    @TableField("province_opinion")
    @ApiModelProperty(value = "省局退回措辞")
    private String provinceOpinion;

    /** 名录情形 0在名录 1已退回移出 */
    @TableField("status")
    @ApiModelProperty(value = "名录情形 0在名录 1已退回移出")
    private Integer status;

    /** 入名录时刻 */
    @TableField("sync_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "入名录时刻")
    private Date syncTime;

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

    public String getProvinceOpinion() {
        return provinceOpinion;
    }

    public void setProvinceOpinion(String provinceOpinion) {
        this.provinceOpinion = provinceOpinion;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(Date syncTime) {
        this.syncTime = syncTime;
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
