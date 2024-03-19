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
package jp.synthtarou.midimixer.libs.swing;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import jp.synthtarou.midimixer.libs.common.MXLogger2;
import jp.synthtarou.midimixer.libs.common.MXRangedValue;
import static jp.synthtarou.midimixer.libs.swing.UITask.NOTHING;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class UIAccessor {

    static final int TYPE_UNKNOWN = 0;
    static final int TYPE_LABEL = 1;
    static final int TYPE_TEXTAREA = 2;
    static final int TYPE_TEXTFIELD = 3;
    static final int TYPE_CHECKBOX = 4;
    static final int TYPE_SLIDER = 5;
    static final int TYPE_SPINNER = 6;
    static final int TYPE_TOGGLEBUTTON = 7;

    final int _supportedType;
    final JComponent _component;

    Object _varAsObject;
    String _varAsText;
    boolean _varAsBoolean;
    int _varAsInt;

    public static int parseNumber(String text) {
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

    public UIAccessor(JComponent component) {
        _component = component;
        if (component instanceof JLabel) {
            _supportedType = TYPE_LABEL;
        } else if (component instanceof JTextArea) {
            _supportedType = TYPE_TEXTAREA;
        } else if (component instanceof JTextField) {
            _supportedType = TYPE_TEXTFIELD;
        } else if (component instanceof JCheckBox) {
            _supportedType = TYPE_CHECKBOX;
        } else if (component instanceof JSlider) {
            _supportedType = TYPE_SLIDER;
        } else if (component instanceof JSpinner) {
            _supportedType = TYPE_SPINNER;
        } else if (component instanceof JToggleButton) {
            _supportedType = TYPE_TOGGLEBUTTON;
        } else {
            MXLogger2.getLogger(UIAccessor.class).severe("Component " + component + " class = " + component.getClass() + " is not supported.");
            _supportedType = TYPE_UNKNOWN;
        }
        readComponentValue();
    }

    public synchronized void readComponentValue() {
        switch (_supportedType) {
            case TYPE_LABEL:
                JLabel label = (JLabel) _component;
                _varAsObject = label.getText();
                _varAsText = label.getText();
                _varAsBoolean = parseBoolean(_varAsText);
                _varAsInt = parseNumber(_varAsText);
                break;
            case TYPE_TEXTAREA:
                JTextArea textArea = (JTextArea) _component;
                _varAsObject = textArea.getText();
                _varAsText = textArea.getText();
                _varAsBoolean = parseBoolean(_varAsText);
                _varAsInt = parseNumber(_varAsText);
                break;
            case TYPE_TEXTFIELD:
                JTextField textField = (JTextField) _component;
                _varAsObject = textField.getText();
                _varAsText = textField.getText();
                _varAsBoolean = parseBoolean(_varAsText);
                _varAsInt = parseNumber(_varAsText);
                break;
            case TYPE_CHECKBOX:
                JCheckBox checkBox = (JCheckBox) _component;
                _varAsBoolean = checkBox.isSelected();
                _varAsInt = _varAsBoolean ? 1 : 0;
                _varAsText = _varAsBoolean ? "yes" : "no";
                _varAsObject = _varAsBoolean;
                break;
            case TYPE_SLIDER:
                JSlider slider = (JSlider) _component;
                _varAsInt = slider.getValue();
                _varAsBoolean = _varAsInt > 0 ? true : false;
                _varAsText = String.valueOf(_varAsInt);
                _varAsObject = _varAsInt;
                break;
            case TYPE_SPINNER:
                JSpinner spinner = (JSpinner) _component;
                _varAsInt = (Integer) spinner.getValue();
                _varAsBoolean = _varAsInt > 0 ? true : false;
                _varAsText = String.valueOf(_varAsInt);
                _varAsObject = _varAsInt;
                break;
            case TYPE_TOGGLEBUTTON:
                JToggleButton toggleButton = (JToggleButton) _component;
                _varAsBoolean = toggleButton.isSelected();
                _varAsInt = _varAsBoolean ? 1 : 0;
                _varAsText = _varAsBoolean ? "yes" : "no";
                _varAsObject = _varAsBoolean;
                break;
        }
        /* TYPE_UNKNOWN = 0; */
    }

    public void writeComponetValue() {
        new UITask() {
            @Override
            public Object run() {
                switch (_supportedType) {
                    case TYPE_LABEL:
                        JLabel label = (JLabel) _component;
                        label.setText(_varAsText);
                        break;
                    case TYPE_TEXTAREA:
                        JTextArea textArea = (JTextArea) _component;
                        textArea.setText(_varAsText);
                        break;
                    case TYPE_TEXTFIELD:
                        JTextField textField = (JTextField) _component;
                        textField.setText(_varAsText);
                        break;
                    case TYPE_CHECKBOX:
                        JCheckBox checkBox = (JCheckBox) _component;
                        checkBox.setSelected(_varAsBoolean);
                        break;
                    case TYPE_SLIDER:
                        JSlider slider = (JSlider) _component;
                        slider.setValue(_varAsInt);
                        break;
                    case TYPE_SPINNER:
                        JSpinner spinner = (JSpinner) _component;
                        spinner.setValue(_varAsInt);
                        break;
                    case TYPE_TOGGLEBUTTON:
                        JToggleButton toggleButton = (JToggleButton) _component;
                        toggleButton.setSelected(_varAsBoolean);
                        break;
                    case TYPE_UNKNOWN:
                        break;
                }
                return NOTHING;
            }
        };
    }

    public synchronized void set(boolean var) {
        _varAsObject = var;
        _varAsBoolean = var;
        _varAsInt = var ? 1 : 0;
        _varAsText = var ? "yes" : "no";
        writeComponetValue();
    }
    
    public synchronized void set(int var) {
        _varAsObject = var;
        _varAsBoolean = var > 0 ? true : false;
        _varAsInt = var;
        _varAsText = Integer.toString(_varAsInt);
        writeComponetValue();
    }

    public synchronized void set(String text) {
        if (text == null) {
            _varAsObject = null;
            _varAsBoolean = false;
            _varAsInt = 0;
            _varAsText = "";
        }
        else {
            _varAsObject = text;
            _varAsText = text;
            _varAsBoolean = parseBoolean(text);
            _varAsInt = parseNumber(text);
        }
        writeComponetValue();
    }

    public synchronized void set(Object var) {
        _varAsObject = var;
        if (var == null) {
            _varAsObject = null;
            _varAsBoolean = false;
            _varAsInt = 0;
            _varAsText = "";
        } else {
            if (var instanceof Boolean) {
                set((boolean)var);
            }
            else if (var instanceof Integer) {
                set((int)var);
            }
            else if (var instanceof String) {
                set((String)var);
            }
            else if (var instanceof MXRangedValue) {
                MXRangedValue rv = (MXRangedValue)var;
                set(rv._value);
            }
            else {
                String text = String.valueOf(var);
                set(text);
            }
        }
        writeComponetValue();
    }

    public synchronized Object get() {
        return _varAsObject;
    }

    public synchronized String getAsText() {
        return _varAsText;
    }

    public synchronized int getAsInt() {
        return _varAsInt;
    }
}
