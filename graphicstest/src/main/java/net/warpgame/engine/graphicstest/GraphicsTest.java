package net.warpgame.engine.graphicstest;

import net.warpgame.engine.core.component.Component;
import net.warpgame.engine.core.component.SceneComponent;
import net.warpgame.engine.core.component.SceneHolder;
import net.warpgame.engine.core.context.EngineContext;
import net.warpgame.engine.core.execution.EngineThread;
import net.warpgame.engine.core.property.TransformProperty;
import net.warpgame.engine.core.runtime.EngineRuntime;
import net.warpgame.engine.graphics.camera.CameraHolder;
import net.warpgame.engine.graphics.camera.CameraProperty;
import net.warpgame.engine.graphics.memory.scene.material.MaterialProperty;
import net.warpgame.engine.graphics.memory.scene.material.Texture;
import net.warpgame.engine.graphics.memory.scene.mesh.MeshProperty;
import net.warpgame.engine.graphics.memory.scene.mesh.StaticMesh;
import net.warpgame.engine.graphics.window.Window;

import java.io.File;
import java.util.List;

/**
 * @author MarconZet
 * Created 06.05.2019
 */
public class GraphicsTest {

    private static EngineContext context;
    private static Component dragon;
    private static Component camera;

    public static void start(EngineRuntime engineRuntime) {
        System.out.println();
        context = new EngineContext( "graphics", "local");
        context.getLoadedContext().addService(engineRuntime.getIdRegistry());
        SceneHolder sceneHolder = context.getLoadedContext().findOne(SceneHolder.class).get();
        try {
            createScene(sceneHolder.getScene());
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        Window window = context.getLoadedContext().findOne(Window.class).get();
        try {
            while (!window.shouldClose()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        close();
    }

/*    public static void main(String... arg){
        System.exit(0);
    }*/

    private static void createScene(Component scene){
        dragon = createDragon(scene);
        camera = createCamera(scene);
    }

    private static Component createCamera(Component parent){
        Component camera = new SceneComponent(parent);

        CameraProperty cameraProperty = new CameraProperty(CameraProperty.CameraType.PERSPECTIVE, (float) Math.PI/2, 600, 400, 10, 1000);
        camera.addProperty(cameraProperty);

        TransformProperty transformProperty = new TransformProperty();
        camera.addProperty(transformProperty);

        parent.getContext().getLoadedContext().findOne(CameraHolder.class).get().setCamera(camera);

        return camera;
    }

    private static Component createDragon(Component parent) {
        Component dragon = new SceneComponent(parent);

        File meshSource = new File(GraphicsTest.class.getResource("dragon.obj").getFile());
        StaticMesh mesh = new StaticMesh(meshSource);
        MeshProperty meshProperty = new MeshProperty(mesh);
        dragon.addProperty(meshProperty);

        File texSource = new File(GraphicsTest.class.getResource("tex.png").getFile());
        Texture texture = new Texture(texSource);
        MaterialProperty materialProperty = new MaterialProperty(texture);
        dragon.addProperty(materialProperty);

        TransformProperty transformProperty = new TransformProperty();
        transformProperty.move(0, 0, -10);
        dragon.addProperty(transformProperty);

        return dragon;
    }

    private static void close() {
        List<EngineThread> engineThreads = context.getLoadedContext().findAll(EngineThread.class);
        engineThreads.forEach(EngineThread::interrupt);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
