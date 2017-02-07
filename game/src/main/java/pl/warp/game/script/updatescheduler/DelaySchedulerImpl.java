package pl.warp.game.script.updatescheduler;

/**
 * @author Jaca777
 *         Created 2017-02-07 at 01
 */
public class DelaySchedulerImpl implements UpdateScheduler {
    private int delay;
    private int delayUntilUpdate;

    public DelaySchedulerImpl(int delay) {
        this.delay = delay;
        this.delayUntilUpdate = delay;
    }


    @Override
    public void update(int delta) {
        delayUntilUpdate -= delta;
    }

    @Override
    public boolean pollUpdate() {
        if (delayUntilUpdate <= 0) {
            delayUntilUpdate += delay;
            return true;
        } else return false;
    }
}
