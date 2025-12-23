# Dynamic Routing Implementation

## Overview

This document describes the dynamic routing feature implementation for the Woodlin Multi-Tenant Management System. The system now supports dynamic menu/route generation based on user permissions stored in the database.

## Changes Made

### Backend Changes

1. **RouteVO DTO** (`woodlin-system-core/src/main/java/com/mumu/woodlin/system/dto/RouteVO.java`)
   - Data transfer object for route information
   - Contains route metadata (path, component, icon, permissions, etc.)
   - Supports nested route structure with children

2. **ISysPermissionService Interface** (`woodlin-system-core/src/main/java/com/mumu/woodlin/system/service/ISysPermissionService.java`)
   - Added `selectRoutesByUserId(Long userId)` method
   - Returns user's accessible routes as a tree structure

3. **SysPermissionServiceImpl** (`woodlin-system-core/src/main/java/com/mumu/woodlin/system/service/impl/SysPermissionServiceImpl.java`)
   - Implemented route tree building logic
   - Filters permissions by type (M=Menu, C=Component)
   - Converts permissions to RouteVO format
   - Builds hierarchical route structure

4. **AuthController** (`woodlin-admin/src/main/java/com/mumu/woodlin/admin/controller/AuthController.java`)
   - Added `GET /auth/routes` endpoint
   - Returns user's accessible routes based on their permissions
   - Requires authentication

### Frontend Changes

1. **API Client** (`woodlin-web/src/api/auth.ts`)
   - Added `getUserRoutes()` method
   - Fetches routes from backend `/auth/routes` endpoint

2. **Permission Store** (`woodlin-web/src/stores/permission.ts`)
   - Updated `generateRoutes()` to fetch from backend
   - Added `convertBackendRoutesToVueRouter()` to convert backend routes to Vue Router format
   - Added `loadComponent()` for dynamic component loading
   - Includes fallback to static routes if backend fails

3. **Route Structure**
   - Routes are now loaded from backend dynamically
   - Components are loaded lazily using dynamic imports
   - Fallback mechanism ensures app works even if backend is unavailable

### Database Changes

1. **Enhanced Permissions SQL** (`sql/mysql/enhanced_permissions.sql`)
   - Comprehensive permission/menu data structure
   - Matches frontend route hierarchy
   - Includes all modules: Dashboard, System, Datasource, Tenant, File, Task, Dev
   - Admin role has all permissions
   - Common role has view-only permissions

## Database Schema

The `sys_permission` table stores menu/route information:

| Field | Type | Description |
|-------|------|-------------|
| permission_id | bigint | Primary key |
| parent_id | bigint | Parent permission ID (0 for root) |
| permission_name | varchar | Display name |
| permission_code | varchar | Permission identifier |
| permission_type | char | M=Menu/Directory, C=Component/Page, F=Function/Button |
| path | varchar | Route path |
| component | varchar | Component path |
| icon | varchar | Icon name |
| sort_order | int | Display order |
| status | char | 1=Enabled, 0=Disabled |
| visible | char | 1=Visible, 0=Hidden |

## Route Structure

Routes are organized hierarchically:

```
/ (Root with AdminLayout)
├── dashboard (Dashboard)
├── system (System Management)
│   ├── user (User Management)
│   ├── role (Role Management)
│   ├── dept (Department Management)
│   ├── permission (Permission Management)
│   ├── dict (Dictionary Management)
│   ├── config (Config Management)
│   └── settings (System Settings)
├── datasource (Datasource Management)
│   ├── list (Datasource List)
│   └── monitor (Datasource Monitor)
├── tenant (Tenant Management)
│   └── list (Tenant List)
├── file (File Management)
│   ├── list (File List)
│   └── storage (Storage Config)
├── task (Task Management)
│   ├── list (Task List)
│   └── log (Task Log)
└── dev (Development Tools)
    ├── sql2api (SQL to API)
    └── generator (Code Generator)
```

## How It Works

1. **User Login**
   - User authenticates through `/auth/login`
   - Backend creates session with user permissions

2. **Fetch User Info**
   - Frontend calls `/auth/userinfo` to get user details
   - User info includes permissions list

3. **Generate Routes**
   - Frontend calls `/auth/routes` to get user's accessible routes
   - Backend queries `sys_permission` table based on user roles
   - Returns filtered route tree structure

4. **Build Frontend Routes**
   - Frontend converts backend routes to Vue Router format
   - Components are loaded dynamically
   - Routes are added to Vue Router
   - 404 catch-all route is added last

5. **Render Menu**
   - Sidebar menu is generated from routes
   - Only visible routes are shown
   - Menu respects hierarchy and ordering

## Deployment Steps

### 1. Apply Database Changes

