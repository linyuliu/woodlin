# Frontend Enhancement Documentation

## Overview

This document describes the enhancements made to the Woodlin frontend application, including improved routing, state management, HTTP interceptors, and security features.

## Key Features

### 1. State Management with Pinia

Four comprehensive stores have been created to manage application state:

#### User Store (`stores/user.ts`)
- **Purpose**: Manages user information, permissions, and roles
- **Key Features**:
  - User profile information (id, username, email, etc.)
  - Permission checking (`hasPermission`, `hasAllPermissions`)
  - Role checking (`hasRole`)
  - Admin and super admin detection
  - User info fetching from API

#### Auth Store (`stores/auth.ts`)
- **Purpose**: Manages authentication state and tokens
- **Key Features**:
  - Token management (set, restore, clear)
  - Login/logout operations
  - Token expiration tracking
  - Remember me functionality
  - Token refresh (TODO)
  - Automatic token restoration from localStorage

#### Permission Store (`stores/permission.ts`)
- **Purpose**: Manages dynamic routes and menu permissions
- **Key Features**:
  - Dynamic route generation based on user permissions
  - Route filtering by permission
  - Menu generation for sidebar
  - Route searching by path or name

#### App Store (`stores/app.ts`)
- **Purpose**: Manages global application state
- **Key Features**:
  - Sidebar collapse state
  - Global loading state
  - Device type detection (mobile/tablet/desktop)
  - Settings drawer control
  - State persistence to localStorage

### 2. Enhanced HTTP Request Interceptor

The request interceptor (`utils/request.ts`) has been significantly enhanced:

#### Request Features
- **Automatic Token Injection**: Adds Bearer token to all authenticated requests
- **Tenant ID Header**: Adds X-Tenant-Id header for multi-tenant support
- **Request ID Tracking**: Generates unique request ID for debugging
- **Request Encryption**: Supports encrypting sensitive request data
- **Duplicate Request Cancellation**: Prevents duplicate requests
- **Loading State Management**: Shows/hides loading indicators

#### Response Features
- **Response Decryption**: Supports decrypting encrypted responses
- **Unified Error Handling**: Handles 401, 403, 404, 500 errors
- **Automatic Redirect**: Redirects to login on 401 errors
- **Request Retry**: Configurable retry logic for failed requests
- **Business Error Handling**: Parses backend error codes and messages

#### Configuration Options
```typescript
{
  encrypt: boolean,      // Encrypt request data
  decrypt: boolean,      // Decrypt response data
  showLoading: boolean,  // Show loading indicator
  showError: boolean,    // Show error message
  retry: boolean,        // Enable retry
  retryCount: number,    // Number of retries
  retryDelay: number,    // Delay between retries (ms)
  ignoreToken: boolean   // Skip token injection
}
```

### 3. Encryption/Decryption Support

The crypto utility (`utils/crypto.ts`) provides:

#### Current Implementation
- Base64 encoding/decoding
- Simple XOR encryption (for demonstration)
- RSA encryption placeholders (TODO)

#### TODO: Production Encryption
For production use, integrate a proper encryption library:
```bash
npm install crypto-js
# or
npm install jsencrypt
```

Example AES encryption:
```typescript
import CryptoJS from 'crypto-js'

export function aesEncrypt(data: any, key: string): string {
  const str = typeof data === 'string' ? data : JSON.stringify(data)
  return CryptoJS.AES.encrypt(str, key).toString()
}
```

### 4. Enhanced Route Guards

The route guards (`router/guards.ts`) provide comprehensive protection:

#### Authentication Guard
- Checks user login status
- Redirects to login page if not authenticated
- Preserves target URL for post-login redirect
- Loads user info and generates dynamic routes on first access

#### Permission Guard
- Checks route-level permissions
- Redirects to 403 page if user lacks permission
- Supports permission arrays (user needs any one permission)
- Super admin bypass

#### Other Guards
- **Title Guard**: Sets page title based on route meta
- **Progress Guard**: Shows loading bar during navigation (TODO)
- **Cache Guard**: Manages page caching with keep-alive (TODO)
- **Log Guard**: Records route access for audit

### 5. Dynamic Route System

Routes are organized into two categories:

#### Static Routes (`constantRoutes`)
- Login page
- Error pages (403, 404, 500)
- No authentication required

#### Dynamic Routes (`asyncRoutes`)
- Main application routes
- Filtered by user permissions
- Organized by functional modules:
  - **Dashboard**: System overview
  - **System Management**: Users, roles, depts, permissions, config, dict
  - **Data Source Management**: List, monitoring
  - **Tenant Management**: Tenant list
  - **File Management**: File list, storage config
  - **Task Management**: Task list, logs
  - **Dev Tools**: SQL to API, code generator

#### Route Meta Configuration
```typescript
{
  title: string,           // Page title
  icon: string,           // Icon name (ionicons5)
  permissions: string[],  // Required permissions
  hideInMenu: boolean,    // Hide from menu
  anonymous: boolean,     // Allow anonymous access
  affix: boolean,         // Pin to tab bar
  logAccess: boolean      // Log route access
}
```

### 6. Module Structure

