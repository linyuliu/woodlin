# SQL Scripts Directory

This directory contains database initialization scripts organized by database type.

## Directory Structure

```
sql/
├── README.md                    # This file
├── mysql/                       # MySQL database scripts ✅ UPDATED
│   ├── woodlin_complete_schema.sql  # ✨ NEW: Complete schema (all tables)
│   ├── woodlin_complete_data.sql    # ✨ NEW: Complete initial data
│   ├── README.md                    # ✨ NEW: Detailed documentation
│   ├── QUICKSTART.md                # ✨ NEW: Quick start guide (中文)
│   ├── CHANGELOG.md                 # ✨ NEW: Change log
│   ├── woodlin_schema.sql      # Legacy: Basic schema
│   ├── woodlin_data.sql        # Legacy: Basic data
│   ├── sql2api_schema.sql      # Legacy: SQL2API feature
│   ├── oss_management_schema.sql # Legacy: OSS management
│   ├── system_config_data.sql  # Legacy: System configuration
│   ├── password_policy_update.sql # Legacy: Password policy
│   ├── rbac1_upgrade.sql       # ⚠️ DEPRECATED: Use complete scripts
│   └── searchable_encryption_example.sql # Example
├── postgresql/                  # PostgreSQL scripts (to be updated)
│   ├── woodlin_schema.sql      # Main database schema
│   ├── woodlin_data.sql        # Initial data
│   ├── sql2api_schema.sql      # SQL2API feature
│   ├── oss_management_schema.sql # OSS management
│   ├── system_config_data.sql  # System configuration
│   ├── password_policy_update.sql # Password policy update
│   ├── rbac1_upgrade.sql       # RBAC upgrade script
│   └── searchable_encryption_example.sql # Example
└── oracle/                      # Oracle scripts (to be updated)
    ├── woodlin_schema.sql      # Main database schema
    ├── woodlin_data.sql        # Initial data
    ├── sql2api_schema.sql      # SQL2API feature
    ├── oss_management_schema.sql # OSS management
    ├── system_config_data.sql  # System configuration
    ├── password_policy_update.sql # Password policy update
    ├── rbac1_upgrade.sql       # RBAC upgrade script
    └── searchable_encryption_example.sql # Example
```

## ⭐ What's New (v2.0.0 - 2025-11-07)

### MySQL Scripts Reorganized ✅

**New Complete Scripts** (Recommended):
- `woodlin_complete_schema.sql` - All 23 tables in one file
- `woodlin_complete_data.sql` - All initial data in one file

**Key Improvements**:
- ✅ No stored procedures (business logic in Java)
- ✅ RBAC1 role inheritance integrated
- ✅ All features included (OSS, SQL2API, password policy, etc.)
- ✅ Comprehensive documentation
- ✅ Simple 2-step import process

**Documentation**:
- `README.md` - Complete usage guide
- `QUICKSTART.md` - Quick start (Chinese)
- `CHANGELOG.md` - Detailed changes

## Usage

### MySQL ✅ (Recommended - Simplified)

**New Simplified Process** (v2.0.0):

1. **Import Complete Schema and Data** (2 steps only!)
   ```bash
   # Step 1: Import all tables (23 tables)
   mysql -u root -p < mysql/woodlin_complete_schema.sql
   
   # Step 2: Import all initial data
   mysql -u root -p < mysql/woodlin_complete_data.sql
   ```

2. **That's it!** Your database is ready with:
   - All system tables (RBAC, users, roles, permissions)
   - RBAC1 role inheritance support
   - File management and OSS storage
   - SQL2API dynamic interfaces
   - All system configurations
   - Default admin user (admin/Passw0rd)

3. **Verify Installation**
   ```bash
   mysql -u root -p
   mysql> USE woodlin;
   mysql> SHOW TABLES;  # Should show 23 tables
   mysql> SELECT * FROM sys_user;  # Should show admin and demo users
   ```

**Documentation**:
- See [mysql/README.md](mysql/README.md) for complete documentation
- See [mysql/QUICKSTART.md](mysql/QUICKSTART.md) for quick start guide (中文)
- See [mysql/CHANGELOG.md](mysql/CHANGELOG.md) for what changed

---

**Legacy Process** (v1.x - Still works but not recommended):

