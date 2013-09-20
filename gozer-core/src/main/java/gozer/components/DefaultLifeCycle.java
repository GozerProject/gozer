package gozer.components;

import gozer.api.Lifecycle;
import gozer.model.Project;
import restx.factory.Component;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

@Component
public class DefaultLifeCycle {

    private NavigableMap<Project.Status, Lifecycle> lifecycleServices = new TreeMap<>();


    public DefaultLifeCycle() {
    }

    public void register(Lifecycle lifecycle) {
        lifecycleServices.put(lifecycle.getTo(), lifecycle);
    }

    public LifeCycleEntry getNextStatus(Project.Status status) {
        Map.Entry<Project.Status, Lifecycle> statusLifecycleEntry = lifecycleServices.ceilingEntry(status);
        return new LifeCycleEntry(statusLifecycleEntry.getKey(), statusLifecycleEntry.getValue());
    }

}
