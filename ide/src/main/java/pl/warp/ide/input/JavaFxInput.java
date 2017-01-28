package pl.warp.ide.input;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.joml.Vector2f;
import pl.warp.engine.core.scene.input.*;
import pl.warp.game.scene.GameScene;

import static java.awt.event.MouseEvent.NOBUTTON;

/**
 * @author Jaca777
 *         Created 2017-01-22 at 12
 */
public class JavaFxInput implements Input {

    private volatile boolean[] keyboardKeys = new boolean[349];
    private volatile boolean[] mouseButtons = new boolean[8];

    private Vector2f lastCursorPos = new Vector2f(0, 0);
    private Vector2f cursorPosition = null;
    private Vector2f cursorPositionDelta = new Vector2f(0, 0);
    private Canvas canvas;
    private GameScene scene;

    public void onKeyReleased(KeyEvent event) {
        int keyCode = JavaFxKeyMapper.toAWTKeyCode(event.getCode());
        if (keyCode > 348) return;
        else keyboardKeys[keyCode] = false;
        scene.triggerEvent(new KeyReleasedEvent(keyCode));
    }

    public void onKeyPressed(KeyEvent event) {
        int keyCode = JavaFxKeyMapper.toAWTKeyCode(event.getCode());
        if (keyCode > 348) return;
        keyboardKeys[keyCode] = true;
        scene.triggerEvent(new KeyPressedEvent(keyCode));
    }

    public void onMouseMoved(MouseEvent event) {
        lastCursorPos.set((float) event.getX(), (float) event.getY());
        if (cursorPosition == null) cursorPosition = new Vector2f(lastCursorPos);
    }

    public void onMousePressed(MouseEvent event) {
        int buttonCode = JavaFxKeyMapper.toAWTButton(event.getButton());
        if (buttonCode == NOBUTTON)
            return; //button unrecognized
        mouseButtons[buttonCode] = true;
        scene.triggerEvent(new MouseButtonPressedEvent(buttonCode));
    }


    public void onMouseReleased(MouseEvent event) {
        int buttonCode = JavaFxKeyMapper.toAWTButton(event.getButton());
        if (buttonCode == NOBUTTON)
            return; //button unrecognized
        mouseButtons[buttonCode] = false;
        scene.triggerEvent(new MouseButtonReleasedEvent(buttonCode));
    }

    @Override
    public void update() {
        if (canvas == null) return;
        if (cursorPosition == null) cursorPosition = new Vector2f(lastCursorPos);
        this.lastCursorPos.sub(cursorPosition, cursorPositionDelta);
        this.cursorPositionDelta.mul(1.0f / (float) canvas.getWidth(), 1.0f / (float) canvas.getHeight());
        this.cursorPosition.set(lastCursorPos);
    }

    @Override
    public Vector2f getCursorPosition() {
        return cursorPosition;
    }

    @Override
    public Vector2f getCursorPositionDelta() {
        return cursorPositionDelta;
    }

    @Override
    public boolean isKeyDown(int key) {
        return keyboardKeys[key];
    }

    @Override
    public boolean isMouseButtonDown(int button) {
        return mouseButtons[button];
    }

    public void listenOn(AnchorPane root, Canvas canvas, GameScene scene) {
        this.canvas = canvas;
        this.scene = scene;
        canvas.setOnMouseMoved(this::onMouseMoved);
        canvas.setOnMouseDragged(this::onMouseMoved);
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseReleased(this::onMouseReleased);
        root.setOnKeyPressed(this::onKeyPressed);
        root.setOnKeyReleased(this::onKeyReleased);
    }
}
