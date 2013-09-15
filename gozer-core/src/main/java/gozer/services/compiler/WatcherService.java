package gozer.services.compiler;

import com.google.common.eventbus.EventBus;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public interface WatcherService {
    Closeable watch(EventBus eventBus, ExecutorService executor, Path dir, boolean recurse);
    boolean isEnabled();
}