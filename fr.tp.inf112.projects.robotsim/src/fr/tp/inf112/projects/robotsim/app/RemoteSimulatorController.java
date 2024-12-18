package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.RemoteFactoryPersistenceManager;

public class RemoteSimulatorController extends SimulatorController {

	private RemoteFactoryPersistenceManager persistenceManager;
	private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());

	Thread update;
	Factory myFactoryModel;
	
	public RemoteSimulatorController(RemoteFactoryPersistenceManager persistenceManager) {
		super(persistenceManager);
		
	}
	
	public RemoteSimulatorController(Factory factoryModel, RemoteFactoryPersistenceManager persistenceManager) {
		super(factoryModel, persistenceManager);
		myFactoryModel = factoryModel;
		
	}
	
	
	@Override
	public void startAnimation() {
		System.out.println(getCanvas().getId());
		HTTPClient client = new HTTPClient("/start/" + getCanvas().getId());
		Factory f = client.requestFactory(true);
		System.out.println(f.getSimulationStarted());
		
		if (f == null) {
			return;
		}
		((Factory) getCanvas()).update(f);

		update = new Thread(() -> {
			try {
				updateViewer();
			} catch (InterruptedException | URISyntaxException | IOException e) {
				LOGGER.severe(e.getMessage());
			}
		});
		update.run();
	}

		
		
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stopAnimation() {
		HttpClient httpClient = HttpClient.newHttpClient();

		String Id =  getCanvas().getId();
		URI uri;
		try {
			uri = new URI("http", null, "localhost", 8080, "/stop/" + Id, null, null);
			HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			HttpResponse<String> response;
			try {
				response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				ObjectMapper objectMapper = new ObjectMapper();
				Factory factory;
				factory = objectMapper.readValue(response.body(), Factory.class);
			} catch (IOException | InterruptedException e) {
				LOGGER.severe(e.getMessage());
			}
		} catch (URISyntaxException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	
	private Factory getFactory() {
		HttpClient httpClient = HttpClient.newHttpClient();

		String Id =  getCanvas().getId();
		URI uri;
		try {
			uri = new URI("http", null, "localhost", 8080, "/getFactory/" + Id, null, null);
			HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			HttpResponse<String> response;
			try {
				response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				ObjectMapper objectMapper = new ObjectMapper();
				Factory factory;
				factory = objectMapper.readValue(response.body(), Factory.class);
				return factory;
			} catch (IOException | InterruptedException e) {
				LOGGER.severe(e.getMessage());
			}
		} catch (URISyntaxException e) {
			LOGGER.severe(e.getMessage());
		}
		
		return null;
		
	}


	private void updateViewer() throws InterruptedException, URISyntaxException, IOException {
		HTTPClient client = new HTTPClient("/start/" + getCanvas().getId());
		Factory f = client.requestFactory(true);
		System.out.println(f.isSimulationStarted());
		while (f.isSimulationStarted()) {
			
			((Factory) getCanvas()).update(f);
			Thread.sleep(100);
		}
	}

}

/*	
	@Override
	public void setCanvas(final Canvas canvasModel) {
		final List<Observer> observers = ((Factory) getCanvas()).getObservers();
		
		super.setCanvas(canvasModel);
		
		for(final Observer observer : observers) {
			((Factory) getCanvas()).addObserver(observer);
		}
		
		((Factory) getCanvas()).notifyObservers();
	}
}
*/
