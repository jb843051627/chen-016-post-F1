package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TPostCompanyArchMapper;
import com.fc.v2.mapper.auto.TPostOutletCardMapper;
import com.fc.v2.mapper.auto.TPostPunishBillMapper;
import com.fc.v2.mapper.auto.TPostPunishNodeMapper;
import com.fc.v2.mapper.auto.TPostPunishSignMapper;
import com.fc.v2.mapper.auto.TPostProvinceRosterMapper;
import com.fc.v2.mapper.auto.TPostRectifyCardMapper;
import com.fc.v2.mapper.auto.TPostRectifyConfirmMapper;
import com.fc.v2.model.auto.TPostCompanyArch;
import com.fc.v2.model.auto.TPostOutletCard;
import com.fc.v2.model.auto.TPostPunishBill;
import com.fc.v2.model.auto.TPostPunishNode;
import com.fc.v2.model.auto.TPostPunishSign;
import com.fc.v2.model.auto.TPostProvinceRoster;
import com.fc.v2.model.auto.TPostRectifyCard;
import com.fc.v2.model.auto.TPostRectifyConfirm;
import com.fc.v2.model.custom.BillTraceVo;
import com.fc.v2.model.custom.OpResult;
import com.fc.v2.model.custom.SignCountVo;
import com.fc.v2.service.ITPostPunishBillService;
import com.fc.v2.service.ITPostQualityRuleService;
import com.fc.v2.shiro.util.ShiroUtils;
import com.fc.v2.util.SnowflakeIdWorker;
import com.fc.v2.util.StringUtils;

