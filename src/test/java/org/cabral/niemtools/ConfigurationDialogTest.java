package org.cabral.niemtools;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.junit.Before;
import org.mockito.Mockito;

public class ConfigurationDialogTest {
    private ProjectProperties properties;

    @Before
    public void setUp() {
        properties = Mockito.mock(ProjectProperties.class);
        Mockito.when(properties.getProperty(Mockito.anyString())).thenReturn("");
        Mockito.when(properties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("");
        Mockito.when(ProjectProperties.getDefaults()).thenReturn(new Properties());
    }

    /*
    @Test
    public void testDialogConstructionDoesNotThrow() {
        try {
            @SuppressWarnings("unused")
            ConfigurationDialog dialog = new ConfigurationDialog(properties);
        } catch (Exception e) {
            fail("Construction of ConfigurationDialog threw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testShowDialogReturnsNullByDefault() {
        ConfigurationDialog dialog = new ConfigurationDialog(properties);
        // showDialog() does not show the dialog in this code, so should return null
        assertNull(dialog.showDialog());
    }

    @Test
    public void testCommandButtonSetsCommandAndClosesDialog() {
        ConfigurationDialog dialog = new ConfigurationDialog(properties);
        JButton button = invokePrivateCommandButton(dialog, "Test Command", "testCommand");
        // Simulate button click
        for (ActionListener al : button.getActionListeners()) {
            al.actionPerformed(new java.awt.event.ActionEvent(button, ActionEvent.ACTION_PERFORMED, ""));
        }
        // The command should be set
        assertEquals("testCommand", getPrivateCommand(dialog));
    }

    @Test
    public void testCheckedBoxUpdatesProperty() {
        Mockito.when(properties.getProperty("testBox")).thenReturn("false");
        ConfigurationDialog dialog = new ConfigurationDialog(properties);
        JCheckBox box = invokePrivateCheckedBox(dialog, "Test Box", "testBox");
        box.setSelected(true);
        Mockito.verify(properties, Mockito.atLeastOnce()).setProperty("testBox", "true");
        box.setSelected(false);
        Mockito.verify(properties, Mockito.atLeastOnce()).setProperty("testBox", "false");
    }
    */
    
    // Helper to access private commandButton method
    private JButton invokePrivateCommandButton(ConfigurationDialog dialog, String name, String command) {
        try {
            var m = ConfigurationDialog.class.getDeclaredMethod("commandButton", String.class, String.class);
            m.setAccessible(true);
            return (JButton) m.invoke(dialog, name, command);
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to access private checkedBox method
    private JCheckBox invokePrivateCheckedBox(ConfigurationDialog dialog, String name, String property) {
        try {
            var m = ConfigurationDialog.class.getDeclaredMethod("checkedBox", String.class, String.class);
            m.setAccessible(true);
            return (JCheckBox) m.invoke(dialog, name, property);
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to access private 'command' field
    private String getPrivateCommand(ConfigurationDialog dialog) {
        try {
            var f = ConfigurationDialog.class.getDeclaredField("command");
            f.setAccessible(true);
            return (String) f.get(dialog);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}