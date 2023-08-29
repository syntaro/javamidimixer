/*
 * Copyright 2023 Syntarou YOSHIDA.
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
package jp.synthtarou.midimixer.mx30controller;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import jp.synthtarou.midimixer.MXAppConfig;
import jp.synthtarou.midimixer.libs.common.RangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXTemplate;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX32MixerData {
    MX32MixerProcess _process;

    private ArrayList<MGSlider>[] _matrixSliderComponent;
    private ArrayList<MGCircle>[] _matrixCircleComponent;
    private ArrayList<MGPad>[] _matrixDrumComponent;

    private ArrayList<MGStatus>[] _matrixSliderStatus;
    private ArrayList<MGStatus>[] _matrixCircleStatus;
    private ArrayList<MGStatus>[] _matrixDrumStatus;

    public boolean ready() {
        return _matrixDrumComponent != null;
    }
    
    ArrayList<MGStatus>[][] _cachedControlChange;
    ArrayList<MGStatus>[][] _cachedChannelMessage;
    ArrayList<MGStatus>[][] _cachedNoteMessage;
    ArrayList<MGStatus>_cachedSystemMessage;
    ArrayList<MGStatus>_cachedDataentry;
    
    protected MX32MixerData(MX32MixerProcess process) {
        _process = process;
        initVolumeMixer();
    }
    
    public static final int INIT_TYPE_ZERO = 0;
    public static final int INIT_TYPE_MIXER = 1;
    public static final int INIT_TYPE_GMTOME = 2;
    public static final int INIT_TYPE_DAW = 3;
    public static final int INIT_TYPE_SOUDMODULE = 4;
    
    public boolean initializeData(int initType) {
        switch(initType) {
            case INIT_TYPE_MIXER:
                initVolumeMixer();
                break;
            case INIT_TYPE_DAW:
                initDaw();
                break;
            case INIT_TYPE_ZERO:
                initZero();
                break;
            case INIT_TYPE_GMTOME:
                initGMTone();
                break;
            case INIT_TYPE_SOUDMODULE:
                initSoundModule();
                break;
            default:
                JOptionPane.showMessageDialog(_process._view, "Not ready", "Sorry", JOptionPane.ERROR_MESSAGE);
                return false;
        }
        _cachedControlChange = null;
        _cachedChannelMessage = null;
        _cachedNoteMessage = null;
        _cachedSystemMessage = null;
        _cachedDataentry = null;
        return true;
    }
    
    private void initVolumeMixer() {
        MX32MixerProcess process = _process;
        ArrayList<MGStatus>[] circleMatrix = new ArrayList[MXAppConfig.CIRCLE_ROW_COUNT];
        ArrayList<MGStatus>[] sliderMatrix = new ArrayList[MXAppConfig.SLIDER_ROW_COUNT];

        int port = process._port;
        int column;
        
        circleMatrix[0] = new ArrayList();
        circleMatrix[1] = new ArrayList();
        circleMatrix[2] = new ArrayList();
        circleMatrix[3] = new ArrayList();

        sliderMatrix[0] = new ArrayList();
        
        MGStatus status;
        MXMessage message;

        for(int row = 0; row < sliderMatrix.length; ++ row) {
            ArrayList<MGStatus> slider = sliderMatrix[row];

            while (slider.size() < MXAppConfig.SLIDER_COLUMN_COUNT) {
                String text;
                column = slider.size();
                if (column >= 16) {
                    //F0H，7FH，7FH，04H，01H，00H，mm，F7H
                    text = "F0h, 7Fh, 7Fh, 04h, 01h, #VL, #VH, F7h";
                    status = new MGStatus(process._port, MGStatus.TYPE_SLIDER, row, column);
                    status.setupByDtext(text, 0, 
                            RangedValue.ZERO7, 
                            new RangedValue(RangedValue.MAX14, 0, RangedValue.MAX14));
                }else {
                    status = new MGStatus(process._port, MGStatus.TYPE_SLIDER, row, column);
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + column, MXMidi.DATA1_CC_CHANNEL_VOLUME, 128 -1);
                    status.setupByDtext(message.toDText(), message.getChannel(), message.getGate(), message.getValue());
                }
                slider.add(status);
            }
            sliderMatrix[row] = slider;
        }

        for (int row = 0; row < circleMatrix.length; ++ row) {
            ArrayList<MGStatus> circle = new ArrayList();
            column = 0;
            int[] ccCode = new int[] { 
                MXMidi.DATA1_CC_EFFECT3_CHORUS,
                MXMidi.DATA1_CC_EFFECT1_REVERVE, 
                MXMidi.DATA1_CC_EXPRESSION,
                MXMidi.DATA1_CC_PANPOT
            };
            while (circle.size() < MXAppConfig.SLIDER_COLUMN_COUNT) {
                if (column >= 16) {
                    status = new MGStatus(process._port, MGStatus.TYPE_CIRCLE, row, column);
                    String text = "F0h, 7Fh, 7Fh, 04h, 01h, #VL, #VH, F7h";

                    status.setupByDtext(text, 0, RangedValue.ZERO7, RangedValue.new14bit(128 * 128 - 1));
                    circle.add(status);
                    column ++;
                }else {
                    status = new MGStatus(process._port, MGStatus.TYPE_CIRCLE, row, column);
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + column, ccCode[row], 64);
                    status.setupByDtext(message.toDText(), column, message.getGate(), message.getValue());
                    circle.add(status);
                    
                    column ++;
                }
            }
            circleMatrix[row] = circle;
        }
 
        _matrixSliderStatus = sliderMatrix;
        _matrixCircleStatus = circleMatrix;
        _cachedControlChange = null;
        initDrumMinMidleMax();
    }
    
    private void initDaw() {
        initZero();
        
        MX32MixerData data =  _process._data;

        MGStatus status;
        MXMessage message;
        int port = _process._port;
        
        int[] cclist = {
            73, 75, 79, 72, 80, 81, 82, 83, 85
        };
        int[] cclist2 = {
            74, 71, 76, 77, 93, 18, 19, 16, 17
        };
        
        for(int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++ row) {
            for(int col = 0; col <MXAppConfig.SLIDER_COLUMN_COUNT; ++ col) {
                status = data.getSliderStatus(row, col);
                if (col < cclist.length) {
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + row, cclist[col], 128 -1);
                }else {
                    message = MXMessageFactory.createDummy();
                }
                status.setupByDtext(message.toDText(), 0, message.getGate(), message.getValue());
            }
        }

        for (int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++ row) {
            for(int col = 0; col <MXAppConfig.SLIDER_COLUMN_COUNT; ++ col) {
                status = data.getCircleStatus(row, col);
                if (col < cclist2.length) {
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + row, cclist2[col], 128 -1);
                    status.setupByDtext(message.toDText(), row, message.getGate(), message.getValue());
                }
            }
        }

        _cachedControlChange = null;
        initDrumMinMidleMax();
    }

    private void initZero() {
        MX32MixerProcess process = _process;
        ArrayList<MGStatus>[] circleMatrix = new ArrayList[MXAppConfig.CIRCLE_ROW_COUNT];
        ArrayList<MGStatus>[] sliderMatrix = new ArrayList[MXAppConfig.SLIDER_ROW_COUNT];

        circleMatrix[0] = new ArrayList();
        circleMatrix[1] = new ArrayList();
        circleMatrix[2] = new ArrayList();
        circleMatrix[3] = new ArrayList();

        sliderMatrix[0] = new ArrayList();
        
        int port = process._port;
        int column = 0;
        MGStatus status;
        MXMessage message;
        
        for(int row = 0; row < sliderMatrix.length; ++ row) {
            ArrayList<MGStatus> slider = new ArrayList();

            while (slider.size() < MXAppConfig.SLIDER_COLUMN_COUNT) {
                status = new MGStatus(process._port, MGStatus.TYPE_SLIDER, row, column);
                slider.add(status);
                column ++;
            }
            sliderMatrix[row] = slider;
        }
        column = 0;
        for (int row = 0; row < circleMatrix.length; ++ row) {
            ArrayList<MGStatus> circle = new ArrayList();
            while (circle.size() < MXAppConfig.SLIDER_COLUMN_COUNT) {
                status = new MGStatus(process._port, MGStatus.TYPE_CIRCLE, row, column);
                circle.add(status);
                column ++;
            }
            circleMatrix[row] = circle;
        }

        _matrixSliderStatus = sliderMatrix;
        _matrixCircleStatus = circleMatrix;
        _cachedControlChange = null;
        initDrumMinMidleMax();
    }

    private void initSoundModule() {
        initZero();

        MX32MixerProcess process = _process;

        int port = process._port;
        MGStatus status;
        MXMessage message;
        
        int[] cclist = {
            114, 18, 19, 16, 17, 91, 79, 72
        };
        int[] cclist2 = {
            112, 74, 71, 76, 77, 93, 73, 75
        };
        
        MX32MixerData data =  _process._data;

        for(int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++ row) {
            for(int col = 0; col <MXAppConfig.SLIDER_COLUMN_COUNT; ++ col) {
                status = data.getSliderStatus(row, col);
                if (col < cclist.length) {
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + row, cclist[col], 128 -1);
                    status.setupByDtext(message.toDText(), 0, message.getGate(), message.getValue());
                }else {
                    message = MXMessageFactory.createDummy();
                    status.setupByDtext(message.toDText(), 0, message.getGate(), message.getValue());
                }
            }
        }

        for(int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++ row) {
            for(int col = 0; col <MXAppConfig.SLIDER_COLUMN_COUNT; ++ col) {
                status = data.getCircleStatus(row, col);
                if (col < cclist2.length) {
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + row, cclist2[col], 128 -1);
                    status.setupByDtext(message.toDText(), 0, message.getGate(), message.getValue());
                }else {
                    message = MXMessageFactory.createDummy();
                    status.setupByDtext(message.toDText(), 0, message.getGate(), message.getValue());
                }
            }
        }
 
        _cachedControlChange = null;
        initDrumMinMidleMax();
    }

    private void initGMTone() {
        initZero();

        MX32MixerProcess process = _process;

        int port = process._port;
        MXMessage message = null;
        MGStatus status = null;
        MX32MixerData data =  _process._data;

        for(int row = 0; row < MXAppConfig.SLIDER_ROW_COUNT; ++ row) {
            for(int col = 0; col <MXAppConfig.SLIDER_COLUMN_COUNT; ++ col) {
                status = data.getSliderStatus(row, col);
                if (col >= 16) {
                    String text = "F0h, 7Fh, 7Fh, 04h, 01h, #VL, #VH, F7h";

                    status.setupByDtext(text, 0, RangedValue.ZERO7, RangedValue.new14bit(0));
                }else {
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + col, MXMidi.DATA1_CC_EXPRESSION, 128 -1);
                    status.setupByDtext(message.toDText(), col, message.getGate(), message.getValue());
                }
            }
        }
        
        int[] ccCode = new int[] { 
            MXMidi.DATA1_CC_SOUND_ATTACKTIME,
            MXMidi.DATA1_CC_SOUND_DECAYTIME, 
            MXMidi.DATA1_CC_SOUND_RELEASETIME, 
            MXMidi.DATA1_CC_SOUND_BLIGHTNESS,
        };
        for(int row = 0; row < MXAppConfig.CIRCLE_ROW_COUNT; ++ row) {
            for(int col = 0; col <MXAppConfig.SLIDER_COLUMN_COUNT; ++ col) {
                status = data.getSliderStatus(row, col);
                if (col >= 16) {
                    String text = "F0h, 7Fh, 7Fh, 04h, 01h, #VL, #VH, F7h";
                    status.setupByDtext(text, 0, RangedValue.ZERO7, RangedValue.new14bit(128 * 128 - 1));
                }else {
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CONTROLCHANGE + col, ccCode[row], 64);
                    status.setupByDtext(message.toDText(), col, message.getGate(), message.getValue());
                }
            }
        }
        
        int[] proglist = {
            -1, 0, 2, 5, 8, 10, 13, 27, 36, 40, 56, 65, 72, 82, 96, 106, 120
        };

        for(int row = 0; row < MXAppConfig.DRUM_ROW_COUNT; ++ row) {
            for(int col = 0; col <MXAppConfig.SLIDER_COLUMN_COUNT; ++ col) {
                status = data.getSliderStatus(row, col);
                int prog = proglist[col];
                switch(row) {
                    case 0:
                        if (prog < 0) {
                            message = MXMessageFactory.fromShortMessage(process._port, MXMidi.COMMAND_CONTROLCHANGE + 0, MXMidi.DATA1_CC_MODULATION, 127);
                        }else {
                            message = MXMessageFactory.fromShortMessage(process._port, MXMidi.COMMAND_PROGRAMCHANGE + 0, prog, 0);
                        }
                        break;
                    case 1:
                        if (prog < 0) {
                            message = MXMessageFactory.fromShortMessage(process._port, MXMidi.COMMAND_CONTROLCHANGE + 0, MXMidi.DATA1_CC_MODULATION, 64);
                        }else {
                            message = MXMessageFactory.fromShortMessage(process._port, MXMidi.COMMAND_PROGRAMCHANGE + 1, prog, 0);
                        }
                        break;
                    case 2:
                        if (prog < 0) {
                            message = MXMessageFactory.fromShortMessage(process._port, MXMidi.COMMAND_CONTROLCHANGE + 0, MXMidi.DATA1_CC_MODULATION, 0);
                        }else {
                            message = MXMessageFactory.fromShortMessage(process._port, MXMidi.COMMAND_PROGRAMCHANGE + 2, prog, 0);
                        }
                        break;
                }
                status.setupByDtext(message.toDText(), message.getChannel(), message.getGate(), message.getValue());
            }
        }
    }
    
    public void fillMaxOfSlider(MGStatus status, int column) {
        MGStatus sliderStatus = _matrixSliderStatus[0].get(column);
        MXMessage message = sliderStatus.toMXMessage(null);
        
        int x = sliderStatus.getValue()._max;
        RangedValue maxValue = new RangedValue(x, x, x);

        if (message != null) {
            status.setupByDtext(message.toDText(), message.getChannel(), message.getGate(), maxValue);
        }
        else {
            status.setupByDtext(null, 0, RangedValue.ZERO7, maxValue);
        }
        status.setSwitchType(MGStatus.SWITCH_TYPE_ON); // 1回のみで
        status.setSwitchOutOnTypeOfValue(MGStatus.SWITCH_OUT_ON_VALUE_FIXED);
        status.setSwitchOutOnValueFixed(maxValue._var);
    }

    public void fillMiddleOfSlider(MGStatus status, int column) {
        MGStatus sliderStatus = _matrixSliderStatus[0].get(column);
        
        int max = sliderStatus.getValue()._max;
        int min = sliderStatus.getValue()._min;
        if (((max - min) % 2) != 0) {
            max ++;
        }
        int middle = (max + min) / 2;
        RangedValue middleValue = new RangedValue(middle, middle, middle);

        MXMessage message = sliderStatus.toMXMessage(null);
        if (message != null) {
            status.setupByDtext(message.toDText(), message.getChannel(), message.getGate(), middleValue);
        }
        else {
            status.setupByDtext(null, 0, RangedValue.ZERO7, middleValue);
        }
        status.setSwitchType(MGStatus.SWITCH_TYPE_ON); // 1回のみで
        status.setSwitchOutOnTypeOfValue(MGStatus.SWITCH_OUT_ON_VALUE_FIXED);
        status.setSwitchOutOnValueFixed(middle);
    }
    
    public void fillMinOfSlider(MGStatus status, int column) {
        MGStatus sliderStatus = _matrixSliderStatus[0].get(column);
        
        int max = sliderStatus.getValue()._max;
        int min = sliderStatus.getValue()._min;
        RangedValue minValue = new RangedValue(min, min, min);

        MXMessage message = sliderStatus.toMXMessage(null);
        if (message != null) {
            status.setupByDtext(message.toDText(), message.getChannel(), message.getGate(), minValue);
        }
        else {
            status.setupByDtext(null, 0, RangedValue.ZERO7, minValue);
        }
        status.setSwitchType(MGStatus.SWITCH_TYPE_ON); // 1回のみで
        status.setSwitchOutOnTypeOfValue(MGStatus.SWITCH_OUT_ON_VALUE_FIXED);
        status.setSwitchOutOnValueFixed(min);
    }
    
    public void initDrumMinMidleMax() {
        MX32MixerProcess process = _process;

        ArrayList<MGStatus>[] sliderMatrix = _matrixSliderStatus;
        ArrayList<MGStatus>[] padMatrix = new ArrayList[MXAppConfig.DRUM_ROW_COUNT];

        padMatrix[0] = new ArrayList();
        padMatrix[1] = new ArrayList();
        padMatrix[2] = new ArrayList();

        int column = 0;
        while (padMatrix[0].size() < MXAppConfig.SLIDER_COLUMN_COUNT) {
            MGStatus status;
            status = new MGStatus(process._port, MGStatus.TYPE_DRUMPAD, 0, column);
            fillMaxOfSlider(status, column);
            padMatrix[0].add(status);

            status = new MGStatus(process._port, MGStatus.TYPE_DRUMPAD, 1, column);
            fillMiddleOfSlider(status, column);
            padMatrix[1].add(status);

            status = new MGStatus(process._port, MGStatus.TYPE_DRUMPAD, 2, column);
            fillMinOfSlider(status, column);
            padMatrix[2].add(status);
            column ++;
        }
        
        _matrixDrumStatus = padMatrix;
    }
    
    public  MGSlider getSlider(int row, int column) {
        if (_matrixSliderComponent == null) {
            return null;
        }
        if (row >= MXAppConfig.SLIDER_ROW_COUNT || column >= MXAppConfig.SLIDER_COLUMN_COUNT) {
            throw new IllegalArgumentException("row " + row + " , column = " + column);
        }
        return _matrixSliderComponent[row].get(column);
    }

    public  MGCircle getCircle(int row, int column) {
        if (_matrixCircleComponent == null) {
            return null;
        }
        if (row >= MXAppConfig.CIRCLE_ROW_COUNT || column >= MXAppConfig.SLIDER_COLUMN_COUNT) {
            throw new IllegalArgumentException("row " + row + " , column = " + column);
        }
        return _matrixCircleComponent[row].get(column);
    }

    public  MGPad getDrumPad(int row, int column) {
        if (_matrixDrumComponent == null) {
            return null;
        }
        if (row >= MXAppConfig.DRUM_ROW_COUNT || column >= MXAppConfig.SLIDER_COLUMN_COUNT) {
            throw new IllegalArgumentException("row " + row + " , column = " + column);
        }
        return _matrixDrumComponent[row].get(column);
    }

    public  MGStatus getSliderStatus(int row, int column) {
        return _matrixSliderStatus[row].get(column);
    }

    public  MGStatus getCircleStatus(int row, int column) {
        return _matrixCircleStatus[row].get(column);
    }

    public  MGStatus getDrumPadStatus(int row, int column) {
        return _matrixDrumStatus[row].get(column);
    }

    public  void  setSliderStatus(int row, int column, MGStatus status) {
        _matrixSliderStatus[row].set(column, status);
    }

    public  void  setCircleStatus(int row, int column, MGStatus status) {
        _matrixCircleStatus[row].set(column, status);
    }

    public  void  setDrumPadStatus(int row, int column, MGStatus status) {
        _matrixDrumStatus[row].set(column, status);
    }

    public void setEveryComponents(ArrayList<MGSlider>[] slider, ArrayList<MGCircle>[] circle, ArrayList<MGPad>[] drum) {
        _matrixSliderComponent = slider; 
        _matrixCircleComponent = circle;
        _matrixDrumComponent = drum;
        _cachedControlChange = null;
    }
}
