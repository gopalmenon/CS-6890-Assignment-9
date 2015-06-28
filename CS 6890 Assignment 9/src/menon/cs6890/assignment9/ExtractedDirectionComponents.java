package menon.cs6890.assignment9;

public class ExtractedDirectionComponents {
	
	private String leaveBuilding;
	private String arriveAtBuilding;
	private String walkInDirection;
	private String turnToDirection;
	
	public ExtractedDirectionComponents(String leaveBuilding, String arriveAtBuilding, String walkInDirection, String turnToDirection) {
		this.leaveBuilding = leaveBuilding;
		this.arriveAtBuilding = arriveAtBuilding;
		this.walkInDirection = walkInDirection;
		this.turnToDirection = turnToDirection;
	}

	public String getLeaveBuilding() {
		return leaveBuilding;
	}

	public String getArriveAtBuilding() {
		return arriveAtBuilding;
	}

	public String getWalkInDirection() {
		return walkInDirection;
	}

	public String getTurnToDirection() {
		return turnToDirection;
	}
	
	
}
