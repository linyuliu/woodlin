export const API_SECURITY_MODES = {
  NONE: 'NONE',
  TOKEN: 'TOKEN',
  AKSK: 'AKSK',
  TOKEN_AND_AKSK: 'TOKEN_AND_AKSK'
} as const

export type ApiSecurityMode = (typeof API_SECURITY_MODES)[keyof typeof API_SECURITY_MODES]

export const API_SIGNATURE_ALGORITHMS = {
  HMAC_SHA256: 'HMAC_SHA256',
  HMAC_SHA512: 'HMAC_SHA512',
  RSA_SHA256: 'RSA_SHA256',
  SM2_SM3: 'SM2_SM3'
} as const

export type ApiSignatureAlgorithm = (typeof API_SIGNATURE_ALGORITHMS)[keyof typeof API_SIGNATURE_ALGORITHMS]

export const API_ENCRYPTION_ALGORITHMS = {
  NONE: 'NONE',
  AES_GCM: 'AES_GCM',
  AES_CBC: 'AES_CBC',
  RSA_OAEP_SHA256: 'RSA_OAEP_SHA256',
  SM4_CBC: 'SM4_CBC'
} as const

export type ApiEncryptionAlgorithm = (typeof API_ENCRYPTION_ALGORITHMS)[keyof typeof API_ENCRYPTION_ALGORITHMS]

export const OPEN_API_HEADERS = {
  AUTHORIZATION: 'Authorization',
  AUTHORIZATION_PREFIX: 'Bearer ',
  ACCESS_KEY: 'X-Access-Key',
  SIGNATURE_ALGORITHM: 'X-Signature-Algorithm',
  TIMESTAMP: 'X-Timestamp',
  NONCE: 'X-Nonce',
  SIGNATURE: 'X-Signature',
  ENCRYPT_ALGORITHM: 'X-Encrypt-Algorithm',
  TENANT_ID: 'X-Tenant-Id',
  REQUEST_ID: 'X-Request-Id'
} as const

export const OPEN_API_DEFAULTS = {
  securityMode: API_SECURITY_MODES.AKSK as ApiSecurityMode,
  signatureAlgorithm: API_SIGNATURE_ALGORITHMS.HMAC_SHA256 as ApiSignatureAlgorithm,
  encryptionAlgorithm: API_ENCRYPTION_ALGORITHMS.AES_GCM as ApiEncryptionAlgorithm,
  nonceCachePrefix: 'openapi:nonce:',
  timestampWindowSeconds: 300
} as const

export type SelectOption = {
  label: string
  value: string
}

export type DictLikeItem = {
  label: string | number
  value: string | number
}

export const OPEN_API_SECURITY_MODE_OPTIONS: SelectOption[] = [
  {label: '仅 Token', value: API_SECURITY_MODES.TOKEN},
  {label: '仅 AK/SK', value: API_SECURITY_MODES.AKSK},
  {label: 'Token + AK/SK', value: API_SECURITY_MODES.TOKEN_AND_AKSK},
  {label: '匿名', value: API_SECURITY_MODES.NONE}
]

export const OPEN_API_SIGNATURE_ALGORITHM_OPTIONS: SelectOption[] = [
  {label: 'HMAC-SHA256', value: API_SIGNATURE_ALGORITHMS.HMAC_SHA256},
  {label: 'HMAC-SHA512', value: API_SIGNATURE_ALGORITHMS.HMAC_SHA512},
  {label: 'RSA-SHA256', value: API_SIGNATURE_ALGORITHMS.RSA_SHA256},
  {label: 'SM2 + SM3', value: API_SIGNATURE_ALGORITHMS.SM2_SM3}
]

export const OPEN_API_ENCRYPTION_ALGORITHM_OPTIONS: SelectOption[] = [
  {label: 'NONE', value: API_ENCRYPTION_ALGORITHMS.NONE},
  {label: 'AES-GCM', value: API_ENCRYPTION_ALGORITHMS.AES_GCM},
  {label: 'AES-CBC', value: API_ENCRYPTION_ALGORITHMS.AES_CBC},
  {label: 'RSA-OAEP-SHA256', value: API_ENCRYPTION_ALGORITHMS.RSA_OAEP_SHA256},
  {label: 'SM4-CBC', value: API_ENCRYPTION_ALGORITHMS.SM4_CBC}
]

