package pl.warp.ide.engine;

import javafx.scene.canvas.Canvas;
import pl.warp.engine.ai.AITask;
import pl.warp.engine.audio.*;
import pl.warp.engine.core.*;
import pl.warp.engine.core.scene.Component;
import pl.warp.engine.core.scene.Scene;
import pl.warp.engine.core.scene.input.Input;
import pl.warp.engine.core.scene.input.InputTask;
import pl.warp.engine.core.scene.script.ScriptTask;
import pl.warp.engine.graphics.Graphics;
import pl.warp.engine.graphics.RenderingConfig;
import pl.warp.engine.graphics.camera.Camera;
import pl.warp.engine.graphics.camera.CameraProperty;
import pl.warp.engine.graphics.pipeline.output.OutputTexture2DRenderer;
import pl.warp.engine.graphics.window.Display;
import pl.warp.engine.physics.DefaultCollisionStrategy;
import pl.warp.engine.physics.MovementTask;
import pl.warp.engine.physics.PhysicsTask;
import pl.warp.engine.physics.RayTester;
import pl.warp.game.GameContextBuilder;
import pl.warp.game.scene.GameComponent;
import pl.warp.game.scene.GameScene;

import java.io.File;


/**
 * @author Jaca777
 *         Created 2017-01-22 at 13
 *         Starts the tasks
 */
public class IDEEngineTaskManager {

    private SceneViewRenderer sceneViewRenderer;
    private GameScene loadedScene;
    private GameComponent cameraComponent;
    private Camera camera;

    private Graphics graphics;
    private RenderingConfig config;
    private GameContextBuilder contextBuilder;

    private Input input;
    private RayTester rayTester;

    public IDEEngineTaskManager(SceneViewRenderer sceneViewRenderer, GameScene scene, GameComponent cameraComponent, RenderingConfig config, GameContextBuilder contextBuilder, Input input) {
        this.sceneViewRenderer = sceneViewRenderer;
        this.loadedScene = scene;
        this.cameraComponent = cameraComponent;
        this.camera = cameraComponent.<CameraProperty>getProperty(CameraProperty.CAMERA_PROPERTY_NAME).getCamera();
        this.config = config;
        this.contextBuilder = contextBuilder;
        this.input = input;
    }

    public void startTasks(Canvas destCanvas) {
        setRenderingTargetSize((int) destCanvas.getWidth(), (int) destCanvas.getHeight());
        OutputTexture2DRenderer outputRenderer = new OutputTexture2DRenderer();
        graphics = createGraphics(contextBuilder.getGameContext(), outputRenderer);
        contextBuilder.setGraphics(graphics);
        startTasks(contextBuilder.getGameContext(), graphics, loadedScene, destCanvas);
    }

    private void setRenderingTargetSize(int width, int height) {
        Display display = this.config.getDisplay();
        display.setWidth(width + 2);
        display.setHeight(height + 2);
    }


    private Graphics createGraphics(EngineContext context, OutputTexture2DRenderer outputRenderer) {
        return new Graphics(context, outputRenderer, camera, config);
    }

    private void startTasks(EngineContext context, Graphics graphics, Scene scene, Canvas destCanvas) {
        createScriptThread(input, graphics.getThread());
        createPhysicsThread(scene, graphics.getThread());
        createAudioThread();
        createAIThread(scene);
        createInputTask();
        initContext();
        graphics.create();
        OutputTexture2DRenderer output = (OutputTexture2DRenderer) graphics.getOutput();
        sceneViewRenderer.startRendering(output.getOutput(), destCanvas);
    }

    private void initContext() {
        contextBuilder.setCamera(camera);
        contextBuilder.setScene(loadedScene);
        contextBuilder.setRayTester(rayTester);
        contextBuilder.setInput(input);
    }

    private void createInputTask() {
        graphics.getThread().scheduleTask(new InputTask(input));
    }

    private void createPhysicsThread(Component root, EngineThread graphicsThread) {
        EngineThread physicsThread = new SyncEngineThread(new SyncTimer(60), new RapidExecutionStrategy());
        rayTester = new RayTester();
        physicsThread.scheduleTask(new MovementTask(root));
        physicsThread.scheduleTask(new PhysicsTask(new DefaultCollisionStrategy(), root, rayTester));
        graphicsThread.scheduleOnce(physicsThread::start);
    }

    private void createScriptThread(Input input, EngineThread graphicsThread) {
        EngineThread scriptThread = new SyncEngineThread(new SyncTimer(60), new RapidExecutionStrategy());
        scriptThread.scheduleTask(new ScriptTask(contextBuilder.getGameContext().getScriptManager()));
        graphicsThread.scheduleOnce(() -> {
            contextBuilder.setInput(input);
            //TODO start input task
            scriptThread.start(); //has to start after the window is created
        });
    }

    private void createAIThread(Component root) {
        EngineThread aiThread = new SyncEngineThread(new SyncTimer(60), new RapidExecutionStrategy());
        aiThread.scheduleOnce(() -> aiThread.scheduleTask(new AITask(root)));
        aiThread.start();
    }

    private void createAudioThread() {
        AudioContext audioContext = new AudioContext();
        audioContext.setAudioListener(new AudioListener(cameraComponent));
        AudioManager audioManager = new AudioManager(audioContext);
        EngineThread audioThread = new SyncEngineThread(new SyncTimer(60), new RapidExecutionStrategy());
        audioThread.scheduleTask(new AudioTask(audioContext));
        audioThread.scheduleTask(new AudioPosUpdateTask(audioContext));
        audioThread.scheduleOnce(() -> {
            audioManager.loadFiles("data" + File.separator + "sound" + File.separator + "effects"); //TODO move it
        });
        audioThread.start();
    }

    public RayTester getRayTester() {
        return rayTester;
    }

    public Graphics getGraphics() {
        return graphics;
    }
}