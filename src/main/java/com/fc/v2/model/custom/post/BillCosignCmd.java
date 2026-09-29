package com.fc.v2.model.custom.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 并签开落名：只报自己盯的那一档角色，落名人由登录会话带入，
 * 不收手工计数、不收代签人。
 * roleKind：2 程序并签（盯程序走没走全）／3 裁量并签（盯裁量当季格）。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ApiModel(value = "BillCosignCmd", description = "并签开落名")
public class BillCosignCmd {

    @ApiModelProperty(value = "核签单主键", required = true)
    private Long id;

    @ApiModelProperty(value = "并签角色 2程序 3裁量", required = true)
    private Integer roleKind;

    @ApiModelProperty(value = "落名意见/裁量依据")
    private String opinion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRoleKind() {
        return roleKind;
    }

    public void setRoleKind(Integer roleKind) {
        this.roleKind = roleKind;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
