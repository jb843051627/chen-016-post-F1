package com.fc.v2.model.custom.post;

import com.fc.v2.model.auto.TPostPunishBill;
import com.fc.v2.model.auto.TPostRectifyNotice;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 核签单全貌：件头 + 当前轮次四开视图 + 完整签迹（作废沉底仍可见但不并列计数）
 * + 倒序复核结果 + 已出具确认单。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ApiModel(value = "BillTraceVo", description = "核签单倒序复核全貌")
public class BillTraceVo {

    @ApiModelProperty(value = "件头")
    private TPostPunishBill bill;

    @ApiModelProperty(value = "当前轮次四开视图（开次正序）")
    private List<BillNodeView> nodes = new ArrayList<>();

    @ApiModelProperty(value = "全部有效签迹（签发→受案倒序）")
    private List<BillActionView> reverseTrail = new ArrayList<>();

    @ApiModelProperty(value = "作废沉底签迹（省局退回的旧轮次名目，不与有效名目并列）")
    private List<BillActionView> sunkActions = new ArrayList<>();

    @ApiModelProperty(value = "已出具的对外整改确认单（最新轮次在前）")
    private List<TPostRectifyNotice> notices = new ArrayList<>();

    @ApiModelProperty(value = "当年第二件点名：同企业同年第2件受案时为真")
    private boolean repeatInYear;

    @ApiModelProperty(value = "复核结论：四开名目一笔不差且与件头开次对得上号")
    private boolean verified;

    @ApiModelProperty(value = "复核意见")
    private String verifyMessage;

    public TPostPunishBill getBill() {
        return bill;
    }

    public void setBill(TPostPunishBill bill) {
        this.bill = bill;
    }

    public List<BillNodeView> getNodes() {
        return nodes;
    }

    public void setNodes(List<BillNodeView> nodes) {
        this.nodes = nodes;
    }

    public List<BillActionView> getReverseTrail() {
        return reverseTrail;
    }

    public void setReverseTrail(List<BillActionView> reverseTrail) {
        this.reverseTrail = reverseTrail;
    }

    public List<BillActionView> getSunkActions() {
        return sunkActions;
    }

    public void setSunkActions(List<BillActionView> sunkActions) {
        this.sunkActions = sunkActions;
    }

    public List<TPostRectifyNotice> getNotices() {
        return notices;
    }

    public void setNotices(List<TPostRectifyNotice> notices) {
        this.notices = notices;
    }

    public boolean isRepeatInYear() {
        return repeatInYear;
    }

    public void setRepeatInYear(boolean repeatInYear) {
        this.repeatInYear = repeatInYear;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerifyMessage() {
        return verifyMessage;
    }

    public void setVerifyMessage(String verifyMessage) {
        this.verifyMessage = verifyMessage;
    }
}
