package com.a2client.render.skybox;

import com.a2client.Main;
import com.a2client.render.Fog;
import com.a2client.render.Render;
import com.a2client.screens.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 25.07.16.
 */
public class Skybox
{
	private static final Logger _log = LoggerFactory.getLogger(Skybox.class.getName());

	private static final float SIZE = 80;

	/**
	 * degrees per second
	 */
	private static final float ROTATE_SPEED = 0.8f;

	private static final float[] VERTICES = {
			-SIZE, SIZE, -SIZE,
			-SIZE, -SIZE, -SIZE,
			SIZE, -SIZE, -SIZE,
			SIZE, -SIZE, -SIZE,
			SIZE, SIZE, -SIZE,
			-SIZE, SIZE, -SIZE,

			-SIZE, -SIZE, SIZE,
			-SIZE, -SIZE, -SIZE,
			-SIZE, SIZE, -SIZE,
			-SIZE, SIZE, -SIZE,
			-SIZE, SIZE, SIZE,
			-SIZE, -SIZE, SIZE,

			SIZE, -SIZE, -SIZE,
			SIZE, -SIZE, SIZE,
			SIZE, SIZE, SIZE,
			SIZE, SIZE, SIZE,
			SIZE, SIZE, -SIZE,
			SIZE, -SIZE, -SIZE,

			-SIZE, -SIZE, SIZE,
			-SIZE, SIZE, SIZE,
			SIZE, SIZE, SIZE,
			SIZE, SIZE, SIZE,
			SIZE, -SIZE, SIZE,
			-SIZE, -SIZE, SIZE,

			-SIZE, SIZE, -SIZE,
			SIZE, SIZE, -SIZE,
			SIZE, SIZE, SIZE,
			SIZE, SIZE, SIZE,
			-SIZE, SIZE, SIZE,
			-SIZE, SIZE, -SIZE,

			-SIZE, -SIZE, -SIZE,
			-SIZE, -SIZE, SIZE,
			SIZE, -SIZE, -SIZE,
			SIZE, -SIZE, -SIZE,
			-SIZE, -SIZE, SIZE,
			SIZE, -SIZE, SIZE
	};

	private Cubemap _cubemap;
	private Cubemap _cubemapNight;

	private ShaderProgram _shader;

	private Mesh _mesh;

	private float _rotate;

	private float _time = 5000;

	private TuningWindow _tuningWindow;

	public Skybox()
	{
		_cubemap = new Cubemap(
				Gdx.files.internal("assets/skybox/nightRight.png"),
				Gdx.files.internal("assets/skybox/nightLeft.png"),
				Gdx.files.internal("assets/skybox/nightTop.png"),
				Gdx.files.internal("assets/skybox/nightBottom.png"),
				Gdx.files.internal("assets/skybox/nightBack.png"),
				Gdx.files.internal("assets/skybox/nightFront.png")

//				Gdx.files.internal("assets/skybox/right.png"),
//				Gdx.files.internal("assets/skybox/left.png"),
//				Gdx.files.internal("assets/skybox/top.png"),
//				Gdx.files.internal("assets/skybox/bottom.png"),
//				Gdx.files.internal("assets/skybox/back.png"),
//				Gdx.files.internal("assets/skybox/front.png")
		);
		_cubemap.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		_cubemapNight = new Cubemap(
				Gdx.files.internal("assets/skybox/nightRight.png"),
				Gdx.files.internal("assets/skybox/nightLeft.png"),
				Gdx.files.internal("assets/skybox/nightTop.png"),
				Gdx.files.internal("assets/skybox/nightBottom.png"),
				Gdx.files.internal("assets/skybox/nightBack.png"),
				Gdx.files.internal("assets/skybox/nightFront.png")
		);
		_cubemapNight.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

//		_shader = Render.makeShader("skybox2");
		_shader = Render.makeShader("skybox");

		makeMesh();

		_tuningWindow = new TuningWindow();
	}

	public void clear()
	{
//		_cubemap.dispose();
	}

	public void Render(Camera camera, Environment environment)
	{
		if (_tuningWindow != null) _tuningWindow.update();
		_shader.begin();
		prepareShader(camera, environment);

		_mesh.render(_shader, GL20.GL_TRIANGLES);
//		_mesh.render(_shader, GL20.GL_LINE_STRIP);

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);

