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
package jp.synthtarou.midimixer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import jp.synthtarou.midimixer.libs.common.MXUtil;
import jp.synthtarou.midimixer.libs.swing.MXPianoKeys;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXProgressDialog extends javax.swing.JDialog {

    MXPianoKeys _piano;

    public void setMessageAsStartUP() {
        StringBuffer text = new StringBuffer();
        text.append("Compiled with OpenJDK 21 + NetBeans IDE19\n");
        text.append(("Copyright(C) SynthTAROU. GNU GPLv3\n"));
        text.append("\n");
        text.append(("Thank you for Trial.\n"));
        text.append(("I wish  You have Fun.\n"));
        text.append("\n\n<Java Information>");
        text.append("java.vendor=" + System.getProperty("java.vendor") + "\n");
        text.append("java.version=" + System.getProperty("java.version") + "\n\n");
        jTextArea1.setText(text.toString());
        jTextArea1.setCaretPosition(0);
        jTextArea1.setEditable(false);
    }

    public void setMessageAsVersion() {
        StringBuffer text = new StringBuffer();
        text.append("It is beta release, please send me issue,\n");
        text.append("Im waiting for public relesae timing.\n\n");
        text.append("V0.59.6 more less CPU Usage(Sequencer)\n");
        text.append("V0.59.3 Auto Scroll PianoRoll\n");
        text.append("V0.59.0 Easy Opratable CCMapping (ListAdd etc)\n");
        text.append("V0.58.8 Virtual Key\n");
        text.append("V0.58.7 CCEditor for XML -> rebuild cache\n");
        text.append("V0.58.6 CCMapping now support Sysex From XML\n");
        text.append("V0.58.5 CCMapping released beta\n");
        text.append("V0.58.3 offset in XML, gate int XML\n");
        text.append("V0.58.2 Minor BugFix\n");
        text.append("V0.58 ResolutionDown\n");
        text.append("V0.57 fixed CheckSum, GateTable etc\n");
        text.append("V0.56 Changed Program Change Timing on Sequencer to General\n");
        text.append("V0.55 MIDI Recorder (save support) in Output Panel\n");
        text.append("V0.54 progress dialog\n");
        text.append("V0.52 Beta published fixed 10~ bugs\n");
        text.append("V0.51 Alpha renew UI 'Mixer' is now 'Surface17' and 'CCMapping'\n");
        text.append("\n");
        text.append("V0.47 sysex bugfix\n");
        text.append("V0.43 Pair CC (+32) support again\n");
        text.append("V0.42b Java Synth Will Ingore GMReset Signal\n");
        text.append("V0.42 SysEX Bugfix\n");
        text.append("V0.4 Phraase Recorder, UWPx3 Bug Fix\n");
        text.append("V0.3+a8 Tuned RangeConverter\n");
        text.append("V0.3 Apache2.0 + GNU GPL3 Dual License\n");
        text.append("\n");
        text.append("V0.16 beta fixed mixer component\n");
        text.append("V0.15 Visitant Architecture, Dataentry Input\n");
        text.append("V0.14 MouseWheel, ControllerSize\n");
        text.append("V0.13 Sequencer RealTime Tuning\n");
        text.append("V0.12 Template for CC Picker\n");
        text.append("V0.11 (Pre) Support DATAENTRY / 14bit\n");
        text.append("V0.10 Support 14 bit CC\n");
        text.append("V0.09 More Humanic Interface\n");
        text.append("V0.08 Drum Pad + Sequence + Chord\n");
        text.append("V0.07 Drum Pad + 0.1\n");
        text.append("V0.06 CC Mixer Chain\n");
        text.append("V0.05 supported Drum Pad\n");
        text.append("V0.04B supported SysEX Checksum\n\n");
        jTextArea1.setText(text.toString());
        jTextArea1.setCaretPosition(0);
        jTextArea1.setEditable(false);
    }

    public void setMessageAsExit() {
        jLabelVersion.setText("Thank you. Closing MixRecipe");
        StringBuffer text = new StringBuffer();
        text.append("Writing Settings.\n");
        text.append("Closing Devices.\n");
        text.append("Shutting Down.\n\n");
        jTextArea1.setText(text.toString());
        jTextArea1.setCaretPosition(0);
        jTextArea1.setEditable(false);
    }
    
    public void writeLine(String line) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {                
                jTextArea1.setText(jTextArea1.getText() + line + "\n");
            }
        });
    }

    public MXProgressDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setTitle("");
        setAlwaysOnTop(true);
        setSize(500, 400);
        MXUtil.centerWindow(this);
        
        jLabelVersion.setText(MXAppConfig.MX_APPNAME);
        getContentPane().setBackground(Color.white);

        _piano = new MXPianoKeys();
        _piano.setNoteRange(0, 3);
        _piano.setLastSelectedColor(MXUtil.mixtureColor(Color.white, 30, Color.green, 50, Color.yellow, 20));
        _piano.setPreferredSize(new Dimension(480, 120));
        jPanelPiano.add(_piano);

        Thread pianoColor = new Thread() {
            public void run() {
                int x = 0;
                int last = -1;
                while (true) {
                    try {
                        Thread.sleep(8);
                    } catch (InterruptedException e) {

                    }
                    x += 1;
                    if (x >= 0 && x <= 36) {
                        if (last >= 0) {
                            _piano.noteOff(last);
                        }
                        last = x;
                        _piano.noteOn(x);
                    } else if (x >= 37 && x <= 72) {
                        int y = 72 - x;
                        if (last >= 0) {
                            _piano.noteOff(last);
                        }
                        last = y;
                        _piano.noteOn(y);
                    } else {
                        x = 0;
                    }
                    if (_pianoStop) {
                        if (last >= 0) {
                            _piano.noteOff(last);
                        }
                        last = 40;
                        _piano.noteOn(40);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            _piano.invalidate();
                            _piano.repaint();
                        }
                    });
                    if (_pianoStop) {
                        break;
                    }
                }
            }
        };
        pianoColor.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                _pianoStop = true;
            }
        });
    }

    boolean _pianoStop = false;
    int _pianoProgress = -1;
    int _lastNote = 0;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanelPiano = new javax.swing.JPanel();
        jLabelVersion = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextArea1.setBackground(new java.awt.Color(255, 247, 234));
        jTextArea1.setColumns(20);
        jTextArea1.setForeground(new java.awt.Color(0, 102, 102));
        jTextArea1.setRows(5);
        jTextArea1.setText("Test");
        jScrollPane1.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jPanelPiano.setLayout(new javax.swing.BoxLayout(jPanelPiano, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanelPiano, gridBagConstraints);

        jLabelVersion.setFont(new java.awt.Font("Bell MT", 0, 24)); // NOI18N
        jLabelVersion.setForeground(new java.awt.Color(0, 0, 255));
        jLabelVersion.setText("Version");
        getContentPane().add(jLabelVersion, new java.awt.GridBagConstraints());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JPanel jPanelPiano;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

}
