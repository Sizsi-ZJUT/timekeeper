package com.timekeeper.service;

import com.timekeeper.entity.Preference;

public interface PreferenceService {

    Preference getByUser(Long userId);

    Preference update(Long userId, Integer pomodoroWorkMinutes,
                      Integer pomodoroBreakMinutes, Integer defaultCountdownMinutes);

    Preference reset(Long userId);
}
