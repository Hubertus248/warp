package pl.warp.engine.graphics.gui;

/**
 * @author Jaca777
 *         Created 2017-03-11 at 14
 */
public interface Node {
    String getName();
    boolean isDirty();
    void setDirty(boolean dirty);
}
