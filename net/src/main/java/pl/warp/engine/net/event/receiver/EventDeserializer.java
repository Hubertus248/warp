package pl.warp.engine.net.event.receiver;

import io.netty.buffer.ByteBuf;
import org.nustaq.serialization.FSTConfiguration;

/**
 * @author Hubertus
 * Created 02.01.2018
 */
public class EventDeserializer {
    private FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();

    public Object deserialize(ByteBuf eventContent) {
        byte[] bytes = new byte[eventContent.readableBytes()];
        eventContent.readBytes(bytes);
        return conf.asObject(bytes);
    }
}