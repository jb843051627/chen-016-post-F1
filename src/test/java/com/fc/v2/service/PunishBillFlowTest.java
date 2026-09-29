package com.fc.v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
import com.fc.v2.model.auto.TPostPunishBill;
import com.fc.v2.model.auto.TPostPunishNode;
import com.fc.v2.model.auto.TPostPunishSign;
import com.fc.v2.model.auto.TPostRectifyCard;
import com.fc.v2.model.auto.TPostRectifyConfirm;
import com.fc.v2.model.custom.BillTraceVo;
import com.fc.v2.model.custom.OpResult;
import com.fc.v2.model.custom.SignCountVo;
import com.fc.v2.service.impl.TPostPunishBillServiceImpl;

/**
 * 核签四开状态机铁律的内存推演（不依赖 Spring / 数据库 / Shiro 容器）。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PunishBillFlowTest {

    @Mock private TPostPunishBillMapper billMapper;
    @Mock private TPostPunishNodeMapper nodeMapper;
    @Mock private TPostPunishSignMapper signMapper;
    @Mock private TPostCompanyArchMapper companyArchMapper;
    @Mock private TPostRectifyCardMapper rectifyCardMapper;
    @Mock private TPostOutletCardMapper outletCardMapper;
    @Mock private TPostRectifyConfirmMapper confirmMapper;
    @Mock private TPostProvinceRosterMapper rosterMapper;
    @Mock private ITPostQualityRuleService qualityRuleService;

    @InjectMocks
    private TPostPunishBillServiceImpl service;

    private final Map<Long, TPostPunishBill> bills = new HashMap<Long, TPostPunishBill>();
    private final List<TPostPunishNode> nodes = new ArrayList<TPostPunishNode>();
    private final List<TPostPunishSign> signs = new ArrayList<TPostPunishSign>();
    private final List<TPostRectifyConfirm> confirms = new ArrayList<TPostRectifyConfirm>();
    private final List<com.fc.v2.model.auto.TPostProvinceRoster> rosters = new ArrayList<com.fc.v2.model.auto.TPostProvinceRoster>();
    private long seq = 1000;

    @BeforeEach
    public void setUp() {
        // bill 内存表：insert 补雪花主键，selectById/updateById 直接回到同一对象
        doAnswer(inv -> {
            TPostPunishBill b = inv.getArgument(0);
            b.setId(++seq);
            bills.put(b.getId(), b);
            return 1;
        }).when(billMapper).insert(any());
        when(billMapper.selectById(any())).thenAnswer(inv -> bills.get(inv.getArgument(0)));
        when(billMapper.updateById(any())).thenReturn(1);
        // 重号检查 0；当年件数 1（所以新建即第二件，应被当场点名）
        when(billMapper.selectCount(any())).thenAnswer(inv -> {
            QueryWrapper<?> qw = inv.getArgument(0);
            return qw.getSqlSegment().contains("bill_no") ? 0 : 1;
        });

        doAnswer(inv -> {
            TPostPunishNode n = inv.getArgument(0);
            n.setId(++seq);
            nodes.add(n);
            return 1;
        }).when(nodeMapper).insert(any());
        when(nodeMapper.selectList(any())).thenAnswer(inv -> new ArrayList<TPostPunishNode>(nodes));
        when(nodeMapper.updateById(any())).thenReturn(1);

        doAnswer(inv -> {
            signs.add(inv.<TPostPunishSign>getArgument(0));
            return 1;
        }).when(signMapper).insert(any());
        when(signMapper.selectList(any())).thenAnswer(inv -> new ArrayList<TPostPunishSign>(signs));
        when(signMapper.updateById(any())).thenReturn(1);

        doAnswer(inv -> {
            confirms.add(inv.<TPostRectifyConfirm>getArgument(0));
            return 1;
        }).when(confirmMapper).insert(any());
        when(confirmMapper.selectList(any())).thenAnswer(inv -> new ArrayList<TPostRectifyConfirm>(confirms));
        when(confirmMapper.updateById(any())).thenReturn(1);
        when(confirmMapper.selectOne(any())).thenAnswer(inv -> {
            // 模拟 SQL：eq 顺序为 bill_id, round_no, del_flag，只取当前轮未删（沉底旧单查不到）
            QueryWrapper<?> qw = inv.getArgument(0);
            java.util.Collection<Object> args = qw.getParamNameValuePairs().values();
            Object[] arr = args.toArray();
            Long wantBill = arr.length > 0 ? ((Number) arr[0]).longValue() : null;
            Integer wantRound = arr.length > 1 ? ((Number) arr[1]).intValue() : null;
            for (int i = confirms.size() - 1; i >= 0; i--) {
                TPostRectifyConfirm c = confirms.get(i);
                if ((wantBill == null || wantBill.equals(c.getBillId()))
                        && (wantRound == null || wantRound.equals(c.getRoundNo()))
                        && (c.getDelFlag() == null || c.getDelFlag() == 0)) {
                    return c;
                }
            }
            return null;
        });
        when(rosterMapper.selectCount(any())).thenReturn(1);
        doAnswer(inv -> {
            rosters.add(inv.<com.fc.v2.model.auto.TPostProvinceRoster>getArgument(0));
            return 1;
        }).when(rosterMapper).insert(any());
        when(rosterMapper.selectList(any())).thenAnswer(inv -> new ArrayList<com.fc.v2.model.auto.TPostProvinceRoster>(rosters));
        when(rosterMapper.updateById(any())).thenReturn(1);

        // 在册底档：YZ00 在册
        TPostCompanyArch arch = new TPostCompanyArch();
        arch.setId(0L);
        arch.setSiteNo("YZ00");
        arch.setSiteName("青屏速递有限公司");
        arch.setStatus(0);
        when(companyArchMapper.selectOne(any())).thenReturn(arch);

        // 网点卡无冲突、整改卡默认无压件
        when(outletCardMapper.selectOne(any())).thenReturn(null);
        when(rectifyCardMapper.selectOne(any())).thenReturn(null);

        // 当季现行判据线：任意输入定到第 2 格
        when(qualityRuleService.evaluate(any(), any(), any())).thenReturn(2);
    }

    private TPostPunishBill acceptForm() {
        TPostPunishBill f = new TPostPunishBill();
        f.setSiteNo("YZ00");
        f.setOutletNo("WD-7");
        f.setOutletAddr("青屏县站前街 7 号");
        f.setCaseReason("末端网点未备案即收寄快件");
        f.setCaseSource("省局案卷评查");
        return f;
    }

    @Test
    public void happyPath_fourOpens_signCountAndSameSourceHold() {
        // 头一开：受案四要素齐，当年第二件被点名
        OpResult<TPostPunishBill> r0 = service.accept(acceptForm());
        assertTrue(r0.isOk(), r0.getMsg());
        assertTrue(r0.getMsg().contains("第 2 件"), "第二件应当场点名：" + r0.getMsg());
        TPostPunishBill bill = r0.getData();
        assertEquals(1, bill.getNodeNo().intValue(), "受案后停在核查开");

        // 少一样进不了门
        TPostPunishBill bad = acceptForm();
        bad.setCaseSource(null);
        assertFalse(service.accept(bad).isOk());

        // 第二开：核查只认字号、当季定格
        OpResult<TPostPunishBill> r1 = service.verify(bill.getId(), new BigDecimal("120"),
                "POST2026", null, "材料齐");
        assertTrue(r1.isOk(), r1.getMsg());
        assertEquals(2, bill.getNodeNo().intValue());
        assertEquals(2, bill.getGradeLevel().intValue());
        assertEquals("POST2026", bill.getRuleCode());

        // 第三开：程序笔
        OpResult<SignCountVo> r2 = service.cosign(bill.getId(), 1, "甲", 0, "程序齐全");
        assertTrue(r2.isOk());
        assertFalse(r2.getData().getComplete(), "只到一名，件仍卡并签");
        assertEquals(1, bill.getSignCount().intValue(), "顺着签迹点算只应有一笔");
        assertEquals(2, bill.getNodeNo().intValue(), "没点齐不能见签发");

        // 同一人不能顶两笔
        assertFalse(service.cosign(bill.getId(), 2, "甲", 0, null).isOk());

        // 替人落的一笔不作数
        OpResult<SignCountVo> rb = service.cosign(bill.getId(), 2, "乙", 1, null);
        assertTrue(rb.isOk());
        assertEquals(1, rb.getData().getViewCount().intValue(), "代签不计入点算");
        assertEquals(1, rb.getData().getOnBehalfCount().intValue());

        // 本人补落裁量笔 → 两笔点齐进签发
        OpResult<SignCountVo> r3 = service.cosign(bill.getId(), 2, "乙", 0, "裁量合当季格");
        assertTrue(r3.isOk(), r3.getMsg());
        assertTrue(r3.getData().getComplete());
        assertTrue(r3.getData().getConsistent(), "给目计数与库存计数须同一次点算接平");
        assertEquals(3, bill.getNodeNo().intValue());

        // 抢步：还停在并签的件签不了发（新开件验）
        TPostPunishBill other = service.accept(acceptForm()).getData();
        assertFalse(service.seal(other.getId(), new java.util.Date(), "60日内复议", null).isOk());

        // 末一开：签章封死 + 省局名录 + 同源确认单
        java.util.Date deadline = new java.util.Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000);
        OpResult<TPostPunishBill> r4 = service.seal(bill.getId(), deadline, "60日内向省局申请复议", null);
        assertTrue(r4.isOk(), r4.getMsg());
        assertEquals(1, bill.getSealed().intValue());
        assertEquals(1, bill.getStatus().intValue());
        assertNotNull(bill.getOutConfirmNo());
        assertEquals(1, confirms.size());
        TPostRectifyConfirm c = confirms.get(0);
        assertEquals(bill.getSiteNo(), c.getSiteNo());
        assertEquals(bill.getOutletAddr(), c.getOutletAddr());
        assertEquals(bill.getCaseReason(), c.getCaseReason());
        assertEquals(deadline, c.getRectifyDeadline());

        // 封死后补不进一笔
        assertFalse(service.verify(bill.getId(), new BigDecimal("1"), null, null, null).isOk());
        assertFalse(service.stop(bill.getId(), 1, null).isOk());

        // 倒计：末一开→受案一笔不差，每开经得起复核，同源一致
        BillTraceVo trace = service.trace(bill.getId());
        assertTrue(trace.getAllRecheckPass(), trace.getProblems().toString());
        assertTrue(trace.getRosterSynced());
        assertEquals(Boolean.TRUE, trace.getConfirmSameSource());
        assertEquals(4, trace.getNodes().size(), "倒着数齐四开");
        assertEquals(3, trace.getNodes().get(0).getNodeNo().intValue(), "倒序头一个是签发");
        assertEquals(0, trace.getNodes().get(3).getNodeNo().intValue(), "倒序末一个是受案");
    }

    @Test
    public void blockingRectifyCard_stopsAtVerify() {
        TPostRectifyCard card = new TPostRectifyCard();
        card.setBizNo("ZG-整改-9");
        card.setStatus(1); // 在办未销案
        when(rectifyCardMapper.selectOne(any())).thenReturn(card);

        TPostPunishBill bill = service.accept(acceptForm()).getData();
        OpResult<TPostPunishBill> r = service.verify(bill.getId(), new BigDecimal("50"), null, null, null);
        assertFalse(r.isOk(), "压着未结整改卡必须停在核查");
        assertTrue(r.getMsg().contains("ZG-整改-9"));
        assertEquals(1, bill.getNodeNo().intValue(), "往后再齐也不往前走");
        assertEquals("ZG-整改-9", bill.getBlockedBy());
    }

    @Test
    public void stopThenNoMoreActions() {
        TPostPunishBill bill = service.accept(acceptForm()).getData();
        assertTrue(service.stop(bill.getId(), 1, "证据不足").isOk());
        assertEquals(3, bill.getStatus().intValue());
        // 事后谁也补不进一笔
        assertFalse(service.verify(bill.getId(), new BigDecimal("1"), null, null, null).isOk());
        assertFalse(service.cosign(bill.getId(), 1, "甲", 0, null).isOk());
        assertFalse(service.stop(bill.getId(), 2, null).isOk());

        BillTraceVo trace = service.trace(bill.getId());
        assertTrue(trace.getAllRecheckPass(), "止步件的核查开按止步凭据复核：" + trace.getProblems());
    }

    @Test
    public void unknownSiteNo_rejectedAtAccept() {
        when(companyArchMapper.selectOne(any())).thenReturn(null);
        assertFalse(service.accept(acceptForm()).isOk());
    }

    @Test
    public void provinceReturn_sinksOldRoundAndRestartsAtVerify() {
        // 走完头一趟四开并封死
        TPostPunishBill bill = service.accept(acceptForm()).getData();
        service.verify(bill.getId(), new BigDecimal("120"), "POST2026", null, null);
        service.cosign(bill.getId(), 1, "甲", 0, null);
        service.cosign(bill.getId(), 2, "乙", 0, null);
        java.util.Date d1 = new java.util.Date(System.currentTimeMillis() + 86400000L);
        assertTrue(service.seal(bill.getId(), d1, "60日内复议", null).isOk());
        assertEquals(1, bill.getStatus().intValue());

        // 省局退回：旧轮沉底，从核查重新累
        OpResult<TPostPunishBill> rb = service.bounce(bill.getId(), "措辞不合规矩");
        assertTrue(rb.isOk(), rb.getMsg());
        assertEquals(1, bill.getBouncedRound().intValue());
        assertEquals(1, bill.getNodeNo().intValue(), "回到核查开");
        assertEquals(2, bill.getStatus().intValue(), "挂已打回");
        assertEquals(0, bill.getSealed().intValue(), "章随退回解开");
        assertNull(bill.getGradeLevel());
        assertNull(bill.getRuleCode());
        assertNull(bill.getVerifyInput());
        assertNull(bill.getOutConfirmNo());
        assertTrue(bill.getRemark().contains("沉底"));

        // 第二趟重新往上累：核查可再过
        assertTrue(service.verify(bill.getId(), new BigDecimal("60"), "POST2026", null, "补正后").isOk());
        assertEquals(2, bill.getNodeNo().intValue());
        // 已打回、第二趟尚未重新签发期间，旧轮确认单已沉底，当前轮无单可查
        assertNull(service.currentConfirm(bill.getId()));
    }
}
