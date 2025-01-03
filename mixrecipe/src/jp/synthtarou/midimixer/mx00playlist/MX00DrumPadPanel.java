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
import java.util.ArrayList;
import javax.swing.JToggleButton;
import jp.synthtarou.libs.MXCountdownTimer;
import jp.synthtarou.midimixer.ccxml.xml.CXGeneralMidiFile;
import jp.synthtarou.midimixer.libs.midi.MXMidiStatic;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MX00DrumPadPanel extends javax.swing.JPanel {
    JToggleButton[] listDrums = new JToggleButton[256];
    ArrayList<ArrayList<JToggleButton>> layout;

    public MX00DrumPadPanel() {
        initComponents();
    }
    
    public void buttonLayout(JToggleButton btn) {
        if (layout == null) {
            layout = new ArrayList<ArrayList<JToggleButton>>();
            layout.add(null);
        }

        do {
            for (int y = 0; y < layout.size(); ++ y) {
                ArrayList<JToggleButton> row = layout.get(y);
                if (row == null) {
                    row = new ArrayList<JToggleButton>();
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    row.add(null);
                    layout.set(y, row);
                }
                for (int x = 0; x < row.size(); ++ x ) {
                    JToggleButton cell = row.get(x);

                    if (cell == null) {
                        row.set(x, btn);
                        /*
                        GridBagConstraints c = new GridBagConstraints();
                        c.weightx = 1;
                        c.weighty = 1;
                        c.fill = GridBagConstraints.BOTH;
                        c.gridx = x;
                        c.gridy = y;
                        add(btn, c);
                        */
                        add(btn);

                        return;
                    }
                }
            }
            layout.add(null);
        }while(true);
    }
    
    protected void addNote(int note) {
        JToggleButton prev = listDrums[note];
        if (prev == null) {
            String drumNote = CXGeneralMidiFile.getInstance().simpleFindDrumName(note);
            String noteName = MXMidiStatic.nameOfNote(note);
            if (drumNote != null) {
                noteName = drumNote;
            }
            noteName = "(" + note + ")" +  noteName;
            JToggleButton button = new JToggleButton(noteName);
            button.setPreferredSize(new Dimension(150, 40));
            listDrums[note] = button;
            buttonLayout(listDrums[note]);
        }
    }
    
    long timer;
    
    public void setSelected(int note, boolean push) {
        JToggleButton btn = listDrums[note];
        if (btn == null) {
            return;
        }

        if (push) {
            btn.setSelected(true);
            timer = System.currentTimeMillis();
        }else {
            if (timer == 0) {
                btn.setSelected(false);
            }
            MXCountdownTimer.letsCountdown(100, () -> {
                btn.setSelected(false);
            });
        }
    }
    public void noteOn(int note) {
        setSelected(note, true);
    }
    
/*
    int colorIndex;
    Color[] colors = new Color[] {
        Color.white,
        Color.red,
        Color.yellow,
        Color.pink,
        Color.cyan
    };
    
  */
    public void noteOff(int note) {
        setSelected(note, false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
