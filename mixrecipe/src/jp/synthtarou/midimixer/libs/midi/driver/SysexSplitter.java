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
import java.util.ArrayList;
import jp.synthtarou.midimixer.libs.midi.smf.SMFInputStream;

/**
 *　SysEXメッセージを分割、結合するため
 * @see jp.synthtarou.midimixer.libs.midi.driver.SplittableSysexMessage
 * @author Syntarou YOSHIDA
 */
public class SysexSplitter {

    /**
     * コンストラクタ
     */
    public SysexSplitter() {
        
    }

    /**
     * バイト配列を格納する
     */
    ByteArrayOutputStream _dataBody = new ByteArrayOutputStream();
    
    /**
     * SysEXデータを連結する
     * @param sysexData 連結するバイト配列
     *  F0かF7で始まる必要がある、終端文字F7の”手前"までを格納する
     *  F0でもF7でもない始まりの場合、そこまでスキップされる、エラーにはしない
     */
    public void append(byte[] sysexData) {
        SMFInputStream reader = new SMFInputStream(sysexData);

        int status = reader.read8();
        
        while (status != 0xf0 && status != 0xf7 && status >= 0) {
            status = reader.read8();
        }
        
        _endingCode = false;
        if (status == 0xf0 || status == 0xf7) {            
            _beginningStatusCode = status;
            while (status >= 0) {
                status = reader.read8();
                if (status < 0) {
                    break;
                }
                
                if (status == 0xf7) {
                    _endingCode = true;
                    break;
                }
                _dataBody.write(status);
            }
        }
    }
    
    /**
     * SyeEXデータが格納されていないかテスト
     * @return 格納されていなければture
     */
    public boolean ieEmpty() {
        return _beginningStatusCode != 0xf0 && _beginningStatusCode != 0xf7;
    }
    
    /**
     * byte配列に分割する
     * @param maxLength パケットの最大長さ、極端に短い(10未満)場合、分割しない
     * @return 分割されてbyte配列。SplittableSysexMessgeを作ると送信可能
     */
    public ArrayList<byte[]> splitOrJoin(int maxLength) {
        if (maxLength < 10) {
            maxLength = 10000000;
        }

        ArrayList<byte[]> listResult = new ArrayList<>();

        byte[] data = _dataBody.toByteArray();
        ByteArrayOutputStream segment = new ByteArrayOutputStream();

        for (int i = 0; i < data.length; ++ i) {
            int ch = data[i] & 0xff;
            
            if (maxLength > 0 && segment.size() >= maxLength) {
                byte[] result = segment.toByteArray();
                if (result.length > 1) {
                    segment.write(ch);
                    listResult.add(result);
                }
                segment = new ByteArrayOutputStream();
            }
            else {
                /* なんか数字がきたらはじめる */
                if (segment.size() == 0) {
                    if (listResult.isEmpty()) {
                        /* 最初はF0はじまり */
                        segment.write(_beginningStatusCode);
                    }
                    else {                        
                        segment.write(0xf7);
                    }
                }
                segment.write(ch);
            }
        }
        
        if (segment.size() >= 1) {
            /* 最期は終端 */
            if (_endingCode) {
                segment.write(0xf7);
            }
            byte[] result = segment.toByteArray();
            listResult.add(result);
        }

        return listResult;
    }

    /**
     * スタータスコード
     */
    int _beginningStatusCode;
    /**
     * 終端F7に出会っているかどうか。その場合splitOrJoinはにF7を追加して出力する
     */
    boolean _endingCode;
}
