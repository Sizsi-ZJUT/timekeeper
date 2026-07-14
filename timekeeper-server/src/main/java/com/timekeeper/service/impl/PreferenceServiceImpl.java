package com.timekeeper.service.impl;

import com.timekeeper.entity.Preference;
import com.timekeeper.repository.PreferenceRepository;
import com.timekeeper.service.PreferenceService;
import org.springframework.stereotype.Service;

@Service
public class PreferenceServiceImpl implements PreferenceService {

    private static final int DEFAULT_WORK = 25;
    private static final int DEFAULT_BREAK = 5;
    private static final int DEFAULT_COUNTDOWN = 25;

    private final PreferenceRepository preferenceRepository;

    public PreferenceServiceImpl(PreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public Preference getByUser(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Preference pref = new Preference(userId);
                    return preferenceRepository.save(pref);
                });
    }

    @Override
    public Preference update(Long userId, Integer pomodoroWorkMinutes,
                             Integer pomodoroBreakMinutes, Integer defaultCountdownMinutes) {
        Preference pref = getByUser(userId);
        if (pomodoroWorkMinutes != null) pref.setPomodoroWorkMinutes(pomodoroWorkMinutes);
        if (pomodoroBreakMinutes != null) pref.setPomodoroBreakMinutes(pomodoroBreakMinutes);
        if (defaultCountdownMinutes != null) pref.setDefaultCountdownMinutes(defaultCountdownMinutes);
        return preferenceRepository.save(pref);
    }

    @Override
    public Preference reset(Long userId) {
        Preference pref = getByUser(userId);
        pref.setPomodoroWorkMinutes(DEFAULT_WORK);
        pref.setPomodoroBreakMinutes(DEFAULT_BREAK);
        pref.setDefaultCountdownMinutes(DEFAULT_COUNTDOWN);
        return preferenceRepository.save(pref);
    }
}
