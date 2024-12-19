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

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTSynthesizerSetting {

    public int getSamplePageSize() {
        return _samplePageSize;
    }

    public void setSamplePageSize(int samplePageSize) {
        if (_samplePageSize != samplePageSize) {
            _samplePageSize = samplePageSize;
            _isUpdated = true;
        }
    }

    public int getSamplePageCount() {
        return _samplePageCount;
    }

    public void setSamplePageCount(int samplePageCount) {
        if (_samplePageCount != samplePageCount) {
            _samplePageCount = samplePageCount;
            _isUpdated = true;
        }
    }

    public XTSynthesizerSetting() {
        try {
            /*
            Clip clip = AudioSystem.getClip();
            AudioFormat format0 = clip.getFormat();
            System.err.println("format0 " + format0);
            _sampleRate = format0.getFrameRate();
            _sampleChannels = 2; //format0.getChannels();
            _sampleBits = 16;//format0.getSampleSizeInBits();
            _samplePageCount = 3;
            _samplePageSize = 256;
            */
        }catch(Throwable ex) {
            ex.printStackTrace();
        }

    }
    public void setCustomSFZ(String file) {
        if (_customSFZ == null) {
            if (file == null) {
                return;
            }
            _customSFZ = file;
            _isUpdated = true;
        }
        if (_customSFZ.equals(file) == false) {
            _customSFZ = file;
            _isUpdated = true;
        }
    }

    public String getCustomSFZ() {
        return _customSFZ;
    }
    
    String _customSFZ;

    public int getPolyPhony() {
        return _polyPhony;
    }

    public void setPolyPhony(int polyPhony) {
        _polyPhony = polyPhony;
    }

    public int getSampleChannel() {
        return _sampleChannels;
    }

    public void setSampleChannels(int sampleChannel) {
        if (_sampleChannels != sampleChannel) {
            _sampleChannels = sampleChannel;
            _isUpdated = true;
        }
    }

    public float getSampleRate() {
        return _sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        if (_sampleRate != sampleRate) {
            _sampleRate = sampleRate;
            _isUpdated = true;
        }
    }

    public int getSampleBits() {
        return _sampleBits;
    }

    public void setSampleBits(int sampleBits) {
        if (_sampleBits != sampleBits) {
            _sampleBits = sampleBits;
            _isUpdated = true;
        }
    }

    static XTSynthesizerSetting _setting = new XTSynthesizerSetting();
    
    public static XTSynthesizerSetting getSetting() {
        return _setting;
    }
    
    private int _polyPhony = 100;
    private int _samplePageSize = 512;
    private int _samplePageCount = 4;
    private int _sampleChannels = 2;

    private float _sampleRate = 48000;
    private int _sampleBits = 16;

    private boolean _isUpdated = false;
    
    public boolean isUpdated() {
        return _isUpdated;
    }

    public void clearUpdated() {
        _isUpdated = false;
    }

    XTSynthesizer _synth = null;
    
    public synchronized XTSynthesizer getSynthInstance() {
        if (_synth == null) {
            _synth = new XTSynthesizer();
        }
        return _synth;
    }
}
