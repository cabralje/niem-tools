const fs = require('fs');
const os = require('os');
const path = require('path');

function copyDir(src, dest) {
  if (!fs.existsSync(src)) {
    return;
  }
  fs.mkdirSync(dest, { recursive: true });
  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);
    if (entry.isDirectory()) {
      copyDir(srcPath, destPath);
    } else if (entry.isFile()) {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

function main() {
  const packageRoot = path.resolve(__dirname, '..');
  const sourceDir = path.join(packageRoot, 'bouml-templates');
  const targetRoot = path.join(os.homedir(), 'niem-tools');
  const targetDir = path.join(targetRoot, 'bouml-templates');

  if (!fs.existsSync(sourceDir)) {
    return;
  }

  copyDir(sourceDir, targetDir);
  console.log(`BoUML templates installed to ${targetDir}`);
}

main();
