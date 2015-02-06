package com.a2client.screens;

import com.a2client.*;
import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Button;
import com.a2client.gui.GUI_Label;
import com.a2client.model.GameObject;
import com.a2client.model.Grid;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game extends BaseScreen
{
    private static final Logger _log = LoggerFactory.getLogger(Game.class.getName());

    public enum GameState
    {
        ENTERING,
        IN_GAME
    }

    private static String _statusText = "";

    private GUI_Label _lblStatus;
    private GUI_Button _btnExit;

    private static Game _instance;
    private GameState _state = GameState.ENTERING;
    private OrthographicCamera _camera;

    private Vector2 _player_pos = new Vector2(0, 0);

    private ShapeRenderer _renderer = new ShapeRenderer();
    private Vector2 _world_mouse_pos = new Vector2();

    static final float MOVE_STEP = 0.2f;

    public Game()
    {
        Player.init();
        ObjectCache.init();
        MapCache.clear();

        GUI.reCreate();
        _lblStatus = new GUI_Label(GUI.rootNormal());
        _lblStatus.SetPos(10, 10);

        _btnExit = new GUI_Button(GUI.rootNormal())
        {
            @Override
            public void DoClick()
            {
                Main.ReleaseAll();
                Login.setStatus("disconnected");
            }
        };
        _btnExit.caption = Lang.getTranslate("generic", "cancel");
        _btnExit.SetSize(100, 25);
        _btnExit.SetPos(Gdx.graphics.getWidth() - 110, Gdx.graphics.getHeight() - 35);

    }

    @Override
    public void dispose()
    {
        Player.getInstance().dispose();
        ObjectCache.getInstance().dispose();
        MapCache.clear();
        _instance = null;
        super.dispose();
    }

    @Override
    public void onUpdate()
    {

        //_player_pos.x += -0.1f * Gdx.graphics.getDeltaTime();


        _world_mouse_pos = screen2world(Gdx.input.getX(), Gdx.input.getY()).sub(getOffset()).sub(_player_pos);

        //_player_pos = new Vector2();
        if (com.a2client.Input.KeyDown(Input.Keys.W))
        {
            _player_pos.y -= MOVE_STEP;
        }
        if (com.a2client.Input.KeyDown(Input.Keys.S))
        {
            _player_pos.y += MOVE_STEP;
        }
        if (com.a2client.Input.KeyDown(Input.Keys.A))
        {
            _player_pos.x += MOVE_STEP;
        }
        if (com.a2client.Input.KeyDown(Input.Keys.D))
        {
            _player_pos.x -= MOVE_STEP;
        }

        if (com.a2client.Input.isWheelUpdated())
        {
            _camera.zoom += com.a2client.Input.MouseWheel / 10f;
            com.a2client.Input.MouseWheel = 0;
        }

        if (_state == GameState.IN_GAME)
        {
            _statusText = _world_mouse_pos.toString();
        }
        _lblStatus.caption = "FPS: "+Gdx.graphics.getFramesPerSecond()+" "+ _statusText;

        if (ObjectCache.getInstance().getMe() != null)
        {
            Vec2i pp = ObjectCache.getInstance().getMe().getCoord().div(11);
            pp = pp.sub(pp.mul(2));
            //            _player_pos = pp.getVector2();
        }
        _camera.position.set(Vector2.Zero, 0);
        _camera.update();

    }

    @Override
    public void onRender3D()
    {
        // оффсет
        Vector2 offset = getOffset();

        offset.add(_player_pos);

        // координаты тайла который рендерим
        Vector2 tc = new Vector2();

        _renderer.setProjectionMatrix(_camera.combined);
        _renderer.begin(ShapeType.Filled);

        for (Grid grid : MapCache.grids)
        {
            for (int x = 0; x < MapCache.GRID_SIZE; x++)
            {
                for (int y = 0; y < MapCache.GRID_SIZE; y++)
                {
                    tc.x = (grid.getGC().x / MapCache.TILE_SIZE) + x + offset.x;
                    tc.y = (grid.getGC().y / MapCache.TILE_SIZE) + y + offset.y;

                    // все вершины тайла попадают в угол обзора
                    if (
                            _camera.frustum.pointInFrustum(tc.x, tc.y, 0) ||
                                    _camera.frustum.pointInFrustum(tc.x + 1, tc.y, 0) ||
                                    _camera.frustum.pointInFrustum(tc.x, tc.y + 1, 0) ||
                                    _camera.frustum.pointInFrustum(tc.x + 1, tc.y + 1, 0)
                            )
                    {
                        _renderer.setColor(Grid.getTileColor(grid._tiles[y][x]));
                        _renderer.box(tc.x, tc.y, 0, 1, 1, 0.7f);
                    }
                }
            }
        }

        for (GameObject o : ObjectCache.getInstance().getObjects())
        {
            renderObject(o);
        }

        _renderer.end();
    }

    private void renderObject(GameObject object)
    {
        Vector2 oc = object.getCoord().div(11).getVector2().add(getOffset()).add(_player_pos);

        _renderer.setColor(Color.RED);
        float sz = 0.5f;
        _renderer.box(oc.x-sz, oc.y-sz, 0, sz, sz, 0.7f);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        float camWidth = width / 48f;

        float camHeight = camWidth * ((float) height / (float) width);

        _camera = new IsometricCamera(camWidth, camHeight);
        _camera.update();
        _camera.zoom = 0.6f;
    }

    public Vector2 screen2world(int x, int y)
    {
        Vector3 touch = new Vector3(x, y, 0);
        _camera.unproject(touch);
        // touch.mul(_invTransform);
        return new Vector2(touch.x, touch.y);
    }

    public Vector2 getOffset()
    {
        Vector2 offset = Vector2.Zero;
        if (ObjectCache.getInstance().getMe() != null)
        {
            Vec2i pp = ObjectCache.getInstance().getMe().getCoord().div(11);
            pp = pp.sub(pp.mul(2));
            offset = pp.getVector2();
        }
        return offset;
    }

    public void setState(GameState state)
    {
        _state = state;
    }

    static public Game getInstance()
    {
        if (_instance == null)
        {
            _log.error("Game instance is NULL!");
        }
        return _instance;
    }

    static public void setStatusText(String statustext)
    {
        _statusText = statustext;
    }

    static public void Show()
    {
        _statusText = "";
        Main.freeScreen();
        _instance = new Game();
        Main.getInstance().setScreen(_instance);
    }
}