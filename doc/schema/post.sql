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

-- 违法行为查处逐级核签单：四开（0受案/1核查/2并签/3签发）状态机。
-- 件的推进只认开次：node_no 指向当前所在开，前一开不满足，后一开不接件。
CREATE TABLE IF NOT EXISTS t_post_punish_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '查处核签单号',
  node_no int DEFAULT NULL COMMENT '当前所在开次 0受案 1核查 2并签 3签发',
  sign_mode int DEFAULT NULL COMMENT '同关并签办法 0任一人 1两名点齐（并签开恒为1）',
  need_count int DEFAULT NULL COMMENT '本开应签人数（并签开恒为2，由并签办法点定，不接受手填）',
  sign_count int DEFAULT NULL COMMENT '本开已签人数（由有效签迹同一次点算回写，页面不设填数格）',
  status int DEFAULT NULL COMMENT '报批情形 0在核 1已核讫 2已打回',
  site_no varchar(64) DEFAULT NULL COMMENT '涉案企业字号（受案四要件之一，核验只认字号不认人）',
  site_name varchar(128) DEFAULT NULL COMMENT '企业全称（核查通过时自底档带入的快照）',
  outlet_no varchar(64) DEFAULT NULL COMMENT '涉案网点号（受案四要件之一）',
  outlet_addr varchar(255) DEFAULT NULL COMMENT '涉案网点地址（随确认单对外）',
  case_reason varchar(500) DEFAULT NULL COMMENT '事由（受案四要件之一）',
  source_channel varchar(128) DEFAULT NULL COMMENT '来源（受案四要件之一）',
  applicant_name varchar(64) DEFAULT NULL COMMENT '申请人姓名（撞得一个不差也不顶用，核验只认字号）',
  season_grade int DEFAULT NULL COMMENT '本件裁量幅度折到的当季那一格 1..3（核查开落定）',
  correct_deadline datetime DEFAULT NULL COMMENT '责令改正期限（随确认单对外）',
  relief_channel varchar(500) DEFAULT NULL COMMENT '救济途径（随确认单对外）',
  round_no int DEFAULT '1' COMMENT '轮次：省局退回后先前名目沉底，从核查重新往上累一轮',
  year_no int DEFAULT NULL COMMENT '受案年度（当年第二件点名用）',
  serial_in_year int DEFAULT NULL COMMENT '同企业同年度受案序（2 即第二件，当场点名）',
  end_type int DEFAULT NULL COMMENT '止步/封口情形 0不予立案 1撤案 2核讫封住；NULL为在途',
  sealed int DEFAULT '0' COMMENT '封口标记 0未封 1章盖到底各栏封死',
  province_status int DEFAULT '0' COMMENT '省局名录情形 0未报 1已入名录 2省局退回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_bill_no (bill_no),
  KEY idx_site_year (site_no, year_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='违法行为查处逐级核签单';

-- 四开签迹：每一开过口落一笔名目；并签开两名并排各记各的，同一分钟也算两笔。
-- 省局退回：旧轮次签迹 valid_flag 置 0 沉底，与新轮次有效签迹不并列在一处。
CREATE TABLE IF NOT EXISTS t_post_bill_action (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint NOT NULL COMMENT '核签单主键',
  round_no int NOT NULL COMMENT '落名时所属轮次',
  node_no int NOT NULL COMMENT '开次 0受案 1核查 2并签 3签发',
  role_kind int NOT NULL COMMENT '名目角色 0受案登记 1核查 2程序并签 3裁量并签 4签发 5止步(不予立案/撤案) 6省局退回',
  signer varchar(64) NOT NULL COMMENT '落名人（取自登录会话，替人落的不作数）',
  action_code varchar(32) NOT NULL COMMENT '动作名目 RECEIVE/VERIFY_PASS/COSIGN/SIGN_FINISH/NO_CASE/WITHDRAW/PROVINCE_RETURN',
  result_code int DEFAULT NULL COMMENT '该笔结论 0放行 1拦下 2止步 3退回',
  opinion varchar(500) DEFAULT NULL COMMENT '落名意见/裁量依据',
  valid_flag int DEFAULT '1' COMMENT '凭据情形 1有效 0省局退回后作废沉底',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_round_role_signer (bill_id, round_no, node_no, role_kind, signer),
  KEY idx_bill_round (bill_id, round_no, valid_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='核签单四开签迹凭据';

-- 省局名录：签发盖到底同步一份；退回则名下移出（status=1），件从核查重新走。
CREATE TABLE IF NOT EXISTS t_post_province_roster (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '查处核签单号',
  site_no varchar(64) DEFAULT NULL COMMENT '企业字号',
  site_name varchar(128) DEFAULT NULL COMMENT '企业全称',
  outlet_no varchar(64) DEFAULT NULL COMMENT '涉案网点号',
  outlet_addr varchar(255) DEFAULT NULL COMMENT '涉案网点地址',
  case_reason varchar(500) DEFAULT NULL COMMENT '事由',
  season_grade int DEFAULT NULL COMMENT '当季裁量格',
  correct_deadline datetime DEFAULT NULL COMMENT '责令改正期限',
  province_opinion varchar(500) DEFAULT NULL COMMENT '省局退回措辞',
  status int DEFAULT '0' COMMENT '名录情形 0在名录 1已退回移出',
  sync_time datetime DEFAULT NULL COMMENT '入名录时刻',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_bill_no (bill_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='省局核签名录';

-- 对外整改确认单：末一开签发后一键由同一件带出，不另请人抄第二遍；
-- 企业字号、网点地址、事由、责令改正期限、救济途径与核签单摆出的数一致。
CREATE TABLE IF NOT EXISTS t_post_rectify_notice (
  id bigint NOT NULL COMMENT '主键',
  notice_no varchar(64) DEFAULT NULL COMMENT '整改确认单号',
  bill_id bigint NOT NULL COMMENT '出处核签单主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '出处核签单号',
  round_no int NOT NULL COMMENT '出处轮次（省局退回重走后再签发，出新一轮版本；旧版留底可倒查）',
  site_no varchar(64) DEFAULT NULL COMMENT '企业字号',
  site_name varchar(128) DEFAULT NULL COMMENT '企业全称',
  outlet_no varchar(64) DEFAULT NULL COMMENT '涉案网点号',
  outlet_addr varchar(255) DEFAULT NULL COMMENT '网点地址',
  case_reason varchar(500) DEFAULT NULL COMMENT '事由',
  season_grade int DEFAULT NULL COMMENT '当季裁量格',
  correct_deadline datetime DEFAULT NULL COMMENT '责令改正期限',
  relief_channel varchar(500) DEFAULT NULL COMMENT '救济途径',
  issue_time datetime DEFAULT NULL COMMENT '出具时刻（末一开签发同时）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_notice_no (notice_no),
  UNIQUE KEY uk_bill_round (bill_id, round_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对外整改确认单';

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
  site_no varchar(64) DEFAULT NULL COMMENT '所属企业字号（与核签单只认的字号对齐）',
  outlet_no varchar(64) DEFAULT NULL COMMENT '所属网点号（同一网点压着未销案卡，核签单停在核查开）',
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

-- 当季现行裁量判据线一条：核查开折 season_grade 必须落在 1..3 格之内。
INSERT IGNORE INTO t_post_quality_rule (id, rule_code, rule_name, th1_max, th2_max, th3_max, eff_start, eff_end, priority, status, del_flag, create_by, create_time)
VALUES (0, 'SEASON-2026', '2026年度处罚裁量当季线（三格）', 30.00, 60.00, 100.00, '2026-01-01 00:00:00', '2027-01-01 00:00:00', 1, 0, 0, 'seed', NOW());

-- ============================================================================
-- 存量库增量（已建过旧版 t_post_punish_bill 的库逐句执行；新库无需执行）
-- ----------------------------------------------------------------------------
-- ALTER TABLE t_post_punish_bill
--   ADD COLUMN site_no varchar(64) NULL COMMENT '涉案企业字号' AFTER status,
--   ADD COLUMN site_name varchar(128) NULL COMMENT '企业全称快照' AFTER site_no,
--   ADD COLUMN outlet_no varchar(64) NULL COMMENT '涉案网点号' AFTER site_name,
--   ADD COLUMN outlet_addr varchar(255) NULL COMMENT '涉案网点地址' AFTER outlet_no,
--   ADD COLUMN case_reason varchar(500) NULL COMMENT '事由' AFTER outlet_addr,
--   ADD COLUMN source_channel varchar(128) NULL COMMENT '来源' AFTER case_reason,
--   ADD COLUMN applicant_name varchar(64) NULL COMMENT '申请人姓名' AFTER source_channel,
--   ADD COLUMN season_grade int NULL COMMENT '裁量幅度折到的当季格 1..3' AFTER applicant_name,
--   ADD COLUMN correct_deadline datetime NULL COMMENT '责令改正期限' AFTER season_grade,
--   ADD COLUMN relief_channel varchar(500) NULL COMMENT '救济途径' AFTER correct_deadline,
--   ADD COLUMN round_no int DEFAULT 1 COMMENT '轮次' AFTER relief_channel,
--   ADD COLUMN year_no int NULL COMMENT '受案年度' AFTER round_no,
--   ADD COLUMN serial_in_year int NULL COMMENT '同企业同年受案序' AFTER year_no,
--   ADD COLUMN end_type int NULL COMMENT '止步/封口 0不予立案 1撤案 2核讫封住' AFTER serial_in_year,
--   ADD COLUMN sealed int DEFAULT 0 COMMENT '封口标记' AFTER end_type,
--   ADD COLUMN province_status int DEFAULT 0 COMMENT '省局名录情形' AFTER sealed,
--   ADD UNIQUE KEY uk_bill_no (bill_no),
--   ADD KEY idx_site_year (site_no, year_no);
-- CREATE TABLE IF NOT EXISTS t_post_bill_action ( ... 见上 ... );
-- CREATE TABLE IF NOT EXISTS t_post_province_roster ( ... 见上 ... );
-- CREATE TABLE IF NOT EXISTS t_post_rectify_notice ( ... 见上 ... );
-- ALTER TABLE t_post_rectify_card
--   ADD COLUMN site_no varchar(64) NULL COMMENT '所属企业字号' AFTER biz_no,
--   ADD COLUMN outlet_no varchar(64) NULL COMMENT '所属网点号' AFTER site_no,
--   ADD KEY idx_outlet_status (site_no, outlet_no, status);
-- ============================================================================
