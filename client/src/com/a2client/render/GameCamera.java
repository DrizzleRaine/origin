package com.a2client.render;

import com.a2client.Terrain;
import com.a2client.model.GameObject;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * простая игровая камера
 * Created by arksu on 25.02.15.
 */
public class GameCamera extends PerspectiveCamera
{
	public static final float OFFSET_SPEED = 8f;

	public static final float NEAR = 0.3f;
	public static final float FAR = 1000f;
	public static final float FOV = 30f;

	/**
	 * дистанция от камеры до точки куда смотрим
	 */
	private float _cameraDistance = 80;

	/**
	 * углы поворота камеры
	 */
	private float _angleY = 90f;
	private float _angleX = 75f;

	/**
	 * координаты мыши в которых начали вращение камеры
	 */
	private Vec2i _startDrag;

	/**
	 * углы поворота камеры на момент начала вращения
	 */
	private float _startAngleY;
	private float _startAngleX;

	/**
	 * объект в который смотрим
	 */
	private GameObject _chaseObj = null;

	/**
	 * отступ от точки куда смотрим, можем сдвинуть точку куда смотрит камера в сторону
	 */
	private final Vector3 _offset = new Vector3(0, 0, 0);

	/**
	 * луч проецирования из координат мыши
	 */
	private Ray _ray;

	private MousePicker _mousePicker;

	public GameCamera()
	{
		fieldOfView = FOV;
		viewportWidth = 800;
		viewportHeight = 600;
		near = NEAR;
		far = FAR;

		_mousePicker = new MousePicker();

		update();
	}

	public void update()
	{
		_ray = getPickRay(Gdx.input.getX(), Gdx.input.getY());
		_mousePicker.update(_ray);

		if (com.a2client.Input.isWheelUpdated())
		{
			_cameraDistance += (_cameraDistance / 15f) * com.a2client.Input.MouseWheel;
			_cameraDistance = Math.min(_cameraDistance, 90);
			_cameraDistance = Math.max(_cameraDistance, 1.4f);
			com.a2client.Input.MouseWheel = 0;
		}

		// установим верх
		up.set(0, 1, 0);

		// обнулим позицию
		position.setZero();
		// установим камеру на нужную нам дистанцию
		position.add(0, _cameraDistance, 0);
		// а теперь повернем как надо
		position.rotate(Vector3.X, _angleX);
		position.rotate(Vector3.Y, _angleY);

		if (_chaseObj != null)
		{
			// и сместим до положения игрока
			position.add(_chaseObj.getWorldCoord());

			// ограничитель чтобы камера не проваливалась под землю
			float h = Terrain.getHeight(position.x, position.z);
			h = Math.max(Terrain.WATER_LEVEL, h) + 0.5f;
			position.y = Math.max(h, position.y);

			// скажем смотреть на игрока
			Vector3 worldCoord = _chaseObj.getWorldCoord().cpy().add(0, 3, 0);
			lookAt(worldCoord);

//			position.add(0,5,0);
		}
		else
		{
			lookAt(0, 0, 0);
		}
		normalizeUp();
		position.add(_offset);

		super.update();
	}

	public void setChaseObject(GameObject obj)
	{
		_chaseObj = obj;

		if (_chaseObj == null)
		{
			return;
		}

		direction.set(0, 0, -1);
	}

	public GameObject getChaseObj()
	{
		return _chaseObj;
	}

	public void onResize(int width, int height)
	{
//		float camWidth = width / 48f;
//		float camHeight = camWidth * ((float) height / (float) width);

		viewportWidth = width;
		viewportHeight = height;

		update();
	}

	public void startDrag(Vec2i startDrag)
	{
		_startDrag = startDrag;
		_startAngleX = _angleX;
		_startAngleY = _angleY;
	}

	public void updateDrag(Vec2i c)
	{
		if (_startDrag != null)
		{
			_angleY = _startAngleY - (c.sub(_startDrag).x * 0.3f);
			_angleX = _startAngleX - (c.sub(_startDrag).y * 0.3f);

			// ограничим вертикальный угол
//			_angleX = Math.min(_angleX, 110f);
			_angleX = Math.max(_angleX, 1f);
		}
	}

	public Vec2i getStartDrag()
	{
		return _startDrag;
	}

	public Vector3 getOffset()
	{
		return _offset;
	}

	public Ray getRay()
	{
		return _ray;
	}

	public float getAngleY()
	{
		return _angleY;
	}

	public float getAngleX()
	{
		return _angleX;
	}

	public float getCameraDistance()
	{
		return _cameraDistance;
	}

	public MousePicker getMousePicker()
	{
		return _mousePicker;
	}
}