1. **Create and Initialize Database**
   ```bash
   # Create database and tables
   mysql -u root -p < mysql/woodlin_schema.sql
   
   # Load initial data
   mysql -u root -p < mysql/woodlin_data.sql
   ```

2. **Optional Features** (Execute as needed)
   ```bash
   # SQL2API feature
   mysql -u root -p woodlin < mysql/sql2api_schema.sql
   
   # OSS management feature
   mysql -u root -p woodlin < mysql/oss_management_schema.sql
   
   # System configuration
   mysql -u root -p woodlin < mysql/system_config_data.sql
   
   # Password policy
   mysql -u root -p woodlin < mysql/password_policy_update.sql
   
   # RBAC upgrade (DEPRECATED - use complete scripts instead)
   mysql -u root -p woodlin < mysql/rbac1_upgrade.sql
   ```

3. **Example Scripts**
   ```bash
   # Searchable encryption example
   mysql -u root -p woodlin < mysql/searchable_encryption_example.sql
   ```

### PostgreSQL

1. **Create and Initialize Database**
   ```bash
   # Create database (as superuser)
   createdb woodlin -E UTF8
   
   # Connect and create tables
   psql -U postgres -d woodlin -f postgresql/woodlin_schema.sql
   
   # Load initial data
   psql -U postgres -d woodlin -f postgresql/woodlin_data.sql
   ```

2. **Optional Features** (Execute as needed)
   ```bash
   # SQL2API feature
   psql -U postgres -d woodlin -f postgresql/sql2api_schema.sql
   
   # OSS management feature
   psql -U postgres -d woodlin -f postgresql/oss_management_schema.sql
   
   # System configuration
   psql -U postgres -d woodlin -f postgresql/system_config_data.sql
   
   # Password policy
   psql -U postgres -d woodlin -f postgresql/password_policy_update.sql
   
   # RBAC upgrade
   psql -U postgres -d woodlin -f postgresql/rbac1_upgrade.sql
   ```

3. **Key Conversions Applied**:
   - `TINYINT` → `SMALLINT`
   - `INT(n)` → `INTEGER`
   - `BIGINT(n)` → `BIGINT`
   - `DATETIME` → `TIMESTAMP`
   - `AUTO_INCREMENT` → Triggers for auto-update
   - Removed backticks and ENGINE clauses
   - Added COMMENT ON statements
   - Created triggers for `update_time` auto-update

### Oracle

1. **Create and Initialize Database**
   ```bash
   # Connect as SYSTEM user
   sqlplus system/password@//localhost:1521/XE
   
   # Create user and grant privileges
   CREATE USER woodlin IDENTIFIED BY password;
   GRANT CONNECT, RESOURCE, DBA TO woodlin;
   
   # Execute schema script
   @oracle/woodlin_schema.sql
   
   # Load initial data
   @oracle/woodlin_data.sql
   ```

2. **Optional Features** (Execute as needed)
   ```bash
   # Connect to Oracle
   sqlplus woodlin/password@//localhost:1521/XE
   
   # SQL2API feature
   @oracle/sql2api_schema.sql
   
   # OSS management feature
   @oracle/oss_management_schema.sql
   
   # System configuration
   @oracle/system_config_data.sql
   
   # Password policy
   @oracle/password_policy_update.sql
   
   # RBAC upgrade
   @oracle/rbac1_upgrade.sql
   ```

3. **Key Conversions Applied**:
   - `TINYINT` → `NUMBER(3)`
   - `INT(n)` → `NUMBER(10)`
   - `BIGINT(n)` → `NUMBER(19)`
   - `VARCHAR` → `VARCHAR2`
   - `DATETIME` → `TIMESTAMP`
   - `TEXT` → `CLOB`
   - `NOW()` → `SYSDATE`
   - `CURRENT_TIMESTAMP` → `SYSTIMESTAMP`
   - Removed backticks and ENGINE clauses
   - Note: AUTO_INCREMENT requires sequences and triggers (to be implemented)

## Database Support

The Woodlin system is designed to support multiple database types through the Dynamic DataSource feature:

| Database | Status | Directory | Notes |
|----------|--------|-----------|-------|
| MySQL | ✅ **Fully Organized** | `mysql/` | **v2.0.0**: Complete scripts, no stored procedures, full docs |
| PostgreSQL | 🔄 To be organized | `postgresql/` | Legacy scripts, will be reorganized like MySQL |
| Oracle | 🔄 To be organized | `oracle/` | Legacy scripts, will be reorganized like MySQL |
| SQL Server | 🚧 Future | - | Future consideration |

