package pinball;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Optional best-score persistence; a null path gives an in-memory session. */
final class ScoreStore {
    private final Path path;
    ScoreStore(Path path) {
        this.path = path;
    }

    long load() {
        if (path == null) return 0;
        try {
            return Long.parseLong(Files.readString(path).trim());
        }
        catch (IOException | NumberFormatException ignored) {
            return 0;
        }
    }

    void save(long score) {
        if (path == null) return;
        try {
            Files.writeString(path, Long.toString(score));
        }
        catch (IOException ignored) {
            /* A run remains playable without persistence. */
        }
    }
}
