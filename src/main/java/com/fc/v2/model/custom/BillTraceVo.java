package com.fc.v2.model.custom;

import java.util.ArrayList;
import java.util.List;

/**
 * 核签单倒计台账：从末一开倒着数回受案那一开，各级落过的名目一笔不差，
 * 数出来的结果与当日屏幕记载对得上号，且每一开都经得起复核。
 *
 * @author fuce
 * @date 2026-09-29
 */
public class BillTraceVo {

    /** 核签单主键 */
    private Long billId;

    /** 核签单号 */
    private String billNo;

    /** 当前轮次 */
    private Integer roundNo;

    /** 当前开次 */
    private Integer nodeNo;

    /** 是否已封死 */
    private Boolean sealed;

    /** 报批情形码 */
    private Integer status;

    /** 倒着数的四开明细（签发在前、受案在后）；沉底旧轮不并列在内 */
    private List<TraceNode> nodes = new ArrayList<TraceNode>();

    /** 并签点算（与明细同口径） */
    private SignCountVo signCount;

    /** 省局名录是否已同步 */
    private Boolean rosterSynced;

    /** 对外确认单与核签单同源核对（两边摆出的数一致才算过） */
    private Boolean confirmSameSource;

    /** 每一开是否都经得起复核 */
    private Boolean allRecheckPass;

    /** 复核不过的名目（一笔不差地列出） */
    private List<String> problems = new ArrayList<String>();

    public static class TraceNode {
        /** 开次 0..3 */
        private Integer nodeNo;
        /** 开次名目（受案/核查/并签/签发） */
        private String nodeName;
        /** 落名人 */
        private String actorName;
        /** 本开记事 */
        private String opinion;
        /** 止步名目 1不予立案 2撤案 */
        private Integer stopKind;
        /** 落名时刻（当日屏幕记载） */
        private String actionTime;
        /** 本开复核是否过 */
        private Boolean recheckPass;
        /** 本开复核说法 */
        private String recheckMsg;

        public Integer getNodeNo() {
            return nodeNo;
        }

        public void setNodeNo(Integer nodeNo) {
            this.nodeNo = nodeNo;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getActorName() {
            return actorName;
        }

        public void setActorName(String actorName) {
            this.actorName = actorName;
        }

        public String getOpinion() {
            return opinion;
        }

        public void setOpinion(String opinion) {
            this.opinion = opinion;
        }

        public Integer getStopKind() {
            return stopKind;
        }

        public void setStopKind(Integer stopKind) {
            this.stopKind = stopKind;
        }

        public String getActionTime() {
            return actionTime;
        }

        public void setActionTime(String actionTime) {
            this.actionTime = actionTime;
        }

        public Boolean getRecheckPass() {
            return recheckPass;
        }

        public void setRecheckPass(Boolean recheckPass) {
            this.recheckPass = recheckPass;
        }

        public String getRecheckMsg() {
            return recheckMsg;
        }

        public void setRecheckMsg(String recheckMsg) {
            this.recheckMsg = recheckMsg;
        }
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

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Boolean getSealed() {
        return sealed;
    }

    public void setSealed(Boolean sealed) {
        this.sealed = sealed;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<TraceNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TraceNode> nodes) {
        this.nodes = nodes;
    }

    public SignCountVo getSignCount() {
        return signCount;
    }

    public void setSignCount(SignCountVo signCount) {
        this.signCount = signCount;
    }

    public Boolean getRosterSynced() {
        return rosterSynced;
    }

    public void setRosterSynced(Boolean rosterSynced) {
        this.rosterSynced = rosterSynced;
    }

    public Boolean getConfirmSameSource() {
        return confirmSameSource;
    }

    public void setConfirmSameSource(Boolean confirmSameSource) {
        this.confirmSameSource = confirmSameSource;
    }

    public Boolean getAllRecheckPass() {
        return allRecheckPass;
    }

    public void setAllRecheckPass(Boolean allRecheckPass) {
        this.allRecheckPass = allRecheckPass;
    }

    public List<String> getProblems() {
        return problems;
    }

    public void setProblems(List<String> problems) {
        this.problems = problems;
    }
}
