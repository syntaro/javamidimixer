/*
 * https://github.com/DerekCook/CoreMidi4J/issues/37
 */

package jp.synthtarou.midimixer.libs.midi.driver;

import javax.sound.midi.MidiMessage;

public class DividedSysex extends MidiMessage
{
    public byte[] getData() { return data; }
    public Object clone() { return new DividedSysex(getMessage()); }
    public DividedSysex(byte[] data) { super(data.clone()); }
    public int getStatus() { return 0xF0; } // not that this really matters 
}