export const OPEN_API_HTTP_METHOD_OPTIONS: SelectOption[] = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', '*'].map(item => ({
  label: item,
  value: item
}))

export interface OpenApiCredentialMaterial {
  accessKey?: string
  secretKey?: string
  signaturePublicKey?: string
  signaturePrivateKey?: string
  encryptionPublicKey?: string
  encryptionPrivateKey?: string
  serverPublicKey?: string
  serverPrivateKey?: string
}

export interface OpenApiRequestContext {
  method: string
  path: string
  queryParameters?: Record<string, string | string[] | undefined>
  body?: string | Uint8Array | ArrayBuffer | Record<string, unknown> | null
  timestamp?: string
  nonce?: string
  tenantId?: string
  accessKey?: string
  requestId?: string
  signatureAlgorithm?: ApiSignatureAlgorithm
  encryptionAlgorithm?: ApiEncryptionAlgorithm
}

export interface OpenApiEncryptedPayload {
  algorithm: ApiEncryptionAlgorithm | string
  encrypted: string
  iv?: string
}

export interface OpenApiSignResult {
  accessKey: string
  timestamp: string
  nonce: string
  requestId?: string
  signatureAlgorithm: ApiSignatureAlgorithm
  encryptionAlgorithm?: ApiEncryptionAlgorithm
  signature: string
  canonicalRequest: string
}

export interface OpenApiVerifyResult {
  verified: boolean
  reason?: string
  canonicalRequest?: string
}

export interface OpenApiGmProvider {
  sign(params: {
    data: Uint8Array
    algorithm: ApiSignatureAlgorithm
    material: OpenApiCredentialMaterial
  }): Promise<string>

  verify(params: {
    data: Uint8Array
    signature: string
    algorithm: ApiSignatureAlgorithm
    material: OpenApiCredentialMaterial
  }): Promise<boolean>

  encrypt(params: {
    data: Uint8Array
    algorithm: ApiEncryptionAlgorithm
    material: OpenApiCredentialMaterial
  }): Promise<OpenApiEncryptedPayload>

  decrypt(params: {
    payload: OpenApiEncryptedPayload
    algorithm: ApiEncryptionAlgorithm
    material: OpenApiCredentialMaterial
  }): Promise<Uint8Array>
}

const GCM_IV_LENGTH = 12
const CBC_IV_LENGTH = 16
const RSA_DIRECT_PAYLOAD_MAX_LENGTH = 190
const textEncoder = new TextEncoder()
const textDecoder = new TextDecoder()

export function normalizeOpenApiDictOptions(items: DictLikeItem[] | undefined, fallbackOptions: SelectOption[]): SelectOption[] {
  const allowedMap = new Map(fallbackOptions.map(item => [String(item.value), item.label]))
  const normalized = (items || [])
    .map(item => ({label: String(item.label), value: String(item.value)}))
    .filter(item => allowedMap.has(item.value))
  return normalized.length > 0 ? normalized : fallbackOptions
}

export async function buildCanonicalRequest(requestContext: OpenApiRequestContext): Promise<string> {
  const normalized = normalizeRequestContext(requestContext, false)
  const bodyDigest = await sha256Hex(toBytes(normalized.body))
  return [
    normalized.method.trim().toUpperCase(),
    normalized.path || '',
    buildSortedQuery(normalized.queryParameters),
    bodyDigest,
    normalized.timestamp || '',
    normalized.nonce || '',
    normalized.tenantId || '',
    normalized.accessKey || ''
  ].join('\n')
}

export async function signRequest(
  requestContext: OpenApiRequestContext,
  material: OpenApiCredentialMaterial,
  gmProvider?: OpenApiGmProvider
): Promise<OpenApiSignResult> {
  const normalized = normalizeRequestContext(requestContext, true, material)
  const canonicalRequest = await buildCanonicalRequest(normalized)
  const signatureAlgorithm = normalized.signatureAlgorithm || OPEN_API_DEFAULTS.signatureAlgorithm
  const signature = await signBytes(textEncoder.encode(canonicalRequest), signatureAlgorithm, material, gmProvider)
  return {
    accessKey: normalized.accessKey || '',
    timestamp: normalized.timestamp || '',
    nonce: normalized.nonce || '',
    requestId: normalized.requestId,
    signatureAlgorithm,
    encryptionAlgorithm: normalized.encryptionAlgorithm,
    signature,
    canonicalRequest
  }
}

