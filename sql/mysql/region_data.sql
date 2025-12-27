-- =============================================
-- 行政区划基础数据（省级）
-- 作者: mumu
-- 描述: GB/T 2260标准的省级行政区划数据
-- 版本: 1.0.0
-- 时间: 2025-12-27
-- 说明: 本脚本包含34个省级行政区，市级和区县级数据需要根据实际需求导入
-- =============================================

USE `woodlin`;

-- =============================================
-- 插入省级行政区划数据（34个省级行政区）
-- =============================================
INSERT INTO `sys_region` (`region_code`, `region_name`, `parent_code`, `region_level`, `region_type`, `short_name`, `pinyin`, `pinyin_abbr`, `sort_order`, `is_municipality`, `status`) VALUES
('110000', '北京市', NULL, 1, 'province', '京', 'Beijing', 'BJ', 1, '1', '1'),
('120000', '天津市', NULL, 1, 'province', '津', 'Tianjin', 'TJ', 2, '1', '1'),
('130000', '河北省', NULL, 1, 'province', '冀', 'Hebei', 'HE', 3, '0', '1'),
('140000', '山西省', NULL, 1, 'province', '晋', 'Shanxi', 'SX', 4, '0', '1'),
('150000', '内蒙古自治区', NULL, 1, 'province', '蒙', 'Inner Mongolia', 'NM', 5, '0', '1'),
('210000', '辽宁省', NULL, 1, 'province', '辽', 'Liaoning', 'LN', 6, '0', '1'),
('220000', '吉林省', NULL, 1, 'province', '吉', 'Jilin', 'JL', 7, '0', '1'),
('230000', '黑龙江省', NULL, 1, 'province', '黑', 'Heilongjiang', 'HL', 8, '0', '1'),
('310000', '上海市', NULL, 1, 'province', '沪', 'Shanghai', 'SH', 9, '1', '1'),
('320000', '江苏省', NULL, 1, 'province', '苏', 'Jiangsu', 'JS', 10, '0', '1'),
('330000', '浙江省', NULL, 1, 'province', '浙', 'Zhejiang', 'ZJ', 11, '0', '1'),
('340000', '安徽省', NULL, 1, 'province', '皖', 'Anhui', 'AH', 12, '0', '1'),
('350000', '福建省', NULL, 1, 'province', '闽', 'Fujian', 'FJ', 13, '0', '1'),
('360000', '江西省', NULL, 1, 'province', '赣', 'Jiangxi', 'JX', 14, '0', '1'),
('370000', '山东省', NULL, 1, 'province', '鲁', 'Shandong', 'SD', 15, '0', '1'),
('410000', '河南省', NULL, 1, 'province', '豫', 'Henan', 'HA', 16, '0', '1'),
('420000', '湖北省', NULL, 1, 'province', '鄂', 'Hubei', 'HB', 17, '0', '1'),
('430000', '湖南省', NULL, 1, 'province', '湘', 'Hunan', 'HN', 18, '0', '1'),
('440000', '广东省', NULL, 1, 'province', '粤', 'Guangdong', 'GD', 19, '0', '1'),
('450000', '广西壮族自治区', NULL, 1, 'province', '桂', 'Guangxi', 'GX', 20, '0', '1'),
('460000', '海南省', NULL, 1, 'province', '琼', 'Hainan', 'HI', 21, '0', '1'),
('500000', '重庆市', NULL, 1, 'province', '渝', 'Chongqing', 'CQ', 22, '1', '1'),
('510000', '四川省', NULL, 1, 'province', '川', 'Sichuan', 'SC', 23, '0', '1'),
('520000', '贵州省', NULL, 1, 'province', '黔', 'Guizhou', 'GZ', 24, '0', '1'),
('530000', '云南省', NULL, 1, 'province', '云', 'Yunnan', 'YN', 25, '0', '1'),
('540000', '西藏自治区', NULL, 1, 'province', '藏', 'Tibet', 'XZ', 26, '0', '1'),
('610000', '陕西省', NULL, 1, 'province', '陕', 'Shaanxi', 'SN', 27, '0', '1'),
('620000', '甘肃省', NULL, 1, 'province', '甘', 'Gansu', 'GS', 28, '0', '1'),
('630000', '青海省', NULL, 1, 'province', '青', 'Qinghai', 'QH', 29, '0', '1'),
('640000', '宁夏回族自治区', NULL, 1, 'province', '宁', 'Ningxia', 'NX', 30, '0', '1'),
('650000', '新疆维吾尔自治区', NULL, 1, 'province', '新', 'Xinjiang', 'XJ', 31, '0', '1'),
('710000', '台湾省', NULL, 1, 'province', '台', 'Taiwan', 'TW', 32, '0', '1'),
('810000', '香港特别行政区', NULL, 1, 'province', '港', 'Hong Kong', 'HK', 33, '1', '1'),
('820000', '澳门特别行政区', NULL, 1, 'province', '澳', 'Macao', 'MO', 34, '1', '1');

-- =============================================
-- 注意事项：
-- 1. 市级和区县级数据量较大（约3000+条记录），建议按需导入
-- 2. 可以使用民政部公开的最新行政区划代码数据
-- 3. 导入格式参考：
--    INSERT INTO sys_region (region_code, region_name, parent_code, region_level, region_type, ...) 
--    VALUES ('110100', '北京市市辖区', '110000', 2, 'city', ...);
-- =============================================
