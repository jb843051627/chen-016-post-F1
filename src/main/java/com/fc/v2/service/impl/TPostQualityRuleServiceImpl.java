package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TPostQualityRuleMapper;
import com.fc.v2.model.auto.TPostQualityRule;
import com.fc.v2.service.ITPostQualityRuleService;

/**
 * 服务质量达标率判据线 Service业务层处理（rule-eval 形状）
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TPostQualityRuleServiceImpl implements ITPostQualityRuleService {

    /** 现行 */
    private static final int STATUS_ACTIVE = 0;

    /** 优先级降序；并列按 ruleCode 降序（确定性） */
    private static final Comparator<TPostQualityRule> PRIORITY_THEN_CODE_DESC = new Comparator<TPostQualityRule>() {
        @Override
        public int compare(TPostQualityRule a, TPostQualityRule b) {
            int pa = a.getPriority() == null ? 0 : a.getPriority();
            int pb = b.getPriority() == null ? 0 : b.getPriority();
            if (pa != pb) {
                return pb - pa;
            }
            String ca = a.getRuleCode() == null ? "" : a.getRuleCode();
            String cb = b.getRuleCode() == null ? "" : b.getRuleCode();
            return cb.compareTo(ca);
        }
    };

    @javax.annotation.Resource
    private TPostQualityRuleMapper postQualityRuleMapper;

    /**
     * 时段比较统一走**墙钟字符串**（yyyy-MM-dd HH:mm:ss）+ 字符串绑定：
     * JDBC 的 serverTimezone 与本机时区不对称，直接把 java.util.Date 作参数会整体偏移，
     * 使边界判定在"切换当日"这类用例上随机错档（实测踩到）。
     */
    private static String ts(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
    }

    @Override
    public TPostQualityRule selectTPostQualityRuleById(Long id) {
        return this.postQualityRuleMapper.selectById(id);
    }

    @Override
    public List<TPostQualityRule> listAvailable(Date at) {
        if (at == null) {
            return new ArrayList<TPostQualityRule>();
        }
        QueryWrapper<TPostQualityRule> qw = new QueryWrapper<TPostQualityRule>()
                .eq("del_flag", 0)
                .eq("status", STATUS_ACTIVE)
                // 启用之日 <= at（未填启用日视为早已启用）
                .and(w -> w.isNull("eff_start").or().le("eff_start", ts(at)))
                // 交棒之日不含：eff_end > at（未填交棒日视为长期现行）
                .and(w -> w.isNull("eff_end").or().gt("eff_end", ts(at)));
        List<TPostQualityRule> rows = this.postQualityRuleMapper.selectList(qw);
        rows.sort(PRIORITY_THEN_CODE_DESC);
        return rows;
    }

    @Override
    public int evaluate(String ruleCode, BigDecimal input, Date at) {
        if (ruleCode == null || ruleCode.trim().isEmpty() || input == null || at == null) {
            return 0;
        }
        TPostQualityRule rule = findRule(ruleCode, at);
        return levelOf(rule, input);
    }

    @Override
    public int evaluateTop(BigDecimal input, Date at) {
        if (input == null || at == null) {
            return 0;
        }
        if (input.compareTo(BigDecimal.ZERO) < 0
                || input.compareTo(new BigDecimal("100")) > 0) {
            return 0;
        }
        List<TPostQualityRule> avail = listAvailable(at);
        if (avail.isEmpty()) {
            return 0;
        }
        return levelOf(avail.get(0), input);
    }

    @Override
    public boolean usable(Long id, Date at) {
        if (id == null || at == null) {
            return false;
        }
        TPostQualityRule r = this.postQualityRuleMapper.selectById(id);
        return r != null
                && (r.getDelFlag() == null || r.getDelFlag() == 0)
                && r.getStatus() != null && r.getStatus() == STATUS_ACTIVE
                && windowContains(r, at);
    }

    @Override
    public int countAvailable(Date at) {
        if (at == null) {
            return 0;
        }
        QueryWrapper<TPostQualityRule> qw = new QueryWrapper<TPostQualityRule>()
                .eq("del_flag", 0)
                .eq("status", STATUS_ACTIVE)
                .and(w -> w.isNull("eff_start").or().le("eff_start", ts(at)))
                .and(w -> w.isNull("eff_end").or().gt("eff_end", ts(at)));
        Integer cnt = this.postQualityRuleMapper.selectCount(qw);
        return cnt == null ? 0 : cnt;
    }

    /** 定位某代号在 at 时刻现行的版本：只在生效窗口内找，窗口内多于一条时让线顺位高者说话 */
    private TPostQualityRule findRule(String ruleCode, Date at) {
        TPostQualityRule hit = null;
        for (TPostQualityRule r : listAvailable(at)) {
            if (ruleCode.equals(r.getRuleCode())) {
                if (hit == null || PRIORITY_THEN_CODE_DESC.compare(r, hit) < 0) {
                    hit = r;
                }
            }
        }
        return hit;
    }

    /** 是否落在 [eff_start, eff_end) 窗口；空边界视为不限 */
    private boolean windowContains(TPostQualityRule r, Date at) {
        if (r.getEffStart() != null && at.before(r.getEffStart())) {
            return false;
        }
        return r.getEffEnd() == null || at.before(r.getEffEnd());
    }

    /** 分档：等于上限取高一档 */
    private int levelOf(TPostQualityRule rule, BigDecimal input) {
        if (rule == null) {
            return 0;
        }
        if (rule.getTh1Max() != null && input.compareTo(rule.getTh1Max()) <= 0) {
            return 1;
        }
        if (rule.getTh2Max() != null && input.compareTo(rule.getTh2Max()) <= 0) {
            return 2;
        }
        if (rule.getTh3Max() != null && input.compareTo(rule.getTh3Max()) <= 0) {
            return 3;
        }
        return 4;
    }
}
