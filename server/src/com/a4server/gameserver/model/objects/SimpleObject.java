package com.a4server.gameserver.model.objects;

import com.a4server.gameserver.model.craft.Craft;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * реализация простого шаблона для большинства объектов
 * Created by arksu on 23.02.15.
 */
public class SimpleObject implements ObjectTemplate
{
    private final String _name;
    private int _typeId;
    private int _width = 10;
    private int _height = 10;
    private CollisionTemplate _collision = null;
    private InventoryTemplate _inventory = null;
    private ItemTemplate _item = null;

    private static final Logger _log = LoggerFactory.getLogger(SimpleObject.class.getName());

    public SimpleObject(String name)
    {
        _name = name;
    }

    public void read(JsonReader in) throws IOException
    {
        while (in.hasNext())
        {
            JsonToken tkn = in.peek();
            switch (tkn)
            {
                case NAME:
                    readParam(in);
                    break;
                case END_OBJECT:
                    return;
                default:
                    _log.warn(getClass().getSimpleName() + ": wrong token " + tkn);
                    return;
            }
        }
    }

    protected void readParam(JsonReader in) throws IOException
    {
        String paramName = in.nextName();
        if ("typeid".equalsIgnoreCase(paramName))
        {
            _typeId = in.nextInt();
        }
        else if ("size".equalsIgnoreCase(paramName))
        {
            int sz = in.nextInt();
            _width = sz;
            _height = sz;
        }
        else if ("collision".equalsIgnoreCase(paramName))
        {
            Gson gson = new Gson();
            _collision = gson.fromJson(in, CollisionTemplate.class);
        }
        else if ("inventory".equalsIgnoreCase(paramName))
        {
            Gson gson = new Gson();
            _inventory = gson.fromJson(in, InventoryTemplate.class);
        }
        else if ("item".equalsIgnoreCase(paramName))
        {
            Gson gson = new Gson();
            _item = gson.fromJson(in, ItemTemplate.class);
        }
        else if ("craft".equalsIgnoreCase(paramName))
        {
            Gson gson = new Gson();
            CraftTemplate craft = gson.fromJson(in, CraftTemplate.class);
            Craft._crafts.add(craft);
        }
    }

    @Override
    public int getTypeId()
    {
        return _typeId;
    }

    @Override
    public int getWidth()
    {
        return _width;
    }

    @Override
    public int getHeight()
    {
        return _height;
    }

    @Override
    public String getName()
    {
        return _name;
    }

    @Override
    public CollisionTemplate getCollision()
    {
        return _collision;
    }

    @Override
    public InventoryTemplate getInventory()
    {
        return _inventory;
    }
}
