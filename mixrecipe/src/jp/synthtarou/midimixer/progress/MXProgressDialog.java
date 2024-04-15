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
package jp.synthtarou.midimixer.progress;

import jp.synthtarou.libs.MXSafeThread;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import jp.synthtarou.libs.MXUtil;
import jp.synthtarou.libs.MainThreadTask;
import jp.synthtarou.libs.log.ListModelOutputStream;
import jp.synthtarou.libs.log.MXFileLogger;
import jp.synthtarou.midimixer.MXConfiguration;
import jp.synthtarou.midimixer.mx00playlist.MXPianoKeys;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class MXProgressDialog extends javax.swing.JDialog {

    MXPianoKeys _piano;
    ListModelOutputStream _stream = MXFileLogger.getListStream();

    public void setMessageAsStartUP() {
        _stream.clearLogLine();
        _stream.addLine("Compiled with OpenJDK 22 + NetBeans IDE19\n");
        _stream.addLine(("Copyright(C) SynthTAROU. GNU GPLv3\n"));
        _stream.addLine("\n");
        _stream.addLine(("Thank you for Install.\n"));
        _stream.addLine(("I wish  You have Fun.\n"));
        _stream.addLine("\n\n<Java Information>");
        _stream.addLine("java.vendor=" + System.getProperty("java.vendor") + "\n");
        _stream.addLine("java.version=" + System.getProperty("java.version") + "\n\n");
    }

    public void setMessageAsVersion() {
        _stream.clearLogLine();
        _stream.addLine("V0.04B supported SysEX Checksum\n\n");
        _stream.addLine("V0.05 supported Drum Pad\n");
        _stream.addLine("V0.06 CC Mixer Chain\n");
        _stream.addLine("V0.07 Drum Pad + 0.1\n");
        _stream.addLine("V0.08 Drum Pad + Sequence + Chord\n");
        _stream.addLine("V0.09 More Humanic Interface\n");
        _stream.addLine("V0.10 Support 14 bit CC\n");
        _stream.addLine("V0.11 (Pre) Support DATAENTRY / 14bit\n");
        _stream.addLine("V0.12 Template for CC Picker\n");
        _stream.addLine("V0.13 Sequencer RealTime Tuning\n");
        _stream.addLine("V0.14 MouseWheel, ControllerSize\n");
        _stream.addLine("V0.15 Visitant Architecture, Dataentry Input\n");
        _stream.addLine("V0.16 beta fixed mixer component\n");
        _stream.addLine("V0.3 Apache2.0 + GNU GPL3 Dual License\n");
        _stream.addLine("V0.3+a8 Tuned RangeConverter\n");
        _stream.addLine("V0.4 Phraase Recorder, UWPx3 Bug Fix\n");
        _stream.addLine("V0.42 SysEX Bugfix\n");
        _stream.addLine("V0.42b Java Synth Will Ingore GMReset Signal\n");
        _stream.addLine("V0.43 Pair CC (+32) support again\n");
        _stream.addLine("V0.47 sysex bugfix\n");
        _stream.addLine("V0.51 Alpha renew UI 'Mixer' is now 'Surface17' and 'CCMapping'\n");
        _stream.addLine("V0.52 Beta published fixed 10~ bugs\n");
        _stream.addLine("V0.54 progress dialog\n");
        _stream.addLine("V0.55 MIDI Recorder (save support) in Output Panel\n");
        _stream.addLine("V0.56 Changed Program Change Timing on Sequencer to General\n");
        _stream.addLine("V0.57 fixed CheckSum, GateTable etc\n");
        _stream.addLine("V0.58 ResolutionDown\n");
        _stream.addLine("V0.58.2 Minor BugFix\n");
        _stream.addLine("V0.58.3 offset in XML, gate int XML\n");
        _stream.addLine("V0.58.5 CCMapping released beta\n");
        _stream.addLine("V0.58.6 CCMapping now support Sysex From XML\n");
        _stream.addLine("V0.58.7 CCEditor for XML -> rebuild cache\n");
        _stream.addLine("V0.58.8 Virtual Key\n");
        _stream.addLine("V0.59.0 Easy Opratable CCMapping (ListAdd etc)\n");
        _stream.addLine("V0.59.3 Auto Scroll PianoRoll\n");
        _stream.addLine("V0.59.6 more less CPU Usage(Sequencer)\n");
        _stream.addLine("V0.59.7 more Smooth Piano Roll\n");
        _stream.addLine("V0.59.8 Piano Roll Support Click and Option\n");
        _stream.addLine("V0.59.9 Some display tuned up\n");
        _stream.addLine("V0.60 Running Status, DATAENTRY\n");
        _stream.addLine("V0.61 DrumPad Implementation\n");
        _stream.addLine("V0.62 dataentry bug fixed 2\n");
        _stream.addLine("V0.63 sysex bug fixed 2\n");
        _stream.addLine("V0.63.2 CPU Optimized for Console\n");
        _stream.addLine("V0.64 BugFix to not Skip note when Inperfect DataEntry\n");
        _stream.addLine("V0.70 Test Driven Development");
        _stream.addLine("V0.71 support json, more good piano roll, ");
        _stream.addLine("V0.71a json not save file for now. plz wait");
        _stream.addLine("V0.75beta fix freeze when doing toomany zoom pianoroll, layer import/export fix");
        _stream.addLine("V0.75beta6 fix drum surface etc");
        _stream.addLine("V0.77 14bit controllable");
        _stream.addLine("V0.77.5 some bug fix");

        _stream.addLine("It is beta release, please send me issue / problem / wish,\n");
        _stream.addLine("Im waiting for public relesae timing.\n\n");
    }

    public void setMessageAsExit() {
        _stream.clearLogLine();
        jLabelVersion.setText("Closing MixRecipe");
        StringBuffer text = new StringBuffer();
        _stream.addLine("Writing Settings.\n");
        _stream.addLine("Closing Devices.\n");
        _stream.addLine("Shutting Down.\n\n");
    }

    public void progressTextLines(String line) {
        _stream.addLine(line);
    }

    public MXProgressDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setTitle("");
        setAlwaysOnTop(true);
        setSize(500, 400);
        MXUtil.centerWindow(this);
        _stream.attachListForLogging(jList1);

        jLabelVersion.setText(MXConfiguration.MX_APPLICATION);

        _piano = new MXPianoKeys();
        _piano.setNoteRange(0, 3);
        _piano.setLastSelectedColor(MXUtil.mixtureColor(Color.white, 30, Color.green, 50, Color.yellow, 20));
        _piano.setPreferredSize(new Dimension(480, 120));
        jPanelPiano.add(_piano);

        Thread pianoColor = new MXSafeThread("*PianoColor", () -> {
            int x1 = 0;
            int last = -1;
            while (true) {
                try {
                    Thread.sleep(8);
                } catch (InterruptedException e) {
                    return;
                }
                x1 += 1;
                if (x1 >= 0 && x1 <= 36) {
                    if (last >= 0) {
                        _piano.noteOff(last);
                    }
                    last = x1;
                    _piano.noteOn(x1);
                } else if (x1 >= 37 && x1 <= 72) {
                    int y1 = 72 - x1;
                    if (last >= 0) {
                        _piano.noteOff(last);
                    }
                    last = y1;
                    _piano.noteOn(y1);
                } else {
                    x1 = 0;
                }
                if (_pianoStop) {
                    if (last >= 0) {
                        _piano.noteOff(last);
                    }
                    last = 40;
                    _piano.noteOn(40);
                }
                new MainThreadTask(() -> {
                    _piano.invalidate();
                    _piano.repaint();
                });
                if (_pianoStop) {
                    break;
                }
            }
        });
        pianoColor.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _pianoStop = true;
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
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

        jPanelPiano = new javax.swing.JPanel();
        jLabelVersion = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanelPiano.setLayout(new javax.swing.BoxLayout(jPanelPiano, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanelPiano, gridBagConstraints);

        jLabelVersion.setFont(new java.awt.Font("Bell MT", 0, 24)); // NOI18N
        jLabelVersion.setText("Version");
        getContentPane().add(jLabelVersion, new java.awt.GridBagConstraints());

        jList1.setBackground(new java.awt.Color(255, 247, 234));
        jList1.setForeground(new java.awt.Color(0, 102, 102));
        jScrollPane2.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanelPiano;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

}
