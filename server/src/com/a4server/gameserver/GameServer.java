package com.a4server.gameserver;

import com.a4server.Config;
import com.a4server.Database;
import com.a4server.Server;
import com.a4server.ThreadPoolManager;
import com.a4server.gameserver.idfactory.IdFactory;
import com.a4server.gameserver.model.World;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import com.a4server.gameserver.model.objects.impl.ObjectClasses;
import com.a4server.gameserver.network.packets.GamePktClient;
import com.a4server.gameserver.network.packets.GameServerHandler;
import com.a4server.gameserver.network.websocket.GameWebsocketServer;
import com.a4server.util.network.AsyncPacketReader;
import com.a4server.util.network.PacketDecoder;
import com.a4server.util.network.PacketEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Locale;

import static com.a4server.Config.IS_WEBSOCKET_SERVER;

/**
 * Created by arksu on 01.01.2015.
 */
public class GameServer
{
	private static Logger _log;
	private static GameServer _instance;
	private InetAddress _bindAddress = null;
	private final AsyncPacketReader<GamePktClient> _reader;

	public static void main(String[] args) throws Exception
	{
		Locale.setDefault(Locale.ROOT);
		System.setProperty("server.logfile", "logs/game_server.log");
		_log = LoggerFactory.getLogger(GameServer.class);

		Server.serverMode = Server.MODE_GAMESERVER;
		for (String arg : args)
		{
			// если запущен с флагом - проведем инициализацию базы
			if ("-initdb".equals(arg))
			{
				Database._needInitialize = true;
			}
		}

		_instance = new GameServer();
	}

	private GameServer() throws Exception
	{
		Config.load();
		ObjectClasses.init();

		logPrintSection("Database");
		Database.getInstance();

		long serverLoadStart = System.currentTimeMillis();
		_instance = this;
		_log.info(getClass().getSimpleName() + ": used mem: " + getUsedMemoryMB() + "MB");

		IdFactory _idFactory = IdFactory.getInstance();

		if (!_idFactory.isInitialized())
		{
			_log.error(getClass().getSimpleName() + ": Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}

		ThreadPoolManager.getInstance();
		logPrintSection("Objects");
		ObjectsFactory.getInstance().loadInternalConfig();
		logPrintSection("World");
		GameTimeController.init();
		World.getInstance();

		long serverLoadEnd = System.currentTimeMillis();
		_log.info("Server Loaded in " + ((serverLoadEnd - serverLoadStart) / 1000) + " seconds");

		// start network
		logPrintSection("Network");
		if (!Config.GAME_SERVER_HOST.equals("*"))
		{
			try
			{
				_bindAddress = InetAddress.getByName(Config.GAME_SERVER_HOST);
			}
			catch (UnknownHostException e)
			{
				_log.warn("WARNING: The LoginServer bind address is invalid, using all avaliable IPs. Reason: " + e
						.getMessage(), e);
			}
		}

		_reader = new AsyncPacketReader<>(Config.GAME_NET_READER_THREADS, "gamePktReader");

		try
		{
			if (IS_WEBSOCKET_SERVER)
			{
				GameWebsocketServer gameWebsocketServer = new GameWebsocketServer(new InetSocketAddress(Config.GAME_SERVER_HOST, Config.GAME_WEBSOCKET_SERVER_PORT));
				gameWebsocketServer.run();
				_log.info(getClass().getSimpleName() + ": websocket finished");
			}
			else
			{
				StartListenNetty();
			}
		}
		catch (Exception e)
		{
			_log.error("ERROR: Start listen failed. Reason: " + e.getMessage(), e);
			System.exit(-1);
		}

	}

	private void StartListenNetty() throws Exception
	{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup(Config.GAME_NET_WORKER_THREADS);
		try
		{
			ServerBootstrap b = new ServerBootstrap();
			b.option(ChannelOption.SO_BACKLOG, 256);
//			b.option(ChannelOption.TCP_NODELAY, true);
			b.group(bossGroup, workerGroup)
			 .channel(NioServerSocketChannel.class)
			 .childHandler(new ChannelInitializer<SocketChannel>()
			 {
				 @Override
				 public void initChannel(SocketChannel ch) throws Exception
				 {
					 ch.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new GameServerHandler(_reader));
				 }
			 });

			_log.info("Game server bind to " + Config.GAME_SERVER_HOST + " : " + Config.GAME_SERVER_PORT + "...");
			// Bind and start to accept incoming connections.
			ChannelFuture future = b.bind(_bindAddress, Config.GAME_SERVER_PORT);

			System.gc();
			// maxMemory is the upper limit the jvm can use, totalMemory the size of
			// the current allocation pool, freeMemory the unused memory in the
			// allocation pool
			long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory())
			                + Runtime.getRuntime().freeMemory()) / 1048576;
			long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
			long usedMem = Runtime.getRuntime().totalMemory() / 1048576;
			_log.info(getClass()
					          .getSimpleName() + ": Started, free memory " + freeMem + " Mb of " + totalMem + " Mb used :" + usedMem + " Mb");

			future.sync().channel().closeFuture().sync();
		}
		finally
		{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; // ;
	}

	public static void logPrintSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
}
