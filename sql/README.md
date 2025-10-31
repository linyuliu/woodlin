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
├── postgresql/                  # PostgreSQL database scripts (coming soon)
└── oracle/                      # Oracle database scripts (coming soon)
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

### PostgreSQL (Coming Soon)

PostgreSQL-specific scripts will be added to the `postgresql/` directory. These will include:
- Data type conversions (e.g., `TINYINT` → `SMALLINT`, `DATETIME` → `TIMESTAMP`)
- Sequence definitions for auto-increment fields
- PostgreSQL-specific syntax adjustments

### Oracle (Coming Soon)

Oracle-specific scripts will be added to the `oracle/` directory. These will include:
- Data type conversions (e.g., `DATETIME` → `TIMESTAMP`, `TEXT` → `CLOB`)
- Sequence and trigger definitions for auto-increment fields
- Oracle-specific syntax adjustments

## Database Support

The Woodlin system is designed to support multiple database types through the Dynamic DataSource feature:

| Database | Status | Directory | Notes |
|----------|--------|-----------|-------|
| MySQL | ✅ Supported | `mysql/` | Primary database, fully tested |
| PostgreSQL | 🚧 Coming Soon | `postgresql/` | Planned support |
| Oracle | 🚧 Coming Soon | `oracle/` | Planned support |
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

- **1.0.0** (2025-10-31): Initial organization of SQL scripts by database type
- **1.0.0** (2025-01-01): Original MySQL scripts
