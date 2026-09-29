package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TPostPunishBill;
import com.fc.v2.model.custom.post.BillCosignCmd;
import com.fc.v2.model.custom.post.BillReceiveForm;
import com.fc.v2.model.custom.post.BillVerifyCmd;
import com.fc.v2.service.ITPostPunishBillService;
import com.fc.v2.shiro.util.ShiroUtils;
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
 * 违法行为查处逐级核签单 Controller（四开：受案/核查/并签/签发）。
 * 落名人一律取自登录会话，接口不收手工计数、不收代签人。
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

    @ApiOperation(value = "流转台账跳转", notes = "流转台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("postPunishBill:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "核签单流转台账", action = "list")
    @ApiOperation(value = "流转台账", notes = "流转台账")
    @GetMapping("/list")
    @RequiresPermissions("postPunishBill:list")
    @ResponseBody
    public ResultTable list(TPostPunishBill record) {
        QueryWrapper<TPostPunishBill> queryWrapper = new QueryWrapper<TPostPunishBill>()
                .eq("del_flag", 0)
                .orderByDesc("create_time");
        if (record.getBillNo() != null && !record.getBillNo().trim().isEmpty()) {
            queryWrapper.eq("bill_no", record.getBillNo().trim());
        }
        if (record.getSiteNo() != null && !record.getSiteNo().trim().isEmpty()) {
            queryWrapper.eq("site_no", record.getSiteNo().trim());
        }
        if (record.getNodeNo() != null) {
            queryWrapper.eq("node_no", record.getNodeNo());
        }
        if (record.getStatus() != null) {
            queryWrapper.eq("status", record.getStatus());
        }
        startPage();
        com.github.pagehelper.PageInfo<TPostPunishBill> page =
                new com.github.pagehelper.PageInfo<TPostPunishBill>(
                        postPunishBillService.selectTPostPunishBillList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "核签单头一开受案", action = "receive")
    @ApiOperation(value = "头一开·受案", notes = "字号/网点/事由/来源四要件齐才进门；当年第二件当场点名")
    @PostMapping("/receive")
    @RequiresPermissions("postPunishBill:receive")
    @ResponseBody
    public AjaxResult receive(BillReceiveForm form) {
        TPostPunishBill bill = postPunishBillService.receive(form, ShiroUtils.getLoginName());
        String tip = bill.getSerialInYear() != null && bill.getSerialInYear() >= 2
                ? "（当场点名：该企业本年度第" + bill.getSerialInYear() + "件）" : "";
        return AjaxResult.success(200, "受案进门，件已到核查开" + tip, bill);
    }

    @Log(title = "核签单第二开核查", action = "verify")
    @ApiOperation(value = "第二开·核查", notes = "只认字号不认人；未销案整改卡压着则停在本开")
    @PostMapping("/verify")
    @RequiresPermissions("postPunishBill:verify")
    @ResponseBody
    public AjaxResult verify(BillVerifyCmd cmd) {
        return AjaxResult.success(200, "核查放行，件已到并签开",
                postPunishBillService.verify(cmd, ShiroUtils.getLoginName()));
    }

    @Log(title = "核签单第三开并签落名", action = "cosign")
    @ApiOperation(value = "第三开·并签落名", notes = "角色2程序/3裁量；两名并排各记各的；计数由签迹点算")
    @PostMapping("/cosign")
    @RequiresPermissions("postPunishBill:cosign")
    @ResponseBody
    public AjaxResult cosign(BillCosignCmd cmd) {
        return AjaxResult.success(200, "并签落名已记下",
                postPunishBillService.cosign(cmd, ShiroUtils.getLoginName()));
    }

    @ApiOperation(value = "并签点算回数", notes = "页面计数与库存计数同一次点算给回")
    @GetMapping("/cosignCount")
    @RequiresPermissions("postPunishBill:list")
    @ResponseBody
    public AjaxResult cosignCount(Long id) {
        return AjaxResult.successData(200, postPunishBillService.cosignCount(id));
    }

    @Log(title = "核签单末一开签发", action = "issue")
    @ApiOperation(value = "末一开·签发", notes = "两笔点齐才盖章；同步省局名录并一键出整改确认单")
    @PostMapping("/issue")
    @RequiresPermissions("postPunishBill:issue")
    @ResponseBody
    public AjaxResult issue(Long id, String opinion) {
        return AjaxResult.success(200, "章盖到底，已报省局名录，整改确认单已出具",
                postPunishBillService.issue(id, opinion, ShiroUtils.getLoginName()));
    }

    @Log(title = "核签单止步", action = "stop")
    @ApiOperation(value = "止步", notes = "0不予立案 1撤案；件到此打住，事后补不进一笔")
    @PostMapping("/stop")
    @RequiresPermissions("postPunishBill:stop")
    @ResponseBody
    public AjaxResult stop(Long id, Integer endType, String opinion) {
        if (endType == null) {
            return AjaxResult.error("止步情形必填：0不予立案 1撤案");
        }
        return AjaxResult.success(200, "件已止步封存",
                postPunishBillService.stop(id, endType, opinion, ShiroUtils.getLoginName()));
    }

    @Log(title = "省局退回", action = "provinceReturn")
    @ApiOperation(value = "省局退回", notes = "先前名目作废沉底，从核查开重新往上累")
    @PostMapping("/provinceReturn")
    @RequiresPermissions("postPunishBill:provinceReturn")
    @ResponseBody
    public AjaxResult provinceReturn(Long id, String provinceOpinion) {
        return AjaxResult.success(200, "省局退回已登记，件回核查开重走一轮",
                postPunishBillService.provinceReturn(id, provinceOpinion, ShiroUtils.getLoginName()));
    }

    @ApiOperation(value = "倒序复核全貌", notes = "末一开倒数回受案，每一名目都经得起复核")
    @GetMapping("/trace")
    @RequiresPermissions("postPunishBill:list")
    @ResponseBody
    public AjaxResult trace(Long id) {
        return AjaxResult.successData(200, postPunishBillService.trace(id));
    }

    @ApiOperation(value = "对外整改确认单", notes = "取最新一轮已出具的确认单")
    @GetMapping("/notice")
    @RequiresPermissions("postPunishBill:list")
    @ResponseBody
    public AjaxResult notice(Long id) {
        return AjaxResult.successData(200, postPunishBillService.latestNotice(id));
    }
}
