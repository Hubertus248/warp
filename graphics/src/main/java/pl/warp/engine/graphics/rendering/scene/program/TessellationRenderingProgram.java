package pl.warp.engine.graphics.rendering.scene.program;

import pl.warp.engine.graphics.material.Material;
import pl.warp.engine.graphics.program.ProgramAssemblyInfo;
import pl.warp.engine.graphics.tessellation.program.TessellationProgram;

/**
 * @author Jaca777
 * Created 2017-09-23 at 22
 */
public class TessellationRenderingProgram extends SceneRenderingProgram {

    private static final int DISPLACEMENT_SAMPLER = 1;

    private int unifDisplacementEnabled;
    private int unifDisplacementFactor;


    public TessellationRenderingProgram(TessellationProgram program) {
        super(ProgramAssemblyInfo.withTesselation(program));
    }

    protected void loadUniforms() {
        super.loadUniforms();
        this.unifDisplacementFactor = getUniformLocation("displacementFactor");
        this.unifDisplacementEnabled = getUniformLocation("displacementEnabled");
    }

    protected void loadSamplers() {
        super.loadSamplers();
        setTextureLocation("displacementMap", DISPLACEMENT_SAMPLER);
    }

    public void useMaterial(Material material) {
        super.useMaterial(material);
        if(material.hasDisplacementMap()) {
            useTexture(DISPLACEMENT_SAMPLER, material.getDisplacementMap());
            setUniformf(unifDisplacementFactor, material.getDisplacementFactor());
            setUniformb(unifDisplacementEnabled, true);
        } else {
            setUniformb(unifDisplacementEnabled, false);
        }
    }

}