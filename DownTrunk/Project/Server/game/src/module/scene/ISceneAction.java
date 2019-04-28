package module.scene;

import net.ISession;
import redis.data.PlayerInfo;

public interface ISceneAction {
	
	/** 玩家加入游戏 */
	public int joinGame(PlayerInfo player, ISession session);

	/** 玩家退出游戏 */
	public int exitGame(PlayerInfo player);

	/** 玩家重进房间 */
	public void resetPlayer(PlayerInfo player);

	/** 重发房间消息 */
	public void resend(PlayerInfo player);
	
	/** 同步房间内其他玩家的消息 */
	public void otherPlayerSyncMessage(PlayerInfo player);
	
	/** 退出房间-通知自己退出房间的消息 */
	public void exitRoomMessage(PlayerInfo player);
	
	/** 退出房间-通知其他人退出房间的消息 */
	public void exitRoomSync(int playerId);
	
	/** 退出房间-预判断是否能退出 */
	public boolean exitRoomPre(PlayerInfo player, ISession session, int state);
	
	/** 开局 */
	public void gameStart();

	/** 游戏结束 */
	public void gameEnd();

	/** 认输 */
	public void giveup(int loser);
	
	/** 结算 */
	public void settlement(int loser);

	/** 销毁房间 */
	public void destroy();
}
