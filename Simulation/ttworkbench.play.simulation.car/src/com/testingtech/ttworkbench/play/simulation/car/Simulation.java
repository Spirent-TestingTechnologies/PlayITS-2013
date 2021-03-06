package com.testingtech.ttworkbench.play.simulation.car;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Executors;

import com.googlecode.protobuf.socketrpc.PersistentRpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.RpcServer;
import com.googlecode.protobuf.socketrpc.ServerRpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;
import com.testingtech.ttworkbench.play.generated.PROTO_API;

public class Simulation {

	private RpcServer server;
	private int serverPort;

	@SuppressWarnings("unused")
	private static Queue<GPSposition> getMap(File mapFile) {
			return new KMLtoGPSQueue(mapFile).getPositions();
		
	}

	public static void main(String[] args) {

		if (args.length == 1) {
			int index = 0;

			int serverPort = parsePort(args, index++);

			// a service implementation should be able to handle multiple cars
			// that's why map file, client host and port should be
			// delivered by the initCarType or another init function

			new Simulation().startServerBlocking(serverPort);

		} else {
			System.out
					.println("Usage: java -jar Simulation.jar <serverPort>");
		}
	}

	private void startServerBlocking(int serverPort) {
		createServer(serverPort);
		server.run();
	}

	private static int parsePort(String[] args, int argIndex)
			throws IllegalArgumentException {
		if (argIndex >= args.length) {
			throw new IllegalArgumentException(
					"Missing port argument at index " + argIndex);
		}
		try {
			int portNumber = Integer.parseInt(args[argIndex]);
			return portNumber;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid port " + args[argIndex]);
		}
	}

	public void startServer(int p_serverPort) throws IOException {
		createServer(p_serverPort);
		server.startServer();
	}

	private void createServer(int p_serverPort) {
		this.serverPort = p_serverPort;
		CarModel carModel = new CarModel();

		ServerRpcConnectionFactory rpcConnectionFactory = 
				PersistentRpcConnectionFactory.createServerInstance(SocketRpcConnectionFactories
				.createServerRpcConnectionFactory(p_serverPort));
		server = new RpcServer(rpcConnectionFactory,
				Executors.newFixedThreadPool(10), true);
		ActionsServiceImpl asi = new ActionsServiceImpl(carModel);
		server.registerBlockingService(PROTO_API.ACTIONS
				.newReflectiveBlockingService(asi)); // For blocking impl
	}

	public void stopServer() throws IOException {
		if (server != null) {
			server.shutDown();
		}
		server = null;
	}

	public int getServerPort() {
		return serverPort;
	}

}
