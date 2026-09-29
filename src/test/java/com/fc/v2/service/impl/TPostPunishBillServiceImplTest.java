package com.fc.v2.service.impl;

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
import com.fc.v2.model.auto.TPostRectifyNotice;
import com.fc.v2.model.custom.post.BillCosignCmd;
import com.fc.v2.model.custom.post.BillFlowConstants;
import com.fc.v2.model.custom.post.BillReceiveForm;
import com.fc.v2.model.custom.post.BillTraceVo;
import com.fc.v2.model.custom.post.BillVerifyCmd;
import com.fc.v2.model.custom.post.CosignCountVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 核签单四开状态机单测：不开数据库，全部闸门逐档过一遍。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ExtendWith(MockitoExtension.class)
class TPostPunishBillServiceImplTest {

    @Mock
    private TPostPunishBillMapper billMapper;
    @Mock
    private TPostBillActionMapper actionMapper;
    @Mock
    private TPostCompanyArchMapper archMapper;
    @Mock
    private TPostRectifyCardMapper rectifyCardMapper;
    @Mock
    private TPostQualityRuleMapper qualityRuleMapper;
    @Mock
    private TPostProvinceRosterMapper rosterMapper;
    @Mock
    private TPostRectifyNoticeMapper noticeMapper;

    @InjectMocks
    private TPostPunishBillServiceImpl service;

    private static final String OPERATOR = "fazhi-zhang";

    private BillReceiveForm form() {
        BillReceiveForm f = new BillReceiveForm();
        f.setSiteNo("YZ00");
        f.setOutletNo("WD-07");
        f.setCaseReason("末端网点未备案收寄件");
        f.setSourceChannel("省局案卷倒查");
        f.setApplicantName("张三");
        return f;
    }

    private TPostPunishBill bill(int node, Integer round) {
        TPostPunishBill b = new TPostPunishBill();
        b.setId(1001L);
        b.setBillNo("HC2026-YZ00-001");
        b.setNodeNo(node);
        b.setRoundNo(round == null ? 1 : round);
        b.setStatus(BillFlowConstants.BILL_RUNNING);
        b.setSealed(0);
        b.setProvinceStatus(BillFlowConstants.PROVINCE_NONE);
        b.setDelFlag(0);
        b.setSiteNo("YZ00");
        b.setOutletNo("WD-07");
        b.setCaseReason("末端网点未备案收寄件");
        return b;
    }

    private TPostBillAction action(int round, int node, int role, String signer) {
        TPostBillAction a = new TPostBillAction();
        a.setId((long) (role + 1));
        a.setBillId(1001L);
        a.setRoundNo(round);
        a.setNodeNo(node);
        a.setRoleKind(role);
        a.setSigner(signer);
        a.setActionCode(BillFlowConstants.ACTION_COSIGN);
        a.setResultCode(BillFlowConstants.RESULT_PASS);
        a.setValidFlag(BillFlowConstants.VALID);
        a.setDelFlag(0);
        a.setCreateTime(new Date());
        return a;
    }

