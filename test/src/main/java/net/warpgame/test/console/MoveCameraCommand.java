package net.warpgame.test.console;

import net.warpgame.engine.common.transform.TransformProperty;
import net.warpgame.engine.graphics.camera.CameraHolder;
import org.joml.Vector3f;

/**
 * @author KocproZ
 * Created 2018-01-10 at 21:02
 */
public class MoveCameraCommand extends Command {

    private CameraHolder cameraHolder; //TODO make output available

    public MoveCameraCommand(CameraHolder holder, ConsoleService consoleService) {
        super("move", Side.CLIENT, "Moves camera", "move [x] [y] [z]");
        this.cameraHolder = holder;

        consoleService.registerVariable(new CommandVariable("camera", holder));
    }

    public void execute(String... args) {
        if (args.length == 3) {
            ((TransformProperty)cameraHolder.getCamera().getCameraComponent().getProperty(TransformProperty.NAME))
                    .move(new Vector3f(Float.valueOf(args[0]), Float.valueOf(args[1]), Float.valueOf(args[2])));
        } else {
            //TODO output getUsageText()
        }
    }

}
