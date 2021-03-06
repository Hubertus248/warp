package net.warpgame.engine.core.context.executor;

import net.warpgame.engine.core.context.ServiceRegistry;
import net.warpgame.engine.core.context.service.Service;
import net.warpgame.engine.core.execution.EngineThread;
import net.warpgame.engine.core.execution.Executor;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jaca777
 * Created 2017-09-23 at 15
 */
@Service
public class ExecutorManager implements ServiceRegistry {

    private Map<String, Executor> executors = new TreeMap<>();

    @Override
    public void registerService(Object service) {
        Class<?> serviceClass = service.getClass();
        if(isRegistrableExecutor(serviceClass)) {
            addExecutor((Executor) service);
        }
    }

    private boolean isRegistrableExecutor(Class<?> serviceClass) {
        RegisterExecutor annotation = serviceClass.getAnnotation(RegisterExecutor.class);
        if(annotation != null) {
            if(executors.containsKey(annotation.value()))
                throw new ExecutorLoadingException(
                        "Executor named " + annotation.value() + " has been already defined.",
                        serviceClass
                );
            if(Executor.class.isAssignableFrom(serviceClass))
                return true;
            else throw new ExecutorLoadingException("Service class doesn't extend a Executor", serviceClass);
        } else return false;
    }

    private void addExecutor(Executor service) {
        RegisterExecutor registerExecutor = service.getClass().getAnnotation(RegisterExecutor.class);
        executors.put(registerExecutor.value(), service);
    }

    private boolean isThread(Object executor) {
        return EngineThread.class.isAssignableFrom(executor.getClass());
    }

    public Executor getExecutor(String name) {
        return executors.get(name);
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
