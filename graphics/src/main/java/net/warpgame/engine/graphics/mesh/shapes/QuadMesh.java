package net.warpgame.engine.graphics.mesh.shapes;

public class QuadMesh extends CustomQuadMesh {
    private static final float[] VERTICES = new float[]{
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f
    };

    public QuadMesh(){
        super(VERTICES);
    }
}
