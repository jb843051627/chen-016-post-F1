-- post 邮政业市场监管与快递末端服务监督 -- schema (chen-016)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_016

CREATE TABLE IF NOT EXISTS t_post_company_arch (
  id bigint NOT NULL COMMENT '主键',
  site_no varchar(64) DEFAULT NULL COMMENT '经营企业在册底档字号',
  site_name varchar(128) DEFAULT NULL COMMENT '企业全称',
  site_type varchar(32) DEFAULT NULL COMMENT '许可类别',
  road_name varchar(128) DEFAULT NULL COMMENT '管辖邮政管理局(省—市—县)',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '单卡日均件量低界(件/日)',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '中界(件/日)',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '高界(件/日)',
  status int DEFAULT NULL COMMENT '底档情形 0在册 1已注销',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经营企业在册底档';

CREATE TABLE IF NOT EXISTS t_post_due_task (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '告知事项号',
  due_at datetime DEFAULT NULL COMMENT '该动手那一日的止点时刻',
  amount decimal(12,2) DEFAULT NULL COMMENT '提前几日开口告知',
  content varchar(255) DEFAULT NULL COMMENT '事由与被告知企业记要',
  status int DEFAULT NULL COMMENT '条目情形 0未开口 1已送达 2送不下去',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经营许可效期与备案年度核验到期告知事项';

CREATE TABLE IF NOT EXISTS t_post_outlet_card (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '末端网点备案建档卡号',
  node_no int DEFAULT NULL COMMENT '本卡版本序',
  site_id int DEFAULT NULL COMMENT '所属企业在册底档',
  site_no varchar(64) DEFAULT NULL COMMENT '所属企业底档字号',
  qty decimal(12,2) DEFAULT NULL COMMENT '本卡网点日均处理件数(件/日)',
  fine_amt decimal(12,2) DEFAULT NULL COMMENT '本卡报备营业面积(平方米)',
  content varchar(255) DEFAULT NULL COMMENT '核验串（随版本序走）',
  grade_level int DEFAULT NULL COMMENT '日均件量折到的服务序位',
  status int DEFAULT NULL COMMENT '进展 0待核对 1已核对 2已冻住',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='快递末端网点备案建档卡';

CREATE TABLE IF NOT EXISTS t_post_punish_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '查处核签单号',
  node_no int DEFAULT NULL COMMENT '当前所在开次 0..3（0受案 1核查 2并签 3签发）',
  sign_mode int DEFAULT NULL COMMENT '同关并签办法 0任一人 1两名点齐',
  need_count int DEFAULT NULL COMMENT '本开应签人数（由签迹点算口径带出，不设手填）',
  sign_count int DEFAULT NULL COMMENT '本开已签人数（与给人过目计数同一次点算回写）',
  -- 受案四开台账要素（头一开：企业字号/涉案网点/事由/来源，少一样进不了门）
  site_no varchar(64) DEFAULT NULL COMMENT '企业字号（在册底档字号）',
  site_name varchar(128) DEFAULT NULL COMMENT '企业全称（受案时自底档带住）',
  outlet_no varchar(64) DEFAULT NULL COMMENT '涉案网点（末端网点备案建档卡号）',
  outlet_addr varchar(255) DEFAULT NULL COMMENT '涉案网点地址',
  case_reason varchar(500) DEFAULT NULL COMMENT '事由',
  case_source varchar(128) DEFAULT NULL COMMENT '来源',
  -- 核查/裁量定格
  site_id bigint DEFAULT NULL COMMENT '在册底档主键（只认字号对底档，不认申请人姓名）',
  applicant_name varchar(64) DEFAULT NULL COMMENT '申请人姓名（仅留痕，不作核对凭据）',
  rule_code varchar(64) DEFAULT NULL COMMENT '本件裁量所依当季判据线代号',
  grade_level int DEFAULT NULL COMMENT '裁量幅度所定格次（当季那一格）',
  blocked_by varchar(64) DEFAULT NULL COMMENT '压件未结整改事务卡号（卡着则件停在核查）',
  verify_input decimal(12,2) DEFAULT NULL COMMENT '核查开现场材料数值（当季判据定格输入，留作倒计复核）',
  -- 止步/封住/退回
  stop_kind int DEFAULT NULL COMMENT '止步名目 1不予立案 2撤案（在哪开写下便止在哪开）',
  sealed int DEFAULT '0' COMMENT '章是否盖到底 0未盖 1已盖（各栏当即封死）',
  bounced_round int DEFAULT '0' COMMENT '省局打回后重新累的轮次（沉底名目不与后补并列）',
  status int DEFAULT NULL COMMENT '报批情形 0在核 1已核讫 2已打回 3已止步（不予立案/撤案）',
  -- 一键转单同源要素
  rectify_deadline datetime DEFAULT NULL COMMENT '责令改正期限（对外确认单同件带出）',
  relief_way varchar(500) DEFAULT NULL COMMENT '救济途径（对外确认单同件带出）',
  out_confirm_no varchar(64) DEFAULT NULL COMMENT '已开出去的整改确认单号（同件存根）',
  year_no int DEFAULT NULL COMMENT '同一企业当年第几件（第二件起当场点名）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_bill_no (bill_no),
  KEY idx_site_year (site_no, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='违法行为查处逐级核签单';

-- 四开各开明细分账：每过一开落一行；打回后旧轮次各开一律作废沉底（valid_flag=0），不与后补并列。
CREATE TABLE IF NOT EXISTS t_post_punish_node (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint NOT NULL COMMENT '核签单主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '核签单号',
  node_no int NOT NULL COMMENT '开次 0受案 1核查 2并签 3签发',
  round_no int NOT NULL DEFAULT '0' COMMENT '轮次（首趟0；省局打回重走累加）',
  valid_flag int NOT NULL DEFAULT '1' COMMENT '本行名目是否作数 1现行 0已作废沉底',
  actor_name varchar(64) DEFAULT NULL COMMENT '本开落名（受案/核查/签发各一记）',
  sign_role int DEFAULT NULL COMMENT '并签两笔账分工 1盯程序 2盯裁量（仅并签开）',
  opinion varchar(500) DEFAULT NULL COMMENT '本开记事/意见',
  stop_kind int DEFAULT NULL COMMENT '本开写下的止步名目 1不予立案 2撤案',
  sealed int DEFAULT '0' COMMENT '本开是否随签发封死',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_bill_round (bill_id, round_no, valid_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='核签单四开明细分账';

-- 并签签迹：两名并排落名各记各的，掐同一分钟也算两笔；件顺着自己的签迹一段段数出已签数，页面无手工填数格。
CREATE TABLE IF NOT EXISTS t_post_punish_sign (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint NOT NULL COMMENT '核签单主键',
  node_id bigint NOT NULL COMMENT '所属并签开明细行',
  sign_role int NOT NULL COMMENT '这笔账盯什么 1程序走没走全 2裁量幅度合不合当季那一格',
  signer_name varchar(64) NOT NULL COMMENT '落名人（替人落的一笔不作数：on_behalf=1 不计）',
  on_behalf int NOT NULL DEFAULT '0' COMMENT '是否替人落名 0本人 1代签（代签不计入点算）',
  valid_flag int NOT NULL DEFAULT '1' COMMENT '该签迹是否作数 1作数 0作废（随打回沉底）',
  round_no int NOT NULL DEFAULT '0' COMMENT '签时轮次',
  sign_time datetime NOT NULL COMMENT '落名时刻',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  -- 同一角色同轮是否已落本人名由服务层顺着签迹点算判（代签笔不占位，不能用唯一键挡掉补签）
  KEY idx_bill_count (bill_id, valid_flag, sign_role, round_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='并签签迹（计数凭据，只许顺着数）';

-- 末一开签章后同步省局名录的存根。
CREATE TABLE IF NOT EXISTS t_post_province_roster (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint NOT NULL COMMENT '核签单主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '核签单号',
  site_no varchar(64) DEFAULT NULL COMMENT '企业字号',
  site_name varchar(128) DEFAULT NULL COMMENT '企业全称',
  outlet_no varchar(64) DEFAULT NULL COMMENT '涉案网点',
  sync_time datetime DEFAULT NULL COMMENT '同步时刻（签章同步，一刻不分先后也同件带住）',
  round_no int DEFAULT '0' COMMENT '同步时轮次',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_bill (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='省局名录同步存根';

-- 一键转出的对外整改确认单存根：字号/网点地址/事由/责令改正期限/救济途径全从同一件带出，不另请人抄第二遍。
CREATE TABLE IF NOT EXISTS t_post_rectify_confirm (
  id bigint NOT NULL COMMENT '主键',
  confirm_no varchar(64) NOT NULL COMMENT '整改确认单号',
  bill_id bigint NOT NULL COMMENT '来源核签单主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '来源核签单号',
  site_no varchar(64) DEFAULT NULL COMMENT '企业字号（同源）',
  site_name varchar(128) DEFAULT NULL COMMENT '企业全称（同源）',
  outlet_no varchar(64) DEFAULT NULL COMMENT '涉案网点（同源）',
  outlet_addr varchar(255) DEFAULT NULL COMMENT '网点地址（同源）',
  case_reason varchar(500) DEFAULT NULL COMMENT '事由（同源）',
  rectify_deadline datetime DEFAULT NULL COMMENT '责令改正期限（同源）',
  relief_way varchar(500) DEFAULT NULL COMMENT '救济途径（同源）',
  issue_time datetime DEFAULT NULL COMMENT '开具时刻（末一开发出）',
  round_no int NOT NULL DEFAULT '0' COMMENT '所属轮次（省局打回后旧版沉底，只认当前轮）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_confirm_no (confirm_no),
  KEY idx_bill (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对外整改确认单存根（与核签单同源不重抄）';

CREATE TABLE IF NOT EXISTS t_post_quality_rule (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(64) DEFAULT NULL COMMENT '服务质量判据代号',
  rule_name varchar(128) DEFAULT NULL COMMENT '线名',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '低界达标率(百分比)',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '中界达标率(百分比)',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '高界达标率(百分比)',
  eff_start datetime DEFAULT NULL COMMENT '启用之日',
  eff_end datetime DEFAULT NULL COMMENT '交棒之日(不含)',
  priority int DEFAULT NULL COMMENT '让线顺位(数值大的先说话)',
  status int DEFAULT NULL COMMENT '线的情形 0现行 1已让位',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务质量达标率判据线';

CREATE TABLE IF NOT EXISTS t_post_rectify_card (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(64) DEFAULT NULL COMMENT '责令整改事务卡号',
  outlet_no varchar(64) DEFAULT NULL COMMENT '压着的末端网点备案建档卡号（没了结则该网点的件停在核查开）',
  stage int DEFAULT NULL COMMENT '当前段次 0..3（下达/整改中/复查/销案）',
  status int DEFAULT NULL COMMENT '卡的落点 0未起 1在办 2已销案',
  content varchar(255) DEFAULT NULL COMMENT '整改记事',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次过口动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='责令整改事务卡';

CREATE TABLE IF NOT EXISTS t_post_report_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '月度业务报表报送批次号',
  row_no int DEFAULT NULL COMMENT '原报表行次',
  item_code varchar(64) DEFAULT NULL COMMENT '被挑出的前置项代号',
  qty decimal(12,2) DEFAULT NULL COMMENT '本行报送业务量(万件)',
  status int DEFAULT NULL COMMENT '行落地情形 0待核 1已收 2挂起',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='月度业务报表核收行';

-- 初始底档数据（在册、已注销两类各一条）
-- t_post_company_arch 两条种子底档：id=0 在册、id=1 已注销。
-- 单卡日均件数三线上限取 30/90/240 件（等于上限算高一序位）。
INSERT IGNORE INTO t_post_company_arch (id, site_no, site_name, site_type, road_name, th1_max, th2_max, th3_max, status, del_flag, create_by, create_time)
VALUES (0, 'YZ00', '青屏市在册企业底档（青屏速递有限公司，在册）', '省内经营', '青屏省—青屏市—青屏县', 30.00, 90.00, 240.00, 0, 0, 'seed', NOW()),
       (1, 'YZ01', '青屏市在册企业底档（云驿快递服务有限公司，已注销）', '省内经营', '青屏省—青屏市—青屏县', 30.00, 90.00, 240.00, 1, 0, 'seed', NOW());


-- ====================================================================
-- 当季现行服务质量判据线种子（核查开「当季那一格」要有线可依）
-- 2026 年度现行：阈值 30/90/240，未填交棒日视为长期现行；等于上限取高一格。
-- ====================================================================
INSERT IGNORE INTO t_post_quality_rule
  (id, rule_code, rule_name, th1_max, th2_max, th3_max, eff_start, eff_end, priority, status, del_flag, create_by, create_time)
VALUES
  (9001, 'POST2026', '2026年当季快递末端服务质量判据线', 30.00, 90.00, 240.00,
   '2026-01-01 00:00:00', NULL, 10, 0, 0, 'seed', NOW());

-- ====================================================================
-- 菜单与权限种子（t_sys_permission）：邮政监管目录 / 核签台账菜单 / 各开按钮
-- 部署后按角色在「权限管理」里勾选授权；超管如需直接可见，可给角色补 '*' 或这些 perms。
-- ====================================================================
INSERT IGNORE INTO t_sys_permission (id, name, descripion, url, is_blank, pid, perms, type, icon, order_num, visible, create_by, create_time)
VALUES
  (9100, '邮政市场监管', '邮政业市场监管与快递末端监督目录', '', 0, 0, '', 0, 'layui-icon layui-icon-vercode', 20, 0, 'seed', NOW()),
  (9101, '查处核签台账', '违法行为查处逐级核签台账（四开）', '/postPunishBill/view', 0, 9100, 'postPunishBill:view', 1, 'layui-icon layui-icon-form', 1, 0, 'seed', NOW()),
  (9102, '核签台账集合', '核签台账分页查询', '/postPunishBill/list', 0, 9101, 'postPunishBill:list', 2, '', NULL, 0, 'seed', NOW()),
  (9103, '头一开·受案', '受案进门', '/postPunishBill/accept', 0, 9101, 'postPunishBill:accept', 2, 'layui-icon layui-icon-add-1', NULL, 0, 'seed', NOW()),
  (9104, '第二开·核查', '在册底档核对与当季定格', '/postPunishBill/verify', 0, 9101, 'postPunishBill:verify', 2, 'layui-icon layui-icon-refresh', NULL, 0, 'seed', NOW()),
  (9105, '第三开·并签', '程序/裁量两笔账并排落名', '/postPunishBill/cosign', 0, 9101, 'postPunishBill:cosign', 2, 'layui-icon layui-icon-edit', NULL, 0, 'seed', NOW()),
  (9106, '末一开·签发', '签章封死、同步省局、开确认单', '/postPunishBill/seal', 0, 9101, 'postPunishBill:seal', 2, 'layui-icon layui-icon-auz', NULL, 0, 'seed', NOW()),
  (9107, '止步', '不予立案/撤案', '/postPunishBill/stop', 0, 9101, 'postPunishBill:stop', 2, 'layui-icon layui-icon-close', NULL, 0, 'seed', NOW()),
  (9108, '省局退回', '旧轮沉底从核查重累', '/postPunishBill/bounce', 0, 9101, 'postPunishBill:bounce', 2, 'layui-icon layui-icon-return', NULL, 0, 'seed', NOW()),
  (9109, '倒计复核', '末一开倒着数回受案', '/postPunishBill/trace', 0, 9101, 'postPunishBill:trace', 2, 'layui-icon layui-icon-log', NULL, 0, 'seed', NOW()),
  (9110, '整改确认单', '查看/打印对外整改确认单', '/postPunishBill/confirmView', 0, 9101, 'postPunishBill:confirm', 2, 'layui-icon layui-icon-print', NULL, 0, 'seed', NOW());
