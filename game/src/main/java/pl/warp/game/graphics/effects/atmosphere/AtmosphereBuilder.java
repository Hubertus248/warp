package pl.warp.game.graphics.effects.atmosphere;

import org.joml.Vector3f;
import pl.warp.engine.graphics.CustomRendererProperty;
import pl.warp.game.GameContext;
import pl.warp.game.graphics.effects.GameComponentBuilder;
import pl.warp.game.graphics.effects.star.StarContextProperty;
import pl.warp.game.scene.GameComponent;
import pl.warp.game.scene.GameScene;
import pl.warp.game.scene.GameSceneComponent;

/**
 * @author Jaca777
 *         Created 2017-03-12 at 11
 */
public class AtmosphereBuilder implements GameComponentBuilder {


    private GameComponent parent;
    private Vector3f color = new Vector3f(1.0f);
    private float radius = 1.07f;
    private GameContext context;

    public AtmosphereBuilder(GameComponent parent) {
        this.parent = parent;
        this.context = parent.getContext();
    }

    public AtmosphereBuilder setColor(Vector3f color) {
        this.color = color;
        return this;
    }

    public AtmosphereBuilder setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public GameComponent build() {
        GameComponent atmosphere = new GameSceneComponent(parent);
        parent.addProperty(new AtmosphereProperty(color, radius));
        AtmosphereContextProperty contextProperty = getContextProperty();
        parent.addProperty(new CustomRendererProperty(contextProperty.getRenderer()));
        return atmosphere;
    }

    private AtmosphereContextProperty getContextProperty() {
        GameScene scene = context.getScene();
        if (scene.hasEnabledProperty(StarContextProperty.STAR_CONTEXT_PROPERTY_NAME))
            return scene.getProperty(StarContextProperty.STAR_CONTEXT_PROPERTY_NAME);
        else return createContext();
    }

    private AtmosphereContextProperty createContext() {
        AtmosphereProgram atmosphereProgram = new AtmosphereProgram();
        AtmosphereRenderer atmosphereRenderer = new AtmosphereRenderer(context.getGraphics(), atmosphereProgram);
        AtmosphereContextProperty property = new AtmosphereContextProperty(atmosphereRenderer, atmosphereProgram);
        context.getScene().addProperty(property);
        return property;
    }

}