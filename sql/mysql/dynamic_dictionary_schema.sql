-- =============================================
-- 动态字典系统数据库架构
-- 作者: mumu
-- 描述: 统一的动态字典管理系统，支持类型查询和数据查询分离
-- 版本: 1.0.0
-- 时间: 2025-12-27
-- =============================================

USE `woodlin`;

-- =============================================
-- 字典类型表
-- =============================================
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
    `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
    `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
    `dict_type` varchar(100) NOT NULL COMMENT '字典类型',
    `dict_category` varchar(50) DEFAULT 'system' COMMENT '字典分类（system-系统字典，business-业务字典，custom-自定义字典）',
    `status` char(1) DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `tenant_id` varchar(64) DEFAULT NULL COMMENT '租户ID（NULL表示通用字典）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (`dict_id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`, `tenant_id`, `deleted`),
    KEY `idx_dict_category` (`dict_category`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- =============================================
-- 字典数据表
-- =============================================
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
    `data_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典数据主键',
    `dict_type` varchar(100) NOT NULL COMMENT '字典类型',
    `dict_label` varchar(100) NOT NULL COMMENT '字典标签',
    `dict_value` varchar(100) NOT NULL COMMENT '字典键值',
    `dict_desc` varchar(500) DEFAULT NULL COMMENT '字典描述',
    `dict_sort` int(11) DEFAULT 0 COMMENT '字典排序',
    `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
    `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
    `is_default` char(1) DEFAULT '0' COMMENT '是否默认（1-是，0-否）',
    `status` char(1) DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
    `extra_data` text DEFAULT NULL COMMENT '扩展数据（JSON格式）',
    `tenant_id` varchar(64) DEFAULT NULL COMMENT '租户ID（NULL表示通用字典）',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (`data_id`),
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_dict_sort` (`dict_sort`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- =============================================
-- 行政区划表（树形结构）
-- =============================================
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region` (
    `region_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '区划主键',
    `region_code` varchar(20) NOT NULL COMMENT '区划代码（GB/T 2260标准6位代码）',
    `region_name` varchar(100) NOT NULL COMMENT '区划名称',
    `parent_code` varchar(20) DEFAULT NULL COMMENT '父区划代码',
    `region_level` int(11) DEFAULT 1 COMMENT '区划层级（1-省级，2-市级，3-区县级，4-街道级）',
    `region_type` varchar(20) DEFAULT NULL COMMENT '区划类型（province-省，city-市，district-区县，street-街道）',
    `short_name` varchar(50) DEFAULT NULL COMMENT '简称',
    `pinyin` varchar(100) DEFAULT NULL COMMENT '拼音',
    `pinyin_abbr` varchar(20) DEFAULT NULL COMMENT '拼音缩写',
    `longitude` decimal(10, 6) DEFAULT NULL COMMENT '经度',
    `latitude` decimal(10, 6) DEFAULT NULL COMMENT '纬度',
    `sort_order` int(11) DEFAULT 0 COMMENT '排序',
    `is_municipality` char(1) DEFAULT '0' COMMENT '是否直辖市/特别行政区（1-是，0-否）',
    `status` char(1) DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` char(1) DEFAULT '0' COMMENT '删除标识（0-正常，1-删除）',
    PRIMARY KEY (`region_id`),
    UNIQUE KEY `uk_region_code` (`region_code`, `deleted`),
    KEY `idx_parent_code` (`parent_code`),
    KEY `idx_region_level` (`region_level`),
    KEY `idx_region_type` (`region_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行政区划表';

-- =============================================
-- 初始化字典类型数据
-- =============================================
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `dict_category`, `status`, `remark`) VALUES
(1, '性别', 'gender', 'system', '1', 'GB/T 2261.1-2003 标准'),
(2, '民族', 'ethnicity', 'system', '1', 'GB/T 3304-1991 标准，56个民族'),
(3, '学历', 'education', 'system', '1', 'GB/T 4658-2006 标准'),
(4, '婚姻状况', 'marital', 'system', '1', 'GB/T 2261.2-2003 标准'),
(5, '政治面貌', 'political', 'system', '1', 'GB/T 4762-1984 标准'),
(6, '证件类型', 'idtype', 'system', '1', 'GB/T 2261.4 标准'),
(7, '用户状态', 'user_status', 'system', '1', '用户账号状态');

-- =============================================
-- 初始化性别字典数据
-- =============================================
INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_desc`, `dict_sort`, `status`) VALUES
('gender', '未知的性别', '0', 'GB/T 2261.1-2003标准', 1, '1'),
('gender', '男性', '1', 'GB/T 2261.1-2003标准', 2, '1'),
('gender', '女性', '2', 'GB/T 2261.1-2003标准', 3, '1'),
('gender', '未说明的性别', '9', 'GB/T 2261.1-2003标准', 4, '1');

-- =============================================
-- 初始化民族字典数据（56个民族）
-- =============================================
INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_desc`, `dict_sort`, `status`) VALUES
('ethnicity', '汉族', '01', 'GB/T 3304-1991', 1, '1'),
('ethnicity', '蒙古族', '02', 'GB/T 3304-1991', 2, '1'),
('ethnicity', '回族', '03', 'GB/T 3304-1991', 3, '1'),
('ethnicity', '藏族', '04', 'GB/T 3304-1991', 4, '1'),
('ethnicity', '维吾尔族', '05', 'GB/T 3304-1991', 5, '1'),
('ethnicity', '苗族', '06', 'GB/T 3304-1991', 6, '1'),
('ethnicity', '彝族', '07', 'GB/T 3304-1991', 7, '1'),
('ethnicity', '壮族', '08', 'GB/T 3304-1991', 8, '1'),
('ethnicity', '布依族', '09', 'GB/T 3304-1991', 9, '1'),
('ethnicity', '朝鲜族', '10', 'GB/T 3304-1991', 10, '1'),
('ethnicity', '满族', '11', 'GB/T 3304-1991', 11, '1'),
('ethnicity', '侗族', '12', 'GB/T 3304-1991', 12, '1'),
('ethnicity', '瑶族', '13', 'GB/T 3304-1991', 13, '1'),
('ethnicity', '白族', '14', 'GB/T 3304-1991', 14, '1'),
('ethnicity', '土家族', '15', 'GB/T 3304-1991', 15, '1'),
('ethnicity', '哈尼族', '16', 'GB/T 3304-1991', 16, '1'),
('ethnicity', '哈萨克族', '17', 'GB/T 3304-1991', 17, '1'),
('ethnicity', '傣族', '18', 'GB/T 3304-1991', 18, '1'),
('ethnicity', '黎族', '19', 'GB/T 3304-1991', 19, '1'),
('ethnicity', '傈僳族', '20', 'GB/T 3304-1991', 20, '1'),
('ethnicity', '佤族', '21', 'GB/T 3304-1991', 21, '1'),
('ethnicity', '畲族', '22', 'GB/T 3304-1991', 22, '1'),
('ethnicity', '高山族', '23', 'GB/T 3304-1991', 23, '1'),
('ethnicity', '拉祜族', '24', 'GB/T 3304-1991', 24, '1'),
('ethnicity', '水族', '25', 'GB/T 3304-1991', 25, '1'),
('ethnicity', '东乡族', '26', 'GB/T 3304-1991', 26, '1'),
('ethnicity', '纳西族', '27', 'GB/T 3304-1991', 27, '1'),
('ethnicity', '景颇族', '28', 'GB/T 3304-1991', 28, '1'),
('ethnicity', '柯尔克孜族', '29', 'GB/T 3304-1991', 29, '1'),
('ethnicity', '土族', '30', 'GB/T 3304-1991', 30, '1'),
('ethnicity', '达斡尔族', '31', 'GB/T 3304-1991', 31, '1'),
('ethnicity', '仫佬族', '32', 'GB/T 3304-1991', 32, '1'),
('ethnicity', '羌族', '33', 'GB/T 3304-1991', 33, '1'),
('ethnicity', '布朗族', '34', 'GB/T 3304-1991', 34, '1'),
('ethnicity', '撒拉族', '35', 'GB/T 3304-1991', 35, '1'),
('ethnicity', '毛南族', '36', 'GB/T 3304-1991', 36, '1'),
('ethnicity', '仡佬族', '37', 'GB/T 3304-1991', 37, '1'),
('ethnicity', '锡伯族', '38', 'GB/T 3304-1991', 38, '1'),
('ethnicity', '阿昌族', '39', 'GB/T 3304-1991', 39, '1'),
('ethnicity', '普米族', '40', 'GB/T 3304-1991', 40, '1'),
('ethnicity', '塔吉克族', '41', 'GB/T 3304-1991', 41, '1'),
('ethnicity', '怒族', '42', 'GB/T 3304-1991', 42, '1'),
('ethnicity', '乌孜别克族', '43', 'GB/T 3304-1991', 43, '1'),
('ethnicity', '俄罗斯族', '44', 'GB/T 3304-1991', 44, '1'),
('ethnicity', '鄂温克族', '45', 'GB/T 3304-1991', 45, '1'),
('ethnicity', '德昂族', '46', 'GB/T 3304-1991', 46, '1'),
('ethnicity', '保安族', '47', 'GB/T 3304-1991', 47, '1'),
('ethnicity', '裕固族', '48', 'GB/T 3304-1991', 48, '1'),
('ethnicity', '京族', '49', 'GB/T 3304-1991', 49, '1'),
('ethnicity', '塔塔尔族', '50', 'GB/T 3304-1991', 50, '1'),
('ethnicity', '独龙族', '51', 'GB/T 3304-1991', 51, '1'),
('ethnicity', '鄂伦春族', '52', 'GB/T 3304-1991', 52, '1'),
('ethnicity', '赫哲族', '53', 'GB/T 3304-1991', 53, '1'),
('ethnicity', '门巴族', '54', 'GB/T 3304-1991', 54, '1'),
('ethnicity', '珞巴族', '55', 'GB/T 3304-1991', 55, '1'),
('ethnicity', '基诺族', '56', 'GB/T 3304-1991', 56, '1');

