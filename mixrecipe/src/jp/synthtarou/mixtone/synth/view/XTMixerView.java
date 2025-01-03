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
package jp.synthtarou.mixtone.synth.view;

import jp.synthtarou.libs.MXQueue;
import jp.synthtarou.libs.smf.OneMessage;
import jp.synthtarou.mixtone.synth.XTSynthesizer;
import jp.synthtarou.mixtone.synth.XTSynthesizerTrack;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTMixerView extends javax.swing.JPanel {
    XTSynthesizer _synth;
    XTMixerLineHeader _header;
    XTMixerLine[] _lines;
    XTSynthesizerTrack[] _track;

    public XTMixerView(XTSynthesizer synth) {
        initComponents();
        removeAll();
        _synth = synth;
        _header = new XTMixerLineHeader(this);
        add(_header);
        _lines = new XTMixerLine[16];
        for (int i = 0; i < 16; ++ i) {
            XTMixerLine line = new XTMixerLine();
            line.setChannel(i);
            _lines[i] = line;
            add(line);
        }
        _track = new XTSynthesizerTrack[16];
        for (int i = 0; i < 16; ++ i) {
            _track[i] = new XTSynthesizerTrack(synth, i);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
    }// </editor-fold>//GEN-END:initComponents

    Thread _processer = null;
    MXQueue<OneMessage> _pool = new MXQueue<>();

    public void dispatchNote(OneMessage smf) {
        int status =  smf.getStatus();
        int data1 = smf.getData1();
        int data2 = smf.getData2();
        if (status >= 0x80 && status <= 0xef) {
            int ch = status & 0x0f;
            _lines[ch].push(smf);
        }
        if (false) {
            _synth.processMessage(smf);
        }
        else{
            if (_processer == null) {
                _processer = new Thread() {
                    public void run() {
                        try {
                            while (true) {
                                OneMessage message = _pool.pop();
                                _synth.processMessage(message);
                            }
                        }catch(Throwable ex) {
                            ex.printStackTrace();
                        }
                        _processer = null;
                    }
                };
                _processer.setPriority(Thread.MAX_PRIORITY);
                _processer.start();
            }
            _pool.push(smf);
        }
    }
    
    public void cleanNote() {
        for (int i = 0; i < 16; ++ i) {
            _lines[i].clear();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
