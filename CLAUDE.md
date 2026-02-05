# CLAUDE.md - AI Assistant Guide for niem-tools

## Project Overview

**niem-tools** is a BOUML (UML modeling tool) plugout that enables developers to create NIEM (National Information Exchange Model) message specifications using UML. It maps UML models to NIEM standards and generates XML schemas (XSD), JSON schemas, HTML documentation, WSDL/OpenAPI specs, and Common Model Format (CMF) files.

- **Author:** Jim Cabral (jim@cabral.org)
- **License:** GNU General Public License v3
- **Repository:** https://github.com/cabralje/niem-tools
- **Current Version:** 2.0 (Maven) / 2.0.6 (npm)

## Which AI Reads Which File

This repo may be used with multiple AI coding tools. Instruction-file behavior varies by tool:

- **GitHub Copilot in VS Code:** Reads `CLAUDE.md` and may also read `.github/copilot-instructions.md`.
- **OpenAI Codex (ChatGPT agent / Codex CLI):** Guided by `AGENTS.md`. Prefer keeping `CLAUDE.md` as the source of truth and having `AGENTS.md` point here plus a few hard rules.

## Build System

### Prerequisites

- **Java 21** (JDK) - required for compilation and runtime
- **Maven 3+** - primary build tool
- **Node.js / npm** - for jDeploy distribution and post-install scripts

### Build Commands

```bash
# Compile and run tests
mvn compile
mvn test

# Full build (compile + test + package JAR + npm/jDeploy bundle)
mvn package

# npm-based build (invoked by Maven during package phase)
npm run build

# Install globally via npm (downloads JDK 21 automatically)
npm install -g niem-tools
```

### Build Output

- JAR: `target/niemtools-2.0.jar`
- Runtime dependencies copied to: `target/lib/`
- JavaFX libraries copied to: `target/javafx-lib/`
- jDeploy bundle: `jdeploy-bundle/`

### Cross-Platform JavaFX Profiles

Maven auto-selects the JavaFX platform classifier via OS-based profiles:
- `windows` profile -> `javafx.platform=win`
- `mac` profile -> `javafx.platform=mac`
- `linux` profile -> `javafx.platform=linux`

## Testing

### Framework

- **JUnit 4** (4.13.1) - unit testing
- **Mockito** (4.11.0) - mocking
- **TestFX** (4.0.18) - JavaFX UI testing

### Running Tests

```bash
mvn test
```

### Test Location

All tests are in `src/test/java/org/cabral/niemtools/`:

| Test Class | What It Tests |
|---|---|
| `BoumlPlugoutTest` | Entry point / main method |
| `NiemUmlModelTest` | Core NIEM-UML mapping model |
| `ProjectPropertiesTest` | Configuration properties |
| `CsvReaderTest` | CSV import |
| `CsvWriterTest` | CSV export |
| `HtmlWriterTest` | HTML export |
| `CmfWriterTest` | CMF export |
| `NamespaceTest` | Namespace handling |
| `NamespaceModelTest` | Namespace model operations |
| `NamespaceResolverTest` | Namespace URI resolution |
| `ConfigurationDialogTest` | UI dialog |
| `LogTest` | Logging utility |

### Test Conventions

- Tests use JUnit 4 annotations (`@Test`), not JUnit 5
- Assertions use `org.junit.Assert` static imports (e.g., `assertNotNull`, `fail`)
- Many tests guard against unexpected exceptions via try/catch with `fail()`
- Test resources directory: `src/test/resources/` (gitignored)
- Python integration tests exist at `src/test/python/test_bouml.py`

## Project Structure

