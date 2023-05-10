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
package jp.synthtarou.midimixer.libs.midi.smf;

// https://qiita.com/takayoshi1968/items/8e3f901539c92a6aac16

import java.util.ArrayList;

public final class SMFTempoList {
    ArrayList<SMFTempo> _listTempos;
    
    // 分解能(四分音符ひとつは何 Tick であるか)
    int _fileResolution;
    public void setFileResolution(int reso) {
        _fileResolution = reso;
    }
    
    public int getFileResolution() {
        return _fileResolution;
    }
    
    // 再生に当たって、NAudio.Midi.MidiEventCollection は実質的に Midi ファイルとして見なせる
    public SMFTempoList(ArrayList<SMFMessage> listMessage, int fileResolution) {
        // Pulses Per Quater note
        int resolution = fileResolution;
        
        ArrayList<SMFMessage> tempoEvents = new ArrayList();

        if (listMessage != null) {

            for (SMFMessage message : listMessage) {
                if (message._status == 0xff) {
                    switch(message._dataType) {
                        case 0: // シーケンス番号
                        case 1: // テキストイベント
                        case 2: // 著作権情報
                        case 3: // シーケンス名・トラック名
                        case 4: // 楽器名
                        case 5: // 歌詞
                        case 6: // マーカー
                        case 7: // キューポイント
                        case 0x20: //MIDIチャンネルプリフィクス
                        case 0x2f: //END
                        case 0x54: //SNMPTE OFFSEt
                        case 0x58: //メトロノーム
                        case 0x59: //調合
                        case 0x75: //シーケンサメタ
                            break;
                        case 0x51: //Tempo
                            tempoEvents.add(message);
                    }
                }
            }

        }
        if (tempoEvents.isEmpty() || (tempoEvents.get(0)._tick != 0)) {
            SMFMessage mm ;
            if (tempoEvents.isEmpty()) {
                byte b1 = (byte)(SMFTempo.DEFAULT_MPQ >> 16);
                byte b2 = (byte)(SMFTempo.DEFAULT_MPQ >> 8);
                byte b3 = (byte)(SMFTempo.DEFAULT_MPQ );
                mm = new SMFMessage(0, 0xff, 0x51, new byte[] { b1, b2, b3 });
            }else {
                mm = tempoEvents.get(0);
            }
            // 先頭にテンポ指定がない場合はデフォルト値を入れる
            tempoEvents.add(0, mm);
        }

        _listTempos = new ArrayList<>();
        
        // 0 Tick 時点での値を先に入れる
        SMFTempo e = new SMFTempo();
        e.mpqStack = tempoEvents.get(0).getMetaTempo();
        e.cumulativeTicks = 0L;
        e.cumulativeMicroseconds = 0L;
        _listTempos.add(e);

        for (SMFMessage event : tempoEvents) {
            SMFTempo prev = e;
            e = new SMFTempo();
            long tick = event._tick;
            e.cumulativeTicks = tick;
            // deltaTick = 前回の Set Tempo からの時間 (Ticks)
            long deltaTick = tick - prev.cumulativeTicks;
            e.mpqStack = event.getMetaTempo();
            // deltaMicroseconds = 前回の Set Tempo からの時間 (us)
            // <= MPQ = mpqStack[pos - 1] で deltaTick だけ経過している
            long deltaMicroseconds = TicksToMicroseconds(deltaTick, prev.mpqStack, resolution);
            e.cumulativeMicroseconds = prev.cumulativeMicroseconds + deltaMicroseconds;
            _listTempos.add(e);
        }
        
        setFileResolution(resolution);
    }// Constructor

    public void addTempo(long time, long tick, long mpq) {
        SMFTempo t = new SMFTempo();
        t.cumulativeMicroseconds = time;
        t.cumulativeTicks = tick;
        t.mpqStack = mpq;
        _listTempos.add(t);
    }

    public long MicrosecondsToTicks(long us) {
        // 曲の開始から us[マイクロ秒] 経過した時点は、
        // 曲の開始から 何Ticks 経過した時点であるかを計算する
        int index = GetIndexFromMicroseconds(us);
        SMFTempo e = _listTempos.get(index);

        // 現在の MPQ は mpq である
        long mpq = e.mpqStack;
        //System.out.println(" last mpq " + mpq + " resolution " + _fileResolution);

        // 直前のテンポ変更があったのは cumUs(マイクロ秒) 経過した時点であった
        long cumUs = e.cumulativeMicroseconds;
        // 直前のテンポ変更があったのは cumTicks(Ticks) 経過した時点であった
        long cumTicks = e.cumulativeTicks;

        // 直前のテンポ変更から deltaUs(マイクロ秒)が経過している
        long deltaUs = us - cumUs;
        // 直前のテンポ変更から deltaTicks(Ticks)が経過している
        long deltaTicks = MicrosecondsToTicks(deltaUs, mpq, _fileResolution);

        return cumTicks + deltaTicks;
    }
    
    public long TicksToMicroseconds(long tick){ 
        int index = GetIndexFromTick(tick);
        SMFTempo e = _listTempos.get(index);

        long mpq = e.mpqStack;

        long cumUs = e.cumulativeMicroseconds;
        long cumTicks = e.cumulativeTicks;

        long deltaTick = tick - cumTicks;
        long deltaUs = TicksToMicroseconds(deltaTick, mpq, getFileResolution());
        
        return cumUs + deltaUs;
    }

    private int GetIndexFromMicroseconds(long us) {
        // 指定された時間(マイクロ秒)時点におけるインデックスを二分探索で探す
        int lo = -1;
        int hi = _listTempos.size();
        while ((hi - lo) > 1) {
            int m = hi - (hi - lo) / 2;
            SMFTempo e = _listTempos.get(m);
            if (e.cumulativeMicroseconds <= us) lo = m;
            else hi = m;
        }
        return lo;
    }

    private int GetIndexFromTick(long tick) {
        // 指定された時間(マイクロ秒)時点におけるインデックスを二分探索で探す
        int lo = -1;
        int hi = _listTempos.size();
        while ((hi - lo) > 1) {
            int m = hi - (hi - lo) / 2;
            SMFTempo e = _listTempos.get(m);
            if (e.cumulativeTicks <= tick) lo = m;
            else hi = m;
        }
        return lo;
    }

    private static long MicrosecondsToTicks(long us, long mpq, int resolution) {
        return us * resolution / mpq;
    }

    private static long TicksToMicroseconds(long tick, long mqp, int resolution) {
        // 時間(Tick)を時間(マイクロ秒)に変換する
        return tick * mqp / resolution;
    }
    
}// class TempoData
