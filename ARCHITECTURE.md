# 前端架构图 / Frontend Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            Woodlin Frontend Application                     │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              User Interface Layer                            │
├─────────────────────────────────────────────────────────────────────────────┤
│  LoginView.vue                                                              │
│  ├── Uses: AuthStore.doLogin()                                              │
│  └── Features: Remember Me, Password Policy                                 │
│                                                                              │
│  AdminLayout.vue                                                            │
│  ├── Uses: AuthStore (logout), AppStore (sidebar)                           │
│  ├── Components: AppHeader, AppSidebar, AppContent                          │
│  └── Menu: Static (TODO: Dynamic from routes)                               │
│                                                                              │
│  Error Pages: 403.vue, 404.vue, 500.vue                                     │
│                                                                              │
│  Module Pages (TODO Backend Integration):                                   │
│  ├── System Management: User, Role, Dept, Permission, Dict, Config          │
│  ├── Datasource: List, Monitor                                              │
│  ├── File: List, Storage                                                    │
│  ├── Task: List, Log                                                        │
│  └── Dev: SQL2API, CodeGenerator                                            │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           State Management Layer (Pinia)                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ UserStore    │  │  AuthStore   │  │ PermStore    │  │  AppStore    │   │
│  ├──────────────┤  ├──────────────┤  ├──────────────┤  ├──────────────┤   │
│  │• userInfo    │  │• token       │  │• routes      │  │• sidebar     │   │
│  │• permissions │  │• tokenType   │  │• addedRoutes │  │• loading     │   │
│  │• roles       │  │• expiresIn   │  │• menuRoutes  │  │• device      │   │
│  │              │  │• loginTime   │  │              │  │• showSettings│   │
│  ├──────────────┤  ├──────────────┤  ├──────────────┤  ├──────────────┤   │
│  │Methods:      │  │Methods:      │  │Methods:      │  │Methods:      │   │
│  │• hasPermiss..│  │• doLogin()   │  │• generate..  │  │• toggle..    │   │
│  │• hasRole()   │  │• doLogout()  │  │• filterAsync │  │• showLoading │   │
│  │• fetchUser.. │  │• setToken()  │  │• clearRoutes │  │• setDevice() │   │
│  │• isAdmin     │  │• checkRefr.. │  │• findRoute.. │  │              │   │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                            Routing & Guard Layer                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Router Instance (router/index.ts)                                          │
│  └── Initial Routes: constantRoutes (Login, Error Pages)                    │
│                                                                              │
│  Route Definitions (router/routes.ts)                                       │
│  ├── constantRoutes: Login, 403, 404, 500                                   │
│  └── asyncRoutes: Dynamic routes filtered by permissions                    │
│                                                                              │
│  Route Guards (router/guards.ts) - Execution Order:                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 1. Auth Guard                                                        │   │
│  │    ├── Check if route in whitelist → Allow                           │   │
│  │    ├── Check authentication → Redirect to login if false            │   │
│  │    ├── Load user info if not loaded                                 │   │
│  │    └── Generate dynamic routes on first access                      │   │
│  │                                                                      │   │
│  │ 2. Permission Guard                                                 │   │
│  │    ├── Check if permission validation enabled                       │   │
│  │    ├── Get route permissions from meta                              │   │
│  │    ├── Check user permissions → Redirect to 403 if false           │   │
│  │    └── Allow if has permission or super admin                       │   │
│  │                                                                      │   │
│  │ 3. Title Guard (afterEach)                                          │   │
│  │    └── Set document.title from route.meta.title                     │   │
│  │                                                                      │   │
│  │ 4. Progress Guard (TODO)                                            │   │
│  │    └── Show/hide NProgress loading bar                              │   │
│  │                                                                      │   │
│  │ 5. Cache Guard (TODO)                                               │   │
│  │    └── Manage keep-alive cache                                      │   │
│  │                                                                      │   │
│  │ 6. Log Guard (afterEach)                                            │   │
│  │    └── Log route changes for audit                                  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                          HTTP Request Layer (Axios)                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Request Interceptor (utils/request.ts)                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Outgoing Request Processing:                                        │   │
│  │ ┌─────────────────────────────────────────────────────────────────┐ │   │
│  │ │ 1. Cancel duplicate request                                     │ │   │
│  │ │ 2. Add to request queue                                         │ │   │
│  │ │ 3. Inject Bearer token (from localStorage/AuthStore)            │ │   │
│  │ │ 4. Add X-Tenant-Id header                                       │ │   │
│  │ │ 5. Add X-Request-Id (timestamp + random)                        │ │   │
│  │ │ 6. Encrypt request data (if config.encrypt = true)              │ │   │
│  │ │ 7. Show loading indicator (if config.showLoading != false)      │ │   │
│  │ │ 8. Log request: method + URL                                    │ │   │
│  │ └─────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  Response Interceptor (utils/request.ts)                                    │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Incoming Response Processing:                                       │   │
│  │ ┌─────────────────────────────────────────────────────────────────┐ │   │
│  │ │ Success Path:                                                   │ │   │
│  │ │ 1. Remove from request queue                                    │ │   │
│  │ │ 2. Hide loading indicator                                       │ │   │
│  │ │ 3. Decrypt response data (if config.decrypt = true)             │ │   │
│  │ │ 4. Check business code (code !== 200 → reject)                  │ │   │
│  │ │ 5. Return data.data                                             │ │   │
│  │ │                                                                  │ │   │
│  │ │ Error Path:                                                     │ │   │
│  │ │ 1. Remove from request queue                                    │ │   │
│  │ │ 2. Hide loading indicator                                       │ │   │
│  │ │ 3. Handle HTTP status:                                          │ │   │
│  │ │    ├── 401 → Clear token, redirect to login                    │ │   │
│  │ │    ├── 403 → Log permission error                              │ │   │
│  │ │    ├── 404 → Log not found                                     │ │   │
│  │ │    ├── 500 → Log server error                                  │ │   │
│  │ │    └── ECONNABORTED → Log timeout                              │ │   │
│  │ │ 4. Retry if configured (retry, retryCount, retryDelay)         │ │   │
│  │ │ 5. Show error message (if config.showError != false)            │ │   │
│  │ └─────────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  Request Configuration Options:                                             │
│  {                                                                           │
│    encrypt: boolean,      // Encrypt request data                           │
│    decrypt: boolean,      // Decrypt response data                          │
│    showLoading: boolean,  // Show loading indicator                         │
│    showError: boolean,    // Show error message                             │
│    retry: boolean,        // Enable retry                                   │
│    retryCount: number,    // Number of retries                              │
│    retryDelay: number,    // Delay between retries (ms)                     │
│    ignoreToken: boolean   // Skip token injection                           │
│  }                                                                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           Utilities & Helper Layer                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Crypto Utilities (utils/crypto.ts)                                         │
│  ├── base64Encode/Decode: Base64 encoding                                   │
│  ├── simpleEncrypt/Decrypt: XOR encryption (demo only)                      │
│  └── rsaEncrypt/Decrypt: RSA placeholder (TODO: use jsencrypt)              │
│                                                                              │
│  Menu Generator (utils/menu-generator.ts)                                   │
│  └── generateMenuFromRoutes(): Generate menu from route config              │
│                                                                              │
│  Config (config/index.ts)                                                   │
│  ├── System: title, version, locale                                         │
│  ├── Layout: sidebar, header, tabs                                          │
│  ├── Theme: colors, mode                                                    │
│  ├── HTTP: baseURL, timeout, token settings                                 │
│  └── Router: mode, loginPath, homePath, permissions                         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              Backend API Layer                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Authentication APIs (api/auth.ts)                                          │
│  ├── POST /auth/login         → LoginResponse { token, expiresIn }          │
│  ├── POST /auth/logout        → void                                        │
│  ├── GET  /auth/userinfo      → UserInfo { id, username, permissions[] }    │
│  ├── GET  /auth/captcha       → CaptchaInfo { uuid, image }                 │
│  └── POST /auth/refresh       → (TODO) RefreshResponse                      │
│                                                                              │
│  Other API Modules:                                                         │
│  ├── api/user.ts              → User management                             │
│  ├── api/role.ts              → Role management                             │
│  ├── api/tenant.ts            → Tenant management                           │
│  ├── api/dict.ts              → Dictionary management                       │
│  ├── api/config.ts            → System config                               │
│  ├── api/datasource.ts        → (TODO) Data source APIs                     │
│  ├── api/file.ts              → (TODO) File management APIs                 │
│  └── api/task.ts              → (TODO) Task scheduling APIs                 │
│                                                                              │
│  Expected Response Format:                                                  │
│  {                                                                           │
│    "code": 200,                  // 200 = success, others = error           │
│    "message": "success",         // Error message if code != 200            │
│    "data": { ... },              // Actual response data                    │
│    "timestamp": "2025-01-01..."  // Optional timestamp                      │
│  }                                                                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                            Data Flow Diagram                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  User Login Flow:                                                           │
│  ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐       │
│  │ User   │───>│ Login  │───>│ Auth   │───>│Request │───>│Backend │       │
│  │ Input  │    │ View   │    │ Store  │    │Intercep│    │  API   │       │
│  └────────┘    └────────┘    └────────┘    └────────┘    └────────┘       │
│                                   │                             │            │
│                                   │ Set Token                   │            │
│                                   ↓                             ↓            │
│  ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐       │
│  │ Admin  │<───│ Router │<───│ User   │<───│ Perm   │<───│Response│       │
│  │ Layout │    │ Push   │    │ Store  │    │ Store  │    │ Data   │       │
│  └────────┘    └────────┘    └────────┘    └────────┘    └────────┘       │
│                                   │             │                            │
│                        Fetch User Info    Generate Routes                   │
│                                                                              │
│  Authenticated API Request Flow:                                            │
│  ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐                     │
│  │Component──>│ API    │───>│Request │───>│Backend │                     │
│  │        │    │Function│    │+Token  │    │  API   │                     │
│  └────────┘    └────────┘    │+Tenant │    └────────┘                     │
│                               │+ReqId  │         │                          │
│                               │+Encrypt│         │                          │
│                               └────────┘         ↓                          │
│  ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐                     │
│  │Component<───│Response│<───│Intercep│<───│Backend │                     │
│  │Update  │    │ Data   │    │+Decrypt│    │Response│                     │
│  └────────┘    └────────┘    │+Error  │    └────────┘                     │
│                               └────────┘                                    │
│                                                                              │
│  Route Navigation Flow:                                                     │
│  ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐      │
│  │ User   │───>│ Router │───>│  Auth  │───>│ Perm   │───>│ Allow  │      │
│  │ Click  │    │Navigate│    │ Guard  │    │ Guard  │    │ Access │      │
│  └────────┘    └────────┘    └────────┘    └────────┘    └────────┘      │
│                                   │             │              │            │
│                          Check Auth   Check Permission   Render View       │
│                                   ↓             ↓              ↓            │
│                            Redirect Login  Redirect 403  Component         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                          Security & Best Practices                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ✅ Implemented:                                                             │
│  • Token-based authentication (Bearer token in Authorization header)        │
│  • Auto token injection to all requests                                     │
│  • Permission-based route access control                                    │
│  • Request/response encryption support (simple XOR for demo)                │
│  • Request deduplication to prevent duplicate calls                         │
│  • 401/403 auto redirect with state cleanup                                 │
│  • Token expiration detection                                               │
│  • XSS protection via proper escaping in templates                          │
│                                                                              │
│  ⚠️ TODO for Production:                                                    │
│  • Replace XOR encryption with AES/RSA (use crypto-js or jsencrypt)         │
│  • Implement token refresh before expiration                                │
│  • Use httpOnly cookies for token storage (more secure than localStorage)   │
│  • Add CSRF token support                                                   │
│  • Implement Content Security Policy (CSP)                                  │
│  • Add request rate limiting                                                │
│  • Enable HTTPS in production                                               │
│  • Implement proper input validation and sanitization                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           Development Checklist                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Backend Requirements:                                                      │
│  □ Implement /auth/login endpoint                                           │
│  □ Implement /auth/logout endpoint                                          │
│  □ Implement /auth/userinfo with permissions array                          │
│  □ Implement /auth/refresh for token renewal                                │
│  □ Return consistent response format { code, message, data }                │
│  □ Support Bearer token authentication                                      │
│  □ Support X-Tenant-Id header for multi-tenancy                             │
│                                                                              │
│  Frontend TODO:                                                             │
│  □ Install crypto-js: npm install crypto-js                                 │
│  □ Replace simpleEncrypt with real AES encryption                           │
│  □ Install nprogress: npm install nprogress                                 │
│  □ Integrate NProgress in route guards                                      │
│  □ Connect datasource pages to backend APIs                                 │
│  □ Connect file management pages to backend APIs                            │
│  □ Connect task management pages to backend APIs                            │
│  □ Implement dynamic menu from routes using menu-generator                  │
│  □ Add breadcrumb navigation component                                      │
│  □ Implement keep-alive page caching                                        │
│                                                                              │
│  Testing:                                                                   │
│  □ Test login with valid credentials                                        │
│  □ Test login with invalid credentials                                      │
│  □ Test token persistence after page reload                                 │
│  □ Test auto redirect to login when token expires                           │
│  □ Test permission-based route access                                       │
│  □ Test 403 page when accessing forbidden route                             │
│  □ Test remember me functionality                                           │
│  □ Test logout and token cleanup                                            │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Files Location