-- =============================================
-- 初始化学历字典数据
-- =============================================
INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_desc`, `dict_sort`, `status`) VALUES
('education', '研究生', '1', 'GB/T 4658-2006', 1, '1'),
('education', '大学本科', '2', 'GB/T 4658-2006', 2, '1'),
('education', '大学专科', '3', 'GB/T 4658-2006', 3, '1'),
('education', '中等专科', '4', 'GB/T 4658-2006', 4, '1'),
('education', '技工学校', '5', 'GB/T 4658-2006', 5, '1'),
('education', '高中', '6', 'GB/T 4658-2006', 6, '1'),
('education', '初中', '7', 'GB/T 4658-2006', 7, '1'),
('education', '小学', '8', 'GB/T 4658-2006', 8, '1'),
('education', '文盲或半文盲', '9', 'GB/T 4658-2006', 9, '1'),
('education', '其他', '10', 'GB/T 4658-2006', 10, '1');

-- =============================================
-- 初始化婚姻状况字典数据
-- =============================================
INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_desc`, `dict_sort`, `status`) VALUES
('marital', '未婚', '10', 'GB/T 2261.2-2003', 1, '1'),
('marital', '已婚', '20', 'GB/T 2261.2-2003', 2, '1'),
('marital', '丧偶', '30', 'GB/T 2261.2-2003', 3, '1'),
('marital', '离婚', '40', 'GB/T 2261.2-2003', 4, '1'),
('marital', '未说明的婚姻状况', '90', 'GB/T 2261.2-2003', 5, '1');

