# jDeploy Distribution Guide

This project uses [jDeploy](https://www.jdeploy.com/) for automated distribution and installation. jDeploy simplifies the installation process by automatically handling Java dependencies and creating native installers.

## For Users

### Installation

Install niem-tools globally using npm:

```bash
npm install -g niem-tools
```

Or install directly from GitHub:

```bash
npm install -g cabralje/niem-tools
```

This will:
- Automatically download the required JDK 21 (if not already installed)
- Install all dependencies (opencsv, commons-lang3, JavaFX)
- Install cmftool (NIEM Common Model Format tools)
- Create a globally available `niem-tools` command

### Running

After installation, you can run niem-tools from anywhere:

```bash
niem-tools
```

### Installed Components

#### BoUML Templates

The BoUML templates are installed in the user's home directory:
- On Windows: `%USERPROFILE%\niem-tools\bouml-templates`
- On macOS/Linux: `~/niem-tools/bouml-templates`

The original npm package directory still contains the templates, but the home directory copy is the recommended location.

#### cmftool

The cmftool suite (Common Model Format tools) is installed in the user's home directory:
- On Windows: `%USERPROFILE%\niem-tools\cmftool`
- On macOS/Linux: `~/niem-tools/cmftool`

cmftool includes command-line utilities for working with NIEM:
- `cmftool` - Main CMF tool for model transformations
- `niemtran` - NIEM transformation utilities
- `scheval` - Schema evaluation tools

To use cmftool commands, either:
- Add the bin directory to your PATH: `~/niem-tools/cmftool/bin` (or `%USERPROFILE%\niem-tools\cmftool\bin` on Windows)
- Use the full path to the executables

## For Developers

### Prerequisites

- Node.js and npm
- Maven
- JDK 21

### Building for jDeploy

1. Build the Maven project:
   ```bash
   mvn clean package
   ```

2. Generate the jDeploy bundle:
   ```bash
   npm install
   npm run build
   ```

   This runs `jdeploy install` and automatically fixes a jDeploy bug (see Known Issues below).

3. Test locally:
   ```bash
   niem-tools
   ```

### Publishing

To publish a new version to npm:

1. Update the version in `package.json`
2. Build the project: `mvn clean package && npm run build`
3. Commit all changes including the updated `jdeploy-bundle/jdeploy.js`
4. Publish to npm: `npm publish`

### Configuration

The jDeploy configuration in `package.json`:

- `jdk`: 21 - Specifies Java 21 is required
- `javafx`: true - Enables JavaFX support
- `jar`: "target/niemtools-2.0.jar" - Path to the main JAR
- `mainClass`: "org.cabral.niemtools.BoumlPlugout" - Main class to run

The BoUML templates are packaged via the top-level `files` array in `package.json` (so they are present in the npm package), but are not duplicated inside the jDeploy bundle.

#### pom.xml Configuration

The `maven-jar-plugin` must be configured to add the Main-Class manifest attribute:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>org.cabral.niemtools.BoumlPlugout</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

Without this, the JAR will fail with "no main manifest attribute" error.

### Dependencies

#### Runtime Dependencies (package.json)

jDeploy requires these Node.js dependencies for JDK downloading and extraction:
- `node-fetch`: ^2.7.0 - HTTP client for downloading JDK
- `yauzl`: ^2.10.0 - ZIP extraction (Windows)
- `tar`: ^7.4.3 - TAR extraction (macOS/Linux)

#### Java Dependencies (pom.xml)

Dependencies are managed through Maven's standard dependency resolution:
- opencsv 5.11
- commons-lang3 3.18.0
- JavaFX 21.0.1 (controls and fxml)

All Java dependencies are included in the JAR's classpath and downloaded automatically by jDeploy during installation.

### Known Issues

#### jDeploy Java Version Bug

jDeploy 5.5.15 has a bug where it ignores the `"jdk": 21` setting in package.json and generates `jdeploy.js` with Java 11. 

**Workaround**: The `npm run build` script automatically runs `scripts/fix-jdeploy-java-version.js` after `jdeploy install` to fix the generated file. This ensures the correct Java version is used.

## Migration from Manual Distribution

Previously, users had to:
1. Download and extract a ZIP/TAR archive
2. Manually add JARs to the CLASSPATH
3. Configure the environment

With jDeploy, this is now automated through npm package installation, making it much easier for users to install and use niem-tools.
