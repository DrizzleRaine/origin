package com.a4server.gameserver.network.packets.serverpackets;

import java.util.List;

/**
 * Created by arksu on 20.08.16.
 */
public class ContextMenu extends GameServerPacket
{
	private final List<String> _list;
	private final int _objectId;

	public ContextMenu(int objectId, List<String> list)
	{
		_list = list;
		_objectId = objectId;
	}

	@Override
	protected void write()
	{
		writeC(0x23);
		writeD(_objectId);
		writeC(_list.size());
		for (String s : _list)
		{
			writeS(s);
		}
	}
}
