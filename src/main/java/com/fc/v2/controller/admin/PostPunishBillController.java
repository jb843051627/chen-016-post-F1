package com.fc.v2.controller.admin;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TPostPunishBill;
import com.fc.v2.model.custom.OpResult;
import com.fc.v2.service.ITPostPunishBillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 违法行为查处逐级核签单 Controller（approval-chain 形状：四开台账）
 *
 * <p>头一开受案 → 第二开核查 → 第三开并签 → 末一开签发；
 * 止步/封死/退回/倒计/对外确认单各有专口，不提供普通增删改。</p>
 *
 * @author fuce
 * @date 2026-09-29
 */
@Api(value = "违法行为查处逐级核签单")
@Controller
@RequestMapping("/postPunishBill")
public class PostPunishBillController extends BaseController {

    private final String prefix = "admin/postPunishBill";

    @Autowired
    private ITPostPunishBillService postPunishBillService;

    private AjaxResult render(OpResult<?> r) {
        if (r == null) {
            return AjaxResult.error("操作无结果");
        }
        return r.isOk() ? AjaxResult.success(200, r.getMsg(), r.getData()) : AjaxResult.error(r.getMsg());
    }

    @ApiOperation(value = "核签台账跳转", notes = "核签台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("postPunishBill:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "核签单台账", action = "list")
    @ApiOperation(value = "台账分页查询", notes = "台账分页查询")
    @GetMapping("/list")
    @RequiresPermissions("postPunishBill:list")
    @ResponseBody
    public ResultTable list(TPostPunishBill query) {
        QueryWrapper<TPostPunishBill> qw = new QueryWrapper<TPostPunishBill>();
        if (query != null) {
            qw.eq(query.getBillNo() != null && !query.getBillNo().trim().isEmpty(), "bill_no", query.getBillNo());
            qw.eq(query.getSiteNo() != null && !query.getSiteNo().trim().isEmpty(), "site_no", query.getSiteNo());
            qw.eq(query.getOutletNo() != null && !query.getOutletNo().trim().isEmpty(), "outlet_no", query.getOutletNo());
            qw.eq(query.getNodeNo() != null, "node_no", query.getNodeNo());
            qw.eq(query.getStatus() != null, "status", query.getStatus());
        }
        startPage();
        com.github.pagehelper.PageInfo<TPostPunishBill> page =
                new com.github.pagehelper.PageInfo<TPostPunishBill>(
                        postPunishBillService.selectTPostPunishBillList(qw));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "办件工作台跳转", notes = "办件工作台跳转")
    @GetMapping("/workbench")
    @RequiresPermissions("postPunishBill:view")
    public String workbench(Long id, ModelMap model) {
        model.put("billId", id);
        return prefix + "/workbench";
    }

    @ApiOperation(value = "件详情", notes = "件详情")
    @GetMapping("/detail")
    @RequiresPermissions("postPunishBill:view")
    @ResponseBody
    public AjaxResult detail(Long id) {
        TPostPunishBill bill = postPunishBillService.selectTPostPunishBillById(id);
        if (bill == null) {
            return AjaxResult.error("件不存在");
        }
        return AjaxResult.successData(200, bill);
    }

    // ---------------------------------------------------------------- 头一开·受案

    @Log(title = "核签单受案", action = "accept")
    @ApiOperation(value = "头一开·受案", notes = "企业字号/涉案网点/事由/来源写齐才进门")
    @PostMapping("/accept")
    @RequiresPermissions("postPunishBill:accept")
    @ResponseBody
    public AjaxResult accept(TPostPunishBill form) {
        return render(postPunishBillService.accept(form));
    }

    // ---------------------------------------------------------------- 第二开·核查

    @Log(title = "核签单核查", action = "verify")
    @ApiOperation(value = "第二开·核查", notes = "只认字号；压件停核查；当季判据定格")
    @PostMapping("/verify")
    @RequiresPermissions("postPunishBill:verify")
    @ResponseBody
    public AjaxResult verify(Long id, BigDecimal input, String ruleCode, String outletAddr, String opinion) {
        return render(postPunishBillService.verify(id, input, ruleCode, outletAddr, opinion));
    }

