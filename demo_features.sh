#!/bin/bash

# Dictionary and Configuration Demo Script
# This script demonstrates the new dictionary functionality and configurations

echo "🚀 Woodlin Dictionary & Configuration Demo"
echo "=========================================="

echo
echo "📋 Features Implemented:"
echo "1. Dictionary Label-Value Support for Enums"
echo "2. Enhanced Jackson Configuration" 
echo "3. Redis Serialization with FastJSON2"
echo "4. Comprehensive Testing"

echo
echo "🧪 Running Tests..."
cd /home/runner/work/woodlin/woodlin

echo "→ Testing Dictionary Functionality..."
mvn test -Dtest=DictEnumTest -pl woodlin-common -q
if [ $? -eq 0 ]; then
    echo "✅ Dictionary tests passed"
else
    echo "❌ Dictionary tests failed"
    exit 1
fi

echo "→ Testing Redis Configuration..."
mvn test -Dtest=RedisConfigTest -pl woodlin-common -q
if [ $? -eq 0 ]; then
    echo "✅ Redis configuration tests passed"
else
    echo "❌ Redis configuration tests failed"
    exit 1
fi

echo
echo "📊 Dictionary Usage Examples:"
echo
echo "1. UserStatus Enum with Label-Value:"
echo '   UserStatus.ENABLE -> {"value": "1", "label": "启用"}'
echo '   UserStatus.DISABLE -> {"value": "0", "label": "禁用"}'
echo

echo "2. Gender Enum with Label-Value:"
echo '   Gender.MALE -> {"value": 1, "label": "男"}'
echo '   Gender.FEMALE -> {"value": 2, "label": "女"}'
echo '   Gender.UNKNOWN -> {"value": 0, "label": "未知"}'
echo

echo "3. API Endpoints Created:"
echo "   GET /api/dict/user-status - Returns user status dictionary"
echo "   GET /api/dict/gender - Returns gender dictionary"
echo "   GET /api/dict/demo-user - Shows enum serialization in user object"
echo

echo "🔧 Configuration Improvements:"
echo "• FastJSON2 dependency added for better performance"
echo "• Enhanced Jackson with BigDecimal support and error handling"
echo "• Redis serialization using FastJSON2 for speed"
echo "• Comprehensive null handling and validation"
echo

echo "🛠️ Utility Methods Available:"
echo "• DictUtil.toDictList(EnumClass) - Get all enum values as list"
echo "• DictUtil.getLabelByValue(EnumClass, value) - Get label for value"
echo "• DictUtil.getValueByLabel(EnumClass, label) - Get value for label"
echo "• DictUtil.containsValue(EnumClass, value) - Check if value exists"
echo

echo "✨ All implementations completed successfully!"
echo "Ready for production use with backward compatibility maintained."