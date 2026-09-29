package com.fc.v2.model.custom.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 受案开入件：企业字号、涉案网点、事由与来源四要件，少一样进不了门。
 * 申请人姓名随件登记，但核查只认字号不认人。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ApiModel(value = "BillReceiveForm", description = "受案开入件四要件")
public class BillReceiveForm {

    @ApiModelProperty(value = "涉案企业字号（必填）", required = true)
    private String siteNo;

    @ApiModelProperty(value = "涉案网点号（必填）", required = true)
    private String outletNo;

    @ApiModelProperty(value = "事由（必填）", required = true)
    private String caseReason;

    @ApiModelProperty(value = "来源（必填）", required = true)
    private String sourceChannel;

    @ApiModelProperty(value = "申请人姓名（仅登记，核验不认）")
    private String applicantName;

    @ApiModelProperty(value = "备注")
    private String remark;

    public String getSiteNo() {
        return siteNo;
    }

    public void setSiteNo(String siteNo) {
        this.siteNo = siteNo;
    }

    public String getOutletNo() {
        return outletNo;
    }

    public void setOutletNo(String outletNo) {
        this.outletNo = outletNo;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
