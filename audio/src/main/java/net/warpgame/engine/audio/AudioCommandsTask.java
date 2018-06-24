package net.warpgame.engine.audio;

import net.warpgame.engine.audio.command.Command;
import net.warpgame.engine.core.context.service.Service;
import net.warpgame.engine.core.context.task.RegisterTask;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;
import net.warpgame.engine.core.execution.task.EngineTask;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.SOFTHRTF.ALC_HRTF_SOFT;
import static org.lwjgl.openal.SOFTHRTF.alcResetDeviceSOFT;

/**
 * @author Hubertus
 *         Created 17.12.16
 */

@Service
@RegisterTask(thread = "audio")
public class AudioCommandsTask extends EngineTask {

    private AudioContext context;
    private BlockingQueue<Command> commandsQueue;
    private long device;
    private long alcContext;

    public AudioCommandsTask(AudioContext context) {
        this.context = context;
        this.commandsQueue = context.getCommandsQueue();
    }

    @Override
    protected void onInit() {
        initOpenAL();
    }

    @Override
    protected void onClose() {
        context.getAllBuffers().stream().map(AudioClip::getId).forEach(AL10::alDeleteBuffers);
        context.getFreeBuffers().forEach(AL10::alDeleteBuffers);

        context.getAllSources().stream().map(AudioSourceProperty::getId).forEach(AL10::alDeleteBuffers);
        context.getFreeSources().forEach(AL10::alDeleteBuffers);

        ALC10.alcDestroyContext(alcContext);
        ALC10.alcCloseDevice(device);
    }

    @Override
    public void update(int delta) {
        while (!commandsQueue.isEmpty()) {
            try {
                commandsQueue.take().execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initOpenAL() {
        device = ALC10.alcOpenDevice((ByteBuffer) null);
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        alcContext = alcCreateContext(device, (IntBuffer) null);
        alcMakeContextCurrent(alcContext);
        AL.createCapabilities(deviceCaps);
        IntBuffer attr = BufferUtils.createIntBuffer(10)
                .put(ALC_HRTF_SOFT)
                .put(ALC_TRUE)
                .put(0);
        attr.flip();
        alcResetDeviceSOFT(device, attr);

        alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);
    }
}