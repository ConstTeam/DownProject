package module.fight;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.model.card.CardModel;
import module.area.Area;
import module.card.CardBase;
import module.card.TroopCard;
import module.scene.GameRoom;
import module.scene.SelectObject;
import sys.GameTimer;
import util.ErrorPrint;

public class BattleNAIRole extends BattleRole {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleNAIRole.class);
	
	private boolean stop = false;
	
	public BattleNAIRole(String nickname, int hp, int uid) {
		super(0, nickname, hp, uid);
		this.setRobot(true);
	}

	public void turn(GameRoom room) {
		action(room);
	}
	
	public void stop(GameRoom room) {
		this.stop = true;
	}
	
	public void action(GameRoom room) {
		try {
			if (this.stop) {
				return;
			}
			boolean actionDone = false;
			actionDone = upAreaLv_IfAllZero(room);
			if (actionDone == false) {
				actionDone = playCard_LowCost(room);
			}
			if (actionDone == false) {
				actionDone = upAreaLv(room);
			}
			if (actionDone == false) {
				actionDone = playAllCard(room);
			}
			if (actionDone == false) {
				// 无可施放的卡牌, 可攻击的部队开始攻击
				actionDone = attack(room);
				if (actionDone == false) {
					//以上都没有可执行的结束回合
					room.turnFinish(this.getPlayerId());
					return;
				}
			}
			GameTimer.getScheduled().schedule(() -> action(room), 3000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.error("Battle Nomal AI Action 出错：" + e);
			ErrorPrint.print(e);
		}
	}
	
	private boolean upAreaLv_IfAllZero(GameRoom room) {
		if (this.getArea(0).getLevel() == 0 && this.getArea(1).getLevel() == 0 && this.getArea(2).getLevel() == 0) {
			int areaIndex = getUpAreaFirstIndex(room);
			return areaLvUp(room, areaIndex);
		}
		return false;
	}
	
	private boolean upAreaLv(GameRoom room) {
		int areaIndex = getUpAreaFirstIndex(room);
		if (areaIndex  >= 0) {
			return room.areaLvUp(this.getPlayerId(), areaIndex);
		}
		return false;
	}
	
	private int getUpAreaFirstIndex(GameRoom room) {
		//优先升级0级区域
		for (int i=0; i<Area.ROW_MAX_INDEX; i++) {
			Area area = this.getArea(i);
			if (area.getLevel() == 0) {
				return area.getIndex();
			}
		}
		Area upArea = null;
		for (int i=0; i<Area.ROW_MAX_INDEX; i++) {
			Area area = this.getArea(i);
			if (area.getLevel() < Area.MAX_LEVEL && (upArea == null || upArea.getLevel() > area.getLevel()) ) {
				upArea = area;
			}
		}
		if (upArea != null) {
			return upArea.getIndex();
		}
		//第二优先级优先对面有部队的区域
		/*
		int enemyId = room.getEnemyId(this.getPlayerId()); // 敌方玩家Id
		BattleRole enemy = room.getBattleRole(enemyId); // 敌方玩家
		for (int areaIndex = 0; areaIndex<Area.ROW_MAX_INDEX; areaIndex++) {
			Area oppoArea = enemy.getArea(areaIndex);
			ArrayList<TroopCard> tempList = oppoArea.getTroops();
			if (tempList.size() > 0) {
				return areaIndex;
			}
		}*/
		return -1;
	}
	
	private boolean playCard_LowCost(GameRoom room) {
		try {
			int diff = this.getResource() - this.getLvUpResource();
			CardBase card = null;
			int minCost = 0;
			ArrayList<CardBase> cardList = this.getHandCards();
			for (int i=cardList.size()-1; i>=0; i--) {
				int cost = cardList.get(i).getCost(this);
				if (cost > 0 && cost <= this.getResource()) {
					if (minCost==0 || minCost > cost) {
						card = cardList.get(i);
						minCost = cost;
					}
				}
			}
			boolean result = false;
			if (card != null && diff >= 0 && diff <= minCost) {
				result = doCard(room, card);
			}
			return result;
		} catch (Exception e) {
			logger.error("Robot playCard_LowCost 出错：" + e);
			ErrorPrint.print(e);
		}
		return false;
	}
	
	private boolean playAllCard(GameRoom room) {
		try {
			CardBase card = null;
			ArrayList<CardBase> cardList = this.getHandCards();
			boolean result = false;
			for (int i=cardList.size()-1; i>=0; i--) {
				int cost = cardList.get(i).getCost(this);
				if (cost <= this.getResource()) {
					card = cardList.get(i);
					result = doCard(room, card);
					if (result == true) {
						break;
					}
				}
			}
			return result;
		} catch (Exception e) {
			logger.error("Robot playAllCard 出错：" + e);
			ErrorPrint.print(e);
		}
		return false;
	}
	
	private boolean doCard(GameRoom room, CardBase card) {
		boolean result = false;
		int type = card.getType();
		switch (type) {
		case CardModel.TROOP:
			result = playerTroopCard(room, card);
			break;
		case CardModel.TRAP:
			result = playerTrapCard(room, card);
			break;
		case CardModel.ARTIFACT:
			result = playerArtiCard(room, card);
			break;
		case CardModel.SPELL:
			result = playerSpellCard(room, card);
			break;
		}
		return result;
	}
	
	private int getEmptySubRowIndex() {
		for (int i=0; i<Area.ROW_MAX_INDEX; i++) {
			Area area = this.getArea(i);
			if (area.haveArtiOrTrapSeat()) {
				return area.getIndex();
			}
		}
		return -1;
	}
	
	private boolean attack(GameRoom room) {
		int enemyId = room.getEnemyId(this.getPlayerId()); // 敌方玩家Id
		boolean result = false;
		BattleRole enemy = room.getBattleRole(enemyId); // 敌方玩家
		for (int areaIndex = 0; areaIndex<Area.ROW_MAX_INDEX; areaIndex++) {
			TroopCard attCard = null;
			Area oppoArea = this.getArea(areaIndex);
			ArrayList<TroopCard> tempList = oppoArea.getTroops();
			if (tempList.size() > 0) {
				for (TroopCard troop : tempList) {
					if (troop.isSleep()) {
						continue;
					}
					if (!troop.isAttack(this)) {
						continue;
					}
					attCard = troop;
					int defCardId = 0;
					TroopCard defCard = null;
					Object target = room.getRandomTarget(enemy, attCard);
					if (target == null) {
						continue;
					}
					if (target instanceof TroopCard) {
						defCard = (TroopCard)target;
						defCardId = defCard.getUid();
					} else if (target instanceof BattleRole) {
						defCardId = enemy.getUid();
					}
					if (attCard.getAttack() > 0) {
						if (room.attack(this.getPlayerId(), attCard.getUid(), defCardId)) {
							result = true;
							break;
						}
					}
				}
			}
			if (result == true) {
				break;
			}
		}
		return result;
	}
	
	private boolean areaLvUp(GameRoom room, int areaIndex) {
		return room.areaLvUp(this.getPlayerId(), areaIndex);
	}
	
	private boolean playerTroopCard(GameRoom room, CardBase card) {
		SelectObject sobj = new SelectObject();
		// o.getTarget().add(id);
		//对面区域部队数—本区域部队数 =x x值最大的区域
		int enemyId = room.getEnemyId(this.getPlayerId()); // 敌方玩家Id
		BattleRole enemy = room.getBattleRole(enemyId); // 敌方玩家
		int playerId = this.getPlayerId();
		int cardUid = card.getUid();
		int maxTroopsCount = 0;
		int maxTroopsAtk = 0;
		Area area = null;
		for (int i=0; i<Area.ROW_MAX_INDEX; i++) {
			Area a = this.getArea(i);
			if (!a.haveTroopSeat()) {
				continue;
			} else {
				Area enemyArea = enemy.getArea(i);
				ArrayList<TroopCard> tempList = enemyArea.getTroops();
				if (maxTroopsCount == 0 || tempList.size() > maxTroopsCount) {
					area = a;
					maxTroopsCount = tempList.size();
					maxTroopsAtk = 0;
					for (TroopCard troop : tempList) {
						maxTroopsAtk += troop.getAttack();
					}
				} else if (tempList.size() == maxTroopsCount) {
					int troopsAtk = 0;
					for (TroopCard troop : tempList) {
						troopsAtk += troop.getAttack();
					}
					if (troopsAtk > maxTroopsAtk) {
						maxTroopsAtk = troopsAtk;
						area = a;
					}
				}
			}
		}
		if (area != null && area.haveTroopSeat()) {
			return room.playCardTroop(playerId, cardUid, area.getIndex(), area.getMainRowSize(), sobj);
		} else {
			logger.info("新手AI施放卡牌失败：{}", card.getRealId());
			return false;
		}
	}
	
	private boolean playerSpellCard(GameRoom room, CardBase card) {
		SelectObject sobj = new SelectObject();
		return room.playCardSpell(this.getPlayerId(), card.getUid(), sobj);
	}
	
	private boolean playerTrapCard(GameRoom room, CardBase card) {
		int playerId = this.getPlayerId();
		int cardUid = card.getUid();
		int areaIndex = getEmptySubRowIndex();
		if (areaIndex < 0) {
			return false;
		}
		Area area = this.getArea(areaIndex);
		if (area.artiOrTrapIsFull()) {
			return false;
		}
		int subRowIndex = area.getSubRowSize() + Area.ROW_MAX_INDEX;
		return room.playCardSpellTrap(playerId, cardUid, areaIndex, subRowIndex);
	}
	
	private boolean playerArtiCard(GameRoom room, CardBase card) {
		int playerId = this.getPlayerId();
		int cardUid = card.getUid();
		int areaIndex = getEmptySubRowIndex();
		if (areaIndex < 0) {
			return false;
		}
		Area area = this.getArea(areaIndex);
		if (area.artiOrTrapIsFull()) {
			return false;
		}
		int subRowIndex = area.getSubRowSize() + Area.ROW_MAX_INDEX;
		return room.playCardArti(playerId, cardUid, areaIndex, subRowIndex);
	}
}
