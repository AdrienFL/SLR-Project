package com.example.demo;

import java.io.IOException;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import server.RequestProcessor;
import server.WebServer;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;


@SpringBootApplication
@RestController
public class Application {

	private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
	
	private static final String host = "localhost";
	private static final int port = 80;
	
	private static HashMap<String, Factory> factories = new HashMap<>();
	
	
	
	private static ObjectMapper objectMapper;
	
	public static void main(String[] args) {
		SimulationRegisterModuleConfig config = new SimulationRegisterModuleConfig();
		objectMapper = config.objectMapper1();

		objectMapper = new ObjectMapper();
		PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
				.allowIfSubType(PositionedShape.class.getPackageName())
				.allowIfSubType(Component.class.getPackageName())
				.allowIfSubType(fr.tp.inf112.projects.graph.impl.BasicVertex.class.getPackageName())
				.allowIfSubType(ArrayList.class.getName())
				.allowIfSubType(LinkedHashSet.class.getName())
				.build();
		
		
		WebServer server = new WebServer();
			
		
		objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
		
		SpringApplication.run(Application.class, args);
	}
	
	@GetMapping("/")
	public String index() {
		return "Hello World !";
	}
	
	@GetMapping("/start/{id}")
	public String start(@PathVariable String id) {
		if (factories.containsKey(id)) {
			LOGGER.fine(id + " already running, sending current state...");
			Factory f = factories.get(id);
			try {
				return objectMapper.writeValueAsString(f);
			} catch (JsonProcessingException e) {
				LOGGER.severe(e.getMessage());
				return null;
			}
		}
		try (Socket socket = new Socket(host, port)) {
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(id);
			InputStream input = socket.getInputStream();
			ObjectInputStream objIn = new ObjectInputStream(input);
			Factory f = (Factory) objIn.readObject();
			factories.put(f.getId(), f);
			LOGGER.info("canvas " + id + " loaded");
			LOGGER.info("start simulation...");
			f.startSimulation();
			f.setSimulationStarted(true);

			return objectMapper.writeValueAsString(f);
		} catch (ClassNotFoundException | IOException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return null;
		
	}
	
	
	@GetMapping("/getFactory/{id}")
	public String getFactory(@PathVariable String id) {
		try (Socket socket = new Socket(host, port)) {
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(id);
			InputStream input = socket.getInputStream();
			ObjectInputStream objIn = new ObjectInputStream(input);
			Factory f = (Factory) objIn.readObject();
	
			return objectMapper.writeValueAsString(f);
		} catch (ClassNotFoundException | IOException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return null;
	}
	
	
	@GetMapping("/stop/{id}")
	public String stop(@PathVariable String id) {
		LOGGER.fine("Removing canvas " + id + "...");
		Factory f = factories.remove(id);
		if (f == null) {
			LOGGER.info("simulation " + id + " not found");
			return "Simulation " + id + " not found";
		}
		LOGGER.info("stopping the simulation...");
		f.stopSimulation();
	
		try {
			return objectMapper.writeValueAsString(f);
		} catch (JsonProcessingException e) {
			LOGGER.severe(e.getMessage());
		}
		return null;
	}
}
