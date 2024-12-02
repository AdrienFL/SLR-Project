package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;

public class RemoteSimulatorController2 extends SimulatorController {

	private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());

	Thread update;
	Factory test;

	public RemoteSimulatorController2(CanvasPersistenceManager persistenceManager) {
		super(persistenceManager);
	}

	public RemoteSimulatorController2(Factory factoryModel, CanvasPersistenceManager persistenceManager) {
		super(factoryModel, persistenceManager);
		test = factoryModel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startAnimation() {

		LOGGER.info(((Factory) getCanvas()).getObservers().toString());
		HTTPClient client = new HTTPClient("/simulation/" + getCanvas().getId());
		Factory f = client.requestFactory(true);
		if (f == null) {
			LOGGER.info(getCanvas().getId() + "not found");
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
//		test.startSimulation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stopAnimation() {
		HTTPClient client = new HTTPClient("/stop/" + getCanvas().getId());
		Factory f = client.requestFactory(true);
//		((Factory) getCanvas()).update(f);
		setCanvas(f);
	}

	private void updateViewer() throws InterruptedException, URISyntaxException, IOException {
		while (((Factory) getCanvas()).isSimulationStarted()) {
			HTTPClient client = new HTTPClient("/simulation/" + getCanvas().getId());
			Factory f = client.requestFactory(true);
			((Factory) getCanvas()).update(f);
			Thread.sleep(100);
		}
	}
}
