package com.mtgmc.niemtools;

import fr.bouml.UmlClassView;

class Namespace {

	String getSchemaURI() {
		return schemaURI;
	}
	void setSchemaURI(String schemaURI) {
		this.schemaURI = schemaURI;
	}
	UmlClassView getNsClassView() {
		return nsClassView;
	}
	void setNsClassView(UmlClassView nsClassView) {
		this.nsClassView = nsClassView;
	}
	UmlClassView getReferenceClassView() {
		return referenceClassView;
	}
	void setReferenceClassView(UmlClassView referenceClassView) {
		this.referenceClassView = referenceClassView;
	}
	String getFilepath() {
		return filepath;
	}
	void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	private String schemaURI = null;
	private UmlClassView nsClassView = null;
	private UmlClassView referenceClassView = null;
	private String filepath = null;
	Namespace(String schemaURI2)
	{
		schemaURI = schemaURI2;
	}
}
