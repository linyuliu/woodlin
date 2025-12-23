/**
 * 加密解密工具模块
 * 
 * @author mumu
 * @description 提供数据加密和解密功能，用于敏感数据传输
 * @since 2025-01-01
 */

/**
 * Base64编码
 * @param str 要编码的字符串
 * @returns Base64编码后的字符串
 */
export function base64Encode(str: string): string {
  try {
    return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (match, p1) => {
      return String.fromCharCode(parseInt(p1, 16))
    }))
  } catch (error) {
    console.error('Base64编码失败:', error)
    return str
  }
}

/**
 * Base64解码
 * @param str Base64编码的字符串
 * @returns 解码后的字符串
 */
export function base64Decode(str: string): string {
  try {
    return decodeURIComponent(atob(str).split('').map((c) => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))
  } catch (error) {
    console.error('Base64解码失败:', error)
    return str
  }
}

/**
 * 简单的异或加密
 * 注意：这只是一个示例实现，生产环境应使用更安全的加密算法（如AES）
 * 
 * @param data 要加密的数据（字符串或对象）
 * @param key 加密密钥
 * @returns 加密后的字符串
 */
export function simpleEncrypt(data: any, key: string = 'woodlin_secret_key'): string {
  try {
    const str = typeof data === 'string' ? data : JSON.stringify(data)
    let encrypted = ''
    
    for (let i = 0; i < str.length; i++) {
      const charCode = str.charCodeAt(i) ^ key.charCodeAt(i % key.length)
      encrypted += String.fromCharCode(charCode)
    }
    
    return base64Encode(encrypted)
  } catch (error) {
    console.error('加密失败:', error)
    return data
  }
}

/**
 * 简单的异或解密
 * 
 * @param encrypted 加密后的字符串
 * @param key 解密密钥
 * @returns 解密后的数据
 */
export function simpleDecrypt(encrypted: string, key: string = 'woodlin_secret_key'): any {
  try {
    const decoded = base64Decode(encrypted)
    let decrypted = ''
    
    for (let i = 0; i < decoded.length; i++) {
      const charCode = decoded.charCodeAt(i) ^ key.charCodeAt(i % key.length)
      decrypted += String.fromCharCode(charCode)
    }
    
    // 尝试解析为JSON，如果失败则返回字符串
    try {
      return JSON.parse(decrypted)
    } catch {
      return decrypted
    }
  } catch (error) {
    console.error('解密失败:', error)
    return encrypted
  }
}

/**
 * TODO: 集成真实的AES加密库
 * 推荐使用 crypto-js 库进行AES加密
 * 
 * 示例：
 * import CryptoJS from 'crypto-js'
 * 
 * export function aesEncrypt(data: any, key: string): string {
 *   const str = typeof data === 'string' ? data : JSON.stringify(data)
 *   return CryptoJS.AES.encrypt(str, key).toString()
 * }
 * 
 * export function aesDecrypt(encrypted: string, key: string): any {
 *   const decrypted = CryptoJS.AES.decrypt(encrypted, key).toString(CryptoJS.enc.Utf8)
 *   try {
 *     return JSON.parse(decrypted)
 *   } catch {
 *     return decrypted
 *   }
 * }
 */

/**
 * RSA公钥加密（模拟实现）
 * TODO: 实际项目中应使用真实的RSA加密库，如 jsencrypt
 * 
 * @param data 要加密的数据
 * @param _publicKey 公钥（未使用，占位参数）
 * @returns 加密后的字符串
 */
export function rsaEncrypt(data: any, _publicKey?: string): string {
  console.warn('TODO: 实现RSA加密，当前使用简单加密代替')
  return simpleEncrypt(data)
}

/**
 * RSA私钥解密（模拟实现）
 * TODO: 实际项目中应使用真实的RSA解密库
 * 
 * @param encrypted 加密后的字符串
 * @param _privateKey 私钥（未使用，占位参数）
 * @returns 解密后的数据
 */
export function rsaDecrypt(encrypted: string, _privateKey?: string): any {
  console.warn('TODO: 实现RSA解密，当前使用简单解密代替')
  return simpleDecrypt(encrypted)
}
