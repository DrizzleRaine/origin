package com.a4server.gameserver.network.packets.serverpackets;

/**
 * Created by arksu on 01.02.15.
 */
public class ObjectRemove extends GameServerPacket
{
	private final int _objectId;

	public ObjectRemove(int objectId)
	{
		_objectId = objectId;
	}

	@Override
	protected void write()
	{
		writeC(0x12);
		writeD(_objectId);
	}
}