    // ---------------------------------------------------------------- 第三开·并签

    @Log(title = "核签单并签", action = "cosign")
    @ApiOperation(value = "第三开·并签落一笔", notes = "1盯程序 2盯裁量；两名并排各记一笔")
    @PostMapping("/cosign")
    @RequiresPermissions("postPunishBill:cosign")
    @ResponseBody
    public AjaxResult cosign(Long id, Integer signRole, String signerName, Integer onBehalf, String opinion) {
        return render(postPunishBillService.cosign(id, signRole, signerName, onBehalf, opinion));
    }

    @ApiOperation(value = "并签点算", notes = "给目计数与库存计数同一次点算")
    @GetMapping("/count")
    @RequiresPermissions("postPunishBill:view")
    @ResponseBody
    public AjaxResult count(Long id) {
        return AjaxResult.successData(200, postPunishBillService.countSigns(id));
    }

    @ApiOperation(value = "当前签迹", notes = "顺着件自己的签迹看")
    @GetMapping("/signList")
    @RequiresPermissions("postPunishBill:view")
    @ResponseBody
    public AjaxResult signList(Long id) {
        return AjaxResult.successData(200, postPunishBillService.listCurrentSigns(id));
    }

    // ---------------------------------------------------------------- 末一开·签发

    @Log(title = "核签单签发", action = "seal")
    @ApiOperation(value = "末一开·签发", notes = "签章封死、同步省局名录、一键开出整改确认单")
    @PostMapping("/seal")
    @RequiresPermissions("postPunishBill:seal")
    @ResponseBody
    public AjaxResult seal(Long id, Date rectifyDeadline, String reliefWay, String opinion) {
        return render(postPunishBillService.seal(id, rectifyDeadline, reliefWay, opinion));
    }

    // ---------------------------------------------------------------- 止步 / 退回

    @Log(title = "核签单止步", action = "stop")
    @ApiOperation(value = "止步", notes = "1不予立案 2撤案，写下便打住")
    @PostMapping("/stop")
    @RequiresPermissions("postPunishBill:stop")
    @ResponseBody
    public AjaxResult stop(Long id, Integer stopKind, String opinion) {
        return render(postPunishBillService.stop(id, stopKind, opinion));
    }

    @Log(title = "省局退回", action = "bounce")
    @ApiOperation(value = "省局退回", notes = "旧轮名目沉底，从核查重新累")
    @PostMapping("/bounce")
    @RequiresPermissions("postPunishBill:bounce")
    @ResponseBody
    public AjaxResult bounce(Long id, String opinion) {
        return render(postPunishBillService.bounce(id, opinion));
    }

    // ---------------------------------------------------------------- 台账/倒计/确认单

    @ApiOperation(value = "当前轮四开明细分账", notes = "沉底旧轮不并列")
    @GetMapping("/nodes")
    @RequiresPermissions("postPunishBill:view")
    @ResponseBody
    public AjaxResult nodes(Long id) {
        return AjaxResult.successData(200, postPunishBillService.listCurrentNodes(id));
    }

    @ApiOperation(value = "倒计复核", notes = "从末一开倒着数回受案，一笔不差、每开可复核")
    @GetMapping("/trace")
    @RequiresPermissions("postPunishBill:trace")
    @ResponseBody
    public AjaxResult trace(Long id) {
        return AjaxResult.successData(200, postPunishBillService.trace(id));
    }

    @ApiOperation(value = "对外整改确认单数据", notes = "与核签单同源")
    @GetMapping("/confirm")
    @RequiresPermissions("postPunishBill:confirm")
    @ResponseBody
    public AjaxResult confirm(Long id) {
        Object c = postPunishBillService.currentConfirm(id);
        if (c == null) {
            return AjaxResult.error("还没开出去");
        }
        return AjaxResult.successData(200, c);
    }

    @ApiOperation(value = "对外整改确认单式样页", notes = "沿用现行文书式样")
    @GetMapping("/confirmView")
    @RequiresPermissions("postPunishBill:confirm")
    public String confirmView(Long id, ModelMap model) {
        model.put("billId", id);
        return prefix + "/confirm";
    }
}
