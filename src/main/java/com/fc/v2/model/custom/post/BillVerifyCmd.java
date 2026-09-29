package com.fc.v2.model.custom.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 核查开落定：在册底档与现场材料对着看后，带回归档快照、当季裁量格，
 * 以及随确认单对外的网点地址、责令改正期限、救济途径（均从同一件带出）。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ApiModel(value = "BillVerifyCmd", description = "核查开落定项")
public class BillVerifyCmd {

    @ApiModelProperty(value = "核签单主键", required = true)
    private Long id;

    @ApiModelProperty(value = "当季裁量格 1..3（必填）", required = true)
    private Integer seasonGrade;

    @ApiModelProperty(value = "涉案网点地址（必填，随确认单对外）", required = true)
    private String outletAddr;

    @ApiModelProperty(value = "责令改正期限 yyyy-MM-dd HH:mm:ss（必填）", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date correctDeadline;

    @ApiModelProperty(value = "救济途径（必填，随确认单对外）", required = true)
    private String reliefChannel;

    @ApiModelProperty(value = "核查意见")
    private String opinion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSeasonGrade() {
        return seasonGrade;
    }

    public void setSeasonGrade(Integer seasonGrade) {
        this.seasonGrade = seasonGrade;
    }

    public String getOutletAddr() {
        return outletAddr;
    }

    public void setOutletAddr(String outletAddr) {
        this.outletAddr = outletAddr;
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

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
