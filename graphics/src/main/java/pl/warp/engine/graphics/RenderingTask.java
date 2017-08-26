package pl.warp.engine.graphics;

import org.apache.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import pl.warp.engine.core.execution.task.EngineTask;
import pl.warp.engine.graphics.pipeline.Pipeline;
import pl.warp.engine.graphics.window.Display;
import pl.warp.engine.graphics.window.WindowManager;

/**
 * @author Jaca777
 *         Created 2016-06-25 at 21
 */
public class RenderingTask extends EngineTask {

    private static Logger logger = Logger.getLogger(RenderingTask.class);

    private Display display;
    private WindowManager windowManager;
    private Pipeline pipeline;

    public RenderingTask(Display display, WindowManager windowManager, Pipeline pipeline) { //TODO resize listener
        this.display = display;
        this.windowManager = windowManager;
        this.pipeline = pipeline;
    }

    @Override
    protected void onInit() {
        logger.info("Initializing rendering task.");
        createOpenGL();
        logger.info("OpenGL capabilities created.");
        pipeline.init();
        logger.info("Initialized pipeline.");
    }


    private void createOpenGL() {
        GL.createCapabilities();
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    protected void onClose() {
        pipeline.destroy();
        windowManager.closeWindow();
    }

    @Override
    public void update(int delta) {
        pipeline.update();
        GLErrors.checkOGLErrors();
    }



}
