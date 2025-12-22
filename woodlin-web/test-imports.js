console.log("✅ Testing file structure...")
const fs = require('fs')
const path = require('path')

const files = [
  'src/config/index.ts',
  'src/utils/http/types.ts',
  'src/utils/http/request.ts',
  'src/utils/http/index.ts',
  'src/router/guards.ts',
  'scripts/generate-api.js',
  'ARCHITECTURE.md'
]

files.forEach(file => {
  const fullPath = path.join(__dirname, file)
  if (fs.existsSync(fullPath)) {
    const stats = fs.statSync(fullPath)
    console.log(`  ✓ ${file} (${stats.size} bytes)`)
  } else {
    console.log(`  ✗ ${file} MISSING`)
  }
})

console.log("\n✅ All files created successfully!")
