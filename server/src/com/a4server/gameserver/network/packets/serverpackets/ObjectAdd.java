package com.a4server.gameserver.network.packets.serverpackets;

import com.a4server.gameserver.model.GameObject;

/**
 * Created by arksu on 01.02.15.
 */
public class ObjectAdd extends GameServerPacket
{
	private final GameObject _object;

	public ObjectAdd(GameObject object)
	{
		_object = object;
	}

	@Override
	protected void write()
	{
		writeC(0x11);
		writeD(_object.getObjectId());
		writeD(_object.getTypeId());
		writeD(_object.getPos().getX());
		writeD(_object.getPos().getY());
		writeH(_object.getPos().getHeading());
		writeS(_object.getName());
		writeS(_object.getTitle());
	}
}
