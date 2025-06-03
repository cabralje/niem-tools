/*
 *   NIEMtools - This is a plug_out that extends the BOUML UML tool with support for the National Information Exchange Model (NIEM) defined at http://niem.gov.
 *   Specifically, it enables a UML Common Information Model (CIM), an abstract class mode, to be mapped into a
 *   UML Platform Specific Model (PSM), the NIEM reference/subset/extension model, and a UML Platform Specific Model (NIEM), NIEM XML Schema.
 *
 *   NOTE: This plug_out requires that the BOUML project include a simple NIEM profile that provides the stereotypes required for mapping.
 *   
 *   Copyright (C) 2025 James E. Cabral Jr., jim@cabral.org, http://github.com/cabralje
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Represents a namespace associated with a schema URI, UML class views, and a file path.
 * <p>
 * This class encapsulates information about a namespace, including its schema URI,
 * associated UML class views for the namespace and references, and the file path
 * where the namespace is defined or stored.
 * </p>
 *
 * <p>
 * Typical usage:
 * <pre>
 *     Namespace ns = new Namespace("http://example.com/schema");
 *     ns.setNsClassView(someUmlClassView);
 *     ns.setReferenceClassView(someReferenceClassView);
 *     ns.setFilepath("/path/to/schema.xsd");
 * </pre>
 * </p>
 *
 * @author James Cabral
 * @version 1.0
 */
package org.cabral.niemtools;

import fr.bouml.UmlClassView;

class Namespace {

    private String schemaURI = null;
    private UmlClassView nsClassView = null;
    private UmlClassView referenceClassView = null;
    private String filepath = null;

    /**
     * @param schemaURI2
     */
    /**
     * Constructs a new {@code Namespace} instance with the specified schema URI.
     *
     * @param schemaURI2 the URI of the schema to associate with this namespace
     */
    Namespace(String schemaURI2) {
        schemaURI = schemaURI2;
    }

    /**
     * @return schema URI as a String
     */
    String getSchemaURI() {
        return schemaURI;
    }

    /**
     * @param schemaURI
     */
    /**
     * @return namespace class view as a UmlClassView
     */
    UmlClassView getNsClassView() {
        return nsClassView;
    }

    /**
     * @param nsClassView
     */
    void setNsClassView(UmlClassView nsClassView) {
        this.nsClassView = nsClassView;
    }

    /**
     * @return
     */
    UmlClassView getReferenceClassView() {
        return referenceClassView;
    }

    /**
     * @param referenceClassView
     */
    void setReferenceClassView(UmlClassView referenceClassView) {
        this.referenceClassView = referenceClassView;
    }

    /**
     * @return file path as a String
     */
    String getFilepath() {
        return filepath;
    }

    /**
     * @param filepath
     */
    void setFilepath(String filepath) {
        this.filepath = filepath;
    }

}
