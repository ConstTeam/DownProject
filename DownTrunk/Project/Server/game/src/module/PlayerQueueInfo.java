package module;

/**
 * 玩家分配队列信息
 *
 * @author hanshuwan
 */
public class PlayerQueueInfo {

    /**
     * 玩家唯一游戏id
     */
    private String playerId;

    /**
     * 进入队列时间
     */
    private long time;

    /**
     * 欢乐豆
     */
    private long happyBeans;

    /**
     * 玩家等级
     */
    private int level;

    /**
     * 玩家段位分数
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
