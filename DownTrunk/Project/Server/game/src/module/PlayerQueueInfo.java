package module;

/**
 * ��ҷ��������Ϣ
 *
 * @author hanshuwan
 */
public class PlayerQueueInfo {

    /**
     * ���Ψһ��Ϸid
     */
    private String playerId;

    /**
     * �������ʱ��
     */
    private long time;

    /**
     * ���ֶ�
     */
    private long happyBeans;

    /**
     * ��ҵȼ�
     */
    private int level;

    /**
     * ��Ҷ�λ����
     */
    private int rankXp;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getHappyBeans() {
        return happyBeans;
    }

    public void setHappyBeans(long happyBeans) {
        this.happyBeans = happyBeans;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRankXp() {
        return rankXp;
    }

    public void setRankXp(int rankXp) {
        this.rankXp = rankXp;
    }

}
