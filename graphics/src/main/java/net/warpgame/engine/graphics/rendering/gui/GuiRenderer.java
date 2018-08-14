package net.warpgame.engine.graphics.rendering.gui;

import net.warpgame.engine.core.context.service.Profile;
import net.warpgame.engine.core.context.service.Service;
import net.warpgame.engine.graphics.framebuffer.TextureFramebuffer;
import net.warpgame.engine.graphics.mesh.shapes.QuadMesh;
import net.warpgame.engine.graphics.program.ShaderCompilationException;
import net.warpgame.engine.graphics.rendering.gui.program.GuiProgram;
import net.warpgame.engine.graphics.rendering.screenspace.ScreenspaceAlbedoHolder;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Profile("graphics")
public class GuiRenderer {
    private static final Logger logger = LoggerFactory.getLogger(GuiRenderer.class);

    private ScreenspaceAlbedoHolder screenspaceAlbedoHolder;

    private GuiProgram guiProgram;
    private QuadMesh quad;

    private TextureFramebuffer destinationFramebuffer;

    private GuiTest guiTest;

    public GuiRenderer(ScreenspaceAlbedoHolder screenspaceAlbedoHolder, GuiTest guiTest) {
        this.screenspaceAlbedoHolder = screenspaceAlbedoHolder;
        this.guiTest = guiTest;
    }

    public void init(){
        this.quad = new QuadMesh();
        try {
            guiProgram = new GuiProgram();
        }catch(ShaderCompilationException e) {
            logger.error("Failed to compile gui rendering program");
        }
        this.destinationFramebuffer = screenspaceAlbedoHolder.getAlbedoTextureFramebuffer();
    }

    public void update() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        prepareFramebuffer();
        prepareProgram();
        renderGui();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

    }

    protected void prepareFramebuffer() {
        destinationFramebuffer.bindDraw();
    }

    protected void prepareProgram() {
        guiProgram.use();
    }

    protected void renderGui() {
        if (guiTest.texture2D == null || guiTest.matrix3f == null) {
            return;
        }
        guiProgram.useTexture(guiTest.texture2D);
        guiProgram.useMatrix(guiTest.matrix3f);
        quad.draw();
    }

    public void destroy() {
        quad.destroy();
    }
}
