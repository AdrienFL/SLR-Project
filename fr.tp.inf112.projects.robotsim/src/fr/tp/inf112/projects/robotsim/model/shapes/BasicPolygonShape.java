package fr.tp.inf112.projects.robotsim.model.shapes;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.tp.inf112.projects.canvas.model.PolygonShape;
import fr.tp.inf112.projects.canvas.model.Vertex;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;

public class BasicPolygonShape extends PositionedShape implements PolygonShape {
	
	private static final long serialVersionUID = -1764316101910546849L;

	private final Set<Vertex> vertices;
	
	public BasicPolygonShape() {
        super(0, 0);

        this.vertices = new LinkedHashSet<>();
        final int baselineSize = 3;
        final int xCoordinate = 10;
        final int yCoordinate = 165;
        final int width = 10;
        final int height = 30;
        addVertex(new BasicVertex(xCoordinate, yCoordinate));
        addVertex(new BasicVertex(xCoordinate + width, yCoordinate));
        addVertex(new BasicVertex(xCoordinate + width, yCoordinate + height - baselineSize));
        addVertex(new BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height - baselineSize));
        addVertex(new BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height));
        addVertex(new BasicVertex(xCoordinate - baselineSize, yCoordinate + height));
        addVertex(new BasicVertex(xCoordinate - baselineSize, yCoordinate + height - baselineSize));
        addVertex(new BasicVertex(xCoordinate, yCoordinate + height - baselineSize));
    }
	
	
	
	@JsonIgnore
	@Override
	public Set<Vertex> getVertices() {
		return vertices;
	}
	
	public boolean addVertex(final Vertex vertex) {
		final boolean added = getVertices().add(vertex);
		
		updatePosition();
		
		return added;
	}
	
	private void updatePosition() {
		int minxCoordinate = Integer.MAX_VALUE;
		int minyCoordinate = minxCoordinate;
		
		for (final Vertex vertex : getVertices()) {
			minxCoordinate = Math.min(minxCoordinate, vertex.getxCoordinate());
			minyCoordinate = Math.min(minyCoordinate, vertex.getyCoordinate());
		}
		
		setxCoordinate(minxCoordinate);
		setyCoordinate(minyCoordinate);
	}
	
	@JsonIgnore
	@Override
	public int getWidth() {
		int minCoordinate = Integer.MAX_VALUE;
		int maxCoordinate = 0;
		
		for (final Vertex vertex : getVertices()) {
			final int coordinate = vertex.getxCoordinate();

			minCoordinate = Math.min(minCoordinate, coordinate);
			maxCoordinate = Math.max(maxCoordinate, coordinate);
		}
		
		return (this == null) ? 0 : maxCoordinate - minCoordinate;
	}

	@JsonIgnore
	@Override
	public int getHeight() {
		int minCoordinate = Integer.MAX_VALUE;
		int maxCoordinate = 0;
		
		for (final Vertex vertex : getVertices()) {
			final int coordinate = vertex.getyCoordinate();

			minCoordinate = Math.min(minCoordinate, coordinate);
			maxCoordinate = Math.max(maxCoordinate, coordinate);
		}
		
		return (this == null) ? 0 : maxCoordinate - minCoordinate;
	}
}
