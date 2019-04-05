package net.warpgame.engine.graphics;

import net.warpgame.engine.core.context.service.Profile;
import net.warpgame.engine.core.context.service.Service;
import net.warpgame.engine.core.context.task.RegisterTask;
import net.warpgame.engine.core.execution.task.EngineTask;

import static net.warpgame.engine.graphics.ZerviceBypass.ENABLE_VALIDATION_LAYERS;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;

/**
 * @author MarconZet
 * Created 05.04.2019
 */

@Service
@Profile("graphics")
@RegisterTask(thread = "graphics")
public class VulkanTask extends EngineTask {
    private Instance instance;
    private DebugCallback debugCallback;

    public VulkanTask(Instance instance, DebugCallback debugCallback) {
        this.instance = instance;
        this.debugCallback = debugCallback;
    }

    @Override
    protected void onInit() {
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }
        if (!glfwVulkanSupported()) {
            throw new AssertionError("GLFW failed to find the Vulkan loader");
        }

        instance.create();
        if(ENABLE_VALIDATION_LAYERS)
            debugCallback.create();
    }

    @Override
    public void update(int delta) {

    }

    @Override
    protected void onClose() {
        if(ENABLE_VALIDATION_LAYERS)
            debugCallback.destroy();
        instance.destroy();
    }
}
