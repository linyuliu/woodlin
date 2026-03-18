const isDev = import.meta.env.DEV

type LogArgs = unknown[]
type DevLogLevel = 'LOG' | 'INFO' | 'WARN' | 'DEBUG'

function writeDevLog(level: DevLogLevel, ...args: LogArgs): void {
  if (!isDev) {
    return
  }

  console.warn(`[${level}]`, ...args)
}

function createDevLogger(level: DevLogLevel) {
  return (...args: LogArgs): void => {
    writeDevLog(level, ...args)
  }
}

export const logger = Object.freeze({
  log: createDevLogger('LOG'),
  info: createDevLogger('INFO'),
  warn: createDevLogger('WARN'),
  error(...args: LogArgs): void {
    console.error('[ERROR]', ...args)
  },
  debug: createDevLogger('DEBUG')
})

export default logger
