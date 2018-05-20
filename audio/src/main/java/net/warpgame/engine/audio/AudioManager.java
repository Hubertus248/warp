package net.warpgame.engine.audio;

import net.warpgame.engine.audio.command.CreateSourceCommand;
import net.warpgame.engine.audio.command.DisposeSourceCommand;
import net.warpgame.engine.audio.command.PauseCommand;
import net.warpgame.engine.audio.command.PlayCommand;
import net.warpgame.engine.core.component.Component;
import net.warpgame.engine.core.context.service.Service;
import org.joml.Vector3f;

import java.io.IOException;

/**
 * @author Hubertus
 *         Created 22.12.16
 */

@Service
public class AudioManager {

    public static AudioManager INSTANCE; //TODO remove

    private AudioContext audioContext;

    public AudioManager(AudioContext audioContext) {
        INSTANCE = this;
        this.audioContext = audioContext;
    }

    public AudioSource createPersistentSource(Component owner, Vector3f offset) {
        AudioSource source = new AudioSource(owner, offset, true);
        audioContext.putCommand(new CreateSourceCommand(source));
        return source;
    }

    public AudioSource createPersistentSource(Vector3f offset) {
        AudioSource source = new AudioSource(offset, true);
        audioContext.putCommand(new CreateSourceCommand(source));
        return source;
    }

    public void pause(AudioSource source){
        audioContext.putCommand(new PauseCommand(source));
    }

    public void deleteSorce(AudioSource source){
        audioContext.putCommand(new DisposeSourceCommand(source));
    }

    private Vector3f emptyVector = new Vector3f();

    public void playSingleRelative(String soundName) {
        playSingle(new AudioSource(emptyVector, false), soundName);
    }

    public void playSingleRelative(String soundName, Vector3f offset) {
        playSingle(new AudioSource(offset, false), soundName);
    }

    public void playSingle(Component owner, String soundName) {
        playSingle(new AudioSource(owner, emptyVector, false), soundName);
    }

    public void playSingle(Component owner, String soundName, Vector3f offset) {
        playSingle(new AudioSource(owner, offset, false), soundName);
    }

    private void playSingle(AudioSource source, String soundName) {
        audioContext.putCommand(new CreateSourceCommand(source));
        audioContext.putCommand(new PlayCommand(source, soundName));
    }

    public void play(AudioSource source, String soundName) {
        audioContext.putCommand(new PlayCommand(source, soundName));
    }

    public void loadFiles(String path) {
        try {
            audioContext.getSoundBank().loadDir(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop(AudioSource source) {

    }

    public void pause() {

    }

}
