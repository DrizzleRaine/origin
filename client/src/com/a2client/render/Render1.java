package com.a2client.render;

import com.a2client.Config;
import com.a2client.Main;
import com.a2client.MapCache;
import com.a2client.ObjectCache;
import com.a2client.model.GameObject;
import com.a2client.model.Grid;
import com.a2client.screens.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * примитивный рендер, пока один. может еще добавим других
 * Created by arksu on 25.02.15.
 */
public class Render1
{
    private static final Logger _log = LoggerFactory.getLogger(Render1.class.getName());

    private Game _game;

    private ShaderProgram _shader;
    public Model model;
    public ModelInstance instance;
    private ModelBatch modelBatch;

    private Environment environment;
    public int _chunksRendered = 0;

    public Render1(Game game) {
        ShaderProgram.pedantic = false;
        _game = game;
        _shader = new ShaderProgram(
                Gdx.files.internal("assets/basic_vert.glsl"),
                Gdx.files.internal("assets/basic_frag.glsl"));

        if (!_shader.isCompiled())
        {
            Gdx.app.log("Shader", _shader.getLog());
            Gdx.app.log("Shader V", _shader.getVertexShaderSource());
            Gdx.app.log("Shader F", _shader.getFragmentShaderSource());
        }

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        ModelLoader loader = new ObjLoader();
        model = loader.loadModel(Gdx.files.internal("assets/ship.obj"));
        instance = new ModelInstance(model);
    }

    public void render() {
        _shader.begin();
        _shader.setUniformMatrix("u_MVPMatrix", _game.getGameCamera()._camera.combined);
//        _shader.setUniformMatrix("u_view", _camera.view);
        _shader.setUniformi("u_texture", 0);

        Main.getAssetManager().get(Config.RESOURCE_DIR + "tiles_atlas.png", Texture.class).bind();

        _shader.setUniformf("u_ambient", ((ColorAttribute)environment.get(ColorAttribute.AmbientLight)).color);
        _chunksRendered = 0;
        for (Grid grid : MapCache.grids)
        {
            _chunksRendered += grid.render(_shader, _game.getGameCamera()._camera);
        }
        _shader.end();

        if (ObjectCache.getInstance() != null)
        {
            modelBatch.begin(_game.getGameCamera()._camera);
            for (GameObject o : ObjectCache.getInstance().getObjects())
            {
                renderObject(o);
            }
            modelBatch.end();
        }
    }

    private void renderObject(GameObject object)
    {
        Vector2 oc = new Vector2(object.getCoord().x, object.getCoord().y);
        oc.x = oc.x / MapCache.TILE_SIZE;
        oc.y = oc.y / MapCache.TILE_SIZE;
        oc = oc.add(getOffset()).add(_game.getGameCamera()._cameraPos).sub(_game.getGameCamera()._camera_offset);

        instance.transform.setToTranslation(oc.x, 0.5f, oc.y);
        modelBatch.render(instance, environment);
    }

    public Vector2 getOffset()
    {
        Vector2 offset = Vector2.Zero;
        if (ObjectCache.getInstance().getMe() != null)
        {
            Vector2 pp = new Vector2(ObjectCache.getInstance().getMe().getCoord());
            pp.x = pp.x / MapCache.TILE_SIZE;
            pp.y = pp.y / MapCache.TILE_SIZE;
            pp = pp.sub(pp.x * 2, pp.y * 2);
            offset = pp;
        }
        return offset;
    }

}