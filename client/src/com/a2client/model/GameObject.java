package com.a2client.model;

import com.a2client.ModelManager;
import com.a2client.Terrain;
import com.a2client.g3d.Animation;
import com.a2client.g3d.Model;
import com.a2client.network.game.serverpackets.ObjectAdd;
import com.a2client.util.Utils;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.a2client.Terrain.TILE_SIZE;

/**
 * базовый игровой объект
 * Created by arksu on 06.02.15.
 */
public class GameObject
{
	private static final Logger _log = LoggerFactory.getLogger(GameObject.class.getName());

	private int _objectId;
	private int _typeId;
	private Vector2 _coord;
	private int _heading;
	private String _name;
	private String _title;
	private boolean _interactive;
	private Mover _mover = null;
	boolean _isMoving = false;

	protected Model _model = null;

	private boolean _needInit = true;

	private Vector3 _worldCoord;

	private final Map<Integer, Model> _lift = new HashMap<>();

	public GameObject(ObjectAdd pkt)
	{
		_name = pkt._name;
		_title = pkt._title;
		_coord = new Vector2(pkt._x, pkt._y);
		_heading = pkt._heading;
		updateCoord();
		_objectId = pkt._objectId;
		_typeId = pkt._typeId;
		_interactive = false;
		initModel();
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getTypeId()
	{
		return _typeId;
	}

	public Vector2 getCoord()
	{
		return _coord;
	}

	public Vector3 getWorldCoord()
	{
		return _worldCoord;
	}

	public String getName()
	{
		return _name;
	}

	public BoundingBox getBoundingBox()
	{
		return _model.getBoundingBox();
	}

	public Model getModel()
	{
		return _model;
	}

	public Map<Integer, Model> getLift()
	{
		return _lift;
	}

	public void setCoord(int x, int y)
	{
		_coord.x = x;
		_coord.y = y;
		updateCoord();
	}

	public void setCoord(Vec2i c)
	{
		_coord.x = c.x;
		_coord.y = c.y;
	}

	public void setCoord(Vector2 c)
	{
		_coord.x = c.x;
		_coord.y = c.y;
		updateCoord();
	}

	public void updateWorldCoord()
	{
		float x = _coord.x / TILE_SIZE;
		float y = _coord.y / TILE_SIZE;
		float h = Terrain.getHeight(x, y);
		_worldCoord = new Vector3(x, h, y);
	}

	public void setInteractive(boolean interactive)
	{
		if (_interactive != interactive)
		{
			_model.playAnimation(interactive ? "open" : "close", 1f, 0.3f, Animation.LoopMode.Last);
		}
		_interactive = interactive;
	}

	public boolean isInteractive()
	{
		return _interactive;
	}

	/**
	 * сервер сообщает о движении объекта
	 * @param cx текущие координаты
	 * @param cy текущие координаты
	 * @param vx вектор движения
	 * @param vy вектор движения
	 */
	public void move(int cx, int cy, int vx, int vy, int speed)
	{
		if (_mover != null)
		{
			_mover.newMove(cx, cy, vx, vy, speed);
		}
		else
		{
			_mover = new Mover(this, cx, cy, vx, vy, speed);
		}

		if (!_isMoving)
		{
			setMoving(true);
		}
	}

	public void stopMove()
	{
		_mover = null;
		setMoving(false);
	}

	public void setMoving(boolean moving)
	{
		if (moving == _isMoving) return;
		_isMoving = moving;
	}

	public void update()
	{
		if (_needInit)
		{
			_needInit = false;
		}

		_model.update();

		if (_mover != null)
		{
			_mover.update();
			if (_mover._arrived)
			{
				_mover = null;
//				setMoving(false);
			}
		}
	}

	private void initModel()
	{
		_model = ModelManager.getInstance().getModelByType(_typeId);
		if (_model != null)
		{
			updateCoord();

			if (_typeId == 1)
			{
				_model.playAnimation("idle");
			}
			_model.setHeading(_heading, true);
		}
	}

	public void updateCoord()
	{
		updateWorldCoord();
		if (_model != null)
		{
			_model.setPos(_worldCoord);
		}
	}

	@Override
	public String toString()
	{
		return "("
		       + (!Utils.isEmpty(_name) ? "\"" + _name + "\" " : "")
		       + "id=" + _objectId
		       + " type=" + _typeId + ")";
	}
}
