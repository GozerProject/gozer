package gozer.services.compiler;

import gozer.services.CompilationService;
import org.joda.time.DateTime;

public class CompilationFinishedEvent {
    private final CompilationService compilationManager;
    private final DateTime endTime;

    public CompilationFinishedEvent(CompilationService compilationManager, DateTime endTime) {
        this.compilationManager = compilationManager;
        this.endTime = endTime;
    }

    public CompilationService getCompilationManager() {
        return compilationManager;
    }

    public DateTime getEndTime() {
        return endTime;
    }
}