```
niem-tools/
├── pom.xml                          # Maven build configuration
├── package.json                     # npm/jDeploy configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── fr/bouml/            # BOUML UML API wrapper classes (~280 classes)
│   │   │   └── org/cabral/niemtools/ # Core application code (~20 classes)
│   │   └── resources/
│   │       └── org/cabral/niemtools/
│   │           ├── App.fxml          # Main JavaFX UI layout
│   │           ├── AboutDialog.fxml  # About dialog layout
│   │           └── PreferencesDialog.fxml
│   └── test/
│       ├── java/org/cabral/niemtools/ # JUnit test classes
│       └── python/                    # Python integration tests
├── bouml-templates/                 # BOUML project templates
│   ├── niem-profile/                # NIEM UML stereotype profile
│   ├── niem-project/                # Base NIEM project template
│   └── niem6-project/               # NIEM 6 pre-configured template
├── scripts/                         # Node.js build/install scripts
│   ├── install-bouml-templates.js   # Copies templates to ~/niem-tools/
│   ├── install-cmftool.js           # Downloads CMFTool
│   └── fix-jdeploy-java-version.js  # Ensures JDK 21 in jDeploy config
├── jdeploy-bundle/                  # jDeploy distribution files
├── README.md                        # Project overview
├── HOWTO.md                         # User guide
├── JDEPLOY.md                       # Distribution guide
└── LICENSE                          # GNU GPL v3
```

## Key Packages

### `org.cabral.niemtools` (core application)

This is the package where all development happens. Key classes:

| Class | Role |
|---|---|
| `BoumlPlugout` | **Main entry point.** Parses CLI args, connects to BOUML via socket, launches JavaFX UI. |
| `NiemUmlModel` | **Core orchestrator.** Handles import/export of NIEM models, stereotype management, schema generation. |
| `NiemModel` | NIEM element/type management, XML parsing, caching. |
| `AppController` | JavaFX main window controller (FXML-bound). |
| `AboutDialogController` | About dialog controller. |
| `PreferencesDialogController` | Preferences dialog controller. |
| `ProjectProperties` | Configuration management. Extends `java.util.Properties`, syncs with BOUML project metadata. |
| `CmfWriter` | Exports NIEM Common Model Format (CMF) XML. |
| `XmlWriter` | Exports WSDL, XML catalogs, Genericode code lists. |
| `HtmlWriter` | Exports mapping spreadsheet as HTML. |
| `JsonWriter` | Exports JSON schemas. |
| `CsvWriter` | Exports mapping as CSV. |
| `CsvReader` | Imports CSV mapping spreadsheets. |
| `CmfToolAdapter` | Wraps external CMFTool CLI for XSD/JSON/WSDL generation. |
| `NamespaceModel` | Collection of namespaces and namespace-to-element mapping. |
| `Namespace` | Represents a schema namespace (URI, class views, filepath). |
| `NamespaceResolver` | Resolves namespace URIs from the NIEM model. |
| `JavaFxLauncher` | Lazy-loads JavaFX via reflection to avoid hard dependency. |
| `Log` | Thread-safe logging utility with JavaFX UI integration. |

### `fr.bouml` (BOUML API wrappers)

~280 auto-generated Java wrapper classes for the BOUML UML tool's C++ API. These provide the UML metamodel (classes, attributes, relations, diagrams, etc.). **Do not manually edit these files** -- they are generated by BOUML.

Key base classes: `UmlItem`, `UmlPackage`, `UmlClass`, `UmlAttribute`, `UmlOperation`, `UmlRelation`, `UmlCom`.

## Architecture

### Pattern: Model-View-Controller (MVC)

- **Model:** `NiemUmlModel`, `NiemModel`, `NamespaceModel`, `ProjectProperties`
- **View:** FXML files (`App.fxml`, `AboutDialog.fxml`, `PreferencesDialog.fxml`)
- **Controller:** `AppController`, `AboutDialogController`, `PreferencesDialogController`

### BOUML Integration

The application communicates with BOUML over a socket connection. The port number is passed as a CLI argument or read from a temp file (`java.io.tmpdir/boumlport.txt`). The `UmlCom` class manages this connection. All UML model reads/writes go through the `fr.bouml` API which proxies to the running BOUML instance.

### Threading Model

- **JavaFX Application Thread:** All UI updates must happen on this thread
- **Background tasks:** Long operations (imports, exports) run via `javafx.concurrent.Task<T>`
- **Log class:** Uses `Platform.runLater()` via reflection for thread-safe UI logging
- **`ProjectProperties.setProperty()`** is `synchronized`

### JavaFX Loading

JavaFX is loaded lazily via reflection (`JavaFxLauncher`, `BoumlPlugout.isJavaFxAvailable()`) to allow the application to run in environments without JavaFX available.

## Code Conventions

### Java Style

