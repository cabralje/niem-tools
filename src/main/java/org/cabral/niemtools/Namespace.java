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
