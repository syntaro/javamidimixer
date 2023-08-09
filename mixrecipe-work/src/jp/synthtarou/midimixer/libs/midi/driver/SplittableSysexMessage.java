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
package jp.synthtarou.midimixer.libs.midi.driver;

import java.io.ByteArrayOutputStream;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class SplittableSysexMessage extends MidiMessage {
    public SplittableSysexMessage(byte[] data) throws InvalidMidiDataException {
        super(new byte[2]);
        setMessage(data, data.length);
    }
    
    protected void setMessage(byte[] data, int dataLength) throws InvalidMidiDataException {
        _status = data[0] & 0xff;
        int last = data[data.length - 1] & 0xff;
                
        if (_status == 0xf0 || _status == 0xf7) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            if (_status == 0xf0 && last == 0xf7) {
            }else if (_status == 0xf0) {
            }else if (true) {
                _offset = 1;
            }            
            out.write(data,  _offset, dataLength - _offset);
            byte[] trans = out.toByteArray();
            _length = trans.length;
            setMessagePlain(trans, trans.length);
        }
        else {
            throw new InvalidMidiDataException("data not start f0 or f7");
        }
    }
    
    protected void setMessagePlain(byte[] data, int dataLength) throws InvalidMidiDataException {
        super.setMessage(data, dataLength);
    }
    
    public Object clone() {
        try {
            byte[] plain = getMessage();
            SplittableSysexMessage inst = new SplittableSysexMessage(new byte[2]);
            inst._status = _status;
            inst._length = _length;
            inst.setMessagePlain(plain, plain.length);
            System.out.println("@clone");
            return inst;
        }
        catch(InvalidMidiDataException midiEx) {
            midiEx.printStackTrace();
            //can't happens
            return null;
        }
    }
    
    @Override
    public int getStatus() {
        return _status;
    }
    
    @Override
    public int getLength() {
        return _length + _offset;
    }
    
    @Override
    public byte[] getMessage() {
        byte[] raw = super.getMessage();
        
        /*int first = raw[0] & 0xff;
        if (first != 0xf0 && first != 0xf7) {
            byte[] data = new byte[raw.length + 1];
            data[0] = (byte)_status;
            System.arraycopy(raw, 0, data, 1, raw.length);
            return data;
        }
        else */{
            return raw;
        }
    }

    int _offset;
    int _status;
    int _length;
}
