package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.exception.post.BillFlowException;
import com.fc.v2.mapper.auto.TPostBillActionMapper;
import com.fc.v2.mapper.auto.TPostCompanyArchMapper;
import com.fc.v2.mapper.auto.TPostProvinceRosterMapper;
import com.fc.v2.mapper.auto.TPostPunishBillMapper;
import com.fc.v2.mapper.auto.TPostQualityRuleMapper;
import com.fc.v2.mapper.auto.TPostRectifyCardMapper;
import com.fc.v2.mapper.auto.TPostRectifyNoticeMapper;
import com.fc.v2.model.auto.TPostBillAction;
import com.fc.v2.model.auto.TPostCompanyArch;
import com.fc.v2.model.auto.TPostProvinceRoster;
import com.fc.v2.model.auto.TPostPunishBill;
import com.fc.v2.model.auto.TPostQualityRule;
import com.fc.v2.model.auto.TPostRectifyCard;
import com.fc.v2.model.auto.TPostRectifyNotice;
import com.fc.v2.model.custom.post.BillActionView;
import com.fc.v2.model.custom.post.BillCosignCmd;
import com.fc.v2.model.custom.post.BillFlowConstants;
import com.fc.v2.model.custom.post.BillNodeView;
import com.fc.v2.model.custom.post.BillReceiveForm;
import com.fc.v2.model.custom.post.BillTraceVo;
import com.fc.v2.model.custom.post.BillVerifyCmd;
import com.fc.v2.model.custom.post.CosignCountVo;
import com.fc.v2.service.ITPostPunishBillService;
import com.fc.v2.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 违法行为查处逐级核签单 Service实现（approval-chain 形状：四开多签）。
 *
 * 规矩：
 * 1) 件的推进只认 node_no 开次，前一开不放行后一开不接件，抢步一律 BillFlowException 拦下；
 * 2) 并签开计数只由有效签迹点算，页面与库里同一次点算给回，不设手工填数；
 * 3) 止步（不予立案/撤案）与封住（核讫盖章）后各栏再不能补笔；
 * 4) 省局退回：旧轮签迹作废沉底（valid_flag=0），从核查开重新累一轮，作废与新账不并列；
 * 5) 确认单与省局名录均由同一件头一键带出，五栏摆出的数必须一致。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Service
public class TPostPunishBillServiceImpl implements ITPostPunishBillService {

    /** 各开应到名目角色：[0]受案登记、[1]核查、[2..3]程序+裁量并签、[4]签发 */
    private static final int[][] REQUIRED_ROLES = {
            {BillFlowConstants.ROLE_RECEIVE},
            {BillFlowConstants.ROLE_VERIFY},
            {BillFlowConstants.ROLE_PROGRAM, BillFlowConstants.ROLE_DISCRETION},
            {BillFlowConstants.ROLE_ISSUE}
    };
    private static final String[] NODE_NAMES = {"受案", "核查", "并签", "签发"};
    private static final String[] ROLE_NAMES = {"受案登记", "核查", "程序并签", "裁量并签", "签发", "止步", "省局退回"};

    @Resource
    private TPostPunishBillMapper postPunishBillMapper;
    @Resource
    private TPostBillActionMapper billActionMapper;
    @Resource
    private TPostCompanyArchMapper companyArchMapper;
    @Resource
    private TPostRectifyCardMapper rectifyCardMapper;
    @Resource
    private TPostQualityRuleMapper qualityRuleMapper;
    @Resource
    private TPostProvinceRosterMapper provinceRosterMapper;
    @Resource
    private TPostRectifyNoticeMapper rectifyNoticeMapper;

    @Override
    public TPostPunishBill selectTPostPunishBillById(Long id) {
        return this.postPunishBillMapper.selectById(id);
    }