    private BillVerifyCmd verifyCmd() {
        BillVerifyCmd c = new BillVerifyCmd();
        c.setId(1001L);
        c.setSeasonGrade(2);
        c.setOutletAddr("青屏县棋盘路 7 号");
        c.setCorrectDeadline(new Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000));
        c.setReliefChannel("60日内向青屏省邮政管理局申请行政复议");
        return c;
    }

    private void stubVerifyDeps(int openCards) {
        TPostCompanyArch arch = new TPostCompanyArch();
        arch.setSiteNo("YZ00");
        arch.setSiteName("青屏速递有限公司");
        arch.setStatus(0);
        when(archMapper.selectOne(any())).thenReturn(arch);
        when(qualityRuleMapper.selectCount(any())).thenReturn(1);
        when(rectifyCardMapper.selectCount(any())).thenReturn(openCards);
    }

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(actionMapper.insert(any())).thenReturn(1);
    }

    // ---------------- 头一开·受案 ----------------

    @Test
    void receive_missingAnyOfFourRequiredFields_isBlocked() {
        BillReceiveForm f = form();
        f.setSourceChannel("  ");
        BillFlowException ex = assertThrows(BillFlowException.class,
                () -> service.receive(f, OPERATOR));
        assertTrue(ex.getMessage().contains("来源"));
        verify(billMapper, never()).insert(any());
    }

    @Test
    void receive_fourFieldsPresent_entersAtVerifyNodeAndLogsOneAction() {
        when(billMapper.selectCount(any())).thenReturn(0);
        TPostPunishBill b = service.receive(form(), OPERATOR);
        assertEquals(BillFlowConstants.NODE_VERIFY, b.getNodeNo());
        assertEquals(1, b.getRoundNo());
        assertEquals(1, b.getSerialInYear());
        verify(billMapper).insert(any());
        verify(actionMapper).insert(any());
    }

    @Test
    void receive_secondCaseSameCompanySameYear_isFlaggedSerialTwo() {
        when(billMapper.selectCount(any())).thenReturn(1);
        TPostPunishBill b = service.receive(form(), OPERATOR);
        assertEquals(2, b.getSerialInYear(), "同企业当年第二件要在件头上点得出名");
    }

    // ---------------- 第二开·核查 ----------------

    @Test
    void verify_matchesRegisteredSiteNoOnly_applicantNameDoesNotCount() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_VERIFY, 1);
        when(billMapper.selectById(1001L)).thenReturn(b);
        stubVerifyDeps(0);

        TPostPunishBill out = service.verify(verifyCmd(), OPERATOR);
        assertEquals(BillFlowConstants.NODE_COSIGN, out.getNodeNo());
        assertEquals("青屏速递有限公司", out.getSiteName());
        assertEquals(2, out.getSeasonGrade());
        assertEquals(BillFlowConstants.MODE_AND, out.getSignMode());
        assertEquals(2, out.getNeedCount());
        assertEquals(0, out.getSignCount());
    }

    @Test
    void verify_siteNoMissingFromRegistry_isBlocked() {
        when(billMapper.selectById(1001L)).thenReturn(bill(BillFlowConstants.NODE_VERIFY, 1));
        when(archMapper.selectOne(any())).thenReturn(null);
        assertThrows(BillFlowException.class, () -> service.verify(verifyCmd(), OPERATOR));
        verify(billMapper, never()).updateById(any());
    }

    @Test
    void verify_archivedDeregistration_isBlocked() {
        when(billMapper.selectById(1001L)).thenReturn(bill(BillFlowConstants.NODE_VERIFY, 1));
        TPostCompanyArch arch = new TPostCompanyArch();
        arch.setStatus(1);
        when(archMapper.selectOne(any())).thenReturn(arch);
        BillFlowException ex = assertThrows(BillFlowException.class,
                () -> service.verify(verifyCmd(), OPERATOR));
        assertTrue(ex.getMessage().contains("注销"));
    }

    @Test
    void verify_openRectifyCardOnSameOutlet_stopsAtVerifyForever() {
        TPostPunishBill held = bill(BillFlowConstants.NODE_VERIFY, 1);
        when(billMapper.selectById(1001L)).thenReturn(held);
        stubVerifyDeps(1);
        BillFlowException ex = assertThrows(BillFlowException.class,
                () -> service.verify(verifyCmd(), OPERATOR));
        assertTrue(ex.getMessage().contains("整改事务卡"));
        // 件仍停在核查，没有往后走一步。
        assertEquals(BillFlowConstants.NODE_VERIFY, held.getNodeNo());
    }

    @Test
    void verify_gradeOutOfSeasonBand_isBlocked() {
        BillVerifyCmd c = verifyCmd();
        c.setSeasonGrade(4);
        when(billMapper.selectById(1001L)).thenReturn(bill(BillFlowConstants.NODE_VERIFY, 1));
        assertThrows(BillFlowException.class, () -> service.verify(c, OPERATOR));
    }

    @Test
    void verify_cannotRunWhenBillAlreadyAtCosign_jumpNotCounted() {
        when(billMapper.selectById(1001L)).thenReturn(bill(BillFlowConstants.NODE_COSIGN, 1));
        BillFlowException ex = assertThrows(BillFlowException.class,
                () -> service.verify(verifyCmd(), OPERATOR));
        assertTrue(ex.getMessage().contains("并签"));
    }

    // ---------------- 第三开·并签 ----------------

    @Test
    void cosign_singleName_billStaysAtCosignWithCountOne() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_COSIGN, 1);
        b.setNeedCount(2);
        when(billMapper.selectById(1001L)).thenReturn(b);
        // 第一次查=落名前的已存签迹（空）；落名后同一次点算再查（程序一笔）。
        when(actionMapper.selectList(any()))
                .thenReturn(Collections.<TPostBillAction>emptyList())
                .thenReturn(Collections.singletonList(
                        action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li-chengxu")));

        BillCosignCmd cmd = new BillCosignCmd();
        cmd.setId(1001L);
        cmd.setRoleKind(BillFlowConstants.ROLE_PROGRAM);
        CosignCountVo vo = service.cosign(cmd, "li-chengxu");

        assertEquals(2, vo.getNeedCount());
        assertEquals(1, vo.getSignCount());
        assertFalse(vo.isFull());
        assertTrue(vo.isProgramSigned());
        assertFalse(vo.isDiscretionSigned());
        assertEquals(BillFlowConstants.NODE_COSIGN, b.getNodeNo(), "只到一名，件仍卡在并签");
        assertEquals(1, b.getSignCount(), "库内回写与回页计数同源");
    }

    @Test
    void cosign_twoDistinctPeopleTwoRoles_countTwoAndBillAdvances() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_COSIGN, 1);
        b.setNeedCount(2);
        when(billMapper.selectById(1001L)).thenReturn(b);
        // 第一次=落名前已存（程序一笔）；第二次=落名后点算（程序+裁量两笔）。
        when(actionMapper.selectList(any()))
                .thenReturn(Collections.singletonList(
                        action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li-chengxu")))
                .thenReturn(Arrays.asList(
                        action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li-chengxu"),
                        action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_DISCRETION, "wang-cailiang")));

        BillCosignCmd cmd = new BillCosignCmd();
        cmd.setId(1001L);
        cmd.setRoleKind(BillFlowConstants.ROLE_DISCRETION);
        cmd.setOpinion("裁量合当季第二格");
        CosignCountVo vo = service.cosign(cmd, "wang-cailiang");

        assertEquals(2, vo.getSignCount());
        assertTrue(vo.isFull());
        assertEquals(BillFlowConstants.NODE_ISSUE, b.getNodeNo(), "两名点齐件才见得着签发");
        assertEquals(2, b.getSignCount());
    }

    @Test
    void cosign_samePersonSignsBothRoles_secondStrokeRejected() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_COSIGN, 1);
        when(billMapper.selectById(1001L)).thenReturn(b);
        when(actionMapper.selectList(any())).thenReturn(Collections.singletonList(
                action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "same-person")));

        BillCosignCmd cmd = new BillCosignCmd();
        cmd.setId(1001L);
        cmd.setRoleKind(BillFlowConstants.ROLE_DISCRETION);
        BillFlowException ex = assertThrows(BillFlowException.class,
                () -> service.cosign(cmd, "same-person"));
        assertTrue(ex.getMessage().contains("同一人"));
        verify(billMapper, never()).updateById(any());
    }

    @Test
    void cosign_sameRoleTwice_secondRejected() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_COSIGN, 1);
        when(billMapper.selectById(1001L)).thenReturn(b);
        when(actionMapper.selectList(any())).thenReturn(Collections.singletonList(
                action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li-chengxu")));

        BillCosignCmd cmd = new BillCosignCmd();
        cmd.setId(1001L);
        cmd.setRoleKind(BillFlowConstants.ROLE_PROGRAM);
        assertThrows(BillFlowException.class, () -> service.cosign(cmd, "another-one"));
    }

    @Test
    void cosignCount_isRecountedFromTraces_notFromBillHeader() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_COSIGN, 1);
        b.setSignCount(2); // 件头被人手工写成2，但签迹只有一笔
        when(billMapper.selectById(1001L)).thenReturn(b);
        when(actionMapper.selectList(any())).thenReturn(Collections.singletonList(
                action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li")));

        CosignCountVo vo = service.cosignCount(1001L);
        assertEquals(1, vo.getSignCount(), "只认顺着签迹点出来的数");
        assertFalse(vo.isFull());
    }

    // ---------------- 末一开·签发 ----------------

    @Test
    void issue_withOnlyOneCosignStroke_cannotReachIssue() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_ISSUE, 1);
        b.setNeedCount(2);
        b.setSignCount(2); // 件头写满也没用
        when(billMapper.selectById(1001L)).thenReturn(b);
        when(actionMapper.selectList(any())).thenReturn(Collections.singletonList(
                action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li")));

        BillFlowException ex = assertThrows(BillFlowException.class,
                () -> service.issue(1001L, null, OPERATOR));
        assertTrue(ex.getMessage().contains("两笔未点齐"));
        verify(rosterMapper, never()).insert(any());
        verify(noticeMapper, never()).insert(any());
    }

    @Test
    void issue_fullChain_sealsSyncsRosterAndIssuesNoticeFromSameBill() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_ISSUE, 1);
        b.setNeedCount(2);
        b.setSiteName("青屏速递有限公司");
        b.setSeasonGrade(2);
        b.setOutletAddr("青屏县棋盘路 7 号");
        Date deadline = new Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000);
        b.setCorrectDeadline(deadline);
        b.setReliefChannel("60日内复议");
        when(billMapper.selectById(1001L)).thenReturn(b);
        when(actionMapper.selectList(any())).thenReturn(Arrays.asList(
                action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li"),
                action(1, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_DISCRETION, "wang")));
        when(rosterMapper.selectOne(any())).thenReturn(null);
        when(noticeMapper.selectOne(any())).thenReturn(null);
        when(rosterMapper.insert(any())).thenReturn(1);
        when(noticeMapper.insert(any())).thenReturn(1);

        TPostPunishBill out = service.issue(1001L, "同意处罚", OPERATOR);

        assertEquals(1, out.getSealed());
        assertEquals(BillFlowConstants.END_SEALED, out.getEndType());
        assertEquals(BillFlowConstants.BILL_PASSED, out.getStatus());
        assertEquals(BillFlowConstants.PROVINCE_SYNCED, out.getProvinceStatus());

        ArgumentCaptor<TPostRectifyNotice> noticeCap = ArgumentCaptor.forClass(TPostRectifyNotice.class);
        verify(noticeMapper).insert(noticeCap.capture());
        TPostRectifyNotice n = noticeCap.getValue();
        assertEquals("YZ00", n.getSiteNo());
        assertEquals("青屏县棋盘路 7 号", n.getOutletAddr());
        assertEquals("末端网点未备案收寄件", n.getCaseReason());
        assertEquals(deadline, n.getCorrectDeadline());
        assertEquals("60日内复议", n.getReliefChannel());
        assertEquals(1, n.getRoundNo());
        assertNotNull(n.getIssueTime());

        ArgumentCaptor<TPostProvinceRoster> rosterCap = ArgumentCaptor.forClass(TPostProvinceRoster.class);
        verify(rosterMapper).insert(rosterCap.capture());
        assertEquals("YZ00", rosterCap.getValue().getSiteNo());
        assertNotNull(rosterCap.getValue().getSyncTime());
    }

    @Test
    void issue_cannotJumpFromVerifyNode() {
        when(billMapper.selectById(1001L)).thenReturn(bill(BillFlowConstants.NODE_VERIFY, 1));
        BillFlowException ex = assertThrows(BillFlowException.class,
                () -> service.issue(1001L, null, OPERATOR));
        assertTrue(ex.getMessage().contains("核查"));
    }

    @Test
    void sealedBill_rejectsAnyFurtherStroke() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_ISSUE, 1);
        b.setSealed(1);
        b.setEndType(BillFlowConstants.END_SEALED);
        when(billMapper.selectById(1001L)).thenReturn(b);
        assertThrows(BillFlowException.class, () -> service.issue(1001L, null, OPERATOR));
    }

    // ---------------- 止步 ----------------

    @Test
    void stop_noCaseOrWithdraw_endsBillAndLocksAllNodes() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_VERIFY, 1);
        when(billMapper.selectById(1001L)).thenReturn(b);
        TPostPunishBill stopped = service.stop(1001L, BillFlowConstants.END_NO_CASE, "事实不清", OPERATOR);
        assertEquals(BillFlowConstants.END_NO_CASE, stopped.getEndType());

        BillVerifyCmd c = verifyCmd();
        BillFlowException ex = assertThrows(BillFlowException.class,
                () -> service.verify(c, OPERATOR));
        assertTrue(ex.getMessage().contains("打住"));
        verify(billMapper, times(1)).updateById(any());
    }

    @Test
    void stop_illegalEndType_rejected() {
        assertThrows(BillFlowException.class,
                () -> service.stop(1001L, 9, null, OPERATOR));
    }

    // ---------------- 省局退回·沉底重走 ----------------

    @Test
    void provinceReturn_onlyAcceptedForSealedSyncedBill() {
        when(billMapper.selectById(1001L)).thenReturn(bill(BillFlowConstants.NODE_VERIFY, 1));
        assertThrows(BillFlowException.class,
                () -> service.provinceReturn(1001L, "措辞不合规", OPERATOR));
    }

    @Test
    void provinceReturn_requiresOpinion() {
        assertThrows(BillFlowException.class,
                () -> service.provinceReturn(1001L, "  ", OPERATOR));
    }

    @Test
    void provinceReturn_sinksOldRoundMovesRosterAndRestartsAtVerify() {
        TPostPunishBill b = sealedBillRound1();
        when(billMapper.selectById(1001L)).thenReturn(b);

        TPostBillAction receive = action(1, 0, BillFlowConstants.ROLE_RECEIVE, "clerk");
        TPostBillAction verify = action(1, 1, BillFlowConstants.ROLE_VERIFY, "verifier");
        TPostBillAction p = action(1, 2, BillFlowConstants.ROLE_PROGRAM, "li");
        TPostBillAction d = action(1, 2, BillFlowConstants.ROLE_DISCRETION, "wang");
        TPostBillAction sign = action(1, 3, BillFlowConstants.ROLE_ISSUE, "chief");
        // 旧轮有效名目（不含退回笔本身）
        when(actionMapper.selectList(any())).thenReturn(new ArrayList<>(Arrays.asList(
                receive, verify, p, d, sign)));

        TPostProvinceRoster roster = new TPostProvinceRoster();
        roster.setId(9L);
        roster.setBillNo(b.getBillNo());
        roster.setStatus(0);
        when(rosterMapper.selectOne(any())).thenReturn(roster);

        TPostPunishBill out = service.provinceReturn(1001L, "措辞不合规矩", OPERATOR);

        assertEquals(2, out.getRoundNo());
        assertEquals(BillFlowConstants.NODE_VERIFY, out.getNodeNo(), "从核查那一开重新往上累");
        assertEquals(0, out.getSignCount());
        assertNull(out.getSeasonGrade());
        assertNull(out.getCorrectDeadline());
        assertEquals(BillFlowConstants.PROVINCE_RETURNED, out.getProvinceStatus());
        assertEquals(0, out.getSealed());
        assertNull(out.getEndType());

        // 先前各开 5 笔一律沉底，退回笔本身不沉底
        assertEquals(BillFlowConstants.SUNK, receive.getValidFlag());
        assertEquals(BillFlowConstants.SUNK, sign.getValidFlag());
        verify(actionMapper, times(5)).updateById(any());
        assertEquals(1, roster.getStatus());
        assertEquals("措辞不合规矩", roster.getProvinceOpinion());
    }

    @Test
    void fullSecondRoundAfterReturn_issuesRoundTwoNoticeAndKeepsRoundOneSunk() {
        // 省局退回后，重走核查→并签→签发，确认单出第2轮；旧轮沉底名目不并列计数。
        TPostPunishBill b = bill(BillFlowConstants.NODE_VERIFY, 2);
        b.setProvinceStatus(BillFlowConstants.PROVINCE_RETURNED);
        when(billMapper.selectById(1001L)).thenReturn(b);
        stubVerifyDeps(0);

        service.verify(verifyCmd(), "verifier2");
        assertEquals(BillFlowConstants.NODE_COSIGN, b.getNodeNo());

        // 四次查询：程序笔落名前(空)→点算(程序)；裁量笔落名前(程序)→点算(程序+裁量)。
        when(actionMapper.selectList(any()))
                .thenReturn(Collections.<TPostBillAction>emptyList())
                .thenReturn(Collections.singletonList(
                        action(2, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li2")))
                .thenReturn(Collections.singletonList(
                        action(2, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li2")))
                .thenReturn(Arrays.asList(
                        action(2, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_PROGRAM, "li2"),
                        action(2, BillFlowConstants.NODE_COSIGN, BillFlowConstants.ROLE_DISCRETION, "wang2")));

        BillCosignCmd c1 = new BillCosignCmd();
        c1.setId(1001L);
        c1.setRoleKind(BillFlowConstants.ROLE_PROGRAM);
        CosignCountVo afterFirst = service.cosign(c1, "li2");
        assertEquals(1, afterFirst.getSignCount(), "旧轮沉底名目不与新轮并列");

        BillCosignCmd c2 = new BillCosignCmd();
        c2.setId(1001L);
        c2.setRoleKind(BillFlowConstants.ROLE_DISCRETION);
        CosignCountVo afterSecond = service.cosign(c2, "wang2");
        assertTrue(afterSecond.isFull());
        assertEquals(BillFlowConstants.NODE_ISSUE, b.getNodeNo());

        when(noticeMapper.selectOne(any())).thenReturn(null);
        when(rosterMapper.selectOne(any())).thenReturn(null);

        service.issue(1001L, null, "chief2");
        assertEquals(2, b.getRoundNo());
        assertEquals(1, b.getSealed());

        ArgumentCaptor<TPostRectifyNotice> cap = ArgumentCaptor.forClass(TPostRectifyNotice.class);
        verify(noticeMapper).insert(cap.capture());
        assertEquals(2, cap.getValue().getRoundNo(), "新一轮确认单从同一件带出");
    }

    // ---------------- 倒序复核 ----------------

    @Test
    void trace_sealedBill_allFourNodesVerifiedAndNoticeMatches() {
        TPostPunishBill b = sealedBillRound1();
        b.setSignCount(2);
        b.setSiteName("青屏速递有限公司");
        b.setOutletAddr("青屏县棋盘路 7 号");
        b.setReliefChannel("60日内复议");
        when(billMapper.selectById(1001L)).thenReturn(b);

        List<TPostBillAction> trail = Arrays.asList(
                action(1, 3, BillFlowConstants.ROLE_ISSUE, "chief"),
                action(1, 2, BillFlowConstants.ROLE_DISCRETION, "wang"),
                action(1, 2, BillFlowConstants.ROLE_PROGRAM, "li"),
                action(1, 1, BillFlowConstants.ROLE_VERIFY, "verifier"),
                action(1, 0, BillFlowConstants.ROLE_RECEIVE, "clerk"));
        // selectList 先后供并签点算与轨迹查询使用：返回同一批（含各开）也能点出并签两笔
        when(actionMapper.selectList(any())).thenReturn(trail);

        TPostRectifyNotice n = new TPostRectifyNotice();
        n.setBillId(1001L);
        n.setRoundNo(1);
        n.setSiteNo("YZ00");
        n.setOutletAddr("青屏县棋盘路 7 号");
        n.setCaseReason("末端网点未备案收寄件");
        n.setCorrectDeadline(b.getCorrectDeadline());
        n.setReliefChannel("60日内复议");
        when(noticeMapper.selectList(any())).thenReturn(Collections.singletonList(n));

        BillTraceVo vo = service.trace(1001L);
        assertTrue(vo.isVerified(), vo.getVerifyMessage());
        assertEquals(4, vo.getNodes().size());
        for (com.fc.v2.model.custom.post.BillNodeView nv : vo.getNodes()) {
            assertTrue(nv.isPassed(), "每一开都经得起复核：" + nv.getNodeName());
        }
        assertEquals(BillFlowConstants.NODE_ISSUE, vo.getReverseTrail().get(0).getNodeNo(),
                "倒序轨迹从末一开数起");
        assertEquals(BillFlowConstants.NODE_RECEIVE,
                vo.getReverseTrail().get(vo.getReverseTrail().size() - 1).getNodeNo());
    }

    @Test
    void trace_countTamperedOnHeader_failsVerification() {
        TPostPunishBill b = sealedBillRound1();
        b.setNeedCount(2);
        b.setSignCount(1); // 当日屏上的数与签迹接不平
        when(billMapper.selectById(1001L)).thenReturn(b);
        when(actionMapper.selectList(any())).thenReturn(Arrays.asList(
                action(1, 3, BillFlowConstants.ROLE_ISSUE, "chief"),
                action(1, 2, BillFlowConstants.ROLE_DISCRETION, "wang"),
                action(1, 2, BillFlowConstants.ROLE_PROGRAM, "li"),
                action(1, 1, BillFlowConstants.ROLE_VERIFY, "verifier"),
                action(1, 0, BillFlowConstants.ROLE_RECEIVE, "clerk")));
        when(noticeMapper.selectList(any())).thenReturn(Collections.<TPostRectifyNotice>emptyList());

        BillTraceVo vo = service.trace(1001L);
        assertFalse(vo.isVerified(), "两条数接不平就算并签开没满");
    }

    @Test
    void trace_sunkActionsAreReportedButNotCounted() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_COSIGN, 2);
        b.setNeedCount(2);
        when(billMapper.selectById(1001L)).thenReturn(b);

        TPostBillAction sunkProgram = action(1, 2, BillFlowConstants.ROLE_PROGRAM, "old-li");
        sunkProgram.setValidFlag(BillFlowConstants.SUNK);
        when(actionMapper.selectList(any())).thenReturn(Collections.singletonList(sunkProgram));
        when(noticeMapper.selectList(any())).thenReturn(Collections.<TPostRectifyNotice>emptyList());

        BillTraceVo vo = service.trace(1001L);
        assertEquals(1, vo.getSunkActions().size());
        com.fc.v2.model.custom.post.BillNodeView cosign = vo.getNodes().get(BillFlowConstants.NODE_COSIGN);
        assertEquals(0, cosign.getSignedCount(), "作废沉底的不并列在一处");
        assertFalse(cosign.isPassed());
        assertFalse(vo.isVerified(), "件停在并签、两笔未齐，复核不过");
    }

    @Test
    void trace_sealedSecondRoundInheritsReceiveAndVerifiesChainWithNoticesPerRound() {
        TPostPunishBill b = sealedBillRound1();
        b.setRoundNo(2);
        when(billMapper.selectById(1001L)).thenReturn(b);

        List<TPostBillAction> all = new ArrayList<>();
        // 第2轮有效名目：核查/并签/签发（受案沿第1轮旧账）
        all.add(action(2, 3, BillFlowConstants.ROLE_ISSUE, "chief2"));
        all.add(action(2, 2, BillFlowConstants.ROLE_DISCRETION, "wang2"));
        all.add(action(2, 2, BillFlowConstants.ROLE_PROGRAM, "li2"));
        all.add(action(2, 1, BillFlowConstants.ROLE_VERIFY, "verifier2"));
        // 第1轮省局退回凭据
        TPostBillAction ret = action(1, 3, BillFlowConstants.ROLE_PROVINCE_RETURN, "province");
        ret.setActionCode(BillFlowConstants.ACTION_PROVINCE_RETURN);
        ret.setResultCode(BillFlowConstants.RESULT_RETURN);
        all.add(ret);
        // 第1轮沉底名目
        for (TPostBillAction a : Arrays.asList(
                action(1, 3, BillFlowConstants.ROLE_ISSUE, "chief"),
                action(1, 2, BillFlowConstants.ROLE_DISCRETION, "wang"),
                action(1, 2, BillFlowConstants.ROLE_PROGRAM, "li"),
                action(1, 1, BillFlowConstants.ROLE_VERIFY, "verifier"),
                action(1, 0, BillFlowConstants.ROLE_RECEIVE, "clerk"))) {
            a.setValidFlag(BillFlowConstants.SUNK);
            all.add(a);
        }
        when(actionMapper.selectList(any())).thenReturn(all);

        Date deadline = b.getCorrectDeadline();
        TPostRectifyNotice n1 = new TPostRectifyNotice();
        n1.setBillId(1001L);
        n1.setRoundNo(1);
        n1.setSiteNo("YZ00");
        n1.setCaseReason("末端网点未备案收寄件");
        n1.setOutletAddr("旧地址");
        n1.setCorrectDeadline(new Date(deadline.getTime() - 86400000L));
        n1.setReliefChannel("旧救济表述");
        TPostRectifyNotice n2 = new TPostRectifyNotice();
        n2.setBillId(1001L);
        n2.setRoundNo(2);
        n2.setSiteNo("YZ00");
        n2.setCaseReason("末端网点未备案收寄件");
        n2.setOutletAddr(b.getOutletAddr());
        n2.setCorrectDeadline(deadline);
        n2.setReliefChannel(b.getReliefChannel());
        when(noticeMapper.selectList(any())).thenReturn(Arrays.asList(n2, n1));

        BillTraceVo vo = service.trace(1001L);
        assertTrue(vo.isVerified(), vo.getVerifyMessage());
        assertTrue(vo.getNodes().get(BillFlowConstants.NODE_RECEIVE).isPassed(),
                "受案沿第1轮旧账，不要求第2轮重受");
        for (com.fc.v2.model.custom.post.BillNodeView nv : vo.getNodes()) {
            assertTrue(nv.isPassed(), "每一开都经得起复核：" + nv.getNodeName());
        }
        assertEquals(5, vo.getSunkActions().size());
        assertEquals(2, vo.getNotices().size());
    }

    @Test
    void trace_roundTwoWithoutProvinceReturnCredential_failsVerification() {
        TPostPunishBill b = sealedBillRound1();
        b.setRoundNo(2);
        when(billMapper.selectById(1001L)).thenReturn(b);

        List<TPostBillAction> all = new ArrayList<>();
        all.add(action(2, 3, BillFlowConstants.ROLE_ISSUE, "chief2"));
        all.add(action(2, 2, BillFlowConstants.ROLE_DISCRETION, "wang2"));
        all.add(action(2, 2, BillFlowConstants.ROLE_PROGRAM, "li2"));
        all.add(action(2, 1, BillFlowConstants.ROLE_VERIFY, "verifier2"));
        // 没有第1轮省局退回凭据，却凭空起了第2轮
        TPostBillAction receive = action(1, 0, BillFlowConstants.ROLE_RECEIVE, "clerk");
        receive.setValidFlag(BillFlowConstants.SUNK);
        all.add(receive);
        when(actionMapper.selectList(any())).thenReturn(all);

        TPostRectifyNotice n2 = new TPostRectifyNotice();
        n2.setBillId(1001L);
        n2.setRoundNo(2);
        n2.setSiteNo("YZ00");
        n2.setCaseReason("末端网点未备案收寄件");
        n2.setOutletAddr(b.getOutletAddr());
        n2.setCorrectDeadline(b.getCorrectDeadline());
        n2.setReliefChannel(b.getReliefChannel());
        when(noticeMapper.selectList(any())).thenReturn(Collections.singletonList(n2));

        BillTraceVo vo = service.trace(1001L);
        assertFalse(vo.isVerified(), "第2轮接不住省局退回凭据，账对不上号");
    }

    private TPostPunishBill sealedBillRound1() {
        TPostPunishBill b = bill(BillFlowConstants.NODE_ISSUE, 1);
        b.setStatus(BillFlowConstants.BILL_PASSED);
        b.setSealed(1);
        b.setEndType(BillFlowConstants.END_SEALED);
        b.setProvinceStatus(BillFlowConstants.PROVINCE_SYNCED);
        b.setNeedCount(2);
        b.setSignCount(2);
        b.setSeasonGrade(2);
        b.setOutletAddr("青屏县棋盘路 7 号");
        b.setCorrectDeadline(new Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000));
        b.setReliefChannel("60日内复议");
        return b;
    }
}
