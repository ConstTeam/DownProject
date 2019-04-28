package module.fight;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.card.CardModel;
import config.model.guide.GuideGateAIModel;
import module.area.Area;
import module.card.CardBase;
import module.card.TroopCard;
import module.scene.GameRoom;
import module.scene.SelectObject;
import sys.GameTimer;
import util.ErrorPrint;

public class GuideBattleRole extends BattleRole {
	
	private static final Logger logger = LoggerFactory.getLogger(GuideBattleRole.class);
	
	/** 升级区域 */
	private static final int AREA_LV_UP = 1;
	/** 打出卡牌 */
	private static final int PLAY_CARD = 2;
	/** 攻击优先英雄 */
	private static final int ATTACK = 3;
	/** 结束回合 */
	private static final int END_TURN = 4;
	/** 打出所有可打出的卡牌 */
	private static final int PLAY_ALL_CARD = 5;
	/** 自动AI:释放卡、攻击、升级区域 */
	private static final int AUTO_AI_ONE = 6;
	/** 自动AI:释放卡、攻击、升级区域 */
	private static final int AREA_LV_UP_INORDER = 7;
	
	
	/** 自动AI: 释放卡牌优先级 - 费用从高到低*/
	private static final int COST_DESCENDING_ORDER = 1;
	/** 自动AI: 攻击延时*/
	private static final int ATTACK_DELAY = 1500;
	
	private int playCardIndex = 0;
	
	private int roundActionIndex = 1;
	
	private boolean stop = false;
	
	public GuideBattleRole(String nickname, int hp, int uid) {
		super(0, nickname, hp, uid);
		this.setRobot(true);
	}

	public void turn(GameRoom room) {
		this.delayDoAction(room, -1);
	}
	
	public void stop(GameRoom room) {
		this.stop = true;
	}
	
	public void action(GameRoom room, GuideGateAIModel gateAI) {
		try {
			if (this.stop) {
				return;
			}
			switch (gateAI.Action) {
			case AREA_LV_UP:
				areaLvUp(room, gateAI);
				delayDoAction(room, -1);
				break;
			case AREA_LV_UP_INORDER:
				areaLvUpInOrder(room, gateAI);
				delayDoAction(room, -1);
				break;
			case PLAY_CARD:
				playerCard(room, gateAI);
				delayDoAction(room, -1);
				break;
			case ATTACK:
				int count = getCanAttackTroopCount(gateAI);
				int delay = count*ATTACK_DELAY;
				attack(room, gateAI);
				delayDoAction(room, delay);
				break;
			case PLAY_ALL_CARD:
				playAllCard(room, gateAI);
				delayDoAction(room, -1);
				break;
			case AUTO_AI_ONE:
				this.playCardIndex = 0;
				autoAIOne(room, gateAI);
				break;
			case END_TURN:
				turnFinish(room);
				break;
			}
		} catch (Exception e) {
			logger.error("Guide AI Action 出错：" + e);
			ErrorPrint.print(e);
		}
	}
	