    @Override
    public List<TPostPunishBill> selectTPostPunishBillList(Wrapper<TPostPunishBill> queryWrapper) {
        return this.postPunishBillMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TPostPunishBill receive(BillReceiveForm form, String operator) {
        if (form == null) {
            throw new BillFlowException("受案入件不能为空");
        }
        requireText(form.getSiteNo(), "企业字号");
        requireText(form.getOutletNo(), "涉案网点");
        requireText(form.getCaseReason(), "事由");
        requireText(form.getSourceChannel(), "来源");
        String signer = requireOperator(operator);

        Date now = new Date();
        int year = yearOf(now);
        // 同企业同年度受案序：第二件由 serialInYear=2 当场点名。
        Integer exist = this.postPunishBillMapper.selectCount(new QueryWrapper<TPostPunishBill>()
                .eq("site_no", form.getSiteNo().trim())
                .eq("year_no", year)
                .eq("del_flag", 0));
        int serial = (exist == null ? 0 : exist) + 1;

        TPostPunishBill bill = new TPostPunishBill();
        bill.setBillNo("HC" + year + "-" + form.getSiteNo().trim() + "-" + pad3(serial));
        bill.setNodeNo(BillFlowConstants.NODE_VERIFY);
        bill.setStatus(BillFlowConstants.BILL_RUNNING);
        bill.setSiteNo(form.getSiteNo().trim());
        bill.setOutletNo(form.getOutletNo().trim());
        bill.setCaseReason(form.getCaseReason().trim());
        bill.setSourceChannel(form.getSourceChannel().trim());
        bill.setApplicantName(trimToNull(form.getApplicantName()));
        bill.setRoundNo(1);
        bill.setYearNo(year);
        bill.setSerialInYear(serial);
        bill.setSealed(0);
        bill.setProvinceStatus(BillFlowConstants.PROVINCE_NONE);
        bill.setDelFlag(0);
        bill.setRemark(trimToNull(form.getRemark()));
        this.postPunishBillMapper.insert(bill);

        writeAction(bill, BillFlowConstants.NODE_RECEIVE, BillFlowConstants.ROLE_RECEIVE,
                signer, BillFlowConstants.ACTION_RECEIVE, BillFlowConstants.RESULT_PASS,
                "四要件齐：字号/网点/事由/来源", now);
        return bill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TPostPunishBill verify(BillVerifyCmd cmd, String operator) {
        if (cmd == null || cmd.getId() == null) {
            throw new BillFlowException("核查入件不能为空");
        }
        TPostPunishBill bill = loadMutable(cmd.getId(), BillFlowConstants.NODE_VERIFY);
        String signer = requireOperator(operator);
        if (cmd.getSeasonGrade() == null || cmd.getSeasonGrade() < 1 || cmd.getSeasonGrade() > 3) {
            throw new BillFlowException("裁量幅度必须落进当季三格（1..3）");
        }
        requireText(cmd.getOutletAddr(), "网点地址");
        if (cmd.getCorrectDeadline() == null) {
            throw new BillFlowException("责令改正期限必填（随确认单对外）");
        }
        requireText(cmd.getReliefChannel(), "救济途径");

        // 只认字号不认人：在册底档按字号对看，申请人姓名撞得一个不差也不顶用。
        TPostCompanyArch arch = this.companyArchMapper.selectOne(new QueryWrapper<TPostCompanyArch>()
                .eq("site_no", bill.getSiteNo())
                .eq("del_flag", 0));
        if (arch == null) {
            throw new BillFlowException("字号对不上在册底档，核查不予放行");
        }
        if (arch.getStatus() != null && arch.getStatus() == 1) {
            throw new BillFlowException("字号在册底档已注销，核查不予放行");
        }
        // 当季须有现行判据线，裁量格才作得了数。
        Integer lineCount = this.qualityRuleMapper.selectCount(new QueryWrapper<TPostQualityRule>()
                .eq("status", 0)
                .eq("del_flag", 0)
                .le("eff_start", new Date())
                .and(w -> w.isNull("eff_end").or().gt("eff_end", new Date())));
        if (lineCount == null || lineCount == 0) {
            throw new BillFlowException("当季无现行裁量判据线，格次无法认定");
        }

        // 同字号同网点压着未了结（未销案）整改事务卡：件停在核查这一开，往后再齐也不走。
        Integer openCard = this.rectifyCardMapper.selectCount(new QueryWrapper<TPostRectifyCard>()
                .eq("site_no", bill.getSiteNo())
                .eq("outlet_no", bill.getOutletNo())
                .ne("status", 2)
                .eq("del_flag", 0));
        if (openCard != null && openCard > 0) {
            throw new BillFlowException("该网点尚有一张未了结的整改事务卡，件停在核查开");
        }

        bill.setSiteName(arch.getSiteName());
        bill.setSeasonGrade(cmd.getSeasonGrade());
        bill.setOutletAddr(cmd.getOutletAddr().trim());
        bill.setCorrectDeadline(cmd.getCorrectDeadline());
        bill.setReliefChannel(cmd.getReliefChannel().trim());
        bill.setSignMode(BillFlowConstants.MODE_AND);
        bill.setNeedCount(BillFlowConstants.COSIGN_NEED);
        bill.setSignCount(0);

        writeAction(bill, BillFlowConstants.NODE_VERIFY, BillFlowConstants.ROLE_VERIFY,
                signer, BillFlowConstants.ACTION_VERIFY_PASS, BillFlowConstants.RESULT_PASS,
                StringUtils.isNotEmpty(cmd.getOpinion()) ? cmd.getOpinion().trim()
                        : ("字号核验在册；裁量落第" + cmd.getSeasonGrade() + "格"),
                new Date());

        bill.setNodeNo(BillFlowConstants.NODE_COSIGN);
        this.postPunishBillMapper.updateById(bill);
        return bill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CosignCountVo cosign(BillCosignCmd cmd, String operator) {
        if (cmd == null || cmd.getId() == null) {
            throw new BillFlowException("并签入件不能为空");
        }
        if (cmd.getRoleKind() == null
                || (cmd.getRoleKind() != BillFlowConstants.ROLE_PROGRAM
                && cmd.getRoleKind() != BillFlowConstants.ROLE_DISCRETION)) {
            throw new BillFlowException("并签只认两档：2程序、3裁量");
        }
        TPostPunishBill bill = loadMutable(cmd.getId(), BillFlowConstants.NODE_COSIGN);
        String signer = requireOperator(operator);

        List<TPostBillAction> existing = listRoundNodeActions(bill, BillFlowConstants.NODE_COSIGN);
        for (TPostBillAction a : existing) {
            if (a.getRoleKind().equals(cmd.getRoleKind())) {
                throw new BillFlowException(cmd.getRoleKind() == BillFlowConstants.ROLE_PROGRAM
                        ? "程序一档已有人落名" : "裁量一档已有人落名");
            }
            if (signer.equals(a.getSigner())) {
                // 同人在并签开落两笔：不论写了什么都不作数。
                throw new BillFlowException("同一人不得在并签开连落两名");
            }
        }

        writeAction(bill, BillFlowConstants.NODE_COSIGN, cmd.getRoleKind(),
                signer, BillFlowConstants.ACTION_COSIGN, BillFlowConstants.RESULT_PASS,
                trimToNull(cmd.getOpinion()), new Date());

        // 同一次点算：回给页面的计数与回写库内 sign_count 都取自下面这一遍查询。
        CosignCountVo count = recountCosign(bill);
        bill.setSignCount(count.getSignCount());
        if (count.isFull()) {
            bill.setNodeNo(BillFlowConstants.NODE_ISSUE);
        }
        this.postPunishBillMapper.updateById(bill);
        return count;
    }

    @Override
    public CosignCountVo cosignCount(Long id) {
        if (id == null) {
            throw new BillFlowException("件号不能为空");
        }
        TPostPunishBill bill = this.postPunishBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            throw new BillFlowException("核签单不存在");
        }
        return recountCosign(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TPostPunishBill issue(Long id, String opinion, String operator) {
        TPostPunishBill bill = loadMutable(id, BillFlowConstants.NODE_ISSUE);
        // 不看件头记的数，顺着签迹重新点：两笔齐才盖得下去。
        CosignCountVo count = recountCosign(bill);
        if (!count.isFull()) {
            throw new BillFlowException("并签两笔未点齐（应到" + count.getNeedCount()
                    + "实到" + count.getSignCount() + "），见不着签发");
        }
        String signer = requireOperator(operator);

        Date now = new Date();
        writeAction(bill, BillFlowConstants.NODE_ISSUE, BillFlowConstants.ROLE_ISSUE,
                signer, BillFlowConstants.ACTION_SIGN_FINISH, BillFlowConstants.RESULT_PASS,
                trimToNull(opinion), now);

        bill.setStatus(BillFlowConstants.BILL_PASSED);
        bill.setEndType(BillFlowConstants.END_SEALED);
        bill.setSealed(1);
        bill.setProvinceStatus(BillFlowConstants.PROVINCE_SYNCED);
        this.postPunishBillMapper.updateById(bill);

        // 章盖到底同步一份到省局名录；对外整改确认单从同一件一键带出（同一事务）。
        syncRoster(bill, now);
        issueNotice(bill, now);
        return bill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TPostPunishBill stop(Long id, int endType, String opinion, String operator) {
        if (endType != BillFlowConstants.END_NO_CASE && endType != BillFlowConstants.END_WITHDRAW) {
            throw new BillFlowException("止步只认不予立案(0)或撤案(1)");
        }
        TPostPunishBill bill = loadAnyOpen(id);
        String signer = requireOperator(operator);
        String actionCode = endType == BillFlowConstants.END_NO_CASE
                ? BillFlowConstants.ACTION_NO_CASE : BillFlowConstants.ACTION_WITHDRAW;
        writeAction(bill, bill.getNodeNo(), BillFlowConstants.ROLE_STOP,
                signer, actionCode, BillFlowConstants.RESULT_STOP, trimToNull(opinion), new Date());

        // 件到此打住：end_type 落定即终态，事后各口守卫一律拦下，补不进一笔。
        bill.setEndType(endType);
        this.postPunishBillMapper.updateById(bill);
        return bill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TPostPunishBill provinceReturn(Long id, String provinceOpinion, String operator) {
        if (StringUtils.isEmpty(provinceOpinion)) {
            throw new BillFlowException("省局退回须带退回措辞");
        }
        if (id == null) {
            throw new BillFlowException("件号不能为空");
        }
        TPostPunishBill bill = this.postPunishBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            throw new BillFlowException("核签单不存在");
        }
        if (bill.getSealed() == null || bill.getSealed() != 1
                || bill.getEndType() == null || bill.getEndType() != BillFlowConstants.END_SEALED
                || bill.getProvinceStatus() == null
                || bill.getProvinceStatus() != BillFlowConstants.PROVINCE_SYNCED) {
            throw new BillFlowException("只有盖到底且已入省局名录的件才接得住退回");
        }
        String signer = requireOperator(operator);
        Date now = new Date();

        // 退回这一笔本身留作凭据；先前各开（受案/核查/并签/签发）落的名目一律作废沉底。
        writeAction(bill, bill.getNodeNo(), BillFlowConstants.ROLE_PROVINCE_RETURN,
                signer, BillFlowConstants.ACTION_PROVINCE_RETURN, BillFlowConstants.RESULT_RETURN,
                provinceOpinion.trim(), now);
        List<TPostBillAction> roundActions = this.billActionMapper.selectList(
                new QueryWrapper<TPostBillAction>()
                        .eq("bill_id", bill.getId())
                        .eq("round_no", bill.getRoundNo())
                        .eq("valid_flag", BillFlowConstants.VALID)
                        .ne("role_kind", BillFlowConstants.ROLE_PROVINCE_RETURN));
        for (TPostBillAction a : roundActions) {
            a.setValidFlag(BillFlowConstants.SUNK);
            this.billActionMapper.updateById(a);
        }

        // 名录名下移出。
        TPostProvinceRoster roster = this.provinceRosterMapper.selectOne(
                new QueryWrapper<TPostProvinceRoster>()
                        .eq("bill_no", bill.getBillNo())
                        .eq("del_flag", 0));
        if (roster != null) {
            roster.setStatus(1);
            roster.setProvinceOpinion(provinceOpinion.trim());
            this.provinceRosterMapper.updateById(roster);
        }

        // 从核查那一开重新往上累：轮次+1，件回核查门口；核查落定项清空，须重新对看重新落格。
        int newRound = (bill.getRoundNo() == null ? 1 : bill.getRoundNo()) + 1;
        bill.setRoundNo(newRound);
        bill.setNodeNo(BillFlowConstants.NODE_VERIFY);
        bill.setStatus(BillFlowConstants.BILL_RUNNING);
        bill.setSignMode(BillFlowConstants.MODE_AND);
        bill.setNeedCount(BillFlowConstants.COSIGN_NEED);
        bill.setSignCount(0);
        bill.setEndType(null);
        bill.setSealed(0);
        bill.setProvinceStatus(BillFlowConstants.PROVINCE_RETURNED);
        bill.setSiteName(null);
        bill.setSeasonGrade(null);
        bill.setOutletAddr(null);
        bill.setCorrectDeadline(null);
        bill.setReliefChannel(null);
        this.postPunishBillMapper.updateById(bill);
        return bill;
    }

    @Override
    public BillTraceVo trace(Long id) {
        if (id == null) {
            throw new BillFlowException("件号不能为空");
        }
        TPostPunishBill bill = this.postPunishBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            throw new BillFlowException("核签单不存在");
        }
        int round = bill.getRoundNo() == null ? 1 : bill.getRoundNo();

        List<TPostBillAction> all = this.billActionMapper.selectList(new QueryWrapper<TPostBillAction>()
                .eq("bill_id", bill.getId())
                .eq("del_flag", 0)
                .orderByDesc("create_time")
                .orderByDesc("id"));

        BillTraceVo vo = new BillTraceVo();
        vo.setBill(bill);
        vo.setRepeatInYear(bill.getSerialInYear() != null && bill.getSerialInYear() >= 2);

        // 每一轮各开走过的角色集合（含沉底签迹——沉底只是不作数于当前轮，倒查仍要见得着）。
        java.util.Map<Integer, Set<Integer>> rolesByRound = new java.util.HashMap<>();
        Set<Integer> returnRounds = new HashSet<>();
        List<BillActionView> currentValid = new ArrayList<>();
        List<BillActionView> sunk = new ArrayList<>();
        for (TPostBillAction a : all) {
            int r = a.getRoundNo() == null ? 1 : a.getRoundNo();
            if (a.getRoleKind() != null && a.getRoleKind() >= 0
                    && a.getRoleKind() <= BillFlowConstants.ROLE_ISSUE
                    && a.getNodeNo() != null) {
                rolesByRound.computeIfAbsent(r, k -> new HashSet<>()).add(a.getRoleKind());
            }
            if (a.getRoleKind() != null
                    && a.getRoleKind() == BillFlowConstants.ROLE_PROVINCE_RETURN) {
                returnRounds.add(r);
            }
            BillActionView v = toView(a);
            boolean currentRound = r == round;
            if (currentRound && a.getValidFlag() != null
                    && a.getValidFlag() == BillFlowConstants.VALID
                    && a.getRoleKind() != null
                    && a.getRoleKind() <= BillFlowConstants.ROLE_ISSUE) {
                currentValid.add(v);
            }
            if (a.getValidFlag() != null && a.getValidFlag() == BillFlowConstants.SUNK) {
                sunk.add(v);
            }
        }
        Collections.reverse(currentValid);
        vo.setSunkActions(sunk);

        // 当前轮从哪一开起累：首轮含受案；省局退回轮从核查重新累（受案沿前轮旧账）。
        int startNode = round <= 1 ? BillFlowConstants.NODE_RECEIVE : BillFlowConstants.NODE_VERIFY;
        List<BillNodeView> nodes = new ArrayList<>();
        Set<Integer> coveredNodes = new HashSet<>();
        for (int node = 0; node <= BillFlowConstants.NODE_ISSUE; node++) {
            BillNodeView nv = new BillNodeView();
            nv.setNodeNo(node);
            nv.setNodeName(NODE_NAMES[node]);
            nv.setNeedCount(REQUIRED_ROLES[node].length);
            Set<Integer> roles = new HashSet<>();
            if (node == BillFlowConstants.NODE_RECEIVE && round > 1) {
                // 受案沿旧轮：任一轮见过受案登记即算（它随旧轮沉底，不与新轮并列计数）。
                for (Set<Integer> oldRoles : rolesByRound.values()) {
                    if (oldRoles.contains(BillFlowConstants.ROLE_RECEIVE)) {
                        roles.add(BillFlowConstants.ROLE_RECEIVE);
                    }
                }
            } else if (node >= startNode) {
                roles.addAll(rolesByRound.getOrDefault(round, Collections.<Integer>emptySet()));
            }
            for (BillActionView v : currentValid) {
                if (v.getNodeNo() != null && v.getNodeNo() == node) {
                    nv.getActions().add(v);
                }
            }
            boolean passed = rolesContainsAll(roles, REQUIRED_ROLES[node]);
            final int currentNode = node;
            nv.setSignedCount((int) roles.stream().filter(r -> {
                for (int rr : REQUIRED_ROLES[currentNode]) {
                    if (r == rr) {
                        return true;
                    }
                }
                return false;
            }).count());
            nv.setPassed(passed);
            if (passed) {
                coveredNodes.add(node);
            }
            nodes.add(nv);
        }
        vo.setNodes(nodes);

        // 倒序轨迹：从末一开倒着数回受案——当前轮有效名目（倒序）在前，省局退回凭据垫后。
        List<BillActionView> reverse = new ArrayList<>();
        for (TPostBillAction a : all) {
            if (a.getValidFlag() != null && a.getValidFlag() == BillFlowConstants.VALID) {
                reverse.add(toView(a));
            }
        }
        vo.setReverseTrail(reverse);

        // 已发确认单：最新轮在前；按轮对号复核。
        List<TPostRectifyNotice> notices = this.rectifyNoticeMapper.selectList(
                new QueryWrapper<TPostRectifyNotice>()
                        .eq("bill_id", bill.getId())
                        .eq("del_flag", 0)
                        .orderByDesc("round_no"));
        vo.setNotices(notices);

        vo.setVerified(verifyTrail(bill, round, startNode, rolesByRound, returnRounds,
                coveredNodes, notices));
        vo.setVerifyMessage(vo.isVerified() ? "四开名目一笔不差，与当日记载对得上号" : "四开名目或对外件数对不上号");
        return vo;
    }

    @Override
    public TPostRectifyNotice latestNotice(Long billId) {
        if (billId == null) {
            return null;
        }
        List<TPostRectifyNotice> notices = this.rectifyNoticeMapper.selectList(
                new QueryWrapper<TPostRectifyNotice>()
                        .eq("bill_id", billId)
                        .eq("del_flag", 0)
                        .orderByDesc("round_no")
                        .last("limit 1"));
        return notices.isEmpty() ? null : notices.get(0);
    }

    // ------------------------------------------------------------------
    // 内部规矩
    // ------------------------------------------------------------------

    private boolean verifyTrail(TPostPunishBill bill, int round, int startNode,
                                java.util.Map<Integer, Set<Integer>> rolesByRound,
                                Set<Integer> returnRounds,
                                Set<Integer> coveredNodes,
                                List<TPostRectifyNotice> notices) {
        int curNode = bill.getNodeNo() == null ? 0 : bill.getNodeNo();

        // 轮次链：第2轮往上必须接得住上一轮的省局退回凭据。
        for (int r = 2; r <= round; r++) {
            if (!returnRounds.contains(r - 1)) {
                return false;
            }
        }

        boolean sealed = bill.getSealed() != null && bill.getSealed() == 1
                && bill.getEndType() != null
                && bill.getEndType() == BillFlowConstants.END_SEALED;
        int mustCoverThrough;
        if (sealed) {
            // 封住件：四开（含末开）全数走满。
            if (curNode != BillFlowConstants.NODE_ISSUE) {
                return false;
            }
            mustCoverThrough = BillFlowConstants.NODE_ISSUE;
        } else {
            // 在途/止步件：当前开之前的每一开必须走满（止步当开不再要放行名目）。
            mustCoverThrough = curNode - 1;
        }
        for (int n = 0; n <= mustCoverThrough; n++) {
            if (!coveredNodes.contains(n)) {
                return false;
            }
        }

        // 当日那一屏的计数：封住件件头应到/实到须都是2。
        if (sealed) {
            if (bill.getNeedCount() == null || bill.getNeedCount() != BillFlowConstants.COSIGN_NEED
                    || bill.getSignCount() == null
                    || bill.getSignCount() != BillFlowConstants.COSIGN_NEED) {
                return false;
            }
            if (bill.getProvinceStatus() == null
                    || bill.getProvinceStatus() != BillFlowConstants.PROVINCE_SYNCED) {
                return false;
            }
        }

        // 确认单按轮对号：每一个封住过的轮次恰有一份，轮次不得跳号。
        java.util.TreeSet<Integer> noticeRounds = new java.util.TreeSet<>();
        for (TPostRectifyNotice n : notices) {
            if (n.getRoundNo() == null || n.getBillId() == null
                    || !n.getBillId().equals(bill.getId())) {
                return false;
            }
            // 跨轮不变的两栏：任何一轮的对外件都必须与同一件头一致。
            if (!eq(n.getSiteNo(), bill.getSiteNo())
                    || !eq(n.getCaseReason(), bill.getCaseReason())) {
                return false;
            }
            noticeRounds.add(n.getRoundNo());
        }
        int sealedRounds = sealed ? round : round - 1;
        if (sealedRounds > 0) {
            for (int r = 1; r <= sealedRounds; r++) {
                if (!noticeRounds.contains(r)) {
                    return false;
                }
            }
            if (noticeRounds.size() != sealedRounds) {
                return false;
            }
        }
        // 当前轮那份确认单的五栏须与件头摆出的数一致（同一件带出，不许抄花）。
        if (sealed) {
            TPostRectifyNotice current = null;
            for (TPostRectifyNotice n : notices) {
                if (n.getRoundNo() != null && n.getRoundNo() == round) {
                    current = n;
                }
            }
            if (current == null
                    || !eq(current.getOutletAddr(), bill.getOutletAddr())
                    || !eq(current.getCorrectDeadline(), bill.getCorrectDeadline())
                    || !eq(current.getReliefChannel(), bill.getReliefChannel())) {
                return false;
            }
        }
        return true;
    }

    private void syncRoster(TPostPunishBill bill, Date now) {
        TPostProvinceRoster roster = this.provinceRosterMapper.selectOne(
                new QueryWrapper<TPostProvinceRoster>()
                        .eq("bill_no", bill.getBillNo())
                        .eq("del_flag", 0));
        boolean isNew = roster == null;
        if (isNew) {
            roster = new TPostProvinceRoster();
            roster.setBillNo(bill.getBillNo());
            roster.setDelFlag(0);
        }
        roster.setSiteNo(bill.getSiteNo());
        roster.setSiteName(bill.getSiteName());
        roster.setOutletNo(bill.getOutletNo());
        roster.setOutletAddr(bill.getOutletAddr());
        roster.setCaseReason(bill.getCaseReason());
        roster.setSeasonGrade(bill.getSeasonGrade());
        roster.setCorrectDeadline(bill.getCorrectDeadline());
        roster.setStatus(0);
        roster.setProvinceOpinion(null);
        roster.setSyncTime(now);
        if (isNew) {
            this.provinceRosterMapper.insert(roster);
        } else {
            this.provinceRosterMapper.updateById(roster);
        }
    }

    private void issueNotice(TPostPunishBill bill, Date now) {
        // 一件一轮只出一份：重签同轮不另抄第二遍。
        TPostRectifyNotice exist = this.rectifyNoticeMapper.selectOne(
                new QueryWrapper<TPostRectifyNotice>()
                        .eq("bill_id", bill.getId())
                        .eq("round_no", bill.getRoundNo())
                        .eq("del_flag", 0));
        TPostRectifyNotice notice = exist;
        if (notice == null) {
            notice = new TPostRectifyNotice();
            notice.setBillId(bill.getId());
            notice.setRoundNo(bill.getRoundNo());
            notice.setNoticeNo("ZG" + bill.getBillNo() + "-R" + bill.getRoundNo());
            notice.setDelFlag(0);
            notice.setCreateTime(now);
        }
        // 五栏全部从同一件头带过去，不再另请一人抄第二遍。
        notice.setBillNo(bill.getBillNo());
        notice.setSiteNo(bill.getSiteNo());
        notice.setSiteName(bill.getSiteName());
        notice.setOutletNo(bill.getOutletNo());
        notice.setOutletAddr(bill.getOutletAddr());
        notice.setCaseReason(bill.getCaseReason());
        notice.setSeasonGrade(bill.getSeasonGrade());
        notice.setCorrectDeadline(bill.getCorrectDeadline());
        notice.setReliefChannel(bill.getReliefChannel());
        notice.setIssueTime(now);
        if (exist == null) {
            this.rectifyNoticeMapper.insert(notice);
        } else {
            this.rectifyNoticeMapper.updateById(notice);
        }
    }

    /**
     * 顺着件自己当前轮的签迹点一遍并签：应到2、程序/裁量各一笔。
     * 页面计数与库内回写共用本方法，两条数天然接得平。
     */
    private CosignCountVo recountCosign(TPostPunishBill bill) {
        List<TPostBillAction> actions = listRoundNodeActions(bill, BillFlowConstants.NODE_COSIGN);
        boolean program = false;
        boolean discretion = false;
        Set<String> signers = new HashSet<>();
        for (TPostBillAction a : actions) {
            signers.add(a.getSigner());
            if (a.getRoleKind() == BillFlowConstants.ROLE_PROGRAM) {
                program = true;
            } else if (a.getRoleKind() == BillFlowConstants.ROLE_DISCRETION) {
                discretion = true;
            }
        }
        // 两笔账：两个角色各一笔、两个不同的人；掐同一分钟也算两笔。
        int count = (program ? 1 : 0) + (discretion ? 1 : 0);
        if (program && discretion && signers.size() < BillFlowConstants.COSIGN_NEED) {
            count = 1;
        }
        return new CosignCountVo(bill.getId(), bill.getRoundNo(),
                BillFlowConstants.COSIGN_NEED, count, program, discretion);
    }

    private List<TPostBillAction> listRoundNodeActions(TPostPunishBill bill, int node) {
        int round = bill.getRoundNo() == null ? 1 : bill.getRoundNo();
        return this.billActionMapper.selectList(new QueryWrapper<TPostBillAction>()
                .eq("bill_id", bill.getId())
                .eq("round_no", round)
                .eq("node_no", node)
                .eq("valid_flag", BillFlowConstants.VALID)
                .eq("del_flag", 0)
                .orderByAsc("id"));
    }

    private void writeAction(TPostPunishBill bill, int node, int role, String signer,
                             String actionCode, int result, String opinion, Date now) {
        TPostBillAction action = new TPostBillAction();
        action.setBillId(bill.getId());
        action.setRoundNo(bill.getRoundNo() == null ? 1 : bill.getRoundNo());
        action.setNodeNo(node);
        action.setRoleKind(role);
        action.setSigner(signer);
        action.setActionCode(actionCode);
        action.setResultCode(result);
        action.setOpinion(opinion);
        action.setValidFlag(BillFlowConstants.VALID);
        action.setDelFlag(0);
        action.setCreateTime(now);
        this.billActionMapper.insert(action);
    }

    /** 取出件并校验：未删、在途未封口、且正停在指定开次。抢步在这里被拦。 */
    private TPostPunishBill loadMutable(Long id, int expectNode) {
        TPostPunishBill bill = loadAnyOpen(id);
        int node = bill.getNodeNo() == null ? 0 : bill.getNodeNo();
        if (node != expectNode) {
            throw new BillFlowException("件当前停在「" + NODE_NAMES[node] + "」开，"
                    + NODE_NAMES[expectNode] + "开接不住这件");
        }
        return bill;
    }

    private TPostPunishBill loadAnyOpen(Long id) {
        if (id == null) {
            throw new BillFlowException("件号不能为空");
        }
        TPostPunishBill bill = this.postPunishBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            throw new BillFlowException("核签单不存在");
        }
        if (bill.getEndType() != null) {
            throw new BillFlowException("件已" + endTypeName(bill.getEndType()) + "打住，各栏不能再补一笔");
        }
        if (bill.getSealed() != null && bill.getSealed() == 1) {
            throw new BillFlowException("件已盖到底封住，想改只能另起一件");
        }
        return bill;
    }

    private String endTypeName(Integer endType) {
        if (endType == null) {
            return "";
        }
        if (endType == BillFlowConstants.END_NO_CASE) {
            return "不予立案";
        }
        if (endType == BillFlowConstants.END_WITHDRAW) {
            return "撤案";
        }
        if (endType == BillFlowConstants.END_SEALED) {
            return "核讫封住";
        }
        return "止步";
    }

    private BillActionView toView(TPostBillAction a) {
        BillActionView v = new BillActionView();
        v.setId(a.getId());
        v.setRoundNo(a.getRoundNo());
        v.setNodeNo(a.getNodeNo());
        v.setNodeName(a.getNodeNo() != null && a.getNodeNo() >= 0 && a.getNodeNo() < NODE_NAMES.length
                ? NODE_NAMES[a.getNodeNo()] : "");
        v.setRoleKind(a.getRoleKind());
        v.setRoleName(a.getRoleKind() != null && a.getRoleKind() >= 0 && a.getRoleKind() < ROLE_NAMES.length
                ? ROLE_NAMES[a.getRoleKind()] : "");
        v.setSigner(a.getSigner());
        v.setActionCode(a.getActionCode());
        v.setResultCode(a.getResultCode());
        v.setOpinion(a.getOpinion());
        v.setValidFlag(a.getValidFlag());
        v.setCreateTime(a.getCreateTime());
        return v;
    }

    private boolean rolesContainsAll(Set<Integer> roles, int[] required) {
        for (int r : required) {
            if (!roles.contains(r)) {
                return false;
            }
        }
        return true;
    }

    private void requireText(String value, String field) {
        if (StringUtils.isEmpty(value) || value.trim().isEmpty()) {
            throw new BillFlowException(field + "缺着，受案进不了门");
        }
    }

    private String requireOperator(String operator) {
        if (StringUtils.isEmpty(operator) || operator.trim().isEmpty()) {
            throw new BillFlowException("落名人取不到（登录会话为空），替人落的不作数");
        }
        return operator.trim();
    }

    private String trimToNull(String s) {
        return StringUtils.isEmpty(s) || s.trim().isEmpty() ? null : s.trim();
    }

    private int yearOf(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    private String pad3(int n) {
        return n < 10 ? "00" + n : n < 100 ? "0" + n : String.valueOf(n);
    }

    private boolean eq(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
