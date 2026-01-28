#!/usr/bin/env node
// Fix jdeploy bug: it ignores jdk:21 in package.json and generates with Java 11
// This script fixes the generated jdeploy.js to use Java 21

const fs = require('fs');
const path = require('path');

const jdeployPath = path.join(__dirname, '..', 'jdeploy-bundle', 'jdeploy.js');
let content = fs.readFileSync(jdeployPath, 'utf8');

// Replace Java 11 with Java 21
content = content.replace(
  /var javaVersionString = "11";/,
  'var javaVersionString = "21";'
);

fs.writeFileSync(jdeployPath, content, 'utf8');
console.log('Fixed jdeploy.js to use Java 21');
