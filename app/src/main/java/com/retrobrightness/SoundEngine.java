package com.retrobrightness;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundEngine {

    private static final int SAMPLE_RATE = 44100;

    private final ExecutorService audioExecutor =
            Executors.newSingleThreadExecutor();

    private volatile boolean sliding = false;

    private AudioTrack slideTrack;

    public void grab() {
        playMechanicalClick(
                0.085f,
                1250f,
                720f
        );
    }

    public void release() {
        playMechanicalClick(
                0.060f,
                920f,
                510f
        );
    }

    public void startSliding() {

        if (sliding) {
            return;
        }

        sliding = true;

        audioExecutor.execute(() -> {

            final int bufferSamples = 2048;

            short[] buffer =
                    new short[bufferSamples];

            try {

                AudioAttributes attributes =
                        new AudioAttributes.Builder()
                                .setUsage(
                                        AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
                                )
                                .setContentType(
                                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                                )
                                .build();

                AudioFormat format =
                        new AudioFormat.Builder()
                                .setEncoding(
                                        AudioFormat.ENCODING_PCM_16BIT
                                )
                                .setSampleRate(
                                        SAMPLE_RATE
                                )
                                .setChannelMask(
                                        AudioFormat.CHANNEL_OUT_MONO
                                )
                                .build();

                slideTrack =
                        new AudioTrack(
                                attributes,
                                format,
                                bufferSamples * 2,
                                AudioTrack.MODE_STREAM,
                                AudioManager.AUDIO_SESSION_ID_GENERATE
                        );

                slideTrack.play();

                long sampleIndex = 0;

                while (sliding) {

                    for (int i = 0;
                         i < bufferSamples;
                         i++) {

                        double t =
                                (sampleIndex + i)
                                        / (double) SAMPLE_RATE;

                        double texture =
                                Math.sin(
                                        2.0
                                                * Math.PI
                                                * 73.0
                                                * t
                                );

                        double rattle =
                                Math.sin(
                                        2.0
                                                * Math.PI
                                                * 137.0
                                                * t
                                );

                        double noise =
                                (Math.random() * 2.0)
                                        - 1.0;

                        double signal =
                                texture * 0.20
                                        + rattle * 0.08
                                        + noise * 0.055;

                        signal *= 0.32;

                        buffer[i] =
                                (short) (
                                        Math.max(
                                                -1.0,
                                                Math.min(
                                                        1.0,
                                                        signal
                                                )
                                        ) * 32767
                                );
                    }

                    slideTrack.write(
                            buffer,
                            0,
                            buffer.length
                    );

                    sampleIndex += bufferSamples;
                }

            } catch (Exception ignored) {

            } finally {

                stopTrack();
            }
        });
    }

    public void stopSliding() {

        sliding = false;

        stopTrack();
    }

    private void stopTrack() {

        try {

            if (slideTrack != null) {

                slideTrack.stop();
                slideTrack.release();
                slideTrack = null;
            }

        } catch (Exception ignored) {
        }
    }

    private void playMechanicalClick(
            float duration,
            float frequency1,
            float frequency2) {

        audioExecutor.execute(() -> {

            int samples =
                    (int) (
                            SAMPLE_RATE
                                    * duration
                    );

            short[] data =
                    new short[samples];

            for (int i = 0;
                 i < samples;
                 i++) {

                double t =
                        i
                                / (double) SAMPLE_RATE;

                double envelope =
                        Math.exp(
                                -t * 55.0
                        );

                double tone1 =
                        Math.sin(
                                2.0
                                        * Math.PI
                                        * frequency1
                                        * t
                        );

                double tone2 =
                        Math.sin(
                                2.0
                                        * Math.PI
                                        * frequency2
                                        * t
                        );

                double noise =
                        (Math.random() * 2.0)
                                - 1.0;

                double signal =
                        (
                                tone1 * 0.48
                                        + tone2 * 0.28
                                        + noise * 0.24
                        )
                                * envelope
                                * 0.55;

                data[i] =
                        (short) (
                                Math.max(
                                        -1.0,
                                        Math.min(
                                                1.0,
                                                signal
                                        )
                                ) * 32767
                        );
            }

            playPcm(data);
        });
    }

    private void playPcm(short[] data) {

        AudioTrack track = null;

        try {

            AudioAttributes attributes =
                    new AudioAttributes.Builder()
                            .setUsage(
                                    AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
                            )
                            .setContentType(
                                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                            )
                            .build();

            AudioFormat format =
                    new AudioFormat.Builder()
                            .setEncoding(
                                    AudioFormat.ENCODING_PCM_16BIT
                            )
                            .setSampleRate(
                                    SAMPLE_RATE
                            )
                            .setChannelMask(
                                    AudioFormat.CHANNEL_OUT_MONO
                            )
                            .build();

            track =
                    new AudioTrack(
                            attributes,
                            format,
                            data.length * 2,
                            AudioTrack.MODE_STATIC,
                            AudioManager.AUDIO_SESSION_ID_GENERATE
                    );

            track.write(
                    data,
                    0,
                    data.length
            );

            track.play();

            Thread.sleep(
                    Math.max(
                            20,
                            data.length * 1000L
                                    / SAMPLE_RATE
                    )
            );

        } catch (Exception ignored) {

        } finally {

            if (track != null) {

                try {
                    track.stop();
                } catch (Exception ignored) {
                }

                track.release();
            }
        }
    }

    public void releaseResources() {

        sliding = false;

        stopTrack();

        audioExecutor.shutdownNow();
    }
}
