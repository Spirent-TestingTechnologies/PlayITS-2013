package com.testingtech.ttworkbench.play.simulation.car;

import java.util.Hashtable;
import java.util.Map;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.testingtech.ttworkbench.play.generated.PROTO_API.ACTIONS.BlockingInterface;
import com.testingtech.ttworkbench.play.generated.PROTO_API.Void;
import com.testingtech.ttworkbench.play.generated.PROTO_API.carInitType;
import com.testingtech.ttworkbench.play.generated.PROTO_API.onOffEngineType;
import com.testingtech.ttworkbench.play.generated.PROTO_API.speedType;
import com.testingtech.ttworkbench.play.generated.PROTO_API.trackType;
import com.testingtech.ttworkbench.play.generated.PROTO_API.warning.EnumValue;
import com.testingtech.ttworkbench.play.generated.PROTO_API.warningType;
import com.testingtech.ttworkbench.play.generated.PROTO_API.widgetExit;

public class ActionsServiceImpl implements BlockingInterface {
	CarModel carModel;
	Map <Car, Socket> carSocket = new Hashtable<Car, Socket>();

	public ActionsServiceImpl(CarModel car) {
		this.carModel = car;
	}

	@Override
	public Void aPIOnOffEngineType(RpcController controller,
			onOffEngineType request) throws ServiceException {
		long id = request.getCarId();
		Car car = getCar(id);
		car.setEngine(request.getEngineStatus());
		return nil();
	}

	@Override
	public Void aPISpeedType(RpcController controller, speedType request)
			throws ServiceException {
		long id = request.getCarId();
		Car car = getCar(id);
		car.speed = request.getSpeed();
		
		return nil();
	}

	@Override
	public Void aPITrackType(RpcController controller, trackType request)
			throws ServiceException {
		// TODO set car track depending on name of the track/map
		// 		-> just init the car again
		long id = request.getCarId();
		Car car = getCar(id);
		car.setTrack(request.getTrackName());

		return nil();
	}

	@Override
	public Void aPIWarningType(RpcController controller, warningType request)
			throws ServiceException {
		
		GPSposition position = new GPSposition(request.getGpsPos().getLongitude(), request.getGpsPos().getLatitude());
		Warnings warning  = convertWarn(request.getWarningName().getEnumValue());
		WarningType wt = new WarningType(warning,position);

		carModel.addWarning(wt,request.getCarId());
		return nil();
	}

	private static Warnings convertWarn(EnumValue warning) {
		Warnings enumValue;
		switch (warning) {
		case accident:
			enumValue = Warnings.ACCIDENT;
			break;
		case deer:
			enumValue = Warnings.DEER;
			break;
		case fog:
			enumValue = Warnings.FOG;
			break;
		case ice:
			enumValue = Warnings.ICE;
			break;
		case rain:
			enumValue = Warnings.RAIN;
			break;
		case snow:
			enumValue = Warnings.SNOW;
			break;
		default:
			throw new IllegalArgumentException("Unknown warning type "+warning);
		}
		return enumValue;
	}

	@Override
	public Void aPICarInitType(RpcController controller, carInitType request)
			throws ServiceException {
		String trackName = request.getTrackName();

		// updates the initial car setup with the wanted field values
		final Car car = new Car(0, request.getMaxSpeed(), 2.0, 
				request.getFuelFilling(), request.getFuelConsumption(), request.getLightSensorExists(), true, request.hasFuelFilling(), true, 
				request.getEspSensorExists(), request.getAbsSensorExists(), false, request.getFogLightSensorExists(), trackName);
		carModel.addCar(car);
		Socket socket = new Socket(car,(int) request.getTtcnEventsPort(),request.getTtcnEventsHostName());
		carSocket.put(car, socket);
		String threadName = "Car #"+car.customID+" events@"+request.getTtcnEventsHostName()+":"+request.getTtcnEventsPort();
		Thread thread = new Thread(socket, threadName);
		thread.start();
		return nil();
	}

	@Override
	public Void aPIWidgetExit(RpcController controller, widgetExit request)
			throws ServiceException {
		// set the car to be destructed in the next update or immediately
		// (depending on simulation)
		long id = request.getCarId();
		Car car = getCar(id);
		car.disposeCar();
		return nil();
	}

	protected Void nil() {
		return Void.newBuilder().build();
	}

	private Car getCar(long id) throws ServiceException {
		Car car = carModel.get(id);
		if (car == null){
			throw new ServiceException("No car with this ID"+ id);
		}
		return car;
	}

}
