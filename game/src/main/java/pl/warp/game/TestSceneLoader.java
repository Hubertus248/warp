package pl.warp.game;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import pl.warp.engine.ai.behaviourTree.BehaviourTree;
import pl.warp.engine.ai.behaviourTree.SequenceNode;
import pl.warp.engine.ai.property.AIProperty;
import pl.warp.engine.audio.AudioManager;
import pl.warp.engine.core.EngineContext;
import pl.warp.engine.core.EngineThread;
import pl.warp.engine.core.SimpleEngineTask;
import pl.warp.engine.core.scene.Component;
import pl.warp.engine.core.scene.NameProperty;
import pl.warp.engine.core.scene.Scene;
import pl.warp.engine.core.scene.SimpleComponent;
import pl.warp.engine.core.scene.listenable.SimpleListenableParent;
import pl.warp.engine.core.scene.properties.TransformProperty;
import pl.warp.engine.core.scene.script.ScriptContext;
import pl.warp.engine.graphics.RenderingConfig;
import pl.warp.engine.graphics.camera.Camera;
import pl.warp.engine.graphics.camera.QuaternionCamera;
import pl.warp.engine.graphics.light.LightProperty;
import pl.warp.engine.graphics.light.SpotLight;
import pl.warp.engine.graphics.material.GraphicsMaterialProperty;
import pl.warp.engine.graphics.material.Material;
import pl.warp.engine.graphics.math.projection.PerspectiveMatrix;
import pl.warp.engine.graphics.mesh.GraphicsCustomRendererProgramProperty;
import pl.warp.engine.graphics.mesh.GraphicsMeshProperty;
import pl.warp.engine.graphics.mesh.Mesh;
import pl.warp.engine.graphics.mesh.shapes.Ring;
import pl.warp.engine.graphics.mesh.shapes.Sphere;
import pl.warp.engine.graphics.particles.GraphicsParticleEmitterProperty;
import pl.warp.engine.graphics.particles.ParticleAnimator;
import pl.warp.engine.graphics.particles.ParticleFactory;
import pl.warp.engine.graphics.particles.SimpleParticleAnimator;
import pl.warp.engine.graphics.particles.dot.DotParticle;
import pl.warp.engine.graphics.particles.dot.DotParticleSystem;
import pl.warp.engine.graphics.particles.dot.RandomSpreadingDotParticleFactory;
import pl.warp.engine.graphics.postprocessing.lens.GraphicsLensFlareProperty;
import pl.warp.engine.graphics.postprocessing.lens.LensFlare;
import pl.warp.engine.graphics.postprocessing.lens.SingleFlare;
import pl.warp.engine.graphics.resource.mesh.ObjLoader;
import pl.warp.engine.graphics.resource.texture.ImageData;
import pl.warp.engine.graphics.resource.texture.ImageDataArray;
import pl.warp.engine.graphics.resource.texture.ImageDecoder;
import pl.warp.engine.graphics.resource.texture.PNGDecoder;
import pl.warp.engine.graphics.skybox.GraphicsSkyboxProperty;
import pl.warp.engine.graphics.texture.Cubemap;
import pl.warp.engine.graphics.texture.Texture1D;
import pl.warp.engine.graphics.texture.Texture2D;
import pl.warp.engine.graphics.texture.Texture2DArray;
import pl.warp.engine.graphics.window.Display;
import pl.warp.engine.physics.property.PhysicalBodyProperty;
import pl.warp.game.ai.SpinLeaf;
import pl.warp.game.program.gas.GasPlanetProgram;
import pl.warp.game.program.ring.PlanetaryRingProgram;
import pl.warp.game.program.ring.PlanetaryRingProperty;
import pl.warp.ide.controller.IDEController;
import pl.warp.ide.scene.SceneLoader;

import java.util.Random;

/**
 * @author Jaca777
 *         Created 2017-01-22 at 11
 */
public class TestSceneLoader implements SceneLoader {

    private static final float ROT_SPEED = 0.05f;
    private static final float MOV_SPEED = 0.2f * 10;
    private static final float BRAKING_FORCE = 0.2f * 10;
    private static final float ARROWS_ROTATION_SPEED = 2f;
    private static final int GUN_COOLDOWN = 5;

    private boolean loaded = false;

    private RenderingConfig config;
    private EngineContext context;
    private AudioManager audioManager;;
    private Scene scene;
    private Component root;
    private Component controllableGoat;
    private Camera camera;


