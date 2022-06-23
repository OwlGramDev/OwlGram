package org.telegram.messenger;

import android.os.Build;
import android.os.VibrationEffect;

import androidx.annotation.RequiresApi;

public enum BotWebViewVibrationEffect {
    IMPACT_LIGHT(new long[] {7}, new int[] {65}, new long[] {50}),
    IMPACT_MEDIUM(new long[] {7}, new int[] {145}, new long[] {50}),
    IMPACT_HEAVY(new long[] {7}, new int[] {255}, new long[] {50}),
    IMPACT_RIGID(new long[] {3}, new int[] {225}, new long[] {50}),
    IMPACT_SOFT(new long[] {10}, new int[] {175}, new long[] {50}),
    NOTIFICATION_ERROR(new long[] {14,48,14,48,14,48,20}, new int[] {200,0,200,0,255,0,145}, new long[] {50}),
    NOTIFICATION_SUCCESS(new long[] {14,65,14}, new int[] {175,0,255}, new long[] {50}),
    NOTIFICATION_WARNING(new long[] {14,64,14}, new int[] {225,0,175}, new long[] {50}),
    SELECTION_CHANGE(new long[] {1}, new int[] {65}, new long[] {50});

    public final long[] timings;
    public final int[] amplitudes;
    public final long[] fallbackTimings;
    private Object vibrationEffect;

    BotWebViewVibrationEffect(long[] timings, int[] amplitudes, long[] fallbackTimings) {
        this.timings = timings;
        this.amplitudes = amplitudes;
        this.fallbackTimings = fallbackTimings;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public VibrationEffect getVibrationEffectForOreo() {
        if (vibrationEffect == null) {
            if (!AndroidUtilities.getVibrator().hasAmplitudeControl()) {
                vibrationEffect = VibrationEffect.createWaveform(fallbackTimings, -1);
            } else {
                vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, -1);
            }
        }

        return (VibrationEffect) vibrationEffect;
    }
}