		_shader.end();
	}

	protected void prepareShader(Camera camera, Environment environment)
	{
		_rotate += ROTATE_SPEED * Main.deltaTime;

		_shader.setUniformMatrix("u_projTrans", camera.projection);
		_shader.setUniformMatrix("u_projViewTrans", camera.combined);


		final float PI = ((float) Math.PI);
		Matrix4 tmp;
		tmp = camera.view.cpy();
		tmp.translate(camera.position);
//		tmp.rotate(0, 1, 0, _rotate);
		_shader.setUniformMatrix("u_viewTrans", tmp);

		tmp = new Matrix4();
		if (Game.getInstance().getCamera().getChaseObj() != null)
		{
			tmp.translate(Game.getInstance().getCamera().getChaseObj().getWorldCoord().cpy().add(0, 0, 0));
		}
		_shader.setUniformMatrix("u_worldTrans", tmp);


		_shader.setUniformf("u_skyColor", Fog.skyColor.r, Fog.skyColor.g, Fog.skyColor.b);
		_shader.setUniformf("u_density", Fog.enabled ? Fog.density : 0f);
		_shader.setUniformf("u_gradient", Fog.gradient);

		if (_tuningWindow != null)
		{
			// ATMO
			float r = 1f;//(Math.max(Render.sunPosition.y, 300f) / 900f);
			float radius = _tuningWindow.getRadius();
			float cameraHeight = radius * _tuningWindow.getCameraHeight() * (r);
//		float cameraHeight = radius + camera.position.y;

			float Kr = 0.0025f;
			float Km = 0.0010f;
			float ESun = _tuningWindow.getEsun();// 15.0f;
			float g = -0.990f;
			float innerRadius = radius;
			float outerRadius = radius * _tuningWindow.getOutRadius();
			Vector3 waveLength = new Vector3(0.650f, 0.570f, 0.475f);
			float scaleDepth = 0.25f;
			float mieScaleDepth = 0.1f;

			Vector3 cpos = camera.position.cpy().add(0, radius, 0);
//		_shader.setUniformf("u_cameraPosition", cpos.x, cpos.y, cpos.z);
			_shader.setUniformf("u_cameraPosition", 0, cameraHeight, 0);

			_shader.setUniformf("v3InvWavelength", new Vector3(
					1f / ((float) Math.pow(waveLength.x, 4)),
					1f / ((float) Math.pow(waveLength.y, 4)),
					1f / ((float) Math.pow(waveLength.z, 4))
			));
			_shader.setUniformf("fCameraHeight", cameraHeight);
			_shader.setUniformf("fCameraHeight2", cameraHeight * cameraHeight);
			_shader.setUniformf("fInnerRadius", innerRadius);
			_shader.setUniformf("fInnerRadius2", innerRadius * innerRadius);
			_shader.setUniformf("fOuterRadius", outerRadius);
			_shader.setUniformf("fOuterRadius2", outerRadius * outerRadius);
			_shader.setUniformf("fKrESun", Kr * ESun);
			_shader.setUniformf("fKmESun", Km * ESun);
			_shader.setUniformf("fKr4PI", Kr * 4.0f * PI);
			_shader.setUniformf("fKm4PI", Km * 4.0f * PI);
			_shader.setUniformf("fScale", 1f / (outerRadius - innerRadius));
			_shader.setUniformf("fScaleDepth", scaleDepth);
			_shader.setUniformf("fScaleOverScaleDepth", 1f / (outerRadius - innerRadius) / scaleDepth);
			_shader.setUniformf("g", g);
			_shader.setUniformf("g2", g * g);
		}

		_shader.setUniformf("v3LightPosition", Render.sunPosition.cpy().nor().scl(1f));
		_shader.setUniformf("u_size", SIZE);

		Cubemap texture1;
		Cubemap texture2;

		float blendValue;

		if (_time >= 0 && _time < 5000)
		{
			texture1 = _cubemapNight;
			texture2 = _cubemapNight;
			blendValue = (_time - 0) / (5000 - 0);
		}
		else if (_time >= 5000 && _time < 8000)
		{
			texture1 = _cubemapNight;
			texture2 = _cubemap;
			blendValue = (_time - 5000) / (8000 - 5000);
		}
		else if (_time >= 8000 && _time < 21000)
		{
			texture1 = _cubemap;
			texture2 = _cubemap;
			blendValue = (_time - 8000) / (21000 - 8000);
		}
		else
		{
			texture1 = _cubemap;
			texture2 = _cubemapNight;
			blendValue = (_time - 21000) / (24000 - 21000);
		}

		_shader.setUniformf("u_blendValue", blendValue);


		texture1 = _cubemap;
		texture2 = _cubemapNight;
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE4);
		texture1.bind();
		_shader.setUniformi("u_texture1", 4);

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE5);
		texture2.bind();
		_shader.setUniformi("u_texture2", 5);
	}

	protected void makeMesh()
	{
//		_mesh = new Mesh(true, VERTICES.length / 3, 0,
//		                 new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position")
//		);
//		_mesh.setVertices(VERTICES);

		Icosahedron icosahedron = new Icosahedron(4, SIZE);
		_mesh = icosahedron.getMesh();
	}

	public void updateDayNight()
	{
		_time += Main.deltaTime * 500f;
		_time %= 24000;

	}
}
