package pl.warp.engine.ai.behaviortree;

/**
 * @author Hubertus
 *         Created 24.01.17
 */
public class InverterNode extends DecoratorNode {

    @Override
    int tick(Ticker ticker, int delta) {
        switch (ticker.tickNode(child)) {
            case Node.RUNNING:
                return Node.RUNNING;
            case Node.SUCCESS:
                return Node.FAILURE;
            case Node.FAILURE:
                return Node.SUCCESS;
            default:
                return Node.FAILURE;
        }
    }

    @Override
    public void onOpen(Ticker ticker) {

    }

    @Override
    public void onReEnter(Ticker ticker) {

    }

    @Override
    protected void onInit(Ticker ticker) {

    }

    @Override
    protected void onClose(Ticker ticker) {

    }
}