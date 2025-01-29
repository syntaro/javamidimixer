
/* calculate cent from key (0 to 11) */
int getKey12Cent(int key) {
    switch(key) {
        case 0: return 0;
        case 2: return 204;
        case 4: return 386;
        case 5: return 498;
        case 7: return 702;
        case 9: return 884;
        case 11: return 1088;
    }
    return (getKey12Cent(key -1) + getKey12Cent(key + 1) ) / 2;
}

/* calculate cent from step (0 to 12 + octave offset (1 octave = 1200)) */
double getKeysCent(int step) {
    if (step == 0) {
        return 0;
    }
    double acent = 0;

    while (step < 0) {
        acent -= 1200;
        step += 12;
    }
    while(step >= 12) {
        acent += 1200;
        step -= 12;
    }
    return acent + getKey12Cent(step);
}

void getPitchesJustIntonation(double *pitches, int root)
{
    /* root = 0 to 12 */
    for (int key = 0; key < 0x80; ++ key) {
        int distance = key - root;
        double cent = getKeysCent(distance);
        pitches[key] = cent;
    }
}

void getPitchesTemperament(double *pitches) {
    for (int key = 0; key < 0x80; ++ key) {
        pitches[key] = key * 100;
    }
}

void adjustAmust(double *pitches, float hzamust) {
    /* hzAmust = around 440 to 443 ? */
    auto log2_ratio = log2(hzamust / 440.);
    double amust = 100. * 69 + 1200. * log2_ratio;

    double slide = amust - pitches[69];
    for (int key = 0; key < 0x80; ++ key) {
        pitches[key] += slide;
    }
}

int my_fluid_retune(fluid_synth_t* synth, int bank, int program, float hzamust, bool equalTemp, int baseKey) {
    int keys[0x80];
    double pitches[0x80];
    const size_t Akey = 69;

    if (bank < 0 || bank >= 128 || program < 0 || program >= 128) {
        // ignore percussion or invalid bank/program
        return FLUID_FAILED;
    }

    if (equalTemp && hzamust== 440.) {
        fluid_synth_activate_tuning(synth, 0, bank, program, 0);
        return FLUID_FAILED;
    }

    for (int i = 0; i < 0x80; ++ i) {
        keys[i] = i;
    }
    if (equalTemp) {
        getPitchesTemperament(pitches);
        adjustAmust(pitches, hzamust);
    }
    else {
        getPitchesJustIntonation(pitches, baseKey);
        adjustAmust(pitches, hzamust);
    }
    int code = fluid_synth_tune_notes(synth, bank, program, 0x80, keys, pitches, 1);
    if (code == FLUID_OK) {
        code = fluid_synth_activate_tuning(synth, 0, bank, program, 1);
    }
    return code;
}