```bash
# Connect to MySQL
mysql -u root -p woodlin

# Run the enhanced permissions script
source sql/mysql/enhanced_permissions.sql

# Verify permissions
SELECT COUNT(*) FROM sys_permission;
SELECT COUNT(*) FROM sys_role_permission WHERE role_id = 1;
```

Expected results:
- Total permissions: ~40+
- Admin permissions: ~40+ (all)
- Common user permissions: ~20+ (view only)

### 2. Build and Deploy Backend

```bash
# Build the backend
mvn clean package -DskipTests

# Run the application
java -jar woodlin-admin/target/woodlin-admin-1.0.0.jar
```

### 3. Build and Deploy Frontend

```bash
cd woodlin-web

# Install dependencies
npm install

# Build for production
npm run build

# Or run development server
npm run dev
```

### 4. Verify Deployment

1. Access the application: http://localhost:5173 (dev) or http://localhost:8080 (production)
2. Login with admin credentials: `admin` / `Passw0rd`
3. Check browser console for route loading logs
4. Verify all menus are visible
5. Check that routes are loaded from backend

## Testing

### Manual Testing Steps

1. **Test Admin User (Full Access)**
   - Login as `admin`
   - Verify all menu items are visible
   - Check browser console logs: "✅ 成功获取后端路由"
   - Navigate to each menu item
   - Confirm all pages load correctly

2. **Test Common User (Limited Access)**
   - Login as `demo`
   - Verify only view menus are visible
   - Button-level operations should not be available
   - Check that restricted menus are hidden

3. **Test Backend API**
   ```bash
   # Get auth token first
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"loginType":"password","username":"admin","password":"Passw0rd"}'
   
   # Use token to get routes
   curl http://localhost:8080/api/auth/routes \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

4. **Test Fallback Mechanism**
   - Stop backend
   - Refresh frontend
   - Verify static routes are used as fallback
   - Check console logs: "⚠️ 后端未返回路由，使用静态路由"

### Automated Testing

Add integration tests:

```java
@Test
public void testGetUserRoutes() {
    // Login as admin
    LoginResponse loginResponse = authController.login(
        new LoginRequest().setUsername("admin").setPassword("Passw0rd")
    );
    
    // Get routes
    R<List<RouteVO>> routes = authController.getUserRoutes();
    
    // Verify
    assertNotNull(routes);
    assertTrue(routes.getData().size() > 0);
}
```

## Troubleshooting

### Issue: No routes returned from backend

**Symptoms:**
- Empty menu in frontend
- Console shows: "⚠️ 后端未返回路由"

**Solutions:**
1. Check if user has any permissions assigned
2. Verify `sys_role_permission` table has data
3. Check if permissions are enabled (status = '1')
4. Look at backend logs for errors

### Issue: Routes not loading dynamically

**Symptoms:**
- Static routes are always used
- Console shows fallback messages

**Solutions:**
1. Check network tab for `/auth/routes` API call
2. Verify backend is running
3. Check CORS settings if frontend and backend on different domains
4. Verify authentication token is valid

### Issue: Component not found errors

**Symptoms:**
- 404 page shown for valid routes
- Console error: "⚠️ 找不到组件"

**Solutions:**
1. Verify component path in database matches actual file path
2. Check component file exists in `woodlin-web/src/views/`
3. Ensure component path format: `system/UserView` (no .vue extension)
4. Check dynamic import glob pattern in permission store

### Issue: Permission denied errors

**Symptoms:**
- Redirected to 403 page
- Console shows: "🚫 用户无权限访问该页面"

**Solutions:**
1. Check user's role assignments in `sys_user_role`
2. Verify role has required permissions in `sys_role_permission`
3. Check route meta permissions match database permission codes
4. Review permission guard logic in router guards

## Future Enhancements

1. **Permission Caching**
   - Cache route data in Redis
   - Reduce database queries
   - Improve performance

2. **Real-time Updates**
   - WebSocket notifications for permission changes
   - Auto-refresh routes when permissions updated
   - No need to re-login

3. **Route Templates**
   - Admin UI for managing routes
   - Drag-and-drop route builder
   - Visual route hierarchy editor

4. **Multi-language Support**
   - I18n for route titles
   - Language-specific menu labels
   - Dynamic translation loading

5. **Route Analytics**
   - Track route access frequency
   - Monitor user navigation patterns
   - Performance metrics per route

## References

- [Vue Router Documentation](https://router.vuejs.org/)
- [Dynamic Route Matching](https://router.vuejs.org/guide/essentials/dynamic-matching.html)
- [RBAC Permission Model](https://en.wikipedia.org/wiki/Role-based_access_control)
- [Woodlin Architecture](../../ARCHITECTURE.md)
