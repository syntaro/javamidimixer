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
package jp.synthtarou.mixtone.synth;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;
import jp.synthtarou.mixtone.synth.oscilator.XTOscilator;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMasterEntry;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMaster;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.IGENEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.IBAGEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.PGENEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.PBAGEntry;
import jp.synthtarou.mixtone.synth.soundfont.table.XTHeader;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.INSTEntry;
import jp.synthtarou.mixtone.synth.soundfont.wrapper.PHDREntry;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTSynthesizerTrack {
    static String TAG = "XTSynthesizerTrack";

    int _track;
    int _program;
    int _bank;
    int _phdr_row;
    XTSynthesizer _synth;
    double _pan;
    double _volume;
    double _expression;
    double _mixing;
    boolean _damper;
    ArrayList<XTOscilator> _markNoteOff;
    static XTGenOperatorMaster _genMaster = null;
    
    public XTSynthesizerTrack(XTSynthesizer synth, int track) {
        _synth = synth;
        _track = track;
        _pan = 0.0;
        _volume = 1.0;
        _expression = 1.0;
        _mixing = 1.0;
        _damper = false;
        _markNoteOff = null;
        if (_genMaster == null) {
            _genMaster = XTGenOperatorMaster.getMaster();
        }
    }

    public List<XTOscilator> createOscilator(int key, int velocity) {
        if (_synth._sfz == null) {
            return null;
        }

        for (int i = 0; i < _listForMessage.size(); ++ i) {
            XTOscilator seek = _listForMessage.get(i);
            if (seek.isFaded()) {
                _listForMessage.remove(i);
                i --;
            }
        }

        PHDREntry phdrEntry = new PHDREntry(_synth._sfz, _phdr_row);
        ArrayList<XTOscilator> result = new ArrayList<>();

        for (int row = 0; row < phdrEntry.countPBAGEntry(); ++ row) {
            PBAGEntry pbag = phdrEntry.getPBAGEntry(row);
            PGENEntry pgen = pbag.getPGENEntry();
            if (pgen.getGenerator(XTGenOperatorMaster.keyRange) != null && pgen.inKeyRange(key) == false) {
                continue;
            }
            if (pgen.getGenerator(XTGenOperatorMaster.velRange) != null && pgen.inVelocityRange(key) == false) {
                continue;
            }

            Integer instrument = pgen.getInstrument();
            if (instrument == null) {
                continue;
            }
            
            INSTEntry instEntry = new INSTEntry(_synth._sfz, instrument);
            IGENEntry root = null;

            for (int inst = 0; inst < instEntry.countIBAGEntry(); ++ inst) {
                IBAGEntry ibag = instEntry.getIBAGEntry(inst);
                IGENEntry igen = ibag.getIGENEntry();
                Integer sampleId = igen.getSampleId();
                if (root == null && sampleId == null) {
                    root = igen;
                    continue;
                }
                if (sampleId == null) {
                    continue;
                }
                if (igen.getGenerator(XTGenOperatorMaster.keyRange) != null) {
                    if (igen.inKeyRange(key) == false) {
                        continue;
                    }
                }else if (root != null && root.getGenerator(XTGenOperatorMaster.keyRange) != null) {
                    if (root.inKeyRange(key) == false) {
                        continue;
                    }
                }
                if (igen.getGenerator(XTGenOperatorMaster.velRange) != null) {
                    if (igen.inVelocityRange(velocity) == false) {
                        continue;
                    }
                }else if (root != null && root.getGenerator(XTGenOperatorMaster.velRange) != null) {
                    if (root.inVelocityRange(velocity) == false) {
                        continue;
                    }
                }
   
                Integer ipan = igen.getPan();
                if (ipan == null && root != null) {
                    ipan = root.getPan();
                }
                
                if (ipan == null) {
                    ipan = 0;
                }
                Double pan = _genMaster.getEntry(XTGenOperatorMaster.pan).asParameter(ipan);

                int min = -500;
                int max = 500;

                double pos = (pan.doubleValue() - min) / (max - min);
                double centerPos = (pos - 0.5) * 2;
                if (centerPos < -1) centerPos = -1;
                if (centerPos > 1) centerPos = 1;

                try {
                    Integer overridingRootKey = igen.getOverridingRootKey();
                    if (overridingRootKey == null && root != null) {
                        overridingRootKey = root.getOverridingRootKey();
                    }
                    Integer loop = igen.getSampleModes();
                    if (loop == null && root != null) {
                        loop = root.getSampleModes();
                    }

                    XTOscilator osc = new XTOscilator(
                            _track,
                            key, 
                            velocity * 1.0 / 127, 
                            _synth._sfz, 
                            sampleId, 
                            loop != null && loop.intValue() > 0,
                            overridingRootKey == null ? -1 : overridingRootKey,
                            centerPos);

                    osc.setPan(_pan);
                    osc.setVolume(_volume * _expression * _mixing);
                    result.add(osc);
                }catch(Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
        return result;
    }
    
    public void setupPHDRObject(int program, int bank) {
        _program = program;
        _bank = bank;
        SFZElement.SFZElement_phdr _phdr = _synth._sfz._phdr;
        XTHeader header = _phdr.getHeader();
        int found = -1;
        
        for (int i = 0; i < _phdr.size() -1; ++ i) {
            XTRow row = _phdr.get(i);
            Number numPreset = row.numberColumn(SFZElement.PHDR_PRESETNO, null);
            Number numBank = row.numberColumn(SFZElement.PHDR_BANK, null);
            if (found < 0) {
                if (numPreset != null && numPreset.intValue() == program) {
                    found = i;
                }
            }
            else {
                if (numPreset != null && numPreset.intValue() == program) {
                    if (_track == 9) {
                        if (numBank != null && numBank.intValue() >= 120) {
                            found = i;
                        }
                    }
                    else {
                        if (numBank != null && numBank.intValue() == bank) {
                            found = i;
                        }
                    }
                }
            }
        }
        _phdr_row = found;
    }
    
    class BindValue {
        XTGenOperatorMasterEntry _oper; 
        Integer _generater = null;
        
        BindValue(XTGenOperatorMasterEntry oper) {
            _oper = oper;
        }

        BindValue(int oper) {
            _oper = _genMaster.getEntry(oper);
        }
        
        public BindValue setGenerator(int x) {
            _generater = x;
            return this;
        }
        
        public Double getAsParameter() {
            if (_generater == null) {
                return _oper.getInitial();
            }
            return _oper.asParameter(_generater);
        }
    }

    List<XTOscilator> _listForMessage = new LinkedList<>();
    int noteTotal = 0;

    public void processMesssage(OneMessage message) {
        int status = message.getStatus();
        int data1 = message.getData1();
        int data2 = message.getData2();
        int polyPhony = XTSynthesizerSetting.getSetting().getPolyPhony();

        if (status >= 0x80 && status <= 0xef) {
            int ch = status & 0x0f;
            if (ch != _track) {
                throw new IllegalArgumentException();
            }
            status &= 0xf0;

            if (status == MXMidiStatic.COMMAND_CH_NOTEON) {
                if (data2 == 0) {
                    status = MXMidiStatic.COMMAND_CH_NOTEOFF;
                }
            }

            switch (status) {
                case MXMidiStatic.COMMAND_CH_NOTEON:
                    for (XTOscilator seek : _listForMessage) {
                        if (seek._playKey == data1) {
                            seek.noteOff();
                        }
                    }

                    while (true) {
                        int countOn = 0;
                        XTSynthesizerTrack maxOn = null;
                        XTSynthesizerTrack maxOff = null;

                        for (int t = 0; t < 16; ++ t) {
                            XTSynthesizerTrack  seek = _synth.getTrack(t);
                            int off = seek.countNoteOffed();
                            if (off > 0) {
                                if (maxOff == null || off >= maxOff.countNoteOffed()) {
                                    maxOff = seek;
                                }
                            }
                        }
                        for (int t = 0; t < 16; ++ t) {
                            XTSynthesizerTrack  seek = _synth.getTrack(t);
                            int all = seek.countNoteAll();
                            if (all > 0) {
                                countOn += all;
                                if (maxOn == null || all >= maxOn.countNoteAll()) {
                                    maxOn = seek;
                                }
                            }
                        }
                        if (countOn > polyPhony) {
                            if (maxOff != null) {
                                maxOff.removeOneOff();
                                continue;
                            }
                            if (maxOn != null) {
                                maxOn.removeOneLower();
                                continue;
                            }
                        }
                        break;
                    }

                    List<XTOscilator> list = createOscilator(data1, data2);
                    if (list != null) {
                        _listForMessage.addAll(list);
                        for (XTOscilator osc : list) {
                            if (osc._playKey != data1) {
                                throw  new IllegalStateException();
                            }
                            _synth._audioStream.push(osc);
                        }
                    }
                    break;

                case MXMidiStatic.COMMAND_CH_NOTEOFF:
                    if (_markNoteOff != null) {
                        for (XTOscilator seek : _listForMessage) {
                            if (seek._playKey == data1) {
                                _markNoteOff.add(seek);
                            }
                        }
                    }
                    else {
                        for (XTOscilator seek : _listForMessage) {
                            if (seek._playKey == data1) {
                                seek.noteOff();
                            }
                        }
                    }

                    break;

                case MXMidiStatic.COMMAND_CH_PROGRAMCHANGE:
                    if (_track == 9) {
                        setupPHDRObject(data1, 128);
                        setupPHDRObject(data1, 127);
                    }
                    else {
                        setupPHDRObject(data1, 0);
                    }
                    break;
                    
                case MXMidiStatic.COMMAND_CH_CONTROLCHANGE:
                    boolean modulated = false;
                    if (data1 == MXMidiStatic.DATA1_CC_ALLNOTEOFF
                        || data1 == MXMidiStatic.DATA1_CC_ALLSOUNDOFF) {
                        allNoteOff();
                    }
                    if (data1 == MXMidiStatic.DATA1_CC_CHANNEL_VOLUME) {
                        _volume = data2 * 1.0 / 127;
                        modulated = true;
                    }
                    if (data1 == MXMidiStatic.DATA1_CC_EXPRESSION) {
                        _expression = data2 * 1.0 / 127;
                        modulated = true;
                    }
                    if (data1 == MXMidiStatic.DATA1_CC_PANPOT) {
                        _pan = (data2 - 64) * 1.0 / 64;
                        modulated = true;
                    }
                    if (modulated) {
                        for (XTOscilator seek : _listForMessage) {
                            seek.setPan(_pan);
                            seek.setVolume(_volume * _expression * _mixing * seek._velocity);
                        }
                    }
                    if (data1 == MXMidiStatic.DATA1_CC_DAMPERPEDAL) {
                        _damper = (data2 >= 0x40);
                        if (_damper) {
                            _markNoteOff = new ArrayList<>();
                        }
                        else {
                            if (_markNoteOff != null) {
                                for (XTOscilator seek : _markNoteOff) {
                                    seek.noteOff();
                                }
                                _markNoteOff.clear();
                                _markNoteOff = null;
                            }
                        }
                    }
            }
        }
    }
    
    /*
        25 	delay           フィルタ 	フィルタ・ピッチ用エンベロープのディレイ(アタックが始まるまでの時間)
        26 	attackModEnv 	フィルタ 	フィルタ・ピッチ用エンベロープのアタック時間
        27 	holdModEnv 	フィルタ 	フィルタ・ピッチ用エンベロープのホールド時間
        (アタックが終わってからディケイが始まるまでの時間）
        28 	decayModEnv 	フィルタ 	フィルタ・ピッチ用エンベロープのディケイ時間
        29 	sustainModEnv 	フィルタ 	フィルタ・ピッチ用エンベロープのサステイン量
        30 	releaseModEnv 	フィルタ 	フィルタ・ピッチ用エンベロープのリリース時間
    */
    public void allNoteOff() {
        for (XTOscilator seek : _listForMessage) {
            seek.noteOff();
        }
    }
    public int countNoteAll() {
        int cnt = 0;
        for (XTOscilator seek : _listForMessage) {
            cnt ++;
        }
        return cnt;
    }

    public int countNoteOffed() {
        int cnt = 0;
        for (XTOscilator seek : _listForMessage) {
            if (seek.isNoteOff()) {
                cnt ++;
            }
        }
        return cnt;
    }

    public void removeOneLower() {
        XTOscilator ret = null;
        for (XTOscilator seek : _listForMessage) {
            if (ret == null || ret._playKey > seek._playKey) {
                ret = seek;
            }
        }
        ret.noteMute();
        _listForMessage.remove(ret);
    }
    public void removeOneOff() {
        XTOscilator ret = null;
        for (XTOscilator seek : _listForMessage) {
            if (seek.isNoteOff()) {
                _listForMessage.remove(seek);
                seek.noteMute();
                break;
            }
        }
    }
}