export async function verifyResponseSignature(
  requestContext: OpenApiRequestContext,
  signature: string,
  material: OpenApiCredentialMaterial,
  gmProvider?: OpenApiGmProvider
): Promise<OpenApiVerifyResult> {
  const normalized = normalizeRequestContext(requestContext, false, material)
  const canonicalRequest = await buildCanonicalRequest(normalized)
  const signatureAlgorithm = normalized.signatureAlgorithm || OPEN_API_DEFAULTS.signatureAlgorithm
  const verified = await verifyBytes(
    textEncoder.encode(canonicalRequest),
    signature,
    signatureAlgorithm,
    material,
    gmProvider
  )
  return {
    verified,
    reason: verified ? undefined : '签名校验失败',
    canonicalRequest
  }
}

export async function encryptPayload(
  plain: string | Uint8Array | ArrayBuffer | Record<string, unknown> | null | undefined,
  algorithm: ApiEncryptionAlgorithm,
  material: OpenApiCredentialMaterial,
  gmProvider?: OpenApiGmProvider
): Promise<OpenApiEncryptedPayload | null> {
  const plainBytes = toBytes(plain)
  if (algorithm === API_ENCRYPTION_ALGORITHMS.NONE) {
    return null
  }
  if (algorithm === API_ENCRYPTION_ALGORITHMS.SM4_CBC) {
    return requireGmProvider(gmProvider).encrypt({data: plainBytes, algorithm, material})
  }
  if (algorithm === API_ENCRYPTION_ALGORITHMS.AES_GCM) {
    const iv = randomBytes(GCM_IV_LENGTH)
    const key = await importAesKey(material.secretKey, 'AES-GCM')
    const encrypted = await getSubtleCrypto().encrypt({
      name: 'AES-GCM',
      iv,
      tagLength: 128
    }, key, plainBytes)
    return {
      algorithm,
      iv: bytesToBase64(iv),
      encrypted: bytesToBase64(new Uint8Array(encrypted))
    }
  }
  if (algorithm === API_ENCRYPTION_ALGORITHMS.AES_CBC) {
    const iv = randomBytes(CBC_IV_LENGTH)
    const key = await importAesKey(material.secretKey, 'AES-CBC')
    const encrypted = await getSubtleCrypto().encrypt({name: 'AES-CBC', iv}, key, plainBytes)
    return {
      algorithm,
      iv: bytesToBase64(iv),
      encrypted: bytesToBase64(new Uint8Array(encrypted))
    }
  }
  if (algorithm === API_ENCRYPTION_ALGORITHMS.RSA_OAEP_SHA256) {
    if (plainBytes.byteLength > RSA_DIRECT_PAYLOAD_MAX_LENGTH) {
      throw new Error('RSA_OAEP_SHA256 仅支持小报文或密钥封装场景')
    }
    const key = await importRsaPublicKey(firstNonEmpty(material.serverPublicKey, material.encryptionPublicKey), 'RSA-OAEP')
    const encrypted = await getSubtleCrypto().encrypt({name: 'RSA-OAEP'}, key, plainBytes)
    return {
      algorithm,
      encrypted: bytesToBase64(new Uint8Array(encrypted))
    }
  }
  throw new Error(`不支持的加密算法: ${algorithm}`)
}

