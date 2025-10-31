# Woodlin Multi-Tenant Management System

Woodlin is a Spring Boot 3.5.6 + Java 25 multi-tenant backend management system with Vue 3 + TypeScript + Naive UI frontend. This is a multi-module Maven project providing comprehensive enterprise-level features including user management, RBAC permissions, file management, and task scheduling.

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Bootstrap, Build, and Test the Repository
Follow these commands in sequence. **NEVER CANCEL** long-running commands:

```bash
# Install dependencies and build backend (NEVER CANCEL: Takes 1-2 minutes first time)
mvn clean package -DskipTests
# Timeout: 180 seconds minimum for first build, 60 seconds for subsequent builds

# Install to local Maven repository (required before running spring-boot:run)
mvn install -DskipTests
# Timeout: 120 seconds

# Build frontend dependencies and application
cd woodlin-web
npm install  # Takes ~16 seconds
npm run build  # Takes ~8-9 seconds
cd ..

# Use the comprehensive build script (includes both backend and frontend)
./scripts/dev.sh build
# NEVER CANCEL: Takes ~18-20 seconds, includes both backend and frontend
# Timeout: 120 seconds
```

### Run the Application

**Prerequisites**: MySQL 8.0+ and Redis 6.0+ are required for full operation.

```bash
# Option 1: Use Docker Compose (recommended for development)
cp .env.example .env  # Modify as needed
docker compose up -d  # Starts MySQL, Redis, and application
# Access: http://localhost:8080/api (backend) and http://localhost:3000 (frontend)

# Option 2: Run components separately for development
# Backend (requires database setup first)
mvn spring-boot:run -pl woodlin-admin -Dspring-boot.run.profiles=dev

# Frontend development server (starts in ~616ms)
cd woodlin-web
npm run dev
# Access: http://localhost:5173/
```

### Development Scripts
Use the provided scripts for common operations:

```bash
# Start backend only
./scripts/dev.sh backend

# Start frontend only  
./scripts/dev.sh frontend

# Start both (uses tmux/screen if available)
./scripts/dev.sh

# Build everything
./scripts/dev.sh build

# Clean build artifacts
./scripts/dev.sh clean

# Full deployment with Docker
./scripts/deploy.sh
```

## Validation

### Always Run These Validation Steps
Before considering any changes complete, execute these validation steps:

1. **Build Validation**:
   ```bash
   # Backend build (NEVER CANCEL: 60+ second timeout)
   mvn clean package -DskipTests
   
   # Frontend build  
   cd woodlin-web && npm run build && cd ..
   
   # Combined build via script
   ./scripts/dev.sh build
   ```

2. **Linting**:
   ```bash
   # Frontend linting (takes ~1.7 seconds, may show errors/warnings)
   cd woodlin-web && npm run lint
   
   # Note: ESLint may show 7 errors and 17 warnings - this is expected
   # Most are related to unused imports and TypeScript any types
   ```

3. **Test Execution**:
   ```bash
   # Backend tests (NEVER CANCEL: 30+ second timeout)
   mvn test
   
   # Note: Some Spring tests may fail due to missing test dependencies
   # This is a known issue and does not affect application functionality
   ```

### Manual Application Testing
For complete validation, test these user scenarios:

1. **Frontend Development Server**: 
   ```bash
   cd woodlin-web && npm run dev
   # Should start in ~616ms and be accessible at http://localhost:5173/
   ```

2. **Backend API Access** (requires database):
   - Default credentials: `admin` / `Passw0rd`
   - API documentation: http://localhost:8080/api/doc.html
   - Database monitoring: http://localhost:8080/api/druid

3. **Docker Container Testing**:
   ```bash
   docker compose up -d
   # Wait 60-90 seconds for all services to start
   # Test API health: curl http://localhost:8080/api/actuator/health
   ```

## Common Issues and Solutions

### Build Issues
- **Missing @vicons/antd**: Fixed in package.json, but if missing: `cd woodlin-web && npm install @vicons/antd`
- **Maven dependency resolution**: Run `mvn install -DskipTests` before `mvn spring-boot:run`
- **Frontend TypeScript errors**: Run `npm run type-check` to see detailed type issues

### Runtime Issues  
- **Database connection failures**: Ensure MySQL 8.0+ is running and database `woodlin` exists
- **Redis connection failures**: Ensure Redis 6.0+ is running on port 6379
- **Port conflicts**: Backend uses 8080, frontend dev uses 5173, production frontend uses 3000

### Test Failures
- **Spring context test failure**: Known issue with JacksonConfig test - application works correctly
- **Maven test failures**: Some tests require full Spring Boot context - use `mvn test -DskipTests=false` for debugging

## Project Structure and Navigation

