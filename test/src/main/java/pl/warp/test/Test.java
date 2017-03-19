package pl.warp.test;

import pl.warp.engine.ai.AIManager;
import pl.warp.engine.ai.AITask;
import pl.warp.engine.audio.*;
import pl.warp.engine.core.EngineThread;
import pl.warp.engine.core.RapidExecutionStrategy;
import pl.warp.engine.core.SyncEngineThread;
import pl.warp.engine.core.SyncTimer;
import pl.warp.engine.core.scene.script.ScriptTask;
import pl.warp.engine.graphics.Graphics;
import pl.warp.engine.graphics.RenderingConfig;
import pl.warp.engine.graphics.camera.Camera;
import pl.warp.engine.graphics.camera.CameraProperty;
import pl.warp.engine.graphics.input.glfw.GLFWInput;
import pl.warp.engine.graphics.input.glfw.GLFWInputTask;
import pl.warp.engine.graphics.pipeline.rendering.OnScreenRenderer;
import pl.warp.engine.graphics.window.GLFWWindowManager;
import pl.warp.engine.physics.DefaultCollisionStrategy;
import pl.warp.engine.physics.MovementTask;
import pl.warp.engine.physics.PhysicsTask;
import pl.warp.engine.physics.RayTester;
import pl.warp.game.GameContext;
import pl.warp.game.GameContextBuilder;
import pl.warp.game.scene.GameComponent;
import pl.warp.game.scene.GameScene;
import pl.warp.game.scene.GameSceneLoader;
import pl.warp.game.script.CameraRayTester;
import pl.warp.game.script.GameScript;
import pl.warp.game.script.GameScriptManager;

import java.awt.event.KeyEvent;

/**
 * @author Jaca777
 *         Created 2016-06-27 at 14
 *         CANCER CODE, ONLY FOR TESTING
 *         TODO KILL IT WITH FIRE
 *
 *         MarconZet
 *         "Always code as if the guy who ends up maintaining your code will be a violent psychopath who knows where you live."
 */

public class Test {
    public static void runTest(RenderingConfig config, int sceneToLoad) {

        GameContextBuilder contextBuilder = new GameContextBuilder();
        GameContext context = contextBuilder.getGameContext();

        contextBuilder.setScriptManager(new GameScriptManager());
        OnScreenRenderer onScreenRenderer = new OnScreenRenderer(config);

        AudioContext audioContext = new AudioContext();
        AudioManager.INSTANCE = new AudioManager(audioContext);

        GameSceneLoader loader = getGameSceneLoader(config, contextBuilder, sceneToLoad);
        loader.loadScene();
        GameComponent cameraComponent = loader.getMainCameraComponent();
        Camera camera = cameraComponent.<CameraProperty>getProperty(CameraProperty.CAMERA_PROPERTY_NAME).getCamera();
        //new GoatControlScript(cameraComponent.getParent(), MOV_SPEED, ROT_SPEED, BRAKING_FORCE, ARROWS_ROTATION_SPEED);
        GameScene scene = loader.getScene();
        GLFWInput input = new GLFWInput(scene);
        audioContext.setAudioListener(new AudioListener(cameraComponent.getParent()));
        Graphics graphics = new Graphics(context, onScreenRenderer, camera, config);
        contextBuilder.setGraphics(graphics);
        EngineThread graphicsThread = graphics.getThread();
        graphics.enableUpsLogging();
        loader.loadGraphics(graphicsThread);


        EngineThread scriptsThread = new SyncEngineThread(new SyncTimer(60), new RapidExecutionStrategy());
        graphicsThread.scheduleOnce(() -> {
            contextBuilder.setInput(input);
            scriptsThread.scheduleTask(new ScriptTask(context.getScriptManager()));
            GLFWWindowManager windowManager = graphics.getWindowManager();
            scriptsThread.scheduleTask(new GLFWInputTask(input, windowManager));
            scriptsThread.start(); //has to start after the window is created
        });


        EngineThread physicsThread = new SyncEngineThread(new SyncTimer(60), new RapidExecutionStrategy());
        RayTester rayTester = new RayTester();
        contextBuilder.setRayTester(new CameraRayTester(context, rayTester));
        contextBuilder.setAIManager(new AIManager());
        physicsThread.scheduleTask(new MovementTask(scene));
        physicsThread.scheduleTask(new PhysicsTask(new DefaultCollisionStrategy(), scene, rayTester));


        EngineThread audioThread = new SyncEngineThread(new SyncTimer(60), new RapidExecutionStrategy());
        audioThread.scheduleTask(new AudioTask(audioContext));
        audioThread.scheduleTask(new AudioPosUpdateTask(audioContext));

        loader.loadSound(audioThread);

        audioThread.start();

        EngineThread aiThread = new SyncEngineThread(new SyncTimer(60), new RapidExecutionStrategy());

        aiThread.scheduleTask(new AITask(context.getAIManager(), scene));
        aiThread.start();
        new GameScript(scene) {
            @Override
            public void init() {

            }

            @Override
            public void update(int delta) {
                if (input.isKeyDown(KeyEvent.VK_ESCAPE)) {
                    scriptsThread.scheduleOnce(scriptsThread::interrupt);
                    graphicsThread.scheduleOnce(graphicsThread::interrupt);
                    physicsThread.scheduleOnce(physicsThread::interrupt);
                    System.exit(0);
                }
            }
        };
        graphicsThread.scheduleOnce(physicsThread::start);
        graphics.create();
    }

    private static GameSceneLoader getGameSceneLoader(RenderingConfig config, GameContextBuilder contextBuilder, int sceneToLoad) {
        switch (sceneToLoad) {
            case 0:
                return new SpaceSceneLoader(config, contextBuilder);
            case 1:
                return new GroundSceneLoader(config, contextBuilder);
            default:
                return new SpaceSceneLoader(config, contextBuilder);
        }
    }

}
