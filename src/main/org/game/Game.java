package main.org.game;

import org.joml.Vector2f;
import org.joml.Vector3f;

import main.org.engine.IGameLogic;
import main.org.engine.MouseInput;
import main.org.engine.Scene;
import main.org.engine.SceneLight;
import main.org.engine.Window;
import main.org.engine.graph.Camera;
import main.org.engine.graph.DirectionalLight;
import main.org.engine.graph.Mesh;
import main.org.engine.graph.Renderer;
import main.org.engine.items.GameItem;
import main.org.engine.items.SkyBox;
import main.org.engine.items.Terrain;

import static org.lwjgl.glfw.GLFW.*;

import java.util.List;
import java.util.Map;

public class Game implements IGameLogic {

	private static final float MOUSE_SENSITIVITY = 0.2f;

	private final Vector3f cameraInc;

	private final Renderer renderer;

	private final Camera camera;

	private Scene scene;

	private Hud hud;

	private float lightAngle;

	private static final float CAMERA_POS_STEP = 0.05f;

	public Game() {
		this.renderer = new Renderer();
		this.camera = new Camera();
		this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
		this.lightAngle = -90;
	}

	@Override
	public void init(Window window) throws Exception {
		this.renderer.init(window);

		this.scene = new Scene();

		float skyBoxScale = 50.0f;
		float terrainScale = 10;
		int terrainSize = 3;
		float minY = -0.1f;
		float maxY = 0.1f;
		int textInc = 40;
		Terrain terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "/textures/heightmap.png", "/textures/terrain.png", textInc);
		this.scene.setGameItems(terrain.getGameItems());

		// Setup SkyBox
		SkyBox skyBox = new SkyBox("/models/skybox.obj", "/textures/skybox.png");
		skyBox.setScale(skyBoxScale);
		this.scene.setSkyBox(skyBox);

		// Setup Lights
		setupLights();

		// Create HUD
		this.hud = new Hud(this.camera.getRotation().toString());

		this.camera.getPosition().x = 0.0f;
		this.camera.getPosition().z = 0.0f;
		this.camera.getPosition().y = -0.2f;
		this.camera.getRotation().x = 0.f;

	}

	private void setupLights() {
		SceneLight sceneLight = new SceneLight();
		this.scene.setSceneLight(sceneLight);

		// Ambient Light
		sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
		sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

		// Directional Light
		float lightIntensity = 1.0f;
		Vector3f lightPosition = new Vector3f(1, 1, 0);
		sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));
	}

	@Override
	public void input(Window window, MouseInput mouseInput) {
		this.cameraInc.set(0, 0, 0);
		if (window.isKeyPressed(GLFW_KEY_W)) {
			this.cameraInc.z = -1;
		} else if (window.isKeyPressed(GLFW_KEY_S)) {
			this.cameraInc.z = 1;
		}
		if (window.isKeyPressed(GLFW_KEY_A)) {
			this.cameraInc.x = -1;
		} else if (window.isKeyPressed(GLFW_KEY_D)) {
			this.cameraInc.x = 1;
		}
		if (window.isKeyPressed(GLFW_KEY_Z)) {
			this.cameraInc.y = -1;
		} else if (window.isKeyPressed(GLFW_KEY_X)) {
			this.cameraInc.y = 1;
		}
	}

	@Override
	public void update(float interval, MouseInput mouseInput) {
		// Update camera based on mouse
		if (mouseInput.isRightButtonPressed()) {
			Vector2f rotVec = mouseInput.getDisplVec();
			this.camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);

			// Update HUD compass
			this.hud.rotateCompass(this.camera.getRotation().y);
		}

		// Update camera position
		this.camera.movePosition(this.cameraInc.x * CAMERA_POS_STEP, this.cameraInc.y * CAMERA_POS_STEP, this.cameraInc.z * CAMERA_POS_STEP);

		// Update directional light direction, intensity and colour
		SceneLight sceneLight = this.scene.getSceneLight();
		DirectionalLight directionalLight = sceneLight.getDirectionalLight();
		this.lightAngle += 0.05f;
		if (this.lightAngle > 90) {
			directionalLight.setIntensity(0);
			if (this.lightAngle >= 360) {
				this.lightAngle = -90;
			}
			sceneLight.getSkyBoxLight().set(0.3f, 0.3f, 0.3f);
		} else if (this.lightAngle <= -80 || this.lightAngle >= 80) {
			float factor = 1 - (float) (Math.abs(this.lightAngle) - 80) / 10.0f;
			sceneLight.getSkyBoxLight().set(factor, factor, factor);
			directionalLight.setIntensity(factor);
			directionalLight.getColor().y = Math.max(factor, 0.9f);
			directionalLight.getColor().z = Math.max(factor, 0.5f);
		} else {
			sceneLight.getSkyBoxLight().set(1.0f, 1.0f, 1.0f);
			directionalLight.setIntensity(1);
			directionalLight.getColor().x = 1;
			directionalLight.getColor().y = 1;
			directionalLight.getColor().z = 1;
		}
		double angRad = Math.toRadians(this.lightAngle);
		directionalLight.getDirection().x = (float) Math.sin(angRad);
		directionalLight.getDirection().y = (float) Math.cos(angRad);
		this.hud.setStatusText(this.camera.getPosition().toString());
	}

	@Override
	public void render(Window window) {
		this.hud.updateSize(window);
		this.renderer.render(window, this.camera, this.scene, this.hud);
	}

	@Override
	public void cleanup() {
		this.renderer.cleanup();
		this.scene.cleanup();
		this.hud.cleanup();
	}

}