### Key Directories
```
woodlin/
├── woodlin-dependencies/           # BOM dependency management
├── woodlin-common/                # Core utilities and configurations  
├── woodlin-system/                # System modules aggregator (unified structure)
│   ├── woodlin-system-security/      # Authentication and authorization
│   ├── woodlin-system-core/          # System management (users, roles, depts)
│   ├── woodlin-system-tenant/        # Multi-tenant functionality
│   ├── woodlin-system-file/          # File management
│   ├── woodlin-system-task/          # Task scheduling  
│   ├── woodlin-system-generator/     # Code generation tools
│   └── woodlin-system-sql2api/       # SQL to API conversion
├── woodlin-dsl/                   # DSL module
├── woodlin-admin/                 # Main Spring Boot application
├── woodlin-web/                  # Vue 3 + TypeScript frontend
├── scripts/                      # Build and deployment scripts
├── sql/                          # Database scripts
└── .github/                      # GitHub configurations
```

### Important Files to Check After Changes
- **Backend API changes**: Always verify `woodlin-admin/src/main/resources/application.yml`
- **Frontend changes**: Check `woodlin-web/package.json` for dependency updates
- **Database changes**: Update database-specific SQL files in `sql/mysql/`, `sql/postgresql/`, or `sql/oracle/` directories
- **Build configuration**: Verify root `pom.xml` and module-specific `pom.xml` files

### Frequently Modified Files
- **Configuration**: `woodlin-admin/src/main/resources/application.yml`
- **Common constants**: `woodlin-common/src/main/java/com/mumu/woodlin/common/constant/`
- **Security policies**: `woodlin-system/woodlin-system-security/src/main/java/com/mumu/woodlin/security/service/`
- **Frontend routes**: `woodlin-web/src/router/index.ts`
- **Frontend API calls**: `woodlin-web/src/utils/request.ts`

## Environment Requirements and Setup

### Development Environment
- **Java**: 25 or higher (OpenJDK 25+36-LTS verified working)
- **Maven**: 3.8+ (Apache Maven 3.9.11 verified working)  
- **Node.js**: 20.19+ or 22.12+ (Node.js 20.19.5 verified working)
- **npm**: 10.8+ (npm 10.8.2 verified working)
- **Docker**: 20+ with Docker Compose v2 (Docker 28.0.4 verified working)

### Database Requirements
- **MySQL**: 8.0+ (configured with utf8mb4 charset)
- **Redis**: 6.0+ (used for caching and session management)
- **Database**: Create `woodlin` database and run SQL scripts in `sql/` directory

### Production Deployment
```bash
# Quick production deployment
./scripts/deploy.sh

# Manual Docker deployment
docker compose -f docker-compose.yml up -d

# Environment variables (see .env.example for full list)
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=your_password  
export REDIS_PASSWORD=your_redis_password
export SERVER_PORT=8080
```

## Performance Expectations

### Build Times (Actual Measured)
- **Maven clean compile**: 66 seconds (first time), 8-17 seconds (subsequent)
- **Maven clean package**: 17 seconds (after dependencies downloaded)
- **Frontend npm install**: 16 seconds
- **Frontend build**: 8-9 seconds  
- **Frontend dev server start**: 616ms
- **Combined build script**: 18-20 seconds
- **Maven install**: ~5 seconds (after build)

### Test and Lint Times
- **Maven tests**: 4-6 seconds (some tests fail - this is expected)
- **Frontend linting**: 1.7 seconds (shows warnings - this is expected)
- **Frontend type checking**: included in build process

### Application Startup Times
- **Frontend dev server**: ~616ms
- **Backend application**: 30-45 seconds with database connection
- **Docker compose full stack**: 60-90 seconds for all services

## Additional Notes

- **Default Credentials**: admin / Passw0rd (defined in SystemConstant.DEFAULT_PASSWORD)
- **Multi-tenant**: System supports tenant data isolation  
- **API Documentation**: Auto-generated with SpringDoc at `/api/doc.html`
- **Database Monitoring**: Druid monitoring at `/api/druid`
- **Health Checks**: Spring Actuator at `/api/actuator/health`
- **Code Generation**: Built-in code generation tools for CRUD operations
- **File Management**: Supports multiple storage backends
- **Task Scheduling**: Integrated Quartz-based job scheduling

**CRITICAL REMINDERS**:
- **NEVER CANCEL** build commands - they may take several minutes
- **ALWAYS** run `mvn install` before `mvn spring-boot:run`  
- **ALWAYS** test both backend and frontend after making changes
- **ALWAYS** use appropriate timeouts: 180+ seconds for builds, 120+ seconds for tests
- Set up MySQL and Redis before attempting to run the full application