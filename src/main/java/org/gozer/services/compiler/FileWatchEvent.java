package org.gozer.services.compiler;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class FileWatchEvent {
    private final Path dir;
    private final Path path;
    private final WatchEvent.Kind kind;
    private final int count;

    public FileWatchEvent(Path dir, Path path, WatchEvent.Kind kind, int count) {
        this.dir = dir;
        this.path = path;
        this.kind = kind;
        this.count = count;
    }

    public Path getDir() {
        return dir;
    }

    public Path getPath() {
        return path;
    }

    public WatchEvent.Kind getKind() {
        return kind;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "FileWatchEvent{" +
                "dir=" + dir +
                ", path=" + path +
                ", kind=" + kind +
                ", count=" + count +
                '}';
    }
}