- **Java 21** language features are used (switch expressions with `->`, pattern matching, etc.)
- Package-private (default) visibility is used for most fields and methods within `org.cabral.niemtools`
- GPL v3 license header appears at the top of core source files (in `org.cabral.niemtools`)
- Javadoc is used for public/protected methods and class-level documentation
- The `fr.bouml` classes do not follow the same conventions (auto-generated code)

### Property Keys

Configuration properties use `PascalCase` naming (e.g., `ImportNIEMVersion`, `ExportProjectDir`, `IEPDName`). All property key constants are defined as `static final String` in `ProjectProperties.java`.

### Logging

Use `Log.trace()` for general messages, `Log.debug()` for debug-only output, and `Log.start()`/`Log.stop()` for profiling blocks. The `Log` class handles JavaFX thread dispatch internally via reflection.

### FXML UI

- FXML field names match the property key names (e.g., `@FXML private TextField ExportProjectDir`)
- UI fields use `PascalCase` (matching the FXML fx:id values), not the typical Java `camelCase` for fields
- Focus-loss listeners on UI controls auto-save property values

## Dependencies

### Runtime (from pom.xml)

| Dependency | Version | Purpose |
|---|---|---|
| `commons-lang3` | 3.18.0 | String/object utilities |
| `opencsv` | 5.11 | CSV parsing and writing |
| `javafx-controls` | 21.0.1 | JavaFX UI controls |
| `javafx-fxml` | 21.0.1 | FXML UI markup support |

### Test

| Dependency | Version | Purpose |
|---|---|---|
| `junit` | 4.13.1 | Unit testing framework |
| `mockito-core` | 4.11.0 | Mocking framework |
| `testfx-core` | 4.0.18 | JavaFX UI testing |

### npm (from package.json)

| Dependency | Purpose |
|---|---|
| `node-fetch` | HTTP requests in install scripts |
| `shelljs` | Shell commands in scripts |
| `tar` | Archive extraction |
| `yauzl` | ZIP extraction |
| `jdeploy` (dev) | Cross-platform distribution |

## Distribution

The application is distributed via **npm + jDeploy**:

1. `npm install -g niem-tools` installs globally
2. jDeploy downloads JDK 21 automatically if needed
3. Post-install scripts copy BOUML templates to `~/niem-tools/bouml-templates/` and install CMFTool
4. The `niem-tools` CLI command launches the JAR via jDeploy

## Key Workflows

### Import NIEM Reference Model

1. User selects NIEM version in the UI (`AppController`)
2. Reference model schemas are downloaded from GitHub (NIEM releases)
3. `NiemUmlModel.importSchemaDir()` parses XSD files
4. UML classes, attributes, and stereotypes are created in BOUML

### Export Specifications

1. User configures export options in the UI (format flags, paths, metadata)
2. `NiemUmlModel` walks the UML model tree collecting NIEM mappings
3. Writers generate output:
   - `CmfWriter` -> CMF XML
   - `CmfToolAdapter` -> invokes external `cmftool` for XSD/JSON/WSDL
   - `XmlWriter` -> Genericode, catalogs
   - `HtmlWriter` -> documentation
   - `CsvWriter` -> mapping spreadsheets

### Mapping Spreadsheet Round-Trip

1. Export via `CsvWriter` -> CSV mapping file
2. User edits mappings in a spreadsheet application
3. Import via `CsvReader` -> updates UML model

## Important Notes for AI Assistants

1. **Do not edit `fr/bouml/` classes** -- they are auto-generated by BOUML and will be overwritten.
2. **All application logic** lives in `src/main/java/org/cabral/niemtools/`.
3. **JavaFX is optional** -- the code uses reflection to check availability before loading UI classes.
4. **Properties sync with BOUML** -- `ProjectProperties.setProperty()` writes to both the Java Properties object and the BOUML project. The `project` field can be `null` in tests.
5. **NIEM is a government standard** -- naming follows NIEM conventions (e.g., IEPD, CMF, NDR). Do not rename these domain-specific terms.
6. **JUnit 4, not 5** -- tests use `@org.junit.Test`, not `@org.junit.jupiter.api.Test`.
7. **The `npm test` script is a stub** (`echo "Error: no test specified"`) -- use `mvn test` for actual tests.
8. **Cross-platform paths** -- default property values use Windows-style backslashes (`\\`). Path handling must work cross-platform.
