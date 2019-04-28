package module.scene;

import net.ISession;
import redis.data.PlayerInfo;

public interface ISceneAction {
	
	/** ��Ҽ�����Ϸ */
	public int joinGame(PlayerInfo player, ISession session);

	/** ����˳���Ϸ */
	public int exitGame(PlayerInfo player);

	/** ����ؽ����� */
	public void resetPlayer(PlayerInfo player);

	/** �ط�������Ϣ */
	public void resend(PlayerInfo player);
	
	/** ͬ��������������ҵ���Ϣ */
	public void otherPlayerSyncMessage(PlayerInfo player);
	
	/** �˳�����-֪ͨ�Լ��˳��������Ϣ */
	public void exitRoomMessage(PlayerInfo player);
	
	/** �˳�����-֪ͨ�������˳��������Ϣ */
	public void exitRoomSync(int playerId);
	
	/** �˳�����-Ԥ�ж��Ƿ����˳� */
	public boolean exitRoomPre(PlayerInfo player, ISession session, int state);
	
	/** ���� */
	public void gameStart();

	/** ��Ϸ���� */
	public void gameEnd();

	/** ���� */
	public void giveup(int loser);
	
	/** ���� */
	public void settlement(int loser);

	/** ���ٷ��� */
	public void destroy();
}
