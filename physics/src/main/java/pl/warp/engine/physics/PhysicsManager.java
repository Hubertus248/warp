package pl.warp.engine.physics;

import pl.warp.engine.common.transform.TransformProperty;
import pl.warp.engine.core.component.Component;
import pl.warp.engine.core.context.annotation.Service;
import pl.warp.engine.core.property.PropertyNotPresentException;

/**
 * @author Hubertus
 * Created 22.09.2017
 */
@Service
public class PhysicsManager {

    private PhysicsWorld mainWorld;

    public PhysicsManager(PhysicsTask physicsTask) {
        mainWorld = physicsTask.getMainWorld();
    }


    public void addRigidBody(Component component) {
        if (!component.hasEnabledProperty(TransformProperty.TRANSFORM_PROPERTY_NAME))
            throw new PropertyNotPresentException(TransformProperty.TRANSFORM_PROPERTY_NAME);

        if (!component.hasEnabledProperty(PhysicsProperty.PHYSICS_PROPERTY_NAME))
            throw new PropertyNotPresentException(PhysicsProperty.PHYSICS_PROPERTY_NAME);
        PhysicsProperty physicsProperty = component.getProperty(PhysicsProperty.PHYSICS_PROPERTY_NAME);
        mainWorld.getDynamicsWorld().addRigidBody(physicsProperty.getRigidBody());
    }
}