```
woodlin-web/src/
├── stores/                 # Pinia state management
│   ├── user.ts            # User info and permissions
│   ├── auth.ts            # Authentication and tokens
│   ├── permission.ts      # Dynamic routes and menus
│   ├── app.ts             # Global app state
│   └── index.ts           # Store exports
├── router/                # Vue Router configuration
│   ├── index.ts           # Router instance
│   ├── routes.ts          # Route definitions
│   └── guards.ts          # Route guards
├── utils/                 # Utility functions
│   ├── request.ts         # Enhanced axios instance
│   └── crypto.ts          # Encryption utilities
├── views/                 # Page components
│   ├── error/             # Error pages (403, 404, 500)
│   ├── datasource/        # Data source management
│   ├── file/              # File management
│   ├── task/              # Task management
│   └── dev/               # Developer tools
└── api/                   # API service modules
```

## Usage Examples

### 1. Using Auth Store
```typescript
import { useAuthStore } from '@/stores'

const authStore = useAuthStore()

// Login
await authStore.doLogin({
  loginType: 'password',
  username: 'admin',
  password: 'password'
})

// Check authentication
if (authStore.isAuthenticated) {
  // User is logged in
}

// Logout
await authStore.doLogout()
```

### 2. Using User Store
```typescript
import { useUserStore } from '@/stores'

const userStore = useUserStore()

// Check permissions
if (userStore.hasPermission('system:user:add')) {
  // Show add user button
}

// Check roles
if (userStore.hasRole('admin')) {
  // Show admin features
}

// Get display name
const name = userStore.displayName
```

### 3. Making Encrypted API Requests
```typescript
import request from '@/utils/request'

// Encrypt sensitive data
const response = await request.post('/api/sensitive', data, {
  encrypt: true,
  decrypt: true
})

// Retry failed requests
const response = await request.get('/api/data', {
  retry: true,
  retryCount: 3,
  retryDelay: 1000
})
```

### 4. Adding Protected Routes
```typescript
// In routes.ts
{
  path: 'new-feature',
  name: 'NewFeature',
  component: () => import('@/views/NewFeature.vue'),
  meta: {
    title: 'New Feature',
    icon: 'star-outline',
    permissions: ['feature:view'] // Required permission
  }
}
```

## TODO Items

### High Priority
1. **Implement Real Encryption**: Replace simple XOR encryption with AES/RSA
2. **Token Refresh**: Implement automatic token refresh before expiration
3. **Progress Bar**: Integrate NProgress for route transitions
4. **Backend Integration**: Connect TODO-marked pages to actual APIs

### Medium Priority
1. **Page Caching**: Implement keep-alive based page caching
2. **Breadcrumb**: Add breadcrumb navigation component
3. **Route Transitions**: Add smooth page transition animations
4. **API Mocking**: Set up MSW for API mocking during development

### Low Priority
1. **Request Queue Management**: Advanced request deduplication
2. **Offline Support**: Service worker for offline functionality
3. **Performance Monitoring**: Track and report performance metrics

## Backend API Requirements

The frontend expects the following API endpoints:

### Authentication
- `POST /auth/login` - User login
- `POST /auth/logout` - User logout
- `GET /auth/userinfo` - Get current user info (with permissions array)
- `POST /auth/refresh` - Refresh access token (TODO)

### User Info Response Format
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "permissions": ["system:user:view", "system:role:view"],
    "roles": ["admin"],
    "nickname": "Administrator",
    "email": "admin@woodlin.com"
  }
}
```

## Migration Guide

If you have existing code using the old request utility:

### Before
```typescript
import request from '@/utils/request'
const token = localStorage.getItem('token')
```

### After
```typescript
import { useAuthStore } from '@/stores'
const authStore = useAuthStore()
const token = authStore.token
```

## Testing

### Manual Testing Checklist
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Token persistence after page reload
- [ ] Automatic redirect to login when token expires
- [ ] Permission-based route access
- [ ] 403 page when accessing forbidden route
- [ ] Remember me functionality
- [ ] Logout and clear token

### Build Testing
```bash
cd woodlin-web
npm run build      # Production build
npm run lint       # Linting
npm run type-check # TypeScript checking
```

## Security Considerations

1. **Token Storage**: Tokens are stored in localStorage. For higher security, consider httpOnly cookies
2. **Encryption**: Current implementation uses simple encryption. Replace with proper crypto libraries
3. **HTTPS**: Always use HTTPS in production
4. **CORS**: Configure proper CORS policies on backend
5. **CSP**: Implement Content Security Policy headers
6. **Input Validation**: Always validate and sanitize user input

## Performance Optimizations

1. **Lazy Loading**: All routes use lazy loading
2. **Code Splitting**: Vite automatically splits vendor code
3. **Request Deduplication**: Duplicate requests are cancelled
4. **State Persistence**: Minimal data stored in localStorage
5. **Tree Shaking**: Unused code is removed during build

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## References

- [Vue Router Documentation](https://router.vuejs.org/)
- [Pinia Documentation](https://pinia.vuejs.org/)
- [Axios Documentation](https://axios-http.com/)
- [vue-vben-admin](https://github.com/vbenjs/vue-vben-admin) - Reference implementation
- [Naive UI](https://www.naiveui.com/) - UI component library

## Support

For issues or questions, please:
1. Check this documentation
2. Review the code comments
3. Check the browser console for errors
4. Contact the development team
