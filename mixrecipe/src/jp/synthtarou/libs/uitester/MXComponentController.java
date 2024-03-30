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
package jp.synthtarou.libs.uitester;

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
import jp.synthtarou.libs.MXFileLogger;
import jp.synthtarou.libs.MXRangedValue;
import static jp.synthtarou.libs.MainThreadTask.NOTHING;
import jp.synthtarou.libs.smf.SMFParser;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXComponentController {

    public static void main(String[] args) {
        MXComponentControllerDemo.main(args);
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

    protected final int _supportedType;
    protected final JComponent _component;

    protected Object _varAsObject;
    protected String _varAsText;
    protected boolean _varAsBoolean;
    protected int _varAsNumeric;
    protected int _selfLock = 0;
    protected ArrayList<MXComponentControllerListener> _listListener = new ArrayList();

    protected MXComponentController(JComponent component, int type) {
        _component = component;
        _supportedType = type;
        uiToInternal();
        installClipboard();
    }

    public MXComponentController(JComponent component, MXComponentControllerListener listener) {
        this(component);
        addChangeListener(listener);
    }

    public MXComponentController(JComponent component) {
        _component = component;
        if (component instanceof JLabel) {
            _supportedType = TYPE_LABEL;
        } else if (component instanceof JTextArea) {
            _supportedType = TYPE_TEXTAREA;
            JTextArea textArea = (JTextArea) component;
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (0 == _selfLock) {
                        // getTextが問題なくなるにはこのタイミングより遅らせる
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                uiToInternal();
                            }
                        });
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (0 == _selfLock) {
                        // getTextが問題なくなるにはこのタイミングより遅らせる
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                uiToInternal();
                            }
                        });
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (0 == _selfLock) {
                        // getTextが問題なくなるにはこのタイミングより遅らせる
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                uiToInternal();
                            }
                        });
                    }
                }
            });
        } else if (component instanceof JTextField) {
            _supportedType = TYPE_TEXTFIELD;
            JTextField textField = (JTextField) component;
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (0 == _selfLock) {
                        // getTextが問題なくなるにはこのタイミングより遅らせる
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                uiToInternal();
                            }
                        });
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (0 == _selfLock) {
                        // getTextが問題なくなるにはこのタイミングより遅らせる
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                uiToInternal();
                            }
                        });
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (0 == _selfLock) {
                        // getTextが問題なくなるにはこのタイミングより遅らせる
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                uiToInternal();
                            }
                        });
                    }
                }
            });
        } else if (component instanceof JCheckBox) {
            _supportedType = TYPE_CHECKBOX;
            JCheckBox checkBox = (JCheckBox) component;
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (0 == _selfLock) {
                        uiToInternal();
                    }
                }
            });
        } else if (component instanceof JSlider) {
            _supportedType = TYPE_SLIDER;
            JSlider slider = (JSlider) component;
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (0 == _selfLock) {
                        uiToInternal();
                    }
                }
            });
        } else if (component instanceof JSpinner) {
            _supportedType = TYPE_SPINNER;
            JSpinner spinner = (JSpinner) component;
            spinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (0 == _selfLock) {
                        uiToInternal();
                    }
                }
            });
        } else if (component instanceof JToggleButton) {
            _supportedType = TYPE_TOGGLEBUTTON;
            JToggleButton toggleButton = (JToggleButton) component;
            toggleButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (0 == _selfLock) {
                        uiToInternal();
                    }
                }
            });
        } else if (component instanceof JButton) {
            _supportedType = TYPE_BUTTON;
            JButton button = (JButton) component;
        } else {
            MXFileLogger.getLogger(MXComponentController.class).severe("Component " + component + " class = " + component.getClass() + " is not supported.");
            _supportedType = TYPE_UNKNOWN;
        }
        //init var fields
        uiToInternal();
        installClipboard();
    }

    public void copyVarFrom(MXComponentController from) {
        synchronized (this) {
            _varAsObject = from._varAsObject;
            _varAsNumeric = from._varAsNumeric;
            _varAsBoolean = from._varAsBoolean;
            _varAsText = from._varAsText;
            internalToUI();
        }
    }

    public void uiToInternal() {
        Object parsedObject;
        String parsedText;
        boolean parsedBoolean;
        int parsedNumeric;

        switch (_supportedType) {
            case TYPE_LABEL:
                JLabel label = (JLabel) _component;
                parsedObject = label.getText();
                parsedText = label.getText();
                parsedBoolean = parseBoolean(parsedText);
                parsedNumeric = parseNumeric(parsedText);
                break;
            case TYPE_TEXTAREA:
                JTextArea textArea = (JTextArea) _component;
                parsedObject = textArea.getText();
                parsedText = textArea.getText();
                parsedBoolean = parseBoolean(parsedText);
                parsedNumeric = parseNumeric(parsedText);
                break;
            case TYPE_TEXTFIELD:
                JTextField textField = (JTextField) _component;
                parsedObject = textField.getText();
                parsedText = textField.getText();
                parsedBoolean = parseBoolean(parsedText);
                parsedNumeric = parseNumeric(parsedText);
                break;
            case TYPE_CHECKBOX:
                JCheckBox checkBox = (JCheckBox) _component;
                parsedBoolean = checkBox.isSelected();
                parsedNumeric = parsedBoolean ? 1 : 0;
                parsedText = parsedBoolean ? "yes" : "no";
                parsedObject = parsedBoolean;
                break;
            case TYPE_SLIDER:
                JSlider slider = (JSlider) _component;
                parsedNumeric = slider.getValue();
                parsedBoolean = parsedNumeric > 0 ? true : false;
                parsedText = String.valueOf(parsedNumeric);
                parsedObject = parsedNumeric;
                break;
            case TYPE_SPINNER:
                JSpinner spinner = (JSpinner) _component;
                parsedNumeric = (Integer) spinner.getValue();
                parsedBoolean = parsedNumeric > 0 ? true : false;
                parsedText = String.valueOf(parsedNumeric);
                parsedObject = parsedNumeric;
                break;
            case TYPE_TOGGLEBUTTON:
                JToggleButton toggleButton = (JToggleButton) _component;
                parsedBoolean = toggleButton.isSelected();
                parsedNumeric = parsedBoolean ? 1 : 0;
                parsedText = parsedBoolean ? "yes" : "no";
                parsedObject = parsedBoolean;
                break;
            default:
                parsedBoolean = false;
                parsedNumeric = 0;
                parsedText = "";
                parsedObject = null;

        }
        fireUpdate(parsedObject, parsedText, parsedNumeric, parsedBoolean);
    }

    public synchronized void fireUpdate(Object parsedObject, String parsedText, int parsedNumeric, boolean parsedBoolean) {
        new MainThreadTask() {
            @Override
            public Object runTask() {
                String needFire = null;
                if (_supportedType == TYPE_SPINNER) {
                    JSpinner spinner = (JSpinner) _component;
                    SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
                    int min = (Integer) model.getMinimum();
                    int max = (Integer) model.getMaximum();
                    if (min <= parsedNumeric && parsedNumeric <= max) {
                    } else {
                        return NOTHING;
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
                    MXComponentControllerEvent evt = new MXComponentControllerEvent(_component, MXComponentController.this);
                    for (MXComponentControllerListener l : _listListener) {
                        l.mxValueChanged(evt);
                    }
                }
                return NOTHING;
            }
        };
    }

    public void internalToUI() {
        new MainThreadTask() {
            @Override
            public Object runTask() {
                _selfLock++;
                try {
                    switch (_supportedType) {
                        case TYPE_LABEL:
                            JLabel label = (JLabel) _component;
                            if (label.getText().equals(_varAsText) == false) {
                                label.setText(_varAsText);
                            }
                            break;
                        case TYPE_TEXTAREA:
                            JTextArea textArea = (JTextArea) _component;
                            if (textArea.getText().equals(_varAsText) == false) {
                                textArea.setText(_varAsText);
                            }
                            break;
                        case TYPE_TEXTFIELD:
                            JTextField textField = (JTextField) _component;
                            if (textField.getText().equals(_varAsText) == false) {
                                textField.setText(_varAsText);
                            }
                            break;
                        case TYPE_CHECKBOX:
                            JCheckBox checkBox = (JCheckBox) _component;
                            if (checkBox.isSelected() != _varAsBoolean) {
                                checkBox.setSelected(_varAsBoolean);
                            }
                            break;
                        case TYPE_SLIDER:
                            JSlider slider = (JSlider) _component;
                            if (slider.getValue() != _varAsNumeric) {
                                slider.setValue(_varAsNumeric);
                            }
                            break;
                        case TYPE_SPINNER:
                            JSpinner spinner = (JSpinner) _component;
                            int x1 = (Integer) spinner.getValue();
                            if (x1 != _varAsNumeric) {
                                spinner.setValue(_varAsNumeric);
                            }
                            break;
                        case TYPE_TOGGLEBUTTON:
                            JToggleButton toggleButton = (JToggleButton) _component;
                            if (toggleButton.isSelected() != _varAsBoolean) {
                                toggleButton.setSelected(_varAsBoolean);
                            }
                            break;
                        case TYPE_UNKNOWN:
                            break;
                    }
                } catch (Throwable ex) {
                    MXFileLogger.getLogger(MXComponentController.class).log(Level.WARNING, ex.getMessage(), ex);
                } finally {
                    _selfLock--;
                }
                return NOTHING;
            }
        }.waitResult();
    }

    public void set(boolean var) {
        if (_varAsBoolean == var) {
            return;
        }
        synchronized (this) {
            fireUpdate(var, var ? "yes" : "no", var ? 1 : 0, var);
        }
    }

    public void set(int var) {
        if (_varAsNumeric == var) {
            return;
        }
        synchronized (this) {
            fireUpdate(var, Integer.toString(var), var, var > 0 ? true : false);
        }
    }

    public synchronized void set(String text) {
        if (text == null) {
            fireUpdate(null, "", 0, false);
        } else {
            if (text.equals(_varAsText)) {
                return;
            }
            synchronized (this) {
                fireUpdate(text, text, parseNumeric(text), parseBoolean(text));
            }
        }
    }

    public synchronized void set(Object var) {
        if (var == null) {
            fireUpdate(null, "", 0, false);
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
                    fireUpdate(var, text, parseNumeric(text), parseBoolean(text));
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
            MXFileLogger.getLogger(MXComponentController.class
            ).log(Level.INFO, ex.getMessage(), ex);
            return null;

        } catch (IOException ex) {
            MXFileLogger.getLogger(MXComponentController.class
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
        switch (_supportedType) {
            case TYPE_TEXTAREA:
                JTextArea textArea = (JTextArea) _component;
                textArea.cut();
                break;
            case TYPE_TEXTFIELD:
                JTextField textField = (JTextField) _component;
                textField.cut();
                break;
        }
    }

    public void selectAlltext() {
        switch (_supportedType) {
            case TYPE_TEXTAREA:
                JTextArea textArea = (JTextArea) _component;
                textArea.selectAll();
                break;
            case TYPE_TEXTFIELD:
                JTextField textField = (JTextField) _component;
                textField.selectAll();
                break;
        }
    }

    public void copyText() {
        switch (_supportedType) {
            case TYPE_LABEL:
                JLabel label = (JLabel) _component;
                setClipboardText(label.getText());
                break;
            case TYPE_TEXTAREA:
                JTextArea textArea = (JTextArea) _component;
                textArea.copy();
                break;
            case TYPE_TEXTFIELD:
                JTextField textField = (JTextField) _component;
                textField.copy();
                break;
            case TYPE_CHECKBOX:
                JCheckBox checkBox = (JCheckBox) _component;
                setClipboardText(checkBox.isSelected() ? "1" : "0");
                break;
            case TYPE_SLIDER:
                JSlider slider = (JSlider) _component;
                setClipboardText(Integer.toString(slider.getValue()));
                break;
            case TYPE_SPINNER:
                JSpinner spinner = (JSpinner) _component;
                int var = (int) spinner.getValue();
                setClipboardText(Integer.toString(var));
                break;
            case TYPE_TOGGLEBUTTON:
                JToggleButton toggleButton = (JToggleButton) _component;
                setClipboardText(toggleButton.isSelected() ? "1" : "0");
                break;
            case TYPE_BUTTON:
                JButton button = (JButton) _component;
                setClipboardText(button.getText());
                break;
        }
    }

    public void pasteText() {
        switch (_supportedType) {
            case TYPE_LABEL:
                java.awt.Toolkit.getDefaultToolkit().beep();
                break;
            case TYPE_TEXTAREA:
                JTextArea textArea = (JTextArea) _component;
                textArea.paste();
                break;
            case TYPE_TEXTFIELD:
                JTextField textField = (JTextField) _component;
                textField.paste();
                break;
            case TYPE_CHECKBOX:
                JCheckBox checkBox = (JCheckBox) _component;
                String boolText = getCLipboardText();
                checkBox.setSelected(parseBoolean(boolText));
                break;
            case TYPE_SLIDER:
                JSlider slider = (JSlider) _component;
                String numText = getCLipboardText();
                slider.setValue(parseNumeric(numText));
                break;
            case TYPE_SPINNER:
                JSpinner spinner = (JSpinner) _component;
                String numText2 = getCLipboardText();
                spinner.setValue((Integer) parseNumeric(numText2));
                break;
            case TYPE_TOGGLEBUTTON:
                JToggleButton toggleButton = (JToggleButton) _component;
                String boolText2 = getCLipboardText();
                toggleButton.setSelected(parseBoolean(boolText2));
                break;
            case TYPE_BUTTON:
                java.awt.Toolkit.getDefaultToolkit().beep();
                break;
        }
    }

    public void doClickAction() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        String command = null;
                        ActionListener[] listListener;

                        if (_supportedType == TYPE_BUTTON) {
                            JButton button = (JButton) _component;
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
                                    MXFileLogger.getLogger(MXComponentController.class
                                    ).log(Level.SEVERE, ex.getMessage(), ex);

                                }
                            }
                        }

                    } catch (RuntimeException ex) {
                        MXFileLogger.getLogger(MXComponentController.class
                        ).log(Level.SEVERE, ex.getMessage(), ex);

                    } catch (Error er) {
                        MXFileLogger.getLogger(MXComponentController.class
                        ).log(Level.SEVERE, er.getMessage(), er);
                    }
                }
            });
        } catch (InvocationTargetException ex) {
            MXFileLogger.getLogger(MXComponentController.class).log(Level.WARNING, ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            MXFileLogger.getLogger(MXComponentController.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    public synchronized void addChangeListener(MXComponentControllerListener listener) {
        _listListener.add(listener);
    }

    public synchronized void removeChangeListener(MXComponentControllerListener listener) {
        _listListener.remove(listener);
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
