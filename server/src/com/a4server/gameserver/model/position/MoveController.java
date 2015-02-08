package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.MoveObject;

/**
 * реализует передвижения объектов
 * расчитывает новую позицию. ставит ее клиенту и уведомляет всех о смене позиции
 * Created by arksu on 09.01.2015.
 */
public abstract class MoveController
{
    protected MoveObject _activeObject;

    /**
     * обработать тик передвижения
     * @return объект в который уперлись
     */
    public abstract GameObject updateMove();

}
