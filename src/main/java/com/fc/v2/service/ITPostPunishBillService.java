package com.fc.v2.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.model.auto.TPostPunishBill;
import com.fc.v2.model.auto.TPostPunishNode;
import com.fc.v2.model.auto.TPostPunishSign;
import com.fc.v2.model.auto.TPostRectifyConfirm;
import com.fc.v2.model.custom.BillTraceVo;
import com.fc.v2.model.custom.OpResult;
import com.fc.v2.model.custom.SignCountVo;

/**
 * 违法行为查处逐级核签单 Service接口（approval-chain 形状：四开台账，无普通增删改查入口）
 *
 * <p>四开：0受案 1核查 2并签 3签发。件的推进只认开次，抢步不算过成；
 * 在哪开写下不予立案/撤案便止在哪开；章盖到底各栏封死，要改另起一件；
 * 省局退回，先前各开名目一律作废沉底，从核查重新累。</p>
 *
 * @author fuce
 * @date 2026-09-14
 */
public interface ITPostPunishBillService {

    /** 按主键回查单据 */
    TPostPunishBill selectTPostPunishBillById(Long id);

    /** 台账列表（只列未删） */
    List<TPostPunishBill> selectTPostPunishBillList(QueryWrapper<TPostPunishBill> queryWrapper);

    /**
     * 头一开·受案：企业字号、涉案网点、事由、来源四样写齐才进门；
     * 只认在册底档字号；同一企业当年第二件起当场点名（结果消息里给回）。
     */
    OpResult<TPostPunishBill> accept(TPostPunishBill form);

    /**
     * 第二开·核查：在册底档对现场材料，只认字号不认申请人姓名；
     * 网点压着未结整改事务卡则停在核查（不前进、不落名）；
     * 过得去则按当季现行判据线定住裁量格次，推入并签。
     *
     * @param input      现场材料数值（送当季判据线定格）
     * @param ruleCode   本件依的判据线代号（当季现行；空则取顺位最高者）
     * @param outletAddr 现场核对带回的网点地址（对外确认单同源要素，可空）
     */
    OpResult<TPostPunishBill> verify(Long id, java.math.BigDecimal input, String ruleCode,
                                     String outletAddr, String opinion);

    /**
     * 第三开·并签落一笔：role 1盯程序 2盯裁量。两笔账各记各的签迹，
     * 掐同一分钟也算两笔；替人落名(onBehalf=1)不作数；同一人不能顶两笔。
     * 返回的应签/已签与库里回写来自同一次点算；两笔齐了才推入签发。
     */
    OpResult<SignCountVo> cosign(Long id, Integer signRole, String signerName, Integer onBehalf, String opinion);

    /** 并签开计数给目：与落库回写同一次点算口径，页面不设手工填数格 */
    SignCountVo countSigns(Long id);

    /** 本开各条签迹（顺着件自己的签迹数） */
    List<TPostPunishSign> listCurrentSigns(Long id);

    /**
     * 末一开·签发：签章封死各栏，同步一份到省局名录，
     * 并把核签结论一键转成对外整改确认单（字号/网点地址/事由/改正期限/救济途径同件带出）。
     */
    OpResult<TPostPunishBill> seal(Long id, java.util.Date rectifyDeadline, String reliefWay, String opinion);

    /** 止步：在当前开写下 1不予立案 2撤案，件到此打住，事后谁也补不进一笔 */
    OpResult<TPostPunishBill> stop(Long id, Integer stopKind, String opinion);

    /** 省局退回：先前各轮名目作废沉底，从核查开重新往上累；作废与后补不并列 */
    OpResult<TPostPunishBill> bounce(Long id, String opinion);

    /** 当前轮四开明细分账（核查/签发页看落名凭据用） */
    List<TPostPunishNode> listCurrentNodes(Long id);

    /** 从末一开倒着数回受案：各级名目一笔不差 + 每开复核结论，须与当日屏幕记载对得上号 */
    BillTraceVo trace(Long id);

    /** 取当前轮开出去的对外整改确认单（与核签单同源）；未签发返回 null */
    TPostRectifyConfirm currentConfirm(Long id);
}