	public void delayDoAction(GameRoom room, int delay) {
		int guideId = room.getTemplet().arg1; // 指引Id
		int round = room.getRound(); // 当前回合数
		Vector<GuideGateAIModel> gateAIVec = ConfigData.guideGateAIModels.get(guideId);
		if (gateAIVec == null || gateAIVec.size() == 0) {
			logger.info("新手AI回合信息缺失:{}", guideId);
			return;
		}
		int index = 1;
		int maxRound = 0;
		for (int i=0; i<gateAIVec.size(); i++) {
			maxRound = maxRound<gateAIVec.get(i).Round ? gateAIVec.get(i).Round : maxRound;
			if(gateAIVec.get(i).Round == round) {
				if (index == this.roundActionIndex) {
					GuideGateAIModel gateAI = gateAIVec.get(i);
					int seconds = delay>0?delay:1000*gateAI.Delay;
					this.roundActionIndex++;
					GameTimer.getScheduled().schedule(() -> action(room, gateAI), seconds, TimeUnit.MILLISECONDS);
					return;
				} else {
					index++;
				}
			}
		}
		for (int i=0; i<gateAIVec.size(); i++) {
			if(gateAIVec.get(i).Round == maxRound) {
				if (index == this.roundActionIndex) {
					GuideGateAIModel gateAI = gateAIVec.get(i);
					int seconds = delay>0?delay:1000*gateAI.Delay;
					this.roundActionIndex++;
					GameTimer.getScheduled().schedule(() -> action(room, gateAI), seconds, TimeUnit.MILLISECONDS);
					return;
				} else {
					index++;
				}
			}
		}
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
	
	private void playerCard(GameRoom room, GuideGateAIModel gateAI) {
		CardBase card = null;
		ArrayList<CardBase> cardList = this.getHandCards();
		for (int i=0; i<cardList.size(); i++) {
			if (cardList.get(i).getId().equals(gateAI.Arg1)) {
				card = cardList.get(i);
				break;
			}
		}
		if (card == null) {
			logger.info("新手AI施放卡牌失败, 手牌中没有卡:{}", gateAI.Arg1);
			return;
		}
		int type = card.getType();
		switch (type) {
		case CardModel.TROOP:
			playerTroopCard(room, card, gateAI);
			break;
		case CardModel.TRAP:
			playerTrapCard(room, card, gateAI);
			break;
		case CardModel.ARTIFACT:
			playerArtiCard(room, card, gateAI);
			break;
		case CardModel.SPELL:
			playerSpellCard(room, card, gateAI);
			break;
		}
	}
	
	private void playAllCard(GameRoom room, GuideGateAIModel gateAI) {
		try {
			if (this.stop) {
				return;
			}
			CardBase card = null;
			ArrayList<CardBase> cardList = this.getHandCards();
			boolean result = true;
			for (int i=cardList.size()-1; i>=0; i--) {
				if (!cardList.get(i).getId().equals(gateAI.Arg1)) {
					continue;
				}
				int cost = cardList.get(i).getCost(this);
				if (cost <= this.getResource()) {
					card = cardList.get(i);
					int type = card.getType();
					switch (type) {
					case CardModel.TROOP:
						result = playerTroopCard(room, card, null);
						break;
					case CardModel.TRAP:
						result = playerTrapCard(room, card, null);
						break;
					case CardModel.ARTIFACT:
						result = playerArtiCard(room, card, null);
						break;
					case CardModel.SPELL:
						result = playerSpellCard(room, card, null);
						break;
					}
					break;
				}
			}
			if (result == true) {
				GameTimer.getScheduled().schedule(() -> playAllCard(room, gateAI), 3000, TimeUnit.MILLISECONDS);
			}//执行失败 结束循环
		} catch (Exception e) {
			logger.error("Guide AI playAllCard 出错：" + e);
			ErrorPrint.print(e);
		}
	}
	
	private void autoAIOne(GameRoom room, GuideGateAIModel gateAI) {
		try {
			if (this.stop) {
				return;
			}
			int actionDelay = 3000;
			boolean actionDone = false;
			CardBase card = null;
			ArrayList<CardBase> cardList = this.getHandCards();
			for (int i=this.playCardIndex; i<cardList.size(); i++) {
				int cost = cardList.get(i).getCost(this);
				if (cost <= this.getResource()) {
					if (gateAI.Arg2.equals("") == false && Integer.valueOf(gateAI.Arg2) == COST_DESCENDING_ORDER) {
						if (card == null || card.getCost(this) < cost) {
							card = cardList.get(i);
						}
					} else {
						card = cardList.get(i);
						break;
					}
				}
			}
			boolean result = false;
			if (card != null) {
				int type = card.getType();
				switch (type) {
				case CardModel.TROOP:
					result = playerTroopCard(room, card, null);
					break;
				case CardModel.TRAP:
					result = playerTrapCard(room, card, null);
					break;
				case CardModel.ARTIFACT:
					result = playerArtiCard(room, card, null);
					break;
				case CardModel.SPELL:
					result = playerSpellCard(room, card, null);
					break;
				}
				this.playCardIndex += 1;
			}
			if (result == true) {
				actionDone = true;
			}
			if (actionDone == false && gateAI.Arg1.equals("1")) {
				Area upArea = null;
				for (int i=0; i<Area.ROW_MAX_INDEX; i++) {
					Area area = this.getArea(i);
					if (upArea == null || upArea.getLevel() > area.getLevel()) {
						upArea = area;
					}
				}
				actionDone = room.areaLvUp(this.getPlayerId(), upArea.getIndex());
			}
			if (actionDone == false) {
				int count = getCanAttackTroopCount(gateAI);
				if (count > 0) {
					actionDelay = count*ATTACK_DELAY;
					if (attack(room, null) == false) {
						turnFinish(room);
						return;
					}
				} else {
					turnFinish(room);
					return;
				}
			}
			GameTimer.getScheduled().schedule(() -> autoAIOne(room, gateAI), actionDelay, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.error("Guide AI autoAIOne 出错：" + e);
			ErrorPrint.print(e);
		}
	}
	
	private int getCanAttackTroopCount(GuideGateAIModel gateAI) {
		int count = 0;
		for (int areaIndex = 0; areaIndex<Area.ROW_MAX_INDEX; areaIndex++) {
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
					if (gateAI != null && gateAI.Arg2.equals("") == false) {
						if (troop.getId().equals(gateAI.Arg2) == false) {
							continue;
						}
					}
					count++;
				}
			}
		}
		return count;
	}
	