export async function decryptPayload(
  payload: OpenApiEncryptedPayload,
  algorithm: ApiEncryptionAlgorithm,
  material: OpenApiCredentialMaterial,
  gmProvider?: OpenApiGmProvider
): Promise<Uint8Array> {
  if (!payload?.encrypted) {
    throw new Error('加密负载不能为空')
  }
  if (algorithm === API_ENCRYPTION_ALGORITHMS.SM4_CBC) {
    return requireGmProvider(gmProvider).decrypt({payload, algorithm, material})
  }
  if (algorithm === API_ENCRYPTION_ALGORITHMS.AES_GCM) {
    const key = await importAesKey(material.secretKey, 'AES-GCM')
    const decrypted = await getSubtleCrypto().decrypt(
      {
        name: 'AES-GCM',
        iv: base64ToBytes(requireText(payload.iv, '初始化向量不能为空')),
        tagLength: 128
      },
      key,
      base64ToBytes(payload.encrypted)
    )
    return new Uint8Array(decrypted)
  }
  if (algorithm === API_ENCRYPTION_ALGORITHMS.AES_CBC) {
    const key = await importAesKey(material.secretKey, 'AES-CBC')
    const decrypted = await getSubtleCrypto().decrypt(
      {name: 'AES-CBC', iv: base64ToBytes(requireText(payload.iv, '初始化向量不能为空'))},
      key,
      base64ToBytes(payload.encrypted)
    )
    return new Uint8Array(decrypted)
  }
  if (algorithm === API_ENCRYPTION_ALGORITHMS.RSA_OAEP_SHA256) {
    const key = await importRsaPrivateKey(firstNonEmpty(material.serverPrivateKey, material.encryptionPrivateKey), 'RSA-OAEP')
    const decrypted = await getSubtleCrypto().decrypt({name: 'RSA-OAEP'}, key, base64ToBytes(payload.encrypted))
    return new Uint8Array(decrypted)
  }
  throw new Error(`不支持的加密算法: ${algorithm}`)
}

export async function buildOpenApiHeaders(
  requestContext: OpenApiRequestContext,
  material: OpenApiCredentialMaterial,
  gmProvider?: OpenApiGmProvider
): Promise<Record<string, string>> {
  const signResult = await signRequest(requestContext, material, gmProvider)
  const headers: Record<string, string> = {
    [OPEN_API_HEADERS.ACCESS_KEY]: signResult.accessKey,
    [OPEN_API_HEADERS.SIGNATURE_ALGORITHM]: signResult.signatureAlgorithm,
    [OPEN_API_HEADERS.TIMESTAMP]: signResult.timestamp,
    [OPEN_API_HEADERS.NONCE]: signResult.nonce,
    [OPEN_API_HEADERS.SIGNATURE]: signResult.signature
  }
  if (signResult.encryptionAlgorithm && signResult.encryptionAlgorithm !== API_ENCRYPTION_ALGORITHMS.NONE) {
    headers[OPEN_API_HEADERS.ENCRYPT_ALGORITHM] = signResult.encryptionAlgorithm
  }
  if (requestContext.tenantId) {
    headers[OPEN_API_HEADERS.TENANT_ID] = requestContext.tenantId
  }
  if (signResult.requestId) {
    headers[OPEN_API_HEADERS.REQUEST_ID] = signResult.requestId
  }
  return headers
}

export function parseTimestamp(raw: string): Date {
  if (!raw?.trim()) {
    throw new Error('时间戳不能为空')
  }
  const normalized = raw.trim()
  if (/^\d{10}$/.test(normalized)) {
    return new Date(Number(normalized) * 1000)
  }
  if (/^\d{13}$/.test(normalized)) {
    return new Date(Number(normalized))
  }
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) {
    throw new Error(`无法解析时间戳: ${raw}`)
  }
  return date
}

export function isTimestampValid(raw: string, windowSeconds: number, now: Date = new Date()): boolean {
  const delta = Math.abs(Math.floor((parseTimestamp(raw).getTime() - now.getTime()) / 1000))
  return delta <= Math.max(windowSeconds, 0)
}

export function buildNonceKey(accessKey: string, nonce: string, prefix = OPEN_API_DEFAULTS.nonceCachePrefix): string {
  return `${prefix}${requireText(accessKey, 'accessKey 不能为空')}:${requireText(nonce, 'nonce 不能为空')}`
}

