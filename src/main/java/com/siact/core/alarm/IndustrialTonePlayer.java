package com.siact.core.alarm;

import lombok.extern.slf4j.Slf4j;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class IndustrialTonePlayer implements AutoCloseable {

    private static final int SAMPLE_RATE = 48000;
    private static final int CHANNELS = 2;
    private static final int FADE_FRAMES = (int) (SAMPLE_RATE * 0.006);

    private final AtomicLong activeSessionId = new AtomicLong(0);
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private volatile Thread workerThread;

    private long device;
    private long context;
    private volatile boolean initialized;

    public IndustrialTonePlayer() {
        try {
            device = ALC10.alcOpenDevice((ByteBuffer) null);
            if (device == 0L) {
                throw new IllegalStateException("无法打开 OpenAL 设备");
            }

            context = ALC10.alcCreateContext(device, (IntBuffer) null);
            if (context == 0L) {
                ALC10.alcCloseDevice(device);
                throw new IllegalStateException("无法创建 OpenAL 上下文");
            }

            ALC10.alcMakeContextCurrent(context);
            ALCCapabilities deviceCaps = ALC.createCapabilities(device);
            AL.createCapabilities(deviceCaps);

            initialized = true;
            log.info("OpenAL 初始化成功");
        } catch (UnsatisfiedLinkError e) {
            log.warn("OpenAL native 库未找到，音频播放不可用: {}", e.getMessage());
            initialized = false;
        } catch (Exception e) {
            log.warn("OpenAL 初始化失败，音频播放不可用: {}", e.getMessage());
            initialized = false;
        }
    }

    // ========== Public API ==========

    public void play(ToneType type) {
        play(type, type.getDefaultDurationMillis());
    }

    public void play(ToneType type, Duration duration) {
        play(type, duration.toMillis());
    }

    public void play(ToneType type, long durationMillis) {
        if (!checkArgs(type, durationMillis)) return;

        long sessionId = beginNewSession();
        try {
            ALC10.alcMakeContextCurrent(context);
            runPlayback(sessionId, type, durationMillis);
        } finally {
            finishSession(sessionId);
        }
    }

    public void startLoop(ToneType type) {
        if (type == null) throw new IllegalArgumentException("ToneType 不能为空");
        if (!initialized) return;

        long sessionId = beginNewSession();
        startWorker(sessionId, type, null);
    }

    public void startLoop(ToneType type, Duration duration) {
        startLoop(type, duration.toMillis());
    }

    public void startLoop(ToneType type, long durationMillis) {
        if (!checkArgs(type, durationMillis)) return;

        long sessionId = beginNewSession();
        startWorker(sessionId, type, durationMillis);
    }

    public void stop() {
        Thread toJoin;
        synchronized (this) {
            activeSessionId.incrementAndGet();
            playing.set(false);
            toJoin = workerThread;
            workerThread = null;
        }

        if (toJoin != null && toJoin != Thread.currentThread()) {
            toJoin.interrupt();
            try {
                toJoin.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isPlaying() {
        return playing.get();
    }

    @Override
    public void close() {
        stop();
        if (initialized) {
            try {
                ALC10.alcMakeContextCurrent(0);
                ALC10.alcDestroyContext(context);
                ALC10.alcCloseDevice(device);
            } catch (Exception e) {
                log.warn("OpenAL 资源释放异常: {}", e.getMessage());
            }
            initialized = false;
        }
    }

    // ========== Session ==========

    private long beginNewSession() {
        stop();
        synchronized (this) {
            long sid = activeSessionId.incrementAndGet();
            playing.set(true);
            return sid;
        }
    }

    private void finishSession(long sessionId) {
        synchronized (this) {
            if (activeSessionId.get() == sessionId) {
                playing.set(false);
                workerThread = null;
            }
        }
    }

    private boolean isCurrentSession(long sessionId) {
        return playing.get()
                && activeSessionId.get() == sessionId
                && !Thread.currentThread().isInterrupted();
    }

    // ========== Worker ==========

    private void startWorker(long sessionId, ToneType type, Long durationMillis) {
        Thread t = new Thread(() -> {
            try {
                ALC10.alcMakeContextCurrent(context);
                runPlayback(sessionId, type, durationMillis);
            } catch (Exception e) {
                if (isCurrentSession(sessionId)) {
                    log.error("播放出错: {}", e.getMessage());
                }
            } finally {
                ALC10.alcMakeContextCurrent(0);
                finishSession(sessionId);
            }
        }, "industrial-tone-" + type.name().toLowerCase());

        t.setDaemon(true);
        synchronized (this) {
            if (isCurrentSession(sessionId)) {
                workerThread = t;
            }
        }
        t.start();
    }

    // ========== Playback ==========

    private void runPlayback(long sessionId, ToneType type, Long durationMillis) {
        int source = AL10.alGenSources();
        int buffer = AL10.alGenBuffers();

        try {
            AL10.alGetError();

            ShortBuffer pcm = generatePatternPCM(type);
            AL10.alBufferData(buffer, AL10.AL_FORMAT_STEREO16, pcm, SAMPLE_RATE);
            AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
            AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_TRUE);
            AL10.alSourcePlay(source);

            if (durationMillis == null) {
                while (isCurrentSession(sessionId)) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } else {
                long endNanos = System.nanoTime() + durationMillis * 1_000_000L;
                while (isCurrentSession(sessionId) && System.nanoTime() < endNanos) {
                    long remaining = (endNanos - System.nanoTime()) / 1_000_000L;
                    if (remaining <= 0) break;
                    try {
                        Thread.sleep(Math.min(remaining, 100));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            AL10.alSourceStop(source);
        } finally {
            try { AL10.alDeleteSources(source); } catch (Exception ignored) {}
            try { AL10.alDeleteBuffers(buffer); } catch (Exception ignored) {}
        }
    }

    // ========== PCM Generation ==========

    private ShortBuffer generatePatternPCM(ToneType type) {
        int totalFrames = 0;
        for (ToneStep step : type.getPattern()) {
            totalFrames += framesFor(step.durationMillis);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(totalFrames * CHANNELS * 2)
                .order(ByteOrder.nativeOrder());
        ShortBuffer pcm = bb.asShortBuffer();

        int frameOffset = 0;
        for (ToneStep step : type.getPattern()) {
            int frames = framesFor(step.durationMillis);
            int fade = Math.min(FADE_FRAMES, frames / 2);

            for (int i = 0; i < frames; i++) {
                short sample;
                if (step.frequencyHz <= 0) {
                    sample = 0;
                } else {
                    double angle = 2.0 * Math.PI * step.frequencyHz * (frameOffset + i) / SAMPLE_RATE;
                    double envelope = 1.0;
                    if (i < fade) envelope = (double) i / fade;
                    int fromEnd = frames - i - 1;
                    if (fromEnd < fade) envelope = Math.min(envelope, (double) fromEnd / fade);
                    sample = (short) (Math.sin(angle) * step.volume * envelope * Short.MAX_VALUE);
                }
                pcm.put(sample);
                pcm.put(sample);
            }
            frameOffset += frames;
        }

        pcm.flip();
        return pcm;
    }

    private int framesFor(long durationMillis) {
        return Math.max(1, (int) (SAMPLE_RATE * durationMillis / 1000.0));
    }

    private boolean checkArgs(ToneType type, long durationMillis) {
        if (type == null) throw new IllegalArgumentException("ToneType 不能为空");
        if (durationMillis <= 0) throw new IllegalArgumentException("播放时长必须大于 0");
        return initialized;
    }

    // ========== Tone Types ==========

    private static ToneStep tone(double frequencyHz, long durationMillis, double volume) {
        return new ToneStep(frequencyHz, durationMillis, volume);
    }

    private static ToneStep silence(long durationMillis) {
        return new ToneStep(0, durationMillis, 0);
    }

    private static class ToneStep {
        final double frequencyHz;
        final long durationMillis;
        final double volume;

        ToneStep(double frequencyHz, long durationMillis, double volume) {
            this.frequencyHz = frequencyHz;
            this.durationMillis = durationMillis;
            this.volume = Math.max(0, Math.min(volume, 1));
        }
    }

    public enum ToneType {
        NOTICE(2_000, new ToneStep[]{
                tone(900, 180, 0.45),
                silence(80),
                tone(1200, 220, 0.45),
                silence(180)
        }),

        STATUS(3_000, new ToneStep[]{
                tone(800, 200, 0.50),
                silence(80),
                tone(1100, 200, 0.50),
                silence(80),
                tone(1400, 300, 0.50),
                silence(250)
        }),

        WARNING(4_000, new ToneStep[]{
                tone(1200, 250, 0.60),
                silence(120),
                tone(1200, 250, 0.60),
                silence(250),
                tone(900, 600, 0.60),
                silence(300)
        }),

        ALARM(6_000, new ToneStep[]{
                tone(1600, 180, 0.65),
                silence(70),
                tone(1600, 180, 0.65),
                silence(70),
                tone(1000, 180, 0.65),
                silence(350),
                tone(800, 800, 0.65),
                silence(400)
        }),

        CRITICAL(9_000, new ToneStep[]{
                tone(1800, 250, 0.75),
                silence(60),
                tone(700, 250, 0.75),
                silence(60),
                tone(1800, 250, 0.75),
                silence(180),
                tone(650, 1200, 0.75),
                silence(500)
        });

        private final long defaultDurationMillis;
        private final ToneStep[] pattern;

        ToneType(long defaultDurationMillis, ToneStep[] pattern) {
            this.defaultDurationMillis = defaultDurationMillis;
            this.pattern = pattern;
        }

        public long getDefaultDurationMillis() {
            return defaultDurationMillis;
        }

        public ToneStep[] getPattern() {
            return pattern;
        }
    }
}