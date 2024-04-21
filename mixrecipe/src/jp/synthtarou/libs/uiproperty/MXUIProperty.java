/*
 * Copyright (C) 2024 Syntarou YOSHIDA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.synthtarou.libs.uiproperty;

import jp.synthtarou.libs.MainThreadTask;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.libs.MXRangedValue;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXUIProperty {

    public static void main(String[] args) {
        MXUIPropertyDemo.main(args);
    }

    protected static final int TYPE_UNKNOWN = 0;
    protected static final int TYPE_LABEL = 1;
    protected static final int TYPE_TEXTAREA = 2;
    protected static final int TYPE_TEXTFIELD = 3;
    protected static final int TYPE_CHECKBOX = 4;
    protected static final int TYPE_SLIDER = 5;
    protected static final int TYPE_SPINNER = 6;
    protected static final int TYPE_TOGGLEBUTTON = 7;
    protected static final int TYPE_BUTTON = 8;
    protected JComponent _component;

    protected Object _varAsObject = null;
    protected String _varAsText = "";
    protected boolean _varAsBoolean = false;
    protected int _varAsNumeric = 0;
    protected int _stopFeedback = 0;
    protected ArrayList<MXUIPropertyListener> _listListener = new ArrayList();

    ActionListener _actionListener = null;
    DocumentListener _documentListener = null;
    ChangeListener _changeListener = null;

    ActionListener generateActionListener() {
        if (_actionListener == null) {
            _actionListener = (ActionEvent e) -> {
                if (0 == _stopFeedback) {
                    uiToInternal();
                }
            };
        }
        return _actionListener;
    }

    DocumentListener generateDocumentListener() {
        if (_documentListener == null) {
            _documentListener = new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (0 == _stopFeedback) {
                        // getTextが問題なくなるにはこのタイミングより遅らせる
                        SwingUtilities.invokeLater(MXUIProperty.this::uiToInternal);
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (0 == _stopFeedback) {
                        // getTextが問題なくなるにはこのタイミングより遅らせる
                        SwingUtilities.invokeLater(MXUIProperty.this::uiToInternal);
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (0 == _stopFeedback) {
                        // getTextが問題なくなるにはこのタイミングより遅らせる
                        SwingUtilities.invokeLater(MXUIProperty.this::uiToInternal);
                    }
                }
            };
        }
        return _documentListener;
    }

    ChangeListener generateChangeListener() {
        if (_changeListener == null) {
            _changeListener = (ChangeEvent e) -> {
                if (0 == _stopFeedback) {
                    uiToInternal();
                }
            };
        }
        return _changeListener;
    }

    public void install(JComponent component) {
        if (_component != null) {
            uninstall();
        }
        _component = component;
        if (component instanceof JLabel) {
        } else if (component instanceof JTextArea textArea) {
            textArea.getDocument().addDocumentListener(generateDocumentListener());
        } else if (component instanceof JTextField textField) {
            textField.getDocument().addDocumentListener(generateDocumentListener());
        } else if (component instanceof JCheckBox checkBox) {
            checkBox.addActionListener(generateActionListener());
        } else if (component instanceof JSlider slider) {
            slider.addChangeListener(generateChangeListener());
        } else if (component instanceof JSpinner spinner) {
            spinner.addChangeListener(generateChangeListener());
        } else if (component instanceof JToggleButton toggleButton) {
            toggleButton.addActionListener(generateActionListener());
        } else if (component instanceof JButton button) {
        } else {
            MXFileLogger.getLogger(MXUIProperty.class).severe("Component " + component + " class = " + component.getClass() + " is not supported.");
        }
        internalToUI();
    }

    public void uninstall() {
        JComponent component = _component;
        if (component == null) {
            return;
        }
        if (component instanceof JLabel) {
        } else if (component instanceof JTextArea textArea) {
            textArea.getDocument().removeDocumentListener(generateDocumentListener());
        } else if (component instanceof JTextField textField) {
            textField.getDocument().removeDocumentListener(generateDocumentListener());
        } else if (component instanceof JCheckBox checkBox) {
            checkBox.removeActionListener(generateActionListener());
        } else if (component instanceof JSlider slider) {
            slider.removeChangeListener(generateChangeListener());
        } else if (component instanceof JSpinner spinner) {
            spinner.removeChangeListener(generateChangeListener());
        } else if (component instanceof JToggleButton toggleButton) {
            toggleButton.removeActionListener(generateActionListener());
        } else if (component instanceof JButton button) {
        } else {
            MXFileLogger.getLogger(MXUIProperty.class).severe("Component " + component + " class = " + component.getClass() + " is not supported.");
        }
        _component = null;
    }

    public MXUIProperty(int defValue) {
        set(defValue);
    }

    public MXUIProperty(String defValue) {
        set(defValue);
    }

    public MXUIProperty() {
        set((String) null);
    }

    public MXUIProperty(boolean defValue) {
        set(defValue);
    }

    public MXUIProperty(Object defValue) {
        set(defValue);
    }

    public void copyVarFrom(MXUIProperty from) {
        synchronized (this) {
            _varAsObject = from._varAsObject;
            _varAsNumeric = from._varAsNumeric;
            _varAsBoolean = from._varAsBoolean;
            _varAsText = from._varAsText;
            internalToUI();
        }
    }

    protected void uiToInternal() {
        Object parsedObject;
        String parsedText;
        boolean parsedBoolean;
        int parsedNumeric;

        JComponent component = _component;
        if (component == null) {
            return;
        }
        if (component instanceof JLabel label) {
            parsedObject = label.getText();
            parsedText = label.getText();
            parsedBoolean = parseBoolean(parsedText);
            parsedNumeric = parseNumeric(parsedText);
        } else if (component instanceof JTextArea textArea) {
            parsedObject = textArea.getText();
            parsedText = textArea.getText();
            parsedBoolean = parseBoolean(parsedText);
            parsedNumeric = parseNumeric(parsedText);
        } else if (component instanceof JTextField textField) {
            parsedObject = textField.getText();
            parsedText = textField.getText();
            parsedBoolean = parseBoolean(parsedText);
            parsedNumeric = parseNumeric(parsedText);
        } else if (component instanceof JCheckBox checkBox) {
            parsedBoolean = checkBox.isSelected();
            parsedNumeric = parsedBoolean ? 1 : 0;
            parsedText = parsedBoolean ? "yes" : "no";
            parsedObject = parsedBoolean;
        } else if (component instanceof JSlider slider) {
            parsedNumeric = slider.getValue();
            parsedBoolean = parsedNumeric > 0 ? true : false;
            parsedText = String.valueOf(parsedNumeric);
            parsedObject = parsedNumeric;
        } else if (component instanceof JSpinner spinner) {
            parsedNumeric = (Integer) spinner.getValue();
            parsedBoolean = parsedNumeric > 0 ? true : false;
            parsedText = String.valueOf(parsedNumeric);
            parsedObject = parsedNumeric;
        } else if (component instanceof JToggleButton toggleButton) {
            parsedBoolean = toggleButton.isSelected();
            parsedNumeric = parsedBoolean ? 1 : 0;
            parsedText = parsedBoolean ? "yes" : "no";
            parsedObject = parsedBoolean;
        } else if (component instanceof JButton button) {
            parsedBoolean = false;
            parsedNumeric = 0;
            parsedText = "";
            parsedObject = null;
        } else {
            parsedBoolean = false;
            parsedNumeric = 0;
            parsedText = "";
            parsedObject = null;
        }
        doUpdateUIThread(parsedObject, parsedText, parsedNumeric, parsedBoolean);
    }

    public synchronized void doUpdateUIThread(Object parsedObject, String parsedText, int parsedNumeric, boolean parsedBoolean) {
        if (SwingUtilities.isEventDispatchThread() == false) {
            SwingUtilities.invokeLater(() -> {
                doUpdateUIThread(parsedObject, parsedText, parsedNumeric, parsedBoolean);
            });
            return;
        }
        String needFire = null;
        if (_component instanceof JSpinner spinner) {
            SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
            int min = (Integer) model.getMinimum();
            int max = (Integer) model.getMaximum();
            if (min <= parsedNumeric && parsedNumeric <= max) {
            } else {
                return;
            }
        }
        if (parsedObject == null && _varAsObject != null) {
            needFire = "object null balanec";
        } else if (parsedObject != null && _varAsObject == null) {
            needFire = "object null balanec";
        } else if (parsedObject != null && _varAsObject != null) {
            try {
                if (parsedObject.equals(_varAsObject) == false) {
                    needFire = "object not equals";
                }
            } catch (Throwable ex) {
                needFire = "object equals thrown exception";
            }
        }

        if (needFire == null) {
            if (parsedText == null && _varAsText != null) {
                needFire = "text null balance";
            } else if (parsedText != null && _varAsText == null) {
                needFire = "text null balance";
            } else if (parsedText != null && _varAsText != null) {
                try {
                    if (parsedText.equals(_varAsText) == false) {
                        needFire = "text null equals";
                    }
                } catch (Throwable ex) {
                    needFire = "text equals thrown exception";
                }
            }
        }

        if (needFire == null) {
            if (parsedBoolean != _varAsBoolean) {
                needFire = "boolean not equals";
            }
            if (parsedNumeric != _varAsNumeric) {
                needFire = "numeric not equals";
            }
        }

        if (needFire != null) {
            _varAsBoolean = parsedBoolean;
            _varAsNumeric = parsedNumeric;
            _varAsObject = parsedObject;
            _varAsText = parsedText;
            internalToUI();

            if (_component != null && _listListener.isEmpty() == false) {
                MXUIPropertyEvent evt = new MXUIPropertyEvent(_component, MXUIProperty.this);
                for (MXUIPropertyListener l : _listListener) {
                    l.uiProperityValueChanged(evt);
                }
            }
        }
    }

    protected void internalToUI() {
        new MainThreadTask(() -> {
            _stopFeedback++;
            try {
                JComponent component = _component;
                if (component instanceof JLabel label) {
                    if (label.getText().equals(_varAsText) == false) {
                        label.setText(_varAsText);
                    }
                } else if (component instanceof JTextArea textArea) {
                    if (textArea.getText().equals(_varAsText) == false) {
                        textArea.setText(_varAsText);
                    }
                } else if (component instanceof JTextField textField) {
                    if (textField.getText().equals(_varAsText) == false) {
                        textField.setText(_varAsText);
                    }
                } else if (component instanceof JCheckBox checkBox) {
                    if (checkBox.isSelected() != _varAsBoolean) {
                        checkBox.setSelected(_varAsBoolean);
                    }
                } else if (component instanceof JSlider slider) {
                    if (slider.getValue() != _varAsNumeric) {
                        slider.setValue(_varAsNumeric);
                    }
                } else if (component instanceof JSpinner spinner) {
                    int x1 = (Integer) spinner.getValue();
                    if (x1 != _varAsNumeric) {
                        spinner.setValue(_varAsNumeric);
                    }
                } else if (component instanceof JToggleButton toggleButton) {
                    if (toggleButton.isSelected() != _varAsBoolean) {
                        toggleButton.setSelected(_varAsBoolean);
                    }
                } else if (component instanceof JButton button) {
                }
            } finally {
                _stopFeedback--;
            }
        }).join();
    }

    public void set(boolean var) {
        if (_varAsBoolean == var) {
            return;
        }
        synchronized (this) {
            doUpdateUIThread(var, var ? "yes" : "no", var ? 1 : 0, var);
        }
    }

    public void set(int var) {
        if (_varAsNumeric == var) {
            return;
        }
        synchronized (this) {
            doUpdateUIThread(var, Integer.toString(var), var, var > 0 ? true : false);
        }
    }

    public synchronized void set(String text) {
        if (text == null) {
            doUpdateUIThread(null, "", 0, false);
        } else {
            if (text.equals(_varAsText)) {
                return;
            }
            synchronized (this) {
                doUpdateUIThread(text, text, parseNumeric(text), parseBoolean(text));
            }
        }
    }

    public synchronized void set(Object var) {
        if (var == null) {
            doUpdateUIThread(null, "", 0, false);
        } else {
            if (var instanceof Boolean) {
                set((boolean) var);
            } else if (var instanceof Integer) {
                set((int) var);
            } else if (var instanceof String) {
                set((String) var);
            } else if (var instanceof MXRangedValue) {
                MXRangedValue rv = (MXRangedValue) var;
                set(rv._value);
            } else {
                synchronized (this) {
                    String text = String.valueOf(var);
                    doUpdateUIThread(var, text, parseNumeric(text), parseBoolean(text));
                }
            }
        }
    }

    public Object get() {
        synchronized (this) {
            return _varAsObject;
        }
    }

    public synchronized String getAsText() {
        synchronized (this) {
            return _varAsText;
        }
    }

    public synchronized int getAsInt() {
        synchronized (this) {
            return _varAsNumeric;
        }
    }

    long _lastUnique = 0;

    public void setClipboardText(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);
    }

    public String getCLipboardText() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        DataFlavor[] listFlavor = clipboard.getAvailableDataFlavors();
        if (listFlavor == null) {
            return null;
        }
        DataFlavor text = DataFlavor.selectBestTextFlavor(listFlavor);
        if (text == null) {
            return null;
        }
        Object data;
        try {
            data = clipboard.getData(text);
            if (data == null) {
                return null;
            }
            return data.toString();

        } catch (UnsupportedFlavorException ex) {
            MXFileLogger.getLogger(MXUIProperty.class
            ).log(Level.INFO, ex.getMessage(), ex);
            return null;

        } catch (IOException ex) {
            MXFileLogger.getLogger(MXUIProperty.class
            ).log(Level.INFO, ex.getMessage(), ex);
            return null;
        }
    }

    public void installClipboard() {
        _component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != 1) {
                    createPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    JPopupMenu _popup = null;

    public JPopupMenu createPopupMenu() {
        if (_popup != null) {
            return _popup;
        }

        JPopupMenu menuPopup = new JPopupMenu();

        JMenuItem menuCut = new JMenuItem("Cut");
        JMenuItem menuCopy = new JMenuItem("Copy");
        JMenuItem menuPaste = new JMenuItem("Paste");
        JMenuItem menuSelectAll = new JMenuItem("Select All");

        menuCut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cutText();
            }
        });
        menuCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyText();
            }
        });
        menuPaste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteText();
            }
        });
        menuSelectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAlltext();
            }
        });
        if (_component instanceof JTextField) {
            JTextField field = (JTextField) _component;
            if (field.isEditable()) {
                menuPopup.add(menuCut);
            }
            menuPopup.add(menuCopy);
            if (field.isEditable()) {
                menuPopup.add(menuPaste);
            }
            menuPopup.add(menuSelectAll);
        } else if (_component instanceof JTextArea) {
            JTextArea area = (JTextArea) _component;
            if (area.isEditable()) {
                menuPopup.add(menuCut);
            }
            menuPopup.add(menuCopy);
            if (area.isEditable()) {
                menuPopup.add(menuPaste);
            }
            menuPopup.add(menuSelectAll);
        } else {
            menuPopup.add(menuCopy);
            menuPopup.add(menuPaste);
        }

        _popup = menuPopup;
        return _popup;
    }

    public void cutText() {
        if (_component instanceof JTextArea textArea) {
            textArea.cut();
        } else if (_component instanceof JTextField textField) {
            textField.cut();
        }
    }

    public void selectAlltext() {
        if (_component instanceof JTextArea textArea) {
            textArea.selectAll();
        } else if (_component instanceof JTextField textField) {
            textField.selectAll();
        }
    }

    public void copyText() {
        JComponent component = _component;
        if (component instanceof JLabel label) {
            setClipboardText(label.getText());
        } else if (component instanceof JTextArea textArea) {
            textArea.copy();
        } else if (component instanceof JTextField textField) {
            textField.copy();
        } else if (component instanceof JCheckBox checkBox) {
            setClipboardText(checkBox.isSelected() ? "1" : "0");
        } else if (component instanceof JSlider slider) {
            setClipboardText(Integer.toString(slider.getValue()));
        } else if (component instanceof JSpinner spinner) {
            int var = (int) spinner.getValue();
            setClipboardText(Integer.toString(var));
        } else if (component instanceof JToggleButton toggleButton) {
            setClipboardText(toggleButton.isSelected() ? "1" : "0");
        } else if (component instanceof JButton button) {
            setClipboardText(button.getText());
        }
    }

    public void pasteText() {
        //allow feedback -> catch
        _stopFeedback++;
        try {
            JComponent component = _component;
            if (component instanceof JLabel label) {
                java.awt.Toolkit.getDefaultToolkit().beep();
            } else if (component instanceof JTextArea textArea) {
                textArea.paste();
                uiToInternal();
            } else if (component instanceof JTextField textField) {
                textField.paste();
                uiToInternal();
            } else if (component instanceof JCheckBox checkBox) {
                String boolText = getCLipboardText();
                checkBox.setSelected(parseBoolean(boolText));
                uiToInternal();
            } else if (component instanceof JSlider slider) {
                String numText = getCLipboardText();
                slider.setValue(parseNumeric(numText));
                uiToInternal();
            } else if (component instanceof JSpinner spinner) {
                String numText2 = getCLipboardText();
                spinner.setValue((Integer) parseNumeric(numText2));
                uiToInternal();
            } else if (component instanceof JToggleButton toggleButton) {
                String boolText2 = getCLipboardText();
                toggleButton.setSelected(parseBoolean(boolText2));
                uiToInternal();
            } else if (component instanceof JButton button) {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } finally {
            _stopFeedback--;
        }
    }

    public void doClickAction() {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    doClickAction();
                });
            } catch (InvocationTargetException ex) {
                MXFileLogger.getLogger(MXUIProperty.class).log(Level.WARNING, ex.getMessage(), ex);
            } catch (InterruptedException ex) {
                MXFileLogger.getLogger(MXUIProperty.class).log(Level.WARNING, ex.getMessage(), ex);
            }
            return;
        }
        try {
            String command = null;
            ActionListener[] listListener;

            if (_component instanceof JButton button) {
                command = button.getActionCommand();
                listListener = button.getActionListeners();
            } else {
                return;
            }

            if (listListener != null) {
                long tick = System.currentTimeMillis();
                while (tick <= _lastUnique) {
                    tick++;
                }
                _lastUnique = tick;

                ActionEvent evt = new ActionEvent(_component, (int) tick, command);

                for (ActionListener l : listListener) {
                    try {
                        l.actionPerformed(evt);

                    } catch (Throwable ex) {
                        MXFileLogger.getLogger(MXUIProperty.class).log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            }

        } catch (RuntimeException ex) {
            MXFileLogger.getLogger(MXUIProperty.class
            ).log(Level.SEVERE, ex.getMessage(), ex);

        } catch (Error er) {
            MXFileLogger.getLogger(MXUIProperty.class
            ).log(Level.SEVERE, er.getMessage(), er);
        }
    }

    public synchronized boolean addChangeListener(MXUIPropertyListener listener) {
        if (_listListener.contains(listener)) {
            return false;
        }
        _listListener.add(listener);
        return true;
    }

    public synchronized boolean removeChangeListener(MXUIPropertyListener listener) {
        return _listListener.remove(listener);
    }

    public static int parseNumeric(String text) {
        try {
            return Integer.parseInt(text);
        } catch (Throwable ex) {
            return 0;
        }
    }

    public static boolean parseBoolean(String text) {
        if (text == null) {
            return false;
        }
        if (text.equalsIgnoreCase("true")
                || text.equalsIgnoreCase("yes")
                || text.equals("1")) {
            return true;
        }
        if (text.equalsIgnoreCase("false")
                || text.equalsIgnoreCase("no")
                || text.equals("0")) {
            return false;
        }
        return false;
    }

    public JComponent getComponent() {
        return _component;
    }
}
