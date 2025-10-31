# SQL Scripts Directory

This directory contains database initialization scripts organized by database type.

## Directory Structure

```
sql/
├── README.md                    # This file
├── mysql/                       # MySQL database scripts
│   ├── woodlin_schema.sql      # Main database schema
│   ├── woodlin_data.sql        # Initial data
│   ├── sql2api_schema.sql      # SQL2API feature tables
│   ├── oss_management_schema.sql # OSS management tables
│   ├── system_config_data.sql  # System configuration data
│   ├── password_policy_update.sql # Password policy update
│   ├── rbac1_upgrade.sql       # RBAC upgrade script
│   └── searchable_encryption_example.sql # Encryption example
├── postgresql/                  # PostgreSQL database scripts
│   ├── woodlin_schema.sql      # Main database schema (PostgreSQL)
│   ├── woodlin_data.sql        # Initial data (PostgreSQL)
│   ├── sql2api_schema.sql      # SQL2API feature tables
│   ├── oss_management_schema.sql # OSS management tables
│   ├── system_config_data.sql  # System configuration data
│   ├── password_policy_update.sql # Password policy update
│   ├── rbac1_upgrade.sql       # RBAC upgrade script
│   └── searchable_encryption_example.sql # Encryption example
└── oracle/                      # Oracle database scripts
    ├── woodlin_schema.sql      # Main database schema (Oracle)
    ├── woodlin_data.sql        # Initial data (Oracle)
    ├── sql2api_schema.sql      # SQL2API feature tables
    ├── oss_management_schema.sql # OSS management tables
    ├── system_config_data.sql  # System configuration data
    ├── password_policy_update.sql # Password policy update
    ├── rbac1_upgrade.sql       # RBAC upgrade script
    └── searchable_encryption_example.sql # Encryption example
```

## Usage

### MySQL

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
   
   # RBAC upgrade
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
| MySQL | ✅ Supported | `mysql/` | Primary database, fully tested, 8 files |
| PostgreSQL | ✅ Supported | `postgresql/` | Converted from MySQL, 8 files |
| Oracle | ✅ Supported | `oracle/` | Converted from MySQL, 8 files |
| SQL Server | 🚧 Future | - | Future consideration |

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

For a fresh installation, execute scripts in this order:

1. **Schema Creation** - `woodlin_schema.sql`
2. **Initial Data** - `woodlin_data.sql`
3. **Optional Features** (as needed):
   - `sql2api_schema.sql`
   - `oss_management_schema.sql`
   - `system_config_data.sql`
4. **Updates/Upgrades** (if upgrading from older version):
   - `password_policy_update.sql`
   - `rbac1_upgrade.sql`

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

- **1.1.0** (2025-10-31): Added PostgreSQL and Oracle SQL conversions (8 files each)
  - Converted all MySQL scripts to PostgreSQL syntax
  - Converted all MySQL scripts to Oracle syntax
  - Added comprehensive comments to all scripts
  - Documented database-specific syntax differences
- **1.0.0** (2025-10-31): Initial organization of SQL scripts by database type
- **1.0.0** (2025-01-01): Original MySQL scripts
