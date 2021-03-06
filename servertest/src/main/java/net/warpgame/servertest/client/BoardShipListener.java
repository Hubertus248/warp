package net.warpgame.servertest.client;

import net.warpgame.content.BoardShipEvent;
import net.warpgame.engine.core.component.Component;
import net.warpgame.engine.core.component.ComponentRegistry;
import net.warpgame.engine.core.component.SceneComponent;
import net.warpgame.engine.core.event.Event;
import net.warpgame.engine.core.event.Listener;
import net.warpgame.engine.core.property.TransformProperty;
import net.warpgame.engine.graphics.camera.CameraHolder;
import net.warpgame.engine.graphics.camera.CameraProperty;
import net.warpgame.engine.graphics.rendering.screenspace.light.LightSourceProperty;
import net.warpgame.engine.graphics.rendering.screenspace.light.SceneLightManager;
import net.warpgame.engine.graphics.window.Display;
import net.warpgame.servertest.client.scripts.CameraZoomControlScript;
import net.warpgame.servertest.client.scripts.GunScript;
import net.warpgame.servertest.client.scripts.MultiplayerCameraControlScript;
import net.warpgame.servertest.client.scripts.MultiplayerControlScript;
import org.joml.Vector3f;

/**
 * @author Hubertus
 * Created 05.01.2018
 */
public class BoardShipListener extends Listener<BoardShipEvent> {
    private final CameraHolder cameraHolder;
    private Display display;
    private final ComponentRegistry componentRegistry;
    private final SceneLightManager lightManager;

    protected BoardShipListener(Component owner,
                                CameraHolder cameraHolder,
                                Display display,
                                ComponentRegistry componentRegistry,
                                SceneLightManager lightManager) {
        super(owner, Event.getTypeId(BoardShipEvent.class));
        this.cameraHolder = cameraHolder;
        this.display = display;
        this.componentRegistry = componentRegistry;
        this.lightManager = lightManager;
    }

    @Override
    public void handle(BoardShipEvent event) {
        Component shipComponent = componentRegistry.getComponent(event.getShipComponentId());
        shipComponent.addProperty(new PlayerProperty());
        shipComponent.addScript(MultiplayerControlScript.class);
        shipComponent.addScript(GunScript.class);

        Component cameraPivot = new SceneComponent(shipComponent);
        TransformProperty pivotTransform = new TransformProperty().move(0, 2, 0);
        cameraPivot.addProperty(pivotTransform);
        cameraPivot.addScript(MultiplayerCameraControlScript.class);

        Component cameraComponent = new SceneComponent(cameraPivot);
        TransformProperty cameraTransform = new TransformProperty();
        cameraTransform.move(new Vector3f(20, 0, 0)).rotateY((float) (Math.PI / 2));
        cameraComponent.addProperty(cameraTransform);
        cameraComponent.addScript(CameraZoomControlScript.class);

        CameraProperty cameraProperty = new CameraProperty(CameraProperty.CameraType.PERSPECTIVE,55f, display.getWidth(), display.getHeight(), 0.1f, 10000f);
        cameraComponent.addProperty(cameraProperty);
        cameraHolder.setCamera(cameraComponent);

        Component light = new SceneComponent(shipComponent);
        light.addProperty(new TransformProperty().move(0f, 1f, 0f));
        LightSourceProperty lightSourceProperty = new LightSourceProperty(new Vector3f(1.3f, 1.3f, 1.3f).mul(20));
        light.addProperty(lightSourceProperty);
        lightManager.addLight(lightSourceProperty);
    }
}
