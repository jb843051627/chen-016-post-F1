package com.fc.v2.service;

import com.fc.v2.model.auto.TPostPunishBill;
import com.fc.v2.model.auto.TPostRectifyNotice;
import com.fc.v2.model.custom.post.BillCosignCmd;
import com.fc.v2.model.custom.post.BillReceiveForm;
import com.fc.v2.model.custom.post.BillTraceVo;
import com.fc.v2.model.custom.post.BillVerifyCmd;
import com.fc.v2.model.custom.post.CosignCountVo;

import com.baomidou.mybatisplus.core.conditions.Wrapper;

import java.util.List;

/**
 * 违法行为查处逐级核签单 Service接口（approval-chain 形状：四开多签）。
 * 四开：受案 → 核查 → 并签（程序/裁量两名并排）→ 签发；只认开次，抢步不成。
 *
 * @author fuce
 * @date 2026-09-29
 */
public interface ITPostPunishBillService {

    /** 按主键回查单据 */
    TPostPunishBill selectTPostPunishBillById(Long id);

    /** 台账列表（分页由 PageHelper 拦） */
    List<TPostPunishBill> selectTPostPunishBillList(Wrapper<TPostPunishBill> queryWrapper);

    /**
     * 头一开·受案：企业字号、涉案网点、事由、来源四要件齐了才进门。
     * 同企业同年度第二件，serialInYear=2，由调用方当场点名。
     */
    TPostPunishBill receive(BillReceiveForm form, String operator);

    /**
     * 第二开·核查：在册底档按字号对看（申请人姓名不作数）；
     * 同字号同网点压着未销案整改事务卡的，停在核查；
     * 通过则带回企业全称快照、当季裁量格、网点地址、改正期限、救济途径，件进并签开。
     */
    TPostPunishBill verify(BillVerifyCmd cmd, String operator);

    /**
     * 第三开·并签落名：角色 2程序 3裁量，两名并排各记各的，同一分钟也算两笔；
     * 同人不得在本开落两笔；替人落名不收。两笔齐了件才进签发开。
     * 返回值中的计数由有效签迹同一次点算给回（库里 sign_count 同步回写）。
     */
    CosignCountVo cosign(BillCosignCmd cmd, String operator);

    /** 只点算不开口：页面计数与库存计数同源；接不平即本开未满。 */
    CosignCountVo cosignCount(Long id);

    /**
     * 末一开·签发：并签两笔点齐才盖得下去。
     * 章盖到底各栏封死；同步一份到省局名录；对外整改确认单一键由同一件带出。
     */
    TPostPunishBill issue(Long id, String opinion, String operator);

    /**
     * 止步：当前所在开写下不予立案(endType=0)或撤案(endType=1)，件到此打住，
     * 事后任何一口都补不进一笔。
     */
    TPostPunishBill stop(Long id, int endType, String opinion, String operator);

    /**
     * 省局退回：只收已封且已入名录的件。先前各轮名目一律作废沉底（不与新名目并列），
     * 名录名下移出，件回核查开重走一轮。
     */
    TPostPunishBill provinceReturn(Long id, String provinceOpinion, String operator);

    /** 倒序复核：末一开倒数回受案，四开名目一笔不差；已发确认单与件头同件比对。 */
    BillTraceVo trace(Long id);

    /** 取某一轮（默认最新轮）对外整改确认单；未出具返回 null。 */
    TPostRectifyNotice latestNotice(Long billId);
}
