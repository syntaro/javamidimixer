/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jp.synthtarou.mixtone.synth.soundfont;

import java.util.ArrayList;

/**
 *
 * @author Syntarou YOSHIDA
 */
public class XTGenOperatorMaster extends ArrayList<XTGenOperator>{
    
    public static final int startAddrsOffset = 0;
    public static final int endAddrsOffset = 1;
    public static final int startloopAddrsOffset = 2;
    public static final int endloopAddrsOffset = 3;
    public static final int startAddrsCoarseOffset = 4;
    public static final int modLfoToPitch = 5;
    public static final int vibLfoToPitch = 6;
    public static final int modEnvToPitch = 7;
    
    public static final int initialFilterFc = 8;
    public static final int initialFilterQ = 9;
    public static final int modLfoToFilterFc = 10;
    public static final int modEnvToFilterFc = 11;
    public static final int endAddrsCoarseOffset = 12;

    public static final int modLfoToVolume = 13;
    public static final int unused1 = 14;
    public static final int chorusEffectsSend = 15;
    public static final int reverbEffectsSend = 16;
    public static final int pan = 17;

    public static final int unused2 = 18;
    public static final int unused3 = 19;
    public static final int unused4 = 20;

    public static final int delayModLFO = 21;
    public static final int freqModLFO = 22;
    public static final int delayVibLFO = 23;
    public static final int freqVibLFO = 24;
    public static final int delayModEnv = 25;

    public static final int attackModEnv = 26;
    public static final int holdModEnv = 27;
    public static final int decayModEnv = 28;
    public static final int sustainModEnv = 29;
    public static final int releaseModEnv = 30;

    public static final int keynumToModEnvHold = 31;
    public static final int keynumToModEnvDecay = 32;
    public static final int delayVolEnv = 33;
    public static final int attackVolEnv = 34;
    public static final int holdVolEnv = 35;

    public static final int decayVolEnv = 36;
    public static final int sustainVolEnv = 37;
    public static final int releaseVolEnv = 38;
    public static final int keynumToVolEnvHold = 39;
    public static final int keynumToVolEnvDecay = 40;

    public static final int instrument = 41;
    public static final int reserved1 = 42;
    public static final int keyRange = 43;
    public static final int velRange = 44;
    public static final int startloopAddrsCoarseOffset = 45;

    public static final int keynum = 46;
    public static final int velocity = 47;
    public static final int initialAttenuation = 48;
    public static final int reserved2 = 49;
    public static final int endloopAddrsCoarseOffset = 50;

    public static final int coarseTune = 51;
    public static final int fineTune = 52;
    public static final int sampleID = 53;
    public static final int sampleModes = 54;
    public static final int reserved3 = 55;

    public static final int scaleTuning = 56;
    public static final int exclusiveClass = 57;
    public static final int overridingRootKey = 58;
    public static final int unused5 = 59;
    public static final int endOper = 60;

