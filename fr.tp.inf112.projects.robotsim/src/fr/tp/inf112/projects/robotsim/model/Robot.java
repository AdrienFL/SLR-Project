package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.tools.javac.Main;

import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Robot extends Component {
	
	private static final long serialVersionUID = -1218857231970296747L;

	private static final Style STYLE = new ComponentStyle(RGBColor.GREEN, RGBColor.BLACK, 3.0f, null);

	private static final Style BLOCKED_STYLE = new ComponentStyle(RGBColor.RED, RGBColor.BLACK, 3.0f, new float[]{4.0f});

	private final Battery battery;
	
	private int speed;
	
	
	private List<Component> targetComponents;
	
	@JsonIgnore
	private transient Iterator<Component> targetComponentsIterator;
	
	private Component currTargetComponent;
	
	@JsonIgnore
	private transient Iterator<Position> currentPathPositionsIter;
	
	@JsonInclude
	private transient boolean blocked;
	
	private Position nextPosition;
	
	private FactoryPathFinder pathFinder;

	final Logger LOGGER = Logger.getLogger(Main.class.getName());

	public Robot(final Factory factory,
				 final FactoryPathFinder pathFinder,
				 final CircularShape shape,
				 final Battery battery,
				 final String name ) {
		super(factory, shape, name);
		
		this.pathFinder = pathFinder;
		
		this.battery = battery;
		
		targetComponents = new ArrayList<>();
		currTargetComponent = null;
		currentPathPositionsIter = null;
		speed = 5;
		blocked = false;
		nextPosition = null;
	}

	public Robot() {
		this(null, null, null, null, null);
		blocked = false;
		this.targetComponents = null;
		this.pathFinder = null;
	}
	
	@Override
	public String toString() {
		return super.toString() + " battery=" + battery + "]";
	}

	protected int getSpeed() {
		return this == null ? 0 : speed;
	}

	protected void setSpeed(final int speed) {
		this.speed = speed;
	}
	
	@JsonInclude
	private List<Component> getTargetComponents() {
		if (targetComponents == null) {
			targetComponents = new ArrayList<>();
		}
		
		return targetComponents;
	}
	
	public Position getNextPosition() {
		return this.nextPosition;
	}
	
	public boolean addTargetComponent(final Component targetComponent) {
		return getTargetComponents().add(targetComponent);
	}
	
	public boolean removeTargetComponent(final Component targetComponent) {
		return getTargetComponents().remove(targetComponent);
	}
	
	@Override
	public boolean isMobile() {
		return true;
	}

	@Override
	public boolean behave() {
		if (getTargetComponents().isEmpty()) {
			return false;
		}
		
		if (currTargetComponent == null || hasReachedCurrentTarget()) {
			currTargetComponent = nextTargetComponentToVisit();
		}
		
		computePathToCurrentTargetComponent();

		return moveToNextPathPosition() != 0;
	}
		
	private Component nextTargetComponentToVisit() {
		if (targetComponentsIterator == null || !targetComponentsIterator.hasNext()) {
			targetComponentsIterator = getTargetComponents().iterator();
		}
		
		return targetComponentsIterator.hasNext() ? targetComponentsIterator.next() : null;
	}
	
	private Position findFreeNeighbourPosition() {
		int x = getNextPosition().getxCoordinate() - getPosition().getxCoordinate();
		int y = getNextPosition().getyCoordinate() - getPosition().getyCoordinate();
		
		Position pos1 = new Position(getPosition().getxCoordinate() + getHeight(), getPosition().getyCoordinate()-getHeight());
		Position pos2 = new Position(getPosition().getxCoordinate() -getHeight(), getPosition().getyCoordinate()-getHeight());
		Position pos3 = new Position(getPosition().getxCoordinate() -getHeight(), getPosition().getyCoordinate()+getHeight());
		
		if (getFactory().hasObstacleAt(new CircularShape(pos1.getxCoordinate(), pos1.getyCoordinate(), this.getHeight())) == false) {
			return pos1;
		}
		if (getFactory().hasObstacleAt(new CircularShape(pos2.getxCoordinate(), pos2.getyCoordinate(), this.getHeight())) == false) {
			return pos2;
		}
		if (getFactory().hasObstacleAt(new CircularShape(pos3.getxCoordinate(), pos3.getyCoordinate(), this.getHeight())) == false) {
			return pos3;
		}
		return null;
	}
	
	private int moveToNextPathPosition() {
		final Motion motion = computeMotion();
		
		int displacement = motion == null ? 0 : motion.moveToTarget();
		
		if (displacement != 0) {
			notifyObservers();
		}
		else if (isBlocked()) {
			final Position freeNeighbouringPosition = findFreeNeighbourPosition();
			
			if (freeNeighbouringPosition != null) {
				LOGGER.info(freeNeighbouringPosition.getxCoordinate() + " , " + freeNeighbouringPosition.getyCoordinate());
				nextPosition = freeNeighbouringPosition;
				displacement = moveToNextPathPosition();
				computePathToCurrentTargetComponent();
			}
		}
		return displacement;
	}
	
	private void computePathToCurrentTargetComponent() {
		final List<Position> currentPathPositions = pathFinder.findPath(this, currTargetComponent);
		currentPathPositionsIter = currentPathPositions.iterator();
	}
	
	private Motion computeMotion() {
		if (!currentPathPositionsIter.hasNext()) {
			
			// There is no free path to the target
			blocked = true;
			
			return null;
		}
		
		final Position nextPosition = this.nextPosition == null ? currentPathPositionsIter.next() : this.nextPosition;
		final PositionedShape shape = new RectangularShape(nextPosition.getxCoordinate(),
				   										   nextPosition.getyCoordinate(),
				   										   2,
				   										   2);
		if (getFactory().hasMobileComponentAt(shape, this)) {
			this.nextPosition = nextPosition;
			LOGGER.info("Robot detected");
			return null;
		}

		this.nextPosition = null;
		
		return new Motion(getPosition(), nextPosition);
	}
	
	private boolean hasReachedCurrentTarget() {
		return getPositionedShape().overlays(currTargetComponent.getPositionedShape());
	}
	
	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}
	
	
	
	public boolean isBlocked() {
		final Position nextPosition = getNextPosition();
		if (nextPosition == null) {
			return false;
		}
		final CircularShape nextPositionShape = new CircularShape(nextPosition.getxCoordinate(), nextPosition.getyCoordinate(), 1);
		
		
		final Robot otherRobot = (Robot) getFactory().getMobileComponentAt(nextPositionShape, this);
		return otherRobot != null && getPosition().equals(otherRobot.getNextPosition());
	}
	
	
	@JsonInclude
	public boolean getBlocked() {
		return blocked;
	}
	
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	@Override
	public Style getStyle() {
		return blocked ? BLOCKED_STYLE : STYLE;
	}
}
