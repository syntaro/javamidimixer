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
package jp.synthtarou.midimixer.mx00playlist;

import java.awt.Dimension;
import jp.synthtarou.midimixer.libs.swing.MXSwingPiano;
import javax.swing.BorderFactory;
import jp.synthtarou.midimixer.mx35cceditor.ccxml.CXGeneralMidiFile;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX00PianoPanel extends javax.swing.JPanel {
    MXSwingPiano _keys;
    int _channel;

    public MX00PianoPanel(MXSwingPiano keys) {
        initComponents();
        _keys = keys;
        add(_keys);
    }
    
    public void autoAdjustHeight(int width) {
        this._keys.paintOnBuffer(null);
        int newHeight = _keys.getAdjustedHeight(width);
        Dimension d = new Dimension(width, newHeight);
        setPreferredSize(d);
    }
    
    public MXSwingPiano getKeys() {
        return _keys;
    }
    
    public void setChannel(int ch) {
        _channel = ch;
    }

    public int getChannel() {
        return _channel;
    }

    public void updateProgramNumber(int program) {
        String name = CXGeneralMidiFile.getInstance().simpleFindProgram(program);
        setBorder(BorderFactory.createTitledBorder("TR "  + (_channel + 1) + " " + name + "(" + program + ")"));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createTitledBorder("abc"));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