    public XTGenOperatorMaster() {
        super();

        XTGenOperator.SFZTranslator div10 = (v) -> v / 10.0;
        XTGenOperator.SFZTranslator div100 = (v) -> v / 100.0;
        XTGenOperator.SFZTranslator thepow = (v) -> Math.pow(2, v / 1200.0);
        XTGenOperator.SFZTranslator thepow8176 = (v) -> 8.176 * Math.pow(2, v / 1200.0);

        String SAMPLES = "smpls";
        String SEMITONE = "cent fs";
        String CENT = "Hz";
        String PERCENT = "%";
        String SAMPLES32k = "x 32768 smpls";

        //https://freepats.zenvoid.org/sf2/sfspec24.pdf

        //0~9
        newEntry(startAddrsOffset, SAMPLES, "startAddrsOffset").range(0, 0, 32767).generator(0, null);
        newEntry(endAddrsOffset, SAMPLES, "endAddrsOffset").range(0, 0, 32767).generator(null, 0);
        newEntry(startloopAddrsOffset, SAMPLES, "startloopAddrsOffset").range(0, 0, 32767).generator(null, null);
        newEntry(endloopAddrsOffset, SAMPLES, "endloopAddrsOffset").range(0, 0, 32767).generator(null, null);
        newEntry(startAddrsCoarseOffset, "x32768 smpls", "startAddrsCoarseOffset").range(0, 0, 32767).generator(0, null);

        newEntry(modLfoToPitch, SEMITONE, "modLfoToPitch").trans(div100).range(0, -120, 120).generator(-12000, 12000);
        newEntry(vibLfoToPitch, SEMITONE, "vibLfoToPitch").trans(div100).range(0, -120, 120).generator(-12000, 12000);
        newEntry(modEnvToPitch, SEMITONE, "modEnvToPitch").trans(div100).range(0, -120, 120).generator(-12000, 12000);
        //newEntry(initialFilterFc, CENT, "initialFilterFc").trans(thepow8176).range(19912.7, 19.5, 19912.7).generator(1500, 13500);
        newEntry(initialFilterFc, CENT, "initialFilterFc").trans(thepow8176).range(24000.0, 8.0, 24000.0).generator(1500, 13500);
        newEntry(initialFilterQ, "cB", "initialFilterQ").trans((v) -> v / 10.0).range(0, 0, 96).generator(0, 960);

        //10-19
        newEntry(modLfoToFilterFc, SEMITONE, "modLfoToFilterFc").trans(div100).range(0, -120, 120).generator(-12000, 12000);
        newEntry(modEnvToFilterFc, SEMITONE, "modEnvToFilterFc").trans(div100).range(0, -120, 120).generator(-12000, 12000);
        newEntry(endAddrsCoarseOffset, SAMPLES32k, "endAddrsCoarseOffset").range(0, 0, 32767).generator(null, null);
        newEntry(modLfoToVolume, "cB fs", "modLfoToVolume").trans(div10).range(0, -96, 96).generator(-960, 960);
        newEntry(unused1, null, "unused1");
        newEntry(chorusEffectsSend, PERCENT, "chorusEffectsSend").trans(div10).range(0, 0, 100).generator(0, 1000);
        newEntry(reverbEffectsSend, PERCENT, "reverbEffectsSend").trans(div10).range(0, 0, 100).generator(0, 1000);
        newEntry(pan, "%", "pan").trans( div10).range(0, -50, 50).generator(-500, 500);
        newEntry(unused2, null, "unused2");
        newEntry(unused3, null, "unused3");

        //20-29
        newEntry(unused4, null, "unused4");
        newEntry(delayModLFO, "sec", "delayModLFO").trans( thepow).range(0.001, 0.001, 17.979).generator(-12000, 5000);
        newEntry(freqModLFO, CENT, "freqModLFO").range(8.2, 0.1, 110.10).generator(-16000, 4500);
        newEntry(delayVibLFO, "sec", "delayVibLFO").trans(thepow).range(0.001, 0.001, 17.959).generator(-12000, 5000);
        newEntry(freqVibLFO, CENT, "freqVibLFO").trans(thepow8176).range(8.2, 0.1, 110.10).generator(-16000, 4500);
        newEntry(delayModEnv, "sec", "delayModEnv").trans(thepow).range(0.001, 0.001, 17.959).generator(-12000, 5000);
        newEntry(attackModEnv, "sec", "attackModEnv").trans(thepow).range(0.001, 0.001, 101.594).generator(-12000, 8000);
        newEntry(holdModEnv, "sec", "holdModEnv").trans(thepow).range(0.001, 0.001, 17.959).generator(-12000, 5000);
        newEntry(decayModEnv, "sec", "decayModEnv").trans(thepow).range(0.001, 0.001, 101.594).generator(8000, 8000);
        newEntry(sustainModEnv, "%", "sustainModEnv").trans(div10).range(0, 0, 100).generator(0, 1000);
        
        //30-39
        newEntry(releaseModEnv, "sec", "releaseModEnv").trans(thepow).range(0.001, 0.001, 101.594).generator(-12000, 8000);
        newEntry(keynumToModEnvHold, "semitone/key", "keynumToModEnvHold").trans(div100).range(0, -12, 12).generator(-1200, 1200);
        newEntry(keynumToModEnvDecay, "semitone/key", "keynumToModEnvDecay").trans(div100).range(0, -12, 12).generator(-1200, 1200);
        newEntry(delayVolEnv, "sec", "delayVolEnv").trans(thepow).range(0.001, 0.001, 17.959).generator(-12000, 5000);
        newEntry(attackVolEnv, "sec", "attackVolEnv").trans(thepow).range(0.001, 0.001, 101.594).generator(-12000, 8000);
        newEntry(holdVolEnv, "sec", "holdVolEnv").trans(thepow).range(0.001, 0.001, 17.959).generator(-12000, 5000);
        newEntry(decayVolEnv, "sec", "decayVolEnv").trans(thepow).range(0.001, 0.001, 101.594).generator(-12000, 8000);

        newEntry(sustainVolEnv, "cB attn", "sustainVolEnv").trans(div10).range(0, 0, 144).generator( 0, 1440);
        newEntry(releaseVolEnv, "sec", "releaseVolEnv").trans(thepow).range(0.001, 0.001, 101.594).generator(-12000, 8000);
        newEntry(keynumToVolEnvHold, "semitone/key", "keynumToVolEnvHold").trans(div100).range(0, -12, 12).generator(-1200, 1200);

        //40-49
        newEntry(keynumToVolEnvDecay, "semitone/key", "keynumToVolEnvDecay").trans(div100).range(0, -12, 12).generator(-1200, 1200);
        newEntry(instrument, null, "instrument");
        newEntry(reserved1, null, "reserved1");
        newEntry(keyRange, "MIDI Key# 8bit+8bit", "keyRange").generator(0, 0x7f7f);//0~7f = min 0~7f = max 8bit+8bit
        newEntry(velRange, "Velocity# 8bit+8bit", "velRange").generator(0, 0x7f7f);//0~7f = min 0~7f = max 8bit+8bit
        newEntry(startloopAddrsCoarseOffset, "x 32768 smpls", "startloopAddrsCoarseOffset").generator(0, 32767);
        newEntry(keynum, "MIDI Key#", "keynum").range(null, 0, 127).generator(0, 127);
        newEntry(velocity, "MIDI Veclotity", "velocity").range(null, 0, 127).generator(0, 127);
        newEntry(initialAttenuation, "cB", "initialAttenuation").trans(div10).range(0.0, 0.0, 144.0).generator(0, 1440);
        newEntry(reserved2, null, "reserved2");

        //50-59
        newEntry(endloopAddrsCoarseOffset, SAMPLES, "endloopAddrsCoarseOffset").range(0, 0, 32767).generator(0, 32767);
        newEntry(coarseTune, SEMITONE, "coarseTune").trans(div10).range(0, -12, 12).generator(-120, 120);
        newEntry(fineTune, "cent", "fineTune").range(0, -99, 99).generator(-99, 99);
        newEntry(sampleID, null, "sampleID");
        newEntry(sampleModes, "BigFlags", "sampleModes").range(0, 0, 3).generator(0, 3);
        newEntry(reserved3, null, "reserved3");
        newEntry(scaleTuning, "cent/key", "scaleTuning").range(100, 0, 1200).generator(0, 1200);
        newEntry(exclusiveClass, null, "exclusiveClass").range(null, 0, 127).generator(0, 127);
        newEntry(overridingRootKey, null, "overridingRootKey").range(null, 0, 127).generator(0, 127);
        newEntry(unused5, null, "unused5");

        //60
        newEntry(endOper, null, "endOper");
    }

    public final XTGenOperator newEntry(int id, String unit, String name) {
        XTGenOperator ope = new XTGenOperator(id, name);        
        add(ope);
        ope.unit(unit);
        return ope;
    }

    @Override
    public XTGenOperator get(int x) {
        if (x >= 0 && x <= size() - 1) {
            return super.get(x);
        }
        return null;
    }
}
