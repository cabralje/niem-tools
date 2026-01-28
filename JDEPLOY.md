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
- Create a globally available `niem-tools` command

### Running

After installation, you can run niem-tools from anywhere:

```bash
niem-tools
```

### Template Location

The BoUML templates are installed in the niem-tools package directory:
- On Windows: `%APPDATA%\npm\node_modules\niem-tools\bouml-templates`
- On macOS/Linux: Find using `npm root -g` then navigate to `niem-tools/bouml-templates`

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
   npx jdeploy install
   ```

3. Test locally:
   ```bash
   npx jdeploy run
   ```

### Publishing

To publish a new version to npm:

1. Update the version in `package.json`
2. Build the project: `mvn clean package`
3. Publish to npm: `npm publish`

### Configuration

The jDeploy configuration in `package.json`:

- `jdk`: 21 - Specifies Java 21 is required
- `javafx`: true - Enables JavaFX support
- `jar`: "target/niemtools-2.0.jar" - Path to the main JAR
- `mainClass`: "org.cabral.niemtools.BoumlPlugout" - Main class to run
- `files`: Includes the `bouml-templates` directory in the distribution

### Dependencies

Dependencies are now managed automatically through Maven's standard dependency resolution:
- opencsv 5.11
- commons-lang3 3.18.0
- JavaFX 21.0.1 (controls and fxml)

All dependencies are included in the JAR's classpath and downloaded automatically by jDeploy during installation.

## Migration from Manual Distribution

Previously, users had to:
1. Download and extract a ZIP/TAR archive
2. Manually add JARs to the CLASSPATH
3. Configure the environment

With jDeploy, this is now automated through npm package installation, making it much easier for users to install and use niem-tools.
