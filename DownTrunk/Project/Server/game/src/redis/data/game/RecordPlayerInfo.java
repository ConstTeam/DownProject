package redis.data.game;

public abstract class RecordPlayerInfo {

	/** ���id */
	private int playerId;

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
}