-- =============================================
-- 初始化政治面貌字典数据
-- =============================================
INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_desc`, `dict_sort`, `status`) VALUES
('political', '中共党员', '01', 'GB/T 4762-1984', 1, '1'),
('political', '中共预备党员', '02', 'GB/T 4762-1984', 2, '1'),
('political', '共青团员', '03', 'GB/T 4762-1984', 3, '1'),
('political', '民革会员', '04', 'GB/T 4762-1984', 4, '1'),
('political', '民盟盟员', '05', 'GB/T 4762-1984', 5, '1'),
('political', '民建会员', '06', 'GB/T 4762-1984', 6, '1'),
('political', '民进会员', '07', 'GB/T 4762-1984', 7, '1'),
('political', '农工党党员', '08', 'GB/T 4762-1984', 8, '1'),
('political', '致公党党员', '09', 'GB/T 4762-1984', 9, '1'),
('political', '九三学社社员', '10', 'GB/T 4762-1984', 10, '1'),
('political', '台盟盟员', '11', 'GB/T 4762-1984', 11, '1'),
('political', '无党派民主人士', '12', 'GB/T 4762-1984', 12, '1'),
('political', '群众', '13', 'GB/T 4762-1984', 13, '1');

-- =============================================
-- 初始化证件类型字典数据
-- =============================================
INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_desc`, `dict_sort`, `status`, `extra_data`) VALUES
('idtype', '居民身份证', '111', 'GB/T 2261.4', 1, '1', '{"pattern": "^[1-9]\\\\d{5}(18|19|20)\\\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\\\d{3}[0-9Xx]$"}'),
('idtype', '临时居民身份证', '112', 'GB/T 2261.4', 2, '1', NULL),
('idtype', '户口簿', '113', 'GB/T 2261.4', 3, '1', NULL),
('idtype', '护照', '414', 'GB/T 2261.4', 4, '1', NULL),
('idtype', '港澳居民来往内地通行证', '420', 'GB/T 2261.4', 5, '1', NULL),
('idtype', '台湾居民来往大陆通行证', '511', 'GB/T 2261.4', 6, '1', NULL),
('idtype', '外国人永久居留身份证', '516', 'GB/T 2261.4', 7, '1', NULL),
('idtype', '军官证', '335', 'GB/T 2261.4', 8, '1', NULL),
('idtype', '士兵证', '336', 'GB/T 2261.4', 9, '1', NULL),
('idtype', '其他', '990', 'GB/T 2261.4', 10, '1', NULL);

-- =============================================
-- 初始化用户状态字典数据
-- =============================================
INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `dict_desc`, `dict_sort`, `list_class`, `status`) VALUES
('user_status', '启用', '1', '用户账号启用状态', 1, 'success', '1'),
('user_status', '禁用', '0', '用户账号禁用状态', 2, 'danger', '1');