    public TestSceneLoader(RenderingConfig config, EngineContext context, AudioManager audioManager) {
        this.config = config;
        this.context = context;
        this.audioManager = audioManager;
    }

    @Override
    public void loadScene() {
        scene = new Scene(context);
        new NameProperty(scene, "Test universe");
        context.setScene(scene);

        context.setScriptContext(new ScriptContext());

        root = new SimpleListenableParent(scene);
        new NameProperty(root, "Root");

        controllableGoat = new SimpleComponent(root);
        new NameProperty(controllableGoat, "Player ship");

        Display display = config.getDisplay();

        camera = new QuaternionCamera(controllableGoat, new PerspectiveMatrix(70, 0.01f, 20000f, display.getWidth(), display.getHeight()));
        new NameProperty(camera, "Camera");
        camera.move(new Vector3f(0, 4f, 15f));

        loaded = true;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void loadGraphics(EngineThread graphicsThread) {
        graphicsThread.scheduleOnce(() -> {
            Mesh goatMesh = ObjLoader.read(Test.class.getResourceAsStream("fighter_1.obj"), false).toVAOMesh();
            ImageData decodedTexture = ImageDecoder.decodePNG(Test.class.getResourceAsStream("fighter_1.png"), PNGDecoder.Format.RGBA);
            Texture2D goatTexture = new Texture2D(decodedTexture.getWidth(), decodedTexture.getHeight(), GL11.GL_RGBA, GL11.GL_RGBA, true, decodedTexture.getData());

            ImageDataArray lensSpritesheet = ImageDecoder.decodeSpriteSheetReverse(Test.class.getResourceAsStream("lens_flares.png"), PNGDecoder.Format.RGBA, 2, 1);
            Texture2DArray lensTexture = new Texture2DArray(lensSpritesheet.getWidth(), lensSpritesheet.getHeight(), lensSpritesheet.getArraySize(), lensSpritesheet.getData());
            SingleFlare[] flares = new SingleFlare[]{
                    new SingleFlare(0.75f, 0, 0.08f, new Vector3f(1)),
                    new SingleFlare(0.1f, 1, 0.02f, new Vector3f(1)),
                    new SingleFlare(-0.2f, 0, 0.06f, new Vector3f(1)),
                    new SingleFlare(-0.4f, 1, 0.08f, new Vector3f(1)),
                    new SingleFlare(-0.5f, 1, 0.05f, new Vector3f(1)),
                    new SingleFlare(-0.6f, 1, 0.08f, new Vector3f(1)),
                    new SingleFlare(-0.4f, 1, 0.25f, new Vector3f(1)),
                    new SingleFlare(-0.1f, 1, 0.2f, new Vector3f(1)),
                    new SingleFlare(0.2f, 1, 0.25f, new Vector3f(1)),
                    new SingleFlare(0.6f, 1, 0.25f, new Vector3f(1f)),
            };


            Component gasSphere = new SimpleComponent(root);
            new NameProperty(gasSphere, "Jupiter");
            Sphere sphere = new Sphere(50, 50);
            new GraphicsMeshProperty(gasSphere, sphere);
            ImageData decodedColorsTexture = ImageDecoder.decodePNG(Test.class.getResourceAsStream("gas.png"), PNGDecoder.Format.RGBA);
            Texture1D colorsTexture = new Texture1D(decodedColorsTexture.getWidth(), GL11.GL_RGBA, GL11.GL_RGBA, false, decodedColorsTexture.getData());
            GasPlanetProgram gasProgram = new GasPlanetProgram(colorsTexture);
            new GraphicsCustomRendererProgramProperty(gasSphere, gasProgram);
            TransformProperty gasSphereTransform = new TransformProperty(gasSphere);
            gasSphereTransform.move(new Vector3f(-1200f, -200f, -500f));
            gasSphereTransform.scale(new Vector3f(1000.0f));
            graphicsThread.scheduleTask(new SimpleEngineTask() {
                @Override
                public void update(int delta) {
                    gasProgram.use();
                    gasProgram.update(delta);
                    gasSphereTransform.rotateY(delta * 0.00001f);
                }
            });

            float startR = 1.5f;
            float endR = 2.5f;
            Component ring = new SimpleComponent(gasSphere);
            new NameProperty(ring, "Jupiter ring");
            Ring ringMesh = new Ring(20, startR, endR);
            new GraphicsMeshProperty(ring, ringMesh);
            new GraphicsCustomRendererProgramProperty(ring, new PlanetaryRingProgram());
            ImageData ringColorsData = ImageDecoder.decodePNG(Test.class.getResourceAsStream("ring_colors.png"), PNGDecoder.Format.RGBA);
            Texture1D ringColors = new Texture1D(ringColorsData.getWidth(), GL11.GL_RGBA, GL11.GL_RGBA, true, ringColorsData.getData());
            ringColors.enableAnisotropy(4);
            new PlanetaryRingProperty(ring, startR, endR, ringColors);

            ImageData brightnessTextureData = ImageDecoder.decodePNG(Test.class.getResourceAsStream("fighter_1_brightness.png"), PNGDecoder.Format.RGBA);
            Texture2D brightnessTexture = new Texture2D(brightnessTextureData.getWidth(), brightnessTextureData.getHeight(), GL11.GL_RGBA, GL11.GL_RGBA, true, brightnessTextureData.getData());

            generateShips(root, goatMesh, goatTexture, brightnessTexture);

            /*Mesh floorMesh = ObjLoader.read(Test.class.getResourceAsStream("floor.obj"), false).toVAOMesh();
            ImageData decodedFloorTexture = ImageDecoder.decodePNG(Test.class.getResourceAsStream("floor_1.png"), PNGDecoder.Format.RGBA);
            Texture2D floorTexture = new Texture2D(decodedFloorTexture.getWidth(), decodedFloorTexture.getHeight(), GL11.GL_RGBA, GL11.GL_RGBA, true, decodedFloorTexture.getData());
            Material floorMaterial = new Material(floorTexture);

            Component floor = new SimpleComponent(root);
            new GraphicsMeshProperty(floor, floorMesh);
            new GraphicsMaterialProperty(floor,floorMaterial);
            new TransformProperty(floor);
            new PhysicalBodyProperty(floor, 100, 100);*/

            ImageDataArray lightSpritesheet = ImageDecoder.decodeSpriteSheetReverse(Test.class.getResourceAsStream("boom_spritesheet.png"), PNGDecoder.Format.RGBA, 4, 4);
            Texture2DArray lightSpritesheetTexture = new Texture2DArray(lightSpritesheet.getWidth(), lightSpritesheet.getHeight(), lightSpritesheet.getArraySize(), lightSpritesheet.getData());
            Component light = new SimpleComponent(root);
            new NameProperty(light, "Particles");
            LightProperty property = new LightProperty(light);
            SpotLight spotLight = new SpotLight(light, new Vector3f(0f, 0f, 0f), new Vector3f(2f, 2f, 2f), new Vector3f(0.6f, 0.6f, 0.6f), 0.1f, 0.1f);
            property.addSpotLight(spotLight);
            TransformProperty lightSourceTransform = new TransformProperty(light);
            lightSourceTransform.move(new Vector3f(30f, 0f, 0f));
            ParticleAnimator animator = new SimpleParticleAnimator(new Vector3f(0), 0, 0);
            ParticleFactory<DotParticle> factory = new RandomSpreadingDotParticleFactory(0.008f, 1000, true, true, new Vector4f(1.0f), 0.01f);
            new GraphicsParticleEmitterProperty(light, new DotParticleSystem(animator, factory, 1000));
            LensFlare flare = new LensFlare(lensTexture, flares);
            new GraphicsLensFlareProperty(light, flare);

            ImageDataArray spritesheet = ImageDecoder.decodeSpriteSheetReverse(Test.class.getResourceAsStream("boom_spritesheet.png"), PNGDecoder.Format.RGBA, 4, 4);
            Texture2DArray spritesheetTexture = new Texture2DArray(spritesheet.getWidth(), spritesheet.getHeight(), spritesheet.getArraySize(), spritesheet.getData());

            GraphicsMeshProperty graphicsMeshProperty = new GraphicsMeshProperty(controllableGoat, goatMesh);
            new PhysicalBodyProperty(controllableGoat, 10f, 10.772f / 2, 1.8f / 2, 13.443f / 2);
            Material material = new Material(goatTexture);
            material.setBrightnessTexture(brightnessTexture);
            new GraphicsMaterialProperty(controllableGoat, material);
            new TransformProperty(controllableGoat);
            new GoatControlScript(controllableGoat, MOV_SPEED, ROT_SPEED, BRAKING_FORCE, ARROWS_ROTATION_SPEED);

            Mesh bulletMesh = new Sphere(15, 15, 0.5f);
            ImageData bulletDecodedTexture = ImageDecoder.decodePNG(Test.class.getResourceAsStream("bullet.png"), PNGDecoder.Format.RGBA);
            Texture2D bulletTexture = new Texture2D(bulletDecodedTexture.getWidth(), bulletDecodedTexture.getHeight(), GL11.GL_RGBA, GL11.GL_RGBA, true, bulletDecodedTexture.getData());

            new GunScript(controllableGoat, GUN_COOLDOWN, root, bulletMesh, spritesheetTexture, bulletTexture, controllableGoat, audioManager);

            SpotLight goatLight = new SpotLight(
                    controllableGoat,
                    new Vector3f(0, 0, 1),
                    new Vector3f(0, 0, 1), 0.15f, 0.2f,
                    new Vector3f(5f, 5f, 5f),
                    new Vector3f(0f, 0f, 0f),
                    0.1f, 0.1f);
            LightProperty directionalLightProperty = new LightProperty(controllableGoat);
            directionalLightProperty.addSpotLight(goatLight);

            ImageDataArray decodedCubemap = ImageDecoder.decodeCubemap("pl/warp/game/stars3");
            Cubemap cubemap = new Cubemap(decodedCubemap.getWidth(), decodedCubemap.getHeight(), decodedCubemap.getData());
            new GraphicsSkyboxProperty(scene, cubemap);

            Component frigate = new SimpleComponent(root);
            new NameProperty(frigate, "Frigate");
            Mesh friageMesh = ObjLoader.read(GunScript.class.getResourceAsStream("frigate_1_heavy.obj"), false).toVAOMesh();
            ImageData frigateDecodedTexture = ImageDecoder.decodePNG(Test.class.getResourceAsStream("frigate_1_heavy.png"), PNGDecoder.Format.RGBA);
            Texture2D frigateTexture = new Texture2D(frigateDecodedTexture.getWidth(), frigateDecodedTexture.getHeight(), GL11.GL_RGBA, GL11.GL_RGBA, true, frigateDecodedTexture.getData());
            Material frigateMaterial = new Material(frigateTexture);
            frigateMaterial.setShininess(20);
            ImageData frigateBrightnessDecodedTexture = ImageDecoder.decodePNG(Test.class.getResourceAsStream("frigate_1_heavy_brightness.png"), PNGDecoder.Format.RGBA);
            Texture2D frigateBrightnessTexture = new Texture2D(frigateBrightnessDecodedTexture.getWidth(), frigateBrightnessDecodedTexture.getHeight(), GL11.GL_RGBA, GL11.GL_RGBA, true, frigateBrightnessDecodedTexture.getData());
            frigateMaterial.setBrightnessTexture(frigateBrightnessTexture);
            new GraphicsMeshProperty(frigate, friageMesh);
            new GraphicsMaterialProperty(frigate, frigateMaterial);
            TransformProperty transformProperty = new TransformProperty(frigate);
            transformProperty.move(new Vector3f(100, 0, 0));
            transformProperty.rotateY((float) -(Math.PI / 2));
            transformProperty.scale(new Vector3f(3));


        });
    }


    public Scene getScene() {
        return scene;
    }

    public Camera getCamera() {
        return camera;
    }

    private static void generateShips(Component parent, Mesh goatMesh, Texture2D goatTexture, Texture2D brightnessTexture) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Component goat = new SimpleComponent(parent);
            new NameProperty(goat, "Ship " + i);
            new GraphicsMeshProperty(goat, goatMesh);
            Material material = new Material(goatTexture);
            material.setShininess(20f);
            material.setBrightnessTexture(brightnessTexture);
            new GraphicsMaterialProperty(goat, material);
            new PhysicalBodyProperty(goat, 1000f, 10.772f / 2, 1.8f / 2, 13.443f / 2);
            float x = random.nextFloat() * 200 - 100f;
            float y = random.nextFloat() * 200 - 100f;
            float z = random.nextFloat() * 200 - 100f;
            TransformProperty transformProperty = new TransformProperty(goat);
            transformProperty.move(new Vector3f(x, y, z));
            SequenceNode basenode = new SequenceNode();
            basenode.addChild(new SpinLeaf());
            BehaviourTree behaviourTree = new BehaviourTree(basenode, goat);
            new AIProperty(goat, behaviourTree);
        }
    }
}