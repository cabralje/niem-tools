package org.cabral.niemtools;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

import fr.bouml.UmlAttribute;
import fr.bouml.UmlClass;
import fr.bouml.UmlClassInstance;
import fr.bouml.UmlItem;

public class CsvWriterTest {

    /*
    @Test
    public void testGetItemCsvForClass() {
        CsvWriter writer = new CsvWriter();
        UmlItem item = Mockito.mock(UmlItem.class);
        Mockito.when(item.kind()).thenReturn(fr.bouml.anItemKind.aClass);
        Mockito.when(item.name()).thenReturn("TestClass");
        Mockito.when(item.parent()).thenReturn(null);
        Mockito.when(item.description()).thenReturn("desc");
        Mockito.when(NiemUmlModel.isNiemUml(item)).thenReturn(true);
        Mockito.when(item.propertyValue(Mockito.anyString())).thenReturn("val");
        String[] csv = writer.getItemCsv(item);
        assertEquals("TestClass", csv[0]);
        assertEquals("desc", csv[4]);
    }

    @Test
    public void testGetItemCsvForAttribute() {
        CsvWriter writer = new CsvWriter();
        UmlAttribute attr = Mockito.mock(UmlAttribute.class);
        UmlItem parent = Mockito.mock(UmlItem.class);
        Mockito.when(attr.kind()).thenReturn(fr.bouml.anItemKind.anAttribute);
        Mockito.when(attr.name()).thenReturn("attr");
        Mockito.when(attr.parent()).thenReturn(parent);
        Mockito.when(parent.name()).thenReturn("ParentClass");
        Mockito.when(attr.type()).thenReturn(null);
        Mockito.when(attr.multiplicity()).thenReturn("1");
        Mockito.when(attr.description()).thenReturn("desc");
        Mockito.when(NiemUmlModel.isNiemUml(attr)).thenReturn(true);
        Mockito.when(attr.propertyValue(Mockito.anyString())).thenReturn("val");
        String[] csv = writer.getItemCsv(attr);
        assertEquals("ParentClass", csv[0]);
        assertEquals("attr", csv[1]);
        assertEquals("1", csv[3]);
        assertEquals("desc", csv[4]);
    }

    @Test
    public void testGetItemCsvForClassInstance() {
        CsvWriter writer = new CsvWriter();
        UmlClassInstance ci = Mockito.mock(UmlClassInstance.class);
        Mockito.when(ci.kind()).thenReturn(fr.bouml.anItemKind.aClassInstance);
        Mockito.when(ci.name()).thenReturn("inst");
        Mockito.when(ci.type()).thenReturn(null);
        Mockito.when(ci.description()).thenReturn("desc");
        Mockito.when(NiemUmlModel.isNiemUml(ci)).thenReturn(true);
        Mockito.when(ci.propertyValue(Mockito.anyString())).thenReturn("val");
        String[] csv = writer.getItemCsv(ci);
        assertEquals("inst", csv[1]);
        assertEquals("desc", csv[4]);
    }

    @Test
    public void testExportCsvCreatesFile() throws IOException {
        CsvWriter writer = new CsvWriter();
        // Mock static dependencies
        Mockito.mockStatic(NiemUmlModel.class);
        Mockito.mockStatic(UmlClass.class);
        Mockito.when(NiemUmlModel.getNiemMap()).thenReturn(new String[][]{{"Col1"},{"Col2"},{"Col3"},{"Col4"},{"Col5"},{"Col6"}});
        java.util.Vector<UmlItem> classes = new java.util.Vector<>();
        UmlItem mockClass = Mockito.mock(UmlItem.class);
        Mockito.when(mockClass.kind()).thenReturn(fr.bouml.anItemKind.aClass);
        Mockito.when(mockClass.name()).thenReturn("TestClass");
        Mockito.when(mockClass.description()).thenReturn("desc");
        Mockito.when(NiemUmlModel.isNiemUml(mockClass)).thenReturn(true);
        Mockito.when(mockClass.children()).thenReturn(new UmlItem[0]);
        classes.add(mockClass);
        UmlClass.classes = classes;
        String filename = System.getProperty("java.io.tmpdir") + File.separator + "test_export.csv";
        writer.exportCsv(filename);
        File file = new File(filename);
        assertTrue(file.exists());
        file.delete();
    }
    */
}