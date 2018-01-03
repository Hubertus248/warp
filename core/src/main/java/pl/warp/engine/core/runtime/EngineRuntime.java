package pl.warp.engine.core.runtime;

import org.objectweb.asm.tree.ClassNode;
import pl.warp.engine.core.event.Event;
import pl.warp.engine.core.property.Property;
import pl.warp.engine.core.runtime.idcall.IdCallProcessor;
import pl.warp.engine.core.runtime.idmethodgen.IdCodeGeneratorProcessor;
import pl.warp.engine.core.runtime.preprocessing.EngineRuntimePreprocessor;
import pl.warp.engine.core.runtime.processing.ComposedProcessor;
import pl.warp.engine.core.runtime.processing.Processor;

/**
 * @author Jaca777
 * Created 2017-12-20 at 23
 */
public class EngineRuntime {

    public static final String PREFIX = "pl.warp";
    public static final String PROPERTY_CLASS_NAME = "pl/warp/engine/core/property/Property";
    public static final String EVENT_CLASS_NAME = "pl/warp/engine/core/event/Event";

    private static final Class[] preprocessedTypes = {Property.class, Event.class};
    private static final EngineRuntimePreprocessor PREPROCESSOR = new EngineRuntimePreprocessor(preprocessedTypes);
    private static final Processor PROCESSOR = createProcessor();

    private static Processor<ClassNode> createProcessor() {
        IdCodeGeneratorProcessor propertyIdGenerator = new IdCodeGeneratorProcessor(PROPERTY_CLASS_NAME, PREPROCESSOR);
        IdCodeGeneratorProcessor eventIdGenerator = new IdCodeGeneratorProcessor(EVENT_CLASS_NAME, PREPROCESSOR);
        IdCallProcessor idCallProcessor = new IdCallProcessor(preprocessedTypes, PREPROCESSOR);
        return new ComposedProcessor(propertyIdGenerator, eventIdGenerator, idCallProcessor);
    }

    public void load() {
        EngineClassLoader classLoader = (EngineClassLoader) Thread.currentThread().getContextClassLoader();
        PREPROCESSOR.preprocess();
        startProcessing(classLoader);
    }

    private void startProcessing(EngineClassLoader classLoader) {
        PROCESSOR.initializeProcessing();
        classLoader.startProcessing(PROCESSOR);
    }
}