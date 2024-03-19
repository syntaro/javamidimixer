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
package jp.synthtarou.midimixer.libs.swing.variableui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import static jp.synthtarou.midimixer.libs.swing.variableui.VUITask.NOTHING;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class VUIAccessor {

    public static void main(String[] args) {
        VUIAccessorDemo.main(args);
    }
    
    protected static final int TYPE_UNKNOWN = 0;
    protected static final int TYPE_LABEL = 1;
    protected static final int TYPE_TEXTAREA = 2;
    protected static final int TYPE_TEXTFIELD = 3;
    protected static final int TYPE_CHECKBOX = 4;
    protected static final int TYPE_SLIDER = 5;
    protected static final int TYPE_SPINNER = 6;
    protected static final int TYPE_TOGGLEBUTTON = 7;
    
    protected final int _supportedType;
    protected final JComponent _component;
    
    protected Object _varAsObject;
    protected String _varAsText;
    protected boolean _varAsBoolean;
    protected int _varAsNumeric;
    protected int _selfLock = 0;
    protected ArrayList<VUIAccessorListener> _listListener = new ArrayList();
    
    public synchronized void addChangeListener(VUIAccessorListener listener) {        
        _listListener.add(listener);
    }
    
    public synchronized void removeChangeListener(VUIAccessorListener listener) {        
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
    
    public VUIAccessor(JComponent component, int type) {
        _component = component;
        _supportedType = type;
        uiToInternal();
    }
    
    public VUIAccessor(JComponent component) {
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
        } else {
            MXLogger2.getLogger(VUIAccessor.class).severe("Component " + component + " class = " + component.getClass() + " is not supported.");
            _supportedType = TYPE_UNKNOWN;
        }
        //init var fields
        uiToInternal();
    }
    
    public synchronized void uiToInternal() {
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
    
    public void fireUpdate(Object parsedObject, String parsedText, int parsedNumeric, boolean parsedBoolean) {
        String needFire = null;
        if (parsedText == null) {
            parsedText = "";
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
            VUIAccessorEvent evt = new VUIAccessorEvent(_component, this);
            for (VUIAccessorListener l : _listListener) {
                l.accessorUIValueChanged(evt);
            }
        }
    }
    
    public void internalToUI() {
        new VUITask() {
            @Override
            public Object run() {
                synchronized (VUIAccessor.this) {
                    _selfLock ++;
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
                                if (((Integer) spinner.getValue()) != _varAsNumeric) {
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
                    } catch (IllegalStateException ex) {
                        ex.printStackTrace();
                    } finally {
                        _selfLock --;
                    }
                }
                return NOTHING;
            }
        };
    }
    
    public synchronized void set(boolean var) {
        if (_varAsBoolean == var) {
            return;
        }
        fireUpdate(var, var ? "yes" : "no", var ? 1 : 0, var);
    }
    
    public synchronized void set(int var) {
        if (_varAsNumeric == var) {
            return;
        }
        fireUpdate(var, Integer.toString(var), var, var > 0 ? true : false);
    }
    
    public synchronized void set(String text) {
        if (text == null) {
            fireUpdate(null, "", 0, false);
        } else {
            if (text.equals(_varAsText)) {
                return;
            }
            fireUpdate(text, text, parseNumeric(text), parseBoolean(text));
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
                String text = String.valueOf(var);
                fireUpdate(var, text, parseNumeric(text), parseBoolean(text));
            }
        }
    }
    
    public synchronized Object get() {
        return _varAsObject;
    }
    
    public synchronized String getAsText() {
        return _varAsText;
    }
    
    public synchronized int getAsInt() {
        return _varAsNumeric;
    }
}
