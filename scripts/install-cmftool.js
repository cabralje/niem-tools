const fs = require('fs');
const os = require('os');
const path = require('path');
const https = require('https');
const { pipeline } = require('stream');
const { promisify } = require('util');
const yauzl = require('yauzl');

const streamPipeline = promisify(pipeline);

const CMFTOOL_VERSION = '1.0';
const CMFTOOL_RELEASE_URL = `https://github.com/niemopen/cmftool/releases/download/v${CMFTOOL_VERSION}/cmftool-allApps-${CMFTOOL_VERSION}.zip`;

async function downloadFile(url, destPath) {
  return new Promise((resolve, reject) => {
    https.get(url, { 
      headers: { 'User-Agent': 'niem-tools' },
      followRedirect: true 
    }, (response) => {
      // Handle redirects
      if (response.statusCode === 301 || response.statusCode === 302) {
        downloadFile(response.headers.location, destPath)
          .then(resolve)
          .catch(reject);
        return;
      }

      if (response.statusCode !== 200) {
        reject(new Error(`Failed to download: ${response.statusCode} ${response.statusMessage}`));
        return;
      }

      const fileStream = fs.createWriteStream(destPath);
      streamPipeline(response, fileStream)
        .then(resolve)
        .catch(reject);
    }).on('error', reject);
  });
}

async function extractZip(zipPath, destDir) {
  return new Promise((resolve, reject) => {
    yauzl.open(zipPath, { lazyEntries: true }, (err, zipfile) => {
      if (err) {
        reject(err);
        return;
      }

      zipfile.readEntry();
      zipfile.on('entry', (entry) => {
        const entryPath = path.join(destDir, entry.fileName);

        if (/\/$/.test(entry.fileName)) {
          // Directory entry
          fs.mkdirSync(entryPath, { recursive: true });
          zipfile.readEntry();
        } else {
          // File entry
          fs.mkdirSync(path.dirname(entryPath), { recursive: true });
          
          zipfile.openReadStream(entry, (err, readStream) => {
            if (err) {
              reject(err);
              return;
            }

            const writeStream = fs.createWriteStream(entryPath);
            readStream.pipe(writeStream);
            
            writeStream.on('finish', () => {
              // Set executable permissions on Unix-like systems
              if (process.platform !== 'win32' && entry.fileName.endsWith('.sh')) {
                fs.chmodSync(entryPath, 0o755);
              }
              zipfile.readEntry();
            });
            
            writeStream.on('error', reject);
          });
        }
      });

      zipfile.on('end', () => {
        resolve();
      });

      zipfile.on('error', reject);
    });
  });
}

async function main() {
  const targetRoot = path.join(os.homedir(), 'niem-tools');
  const cmftoolDir = path.join(targetRoot, 'cmftool');
  const zipPath = path.join(targetRoot, `cmftool-${CMFTOOL_VERSION}.zip`);

  try {
    // Create target directory
    fs.mkdirSync(targetRoot, { recursive: true });

    // Check if cmftool is already installed
    if (fs.existsSync(cmftoolDir)) {
      console.log(`cmftool already installed at ${cmftoolDir}`);
      return;
    }

    console.log(`Downloading cmftool ${CMFTOOL_VERSION}...`);
    await downloadFile(CMFTOOL_RELEASE_URL, zipPath);
    console.log('Download complete.');

    console.log('Extracting cmftool...');
    await extractZip(zipPath, cmftoolDir);
    console.log(`cmftool installed to ${cmftoolDir}`);

    // Clean up zip file
    fs.unlinkSync(zipPath);

    // Display installed tools
    console.log('\nInstalled cmftool executables:');
    const binDir = path.join(cmftoolDir, 'bin');
    if (fs.existsSync(binDir)) {
      const files = fs.readdirSync(binDir);
      files.forEach(file => {
        if (file.endsWith('.bat') || file.endsWith('.sh')) {
          console.log(`  - ${file}`);
        }
      });
    }

    console.log(`\nTo use cmftool, add ${binDir} to your PATH or use the full path.`);

  } catch (error) {
    console.error('Error installing cmftool:', error.message);
    // Clean up on error
    if (fs.existsSync(zipPath)) {
      fs.unlinkSync(zipPath);
    }
    throw error;
  }
}

main().catch(error => {
  console.error('Installation failed:', error);
  process.exit(1);
});
