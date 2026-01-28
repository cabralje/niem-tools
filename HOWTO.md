# HOWTO: niem-tools BoUML Plugout

This is a quick HOWTO for getting started with using `BoUML` and `niemtools` to develop NIEM message specifications.  The steps include:

1. [Install BoUML and niem-tools](#1-install-bouml-and-niem-tools)
2. [Model messages in UML](#2-model-messages-in-uml)
3. [Map the UML models to NIEM](#3-map-the-uml-models-to-niem)
4. [Publish the NIEM model and schemas](#4-publish-the-niem-model-and-schemas)
5. [Model components and interfaces](#5-model-components-and-interfaces)
6. [Publish NIEM message specification](#6-publish-niem-message-specification)

---
## 1. Install BoUML, niem-tools and cmftool
### 1a.  Install BoUML
* Download and install the latest version of [BoUML](https://bouml.fr) for Windows, macOS or Linux.
### 1b. Install niem-tools

#### Option 1: Using npm (Recommended)
* Install niem-tools using npm (requires Node.js):
  ```bash
  npm install -g niem-tools
  ```
* Or install directly from GitHub:
  ```bash
  npm install -g cabralje/niem-tools
  ```
* The installation will automatically download and install all required dependencies including JDK 21, opencsv, commons-lang3, and JavaFX libraries.

#### Option 2: Manual Installation

If you don't have Node.js/npm installed:

1. **Install JDK 21** (if not already installed)
   - Download from [Adoptium](https://adoptium.net/), [Oracle](https://www.oracle.com/java/technologies/downloads/), or [Azul Zulu](https://www.azul.com/downloads/)

2. **Download niem-tools**
   - Download the latest release from the [Releases page](https://github.com/cabralje/niem-tools/releases)
   - Or clone this repository: `git clone https://github.com/cabralje/niem-tools.git`

3. **Build (if cloning from source)**
   ```bash
   mvn clean package
   npm install
   npm run build
   ```

4. **Note the installation directory** for the next step (template setup)

### 1c. Setup niem-tools template
* Locate the niem-tools installation directory:
  - **If installed via npm:**
    - On Windows: `%APPDATA%\npm\node_modules\niem-tools`
    - On macOS/Linux: Run `npm root -g` to find the global modules directory, then navigate to `niem-tools`
  - **If installed manually:** Use the directory where you cloned/extracted niem-tools
* Start BoUML.
* Click on `Miscellaneous->Set Environment`.
* Under `Template Project`, select the location of the `bouml-templates/niem-project/niem-project.prj` BoUML project template in the niem-tools installation directory.
* **For manual installations only:** Configure the BoUML plugout command:
  - In BoUML, go to the package that uses the plugout
  - Set the plugout command to: `java -jar /path/to/niem-tools/jdeploy-bundle/niemtools-2.0.jar`
  - (For npm installations, the `niem-tools` command is automatically available)
### 1d. Install cmftool (recommended)
* Download and extract [cmftool](https://github.com/niemopen/cmftool/tags)
* Add the `bin` directory to your system path.
## 2. Model messages in UML
### 2a. Create a NIEM Message Specification Project
* In BoUML, click on `Project -> Create from Template`.
* Select the location for your BoUML project.
### 2b. Import the NIEM reference model
* Download the expand the release of the [NIEM reference model](https://github.com/niemopen/niem-model/tags) to use.
* In Bouml, click on `Tools -> Import Reference Model`.
* There are several options to control the NIEM domains and codes to be included in the model:
  * To import only specific NIEM domains, enter a comma-separated list domains to import under `Include domains`.
  * To import all NIEM domains except certain domains, enter a comma-separated list of domains not to import under `Exclude domains`.
  * To import all NIEM codes except specific codelists, enter a comma-separated list of codes not to import under `Exclude codes`.
* Select `Import NIEM Reference Model` to import the the selected portion of the NIEM model. (It will take several minutes.)
* Save the BoUML project.
### 2c. Configure external schemas (if needed)
* In Bouml, select `Tools -> Define External Schemas`
* For each non-NIEM namespace, select `Add namespace` and enter the prefix, name and URL of the external namespace.
### 2d. Model the Message(s) and Data Content
* In BouML, model the data requirements for each message as follows
  * In the `Messages` package, right click and select `New class view` to create a folder for each message.
  * In the message folder, right click and select `New class diagram` to create a UML class diagram of the message.
  * In the class diagram, select `Add class` and name the class for each object in the message.
  * For each object property based on a simple type (e.g., string, number), right click on the class in the diagram and select `Add attribute` enter the name, type and the multiplicity of the property.
  * For each object property based on an another object, select `Directional Aggregation` in the diagram and draw a line from the object class to the property class. Click on the line and enter the name and multiplicity of the property.
  * To relate a child class to a parent class, select `Generalization` in the diagram and draw a line from the child object to the parent object.
* In BouML, select the `Messages` package, right click and select `Tools -> Add NIEM stereotype`.
* Save the BoUML project.
### 2e. Publish the UML model
* In the BoUML menu, select `Tools -> Map to NIEM`.
* Click `Browse` to select the project directory.
* Select `Publish UML model`.
* The UML model will be published to the `model` folder under the project directory.
  * The HTML documentation will be saved on `index-withframe.html`
  * The mapping of the UML model to NIEM will be saved in `niem-mapping.csv` (CSV format) and `niem-mapping.html` (HTML format).
## 3. Map the UML models to NIEM 
### 3a. Input the mappings for each message to NIEM
* Open `niem-mapping.csv` in a spreadsheet application (e.g. Excel).
* Map each UML class and attribute to NIEM using the [NIEM mapping format](#appendix-niem-mapping-file-format)
### 3b. Import the NIEM mappings
* Save `model/niem-mapping.csv`.
* In BoUML, run `Tools -> Import NIEM Mapping` to import mappings.
* Select the `model/niem-mapping.csv` file.
* Save the BoUML project. 
### 3c. Validate the NIEM mappings
* In BoUML, select `Tools -> Validate NIEM mapping`.
* The `niem-mapping.csv` and `niem-mapping.html` files will be regenerated.
  - Invalid mappings to NIEM types or multiplicities will be reported and shown in red in `model/niem-mapping.html`.
* As needed, repeat 3a-c to resolve any invalid mappings.
## 4. Publish the NIEM model and schemas
### 4a. Publish the NIEM model in Common Model Format (CMF)
* In BouML, select  `Tools -> Publish CMF`.
* Select the version of the CMF to generate.
### 4b. Publish the XSD and/or JSON schema representation(s) of the NIEM model
* There are several options for publishing representations of the NIEM model in XSD and/or JSON schema format, including
  * Upload the CMF to the [NIEM Toolbox](https://niemopen.github.io/niem-toolbox/) tool.
  * In BoUML, select `Tools -> Publish XSD` or `Tools -> Publish JSON`.
    * To use `cmftool`, select the `Use cmftool to generate XSDs (or JSON) from CMF` options.
    * Extension schemas will be published in the `schema` folder.
    * If `cmtool` is not used, the NIEM subset wantlist for import into the [NIEM Subset Schema Generator Tool](https://tools.niem.gov/niemtools/ssgt/index.iepd) will be in the `schema\wantlist.xml` file.
  * Use a XML/JSON editor (e.g. Oxygen, XMLSpy) to generate XML or JSON examples of each message from the schemas.
    * Validate the examples satisfy the data requirements modeled in step 2.
    * If necessary, repeat steps 3 and 4, until the examples are acceptable.
## 5. Model components and interfaces
### 5a. Create Components
* In BoUML, select the `Components` package and right click and select `New component diagram` for each component.
* In the diagram, select `New component` for each component.
### 5b. Create Interfaces
* In BoUML, select the `Interfaces and Operations` package and right click and select `New class` for each interface and enter the name the interface.  
* Select each interface and change the stereotype to `interface`.
## 6. Publish NIEM Message Specification
### 6a. Publish the Web Services Description Language (WSDL) for each message
* In BoUML, select `Tools -> Publish NIEM Message Specification`.
### 6b. Publish the OpenAPI for each message
### 6c. Publish the XML catalog for the message specification

# Appendix: NIEM Mapping File Format
| Column | Description | Example 1 | Example 2 |
|--------|-------------|-----------|-----------|
| **Model Class** | Class in the UML model(read-only) | `Case` |
| **Model Attribute** | Class attribute in the UML model (read-only) | `CaseNumber` |
| **Model Type** | Class/attribute type in the UML model (read-only) | `string` |
| **Model Multiplicity** | Cardinality/multiplicity of the attribute in the UML model (read-only) | `0..1` |
| **Model Definition** | Description of the class/attribute in the UML model (read-only) | `A court case` |
| **NIEM XPath** | Full path from root to the specific element | `nc:Case/j:CaseAugmentation/j:CaseDefendantParty/nc:EntityPerson/nc:PersonOtherIdentification/ecf:PersonIdentificationCategoryType` | `ecf:CourtEventAugmentation/ecf:ConnectedDocument` |
| **NIEM Type** | NIEM or extension type that contains the element | `nc:IdentificationType` | `ecf:CourtEventAugmentationType` |
| **NIEM Property** | Element(s), comma-separated; use `^` for references, parentheses for representations | `nc:IdentificationCategory`, `^ecf:ConnectedDocument`, `(ecf:PersonIdentificationCategoryCode)` |
| **NIEM Base Type** | Base type of the NIEM Property | `xs:normalizedString` |
| **NIEM Multiplicity** | Min and max occurrences | `0,1` |
| **Old XPath** | Full path from a mapping to a previous reference model (e.g. GJXDM) | `ecf:CourtEventAugmentation/ecf:ConnectedDocument` |
| **Old Multiplicity** | Min and max occurrences from a mapping to a previous reference model | `0,1` |
| **NIEM Mapping Notes** | Optional mapping notes; maybe the name of a Genericode code list | CourtLocationCode.gc|
| **Code List** | Extension code list values and definitions in the format "code1=definition1; code2=definition2" | `civil=Civil Case Type; criminal=Criminal Case Type; domestic=Domestic Case Type` |

**Mapping Tips:**

* DO NOT MODIFY the **Model Class/Attribute/Type/Multiplicity/Defintion** columns in the mapping file - these are generated automatically from the UML.
* NIEM namespace mappings (e.g., `nc:`) are reflected in the wantlist and subset.
* Other prefixes will generate a schema extension file named `<prefix>.xsd`.
* Optionally compare mappings to previous specifications using the **Old XPath** and **Old Multiplicity** columns.