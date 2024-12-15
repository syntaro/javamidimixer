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
package jp.synthtarou.mixtone.main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.midimixer.libs.midi.MXMidi;
import jp.synthtarou.mixtone.listmodel.IBagLineMean;
import jp.synthtarou.mixtone.listmodel.PBagLineMean;
import jp.synthtarou.mixtone.synth.oscilator.XTOscilator;
import jp.synthtarou.mixtone.synth.soundfont.SFZElement;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperator;
import jp.synthtarou.mixtone.synth.soundfont.XTGenOperatorMaster;
import jp.synthtarou.mixtone.synth.soundfont.table.XTHeader;
import jp.synthtarou.mixtone.synth.soundfont.table.XTRow;
import jp.synthtarou.mixtone.synth.soundfont.table.XTTable;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTSynthesizerTrack {
    int _track;
    int _program;
    int _bank;
    SFZElement.SFZElement_phdr _phdr;
    SFZElement.SFZElement_inst _inst;
    XTRow _phdr_row;
    XTSynthesizer _synth;
    
    public XTSynthesizerTrack(XTSynthesizer synth, int track) {
        _synth = synth;
        _track = track;
    }            
    
    public List<XTOscilator> createOscilator(int key, int velocity) {
        if (_synth._sfz == null) {
            return null;
        }

        XTTable table = _phdr_row.tableColumn(SFZElement.PHDR_BAGINDEX_TABLE);
        ArrayList<XTOscilator> result = new ArrayList<>();
        for (int row = 0; row <table.size(); ++ row) {
            PBagLineMean pmean = new PBagLineMean(table.get(row));
            if (pmean.keyRange() != null && pmean.keyRange() != 0) {
                if (pmean.keyRangeLo() <= key && key <= pmean.keyRangeHi()) {
                }else {
                    continue;
                }
            }
            if (pmean.velRange() != null && pmean.velRange() != 0) {
                if (pmean.velRangeLo() <= velocity && velocity <= pmean.velRangeHi()) {
                }else {
                    continue;
                }
            }
            Integer instrument = pmean.instrument();
            if (instrument == null) {
                continue;
            }
            XTRow instrumentRow = _inst.get(instrument);
            if (instrumentRow == null) {
                continue;
            }
            XTTable instBag = instrumentRow.tableColumn(SFZElement.INST_BAGINDEX_TABLE);
            IBagLineMean root = null;
            for (int inst = 0; inst < instBag.size() ; ++ inst) {
                IBagLineMean imean = new IBagLineMean(instBag.get(inst));
                if (root == null) {
                    root = imean;
                }
                if (imean.keyRange() != null && imean.keyRange() != 0) {
                    if (imean.keyRangeLo() <= key && key <= imean.keyRangeHi()) {
                    }else {
                        continue;
                    }
                }
                if (imean.velRange() != null && imean.velRange() != 0) {
                    if (imean.velRangeLo() <= velocity && velocity <= imean.velRangeHi()) {
                    }else {
                        continue;
                    }
                }
                try {
                    Integer sampleId = imean.sampleID();
                    if (sampleId == null) {
                        sampleId = root.sampleID();
                    }
                    if (sampleId == null) {
                        continue;
                    }
                    Integer overridingRootKey = imean.overridingRootKey();
                    boolean loop = imean.sampleModes();

                    XTOscilator osc = new XTOscilator(key, _synth._sfz, sampleId, loop, overridingRootKey == null ? -1 : overridingRootKey);
                    result.add(osc);
                }catch(Throwable ex) {
                    System.err.println("inst "  + instBag.get(inst));
                    System.err.println("imean "  + imean);
                    ex.printStackTrace();
                }
            }
        }
        return result;
    }
    
    public void setupPHDRObject(int program, int bank) {
        _program = program;
        _bank = bank;
        _phdr = (SFZElement.SFZElement_phdr)_synth._sfz.getElement("phdr");
        _inst = (SFZElement.SFZElement_inst)_synth._sfz.getElement("inst");
        XTHeader header = _phdr.getHeader();
        XTRow found = null;
        
        for (int i = 0; i < _phdr.size() -1; ++ i) {
            XTRow row = _phdr.get(i);
            Number numPreset = row.numberColumn(SFZElement.PHDR_PRESETNO, null);
            Number numBank = row.numberColumn(SFZElement.PHDR_BANK, null);
            if (found == null) {
                if (numPreset != null && numPreset.intValue() == program) {
                    found = row;
                }
            }
            else {
                if (numPreset != null && numPreset.intValue() == program) {
                    if (_track == 9) {
                        if (numBank != null && numBank.intValue() >= 120) {
                            found = row;
                        }
                    }
                    else {
                        if (numBank != null && numBank.intValue() == bank) {
                            found = row;
                        }
                    }
                }
            }
        }
        if (found != null) {
            _phdr_row = found;
            XTTable bag = _phdr_row.tableColumn(SFZElement.PHDR_BAGINDEX_TABLE);
            if (bag == null) {
                System.err.println("bag = null +" + _phdr_row);
                new Throwable().printStackTrace();
            }
        }
    }

    int _note;
    int _velocity;
    static XTGenOperatorMaster _opratorMaster = new XTGenOperatorMaster();
    
    class BindValue {
        XTGenOperator _oper; 
        Integer _generater = null;
        
        BindValue(XTGenOperator oper) {
            _oper = oper;
        }

        BindValue(int oper) {
            _oper = _opratorMaster.get(oper);
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
    
    List<XTOscilator> _listOscilator = new LinkedList<>();
    ArrayList<XTOscilator> _listRemove = new ArrayList<>();
   
    public void processMesssage(OneMessage message) {
        int status = message.getStatus();
        int data1 = message.getData1();
        int data2 = message.getData2();
        if (status >= 0x80 && status <= 0xef) {
            int ch = status & 0x0f;
            status = status & 0xf0;
            if (status == MXMidi.COMMAND_CH_NOTEON) {
                if (data2 == 0) {
                    status = MXMidi.COMMAND_CH_NOTEOFF;
                }
            }
            
            switch (status) {
                case MXMidi.COMMAND_CH_NOTEON:
                    List<XTOscilator> list = createOscilator(data1, data2);
                    for (XTOscilator seek : list) {
                        _listOscilator.add(seek);
                        _synth._audioStream.addToQueue(seek);
                    }
                    break;

                case MXMidi.COMMAND_CH_NOTEOFF:
                    _listRemove.clear();
                    for (XTOscilator seek : _listOscilator) {
                        if (seek._playKey == data1) {
                            seek.noteOff();
                        }
                    }

                    break;

                case MXMidi.COMMAND_CH_PROGRAMCHANGE:
                    if (_track == 9) {
                        setupPHDRObject(data1, 128);
                        setupPHDRObject(data1, 127);
                    }
                    else {
                        setupPHDRObject(data1, 0);
                    }
                    break;
                    
                case MXMidi.COMMAND_CH_CONTROLCHANGE:
                    if (data1 == MXMidi.DATA1_CC_ALLNOTEOFF
                        || data1 == MXMidi.DATA1_CC_ALLSOUNDOFF) {
                        allNoteOff();
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
        for (XTOscilator seek : _listOscilator) {
            seek.noteOff();
        }
    }
}
