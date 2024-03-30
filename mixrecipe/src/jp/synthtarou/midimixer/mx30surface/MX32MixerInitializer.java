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
 * along with _mixer program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.synthtarou.midimixer.mx30surface;

import java.util.ArrayList;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.libs.MXRangedValue;
import jp.synthtarou.midimixer.libs.midi.MXMessage;
import jp.synthtarou.midimixer.libs.midi.MXMessageFactory;
import jp.synthtarou.midimixer.libs.midi.MXMidi;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX32MixerInitializer {
    MX32MixerProcess _mixer;
    
    public MX32MixerInitializer(MX32MixerProcess mixer) {
        _mixer = mixer;
    }

    public static final int INIT_TYPE_ZERO = 0;
    public static final int INIT_TYPE_MIXER = 1;
    public static final int INIT_TYPE_GMTOME = 2;
    public static final int INIT_TYPE_DAW = 3;
    public static final int INIT_TYPE_SOUDMODULE = 4;

    public boolean initializeData(int initType) {
        switch (initType) {
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
                return false;
        }
        _mixer.notifyCacheBroken();
        return true;
    }

    void initVolumeMixer() {
        ArrayList<MGStatus>[] circleMatrix = new ArrayList[MXConfiguration.CIRCLE_ROW_COUNT];
        ArrayList<MGStatus>[] sliderMatrix = new ArrayList[MXConfiguration.SLIDER_ROW_COUNT];

        circleMatrix[0] = new ArrayList();
        circleMatrix[1] = new ArrayList();
        circleMatrix[2] = new ArrayList();
        circleMatrix[3] = new ArrayList();

        sliderMatrix[0] = new ArrayList();

        MGStatus status;
        MXMessage message;
        
        int port = _mixer._port;

        for (int row = 0; row < sliderMatrix.length; ++row) {
            ArrayList<MGStatus> slider = sliderMatrix[row];

            while (slider.size() < MXConfiguration.SLIDER_COLUMN_COUNT) {
                String text;
                if (slider.size() >= 16) {
                    //F0H，7FH，7FH，04H，01H，00H，mm，F7H
                    text = "F0h, 7Fh, 7Fh, 04h, 01h, #VL, #VH, F7h";
                    MXMessage base = MXMessageFactory.fromCCXMLText(port, text, 0);
                    status = new MGStatus(_mixer,  MGStatus.TYPE_SLIDER, row, slider.size());
                    status._base = base;
                    status.setMessageValue(MXRangedValue.new14bit(128 * 128 - 1));
                } else {
                    MXMessage base = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_CONTROLCHANGE + slider.size(), MXMidi.DATA1_CC_CHANNEL_VOLUME, 128 - 1);
                    status = new MGStatus(_mixer,  MGStatus.TYPE_SLIDER, row, slider.size());
                    status._base = base;
                }
                slider.add(status);
            }
            sliderMatrix[row] = slider;
        }

        for (int row = 0; row < circleMatrix.length; ++row) {
            ArrayList<MGStatus> circle = new ArrayList();

            int[] ccCode = new int[]{
                MXMidi.DATA1_CC_EFFECT3_CHORUS,
                MXMidi.DATA1_CC_EFFECT1_REVERVE,
                MXMidi.DATA1_CC_EXPRESSION,
                MXMidi.DATA1_CC_PANPOT
            };
            while (circle.size() < MXConfiguration.SLIDER_COLUMN_COUNT) {
                if (circle.size() >= 16) {
                    status = new MGStatus(_mixer,  MGStatus.TYPE_CIRCLE, row, circle.size());
                    String text = "F0h, 7Fh, 7Fh, 04h, 01h, #VL, #VH, F7h";

                    MXMessage base = MXMessageFactory.fromCCXMLText(port, text, 0);
                    status.setBaseMessage(base);
                    circle.add(status);
                } else {
                    status = new MGStatus(_mixer,  MGStatus.TYPE_CIRCLE, row, circle.size());
                    message = MXMessageFactory.fromShortMessage(port, MXMidi.COMMAND_CH_CONTROLCHANGE + circle.size(), ccCode[row], 64);
                    status._base = message;
                    circle.add(status);
                }
            }
            circleMatrix[row] = circle;
        }

        _mixer._matrixSliderStatus = sliderMatrix;
        _mixer._matrixCircleStatus = circleMatrix;
        _mixer.notifyCacheBroken();
        
        initDrumMinMidleMax();
    }

    void initDaw() {
        initZero();

        MGStatus status;
        MXMessage message;

        int[] cclist = {
            73, 75, 79, 72, 80, 81, 82, 83, 85
        };
        int[] cclist2 = {
            74, 71, 76, 77, 93, 18, 19, 16, 17
        };
        
        int _port = _mixer._port;

        for (int row = 0; row < MXConfiguration.SLIDER_ROW_COUNT; ++row) {
            for (int col = 0; col < MXConfiguration.SLIDER_COLUMN_COUNT; ++col) {
                status = _mixer.getStatus(MGStatus.TYPE_SLIDER, row, col);
                if (col < cclist.length) {
                    message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + row, cclist[col], 128 - 1);
                } else {
                    message = MXMessageFactory.createDummy();
                }
                status._base = message;
            }
        }

        for (int row = 0; row < MXConfiguration.CIRCLE_ROW_COUNT; ++row) {
            for (int col = 0; col < MXConfiguration.SLIDER_COLUMN_COUNT; ++col) {
                status = _mixer.getStatus(MGStatus.TYPE_CIRCLE, row, col);
                if (col < cclist2.length) {
                    message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + row, cclist2[col], 128 - 1);
                    status._base = message;
                }
            }
        }

        _mixer.notifyCacheBroken();
        initDrumMinMidleMax();
    }

    void initZero() {
        ArrayList<MGStatus>[] circleMatrix = new ArrayList[MXConfiguration.CIRCLE_ROW_COUNT];
        ArrayList<MGStatus>[] sliderMatrix = new ArrayList[MXConfiguration.SLIDER_ROW_COUNT];

        circleMatrix[0] = new ArrayList();
        circleMatrix[1] = new ArrayList();
        circleMatrix[2] = new ArrayList();
        circleMatrix[3] = new ArrayList();

        sliderMatrix[0] = new ArrayList();

        MGStatus status;
        MXMessage message;

        int _port = _mixer._port;

        for (int row = 0; row < sliderMatrix.length; ++row) {
            ArrayList<MGStatus> slider = new ArrayList();

            while (slider.size() < MXConfiguration.SLIDER_COLUMN_COUNT) {
                status = new MGStatus(_mixer,  MGStatus.TYPE_SLIDER, row, slider.size());
                slider.add(status);
            }
            sliderMatrix[row] = slider;
        }
        for (int row = 0; row < circleMatrix.length; ++row) {
            ArrayList<MGStatus> circle = new ArrayList();
            while (circle.size() < MXConfiguration.SLIDER_COLUMN_COUNT) {
                status = new MGStatus(_mixer,  MGStatus.TYPE_CIRCLE, row, circle.size());
                circle.add(status);
            }
            circleMatrix[row] = circle;
        }

        _mixer._matrixSliderStatus = sliderMatrix;
        _mixer._matrixCircleStatus = circleMatrix;
        _mixer.notifyCacheBroken();

        initDrumMinMidleMax();
    }

    void initSoundModule() {
        initZero();

        MGStatus status;
        MXMessage message;
        int _port = _mixer._port;

        int[] cclist = {
            114, 18, 19, 16, 17, 91, 79, 72
        };
        int[] cclist2 = {
            112, 74, 71, 76, 77, 93, 73, 75
        };

        for (int row = 0; row < MXConfiguration.SLIDER_ROW_COUNT; ++row) {
            for (int col = 0; col < MXConfiguration.SLIDER_COLUMN_COUNT; ++col) {
                status = _mixer.getStatus(MGStatus.TYPE_SLIDER, row, col);
                if (col < cclist.length) {
                    message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + row, cclist[col], 128 - 1);
                } else {
                    message = MXMessageFactory.createDummy();
                }
                status._base = message;
            }
        }

        for (int row = 0; row < MXConfiguration.CIRCLE_ROW_COUNT; ++row) {
            for (int col = 0; col < MXConfiguration.SLIDER_COLUMN_COUNT; ++col) {
                status = _mixer.getStatus(MGStatus.TYPE_CIRCLE, row, col);
                if (col < cclist2.length) {
                    message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + row, cclist2[col], 128 - 1);
                } else {
                    message = MXMessageFactory.createDummy();
                }
                status._base = message;
            }
        }

        _mixer.notifyCacheBroken();

        initDrumMinMidleMax();
    }

    void initGMTone() {
        initZero();

        MXMessage message = null;
        MGStatus status = null;
        int _port = _mixer._port;

        for (int row = 0; row < MXConfiguration.SLIDER_ROW_COUNT; ++row) {
            for (int col = 0; col < MXConfiguration.SLIDER_COLUMN_COUNT; ++col) {
                status = _mixer.getStatus(MGStatus.TYPE_SLIDER, row, col);
                if (col >= 16) {
                    String text = "F0h, 7Fh, 7Fh, 04h, 01h, #VL, #VH, F7h";
                    message = MXMessageFactory.fromCCXMLText(_port, text, 0);
                } else {
                    message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + col, MXMidi.DATA1_CC_EXPRESSION, 128 - 1);
                }
                status._base = message;
            }
        }

        int[] ccCode = new int[]{
            MXMidi.DATA1_CC_SOUND_ATTACKTIME,
            MXMidi.DATA1_CC_SOUND_DECAYTIME,
            MXMidi.DATA1_CC_SOUND_RELEASETIME,
            MXMidi.DATA1_CC_SOUND_BLIGHTNESS,};
        for (int row = 0; row < MXConfiguration.CIRCLE_ROW_COUNT; ++row) {
            for (int col = 0; col < MXConfiguration.SLIDER_COLUMN_COUNT; ++col) {
                status = _mixer.getStatus(MGStatus.TYPE_CIRCLE, row, col);
                if (col >= 16) {
                    String text = "F0h, 7Fh, 7Fh, 04h, 01h, #VL, #VH, F7h";
                    message = MXMessageFactory.fromCCXMLText(_port, text, 0, MXRangedValue.ZERO7, MXRangedValue.new14bit(0));
                } else {
                    message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + col, ccCode[row], 64);
                }
                status._base = message;
            }
        }

        int[] proglist = {
            -1, 0, 2, 5, 8, 10, 13, 27, 36, 40, 56, 65, 72, 82, 96, 106, 120
        };

        for (int row = 0; row < MXConfiguration.DRUM_ROW_COUNT; ++row) {
            for (int col = 0; col < MXConfiguration.SLIDER_COLUMN_COUNT; ++col) {
                status = _mixer.getStatus(MGStatus.TYPE_DRUMPAD, row, col);
                int prog = proglist[col];
                switch (row) {
                    case 0:
                        if (prog < 0) {
                            message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + 0, MXMidi.DATA1_CC_MODULATION, 127);
                        } else {
                            message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_PROGRAMCHANGE + 0, prog, 0);
                        }
                        break;
                    case 1:
                        if (prog < 0) {
                            message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + 0, MXMidi.DATA1_CC_MODULATION, 64);
                        } else {
                            message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_PROGRAMCHANGE + 1, prog, 0);
                        }
                        break;
                    case 2:
                        if (prog < 0) {
                            message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_CONTROLCHANGE + 0, MXMidi.DATA1_CC_MODULATION, 0);
                        } else {
                            message = MXMessageFactory.fromShortMessage(_port, MXMidi.COMMAND_CH_PROGRAMCHANGE + 2, prog, 0);
                        }
                        break;
                }
                status._base = message;
            }
        }
    }

    
    public void fillMaxOfSlider(MGStatus status, int column) {
        MGStatus sliderStatus = _mixer._matrixSliderStatus[0].get(column);
        MXMessage base = (MXMessage)sliderStatus._base.clone();

        int x = sliderStatus.getValue()._max;
        MXRangedValue hit = new MXRangedValue(x, x, x);
        status._base = base;
        status._drum._outStyle = MGStatusForDrum.STYLE_LINK_SLIDER;
        status._drum._linkKontrolType = MGStatus.TYPE_SLIDER;
        status._drum._linkMode = MGStatusForDrum.LINKMODE_MAX;
        status._drum._linkRow = 0;
        status._drum._linkColumn = -1;
        status._drum._strikeZone = hit;
        status._drum._mouseOnValue = x;
        status._drum._outValueTypeOn = MGStatusForDrum.VALUETYPE_AS_INPUT;
        status._drum._outValueTypeOff = MGStatusForDrum.VALUETYPE_NOTHING;
    }

    public void fillMiddleOfSlider(MGStatus status, int column) {
        MGStatus sliderStatus = _mixer._matrixSliderStatus[0].get(column);
        MXMessage base = (MXMessage)sliderStatus._base.clone();

        int x = (int)Math.round((sliderStatus.getValue()._max + sliderStatus.getValue()._min) /2.0);
        MXRangedValue hit = new MXRangedValue(x, x, x);
        status._base = base;
        status._drum._outStyle = MGStatusForDrum.STYLE_LINK_SLIDER;
        status._drum._linkKontrolType = MGStatus.TYPE_SLIDER;
        status._drum._linkMode = MGStatusForDrum.LINKMODE_MIDDLE;
        status._drum._linkRow = 0;
        status._drum._linkColumn = -1;
        status._drum._strikeZone = hit;
        status._drum._mouseOnValue = x;
        status._drum._outValueTypeOn = MGStatusForDrum.VALUETYPE_AS_INPUT;
        status._drum._outValueTypeOff = MGStatusForDrum.VALUETYPE_NOTHING;
    }

    public void fillMinOfSlider(MGStatus status, int column) {
        MGStatus sliderStatus = _mixer._matrixSliderStatus[0].get(column);
        MXMessage base = (MXMessage)sliderStatus._base.clone();

        int x = sliderStatus.getValue()._min;
        MXRangedValue hit = new MXRangedValue(x, x, x);
        status._base = base;
        status._drum._outStyle = MGStatusForDrum.STYLE_LINK_SLIDER;
        status._drum._linkKontrolType = MGStatus.TYPE_SLIDER;
        status._drum._linkMode = MGStatusForDrum.LINKMODE_MIN;
        status._drum._linkRow = 0;
        status._drum._linkColumn = -1;
        status._drum._strikeZone = hit;
        status._drum._mouseOnValue = x;
        status._drum._outValueTypeOn = MGStatusForDrum.VALUETYPE_AS_INPUT;
        status._drum._outValueTypeOff = MGStatusForDrum.VALUETYPE_NOTHING;
    }

    public void initDrumMinMidleMax() {
        ArrayList<MGStatus>[] sliderMatrix = _mixer._matrixSliderStatus;
        ArrayList<MGStatus>[] padMatrix = new ArrayList[MXConfiguration.DRUM_ROW_COUNT];

        padMatrix[0] = new ArrayList();
        padMatrix[1] = new ArrayList();
        padMatrix[2] = new ArrayList();

        int column = 0;
        while (padMatrix[0].size() < MXConfiguration.SLIDER_COLUMN_COUNT) {
            MGStatus status;
            status = new MGStatus(_mixer,  MGStatus.TYPE_DRUMPAD, 0, column);
            fillMaxOfSlider(status, column);
            padMatrix[0].add(status);

            status = new MGStatus(_mixer,  MGStatus.TYPE_DRUMPAD, 1, column);
            fillMiddleOfSlider(status, column);
            padMatrix[1].add(status);

            status = new MGStatus(_mixer,  MGStatus.TYPE_DRUMPAD, 2, column);
            fillMinOfSlider(status, column);
            padMatrix[2].add(status);
            column++;
        }
        _mixer._matrixDrumStatus = padMatrix;
    }
}   