	private boolean attack(GameRoom room, GuideGateAIModel gateAI) {
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
					if (gateAI != null && gateAI.Arg2.equals("") == false) {
						if (troop.getId().equals(gateAI.Arg2) == false) {
							continue;
						}
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
						if (gateAI != null && gateAI.Arg1.equals("") == true) {
							if (defCard.isGuardian() || defCard.getAreaIndex() == attCard.getAreaIndex()) {
								defCardId = defCard.getUid();
							} else {
								defCardId = enemy.getUid();
							}
						} else {
							defCardId = defCard.getUid();
						}
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
				GameTimer.getScheduled().schedule(() -> attack(room, gateAI), ATTACK_DELAY, TimeUnit.MILLISECONDS);
				break;
			}
		}
		return result;
	}
	
	private void areaLvUp(GameRoom room, GuideGateAIModel gateAI) {
		room.areaLvUp(this.getPlayerId(), Integer.parseInt(gateAI.Arg1));
	}
	
	private void turnFinish(GameRoom room) {
		room.turnFinish(this.getPlayerId());
		this.roundActionIndex = 1;
	}
	
	private void areaLvUpInOrder(GameRoom room, GuideGateAIModel gateAI) {
		int areaIndex = -1;
		for (int i=0; i<Area.ROW_MAX_INDEX; i++) {
			Area area = this.getArea(i);
			if (area.getLevel() < Area.MAX_LEVEL) {
				areaIndex = area.getIndex();
			}
		}
		if (areaIndex>=0) {
			room.areaLvUp(this.getPlayerId(), areaIndex);
		}
	}
	
	private boolean playerTroopCard(GameRoom room, CardBase card, GuideGateAIModel gateAI) {
		SelectObject sobj = new SelectObject();
		// o.getTarget().add(id);
		int playerId = this.getPlayerId();
		int cardUid = card.getUid();
		int areaIndex = 0;
		Area area = null;
		if (gateAI == null || gateAI.Arg2.equals("")) {
			if (card.getPathfinder() != 3) {
				area = this.getArea(card.getPathfinder());
			} else {
				for (int i=0; i<Area.ROW_MAX_INDEX; i++) {
					area = this.getArea(i);
					if (!area.haveTroopSeat()) {
						area = null;
						continue;
					} else {
						areaIndex = area.getIndex();
						break;
					}
				}
			}
		} else {
			if (card.getPathfinder() != 3) {
				card.setPathfinder(Integer.parseInt(gateAI.Arg2));
			}
			areaIndex = Integer.parseInt(gateAI.Arg2);
			area = this.getArea(areaIndex);
		}
		if (area != null && area.haveTroopSeat()) {
			room.playCardTroop(playerId, cardUid, areaIndex, area.getMainRowSize(), sobj);
			return true;
		} else {
			logger.info("新手AI施放卡牌失败：{}", card.getId());
			return false;
		}
	}
	
	private boolean playerSpellCard(GameRoom room, CardBase card, GuideGateAIModel gateAI) {
		SelectObject sobj = new SelectObject();
		return room.playCardSpell(this.getPlayerId(), card.getUid(), sobj);
	}
	
	private boolean playerTrapCard(GameRoom room, CardBase card, GuideGateAIModel gateAI) {
		int playerId = this.getPlayerId();
		int cardUid = card.getUid();
		int areaIndex = 0;
		if (gateAI != null) {
			areaIndex = Integer.parseInt(gateAI.Arg2);
		} else {
			areaIndex = getEmptySubRowIndex();
		}
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
	
	private boolean playerArtiCard(GameRoom room, CardBase card, GuideGateAIModel gateAI) {
		int playerId = this.getPlayerId();
		int cardUid = card.getUid();
		int areaIndex = 0;
		if (gateAI != null) {
			areaIndex = Integer.parseInt(gateAI.Arg2);
		} else {
			areaIndex = getEmptySubRowIndex();
		}
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
