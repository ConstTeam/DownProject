package message.game.fight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.card.CardModel;
import message.game.GameMsgModuleConst;
import module.area.Area;
import module.card.ArtifactCard;
import module.card.CardBase;
import module.card.FindCard;
import module.card.TrapCard;
import module.card.TroopCard;
import module.fight.BattleRole;
import module.log.LogDetailInfo;
import module.log.LogInfo;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;
import redis.RedisProxy;
import skill.SkillManager;
import util.Tools;

/**
 * 战斗模块消息发送
 *
 */
public class FightMsgSend {
	
	private static final Logger logger = LoggerFactory.getLogger(FightMsgSend.class);
	
	private static void syncSimpleCardInfo(IByteBuffer data, CardBase card) {
		data.writeUTF(card == null ? "0" : card.getRealId());
	}
	
	private static void syncDetailCardInfo(IByteBuffer data, CardBase card, BattleRole fighter) {
		data.writeByte(card.getType());
		data.writeInt(card.getUid());
		data.writeUTF(card.getRealId());
		data.writeByte(card.getCost(fighter));
		data.writeByte(card.getPathfinder());
		logger.debug("playCard ========================= cardId:{} cardUid:{}", card.getRealId(), card.getUid());
	}

	public static void messageBox(ISession session, String message) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.MSG_BOX);

		data.writeUTF(message);

		session.send(data);
	}

	public static void messageBox(ISession session, int id) {
		if (session == null || session.isClosed()) {
			return;
		}
		String message = ConfigData.messageBox.get(id);
		if (Tools.isEmptyString(message)) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.MSG_BOX);

		data.writeUTF(message);

		session.send(data);
	}
	
	public static void intoRoom(ISession session, BattleRole fighter, BattleRole enemy) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.START_GAME);

		session.send(data);
	}
	
	public static void firstDeal(ISession session, BattleRole fighter, boolean isFirst) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.FIRST_DEAL);
		
		data.writeBoolean(isFirst);
		for (CardBase card : fighter.getHandCards()) {
			syncDetailCardInfo(data, card, fighter);
		}

		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.FIRST_DEAL);
	}

	public static void deckCardNumberSync(ISession session, boolean isMine, int number) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.DECK_CARD_NUMER_SYNC);
		
		data.writeBoolean(isMine);
		data.writeInt(number);
		
		session.send(data);
		logger.debug("{} - {} isMine:{} number:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.DECK_CARD_NUMER_SYNC, isMine, number);
	}
	
	public static void startMyTurn(ISession session, boolean isMine, boolean isTurboMode, boolean isFirst) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.MY_TURN);
		
		data.writeBoolean(isMine);
		data.writeBoolean(isTurboMode);
		data.writeBoolean(isFirst);
		
		session.send(data);
		logger.debug("{} - {} isMine:{} isTurboMode:{} isFirst:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.MY_TURN, isMine, isTurboMode, isFirst);
	}
	
	public static void startMyTurnEnd(ISession session, boolean isMine, boolean isTurboMode, boolean isEnd) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.MY_TURN_END);

		data.writeBoolean(isMine);
		data.writeBoolean(isTurboMode);
		data.writeBoolean(isEnd);
		
		session.send(data);
		logger.debug("{} - {} isMine:{} isTurboMode:{} isEnd:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.MY_TURN_END, isMine, isTurboMode, isEnd);
	}

	public static void notice(ISession session) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.NOTICE);

		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.NOTICE);
	}
	
	public static void turnNotice(ISession session) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TURN_NOTICE);

		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TURN_NOTICE);
	}
	
	public static void selfReadySync(ISession session, ArrayList<CardBase> cards) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.SELF_READY_SYNC);
		
		data.writeByte(cards.size());
		for (CardBase card : cards) {
			data.writeInt(card.getUid());
		}
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.SELF_READY_SYNC);
	}
	
	public static void enemyCardSync(ISession session, ArrayList<CardBase> cards, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.ENEMY_CARD_SYNC);
		
		CardBase cardBase = cards.get(0);
		data.writeBoolean(!cardBase.isEnemyCard());
		data.writeByte(cards.size());
		for (CardBase card : cards) {
			data.writeInt(card.getUid());
			data.writeBoolean(fighter.getHandCards().indexOf(card) == -1);
		}
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.ENEMY_CARD_SYNC);
	}
	
	public static void summonTroop(ISession session, int playerId, TroopCard card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.SUMMON_SYNC);

		data.writeBoolean(playerId == card.getPlayerId());
		data.writeByte(card.getType());
		data.writeInt(card.getUid());
		syncSimpleCardInfo(data, card);
		data.writeByte(card.getAreaIndex());
		data.writeByte(card.getMainRowIndex());
		data.writeInt(card.getRealAttack());
		data.writeInt(card.getHp());
		
		session.send(data);
		logger.debug("{} - {} cardUid:{} cardId:{} Area:{} MainIndex:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.SUMMON_SYNC, card.getUid(), card.getRealId(), card.getAreaIndex(), card.getMainRowIndex());
	}
	
	public static void summonArtifact(ISession session, int playerId, ArtifactCard card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.SUMMON_SYNC);

		data.writeBoolean(playerId == card.getPlayerId());
		data.writeByte(card.getType());
		data.writeInt(card.getUid());
		syncSimpleCardInfo(data, card);
		data.writeByte(card.getAreaIndex());
		data.writeByte(card.getMainRowIndex());
		data.writeInt(0);
		data.writeInt(0);
		
		session.send(data);
		logger.debug("{} - {} cardUid:{} cardId:{} Area:{} MainIndex:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.SUMMON_SYNC, card.getUid(), card.getRealId(), card.getAreaIndex(), card.getMainRowIndex());
	}
	
	public static void summonTrap(ISession session, int playerId, TrapCard card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.SUMMON_SYNC);

		data.writeBoolean(playerId == card.getPlayerId());
		data.writeByte(card.getType());
		data.writeInt(card.getUid());
		syncSimpleCardInfo(data, playerId == card.getPlayerId() ? card : null);
		data.writeByte(card.getAreaIndex());
		data.writeByte(card.getMainRowIndex());
		data.writeInt(0);
		data.writeInt(0);
		
		session.send(data);
		logger.debug("{} - {} cardUid:{} cardId:{} Area:{} MainIndex:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.SUMMON_SYNC, card.getUid(), card.getRealId(), card.getAreaIndex(), card.getMainRowIndex());
	}
	
	public static void summonHandCardToPlay(ISession session, int playerId, TroopCard card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.HAND_CARD_TO_PLAY);

		data.writeBoolean(playerId == card.getPlayerId());
		data.writeByte(card.getType());
		data.writeInt(card.getUid());
		syncSimpleCardInfo(data, card);
		data.writeByte(card.getAreaIndex());
		data.writeByte(card.getMainRowIndex());
		data.writeInt(card.getRealAttack());
		data.writeInt(card.getHp());
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.HAND_CARD_TO_PLAY);
	}
	
	public static void revealCardSync(ISession session, CardBase card, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.REVEAL_CARD_SYNC);
		
		syncDetailCardInfo(data, card, fighter);
		if (card.getType() == CardModel.TROOP) {
			TroopCard troop = (TroopCard) card;
			data.writeInt(troop.getRealAttack());
			data.writeInt(troop.getHp());
		} else {
			data.writeInt(0);
			data.writeInt(0);
		}
		session.send(data);
		logger.debug("{} - {} playerId:{} cardUid:{} cardId:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.REVEAL_CARD_SYNC, card.getPlayerId(), card.getUid(), card.getRealId());
	}

	public static void troopCardPlay(ISession session, int playerId, TroopCard card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.PLAY_TROOP_SYNC);

		data.writeBoolean(playerId == card.getPlayerId());
		syncSimpleCardInfo(data, card);
		data.writeInt(card.getUid());
		data.writeByte(card.getAreaIndex());
		data.writeByte(card.getMainRowIndex());
		data.writeInt(card.getRealAttack());
		data.writeInt(card.getHp());
		data.writeBoolean(card.getTarget().size() != 0); // 是否已落下
		
		session.send(data);
		logger.debug("{} - {} cardUid:{} cardId:{} Area:{} MainIndex:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.PLAY_TROOP_SYNC, card.getUid(), card.getRealId(), card.getAreaIndex(), card.getMainRowIndex());
	}


	public static void artiCardPlay(ISession session, boolean isMine, CardBase card, int areaIndex) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.PLAY_ARTI_SYNC);

		data.writeBoolean(isMine);
		syncSimpleCardInfo(data, card);
		data.writeInt(card.getUid());
		data.writeByte(areaIndex);
		data.writeByte(card.getMainRowIndex());
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.PLAY_ARTI_SYNC);
	}
	
	public static void trapCardPlay(ISession session, CardBase card, String cardId, int cardUid, int areaIndex, int mainRowIndex) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.PLAY_TRAP_SYNC);

		data.writeBoolean(card != null);
		syncSimpleCardInfo(data, card);
		data.writeInt(cardUid);
		data.writeByte(areaIndex);
		data.writeByte(mainRowIndex);
		data.writeBoolean(false);
		
		session.send(data);
		logger.debug("{} - {} cardId:{} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.PLAY_TRAP_SYNC, cardId, cardUid);
	}
	
	public static void spellCardPlay(ISession session, boolean isMine, CardBase card, int handCardIndex) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.PLAY_SPELL_SYNC);

		data.writeBoolean(isMine);
		data.writeInt(card.getUid());
		
		session.send(data);
		logger.debug("{} - {} cardId:{}, cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.PLAY_SPELL_SYNC, card.getRealId(), card.getUid());
	}

	public static void attack(ISession session, int attCardId, int attHp, int defCardId, int defHp, int isThump) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.ATTACK_SYNC);

		data.writeInt(attCardId);
		data.writeInt(attHp);
		data.writeInt(defCardId);
		data.writeInt(defHp);
		data.writeByte(isThump);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.ATTACK_SYNC);
	}

	public static void excessDamageSync(ISession session, int defCardUid, int excessDamage) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.EXCESS_SYNC);

		data.writeInt(defCardUid);
		data.writeInt(excessDamage);
		
		session.send(data);
		logger.debug("{} - {} cardUid:{} excessDamage:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.EXCESS_SYNC, defCardUid, excessDamage);
	}
	
	public static void gameStartSync(ISession session, BattleRole my, BattleRole enemy) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.GAME_START_SYNC);
		
		data.writeByte(my.getHp());
		data.writeByte(enemy.getHp());
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.GAME_START_SYNC);
	}
	
	public static void readySync(ISession session, int playerId) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.READY_SYNC);
		
		data.writeInt(playerId);
		
		session.send(data);
		logger.debug("{} - {} playerId:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.READY_SYNC, playerId);
	}
	
	public static void areaLvUpResult(ISession session, boolean isMine, int areaIndex, int level, BattleRole role) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.AREA_LV_UP_END);

		data.writeBoolean(isMine);
		data.writeByte(areaIndex);
		data.writeByte(level);
		data.writeBoolean(role.isAreaLvUp());
		
		session.send(data);
		logger.debug("{} - {} isMine:{} areaIndex:{} level:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.AREA_LV_UP_END, isMine, areaIndex, level);
	}
	
	public static void resourceSync(ISession session, boolean isMine, int resource, int replResource) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.RESOURCE_SYNC);

		data.writeBoolean(isMine);
		data.writeByte(resource);
		data.writeByte(replResource);
		
		session.send(data);
		logger.debug("{} - {} isMine:{} resource:{} replResource:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.RESOURCE_SYNC, isMine, resource, replResource);
	}
	
	public static void pvpSettlement(ISession session, boolean isWinner, ArrayList<Object> result, int rank) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.PVPSETTLEMENT_SYNC);

		data.writeBoolean(isWinner);

		int gold = (int) result.remove(0);
		data.writeInt(gold); // 赢家奖励
		
		session.send(data);
		logger.debug("{} - {} isWinner:{} changeQuestSize:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.PVPSETTLEMENT_SYNC, isWinner);
	}
	
	public static void guideSettlement(ISession session, int playerId, boolean isWinner) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.GUIDESETTLEMENT_SYNC);
		data.writeBoolean(isWinner);
		int guideId = RedisProxy.getInstance().getPlayerGuideID(playerId);
		data.writeByte(guideId);
		
		session.send(data);
		logger.debug("{} - {} isWinner:{} guideId:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.PVPSETTLEMENT_SYNC, isWinner, guideId);
	}
	
	public static void replaceDealResult(ISession session, HashMap<Integer, CardBase> cards, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.REPLACE_FIRST_DEAL_RESULT);

		data.writeByte(cards.size());
		Iterator<Entry<Integer, CardBase>> iterator = cards.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, CardBase> next = iterator.next();
			Integer uid = next.getKey();
			CardBase card = next.getValue();
			data.writeInt(uid);
			syncDetailCardInfo(data, card, fighter);
		}
		
		session.send(data);
		logger.debug("{} - {} cards:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.REPLACE_FIRST_DEAL_RESULT, cards);
	}

	public static void roleStatusSync(ISession session, boolean isMine, int type, int value) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.ROLE_STATUS_SYNC);
		
		data.writeBoolean(isMine);
		
		data.writeByte(type);
		data.writeInt(value);
		
		session.send(data);
		logger.debug("{} - {} isMine:{} type:{} value:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.PVPSETTLEMENT_SYNC, isMine, type, value);
	}
	
	public static void heroHpSync(ISession session, int playerId, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.HERO_HP_SYNC);

		data.writeBoolean(playerId == fighter.getPlayerId());
		data.writeInt(fighter.getHp());
		
		session.send(data);
		logger.debug("{} - {} roleId:{} hp:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.HERO_HP_SYNC, fighter.getPlayerId(), fighter.getHp());
	}
	
	public static void troopSync(ISession session, TroopCard troop) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TROOP_ATTR_SYNC);

		data.writeInt(troop.getUid());
		data.writeInt(troop.getRealAttack());
		data.writeInt(troop.getRealHp());
		
		session.send(data);
		logger.debug("{} - {} cardId:{}, cardUid:{}, areaIndex:{}, mainRowIndex:{} attack:{} hp:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_ATTR_SYNC, troop.getRealId(), troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), troop.getRealAttack(), troop.getRealHp());
	}
	
	public static void troopStatusSync(ISession session, String status, int playerId, TroopCard troop, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		setTroopStatusSubModuleId(data, status);

		data.writeInt(troop.getUid());
		setTroopStatus(data, status, troop, fighter);
		
		session.send(data);
	}
	
	private static void setTroopStatusSubModuleId(IByteBuffer data, String status) {
		switch (status) {
		case TroopCard.STUN:
			data.writeByte(FightMsgConst.TROOP_STUN_SYNC);
			break;
		case TroopCard.FLIGHT:
			data.writeByte(FightMsgConst.TROOP_FLIGHT_SYNC);
			break;
		case TroopCard.GUARDIAN:
			data.writeByte(FightMsgConst.TROOP_GUARDIAN_SYNC);
			break;
		case TroopCard.SLEEP:
			data.writeByte(FightMsgConst.TROOP_SLEEP_SYNC);
			break;
		case TroopCard.ATTACKED:
			data.writeByte(FightMsgConst.TROOP_ATTACK_SYNC);
			break;
		case TroopCard.FORCE_SHIELD:
			data.writeByte(FightMsgConst.TROOP_FORCE_SHIELD_SYNC);
			break;
		case TroopCard.SPELL_BLOCK:
			data.writeByte(FightMsgConst.TROOP_SPELL_BLOCK_SYNC);
			break;
		case TroopCard.CONTROL:
			data.writeByte(FightMsgConst.TROOP_CONTROL_SYNC);
			break;
		case TroopCard.SPEED:
			data.writeByte(FightMsgConst.TROOP_SPEED_SYNC);
			break;
		case TroopCard.LIFEDRAIN:
			data.writeByte(FightMsgConst.TROOP_LIFEDRAIN_SYNC);
			break;
		case TroopCard.AVOID_ATTACKED:
			data.writeByte(FightMsgConst.CAN_BE_ATTACK);
			break;
		case BattleRole.AMPLIFY:
			data.writeByte(FightMsgConst.AMPLIFY_SYNC);
			break;
		case TroopCard.AVOID_OPP_ATTACKED:
			data.writeByte(FightMsgConst.CAN_BE_OPP_AREA_ATTACK);
			break;
		case TroopCard.INVINCIBLE:
			data.writeByte(FightMsgConst.TROOP_INVINCIBLE_SYNC);
			break;
		case TroopCard.ALWAYS_ATTACK_HERO:
			data.writeByte(FightMsgConst.ALWAYS_ATTACK_HERO);
			break;
		case TroopCard.ENCHANT:
			data.writeByte(FightMsgConst.TROOP_ATTACKED_SYNC);
			break;
		case TroopCard.ATTACK_TYPE:
			data.writeByte(FightMsgConst.TROOP_ATTACK_TYPE);
			break;
		case TroopCard.DAMAGED:
			data.writeByte(FightMsgConst.TROOP_DAMAGED_SYNC);
			break;
		}
	}
	
	private static void setTroopStatus(IByteBuffer data, String status, TroopCard troop, BattleRole fighter) {
		switch (status) {
		case TroopCard.STUN:
			data.writeInt(troop.getStunCount());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_STUN_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.getStunCount());
			break;
		case TroopCard.FLIGHT:
			data.writeBoolean(troop.isFlight());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_FLIGHT_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isFlight());
			break;
		case TroopCard.GUARDIAN:
			data.writeBoolean(troop.isGuardian());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_GUARDIAN_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isGuardian());
			break;
		case TroopCard.SLEEP:
			data.writeBoolean(troop.isSleep());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_SLEEP_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isSleep());
			break;
		case TroopCard.ATTACKED:
			data.writeBoolean(troop.isAttack(fighter));
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_ATTACK_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isAttack(fighter));
			break;
		case TroopCard.FORCE_SHIELD:
			data.writeBoolean(troop.isForceShield());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_FORCE_SHIELD_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isForceShield());
			break;
		case TroopCard.SPELL_BLOCK:
			data.writeBoolean(troop.isSpellBlock());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_SPELL_BLOCK_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isSpellBlock());
			break;
		case TroopCard.CONTROL:
			data.writeBoolean(troop.isControl());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_CONTROL_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isControl());
			break;
		case TroopCard.SPEED:
			data.writeBoolean(troop.isSpeed());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_SPEED_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isSpeed());
			break;
		case TroopCard.LIFEDRAIN:
			data.writeBoolean(troop.isLifedrain());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_LIFEDRAIN_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isLifedrain());
			break;
		case TroopCard.AVOID_ATTACKED:
			data.writeBoolean(troop.canBeAttack());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.CAN_BE_ATTACK, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.canBeAttack());
			break;
		case BattleRole.AMPLIFY:
			data.writeInt(troop.getAmplify());
			logger.debug("{} - {}, cardUid:{}, Amplify:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.AMPLIFY_SYNC, troop.getUid(), troop.getAmplify());
			break;
		case TroopCard.AVOID_OPP_ATTACKED:
			data.writeBoolean(troop.canBeOppAttack());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.CAN_BE_OPP_AREA_ATTACK, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.canBeOppAttack());
			break;
		case TroopCard.INVINCIBLE:
			data.writeBoolean(troop.isInvincible());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_INVINCIBLE_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isInvincible());
			break;
		case TroopCard.ALWAYS_ATTACK_HERO:
			data.writeBoolean(troop.isAlwaysAttackHero());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.ALWAYS_ATTACK_HERO, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isAlwaysAttackHero());
			break;
		case TroopCard.ENCHANT:
			data.writeBoolean(troop.isEnchant());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_ATTACKED_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.isEnchant());
			break;
		case TroopCard.ATTACK_TYPE:
			data.writeInt(troop.getAttackType());
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_ATTACK_TYPE, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.getAttackType());
			break;
		case TroopCard.DAMAGED:
			data.writeBoolean(troop.getStatus(TroopCard.DAMAGED));
			logger.debug("{} - {}, cardUid:{} areaIndex:{}, mainRowIndex:{}, status:{}-{} ", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_DAMAGED_SYNC, troop.getUid(), troop.getAreaIndex(), troop.getMainRowIndex(), status, troop.getStatus(TroopCard.DAMAGED));
			break;
		}
	}
	
	public static void destoryCard(ISession session, CardBase card, boolean isReplace) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.DESTORY_CARD);

		data.writeByte(card.getType());
		data.writeInt(card.getUid());
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.DESTORY_CARD, card.getUid());
	}
	
	public static void trunFinish(ISession session, boolean isMine, boolean isFirst) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TRUN_FINISH);
		
		data.writeBoolean(isMine);
		data.writeBoolean(isFirst);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TRUN_FINISH);
	}
	
	public static void trunFinishEnd(ISession session, boolean isMine, boolean isFirst) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TRUN_FINISH_END);

		data.writeBoolean(isMine);
		data.writeBoolean(isFirst);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TRUN_FINISH_END);
	}
	
	public static void findCardSync(ISession session, int playerId, FindCard findCard, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.FIND_CARD_SYNC);
		
		boolean isMine = fighter.getPlayerId() == playerId;

		data.writeBoolean(isMine);
		data.writeByte(findCard.getCards().size());
		if (isMine) {
			for (CardBase card : findCard.getCards()) {
				syncDetailCardInfo(data, card, fighter);
			}
		}else {
			for (CardBase card : findCard.getCards()) {
				data.writeInt(card.getUid());
			}
		}
		
		session.send(data);
		logger.debug("{} - {} show:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.FIND_CARD_SYNC);
	}
	
	public static void constructTrap(ISession session, int playerId, FindCard trapCard, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.CONSTRUCT_TRAP_SYNC);

		data.writeBoolean(playerId == fighter.getPlayerId());
		data.writeBoolean(trapCard.isOutput());
		data.writeByte(trapCard.getCards().size());
		for (CardBase card : trapCard.getCards()) {
			data.writeInt(card.getUid());
			if (playerId == fighter.getPlayerId()) {
				data.writeUTF(card.getRealId());
				data.writeByte(card.getDefaultCost());
			}
		}
		
		session.send(data);
		logger.debug("{} - {} show:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.CONSTRUCT_TRAP_SYNC);
	}
	
	public static void constructTrapRes(ISession session, int playerId, CardBase card, BattleRole fighter, boolean isSummon) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.CONSTRUCT_TRAP_RES);
		
		data.writeBoolean(playerId == fighter.getPlayerId());
		data.writeBoolean(isSummon);
		if (playerId == fighter.getPlayerId()) {
			syncDetailCardInfo(data, card, fighter);
		} else {
			data.writeInt(card.getUid());
		}
		data.writeBoolean(fighter.getHandCards().indexOf(card) == -1);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.CONSTRUCT_TRAP_RES);
	}
	
	public static void cardSync(ISession session, ArrayList<CardBase> cards, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		if (cards.size() == 0) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.DEAL_CARD_SYNC);
		
		CardBase cardBase = cards.get(0);
		data.writeBoolean(!cardBase.isEnemyCard());
		data.writeByte(cards.size());
		for (CardBase card : cards) {
			syncDetailCardInfo(data, card, fighter);
			data.writeBoolean(fighter.getHandCards().indexOf(card) == -1);
		}
		
		session.send(data);
		logger.debug("{} - {} show:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.DEAL_CARD_SYNC);
	}
	
	public static void cardSync(ISession session, CardBase card, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.DEAL_CARD);
		
		syncDetailCardInfo(data, card, fighter);
		data.writeBoolean(fighter.getHandCards().indexOf(card) == -1);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.DEAL_CARD);
	}
	
	public static void copyAndPlay(ISession session, CardBase card, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.COPY_AND_PLAY);
		
		syncDetailCardInfo(data, card, fighter);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.COPY_AND_PLAY);
	}
	
	public static void discardSync(ISession session, boolean isMine, ArrayList<CardBase> cards) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.DISCARD_SYNC);
		
		data.writeBoolean(isMine);
		data.writeByte(cards.size());
		for (CardBase card : cards) {
			data.writeInt(card.getUid());
		}
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.DISCARD_SYNC);
	}
	
	public static void cardCheckSync(ISession session, CardBase card, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.CHECK_CARD_SYNC);
		
		syncDetailCardInfo(data, card, fighter);
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.CHECK_CARD_SYNC, card.getUid());
	}
	
	public static void cardCheckSelectResult(ISession session, CardBase card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.CHECK_CARD_SELECT_RESULT);
		
		data.writeInt(card.getUid());
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.CHECK_CARD_SELECT_RESULT, card.getUid());
	}
	
	public static void findResult(ISession session, boolean isMine, int index, boolean isInHandCards) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.FIND_RESULT);

		data.writeBoolean(isMine);
		data.writeByte(index);
		data.writeBoolean(isInHandCards);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.FIND_RESULT);
	}
	
	public static void cardCostSync(ISession session, CardBase card, BattleRole fighter, boolean show) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.CARD_COST_SYNC);
		
		data.writeBoolean(show);  // TODO 是否立即显示
		data.writeInt(card.getUid());
		data.writeByte(card.getCost(fighter));
		
		session.send(data);
		logger.debug("{} - {} cardUid:{} cardId:{} cost:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.CARD_COST_SYNC, card.getUid(), card.getId(), card.getCost(fighter));
	}
	
	public static void handCardToDeck(ISession session, int playerId, CardBase card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.HAND_CARD_TO_DECK);
		
		data.writeBoolean(playerId == card.getPlayerId());
		data.writeInt(card.getUid());
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.HAND_CARD_TO_DECK, card.getUid());
	}

	public static void handcardAttrSync(ISession session, TroopCard card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.HANDCARD_ATTR_SYNC);
		
		data.writeInt(card.getUid());
		data.writeInt(card.getAttack());
		data.writeInt(card.getHp());
		
		session.send(data);
		logger.debug("{} - {} cardUid:{} atk:{} hp:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.HANDCARD_ATTR_SYNC, card.getUid(), card.getRealAttack(), card.getHp());
	}

	public static void troopDeathcrySync(ISession session, CardBase card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TROOP_DEATHCRY_SYNC);
		
		data.writeInt(card.getUid());
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_DEATHCRY_SYNC, card.getUid());
	}

	public static void troopEnchantSync(ISession session, TroopCard card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TROOP_ENCHANT_SYNC);
		
		data.writeInt(card.getUid());
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TROOP_ENCHANT_SYNC, card.getUid());
	}

	public static void startSync(ISession session, CardBase card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.START_SYNC);
		
		data.writeInt(card.getUid());
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.START_SYNC, card.getUid());
	}

	public static void warcrySync(ISession session, CardBase card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.WARCRY_SYNC);

		data.writeByte(card.getType());
		data.writeInt(card.getUid());
		
		session.send(data);
		logger.debug("{} - {} cardType:{} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.WARCRY_SYNC, card.getType(), card.getUid());
	}
	
	public static void returnHandCardsSync(ISession session, int playerId, CardBase card, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.RETURN_HAND_CARDS);

		data.writeInt(card.getUid());
		data.writeByte(card.getType());
		data.writeBoolean(playerId == card.getPlayerId());
		data.writeBoolean(fighter.getHandCards().indexOf(card) == -1);
		
		session.send(data);
		logger.debug("{} - {} playerId:{}, cardNowOwner:{}, cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.RETURN_HAND_CARDS, playerId, card.getPlayerId(), card.getUid());
	}
	
	public static void moveSync(ISession session, int playerId, CardBase card, boolean isMineArea, int oldAreaIndex, int oldMainIndex) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.MOVE_SYNC);

		data.writeInt(card.getUid());
		data.writeByte(card.getAreaIndex());
		data.writeByte(card.getMainRowIndex());
		data.writeBoolean(isMineArea);
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}, oldAreaIndex:{}, oldMainRowIndex:{}, isMineArea:{}, areaIndex:{}, mainRowIndex:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.MOVE_SYNC, card.getUid(), oldAreaIndex, oldMainIndex, isMineArea, card.getAreaIndex(), card.getMainRowIndex());
	}
	
	public static void syncStart(ISession session) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.SYNC_START);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.SYNC_START);
	}
	
	public static void syncEnd(ISession session) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.SYNC_END);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.SYNC_END);
	}
	
	public static void tiretSync(ISession session, int count) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TIRET_SYNC);
		
		data.writeByte(count);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TIRET_SYNC);
	}
	
	public static void areaLvUpNeedResource(ISession session, int playerId, int resource) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.AREA_LV_UP_NEED_RES);

		data.writeInt(playerId);
		data.writeByte(resource);
		
		session.send(data);
		logger.debug("{} - {} resource:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.AREA_LV_UP_NEED_RES, resource);
	}
	
	public static void amplifySync(ISession session, int cardUid, int amplify) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.AMPLIFY_SYNC);

		data.writeInt(cardUid);
		data.writeInt(amplify);
		
		session.send(data);
		logger.debug("{} - {} cardUid:{} amplify:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.AMPLIFY_SYNC, cardUid, amplify);
	}
	
	public static void lifedrainSync(ISession session, int cardUid) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.LIFEDRAIN_SYNC);

		data.writeInt(cardUid);
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.LIFEDRAIN_SYNC, cardUid);
	}
	
	public static void trapTriggerSync(ISession session, int cardUid) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TRAP_TRIGGER_SYNC);

		data.writeInt(cardUid);
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TRAP_TRIGGER_SYNC, cardUid);
	}

	public static void artiTriggerSync(ISession session, int cardUid) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.ARTI_TRIGGER_SYNC);

		data.writeInt(cardUid);
		
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.ARTI_TRIGGER_SYNC, cardUid);
	}
	
	public static void reduceHpSync(ISession session, int attUid, int defUid, int reduceHp) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.REDUCE_HP_SYNC);

		data.writeInt(attUid);
		data.writeInt(defUid);
		data.writeInt(reduceHp);
		
		session.send(data);
		logger.debug("{} - {} attUid:{} defUid:{} reduceHp:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.REDUCE_HP_SYNC, attUid, defUid, reduceHp);
	}
	
	public static void transform(ISession session, int playerId, TroopCard from, TroopCard to) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TRANSFORM_SYNC);

		data.writeBoolean(playerId == from.getPlayerId());
		data.writeInt(from.getUid());
		data.writeInt(to.getUid());
		data.writeUTF(to.getRealId());
		data.writeInt(to.getRealAttack());
		data.writeInt(to.getHp());
		
		session.send(data);
		logger.debug("{} - {} fromUid:{} toUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TRANSFORM_SYNC, from.getUid(), to.getUid());
	}
	
	public static void changeHandCards(ISession session, int playerId, BattleRole fighter, ArrayList<CardBase> from, ArrayList<CardBase> to) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.CHANGE_HANDCARDS);

		data.writeBoolean(playerId == fighter.getPlayerId());
		data.writeByte(from.size());
		for (int i = 0; i < from.size(); i++) {
			CardBase fromCard = from.get(i);
			CardBase toCard = to.get(i);
			data.writeInt(fromCard.getUid());
			if (playerId == fighter.getPlayerId()) {
				syncDetailCardInfo(data, toCard, fighter);
			} else {
				data.writeInt(toCard.getUid());
			}
		}
		
		session.send(data);
		logger.debug("{} - {} size:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.CHANGE_HANDCARDS, from.size());
	}
	
	public static void logSync(ISession session, int playerId, LogInfo logInfo) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.LOG_SYNC);

		data.writeInt(logInfo.getPlayerId());
		data.writeByte(logInfo.getType()); // 0升级区域 1攻击 2打牌 3触发
		switch (logInfo.getType()) {
		case LogInfo.AREA_LV_UP:
			
			break;
		case LogInfo.ATTACK:
			data.writeUTF(logInfo.getCardId());
			data.writeByte(logInfo.getTarget().size());
			for (LogDetailInfo info : logInfo.getTarget()) {
				data.writeUTF(info.getCardId());
				data.writeInt(info.getValue());
			}
			break;
		case LogInfo.PLAY_CARD:
			if (logInfo.getCardType() == CardModel.TRAP && logInfo.getPlayerId() != playerId) {
				data.writeUTF("");
			} else {
				data.writeUTF(logInfo.getCardId());
			}
			data.writeByte(logInfo.getTarget().size());
			for (LogDetailInfo info : logInfo.getTarget()) {
				if (info.getType() == LogDetailInfo.DRAW && logInfo.getPlayerId() != playerId) {
					data.writeUTF("");
					data.writeInt(info.getValue());
					continue;
				}
				data.writeUTF(info.getCardId());
				data.writeInt(info.getValue());
			}
			break;
		}
		
		session.send(data);
		logger.debug("{} - {} playerId:{} type:{} cardId:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.LOG_SYNC, logInfo.getPlayerId(), logInfo.getType(), logInfo.getCardId());
	}
		
	public static void summonSelect(ISession session, int areaIndex) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.SUMMON_SELECT);
		
		data.writeByte(areaIndex);
		
		session.send(data);
		logger.debug("{} - {} playerId:{} guide:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.SUMMON_SELECT, areaIndex);
	}
	
	public static void tapCardSync(ISession session, IByteBuffer d) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.TAP_CARD_SYNC);
		
		data.writeByteBuffer(d);
		
		session.send(data);
	}
	
	public static void other(ISession session, IByteBuffer d) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.OTHER_SYNC);
		
		data.writeByteBuffer(d);
		
		session.send(data);
	}
	
	public static void targetSelectRequest(ISession session, CardBase card, String type, String target) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		switch (type) {
		case SkillManager.MOVE:
			data.writeByte(FightMsgConst.MOVE_TARGET_SELECT_REQUEST);
			data.writeByte(card.getType());
			data.writeInt(card.getUid());
			data.writeUTF(target);
			data.writeBoolean(false);
			logger.debug("{} - {} target:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.MOVE_TARGET_SELECT_REQUEST, target);
			break;
		case SkillManager.MOVE_SELF:
			data.writeByte(FightMsgConst.MOVE_TARGET_SELECT_REQUEST);
			data.writeByte(card.getType());
			data.writeInt(card.getUid());
			data.writeUTF(target);
			data.writeBoolean(true);
			logger.debug("{} - {} target:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.MOVE_TARGET_SELECT_REQUEST, target);
			break;
		default:
			data.writeByte(FightMsgConst.TARGET_SELECT_REQUEST);
			data.writeByte(card.getType());
			data.writeInt(card.getUid());
			data.writeUTF(target);
			logger.debug("{} - {} target:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.TARGET_SELECT_REQUEST, target);
			break;
		}
		
		session.send(data);
	}
	
	public static void cardTargetSelectRequest(ISession session, BattleRole fighter, ArrayList<CardBase> cards) {
		if (session == null || session.isClosed()) {
			return;
		}
		if (cards.size() == 0) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.CARD_TARGET_SELECT_REQUEST);
		
		data.writeByte(cards.size());
		for (CardBase card : cards) {
			syncDetailCardInfo(data, card, fighter);
		}
		
		session.send(data);
		logger.debug("{} - {} size:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.CARD_TARGET_SELECT_REQUEST, cards.size());
	}
	
	public static void playCardEnd(ISession session, CardBase card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.PLAY_CARD_END);
		
		data.writeByte(card.getType());
		
		session.send(data);
		logger.debug("{} - {} cardType:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.PLAY_CARD_END, card.getType());
	}

	public static void destoryCard(ISession session, ArrayList<CardBase> cards) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.DESTORY_TROOP_CARD);

		data.writeByte(cards.size());
		for (CardBase card : cards) {
			data.writeInt(card.getUid());
		}
		
		session.send(data);
		logger.debug("{} - {} size:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.DESTORY_TROOP_CARD, cards.size());
	}

	public static void destoryArea(ISession session, int playerId, Area area) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.DESTORY_AREA);

		data.writeBoolean(playerId == area.getPlayerId());
		data.writeByte(area.getIndex());
				
		session.send(data);
		logger.debug("{} - {} playerId:{} areaIndex:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.DESTORY_AREA, area.getPlayerId(), area.getIndex());
	}

	public static void interruptCard(ISession session, CardBase card) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.INTERRUPT_CARD);

		data.writeInt(card.getUid());
				
		session.send(data);
		logger.debug("{} - {} cardUid:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.DESTORY_AREA, card.getUid());
	}

	public static void roleCanBeAttack(ISession session, BattleRole fighter) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.ROLE_CAN_BE_ATTACK);

		boolean isCanBeAttack = fighter.isCanBeAttack();
		data.writeInt(fighter.getUid());
		data.writeBoolean(isCanBeAttack);
		
		session.send(data);
		logger.debug("{} - {} heroUid:{} isCanBeAttack:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.ROLE_CAN_BE_ATTACK, fighter.getUid(), isCanBeAttack);
	}

	public static void areaLvUpChangeDraw(ISession session, boolean isMine, int value) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.AREA_LV_UP_CHANGE_DRAW);

		data.writeBoolean(isMine);
		data.writeByte(value);
		
		session.send(data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.AREA_LV_UP_CHANGE_DRAW);
	}
}
