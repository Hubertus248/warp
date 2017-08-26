package pl.warp.test;

import pl.warp.engine.core.component.Component;
import pl.warp.engine.core.property.Property;
import pl.warp.engine.game.scene.GameComponent;

import java.util.ArrayList;

/**
 * @author Hubertus
 *         Created 24.01.17
 */
public class DroneProperty extends Property{

    private int hitPoints;
    private int team;
    private ArrayList<Component> targetList;

    public static final String DRONE_PROPERTY_NAME = "droneProperty";
    private GameComponent respawn;

    public DroneProperty(int hitPoints, int team, ArrayList<Component> targetList, GameComponent respawn) {
        super(DRONE_PROPERTY_NAME);
        this.hitPoints = hitPoints;
        this.team = team;
        this.targetList = targetList;
        this.respawn = respawn;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public int getTeam() {
        return team;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public ArrayList<Component> getTargetList() {
        return targetList;
    }

    public GameComponent getRespawn() {
        return respawn;
    }
}