```
woodlin-web/
├── src/
│   ├── stores/                     # State management
│   │   ├── user.ts                 # User info & permissions
│   │   ├── auth.ts                 # Authentication & tokens
│   │   ├── permission.ts           # Dynamic routes
│   │   └── app.ts                  # Global app state
│   ├── router/                     # Routing
│   │   ├── index.ts                # Router instance
│   │   ├── routes.ts               # Route definitions
│   │   └── guards.ts               # Route guards
│   ├── utils/                      # Utilities
│   │   ├── request.ts              # Enhanced axios
│   │   ├── crypto.ts               # Encryption
│   │   └── menu-generator.ts      # Dynamic menu
│   ├── api/                        # API services
│   │   ├── auth.ts                 # Auth APIs
│   │   └── ...                     # Other APIs
│   └── views/                      # Page components
│       ├── LoginView.vue           # Login page
│       ├── error/                  # Error pages
│       ├── datasource/             # Datasource pages (TODO)
│       ├── file/                   # File pages (TODO)
│       ├── task/                   # Task pages (TODO)
│       └── dev/                    # Dev tool pages (TODO)
├── FRONTEND_ENHANCEMENTS.md        # Detailed documentation
└── package.json                    # Dependencies

/
└── IMPLEMENTATION_SUMMARY.md       # This summary
```

## Reference Links

- **vue-vben-admin**: https://github.com/vbenjs/vue-vben-admin
- **Vue Router**: https://router.vuejs.org/
- **Pinia**: https://pinia.vuejs.org/
- **Naive UI**: https://www.naiveui.com/
- **Axios**: https://axios-http.com/
- **crypto-js**: https://www.npmjs.com/package/crypto-js
- **jsencrypt**: https://www.npmjs.com/package/jsencrypt
- **nprogress**: https://www.npmjs.com/package/nprogress
