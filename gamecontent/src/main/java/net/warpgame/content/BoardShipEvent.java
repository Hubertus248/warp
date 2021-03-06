package net.warpgame.content;

import net.warpgame.engine.core.context.service.Service;
import net.warpgame.engine.core.serialization.Deserializer;
import net.warpgame.engine.core.serialization.Serialization;
import net.warpgame.engine.core.serialization.Serializer;
import net.warpgame.engine.net.messagetypes.event.NetworkEvent;

import java.io.Serializable;

/**
 * @author Hubertus
 * Created 05.01.2018
 */
public class BoardShipEvent extends NetworkEvent implements Serializable {
    private int shipComponentId;

    public BoardShipEvent(int shipComponentId, int clientId) {
        super(clientId);
        this.shipComponentId = shipComponentId;
    }

    public int getShipComponentId() {
        return shipComponentId;
    }

    @Service
    public static class BoardShipSerialization extends Serialization<BoardShipEvent>{
        public BoardShipSerialization() {
            super(BoardShipEvent.class);
        }

        @Override
        public void serialize(BoardShipEvent object, Serializer serializer) {
            serializer
                    .write(object.shipComponentId)
                    .write(object.getTargetPeerId());
        }

        @Override
        public BoardShipEvent deserialize(Deserializer deserializer) {
            return new BoardShipEvent(
                    deserializer.getInt(),
                    deserializer.getInt()
            );
        }

        @Override
        public int getType() {
            return 4;
        }
    }
}
