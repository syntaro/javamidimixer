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
import java.util.logging.Level;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import jp.synthtarou.midimixer.libs.common.MXLogger2;

/**
 *　Java20以上で、SysEXを処理するJava標準MidiMessageのラッパー
 * F0で始まる場合、getStatus()をF0として、バイト配列はすべてを返します
 * F7で始まる場合、getStauts()をF7として、バイト配列は先頭のF7を含みません
 * このようにすると、JavaAPIは正しく処理します。
 * ただし、Java20以上が必要ですJDK20未満ではアプリがクラッシュしていました。
 * Windows10のみ動作確認すみ。
 * @author Syntarou YOSHIDA
 * @see jp.synthtarou.midimixer.libs.midi.driver.SysexSplitter
 */
public class SplittableSysexMessage extends MidiMessage {

    /**
     * @param data
     * @throws InvalidMidiDataException
     */
    public SplittableSysexMessage(byte[] data) throws InvalidMidiDataException {
        super(new byte[2]);
        setMessage(data, data.length);
    }
    
    /**
     * メッセージをこのクラスとして加工して設定します。
     * @param data F0またはF7ではじまるバイト配列、最終バイトはF7である必要はない
     * @param dataLength バイト長 (先頭バイトをふくむ)
     * @throws InvalidMidiDataException
     */
    @Override
    protected void setMessage(byte[] data, int dataLength) throws InvalidMidiDataException {
        _status = data[0] & 0xff;
        int last = data[data.length - 1] & 0xff;
                
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if (_status == 0xf0 && last == 0xf7) {
            _status = 0xf0;
            _offset = 0;
        }else if (_status == 0xf0) {
            _status = 0xf0;
            _offset = 0;
        }else if (_status == 0xf7) { 
            //終端文字は問わない
            _status = 0xf7;
            _offset = 1;
        } else {
            throw new InvalidMidiDataException("data not start f0 or f7");
        }
        out.write(data,  _offset, dataLength - _offset);
        byte[] trans = out.toByteArray();
        _length = trans.length;
        setMessagePlain(trans, trans.length);
    }
    
    /**
     * メッセージをこのクラスなりの加工をせず、スーパークラス形式で設定します。
     * @param data バイト配列
     * @param dataLength バイト長
     * @throws InvalidMidiDataException
     */
    protected void setMessagePlain(byte[] data, int dataLength) throws InvalidMidiDataException {
        super.setMessage(data, dataLength);
    }
    
    /**
     *　SplittableSysexMessageを複製します
     * @return 複製されたObject
     */
    public Object clone() {
        try {
            byte[] plain = getMessage();
            SplittableSysexMessage inst = new SplittableSysexMessage(new byte[2]);
            inst._status = _status;
            inst._length = _length;
            inst.setMessagePlain(plain, plain.length);
            return inst;
        }
        catch(InvalidMidiDataException ex) {
            MXLogger2.getLogger(SplittableSysexMessage.class).log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }
    }
    
    /**
     * Receiverが受け付けるスタータスコード
     * @return F0またはF7
     * setMessageおよびコンストラクタで設定したステータス
     */
    @Override
    public int getStatus() {
        return _status;
    }
    
    /**
     * Receiverが受け付けるデータ長さ
     * @return getMessageで返されるバイト配列の長さ
     */
    @Override
    public int getLength() {
        return _length /* + _offset */;
    }
    
    /**
     * Receiverが受け付けるデータ
     * @return 送信するバイト配列、F0やF7で始まるかは問わない
     */
    @Override
    public byte[] getMessage() {
        byte[] raw = super.getMessage();
        if (raw.length != getLength()) {
            throw new IllegalArgumentException("raw.length " + raw.length + " != data.length " + getLength());
        }
        if (raw.length > 0 && (raw[0] & 0xff) == 0xf7) {
            Exception ex = new Exception("Something Wrong");
            MXLogger2.getLogger(SplittableSysexMessage.class).log(Level.WARNING, ex.getMessage(), ex);
        }
        return raw;
    }

    int _offset;
    int _status;
    int _length;
}
