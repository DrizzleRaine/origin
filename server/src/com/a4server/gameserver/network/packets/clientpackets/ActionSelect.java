package com.a4server.gameserver.network.packets.clientpackets;

import com.a4server.gameserver.model.Cursor;
import com.a4server.gameserver.model.GameLock;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a4server.gameserver.model.Cursor.CursorName.*;

/**
 * игрок выбрал некое действие
 * Created by arksu on 18.10.15.
 */
public class ActionSelect extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ActionSelect.class.getName());

	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS();
	}

	@Override
	public void run()
	{
		_log.debug("action: " + _name);

		Player player = client.getPlayer();
		if (player != null)
		{
			try (GameLock ignored = player.lock())
			{
				if ("online".equals(_name))
				{

				}
				else if ("lift_up".equals(_name))
				{
					player.setCursor(LiftUp);
				}
//				else if ("spawn_pine".equals(_name))
//				{
//					player.setCursor(Spawn, 14);
//				}
				else if (_name.startsWith("spawn_"))
				{
					String obname = _name.substring(6);
					ObjectTemplate template = ObjectsFactory.getInstance().getTemplate(obname);
					if (template != null)
					{
						player.setCursor(Spawn, template.getTypeId());
					}
				}
				else
				{
					Cursor.CursorName cursor = Arrow;
					if ("tile_up".equals(_name))
					{
						cursor = TileUp;
					}
					else if ("tile_down".equals(_name))
					{
						cursor = TileDown;
					}
					else if ("tile_sand".equals(_name))
					{
						cursor = TileSand;
					}
					else if ("tile_grass".equals(_name))
					{
						cursor = TileGrass;
					}
					player.setCursor(cursor);
				}
			}
		}
	}
}
