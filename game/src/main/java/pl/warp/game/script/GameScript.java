package pl.warp.game.script;

import pl.warp.engine.core.scene.Script;
import pl.warp.game.scene.GameComponent;
import pl.warp.game.GameContext;

/**
 * @author Jaca777
 *         Created 2017-01-27 at 17
 */
public abstract class GameScript<T extends GameComponent> extends Script<T> {
    private GameContext gameContext;
    private GameInputHandler inputHandler;

    public GameScript(T owner) {
        super(owner);
        this.gameContext = owner.getContext();
    }

    @Override
    protected GameContext getContext() {
        return gameContext;
    }


    @Override
    public void onInit() {
        init();
    }

    protected abstract void init();

    @Override
    public void onUpdate(int delta) {
        update(delta);
    }

    protected abstract void update(int delta);
}
