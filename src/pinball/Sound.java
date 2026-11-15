package pinball;

import javax.sound.sampled.*;
import java.util.concurrent.*;

/** Procedural arcade tones; audio is optional when no device is available. */
final class Sound {
    private static final ExecutorService WORKER =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "arcade-audio");
                t.setDaemon(true);
                return t;
            });
    private static volatile long last;
    private static volatile boolean unavailable;

    static void play(int hz) {
        long now = System.nanoTime();
        if (unavailable || now - last < 70_000_000) return;
        last = now;
        WORKER.execute(() -> {
            try {
                AudioFormat format = new AudioFormat(22050, 16, 1, true, false);
                byte[] data = new byte[2646];
                for (int i = 0; i < data.length / 2; i++) {
                    double t = i / 22050.;
                    short v =
                            (short)
                                    (Math.sin(2 * Math.PI * (hz * t - 700 * t * t))
                                            * 5000
                                            * Math.exp(-t * 55));
                    data[2 * i] = (byte) v;
                    data[2 * i + 1] = (byte) (v >> 8);
                }
                Clip clip = AudioSystem.getClip();
                clip.open(format, data, 0, data.length);
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
                clip.start();
            } catch (Exception e) {
                unavailable = true;
            }
        });
    }
}