### Why MySQL is Recommended

✅ **Complete reorganization (v2.0.0)**:
- Single schema file (23 tables)
- Single data file (all initial data)
- No stored procedures (business logic in Java)
- RBAC1 integrated
- Comprehensive documentation
- Quick 2-step import

🔄 **PostgreSQL & Oracle**: Coming soon with similar organization

## Configuration

Update your `application.yml` to configure the database connection:

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/woodlin
          username: root
          password: your_password
          driver-class-name: com.mysql.cj.jdbc.Driver
```

For PostgreSQL:
```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:postgresql://localhost:5432/woodlin
          username: postgres
          password: your_password
          driver-class-name: org.postgresql.Driver
```

For Oracle:
```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:oracle:thin:@localhost:1521:woodlin
          username: system
          password: your_password
          driver-class-name: oracle.jdbc.OracleDriver
```

## Script Execution Order

### For MySQL (v2.0.0 - Recommended) ✅

**Simple 2-step process**:
1. `woodlin_complete_schema.sql` - All tables
2. `woodlin_complete_data.sql` - All initial data

Done! All features are included.

---

### For Legacy Scripts (v1.x)

For a fresh installation using legacy scripts, execute in this order:

1. **Schema Creation** - `woodlin_schema.sql`
2. **Initial Data** - `woodlin_data.sql`
3. **Optional Features** (as needed):
   - `sql2api_schema.sql`
   - `oss_management_schema.sql`
   - `system_config_data.sql`
4. **Updates/Upgrades** (if upgrading from older version):
   - `password_policy_update.sql`
   - `rbac1_upgrade.sql` (⚠️ Deprecated - use complete scripts)

---

### For PostgreSQL / Oracle (Legacy)

Execute scripts in this order:

1. **Schema Creation** - `woodlin_schema.sql`
2. **Initial Data** - `woodlin_data.sql`
3. **Optional Features** (as needed in any order):
   - `sql2api_schema.sql`
   - `oss_management_schema.sql`
   - `system_config_data.sql`
   - `password_policy_update.sql`
   - `rbac1_upgrade.sql`

**Note**: PostgreSQL and Oracle scripts will be reorganized similar to MySQL in future updates.

## Contributing

When adding SQL scripts for new features or database types:

1. **For MySQL**: Add scripts to `mysql/` directory
2. **For PostgreSQL**: Add equivalent scripts to `postgresql/` directory with appropriate syntax
3. **For Oracle**: Add equivalent scripts to `oracle/` directory with appropriate syntax
4. Update this README.md to document new scripts
5. Ensure scripts include proper comments and metadata:
   ```sql
   -- =============================================
   -- Script Name: feature_name.sql
   -- Author: your_name
   -- Description: Brief description
   -- Version: 1.0.0
   -- Date: YYYY-MM-DD
   -- Database: MySQL/PostgreSQL/Oracle
   -- =============================================
   ```

## Version History

- **2.0.0** (2025-11-07): **Major MySQL Scripts Reorganization**
  - ✨ Added complete schema script (`woodlin_complete_schema.sql`) - 23 tables
  - ✨ Added complete data script (`woodlin_complete_data.sql`)
  - ✨ Removed stored procedures (business logic moved to Java)
  - ✨ Integrated RBAC1 role inheritance into main scripts
  - ✨ Added comprehensive documentation (README, QUICKSTART, CHANGELOG)
  - ⚠️ Deprecated `rbac1_upgrade.sql` (use complete scripts)
  - 🎯 Simplified import process: 2 steps instead of 6+
  - 📚 Added detailed Chinese quick start guide
- **1.1.0** (2025-10-31): Added PostgreSQL and Oracle SQL conversions (8 files each)
  - Converted all MySQL scripts to PostgreSQL syntax
  - Converted all MySQL scripts to Oracle syntax
  - Added comprehensive comments to all scripts
  - Documented database-specific syntax differences
- **1.0.0** (2025-10-31): Initial organization of SQL scripts by database type
- **1.0.0** (2025-01-01): Original MySQL scripts