async function signBytes(
  data: Uint8Array,
  algorithm: ApiSignatureAlgorithm,
  material: OpenApiCredentialMaterial,
  gmProvider?: OpenApiGmProvider
): Promise<string> {
  if (algorithm === API_SIGNATURE_ALGORITHMS.HMAC_SHA256 || algorithm === API_SIGNATURE_ALGORITHMS.HMAC_SHA512) {
    const hash = algorithm === API_SIGNATURE_ALGORITHMS.HMAC_SHA256 ? 'SHA-256' : 'SHA-512'
    const key = await importHmacKey(material.secretKey, hash)
    const signature = await getSubtleCrypto().sign('HMAC', key, data)
    return bytesToBase64(new Uint8Array(signature))
  }
  if (algorithm === API_SIGNATURE_ALGORITHMS.RSA_SHA256) {
    const key = await importRsaPrivateKey(material.signaturePrivateKey, 'RSASSA-PKCS1-v1_5')
    const signature = await getSubtleCrypto().sign('RSASSA-PKCS1-v1_5', key, data)
    return bytesToBase64(new Uint8Array(signature))
  }
  return requireGmProvider(gmProvider).sign({data, algorithm, material})
}

async function verifyBytes(
  data: Uint8Array,
  signature: string,
  algorithm: ApiSignatureAlgorithm,
  material: OpenApiCredentialMaterial,
  gmProvider?: OpenApiGmProvider
): Promise<boolean> {
  if (algorithm === API_SIGNATURE_ALGORITHMS.HMAC_SHA256 || algorithm === API_SIGNATURE_ALGORITHMS.HMAC_SHA512) {
    const expected = await signBytes(data, algorithm, material, gmProvider)
    return expected === signature
  }
  if (algorithm === API_SIGNATURE_ALGORITHMS.RSA_SHA256) {
    const key = await importRsaPublicKey(material.signaturePublicKey, 'RSASSA-PKCS1-v1_5')
    return getSubtleCrypto().verify('RSASSA-PKCS1-v1_5', key, base64ToBytes(signature), data)
  }
  return requireGmProvider(gmProvider).verify({data, signature, algorithm, material})
}

function normalizeRequestContext(
  requestContext: OpenApiRequestContext,
  fillDefaults: boolean,
  material?: OpenApiCredentialMaterial
): OpenApiRequestContext {
  const nowTimestamp = Math.floor(Date.now() / 1000).toString()
  return {
    ...requestContext,
    accessKey: requestContext.accessKey || material?.accessKey || '',
    timestamp: requestContext.timestamp || (fillDefaults ? nowTimestamp : ''),
    nonce: requestContext.nonce || (fillDefaults ? generateNonce() : ''),
    requestId: requestContext.requestId || (fillDefaults ? generateRequestId() : undefined),
    signatureAlgorithm: requestContext.signatureAlgorithm || OPEN_API_DEFAULTS.signatureAlgorithm,
    encryptionAlgorithm: requestContext.encryptionAlgorithm || API_ENCRYPTION_ALGORITHMS.NONE
  }
}

function buildSortedQuery(queryParameters: OpenApiRequestContext['queryParameters']): string {
  if (!queryParameters || Object.keys(queryParameters).length === 0) {
    return ''
  }
  return Object.keys(queryParameters)
    .sort()
    .flatMap(key => {
      const rawValue = queryParameters[key]
      const values = Array.isArray(rawValue) ? [...rawValue] : [rawValue || '']
      return values.sort().map(value => `${encodeComponent(key)}=${encodeComponent(String(value ?? ''))}`)
    })
    .join('&')
}

async function sha256Hex(data: Uint8Array): Promise<string> {
  const digest = await getSubtleCrypto().digest('SHA-256', data)
  return Array.from(new Uint8Array(digest))
    .map(value => value.toString(16).padStart(2, '0'))
    .join('')
}

async function importHmacKey(secretKey: string | undefined, hash: 'SHA-256' | 'SHA-512'): Promise<CryptoKey> {
  const derived = await deriveSecretKey(secretKey)
  return getSubtleCrypto().importKey('raw', derived, {
    name: 'HMAC',
    hash
  }, false, ['sign', 'verify'])
}

async function importAesKey(secretKey: string | undefined, algorithm: 'AES-GCM' | 'AES-CBC'): Promise<CryptoKey> {
  const derived = await deriveSecretKey(secretKey)
  return getSubtleCrypto().importKey('raw', derived, {
    name: algorithm,
    length: 256
  }, false, ['encrypt', 'decrypt'])
}

