# HOWTO: niem-tools BOUML Plugout
---

## 1. Setup Environment

- Run 'mvn package' or download a copy of `opencsv-5.10.jar` from [Sourceforge](https://sourceforge.net/projects/opencsv/files/opencsv/).
- Add `niemtools-bouml.jar` and `opencsv-5.10.jar` to the system environment property `CLASSPATH`.

---

## 2. Create a NIEM Message Specification Project

### a.
- Copy the `BOUML-niem` template **OR**

### b.
- Create a blank BOUML project.
- Right-click on the root package.
- Select **"Import project"** and choose the **"niem-profile"** BOUML project.

---

## 3. Configure the Project

- Right click on the root package
- Select the properties tab
- Edit the options to match your environment.

---

## 4. Model the Exchange Content (Data)

### a.
- Create a UML class for each data object.
- Set the stereotype to `"niem"`.

### b.
- Create UML attributes for each property of each class.
- Set the stereotype to `"niem"`.

### c.
- Create a UML class instance for each message.
- Set the stereotype to `"niem"`.

### d.
- Save the BOUML project.

### e.
- Run `Tools -> Generate NIEM IEPD` to generate:
  - HTML documentation
  - Blank NIEM mapping `.csv` and `.html` files

### f.
- Open `tniem-mapping.csv` in Excel.
- Map each UML class and attribute to NIEM using the following columns:

| Column | Description | Example 1 | Example 2 |
|--------|-------------|-----------|-----------|
| **NIEM XPath** | Full path from root to the specific element | `nc:Case/j:CaseAugmentation/j:CaseDefendantParty/nc:EntityPerson/nc:PersonOtherIdentification/ecf:PersonIdentificationCategoryType` | `ecf:CourtEventAugmentation/ecf:ConnectedDocument` |
| **NIEM Type** | NIEM or extension type that contains the element | `nc:IdentificationType` | `ecf:CourtEventAugmentationType` |
| **NIEM Property** | Element(s), comma-separated; use `@` for references, parentheses for representations | `nc:IdentificationCategory`, `@ecf:ConnectedDocument`, `(ecf:PersonIdentificationCategoryCode)` |
| **NIEM Base Type** | Base type of the NIEM Property | `xs:normalizedString` |
| **NIEM Multiplicity** | Min and max occurrences | `0,1` |
| **NIEM Mapping Notes** | Optional mapping notes, including code list info | Genericode code list |

**Note:** Elements are included in the NIEM extension schemas as comments.

**Code List Example:**
- A code list such as `PersonIdentificationCategoryCode.gc` will be created.
- Sample values:  
  `DefendantNumber=Defendant Identifier`,  
  `LocalAgencyID=Prosecutor Identifier`,  
  `PersonID=Generic Person Identifier`,  
  `PrisonerID=Jail Identifier`

**Mapping Tips:**
- Optionally compare mappings to previous specifications using "Old XPath" and "Old Multiplicity" columns.
- **Do not modify** UML model columns.
- NIEM namespace mappings (e.g., `nc:`) are reflected in the wantlist and subset.
- Other prefixes will generate a schema extension file named `<prefix>.xsd`.

### g.
- Save `niem-mapping.csv`.

### h.
- In BOUML, run `Tools -> Import NIEM Mapping` to import mappings.

### i.
- Save the BOUML project.

### j.
- If UML packages `NIEMSubset` and `NIEMExtension` exist, delete them.

### k.
- Run `Tools -> Generate NIEM IEPD` again to regenerate:
  - Mapping CSV and HTML
  - HTML documentation
  - NIEM wantlist
  - Extension schemas

### i.
- Invalid types or multiplicities will be reported and shown in red in `niem-mapping.html`.

### ii.
- Differences between current and old mappings will appear in blue.

### l.
- Repeat steps **e–k** until all mappings are valid.

---

## 5. Model Components and Interfaces

### a.
- Create a UML class for each component.
- Set the stereotype to `"interface"`.

### b.
- Create a UML operation under each class for each operation.

### c.
- Run `Tools -> Generate NIEM IEPD` again to regenerate all files including WSDLs.

---

## 6. Generate the NIEM Subset

### a.
- Open the [NIEM Subset Schema Generator Tool](https://tools.niem.gov/niemtools/ssgt/index.iepd)

### b.
- Select **"Options" → "Browse"**, choose `wantlist.xml`, and **"Load Wantlist"**

### c.
- Click **"Generate"**, then **"Save Subset Schema to a file"**, and save the ZIP to the `XSD` folder.

### d.
- Extract the ZIP contents into a folder named `niem`.

---

## 7. Generate XML Instances for Each Message

### a.
- Use an XML editor (e.g., Altova XMLSpy) to create and validate instances based on each schema.

### b.
- If needed, adjust mappings by repeating steps **2.e–k** until all mappings are complete and valid.
