package db.module.ladder;

public class LadderInfo {
	
	private int playerId;
	private int ladderId;
	private int starCount;
	private int mmrScore;
	
	public int getPlayerID() {
		return playerId;
	}

	public void setPlayerID(int playerId) {
		this.playerId = playerId;
	}
	
	public void setLadderId(int ladderId) {
		this.ladderId = ladderId;
	}
	
	public int getLadderId() {
		return ladderId;
	}
	
	
	public void setStarCount(int starCount) {
		this.starCount = starCount;
	}
	
	public int getStarCount() {
		return starCount;
	}
	
	public void setMMRScore(int mmrScore) {
		this.mmrScore = mmrScore;
	}
	
	public int getMMRScore() {
		return mmrScore;
	}
}
