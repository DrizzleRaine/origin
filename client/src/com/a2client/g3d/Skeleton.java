package com.a2client.g3d;

import com.a2client.g3d.math.DualQuat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by arksu on 17.03.17.
 */
public class Skeleton
{
	private final Model _model;
	private final SkeletonData _data;

	private Skeleton _parent;
	private int _parentJoint;

	private DualQuat[] _joint;
	private DualQuat[] _jquat;

	public Skeleton(SkeletonData data, Model model)
	{
		_model = model;
		_data = data;
		_jquat = new DualQuat[_data.getJointsCount()];
		_joint = new DualQuat[_data.getJointsCount()];
	}

	public void bind(ShaderProgram shader)
	{
		DualQuat[] jquat = new DualQuat[_data.getJointsCount()];

		for (int i = 0; i < _data.getJointsCount(); i++)
		{
			updateJoint(i);
			jquat[i] = _joint[i].mul(_data.getJoints()[i].getBind());
		}

		int location = shader.fetchUniformLocation("u_joints", true);
		Gdx.gl20.glUniform4fv(location, 0, getFloatBuffer(jquat));
	}

	public void updateJoint(int idx)
	{
		DualQuat cjoint = _data.getJoints()[idx].getFrame();
		DualQuat defaultJoint = new DualQuat(cjoint);

		// идем с последней добавленной анимации и смотрим на ее вес.
		// с каждой пройденной анимацией вычитаем вес
		float w = 1f;
		List<Animation> list = _model.getAnimations();
		if (idx > -1 && list.size() > 0)
		{
			int i = 0;
			while (i < list.size())
			{
				Animation animation = list.get(i);
				if (w < Const.EPS)
				{
					list.remove(i);
					continue;
				}
				else
				{
					animation.lerpJoint(defaultJoint, idx);
					cjoint = cjoint.lerp(animation.joint[idx], w);
					w -= animation.getWeight();
				}
				i++;
			}
		}
		w = 1f;
		list = _model.getMergeAnimations();
		if (idx > -1 && list.size() > 0)
		{
			int i = 0;
			while (i < list.size())
			{
				Animation animation = list.get(i);
				if (w < Const.EPS)
				{
					list.remove(i);
					continue;
				}
				else
				{
					animation.lerpJoint(cjoint, idx);
					cjoint = cjoint.lerp(animation.joint[idx], w * animation.getWeight());
					w -= animation.getWeight();
				}
				i++;
			}
		}

		if (_data.getJoints()[idx].getParentIndex() > -1)
		{
			updateJoint(_data.getJoints()[idx].getParentIndex());
			_joint[idx] = _joint[_data.getJoints()[idx].getParentIndex()].mul(cjoint);
		}
		else
		{
			if (_parent != null)
			{
				_parent.updateJoint(_parentJoint);
				if (_parent._joint[_parentJoint] != null)
				{
					_joint[idx] = _parent._joint[_parentJoint].mul(cjoint);
				}
			}
			else
			{
				_joint[idx] = cjoint;
			}
		}
	}

	public FloatBuffer getFloatBuffer(DualQuat[] d)
	{
		ByteBuffer temp = ByteBuffer.allocateDirect(d.length * 8 * 4);
		temp.order(ByteOrder.nativeOrder());
		FloatBuffer fb = temp.asFloatBuffer();

		for (DualQuat dq : d)
		{
			fb.
					  put(dq.real.x).
					  put(dq.real.y).
					  put(dq.real.z).
					  put(dq.real.w).

					  put(dq.dual.x).
					  put(dq.dual.y).
					  put(dq.dual.z).
					  put(dq.dual.w);
		}
		fb.flip();
		return fb;
	}

	public int getJointsCount()
	{
		return _data.getJointsCount();
	}
}