async function deriveSecretKey(secretKey: string | undefined): Promise<Uint8Array> {
  const digest = await getSubtleCrypto().digest('SHA-256', textEncoder.encode(requireText(secretKey, '密钥不能为空')))
  return new Uint8Array(digest).slice(0, 32)
}

async function importRsaPrivateKey(privateKey: string | undefined, algorithm: 'RSASSA-PKCS1-v1_5' | 'RSA-OAEP'): Promise<CryptoKey> {
  return getSubtleCrypto().importKey(
    'pkcs8',
    decodePem(privateKey, '密钥不能为空'),
    {name: algorithm, hash: 'SHA-256'},
    false,
    algorithm === 'RSA-OAEP' ? ['decrypt'] : ['sign']
  )
}

async function importRsaPublicKey(publicKey: string | undefined, algorithm: 'RSASSA-PKCS1-v1_5' | 'RSA-OAEP'): Promise<CryptoKey> {
  return getSubtleCrypto().importKey(
    'spki',
    decodePem(publicKey, '密钥不能为空'),
    {name: algorithm, hash: 'SHA-256'},
    false,
    algorithm === 'RSA-OAEP' ? ['encrypt'] : ['verify']
  )
}

function decodePem(rawKey: string | undefined, message: string): Uint8Array {
  const normalized = requireText(rawKey, message)
    .replace(/-----BEGIN [A-Z ]+-----/g, '')
    .replace(/-----END [A-Z ]+-----/g, '')
    .replace(/\s+/g, '')
  return base64ToBytes(normalized)
}

function toBytes(data: OpenApiRequestContext['body']): Uint8Array {
  if (data == null) {
    return new Uint8Array()
  }
  if (data instanceof Uint8Array) {
    return data
  }
  if (data instanceof ArrayBuffer) {
    return new Uint8Array(data)
  }
  if (typeof data === 'string') {
    return textEncoder.encode(data)
  }
  return textEncoder.encode(JSON.stringify(data))
}

function bytesToBase64(bytes: Uint8Array): string {
  if (typeof btoa === 'function') {
    let binary = ''
    bytes.forEach(value => {
      binary += String.fromCharCode(value)
    })
    return btoa(binary)
  }
  return Buffer.from(bytes).toString('base64')
}

function base64ToBytes(value: string): Uint8Array {
  if (typeof atob === 'function') {
    const binary = atob(value)
    return Uint8Array.from(binary, char => char.charCodeAt(0))
  }
  return new Uint8Array(Buffer.from(value, 'base64'))
}

function randomBytes(length: number): Uint8Array {
  const bytes = new Uint8Array(length)
  getCryptoRoot().getRandomValues(bytes)
  return bytes
}

function generateNonce(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID().replace(/-/g, '')
  }
  return `${Date.now().toString(16)}${Math.random().toString(16).slice(2)}`
}

function generateRequestId(): string {
  return `${Date.now().toString(16)}-${generateNonce().slice(0, 12)}`
}

function encodeComponent(value: string): string {
  return encodeURIComponent(value ?? '').replace(/\+/g, '%20')
}

function requireText(value: string | undefined, message: string): string {
  if (!value?.trim()) {
    throw new Error(message)
  }
  return value.trim()
}

function firstNonEmpty(...values: Array<string | undefined>): string {
  const matched = values.find(value => value?.trim())
  if (!matched) {
    throw new Error('密钥不能为空')
  }
  return matched.trim()
}

function requireGmProvider(gmProvider?: OpenApiGmProvider): OpenApiGmProvider {
  if (!gmProvider) {
    throw new Error('当前算法需要注入 gmProvider')
  }
  return gmProvider
}

function getCryptoRoot(): Crypto {
  if (typeof globalThis !== 'undefined' && globalThis.crypto) {
    return globalThis.crypto
  }
  throw new Error('当前环境不支持 Web Crypto API')
}

function getSubtleCrypto(): SubtleCrypto {
  const subtle = getCryptoRoot().subtle
  if (!subtle) {
    throw new Error('当前环境不支持 SubtleCrypto')
  }
  return subtle
}

export function bytesToUtf8(bytes: Uint8Array): string {
  return textDecoder.decode(bytes)
}
