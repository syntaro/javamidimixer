#pragma once

struct TimeInfo
{
    double sample_rate_ = 44100.0;
    long sample_pos_ = 0;
    long sample_length_ = 0;
    double ppq_pos_ = 0;
    bool is_playing_ = false;
    double tempo_ = 120.0;
    long clock = 0;
};