/**
 * 违法行为查处逐级核签单 Service业务层处理（approval-chain 形状：四开台账）
 *
 * <p>铁律：①推进只认开次，抢步不算过成；②并签已签数只许顺着签迹一段段数，
 * 页面无手填格；③止步/封死后补不进一笔；④省局退回先前名目作废沉底，从核查重累，
 * 沉底与后补不并列；⑤对外确认单与核签单同源，两边的数一致。</p>
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TPostPunishBillServiceImpl implements ITPostPunishBillService {

    // 开次
    private static final int NODE_ACCEPT = 0;
    private static final int NODE_VERIFY = 1;
    private static final int NODE_COSIGN = 2;
    private static final int NODE_SEAL = 3;
    private static final String[] NODE_NAMES = {"受案", "核查", "并签", "签发"};

    // 并签两笔账
    private static final int ROLE_PROGRAM = 1;
    private static final int ROLE_DISCRETION = 2;

    // 报批情形
    private static final int STATUS_RUNNING = 0;
    private static final int STATUS_PASS = 1;
    private static final int STATUS_RETURNED = 2;
    private static final int STATUS_STOPPED = 3;

    // 整改事务卡落点
    private static final int RECTIFY_DONE = 2;

    @javax.annotation.Resource
    private TPostPunishBillMapper billMapper;
    @javax.annotation.Resource
    private TPostPunishNodeMapper nodeMapper;
    @javax.annotation.Resource
    private TPostPunishSignMapper signMapper;
    @javax.annotation.Resource
    private TPostCompanyArchMapper companyArchMapper;
    @javax.annotation.Resource
    private TPostRectifyCardMapper rectifyCardMapper;
    @javax.annotation.Resource
    private TPostOutletCardMapper outletCardMapper;
    @javax.annotation.Resource
    private TPostRectifyConfirmMapper confirmMapper;
    @javax.annotation.Resource
    private TPostProvinceRosterMapper rosterMapper;
    @javax.annotation.Resource
    private ITPostQualityRuleService qualityRuleService;

    @Override
    public TPostPunishBill selectTPostPunishBillById(Long id) {
        TPostPunishBill b = billMapper.selectById(id);
        return b != null && (b.getDelFlag() == null || b.getDelFlag() == 0) ? b : null;
    }

    @Override
    public List<TPostPunishBill> selectTPostPunishBillList(QueryWrapper<TPostPunishBill> queryWrapper) {
        QueryWrapper<TPostPunishBill> qw = queryWrapper == null ? new QueryWrapper<TPostPunishBill>() : queryWrapper;
        qw.eq("del_flag", 0).orderByDesc("create_time");
        return billMapper.selectList(qw);
    }

    // ---------------------------------------------------------------- 头一开·受案

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResult<TPostPunishBill> accept(TPostPunishBill form) {
        if (form == null) {
            return OpResult.fail("受案要素不齐");
        }
        // 四样写齐，少一样进不了门：企业字号、涉案网点、事由、来源
        if (StringUtils.isEmpty(form.getSiteNo()) || StringUtils.isEmpty(form.getOutletNo())
                || StringUtils.isEmpty(form.getCaseReason()) || StringUtils.isEmpty(form.getCaseSource())) {
            return OpResult.fail("企业字号、涉案网点、事由与来源须写齐，少一样进不了门");
        }
        // 只认字号：拿字号去在册底档里对，申请人姓名不作核对凭据
        TPostCompanyArch arch = companyArchMapper.selectOne(new QueryWrapper<TPostCompanyArch>()
                .eq("site_no", form.getSiteNo().trim()).eq("del_flag", 0));
        if (arch == null) {
            return OpResult.fail("字号对不上在册底档，进不了门（不看申请人姓名）");
        }
        if (arch.getStatus() != null && arch.getStatus() == 1) {
            return OpResult.fail("该字号底档已注销，进不了门");
        }

        String billNo = StringUtils.isNotEmpty(form.getBillNo()) ? form.getBillNo().trim() : genNo("HC");
        Integer dup = billMapper.selectCount(new QueryWrapper<TPostPunishBill>()
                .eq("bill_no", billNo).eq("del_flag", 0));
        if (dup != null && dup > 0) {
            return OpResult.fail("核签单号 " + billNo + " 已存在");
        }

        // 同一企业当年第二件起当场点名
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Integer yearCnt = billMapper.selectCount(new QueryWrapper<TPostPunishBill>()
                .eq("site_no", arch.getSiteNo()).eq("del_flag", 0)
                .apply("DATE_FORMAT(create_time,'%Y') = {0}", String.valueOf(year)));
        int yearNo = (yearCnt == null ? 0 : yearCnt) + 1;

        TPostPunishBill bill = new TPostPunishBill();
        bill.setBillNo(billNo);
        bill.setNodeNo(NODE_VERIFY);          // 受案这一开走完，件停在核查开等核查
        bill.setSignMode(1);                  // 两名点齐
        bill.setNeedCount(2);
        bill.setSignCount(0);
        bill.setStatus(STATUS_RUNNING);
        bill.setSealed(0);
        bill.setBouncedRound(0);
        bill.setSiteId(arch.getId());
        bill.setSiteNo(arch.getSiteNo());
        bill.setSiteName(arch.getSiteName());
        bill.setOutletNo(form.getOutletNo().trim());
        bill.setOutletAddr(StringUtils.isNotEmpty(form.getOutletAddr()) ? form.getOutletAddr().trim() : null);
        bill.setCaseReason(form.getCaseReason().trim());
        bill.setCaseSource(form.getCaseSource().trim());
        bill.setApplicantName(form.getApplicantName()); // 仅留痕，不作核对凭据
        bill.setYearNo(yearNo);
        bill.setDelFlag(0);
        if (StringUtils.isNotEmpty(form.getRemark())) {
            bill.setRemark(form.getRemark());
        }
        billMapper.insert(bill);

        writeNode(bill, NODE_ACCEPT, actor(), "受案：字号/网点/事由/来源写齐，进门", null);

        String msg = "受案已成，件到核查开";
        if (yearNo >= 2) {
            msg = "当场点名：该企业 " + year + " 年第 " + yearNo + " 件。" + msg;
        }
        return OpResult.ok(msg, bill);
    }

    // ---------------------------------------------------------------- 第二开·核查

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResult<TPostPunishBill> verify(Long id, BigDecimal input, String ruleCode,
                                            String outletAddr, String opinion) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        OpResult<TPostPunishBill> guard = guardOperable(bill, NODE_VERIFY);
        if (guard != null) {
            return guard;
        }
        if (input == null) {
            return OpResult.fail("现场材料数值缺着，对不住在册底档");
        }

        // 现场材料对在册底档：只认字号，姓名撞得一个不差也不顶用
        TPostCompanyArch arch = companyArchMapper.selectOne(new QueryWrapper<TPostCompanyArch>()
                .eq("site_no", bill.getSiteNo()).eq("del_flag", 0));
        if (arch == null || (arch.getStatus() != null && arch.getStatus() == 1)) {
            return OpResult.fail("字号在在册底档里对不上（已注销/不存在），核查不放行；申请人姓名不作数");
        }
        TPostOutletCard outlet = outletCardMapper.selectOne(new QueryWrapper<TPostOutletCard>()
                .eq("bill_no", bill.getOutletNo()).eq("del_flag", 0).last("limit 1"));
        if (outlet != null && StringUtils.isNotEmpty(outlet.getSiteNo())
                && !outlet.getSiteNo().equals(bill.getSiteNo())) {
            return OpResult.fail("现场网点卡字号与底档字号不一致，核查不放行");
        }

        // 这个网点还有没了结的整改事务卡压着，件停在核查，往后再齐也不往前走
        TPostRectifyCard blocking = rectifyCardMapper.selectOne(new QueryWrapper<TPostRectifyCard>()
                .eq("outlet_no", bill.getOutletNo())
                .ne("status", RECTIFY_DONE)
                .eq("del_flag", 0)
                .orderByDesc("create_time").last("limit 1"));
        if (blocking != null) {
            bill.setBlockedBy(blocking.getBizNo());
            billMapper.updateById(bill);
            return OpResult.fail("网点 " + bill.getOutletNo() + " 尚有未了结整改事务卡 "
                    + blocking.getBizNo() + " 压着，件停在核查这一开");
        }
        bill.setBlockedBy(null);

        // 裁量幅度合当季定下的那一格：按当下现行判据线定格（件量类数值不套 0..100 的达标率口径）
        Date now = new Date();
        String code = StringUtils.isNotEmpty(ruleCode) ? ruleCode.trim()
                : (StringUtils.isNotEmpty(bill.getRuleCode()) ? bill.getRuleCode() : pickTopRuleCode(now));
        if (StringUtils.isEmpty(code)) {
            return OpResult.fail("当季没有现行判据线，裁量格定不下来，停在核查");
        }
        int grade = qualityRuleService.evaluate(code, input, now);
        if (grade == 0) {
            return OpResult.fail("判据线 " + code + " 在当下未生效或缺阈值，定不出格次，停在核查");
        }
        bill.setVerifyInput(input);
        bill.setRuleCode(code);
        bill.setGradeLevel(grade);
        if (StringUtils.isNotEmpty(outletAddr)) {
            bill.setOutletAddr(outletAddr.trim());
        }

        writeNode(bill, NODE_VERIFY, actor(),
                "核查过：只认字号对底档；当季判据线 " + code + " 定格第 " + grade + " 格"
                        + (StringUtils.isNotEmpty(opinion) ? "；" + opinion.trim() : ""), null);

        bill.setNodeNo(NODE_COSIGN);
        bill.setSignCount(0);
        bill.setNeedCount(2);
        billMapper.updateById(bill);
        return OpResult.ok("核查过，件到并签开（裁量定格第 " + grade + " 格）", bill);
    }

    // ---------------------------------------------------------------- 第三开·并签

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResult<SignCountVo> cosign(Long id, Integer signRole, String signerName,
                                        Integer onBehalf, String opinion) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        OpResult<TPostPunishBill> guard = guardOperable(bill, NODE_COSIGN);
        if (guard != null) {
            return OpResult.fail(guard.getMsg());
        }
        if (signRole == null || (signRole != ROLE_PROGRAM && signRole != ROLE_DISCRETION)) {
            return OpResult.fail("并签两笔账：1盯程序、2盯裁量，落哪一笔须讲明");
        }
        if (StringUtils.isEmpty(signerName)) {
            return OpResult.fail("落名人不能为空");
        }
        String signer = signerName.trim();
        int behalf = onBehalf != null && onBehalf == 1 ? 1 : 0;
        int round = bill.getBouncedRound() == null ? 0 : bill.getBouncedRound();

        TPostPunishNode cosignNode = findNode(bill.getId(), round, NODE_COSIGN);
        if (cosignNode == null) {
            cosignNode = writeNode(bill, NODE_COSIGN, null, "并签开（两笔账各记各的）", null);
        }

        List<TPostPunishSign> signs = currentSigns(bill.getId(), round);
        TPostPunishSign program = realSign(signs, ROLE_PROGRAM);
        TPostPunishSign discretion = realSign(signs, ROLE_DISCRETION);

        // 替人落的一笔不作数，也不占位，允许之后本人补落
        if (behalf == 0) {
            TPostPunishSign mine = signRole == ROLE_PROGRAM ? program : discretion;
            if (mine != null) {
                return OpResult.fail("这一笔账已由 " + mine.getSignerName() + " 落名，不能再落第二笔");
            }
            TPostPunishSign other = signRole == ROLE_PROGRAM ? discretion : program;
            if (other != null && signer.equals(other.getSignerName())) {
                return OpResult.fail("两名并排落名，一人顶一笔；同一人不能把两笔账都占了");
            }
        }

        TPostPunishSign sign = new TPostPunishSign();
        sign.setBillId(bill.getId());
        sign.setNodeId(cosignNode.getId());
        sign.setSignRole(signRole);
        sign.setSignerName(signer);
        sign.setOnBehalf(behalf);
        sign.setValidFlag(1);
        sign.setRoundNo(round);
        sign.setSignTime(new Date());
        sign.setDelFlag(0);
        signMapper.insert(sign);

        SignCountVo vo = recount(bill, true);
        if (!vo.getComplete()) {
            return OpResult.ok("已落一笔（" + roleName(signRole) + "），还差一笔，件仍卡在并签开", vo);
        }
        bill.setNodeNo(NODE_SEAL);
        billMapper.updateById(bill);
        return OpResult.ok("两笔账点齐，件到签发开", vo);
    }

    @Override
    public SignCountVo countSigns(Long id) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        if (bill == null) {
            return null;
        }
        return recount(bill, false);
    }

    @Override
    public List<TPostPunishSign> listCurrentSigns(Long id) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        if (bill == null) {
            return new ArrayList<TPostPunishSign>();
        }
        return currentSigns(bill.getId(), bill.getBouncedRound() == null ? 0 : bill.getBouncedRound());
    }

    // ---------------------------------------------------------------- 末一开·签发

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResult<TPostPunishBill> seal(Long id, Date rectifyDeadline, String reliefWay, String opinion) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        OpResult<TPostPunishBill> guard = guardOperable(bill, NODE_SEAL);
        if (guard != null) {
            return guard;
        }
        SignCountVo vo = recount(bill, false);
        if (!vo.getComplete()) {
            return OpResult.fail("并签两笔账没点齐（程序笔=" + (vo.getProgramSigned() ? vo.getProgramSigner() : "缺")
                    + "，裁量笔=" + (vo.getDiscretionSigned() ? vo.getDiscretionSigner() : "缺") + "），见不着签发");
        }
        if (StringUtils.isEmpty(bill.getOutletAddr())) {
            return OpResult.fail("网点地址还没带住，对外确认单开不出去，先补齐再签发");
        }
        if (rectifyDeadline == null) {
            return OpResult.fail("责令改正期限须从这一开同件带出");
        }
        if (StringUtils.isEmpty(reliefWay)) {
            return OpResult.fail("救济途径须从这一开同件带出");
        }
        int round = bill.getBouncedRound() == null ? 0 : bill.getBouncedRound();
        Date now = new Date();

        // 一键转对外整改确认单：五样全从同一件带出，不另请一人抄第二遍
        TPostRectifyConfirm confirm = new TPostRectifyConfirm();
        confirm.setConfirmNo(genNo("ZG"));
        confirm.setBillId(bill.getId());
        confirm.setBillNo(bill.getBillNo());
        confirm.setSiteNo(bill.getSiteNo());
        confirm.setSiteName(bill.getSiteName());
        confirm.setOutletNo(bill.getOutletNo());
        confirm.setOutletAddr(bill.getOutletAddr());
        confirm.setCaseReason(bill.getCaseReason());
        confirm.setRectifyDeadline(rectifyDeadline);
        confirm.setReliefWay(reliefWay.trim());
        confirm.setIssueTime(now);
        confirm.setRoundNo(round);
        confirm.setDelFlag(0);
        confirmMapper.insert(confirm);

        // 同步一份到省局名录
        TPostProvinceRoster roster = new TPostProvinceRoster();
        roster.setBillId(bill.getId());
        roster.setBillNo(bill.getBillNo());
        roster.setSiteNo(bill.getSiteNo());
        roster.setSiteName(bill.getSiteName());
        roster.setOutletNo(bill.getOutletNo());
        roster.setSyncTime(now);
        roster.setRoundNo(round);
        roster.setDelFlag(0);
        provinceRosterMapper().insert(roster);

        writeNode(bill, NODE_SEAL, actor(),
                "签章：同步省局名录，开出整改确认单 " + confirm.getConfirmNo()
                        + (StringUtils.isNotEmpty(opinion) ? "；" + opinion.trim() : ""), null);

        // 章盖到底，当前轮各栏当即封死
        List<TPostPunishNode> nodes = currentNodes(bill.getId(), round);
        for (TPostPunishNode n : nodes) {
            n.setSealed(1);
            nodeMapper.updateById(n);
        }
        bill.setSealed(1);
        bill.setStatus(STATUS_PASS);
        bill.setRectifyDeadline(rectifyDeadline);
        bill.setReliefWay(reliefWay.trim());
        bill.setOutConfirmNo(confirm.getConfirmNo());
        billMapper.updateById(bill);
        return OpResult.ok("章盖到底，各栏封死；省局名录已同步，整改确认单已开出", bill);
    }

    // ---------------------------------------------------------------- 止步 / 省局退回

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResult<TPostPunishBill> stop(Long id, Integer stopKind, String opinion) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        if (bill == null) {
            return OpResult.fail("件不存在");
        }
        if (bill.getSealed() != null && bill.getSealed() == 1) {
            return OpResult.fail("已盖到底的件不能再写止步；省局有话说走退回");
        }
        if (bill.getStopKind() != null) {
            return OpResult.fail("件已止步（" + stopName(bill.getStopKind()) + "），事后谁也补不进一笔");
        }
        if (stopKind == null || (stopKind != 1 && stopKind != 2)) {
            return OpResult.fail("止步名目：1不予立案 2撤案");
        }
        int atNode = bill.getNodeNo() == null ? 0 : bill.getNodeNo();
        writeNode(bill, atNode, actor(),
                stopName(stopKind) + (StringUtils.isNotEmpty(opinion) ? "：" + opinion.trim() : ""), stopKind);
        bill.setStopKind(stopKind);
        bill.setStatus(STATUS_STOPPED);
        billMapper.updateById(bill);
        return OpResult.ok("件在" + NODE_NAMES[atNode] + "开写下" + stopName(stopKind) + "，到此打住", bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResult<TPostPunishBill> bounce(Long id, String opinion) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        if (bill == null) {
            return OpResult.fail("件不存在");
        }
        if (bill.getStopKind() != null) {
            return OpResult.fail("已止步的件不退，要查另起一件");
        }
        if (bill.getSealed() == null || bill.getSealed() != 1) {
            return OpResult.fail("只有盖到底、被省局退回的件才走这一道");
        }
        int oldRound = bill.getBouncedRound() == null ? 0 : bill.getBouncedRound();
        int newRound = oldRound + 1;

        // 先前各开落下的名目一律作废沉底（受案开不算，从核查重新往上累）；签迹随轮作废
        List<TPostPunishNode> oldNodes = nodeMapper.selectList(new QueryWrapper<TPostPunishNode>()
                .eq("bill_id", bill.getId()).eq("round_no", oldRound)
                .ge("node_no", NODE_VERIFY).eq("valid_flag", 1).eq("del_flag", 0));
        for (TPostPunishNode n : oldNodes) {
            n.setValidFlag(0);
            n.setSealed(0);
            nodeMapper.updateById(n);
        }
        List<TPostPunishSign> oldSigns = signMapper.selectList(new QueryWrapper<TPostPunishSign>()
                .eq("bill_id", bill.getId()).eq("round_no", oldRound)
                .eq("valid_flag", 1).eq("del_flag", 0));
        for (TPostPunishSign s : oldSigns) {
            s.setValidFlag(0);
            signMapper.updateById(s);
        }
        // 旧轮已同步省局的名录、已开出去的确认单文本随旧轮沉底，不与新轮并列
        List<TPostProvinceRoster> oldRosters = rosterMapper.selectList(new QueryWrapper<TPostProvinceRoster>()
                .eq("bill_id", bill.getId()).eq("round_no", oldRound).eq("del_flag", 0));
        for (TPostProvinceRoster r : oldRosters) {
            r.setDelFlag(1);
            rosterMapper.updateById(r);
        }
        List<TPostRectifyConfirm> oldConfirms = confirmMapper.selectList(new QueryWrapper<TPostRectifyConfirm>()
                .eq("bill_id", bill.getId()).eq("round_no", oldRound).eq("del_flag", 0));
        for (TPostRectifyConfirm c : oldConfirms) {
            c.setDelFlag(1);
            confirmMapper.updateById(c);
        }

        // 受案凭据跨轮仍作数，但章已随退回解开，受案栏的封死记号一并摘掉
        List<TPostPunishNode> acceptNodes = nodeMapper.selectList(new QueryWrapper<TPostPunishNode>()
                .eq("bill_id", bill.getId()).eq("node_no", NODE_ACCEPT)
                .eq("valid_flag", 1).eq("del_flag", 0));
        for (TPostPunishNode n : acceptNodes) {
            n.setSealed(0);
            nodeMapper.updateById(n);
        }

        bill.setBouncedRound(newRound);
        bill.setNodeNo(NODE_VERIFY);
        bill.setStatus(STATUS_RETURNED);
        bill.setSealed(0);
        bill.setSignCount(0);
        bill.setNeedCount(2);
        bill.setBlockedBy(null);
        bill.setVerifyInput(null);
        bill.setRuleCode(null);
        bill.setGradeLevel(null);
        bill.setOutConfirmNo(null);
        bill.setRectifyDeadline(null);
        bill.setReliefWay(null);
        String note = "第 " + newRound + " 趟：省局退回（"
                + (StringUtils.isNotEmpty(opinion) ? opinion.trim() : "措辞不合规矩") + "），旧轮名目已沉底，从核查重累";
        bill.setRemark(StringUtils.isNotEmpty(bill.getRemark()) ? bill.getRemark() + " | " + note : note);
        billMapper.updateById(bill);
        return OpResult.ok("省局退回：先前名目一律作废沉底，件回到核查开重新累", bill);
    }

    // ---------------------------------------------------------------- 台账 / 倒计 / 确认单

    @Override
    public List<TPostPunishNode> listCurrentNodes(Long id) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        if (bill == null) {
            return new ArrayList<TPostPunishNode>();
        }
        return currentNodes(bill.getId(), bill.getBouncedRound() == null ? 0 : bill.getBouncedRound());
    }

    @Override
    public BillTraceVo trace(Long id) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        if (bill == null) {
            return null;
        }
        int round = bill.getBouncedRound() == null ? 0 : bill.getBouncedRound();
        BillTraceVo vo = new BillTraceVo();
        vo.setBillId(bill.getId());
        vo.setBillNo(bill.getBillNo());
        vo.setRoundNo(round);
        vo.setNodeNo(bill.getNodeNo());
        vo.setSealed(bill.getSealed() != null && bill.getSealed() == 1);
        vo.setStatus(bill.getStatus());

        List<TPostPunishNode> nodes = currentNodes(bill.getId(), round);
        SignCountVo signCount = recount(bill, false);
        vo.setSignCount(signCount);

        // 该有哪几开：封死→0..3；止步→0..止步开；在核→0..(当前开-1)
        int topNode;
        if (vo.getSealed()) {
            topNode = NODE_SEAL;
        } else if (bill.getStopKind() != null) {
            topNode = bill.getNodeNo() == null ? 0 : bill.getNodeNo();
        } else {
            topNode = (bill.getNodeNo() == null ? 0 : bill.getNodeNo()) - 1;
        }
        List<String> problems = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<BillTraceVo.TraceNode> traceNodes = new ArrayList<BillTraceVo.TraceNode>();
        for (int k = NODE_SEAL; k >= NODE_ACCEPT; k--) {
            TPostPunishNode n = findNode(nodes, k);
            if (k > topNode) {
                continue; // 还没走到的开不在倒计数里
            }
            BillTraceVo.TraceNode tn = new BillTraceVo.TraceNode();
            tn.setNodeNo(k);
            tn.setNodeName(NODE_NAMES[k]);
            if (n == null) {
                tn.setRecheckPass(false);
                tn.setRecheckMsg("缺这一开的落名凭据");
                problems.add(NODE_NAMES[k] + "开：缺落名行");
            } else {
                tn.setActorName(n.getActorName());
                tn.setOpinion(n.getOpinion());
                tn.setStopKind(n.getStopKind());
                tn.setActionTime(n.getCreateTime() == null ? null : sdf.format(n.getCreateTime()));
                String problem = recheckNode(k, n, bill, signCount, round);
                if (problem == null) {
                    tn.setRecheckPass(true);
                    tn.setRecheckMsg("复核通过");
                } else {
                    tn.setRecheckPass(false);
                    tn.setRecheckMsg(problem);
                    problems.add(NODE_NAMES[k] + "开：" + problem);
                }
            }
            traceNodes.add(tn);
        }

        // 省局名录存根（当前轮）
        Integer rosterCnt = provinceRosterMapper().selectCount(new QueryWrapper<TPostProvinceRoster>()
                .eq("bill_id", bill.getId()).eq("round_no", round).eq("del_flag", 0));
        vo.setRosterSynced(rosterCnt != null && rosterCnt > 0);
        if (vo.getSealed() && !vo.getRosterSynced()) {
            problems.add("签发开：省局名录查不到当前轮同步存根");
        }

        // 对外确认单同源核对（未封死无单可对，置 null；封死后必须真单且各数接平）
        TPostRectifyConfirm confirm = currentConfirm(id);
        Boolean sameSource = null;
        if (vo.getSealed()) {
            if (confirm == null) {
                sameSource = false;
                problems.add("签发开：整改确认单存根缺失");
            } else {
                sameSource = eq(confirm.getSiteNo(), bill.getSiteNo())
                        && eq(confirm.getSiteName(), bill.getSiteName())
                        && eq(confirm.getOutletNo(), bill.getOutletNo())
                        && eq(confirm.getOutletAddr(), bill.getOutletAddr())
                        && eq(confirm.getCaseReason(), bill.getCaseReason())
                        && confirm.getRectifyDeadline() != null
                        && confirm.getRectifyDeadline().equals(bill.getRectifyDeadline())
                        && eq(confirm.getReliefWay(), bill.getReliefWay());
                if (!sameSource) {
                    problems.add("签发开：确认单与核签单摆的数接不平，不是同一件带出去的");
                }
            }
        }
        vo.setConfirmSameSource(sameSource);
        vo.setNodes(traceNodes);
        vo.setAllRecheckPass(problems.isEmpty());
        vo.setProblems(problems);
        return vo;
    }

    @Override
    public TPostRectifyConfirm currentConfirm(Long id) {
        TPostPunishBill bill = selectTPostPunishBillById(id);
        if (bill == null) {
            return null;
        }
        int round = bill.getBouncedRound() == null ? 0 : bill.getBouncedRound();
        return confirmMapper.selectOne(new QueryWrapper<TPostRectifyConfirm>()
                .eq("bill_id", bill.getId()).eq("round_no", round).eq("del_flag", 0)
                .orderByDesc("issue_time").last("limit 1"));
    }

    // ---------------------------------------------------------------- 内部口径

    /** 操作前置：件在、未止步、未封死，且正停在要操作的那一开（抢步不算过成） */
    private OpResult<TPostPunishBill> guardOperable(TPostPunishBill bill, int expectNode) {
        if (bill == null) {
            return OpResult.fail("件不存在");
        }
        if (bill.getStopKind() != null) {
            return OpResult.fail("件已止步（" + stopName(bill.getStopKind()) + "），补不进一笔");
        }
        if (bill.getSealed() != null && bill.getSealed() == 1) {
            return OpResult.fail("章已盖到底，各栏封死，想改只能另起一件");
        }
        if (bill.getNodeNo() == null || bill.getNodeNo() != expectNode) {
            int at = bill.getNodeNo() == null ? 0 : bill.getNodeNo();
            return OpResult.fail("件停在" + (at >= 0 && at <= 3 ? NODE_NAMES[at] : ("第" + at + "开"))
                    + "，推进只认开次，抢步不算过成");
        }
        return null;
    }

    /** 顺着件自己的签迹一段段数：给人过目的数与回写库的数同一次点算给回 */
    private SignCountVo recount(TPostPunishBill bill, boolean persist) {
        int round = bill.getBouncedRound() == null ? 0 : bill.getBouncedRound();
        List<TPostPunishSign> signs = currentSigns(bill.getId(), round);
        TPostPunishSign program = realSign(signs, ROLE_PROGRAM);
        TPostPunishSign discretion = realSign(signs, ROLE_DISCRETION);
        int behalfCnt = 0;
        for (TPostPunishSign s : signs) {
            if (s.getOnBehalf() != null && s.getOnBehalf() == 1) {
                behalfCnt++;
            }
        }
        int counted = (program != null ? 1 : 0) + (discretion != null ? 1 : 0);

        if (persist) {
            bill.setNeedCount(2);
            bill.setSignCount(counted);
            billMapper.updateById(bill);
        }

        SignCountVo vo = new SignCountVo();
        vo.setBillId(bill.getId());
        vo.setBillNo(bill.getBillNo());
        vo.setNodeNo(bill.getNodeNo());
        vo.setRoundNo(round);
        vo.setNeedCount(2);
        vo.setViewCount(counted);                       // 顺着签迹数出来的
        vo.setDbCount(bill.getSignCount() == null ? 0 : bill.getSignCount()); // 库里存着的
        vo.setConsistent(vo.getViewCount().equals(vo.getDbCount()));
        vo.setProgramSigned(program != null);
        vo.setDiscretionSigned(discretion != null);
        vo.setProgramSigner(program == null ? null : program.getSignerName());
        vo.setDiscretionSigner(discretion == null ? null : discretion.getSignerName());
        vo.setOnBehalfCount(behalfCnt);
        // 两数接不平，这一开就算没满
        vo.setComplete(vo.getConsistent() && counted == 2
                && program != null && discretion != null
                && !program.getSignerName().equals(discretion.getSignerName()));
        return vo;
    }

    /** 当前轮有效签迹（代签笔仍在，点算时按 on_behalf 排除） */
    private List<TPostPunishSign> currentSigns(Long billId, int round) {
        return signMapper.selectList(new QueryWrapper<TPostPunishSign>()
                .eq("bill_id", billId).eq("round_no", round)
                .eq("valid_flag", 1).eq("del_flag", 0)
                .orderByAsc("sign_time").orderByAsc("id"));
    }

    /** 某角色那一笔本人账（替人落的不取） */
    private TPostPunishSign realSign(List<TPostPunishSign> signs, int role) {
        for (TPostPunishSign s : signs) {
            if (s.getSignRole() != null && s.getSignRole() == role
                    && (s.getOnBehalf() == null || s.getOnBehalf() == 0)) {
                return s;
            }
        }
        return null;
    }

    /**
     * 当前轮台账：受案开（首趟落的，跨轮始终作数）+ 本轮核查往上各开。
     * 作废沉底的旧轮行不并列在内。
     */
    private List<TPostPunishNode> currentNodes(Long billId, int round) {
        List<TPostPunishNode> list = nodeMapper.selectList(new QueryWrapper<TPostPunishNode>()
                .eq("bill_id", billId).eq("valid_flag", 1).eq("del_flag", 0)
                .and(w -> w.eq("node_no", NODE_ACCEPT).or(q -> q.ge("node_no", NODE_VERIFY).eq("round_no", round)))
                .orderByAsc("node_no"));
        return list;
    }

    private TPostPunishNode findNode(Long billId, int round, int nodeNo) {
        return findNode(currentNodes(billId, round), nodeNo);
    }

    private TPostPunishNode findNode(List<TPostPunishNode> nodes, int nodeNo) {
        TPostPunishNode plain = null;
        for (TPostPunishNode n : nodes) {
            if (n.getNodeNo() != null && n.getNodeNo() == nodeNo) {
                // 同一开可能有两行（如并签开行行 + 后写的止步行），带止步名目的行才是这一开的落点
                if (n.getStopKind() != null) {
                    return n;
                }
                if (plain == null) {
                    plain = n;
                }
            }
        }
        return plain;
    }

    private TPostPunishNode writeNode(TPostPunishBill bill, int nodeNo, String actor, String opinion, Integer stopKind) {
        int round = bill.getBouncedRound() == null ? 0 : bill.getBouncedRound();
        TPostPunishNode n = new TPostPunishNode();
        n.setBillId(bill.getId());
        n.setBillNo(bill.getBillNo());
        n.setNodeNo(nodeNo);
        n.setRoundNo(round);
        n.setValidFlag(1);
        n.setActorName(actor);
        n.setOpinion(opinion);
        n.setStopKind(stopKind);
        n.setSealed(bill.getSealed() != null && bill.getSealed() == 1 ? 1 : 0);
        n.setDelFlag(0);
        nodeMapper.insert(n);
        return n;
    }

    /** 每开复核：受案四要素 / 核查底档与定格 / 并签两笔 / 签发封死同步同源 */
    private String recheckNode(int k, TPostPunishNode n, TPostPunishBill bill,
                               SignCountVo signCount, int round) {
        // 止步终止开：在这开写下不予立案/撤案的，按止步凭据复核，不要求该开走满
        if (n.getStopKind() != null) {
            if (n.getStopKind() != 1 && n.getStopKind() != 2) {
                return "止步名目只能是 1不予立案 2撤案";
            }
            if (!n.getStopKind().equals(bill.getStopKind())) {
                return "分账止步名目与件上对不上号";
            }
            if (StringUtils.isEmpty(n.getActorName())) {
                return "写下止步却没落名";
            }
            return null;
        }
        switch (k) {
            case NODE_ACCEPT:
                if (StringUtils.isEmpty(bill.getSiteNo()) || StringUtils.isEmpty(bill.getOutletNo())
                        || StringUtils.isEmpty(bill.getCaseReason()) || StringUtils.isEmpty(bill.getCaseSource())) {
                    return "受案四要素缺样";
                }
                if (StringUtils.isEmpty(n.getActorName())) {
                    return "受案没落名";
                }
                return null;
            case NODE_VERIFY:
                if (StringUtils.isEmpty(bill.getSiteNo())) {
                    return "核查只认字号，件上却没字号";
                }
                if (bill.getVerifyInput() == null || bill.getGradeLevel() == null
                        || StringUtils.isEmpty(bill.getRuleCode())) {
                    return "当季定格凭据不全（现场值/格次/判据线）";
                }
                if (StringUtils.isNotEmpty(bill.getBlockedBy())) {
                    return "件仍被整改事务卡 " + bill.getBlockedBy() + " 压着，本不该过核查";
                }
                if (StringUtils.isEmpty(n.getActorName())) {
                    return "核查没落名";
                }
                return null;
            case NODE_COSIGN:
                if (!Boolean.TRUE.equals(signCount.getConsistent())) {
                    return "给目计数与库存计数不是同一次点算，接不平";
                }
                if (!Boolean.TRUE.equals(signCount.getComplete())) {
                    return "两笔账没点齐或被同一人顶了";
                }
                return null;
            case NODE_SEAL:
                if (bill.getSealed() == null || bill.getSealed() != 1) {
                    return "章没盖到底";
                }
                if (n.getSealed() == null || n.getSealed() != 1) {
                    return "本开栏位没封死";
                }
                Integer rosterCnt = provinceRosterMapper().selectCount(new QueryWrapper<TPostProvinceRoster>()
                        .eq("bill_id", bill.getId()).eq("round_no", round).eq("del_flag", 0));
                if (rosterCnt == null || rosterCnt == 0) {
                    return "省局名录没同步";
                }
                return null;
            default:
                return "未知开次";
        }
    }

    private String pickTopRuleCode(Date at) {
        List<com.fc.v2.model.auto.TPostQualityRule> avail = qualityRuleService.listAvailable(at);
        return avail.isEmpty() ? null : avail.get(0).getRuleCode();
    }

    private TPostProvinceRosterMapper provinceRosterMapper() {
        return this.rosterMapper;
    }    private String actor() {
        try {
            return ShiroUtils.getLoginName();
        } catch (Exception e) {
            return "Task";
        }
    }

    private static String roleName(int role) {
        return role == ROLE_PROGRAM ? "盯程序" : "盯裁量";
    }

    private static String stopName(Integer kind) {
        if (kind == null) {
            return "";
        }
        return kind == 1 ? "不予立案" : kind == 2 ? "撤案" : ("止步(" + kind + ")");
    }

    private static boolean eq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static String genNo(String prefix) {
        String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        long suffix = Long.parseLong(SnowflakeIdWorker.getUUID());
        return prefix + ts + String.format("%05d", suffix % 100000);
    }
}
