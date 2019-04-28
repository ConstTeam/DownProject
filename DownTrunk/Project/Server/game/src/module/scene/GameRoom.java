package module.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.card.CardDeckModel;
import config.model.card.CardModel;
import config.model.guide.GuideCardModel;
import config.model.guide.GuideGateCardGroupModel;
import config.model.guide.GuideGateModel;
import config.model.robot.RobotCardGroupModel;
import config.model.robot.RobotCardModel;
import config.model.robot.RobotModel;
import config.model.skill.SkillModel;
import db.GameRoomDao;
import message.game.fight.FightMsgSend;
import message.game.room.RoomMsgSend;
import module.area.Area;
import module.card.ArtifactCard;
import module.card.CardBase;
import module.card.FindCard;
import module.card.SpellCard;
import module.card.TrapCard;
import module.card.TroopCard;
import module.fight.BattleNAIRole;
import module.fight.BattleRole;
import module.fight.GuideBattleRole;
import module.fight.IBattleObject;
import module.log.LogDetailInfo;
import module.log.LogInfo;
import module.templet.TempletBase;
import net.ISession;
import quest.QuestManager;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import skill.CardComparator;
import skill.SkillArg;
import skill.SkillManager;
import skill.TrapTriggerManager;
import skill.TriggerEvent;
import skill.TriggerManager;
import sys.GameRoomManager;
import sys.GameTimer;
import util.ErrorPrint;
import util.Tools;

public class GameRoom extends RoomConst implements ISceneAction {

	private static final Logger logger = LoggerFactory.getLogger(GameRoom.class);

	/** 房间Id */
	private int roomId;
	/** 房主 */
	private int owner;
	/** 房间状态 */
	private int state = ROOM_STATE_PLAYING;
	/** 房间状态 */
	private int playState = PLAY_STATE_READY;
	/** 当前局玩家 */
	private int nowPlayer;
	/** 先手玩家 */
	private int firstPlayer;
	/** 回合数 */
	private int round = 0;

	/** 玩家Session列表<玩家id, ISession> */
	private HashMap<Integer, ISession> sessions = new HashMap<>();
	/** 玩家表 <玩家id, Player> */
	private HashMap<Integer, PlayerInfo> players = new HashMap<>();
	/** 战斗玩家表 <玩家id, FightRole> */
	private HashMap<Integer, BattleRole> fighters = new HashMap<>();
	/** 战斗玩家表 <玩家uid, FightRole> */
	private HashMap<Integer, BattleRole> fightersByUid = new HashMap<>();

	/** 玩家准备表 <玩家id, Boolean> */
	private HashMap<Integer, Boolean> ready = new HashMap<>();
	/** 触发管理器 */
	private TriggerManager triggerManager = new TriggerManager();
	/** 游戏初始参数模板 */
	private TempletBase templet;
	
	private ScheduledFuture<?> future;
	/** 卡牌表 <卡牌Uid, CardBase> */
	private HashMap<Integer, CardBase> cards = new HashMap<>();
	/** 卡组表 <卡组Id, CardDeckModel> */
	private HashMap<Integer, CardDeckModel> decks = new HashMap<>();
	/** 组合陷阱 <输入输出, ArrayList<CardBase>> */
	private HashMap<String, ArrayList<CardBase>> constructTrapCards = new HashMap<>();
	/** 回溯log */
	private ArrayList<LogInfo> logInfos = new ArrayList<>();

	/** 当前局卡牌唯一ID自增 */
	private int cardUId = 1001;
	/** 当前局英雄唯一ID自增 */
	private int heroUId = 100;
	/** 是否开启倒计时 */
	private boolean countDown = true; 
	/** 倒计时时长（毫秒） */
	private int turnCountDownTime = TURN_COUNT_DOWN_TIME;
	
	@Override
	public int joinGame(PlayerInfo player, ISession session) {
		int playerId = player.getPlayerId();
		if (players.get(playerId) != null || players.size() >= 2) {
			return -1;
		}
		/*
		 * 创建本局中的英雄
		 */
		BattleRole battleRole = new BattleRole(playerId, player.getNickname(), this.templet.initHp, this.heroUId);
		battleRole.setRoomId(this.roomId);
		battleRole.setIcon(player.getIcon());
		if (battleRole.getQuestManager() == null) {
			return -1;
		}
		players.put(playerId, player);
		sessions.put(playerId, session);
		fighters.put(playerId, battleRole);
		fightersByUid.put(battleRole.getUid(), battleRole);
		this.heroUId++;
		/*
		 * 更新Redis信息
		 */
		logger.info("玩家：{}，房间Id：{}，加入房间。", playerId, this.roomId);
		RoomMsgSend.intoRoom(session);
		if (players.size() == 2) {
			gameStart();
		}
		return SUCCESS;
	}
	
	/**
	 * 初始化机器人加入游戏房间
	 */
	public void guideRobotJoinGame() {
		int playerId = 0;
		int guideId = this.templet.arg1;
		GuideGateModel gate = ConfigData.guideGateModels.get(guideId);
		String nickname = gate.RobotName; // TODO 机器人昵称
		String icon = gate.RobotIcon; 	  // TODO 机器人头像
		PlayerInfo playerInfo = new PlayerInfo();
		playerInfo.setPlayerId(playerId);
		
		GuideBattleRole battleRole = new GuideBattleRole(nickname, this.templet.initHp, this.heroUId);
		battleRole.setRoomId(this.roomId);
		battleRole.setIcon(icon);
		players.put(battleRole.getPlayerId(), playerInfo);
		fighters.put(playerId, battleRole);
		fightersByUid.put(battleRole.getUid(), battleRole);
		this.heroUId++;
		ready(playerId);
		logger.info("玩家：{}，房间Id：{}，加入房间。", playerId, this.roomId);
	}

	/**
	 * 初始化机器人加入游戏房间
	 */
	public void robotJoinGame() {
		int playerId = 0;
		int robotId = this.templet.arg1;
		
		RobotModel robotModel = ConfigData.robotModels.get(robotId);
		String nickname = robotModel.RobotName; // TODO 机器人昵称
		String icon = robotModel.RobotIcon; // TODO 机器人头像
		PlayerInfo playerInfo = new PlayerInfo();
		playerInfo.setPlayerId(playerId);
		
		BattleNAIRole battleRole = new BattleNAIRole(nickname, this.templet.initHp, this.heroUId); // TODO 机器人AI
		battleRole.setRoomId(this.roomId);
		battleRole.setIcon(icon);
		players.put(battleRole.getPlayerId(), playerInfo);
		fighters.put(playerId, battleRole);
		fightersByUid.put(battleRole.getUid(), battleRole);
		this.heroUId++;
		ready(playerId);
		logger.info("玩家：{}，房间Id：{}，加入房间。", playerId, this.roomId);
	}
	
	@Override
	public int exitGame(PlayerInfo player) {
		int playerId = player.getPlayerId();

		/*
		 * 更新Redis信息
		 */
		RedisProxy.getInstance().updatePlayerInfo(player, "roomId");
		logger.info("玩家：{}，房间Id：{}，退出房间。", playerId, this.roomId);
		return SUCCESS;
	}

	@Override
	public void gameStart() {
		logger.info("房间Id：{}，开始游戏。", this.roomId);
		playState = PLAY_STATE_START;
		
		switch (this.getTemplet().type) {
		case RoomConst.ROOM_TYPE_PVP:
//			startTimer();
			pvpGameStart();
			// 发牌
//			firstDeal();
			break;
		case RoomConst.ROOM_TYPE_GUIDE:
			guideGameStart();
			guideFirstDeal();
			break;
		case RoomConst.ROOM_TYPE_ROBOT:
			startTimer();
			robotGameStart();
			// 发牌
			firstDeal();
			break;
		}
	}
	
	/**
	 * 新手指引模式
	 */
	public void guideGameStart() {
		
		int guideId = this.getTemplet().arg1;
		GuideGateModel gate = ConfigData.guideGateModels.get(guideId);
		GuideGateCardGroupModel robotCardGroup = ConfigData.guideGateCardGroupModels.get(gate.RobotCardGroupId);
		GuideGateCardGroupModel cardGroup = ConfigData.guideGateCardGroupModels.get(gate.CardGroupId);
		int playerId = 0;
		int robotId = 0;
		for (BattleRole fighter : fighters.values()) {
			ArrayList<CardBase> cards = fighter.getDecks();
			if (fighter.isRobot()) {
				robotId = fighter.getPlayerId();
				fighter.setHp(gate.RobotHP);
				// 设置区域符文
				for (int i = 0; i <= BattleRole.AREA_MAX_INDEX; i++) {
					Area area = fighter.getArea(i);
					area.setRune(robotCardGroup.Runes.get(i));
					// 设置主符文
					if (i == 1) {
						fighter.setMainRune(area.getRune());
					}
				}
				// TODO 设置机器人卡组
				for (int i=0; i<robotCardGroup.cards.size(); i++) {
					GuideCardModel guideCard = robotCardGroup.cards.get(i);
					CardBase card = createCard(robotId, String.valueOf(guideCard.CardID));
					if (card != null) {
						cards.add(card);
					}
				}
			} else {
				playerId = fighter.getPlayerId();
				fighter.setHp(gate.HP);
				// TODO 设置玩家卡组
				// 设置区域符文
				for (int i = 0; i <= BattleRole.AREA_MAX_INDEX; i++) {
					Area area = fighter.getArea(i);
					area.setRune(cardGroup.Runes.get(i));
					// 设置主符文
					if (i == 1) {
						fighter.setMainRune(area.getRune());
					}
				}
				// TODO 设置玩家卡组
				for (int i=0; i<cardGroup.cards.size(); i++) {
					GuideCardModel guideCard = cardGroup.cards.get(i);
					CardBase card = createCard(playerId, String.valueOf(guideCard.CardID));
					if (card != null) {
						if (guideCard.PathFinder != 3) {
							card.setPathfinder(guideCard.PathFinder);
						}
						cards.add(card);
					}
				}
			}
		}
			
		// TODO 设置先手
		if (gate.First == 1) {
			nowPlayer = robotId;
			firstPlayer = robotId;
		} else {
			nowPlayer = playerId;
			firstPlayer = playerId;
		}
		
		FightMsgSend.intoRoom(this.sessions.get(playerId), fighters.get(playerId), fighters.get(robotId));
	}
	
	/**
	 * 匹配场模式
	 */
	public void pvpGameStart() {

		// 随机先手
		ArrayList<Integer> list = new ArrayList<>();
		list.addAll(players.keySet());
		Collections.shuffle(list);
		nowPlayer = list.get(0);
		firstPlayer = list.get(0);

		// 初始化消息
		int enemyPlayerId = getEnemyId(nowPlayer);
		FightMsgSend.intoRoom(this.sessions.get(nowPlayer), fighters.get(nowPlayer), fighters.get(enemyPlayerId));
		FightMsgSend.intoRoom(this.sessions.get(enemyPlayerId), fighters.get(enemyPlayerId), fighters.get(nowPlayer));
	}
	
	/**
	 * 匹配机器人模式
	 */
	public void robotGameStart() {
		
		int robotConfId = this.getTemplet().arg1;
		RobotModel robot = ConfigData.robotModels.get(robotConfId);
		RobotCardGroupModel robotCardGroup = ConfigData.robotCardGroupModels.get(robot.RobotCardGroupId);
		int playerId = 0;
		int robotId = 0;
		for (BattleRole fighter : fighters.values()) {
			ArrayList<CardBase> cards = fighter.getDecks();
			if (fighter.isRobot()) {
				robotId = fighter.getPlayerId();
				// 设置区域符文
				for (int i = 0; i <= BattleRole.AREA_MAX_INDEX; i++) {
					Area area = fighter.getArea(i);
					area.setRune(robotCardGroup.Runes.get(i));
					// 设置主符文
					if (i == 1) {
						fighter.setMainRune(area.getRune());
					}
				}
				// TODO 设置机器人卡组
				for (int i=0; i<robotCardGroup.cards.size(); i++) {
					RobotCardModel robotCard = robotCardGroup.cards.get(i);
					CardBase card = createCard(robotId, String.valueOf(robotCard.CardID));
					if (card != null) {
						cards.add(card);
					}
				}
				if (robot.Rule == 1) {
					Collections.shuffle(cards);
				}
			} else {
				/*
				 * 玩家卡组
				 */
				playerId = fighter.getPlayerId();
				CardDeckModel deckModel = decks.get(fighter.getPlayerId());
				// 设置区域符文
				for (int i = 0; i <= BattleRole.AREA_MAX_INDEX; i++) {
					Area area = fighter.getArea(i);
					area.setRune(deckModel.Runes.get(i));
					// 设置主符文
					if (i == 1) {
						fighter.setMainRune(area.getRune());
					}
				}

				Iterator<Entry<String, Object>> iterator = deckModel.cardIds.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, Object> next = iterator.next();
					String cardId = next.getKey();
					int num = (int) next.getValue();
					for (int i = 1; i <= num; i++) {
						CardBase card = createCard(fighter.getPlayerId(), cardId);
						if (card != null) {
							cards.add(card);
						}
					}
				}
				Collections.shuffle(cards);
			}
		}
			
		// 随机先手
		ArrayList<Integer> list = new ArrayList<>();
		list.addAll(players.keySet());
		Collections.shuffle(list);
		nowPlayer = list.get(0);
		firstPlayer = list.get(0);
		
		FightMsgSend.intoRoom(this.sessions.get(playerId), fighters.get(playerId), fighters.get(robotId));
	}
	
	/**
	 * 创建一张卡牌
	 * 
	 * @param playerId
	 * @param cardId
	 * @return
	 */

	public CardBase createCard(int playerId, String cardId) {
		BattleRole role = this.getBattleRole(playerId);
		String replaceCardId = cardId;
		if (role != null && !Tools.isEmptyString(role.getReplaceCard().get(cardId))) {
			replaceCardId = role.getReplaceCard().get(cardId);
		}
		CardBase card = createCard(playerId, replaceCardId, false);
		if (card == null) {
			return null;
		}
		card.setId(cardId);
		return card;
	}
	
	private CardBase createCard(int playerId, String cardId, boolean gm) {
		CardModel cardModel = ConfigData.cardModels.get(cardId);
		if (cardModel == null) {
			return null;
		}
		if (cardModel.Open == 0 && !gm) {
			logger.error("玩家：{}，房间Id：{}，卡牌Id:{}。该卡牌未开放，不能进行创建。", playerId, this.roomId, cardId);
			return null;
		}
		CardBase card = null; 
		switch (cardModel.type) {
		case CardModel.TROOP:
			card = new TroopCard(cardModel);
			SkillManager.getInstance().initSkillEffect(card);
			break;
		case CardModel.SPELL:
			card = new SpellCard(cardModel);
			SkillManager.getInstance().initSkillEffect(card);
			break;
		case CardModel.ARTIFACT:
			card = new ArtifactCard(cardModel);
			break;
		case CardModel.TRAP:
			card = new TrapCard(cardModel);
			break;
		default:
			return card;
		}

		// 设置当前局中唯一Id
		card.setUid(this.cardUId);
		card.setPlayerId(playerId);
		cardUId++;
		cards.put(card.getUid(), card);

		this.triggerManager.addTriggerEvent(card);
		triggerEffect(TriggerManager.CREATE_CARD, playerId, card, 0);
		return card;
	}
	
	/**
	 * 按卡牌Uid获取卡牌
	 * 
	 * @param cardUid
	 * @return
	 */
	public CardBase getCard(int cardUid) {
		return cards.get(cardUid);
	}

	/**
	 * 首次发牌
	 */
	private void firstDeal() {
		for (BattleRole fighter : fighters.values()) {
			int playerId = fighter.getPlayerId();
			for (int i = 0; i < this.templet.firstCardNum; i++) {
				dealInHandCards(fighter, -1);
			}
			boolean isFirst = playerId == nowPlayer;
			// 进行消息同步
			FightMsgSend.firstDeal(sessions.get(playerId), fighter, isFirst);
		}
	}
	
	private void guideFirstDeal() {
		for (BattleRole fighter : fighters.values()) {
			for (int i = 0; i < this.templet.firstCardNum; i++) {
				dealInHandCards(fighter, -1);
			}
		}
	}
	
	/**
	 * 开局换牌
	 * 
	 * @param playerId
	 * @param isReplace
	 * @return
	 */
	public boolean replaceDealCard(int playerId, ArrayList<Boolean> isReplace) {
		BattleRole fighter = fighters.get(playerId);
		if (getRound() >= 1) {
			return false;
		}
		if (fighter.getReplaceDealCount() >= this.templet.canChangeCount) {
			logger.error("玩家：{}，房间Id：{}，已起手换牌{}次。", playerId, this.roomId, fighter.getReplaceDealCount());
			return false;
		}
		HashMap<Integer, CardBase> cards = new HashMap<>();
		for (int i = 0; i < this.templet.firstCardNum; i++) {
			if (isReplace.get(i)) {
				CardBase remove = fighter.getHandCards().remove(i);
				fighter.getDecks().add(remove);
				CardBase card = dealInHandCards(fighter, i);
				cards.put(remove.getUid(), card);
			}
		}
		fighter.addReplaceDealCount();
		FightMsgSend.replaceDealResult(this.sessions.get(playerId), cards, fighter);
		return true;
	}

	/**
	 * 发现-选择一张卡牌
	 * 
	 * @param playerId
	 * @param index
	 * @return
	 */
	public boolean findCardSelect(int playerId, int index) {
		BattleRole fighter = this.fighters.get(playerId);
		int enemyId = getEnemyId(playerId);
		if (fighter.getFindCards().size() == 0) {
			return false;
		}
		
		syncStart(playerId);
		
		ArrayList<CardBase> cards = new ArrayList<>();
		FindCard findCard = fighter.getFindCards().remove(0);
		if (index >= findCard.getCards().size()) {
			logger.error("玩家：{}，房间Id：{}，选牌Index：{}，发现选择卡牌失败。index有误。", playerId, roomId, index);
			return false;
		}
		CardBase cardBase = findCard.getCards().get(index);
		cardBase.setPlayerId(playerId);
		addHandCard(fighter, cardBase);
		cards.add(cardBase);
		fighter.setFirstFind(findCard.getCardUid(), cardBase.getRealId());
		
		if (playState == PLAY_STATE_AUTO && !findCard.isSend()) {
			FightMsgSend.cardSync(this.sessions.get(playerId), cards, fighter);
			FightMsgSend.enemyCardSync(this.sessions.get(enemyId), cards, fighter);
		} else {
			FightMsgSend.findResult(this.sessions.get(playerId), true, index, fighter.getHandCards().indexOf(cardBase) == -1);
			FightMsgSend.findResult(this.sessions.get(enemyId), false, index, fighter.getHandCards().indexOf(cardBase) == -1);
		}
		
		if (findCard.getType() == FindCard.FIND && CardModel.TALE.equals(cardBase.getSubType())) {
			triggerEffect(TriggerManager.FIND_TALE, fighter.getPlayerId(), cardBase, 1);
		}
		
		this.findCardSync(fighter);
		
		syncEnd(playerId);
		
		if (fighter.getFindCards().size() == 0) {
			fighter.setInterruptSkillArg(null);
			switch (fighter.getState()) {
			case BattleRole.START:
				nextStart(playerId, false);
				break;
			case BattleRole.END:
				nextTurnFinish(playerId, false);
				break;
			}
		}
		return true;
	}

	/**
	 * 组装陷阱-选择
	 * 
	 * @param playerId
	 * @param cardUid
	 * @return
	 */
	public boolean constructTrapCardSelect(int playerId, int cardUid) {
		BattleRole fighter = this.fighters.get(playerId);
		int enemyId = getEnemyId(playerId);
		if (fighter.getFindCards().size() == 0) {
			return false;
		}
		
		FindCard findCard = fighter.getFindCards().remove(0);
		try {
			CardBase card = this.cards.get(cardUid);
			if (findCard.getCards().indexOf(card) == -1) {
				logger.error("玩家：{}，房间Id：{}，cardUid:{}，选择的牌不在发送的陷阱列表内。", playerId, this.roomId, cardUid);
				return false;
			}
			if (CardModel.TRAP_INPUT.equals(card.getGenius())) {
				fighter.setInputCard(card);
				syncStart(playerId);
				this.findCardSync(fighter);
				syncEnd(playerId);
			} else if (CardModel.TRAP_OUTPUT.equals(card.getGenius())) {
				if (fighter.getInputCard() == null) {
					logger.error("玩家：{}，房间Id：{}，cardUid:{}，找不到前置组装陷阱INPUT牌。", playerId, this.roomId, cardUid);
					return false;
				}
				String str = String.format("%s-%s", fighter.getInputCard().getRealId(), card.getRealId());
				CardModel model = ConfigData.constructTraps.get(str);
				CardBase cardBase = createCardInHandCards(model.ID, fighter);
				if (cardBase == null) {
					logger.error("玩家：{}，房间Id：{}，cardId:{}，组装：{}。组装陷阱失败，卡牌ID有误。", playerId, this.roomId, model.ID, str);
					return false;
				}
				boolean isSummon = false;
				if (findCard.getAreaIndex() >= 0 && findCard.getAreaIndex() <= 2) {
					Area area = fighter.getArea(findCard.getAreaIndex());
					summon(playerId, fighter, area, cardBase, null, null, false, false);
					removeHandCard(fighter, cardBase);	
					isSummon = true;
				}
				fighter.setFirstFind(findCard.getCardUid(), cardBase.getRealId());
				
				syncStart(playerId);
				
				FightMsgSend.constructTrapRes(this.sessions.get(playerId), playerId, cardBase, fighter, isSummon);
				FightMsgSend.constructTrapRes(this.sessions.get(enemyId), enemyId, cardBase, fighter, isSummon);
				if (isSummon) {
					FightMsgSend.trapCardPlay(this.sessions.get(playerId), cardBase, cardBase.getRealId(), cardBase.getUid(), findCard.getAreaIndex(), cardBase.getMainRowIndex());
					FightMsgSend.trapCardPlay(this.sessions.get(enemyId), null, cardBase.getRealId(), cardBase.getUid(), findCard.getAreaIndex(), cardBase.getMainRowIndex());
				}

				fighter.setInputCard(null);
				this.findCardSync(fighter);
				syncEnd(playerId);
			}
		} finally {
			if (fighter.getFindCards().size() == 0) {
				fighter.setInterruptSkillArg(null);
				switch (fighter.getState()) {
				case BattleRole.START:
					nextStart(playerId, false);
					break;
				case BattleRole.END:
					nextTurnFinish(playerId, false);
					break;
				}
			}
		}
		return true;
	}

	public boolean checkCardSelect(int playerId, boolean select) {
		BattleRole fighter = this.fighters.get(playerId);
		if (fighter.getDecks().size() == 0) {
			return false;
		}
		CardBase card = fighter.getDecks().get(0);
		if (select) {
			fighter.getDecks().remove(0);
			fighter.getDecks().add(card);
		}
		fighter.setStatus(BattleRole.CHECK, false);
		FightMsgSend.cardCheckSelectResult(this.sessions.get(playerId), card);
		fighter.setInterruptSkillArg(null);
		switch (fighter.getState()) {
		case BattleRole.START:
			nextStart(playerId, false);
			break;
		case BattleRole.END:
			nextTurnFinish(playerId, false);
			break;
		}
		return true;
	}
	
	public void sendTargetSelect(CardBase card, SkillArg arg) {
		if (!SkillManager.getInstance().haveTarget(arg)) {
			return;
		}
		BattleRole fighter = null;
		if (card.getType() == CardModel.TRAP) {
			fighter = arg.getFighter();
			FightMsgSend.revealCardSync(this.getSession(fighter.getPlayerId()), card, getBattleRole(card.getPlayerId()));
		} else {
			fighter = this.getBattleRole(card.getPlayerId());
		}
		if (fighter.isRobot()) {
			return;
		}
		fighter.setInterruptSkillArg(arg);
		if (fighter.getState() == BattleRole.PLAY_CARD) {
			if (arg.getModel().Trigger.equals(TriggerManager.WARCRY)) {
				fighter.setStatus(BattleRole.WARCRY_INTERRUPT, true);
			}
		}
		fighter.setStatus(BattleRole.TARGET_SELECT, true);

		if (SkillManager.C_HAND_CARD_ENEMY.equals(arg.getModel().Target)) {
			int enemyId = this.getEnemyId(card.getPlayerId());
			BattleRole enemy = this.getBattleRole(enemyId);
			FightMsgSend.cardTargetSelectRequest(getSession(card.getPlayerId()), fighter, enemy.getHandCards());
			return;
		}
		FightMsgSend.targetSelectRequest(getSession(arg.getFighter().getPlayerId()), card, arg.getModel().Type, arg.getModel().Target);
	}

	public boolean targetSelect(int playerId, SelectObject sobj) {
		int enemyId = getEnemyId(playerId);
 		BattleRole fighter = getBattleRole(playerId);
		SkillArg arg = fighter.getInterruptSkillArg();
		if (arg == null) {
			logger.error("玩家：{}，房间Id：{}，选择目标失败，中断参数不存在。", playerId, this.roomId);
			return false;
		}
		CardBase card = arg.getSelfCard();
		if (!setSpellSkillTarget(sobj, fighter, enemyId, card)) {
			return false;
		}
		return interruptRestart(playerId);
	}
	
	public boolean interruptRestart(int playerId) {
		BattleRole fighter = getBattleRole(playerId);
		SkillArg arg = fighter.getInterruptSkillArg();
		if (arg == null) {
			return false;
		}
		CardBase selfCard = arg.getSelfCard();
		CardBase card = arg.getTriggerOwner();
		Area area = arg.getArea();

		fighter.setStatus(BattleRole.TARGET_SELECT, false);
		fighter.setInterruptSkillArg(null);
		
		switch (fighter.getState()) {
		case BattleRole.START:
			triggerManager.startEffect(this, card, fighter, area, 2);
			nextStart(playerId, false);
			break;
		case BattleRole.END:
			triggerManager.endEffect(this, card, fighter, area);
			nextTurnFinish(playerId, false);
			break;
		case BattleRole.BREACH:
			// triggerManager.breachEffect(this, card, fighter, area);
			break;
		case BattleRole.PLAY_CARD:
			syncStart(playerId);
			if (fighter.getStatus(BattleRole.WARCRY_INTERRUPT)) {
				triggerManager.autoWarcry(this, card, fighter, area);
				fighter.setStatus(BattleRole.WARCRY_INTERRUPT, false);
			} else {
				triggerManager.autoPlayCard(arg, card);
				if (fighter.getInterruptSkillArg() == null) {
					FightMsgSend.playCardEnd(getSession(playerId), card);
				}
			}
			syncEnd(playerId);
			clearRoleState(playerId);
			break;
		}

		selfCard.getTarget().clear();
		return true;
	}

	public ArrayList<Object> getTarget(SkillArg arg) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		BattleRole fighter = room.getBattleRole(arg.getTriggerOwner().getPlayerId());
		ArrayList<Object> target = selfCard.getTarget();
		if (target.size() == 0) {
			if (playState == PLAY_STATE_AUTO || fighter.isRobot()) {
				SkillManager.getInstance().randomTarget(arg);
				target = selfCard.getTarget();
			} else {
				if (triggerManager.isNeedTargetSelect(selfCard, arg.getModel())) {
					sendTargetSelect(selfCard, arg);
					return target;
				}
			}
		}
		return target;
	}
	
	private void awakeTroop(BattleRole fighter) {
		ArrayList<TroopCard> troops = new ArrayList<>();
		ArrayList<TroopCard> sleeps = new ArrayList<>();
		ArrayList<TroopCard> controls = new ArrayList<>();
		// 唤醒所有区域的部队牌
		for (Area area : fighter.getAreas()) {
			for (CardBase card : area.getTroops()) {
				TroopCard troopCard = (TroopCard) card;
				troopCard.startTurn();
				troops.add(troopCard);
				if (troopCard.isChange()) {
					sleeps.add(troopCard);
				}
			}
		}
		for (CardBase card : fighter.getHandCards()) {
			if (card.getType() == CardModel.TROOP) {
				TroopCard troopCard = (TroopCard) card;
				troopCard.startTurn();
			}
		}

		this.troopStatusSync(troops, TroopCard.ATTACKED);
		this.troopStatusSync(troops, TroopCard.ENCHANT);
		this.troopStatusSync(sleeps, TroopCard.SLEEP);
		this.troopStatusSync(controls, TroopCard.CONTROL);
	}
	
	private void removeControl(BattleRole fighter) {
		ArrayList<TroopCard> controls = new ArrayList<>();
		// 唤醒所有区域的部队牌
		for (Area area : fighter.getAreas()) {
			for (CardBase card : area.getTroops()) {
				TroopCard troopCard = (TroopCard) card;
				if (troopCard.isControl()) {
					boolean isMove = SkillManager.getInstance().move(this, troopCard, troopCard.getOldArea());
					if (isMove) {
						troopCard.setPlayerId(getEnemyId(fighter.getPlayerId()));
						troopCard.setOldArea(null);
						troopCard.setStatus(TroopCard.CONTROL, false);
						controls.add(troopCard);
					}
				}
			}
		}
		this.troopStatusSync(controls, TroopCard.CONTROL);
	}
	
	public CardBase createCardInHandCards(String cardId, BattleRole fighter) {
		CardBase card = createCard(fighter.getPlayerId(), cardId);
		if (card == null) {
			return null;
		}
		addHandCard(fighter, card);
		return card;
	}

	public CardBase deal(BattleRole fighter) {
		String key = this.triggerManager.drawChange(this, fighter);
		if (Tools.isEmptyString(key)) {
			gmCreateCard(fighter);
			CardBase card = fighter.deal();
			deckCardNumberSync(fighter);
			return card;
		}
		switch (key) {
		case ArtifactCard.DECK_COST_MAX:
			return fighter.removeCostMaxInDeck();
		}
		return null;
	}

	private CardBase dealInHandCards(BattleRole fighter, int index) {
		CardBase card = deal(fighter);
		addHandCard(fighter, card, index);
		return card;
	}
	
	private void addHandCards(BattleRole fighter, ArrayList<CardBase> cards) {
		for (CardBase card : cards) {
			addHandCard(fighter, card);
		}
	}
	
	public void addHandCard(BattleRole fighter, CardBase card) {
		addHandCard(fighter, card, -1);
	}
	
	public void addHandCard(BattleRole fighter, CardBase card, int index) {
		if (fighter.getHandCards().size() >= BattleRole.HAND_CARD_MAX_COUNT) {
			return;
		}
		if (fighter.getState() != BattleRole.END) {
			triggerManager.triggerEffect(this, TriggerManager.DRAW, fighter.getPlayerId(), card, 1);
		}
		if (index < 0) {
			fighter.addHandCard(card);
		} else {
			fighter.addHandCard(card, index);
		}
		this.triggerEffect(TriggerManager.HAND_CARD_CHANGE, fighter.getPlayerId(), card, 1);
	}
	
	public void removeHandCard(BattleRole fighter, CardBase card) {
		fighter.removeHandCard(card);
		this.triggerEffect(TriggerManager.HAND_CARD_CHANGE, fighter.getPlayerId(), card, 1);
	}
	
	public void ready(int playerId) {
		if (this.ready.get(playerId) != null) {
			return;
		}
		this.ready.put(playerId, true);
		if (this.getTemplet().type == ROOM_TYPE_PVP) {
			BattleRole fighter = fighters.get(playerId);
			Collections.shuffle(fighter.getDecks());
			if (this.ready.size() < 2) {
				readySync(playerId);
			}
		}
		if (this.ready.size() == 2) {
			switch (this.getTemplet().type) {
			case ROOM_TYPE_PVP:
				pvpfirstTurnStart();
				break;

			case ROOM_TYPE_GUIDE:
				guidefirstTurnStart();
				break;
				
			case ROOM_TYPE_ROBOT:
				pvpfirstTurnStart();
				break;
			}
		}
	}
	
	private void pvpfirstTurnStart() {
		interruptTimer();
		int enemyId = getEnemyId(nowPlayer);
		BattleRole firstRole = getBattleRole(nowPlayer);
		BattleRole enemyRole = getBattleRole(enemyId);
		
		deckCardNumberSync(firstRole);
		deckCardNumberSync(enemyRole);
		
		FightMsgSend.enemyCardSync(this.sessions.get(nowPlayer), enemyRole.getHandCards(), enemyRole);
		FightMsgSend.enemyCardSync(this.sessions.get(enemyId), firstRole.getHandCards(), firstRole);
		// 通知开始
		FightMsgSend.selfReadySync(this.sessions.get(nowPlayer), firstRole.getHandCards());
		FightMsgSend.selfReadySync(this.sessions.get(enemyId), enemyRole.getHandCards());
		FightMsgSend.gameStartSync(this.sessions.get(nowPlayer), firstRole, enemyRole);
		FightMsgSend.gameStartSync(this.sessions.get(enemyId), enemyRole, firstRole);
		
		// 后手玩家多发一张主元素牌
		CardBase card = createCardInHandCards(String.valueOf(200 + enemyRole.getMainRune()), enemyRole);
		if (card != null) {
			ArrayList<CardBase> cards = new ArrayList<>();
			cards.add(card);
			syncStart(enemyId);
			FightMsgSend.cardSync(this.sessions.get(enemyId), card, enemyRole);
			FightMsgSend.enemyCardSync(this.sessions.get(nowPlayer), cards, enemyRole);
			syncEnd(enemyId);
		}
		
		// 通知先手玩家出牌
		turnStart(nowPlayer);
	}
	
	private void guidefirstTurnStart() {
		interruptTimer();
		int enemyId = getEnemyId(nowPlayer);
		BattleRole firstRole = getBattleRole(nowPlayer);
		BattleRole enemyRole = getBattleRole(enemyId);
		
		deckCardNumberSync(firstRole);
		deckCardNumberSync(enemyRole);
		
		drawCardSync(firstRole, firstRole.getHandCards());
		drawCardSync(enemyRole, enemyRole.getHandCards());
		
		// 通知开始
		FightMsgSend.gameStartSync(this.sessions.get(nowPlayer), firstRole, enemyRole);
		FightMsgSend.gameStartSync(this.sessions.get(enemyId), enemyRole, firstRole);
		
		if (this.templet.arg1 > 2) {
			// 新手第三关开始后手多发一张主元素牌
			CardBase card = createCardInHandCards(String.valueOf(200 + enemyRole.getMainRune()), enemyRole);
			if (card != null) {
				ArrayList<CardBase> cards = new ArrayList<>();
				cards.add(card);
				syncStart(enemyId);
				FightMsgSend.cardSync(this.sessions.get(enemyId), card, enemyRole);
				FightMsgSend.enemyCardSync(this.sessions.get(nowPlayer), cards, enemyRole);
				syncEnd(enemyId);
			}
		}
		
		// 通知先手玩家出牌
		turnStart(nowPlayer);
	}

	public void turnStart(int playerId) {
		if (playerId == firstPlayer) {
			addRound();
		}
		playState = PLAY_STATE_WAIT;
		if (this.getTemplet().type == ROOM_TYPE_PVP) {
			startTurnTimer();
		}
		int enemyId = getEnemyId(playerId);
		BattleRole fighter = fighters.get(playerId);
		BattleRole enemy = fighters.get(enemyId);
		fighter.startTrun();
		setRoleState(playerId, BattleRole.START);
		
		FightMsgSend.startMyTurn(this.sessions.get(playerId), true, this.turboModeStart(), true);
		FightMsgSend.startMyTurn(this.sessions.get(enemyId), false, this.turboModeStart(), true);
		fighter.msgBegin();
		enemy.msgBegin();

		// 恢复资源
		replenishedResource(fighter);
		this.resourceSync(fighter);

		areaLvUpChangeDraw(enemy);
		areaLvUpChangeDraw(fighter);
		
		// 唤醒所有区域的部队牌
		awakeTroop(fighter);

		triggerEffect(TriggerManager.MY_TURN, playerId, 1);

		turnStartDrawCard(fighter);
		
		nextStart(playerId, true);
	}
		
	public void nextStart(int playerId, boolean isFirst) {
		int enemyId = getEnemyId(playerId);
		BattleRole fighter = fighters.get(playerId);
		BattleRole enemy = fighters.get(enemyId);
		if (!isFirst) {
			FightMsgSend.startMyTurn(this.sessions.get(playerId), true, this.turboModeStart(), isFirst);
			FightMsgSend.startMyTurn(this.sessions.get(enemyId), false, this.turboModeStart(), isFirst);
			fighter.msgBegin();
			enemy.msgBegin();
		}

		getTriggerManager().start(this, fighter);
		if (fighter.getInterruptSkillArg() != null) {
			this.findCardSync(fighter);
			FightMsgSend.startMyTurnEnd(this.sessions.get(playerId), true, this.turboModeStart(), false);
			FightMsgSend.startMyTurnEnd(this.sessions.get(enemyId), false, this.turboModeStart(), false);
			fighter.msgEnd();
			enemy.msgEnd();
			return;
		}

		this.findCardSync(fighter);
		FightMsgSend.startMyTurnEnd(this.sessions.get(playerId), true, this.turboModeStart(), true);
		FightMsgSend.startMyTurnEnd(this.sessions.get(enemyId), false, this.turboModeStart(), true);
		fighter.msgEnd();
		enemy.msgEnd();
		
		clearRoleState(playerId);
		fighter.clearEffectCard();
		if (fighter.isRobot()) {
			if (this.getTemplet().type == ROOM_TYPE_GUIDE) {
				GuideBattleRole role = (GuideBattleRole)fighter;
				role.turn(this);
			} else if (this.getTemplet().type == ROOM_TYPE_ROBOT) {
				BattleNAIRole role = (BattleNAIRole)fighter;
				role.turn(this);
			}
		}
	}

	public void turnStartDrawCard(BattleRole fighter) {
		ArrayList<CardBase> cards = new ArrayList<>();
		
		// 抽一张卡
		CardBase card = deal(fighter);
		if (card == null) {
			// 罢工标记
			fighter.addStatusCount(BattleRole.TIRET, 1);
			FightMsgSend.tiretSync(this.sessions.get(fighter.getPlayerId()), fighter.getStatusCount(BattleRole.TIRET));
			if (fighter.getStatusCount(BattleRole.TIRET) >= 3) {
				fighter.setHp(0);
				settlement(fighter.getPlayerId());
			}
			return;
		}
		cards.add(card);
		logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。抽取卡牌。", fighter.getPlayerId(), this.roomId, card.getRealId());
		if (isTurboMode()) { // 加速模式多抽一张牌
			card = deal(fighter);
			if (card != null) {
				cards.add(card);
				logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。抽取卡牌。", fighter.getPlayerId(), this.roomId, card.getRealId());
			}
		}
		drawCardAndSync(fighter, cards);
	}
	
	private void gmCreateCard(BattleRole fighter) {
		// GM修改抽取的卡牌
		CardBase createCard = createCard(fighter.getPlayerId(), fighter.getDealCardId(), true);
		if (createCard != null) {
			fighter.getDecks().add(0, createCard);
		}
	}
	
	/**
	 * 回合开始时，增加资源上限，当前资源回复至上限
	 * 
	 * @param fighter
	 */
	public void replenishedResource(BattleRole fighter) {
		int addResourece = 1;
		if (isTurboMode()) {
			addResourece = 2;
		}
		if (!fighter.getStatus(SkillManager.ADD_RES_STOP)) {
			fighter.addReplResource(addResourece);
		}
		fighter.replenishedResource();
	}

	/**
	 * 回合结束 更换当前回合的玩家 通知下回合开始
	 * 
	 * @param playerId
	 */
	public void turnFinish(int playerId) {
		BattleRole fighter = getBattleRole(playerId);
		int enemyId = getEnemyId(playerId);
		BattleRole enemy = getBattleRole(enemyId);
		ISession session = this.sessions.get(playerId);
		ISession enemySession = this.sessions.get(enemyId);
		
		setRoleState(playerId, BattleRole.END);
		
		FightMsgSend.trunFinish(session, true, true);
		FightMsgSend.trunFinish(enemySession, false, true);
		fighter.msgBegin();
		enemy.msgBegin();
		
		// 移除控制的部队
		removeControl(fighter);
		// 移除只在自身回合有效的技能效果
		removeEffect(TriggerManager.MY_TURN, 1);
		removeEffect(TriggerManager.THIS_TURN, 1);
		nextTurnFinish(playerId, true);
	}
	
	public void nextTurnFinish(int playerId, boolean isFirst) {
		BattleRole fighter = getBattleRole(playerId);
		int enemyId = getEnemyId(playerId);
		BattleRole enemy = getBattleRole(enemyId);

		ISession session = this.sessions.get(playerId);
		ISession enemySession = this.sessions.get(enemyId);
		if (!isFirst) {
			FightMsgSend.trunFinish(session, true, isFirst);
			FightMsgSend.trunFinish(enemySession, false, isFirst);
			fighter.msgBegin();
			enemy.msgBegin();
		}
		
		// 回合结束 和 蛊惑
		getTriggerManager().end(this, fighter);
		if (fighter.getInterruptSkillArg() != null) {
			this.findCardSync(fighter);
			FightMsgSend.trunFinishEnd(session, true, false);
			FightMsgSend.trunFinishEnd(enemySession, false, false);
			fighter.msgEnd();
			enemy.msgEnd();
			return;
		}
		
		fighter.setStatusTrun(BattleRole.DECK_CARD_MODIFY_COUNT, 0);
		fighter.setStatusTrun(BattleRole.PLAY_CARD_COUNT, 0);
		
		// 自身场上部队眩晕减1回合
		endTrun(fighters.get(playerId));

		this.findCardSync(fighter);
		FightMsgSend.trunFinishEnd(session, true, true);
		FightMsgSend.trunFinishEnd(enemySession, false, true);
		fighter.msgEnd();
		enemy.msgEnd();

		clearRoleState(playerId);
		fighter.clearEffectCard();
		
		/*
		 *  准备更换当前玩家，开始下一回合
		 */
		if (!fighter.isExtraTurn()) { // 是否有额外的一回合
			nowPlayer = getEnemyId(playerId);
		}
		turnStart(nowPlayer);
	}

	private void endTrun(BattleRole fighter) {
		ArrayList<TroopCard> troops = new ArrayList<>();
		for (Area area : fighter.getAreas()) {
			for (TroopCard troop : area.getTroops()) {
				boolean isStun = troop.isStun();
				troop.endTurn();
				troops.add(troop);
				if (isStun)
					if (!troop.isStun()) {
						SkillManager.getInstance().stunRemove(this, troop, -1);
					} else {
						this.troopStatusSync(troop, TroopCard.STUN);
				}
			}
			for (ArtifactCard artifact : area.getArtifact()) {
				artifact.endTrun();
			}
		}
		this.troopStatusSync(troops, TroopCard.ATTACKED);
		this.troopStatusSync(troops, TroopCard.ENCHANT);
		
		int enemyId = getEnemyId(fighter.getPlayerId());
		BattleRole enemy = getBattleRole(enemyId);
		for (Area area : enemy.getAreas()) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.getStatus(TroopCard.DAMAGED)) {
					troop.setStatus(TroopCard.DAMAGED, false);
					this.troopStatusSync(troop, TroopCard.DAMAGED);
				}
			}
		}
	}
	
	public boolean playCardInterrupt(BattleRole fighter, CardBase playCard) {
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		BattleRole enemy = getBattleRole(enemyId);
		playCardCount(playCard, fighter);
		for (Area area : enemy.getAreas()) {
			for (TrapCard trap : area.getTrap()) {
				if (trap.getStatus(SkillManager.INTERRUPT)) {
					boolean result = TrapTriggerManager.getInstance().interrupt(this, trap, playCard);
					if (result) {
						// 消耗点数
						
						syncStart(playerId);

						int cost = playCard.getCost(fighter);
						fighter.addResource(-cost);
						this.resourceSync(fighter);
						
						playCard.getLogInfo().addTarget(LogDetailInfo.ATTACK, trap.getRealId());
						logSync(playCard.getLogInfo());
						playCard.setLogInfo(null);
						
						syncEnd(playerId);
					}
					return result;
				}
			}
		}
		return false;
	}

	/**
	 * 打出手牌
	 * 
	 * @param playerId
	 * @param type
	 * @param cardUid
	 * @param areaIndex
	 * @return
	 */
	public boolean playCard(int playerId, int type, int cardUid, int areaIndex, int mainRowIndex, SelectObject sobj) {
		switch (type) {
		case CardModel.TROOP:
			return playCardTroop(playerId, cardUid, areaIndex, mainRowIndex, sobj);
		case CardModel.ARTIFACT:
			CardBase cardBase = cards.get(cardUid);
			if (cardBase == null) {
				logger.error("玩家：{}，房间Id：{}。手牌Uid：{}。打出失败，该卡牌不存在。", playerId, roomId, cardUid);
				return false;
			}
			if (cardBase.getType() == CardModel.ARTIFACT) {
				return playCardArti(playerId, cardUid, areaIndex, mainRowIndex);
			} else if (cardBase.getType() == CardModel.TRAP) {
				return playCardSpellTrap(playerId, cardUid, areaIndex, mainRowIndex);
			}
		default:
			return false;
		}
	}
	
	/**
	 * 打出法术牌
	 * 
	 * @param playerId
	 * @param handCardIndex
	 * @param sobj
	 * @return
	 */
	public boolean playCardSpell(int playerId, int cardUid, SelectObject sobj) {
		int enemyId = getEnemyId(playerId);
		BattleRole fighter = fighters.get(playerId);
		ArrayList<CardBase> handCards = fighter.getHandCards();
		boolean attackLimitSpell = fighter.isPlaySpellCard();

		CardBase cardBase = cards.get(cardUid);
		int handCardIndex = handCards.indexOf(cardBase);
		if (cardBase.getStatus(SkillManager.COPY_AND_PLAY)) {
			fighter.getFindCards().remove(0);
			fighter.setInterruptSkillArg(null);
		} else if (handCardIndex == -1) {
			logger.error("玩家：{}，房间Id：{}。手牌Uid：{}。打出失败，该卡牌不在手牌中。", playerId, roomId, cardUid);
			return false;
		}
		if (cardBase.getType() != CardModel.SPELL) {
			logger.error("玩家：{}，房间Id：{}。手牌Id：{}，不是法术牌，打出手牌失败。", playerId, roomId, cardBase.getRealId());
			return false;
		}
		if (cardBase.isNeed3Temples()) {
			if (fighter.getTempleCount() < 3) {
				logger.error("玩家：{}，房间Id：{}。手牌Id：{}，只有控制3个神殿时才能出打此牌，打出手牌失败。", playerId, roomId, cardBase.getRealId());
				return false;
			}
		}
		SpellCard card = (SpellCard) cardBase;
		int cost = card.getCost(fighter);
		if (cost > 0 && fighter.getResource() < cost) {
			logger.error("玩家：{}，房间Id：{}。手牌Id：{}，所需资源：{}，当前资源：{}。打出手牌失败，资源不足。", playerId, roomId, card.getRealId(),
					cost, fighter.getResource());
			return false;
		}
		// 技能目标
		if (!setSpellSkillTarget(sobj, fighter, enemyId, card)) {
			return false;
		}
		if (playCardInterrupt(fighter, card)) { // 打断卡牌，并返手
			return true;
		}
		
		// 消耗点数
		fighter.addResource(-cost);

		// 从手牌中移除
		removeHandCard(fighter, cardBase);	
		fighter.getDiscards().add(card);
		
		setRoleState(playerId, BattleRole.PLAY_CARD);
		
		/*
		 * 同步开始 
		 */
		syncStart(playerId);

		this.resourceSync(fighter);
		
		FightMsgSend.revealCardSync(this.sessions.get(enemyId), card, fighter);
		FightMsgSend.spellCardPlay(this.sessions.get(playerId), true, card, handCardIndex);
		FightMsgSend.spellCardPlay(this.sessions.get(enemyId), false, card, handCardIndex);
		
		playCardEffect(card, null, fighter);
		
		if (!attackLimitSpell && fighter.getAttackLimitSpell().size() > 0) {
			attackLimitSpellSync(fighter);
		}

		if (fighter.getInterruptSkillArg() == null) {
			FightMsgSend.playCardEnd(getSession(playerId), cardBase);
		}
		
		/*
		 * 同步结束 
		 */
		syncEnd(playerId);
		
		clearRoleState(playerId);
		card.getTarget().clear();
		logger.info("玩家：{}，房间Id：{}。打出法术牌：{}，成功。", playerId, roomId, card.getRealId());
		if (cardBase.getStatus(SkillManager.COPY_AND_PLAY)) {
			cardBase.setStatus(SkillManager.COPY_AND_PLAY, false);
			findCardSync(fighter);
		}
		return true;
	}

	/**
	 * 打出神器卡牌
	 * 
	 * @param playerId
	 * @param handCardIndex
	 * @param areaIndex
	 * @return
	 */
	public boolean playCardArti(int playerId, int cardUid, int areaIndex, int mainRowIndex) {

		int enemyId = getEnemyId(playerId);
		BattleRole fighter = fighters.get(playerId);
		
		ArrayList<CardBase> handCards = fighter.getHandCards();
		Area area = fighter.getArea(areaIndex);
		CardBase oldCard = area.getArtiOrTrapByIndex(mainRowIndex);
		if (area.getLevel() <= 0) {
			logger.error("玩家：{}，房间Id：{}，区域：{}。手牌Uid：{}。打出手牌失败，区域为0级。", playerId, roomId, areaIndex, cardUid);
			return false;
		}
		CardBase cardBase = cards.get(cardUid);
		int handCardIndex = handCards.indexOf(cardBase);
		if (cardBase.getStatus(SkillManager.COPY_AND_PLAY)) {
			fighter.getFindCards().remove(0);
			fighter.setInterruptSkillArg(null);
		} else if (handCardIndex == -1) {
			logger.error("玩家：{}，房间Id：{}。手牌Uid：{}。打出失败，该卡牌不在手牌中。", playerId, roomId, cardUid);
			return false;
		}
		if (cardBase.getType() != CardModel.ARTIFACT) {
			logger.error("玩家：{}，房间Id：{}。手牌Id：{}，不是神器牌，打出手牌失败。", playerId, roomId, cardBase.getRealId());
			return false;
		}
		if (cardBase.isNeed3Temples()) {
			if (fighter.getTempleCount() < 3) {
				logger.error("玩家：{}，房间Id：{}。手牌Id：{}，只有控制3个神殿时才能出打此牌，打出手牌失败。", playerId, roomId, cardBase.getRealId());
				return false;
			}
		}
		ArtifactCard card = (ArtifactCard) cardBase;
		int cost = card.getCost(fighter);
		if (cost > 0 && fighter.getResource() < cost) {
			logger.error("玩家：{}，房间Id：{}，区域：{}。手牌Id：{}，所需资源：{}，当前资源：{}。打出手牌失败，资源不足。", playerId, roomId, areaIndex,
					card.getRealId(), cost, fighter.getResource());
			return false;
		}
		if (playCardInterrupt(fighter, card)) { // 打断卡牌，并返手
			return true;
		}
		if (oldCard != null) {
			syncStart(playerId);
			cardKill(oldCard);
			replaceDestoryCard(oldCard.getPlayerId(), oldCard);
			syncEnd(playerId);
			logger.info("玩家：{}，房间Id：{}，区域：{}，销毁卡牌Uid：{}。打出手牌替换场上牌。", playerId, roomId, areaIndex, oldCard.getUid());
		}
		// 消耗资源点数
		fighter.addResource(-cost);
		// 从手牌中移除
		removeHandCard(fighter, cardBase);	
		// 放入区域神器位
		area.addArtiOrTrap(mainRowIndex, card);
		
		setRoleState(playerId, BattleRole.PLAY_CARD);

		/*
		 * 同步开始 
		 */
		syncStart(playerId);
		
		this.resourceSync(fighter);
		
		FightMsgSend.revealCardSync(this.sessions.get(enemyId), card, fighter);
		FightMsgSend.artiCardPlay(this.sessions.get(playerId), true, card, areaIndex);
		FightMsgSend.artiCardPlay(this.sessions.get(enemyId), false, card, areaIndex);
		
		playCardEffect(card, area, fighter);

		logger.info("玩家：{}，房间Id：{}，区域：{}。打出神器牌：{}，成功。", playerId, roomId, areaIndex, card.getRealId());

		
		if (fighter.getInterruptSkillArg() == null) {
			FightMsgSend.playCardEnd(getSession(playerId), cardBase);
		}
		
		/*
		 * 同步结束 
		 */
		syncEnd(playerId);
		
		clearRoleState(playerId);
		if (cardBase.getStatus(SkillManager.COPY_AND_PLAY)) {
			cardBase.setStatus(SkillManager.COPY_AND_PLAY, false);
			findCardSync(fighter);
		}
		return true;
	}

	/**
	 * 打出陷阱牌
	 * 
	 * @param playerId
	 * @param handCardIndex
	 * @param areaIndex
	 * @param sobj
	 * @return
	 */
	public boolean playCardSpellTrap(int playerId, int cardUid, int areaIndex, int mainRowIndex) {

		int enemyId = getEnemyId(playerId);
		BattleRole fighter = fighters.get(playerId);
		ArrayList<CardBase> handCards = fighter.getHandCards();
		Area area = fighter.getArea(areaIndex);
		CardBase trap = area.getArtiOrTrapByIndex(mainRowIndex);
		boolean attackLimitSpell = fighter.isPlaySpellCard();

		if (area.getLevel() <= 0) {
			logger.error("玩家：{}，房间Id：{}，区域：{}。手牌Uid：{}。打出手牌失败，区域为0级。", playerId, roomId, areaIndex, cardUid);
			return false;
		}
		CardBase cardBase = cards.get(cardUid);
		int handCardIndex = handCards.indexOf(cardBase);
		if (cardBase.getStatus(SkillManager.COPY_AND_PLAY)) {
			fighter.getFindCards().remove(0);
			fighter.setInterruptSkillArg(null);
		} else if (handCardIndex == -1) {
			logger.error("玩家：{}，房间Id：{}。手牌Uid：{}。打出失败，该卡牌不在手牌中。", playerId, roomId, cardUid);
			return false;
		}
		if (cardBase.getType() != CardModel.TRAP) {
			logger.error("玩家：{}，房间Id：{}。手牌Id：{}，不是法术-陷阱牌，打出手牌失败。", playerId, roomId, cardBase.getRealId());
			return false;
		}
		if (cardBase.isNeed3Temples()) {
			if (fighter.getTempleCount() < 3) {
				logger.error("玩家：{}，房间Id：{}。手牌Id：{}，只有控制3个神殿时才能出打此牌，打出手牌失败。", playerId, roomId, cardBase.getRealId());
				return false;
			}
		}
		TrapCard card = (TrapCard) cardBase;
		int cost = card.getCost(fighter);
		if (cost > 0 && fighter.getResource() < cost) {
			logger.error("玩家：{}，房间Id：{}，区域：{}。手牌Id：{}，所需资源：{}，当前资源：{}。打出手牌失败，资源不足。", playerId, roomId, areaIndex,
					card.getRealId(), cost, fighter.getResource());
			return false;
		}

		if (playCardInterrupt(fighter, card)) { // 打断卡牌，并返手
			return true;
		}
		if (trap != null) {
			syncStart(playerId);
			cardKill(trap);
			replaceDestoryCard(trap.getPlayerId(), trap);
			syncEnd(playerId);
			logger.info("玩家：{}，房间Id：{}，区域：{}，销毁卡牌Uid：{}。打出手牌替换场上牌。", playerId, roomId, areaIndex, trap.getUid());
		}
		// 消耗点数
		fighter.addResource(-cost);
		// 从手牌中移除
		removeHandCard(fighter, cardBase);	
		// 放入区域陷阱位
		area.addArtiOrTrap(mainRowIndex, card);

		/*
		 * 同步开始 
		 */
		syncStart(playerId);
		
		this.resourceSync(fighter);
		
		FightMsgSend.trapCardPlay(this.sessions.get(playerId), card, card.getRealId(), card.getUid(), areaIndex, card.getMainRowIndex());
		FightMsgSend.trapCardPlay(this.sessions.get(enemyId), null, card.getRealId(), card.getUid(), areaIndex, card.getMainRowIndex());
		
		playCardEffect(card, area, fighter);
		
		logger.info("玩家：{}，房间Id：{}，区域：{}。放置陷阱牌：{}，成功。", playerId, roomId, areaIndex, card.getRealId());
		
		if (!attackLimitSpell && fighter.getAttackLimitSpell().size() > 0) {
			attackLimitSpellSync(fighter);
		}

		if (fighter.getInterruptSkillArg() == null) {
			FightMsgSend.playCardEnd(getSession(playerId), cardBase);
		}
		
		/*
		 * 同步结束 
		 */
		syncEnd(playerId);

		if (cardBase.getStatus(SkillManager.COPY_AND_PLAY)) {
			cardBase.setStatus(SkillManager.COPY_AND_PLAY, false);
			findCardSync(fighter);
		}
		return true;
	}

	/**
	 * 打出部队牌
	 * 
	 * @param playerId
	 * @param cardUid
	 * @param areaIndex
	 * @param sobj
	 * @return
	 */
	public boolean playCardTroop(int playerId, int cardUid, int areaIndex, int mainRowIndex, SelectObject sobj) {

		int enemyId = getEnemyId(playerId);
		BattleRole fighter = fighters.get(playerId);
		
		ArrayList<CardBase> handCards = fighter.getHandCards();
		Area area = fighter.getArea(areaIndex);

		if (area.getLevel() <= 0) {
			logger.error("玩家：{}，房间Id：{}，区域：{}。手牌Uid：{}。打出手牌失败，区域为0级。", playerId, roomId, areaIndex, cardUid);
			return false;
		}
		if (mainRowIndex < 0 || mainRowIndex > Area.ROW_MAX_INDEX) {
			logger.info("玩家：{}，房间Id：{}，打出手牌失败。主行Index：{}，有误。", playerId, roomId, mainRowIndex);
			return false;
		}
		CardBase cardBase = cards.get(cardUid);
		int handCardIndex = handCards.indexOf(cardBase);
		if (cardBase.getStatus(SkillManager.COPY_AND_PLAY)) {
			fighter.getFindCards().remove(0);
			fighter.setInterruptSkillArg(null);
		} else if (handCardIndex == -1) {
			logger.error("玩家：{}，房间Id：{}。手牌Uid：{}。打出失败，该卡牌不在手牌中。", playerId, roomId, cardUid);
			return false;
		}
		if (cardBase.getType() != CardModel.TROOP) {
			logger.error("玩家：{}，房间Id：{}。手牌Id：{}，不是部队牌，打出手牌失败。", playerId, roomId, cardBase.getRealId());
			return false;
		}
		if (cardBase.isNeed3Temples()) {
			if (fighter.getTempleCount() < 3) {
				logger.error("玩家：{}，房间Id：{}。手牌Id：{}，只有控制3个神殿时才能出打此牌，打出手牌失败。", playerId, roomId, cardBase.getRealId());
				return false;
			}
		}
		// 消耗点数
		TroopCard card = (TroopCard) cardBase;
		int cost = card.getCost(fighter);
		if (cost > 0 && fighter.getResource() < cost) {
			logger.error("玩家：{}，房间Id：{}，区域：{}。手牌Id：{}，所需资源：{}，当前资源：{}。打出手牌失败，资源不足。", playerId, roomId, areaIndex,
					card.getRealId(), cost, fighter.getResource());
			return false;
		}

		// 技能目标
		if (!setSpellSkillTarget(sobj, fighter, enemyId, card)) {
			return false;
		}
		if (playCardInterrupt(fighter, card)) { // 打断卡牌，并返手
			return true;
		}
		if (area.getCardByIndex(mainRowIndex) != null) {
			TroopCard temp = (TroopCard)area.getCardByIndex(mainRowIndex);
			temp.setHp(0);
			syncStart(playerId);
			temp.setDeathcry(false);
			cardDeath(temp, 1);
			replaceDestoryCard(temp.getPlayerId(), temp);
			syncEnd(playerId);
			logger.info("玩家：{}，房间Id：{}，区域：{}，销毁卡牌Uid：{}。打出手牌替换场上牌。", playerId, roomId, areaIndex, temp.getUid());
		}
		fighter.addResource(-cost);
		// 从手牌中移除
		removeHandCard(fighter, cardBase);
		// 打出的部队默认是在睡眠中
		card.setSleep(true);
		// 放入区域
		area.addTroop(card);

		setRoleState(playerId, BattleRole.PLAY_CARD);
		
		/*
		 * 同步开始 
		 */
		syncStart(playerId);
		
		this.resourceSync(fighter);
		
		FightMsgSend.revealCardSync(this.sessions.get(enemyId), card, fighter);
		FightMsgSend.troopCardPlay(this.sessions.get(playerId), playerId, card);
		FightMsgSend.troopCardPlay(this.sessions.get(enemyId), enemyId, card);

		playCardEffect(card, area, fighter);
		
		logger.info("玩家：{}，房间Id：{}，区域：{}。打出部队牌：{}，CardUid：{}，成功。", playerId, roomId, areaIndex, card.getRealId(), card.getUid());
		
		attackLimitTroopSync(fighter, area, card);
		
		if (!card.getStatus(TroopCard.ATTACKED)) {
			troopStatusSync(card, TroopCard.ATTACKED);
		}
		playTroopStatusSync(card);

		if (fighter.getInterruptSkillArg() == null) {
			FightMsgSend.playCardEnd(getSession(playerId), cardBase);
		}
		
		/*
		 * 同步结束 
		 */
		syncEnd(playerId);
		
		clearRoleState(playerId);
		card.getTarget().clear();

		if (cardBase.getStatus(SkillManager.COPY_AND_PLAY)) {
			cardBase.setStatus(SkillManager.COPY_AND_PLAY, false);
			findCardSync(fighter);
		}
		return true;
	}
	
	/**
	 * 区域范围光环效果
	 * 
	 * 神器和Leader
	 * 
	 * @param area
	 * @param fighter
	 */
	public void areaSkillEffect(Area area) {
		int playerId = area.getPlayerId();
		BattleRole fighter = this.getBattleRole(playerId);
		for (ArtifactCard artifact: area.getArtifact()) {
			boolean isEffect = skillEffect(artifact, playerId, area, fighter, 1, false);
			if (isEffect) {
				this.artiTriggerSync(artifact);
			}
		}

		for (TroopCard troop : area.getTroops()) {
			if (troop.isStun()) {
				continue;
			}
			for (String id : troop.getLeaderTriggers()) {
				HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(id);
				if (skill == null || skill.size() == 0) {
					continue;
				}
				for (SkillModel model : skill.values()) {
					if (SkillManager.LEADER.equals(model.Genius) || model.Cancel == 1) {
						SkillArg arg = new SkillArg(this, playerId, troop, area, fighter, model, 1);
						SkillManager.getInstance().triggerRegister(arg);
					}
				}
			}
		}
	}

	public boolean setSpellSkillTarget(SelectObject sobj, BattleRole fighter, int enemyId, CardBase card) {
		card.getTarget().clear();
		ArrayList<Integer> target = sobj.getTarget();
		if (target.size() == 0) {
			return true;
		}
		int playerId = fighter.getPlayerId();
		for (int targetId : target) {
			if (targetId < 0) {
				continue;
			}
			if (targetId >= 1000) {
				CardBase targetCard = this.cards.get(targetId);
				if (targetCard == null) {
					logger.error("玩家：{}，房间Id：{}。手牌Id：{}。打出手牌失败，选择的卡牌不存在，CardUid：{}。", playerId, roomId, card.getRealId(), targetId);
					return false;
				}
				if (card.getStatus(TriggerManager.WARCRY) && card.getUid() == targetCard.getUid()) {
					logger.error("玩家：{}，房间Id：{}。手牌Id：{}。打出手牌失败，战吼不能选择自身，CardUid：{}。", playerId, roomId, card.getRealId(), targetId);
					return false;
				}
				card.getTarget().add(targetCard);
				triggerEffect(TriggerManager.TARGET, playerId, targetCard, 1);
			} else if (targetId >= 100) {
				BattleRole role = this.fightersByUid.get(targetId);
				card.getTarget().add(role);
			} else if (targetId >= 0 && targetId <= 2) {
				Area area;
				if (card.getTarget().size() != 0) {
					CardBase targetCard = (CardBase) card.getTarget().get(0);
					area = fighters.get(targetCard.getPlayerId()).getArea(targetId);
				} else {
					area = fighter.getArea(targetId);
				}
				card.getTarget().add(area);
			} else if (targetId >= 3 && targetId <= 5) {
				targetId -= 3;
				Area area = fighters.get(enemyId).getArea(targetId);
				card.getTarget().add(area);
			}
		}
		return true;
	}

	public boolean attack(int playerId, int attCardId, int defCardId) {
		int enemyId = getEnemyId(playerId);
		BattleRole attacker = fighters.get(playerId);
		BattleRole defender = fighters.get(enemyId);
		CardBase attCardBase = cards.get(attCardId);
		CardBase defCardBase = cards.get(defCardId);

		if (attCardBase == null) {
			logger.error("玩家：{}，房间Id：{}，CardUid：{}。攻击失败，卡牌不存在。", playerId, this.roomId, attCardId);
			return false;
		}
		if (fightersByUid.get(defCardId) == null && defCardBase == null) {
			logger.error("敌方玩家：{}，房间Id：{}，CardUid：{}。攻击失败，卡牌不存在。", enemyId, this.roomId, defCardId);
			return false;
		}
		if (fightersByUid.get(defCardId) != null && fightersByUid.get(defCardId).getPlayerId() == playerId) {
			logger.error("玩家：{}，房间Id：{}，CardUid：{}。攻击失败，不能攻击自己。", playerId, this.roomId, defCardId);
			return false;
		}
		if (defCardBase != null && defCardBase.getPlayerId() == playerId) {
			logger.error("玩家：{}，房间Id：{}，CardUid：{}。攻击失败，不能攻击自己的部队。", playerId, this.roomId, defCardId);
			return false;
		}
		
		TroopCard attCard = (TroopCard)attCardBase;
		TroopCard defCard = defCardBase == null ? null : (TroopCard)defCardBase;
		int attAreaIndex = attCard.getAreaIndex();
		
		if (attCard.isRandomTarget()) {
			Object target = getRandomTarget(defender, attCard);
			if (target == null) {
				logger.info("敌方玩家：{}，房间Id：{}，CardUid：{}。攻击失败，没有可攻击的对象。", enemyId, this.roomId, defCardId);
				return false;
			}
			if (target instanceof TroopCard) {
				defCard = (TroopCard)target;
				defCardId = defCard.getUid();
			} else if (target instanceof BattleRole) {
				defCard = null;
				defCardId = defender.getUid();
			}
		}
		boolean isMine = attCard.getPlayerId() == playerId;
		Area attackerArea = isMine ? attacker.getArea(attCardBase.getAreaIndex()) : defender.getArea(attCardBase.getAreaIndex());
		Area defenderArea = defCard == null ? null : defender.getArea(defCard.getAreaIndex());
		boolean isWin = false;

		if (defCard != null && attCard.isHeroAttacker()) {
			logger.error("玩家：{}，房间Id：{}，AttCardId：{}，DefCardId：{}。攻击失败，该卡牌只能攻击英雄。", playerId, this.roomId, attCard.getRealId(), defCard.getRealId());
			FightMsgSend.messageBox(this.sessions.get(playerId), 10001);
			return false;
		}
		if (defCard != null && defCard.isDead()) {
			logger.error("玩家：{}，房间Id：{}，AttCardId：{}，DefCardId：{}。攻击失败，该卡牌已死亡。", playerId, this.roomId, attCard.getRealId(), defCard.getRealId());
			return false;
		}
		if (!isMine) {
			if (!attCard.isControl()) {
				logger.error("玩家：{}，房间Id：{}，CardUid：{}，CardId：{}。攻击失败，该卡牌未被控制。", playerId, this.roomId, attCard.getUid(), attCard.getRealId());
				return false;
			}
		} else {
			
			if (attCard.isSleep()) {
				logger.error("玩家：{}，房间Id：{}，CardUid：{}，CardId：{}。攻击失败，该卡牌在睡眠中。", playerId, this.roomId, attCard.getUid(), attCard.getRealId());
				return false;
			}
			if (!attCard.isAttack(attacker)) {
				logger.error("玩家：{}，房间Id：{}，CardUid：{}，CardId：{}。攻击失败，该卡牌已进行过攻击。", playerId, this.roomId, attCard.getUid(), attCard.getRealId());
				return false;
			}
	
			if (attCard.isAttackLimitTroop()) {
				if (!attackerArea.troopIsFull()) {
					logger.error("玩家：{}，房间Id：{}，CardId：{}，区域Index：{}，区域部队数量：{}。攻击失败，所在区域需要有其他部队才可攻击。", playerId,
							this.roomId, attCard.getRealId(), attCard.getAreaIndex(), attackerArea.getTroops().size());
					return false;
				}
			}
			
			if (attCard.isAttackLimitSpell()) {
				if (!attacker.isPlaySpellCard()) {
					logger.error("玩家：{}，房间Id：{}，CardId：{}。攻击失败，本回合打出过一张法术牌后才可以攻击。", playerId, this.roomId, attCard.getRealId());
					return false;
				}
			}
		
			if (!attCard.isRandomTarget()) {
				int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(attCard.getAreaIndex());
				Area oppoArea = defender.getArea(oppoAreaIndex);
				ArrayList<TroopCard> tempList = oppoArea.getOppCanAttackTroops();
				if (defCard == null) {
					if (!attCard.isAlwaysAttackHero()) {
						for (TroopCard troop : tempList) {
							if (troop.canBeAttack()) {
								logger.error("玩家：{}，房间Id：{}，区域Index：{}。攻击英雄失败，对面区域有部队牌，需优先攻击。", playerId, this.roomId, attCard.getAreaIndex());
								FightMsgSend.messageBox(this.sessions.get(playerId), 10002);
								return false;
							}
						}
					}
				} else {
					if (!defCard.canBeAttack()) {
						logger.error("玩家：{}，房间Id：{}，DefCardId：{}。攻击失败，该部队无法被攻击。", playerId, this.roomId, defCard.getRealId());
						return false;
					}
					if (defCard.getAreaIndex() != oppoAreaIndex) { 
						for (TroopCard troop : tempList) {
							if (troop.canBeAttack()) {
								logger.error("玩家：{}，房间Id：{}，区域Index：{}。攻击失败，对面区域有部队牌，需优先攻击。", playerId, this.roomId, attCard.getAreaIndex());
								FightMsgSend.messageBox(this.sessions.get(playerId), 10004);
								return false;
							}
						}
						if (!defCard.isGuardian() && haveGuardian(defCard.getArea(), attCard)) {
							logger.error("玩家：{}，房间Id：{}。攻击失败，敌方有嘲讽牌，需优先攻击。", playerId, this.roomId);
							FightMsgSend.messageBox(this.sessions.get(playerId), 10005);
							return false;
						}
						if (defCard.isFlight()) {
							logger.error("玩家：{}，房间Id：{}，AttCardId：{}，DefCardId：{}。攻击失败，该卡牌为飞行牌。", playerId, this.roomId, attCard.getRealId(), defCard.getRealId());
							FightMsgSend.messageBox(this.sessions.get(playerId), 10009);
							return false;
						}
					} else {
						if (!defCard.canBeOppAttack()) {
							logger.error("玩家：{}，房间Id：{}，DefCardId：{}。攻击失败，该部队无法被对面区域部队攻击。", playerId, this.roomId, defCard.getRealId());
							return false;
						}
						if (!defCard.isGuardian() && haveGuardian(defenderArea, attCard)) {
							logger.error("玩家：{}，房间Id：{}，区域Index：{}。攻击失败，敌方对面区域有嘲讽牌，需优先攻击。", playerId, this.roomId, attCard.getAreaIndex());
							FightMsgSend.messageBox(this.sessions.get(playerId), 10007);
							return false;
						}
					}
					if (defCard.isMermaidLover() && defenderArea.getTroops().size() > 1) {
						logger.error("玩家：{}，房间Id：{}，区域Index：{}。攻击失败，如果本区域有其他部队的话，无法被攻击。", playerId, this.roomId, attCard.getAreaIndex());
						FightMsgSend.messageBox(this.sessions.get(playerId), 10008);
						return false;
					}
				}
			}
		}
		
		attCard.attack();
		
		LogInfo logInfo = new LogInfo(playerId, LogInfo.ATTACK);
		logInfo.setCardId(attCard.getRealId());
		logInfos.add(logInfo);
		attCard.setLogInfo(logInfo);

		syncStart(playerId);
		setRoleState(playerId, BattleRole.ATTACK);
		
		/*
		 * 攻击前触发
		 */
		attCard.setAttackTarget(defCard == null ? defender : defCard);
		if (defCard == null) {
			defender.addStatusCount(TriggerManager.HERO_BE_ATTACK_COUNT, 1);
		}
		triggerEffect(TriggerManager.ATTACK_BEFORE, attCard.getPlayerId(), attCard, 1);
		triggerEffect(TriggerManager.DAMAGE_BEFORE, attCard.getPlayerId(), attCard, 1);
		
		if (attCard.isAlive() && !attCard.isStun()) {
			
			/*
			 * 陷阱触发
			 */
			for (Area tempArea : defender.getAreas()) {
				for (TrapCard trap : tempArea.getTrap()) {
					if (trap.isAfterAttack() && defCard == null) {
						TrapTriggerManager.getInstance().afterAttack(this, trap, attCard);
					}
				}
			}

			// 是否重击
			int isThump = 0;
			if (attCard.getAttack() >= ConfigData.arguments.get("SUPER_THUMP_VALUE")) {
				isThump = 2;
			} else if (attCard.getAttack() >= ConfigData.arguments.get("THUMP_VALUE")) {
				isThump = 1;
			}
			
			if (defCard == null) { // 攻击英雄
				int damage = attCard.getAttack();
				if (defender.getStatus(TroopCard.DOUBLE_DAMAGE)) {
					damage *= 2;
				}
				defender.addHp(-damage);
				FightMsgSend.attack(this.sessions.get(playerId), attCardId, attCard.getRealHp(), defCardId, defender.getRealHp(), isThump);
				FightMsgSend.attack(this.sessions.get(enemyId), attCardId, attCard.getRealHp(), defCardId, defender.getRealHp(), isThump);
				
				attacker.getQuestManager().addCount(QuestManager.DAMAGE, QuestManager.HERO, attCard.getAttack());
				logInfo.addTarget(LogDetailInfo.ATTACK, "hero" + enemyId, defender.getHp() <= 0 ? LogDetailInfo.DEATH : -attCard.getAttack());
				logger.info("玩家：{}，房间Id：{}, 卡牌：{}，攻击敌方玩家遭成{}点伤害", playerId, this.roomId, attCard.getRealId(), attCard.getAttack());
				
				SkillManager.getInstance().lifeDrain(this, attCard, attacker, attCard.getAttack());
				this.heroHpSync(defender);
				triggerEffect(TriggerManager.MINE_HERO_LIFE, enemyId, 1);
				
				if (attCard.getStatus(TriggerManager.BREACH)) {
					this.triggerManager.breachEffect(this, attCard, attacker, attackerArea);
				}
				if (damage > 0) {
					triggerEffect(TriggerManager.DAMAGE_AFTER, attCard.getPlayerId(), attCard, 1);
				}
			} else { // 攻击部队
				if (defCard.getArea() != null && defCard.isAlive()) {
					ArrayList<TroopCard> aliveTroops = new ArrayList<>();
					attackByCard(attCard, defCard, attacker, defender, aliveTroops);
	
					ArrayList<CardBase> destoryTroops = new ArrayList<>();
					ArrayList<CardBase> deadTroops = new ArrayList<>();
					cardDeath(attCard);
					cardDeath(defCard);
					addDestoryCard(destoryTroops, deadTroops, attCard);
					addDestoryCard(destoryTroops, deadTroops, defCard);
					
					if (attCard.isSplash()) {
						ArrayList<TroopCard> defTroops = new ArrayList<>();
						defTroops.addAll(defenderArea.getTroops());
						for (TroopCard defTroop : defTroops) {
							if (defTroop.getUid() != defCard.getUid()) {
								if (defTroop.isAlive()) {
								}
								splash(attCard, defTroop, attacker, aliveTroops);
								troopSync(playerId, enemyId, defTroop);
								cardDeath(defTroop);
								addDestoryCard(destoryTroops, deadTroops, defTroop);
							}
						}
					}
					removeEffect(TriggerManager.THIS_ATTACK, 1);
					deathcrySync(deadTroops);
					destoryCard(playerId, destoryTroops);
					deathcry(deadTroops);
					
					for (TroopCard tempTroop : aliveTroops) {
						triggerEffect(TriggerManager.DAMAGE_ALIVE, tempTroop.getPlayerId(), tempTroop, 1);
					}
					logInfo.addTarget(LogDetailInfo.ATTACK, defCard.getRealId(), defender.getHp() <= 0 ? LogDetailInfo.DEATH : -attCard.getAttack());
					if (defCard.getStatusCount(TroopCard.LAST_DAMAGE) > 0) {
						triggerEffect(TriggerManager.DAMAGE_AFTER, attCard.getPlayerId(), attCard, 1);
						triggerEffect(TriggerManager.ATTACK_AFTER, attCard.getPlayerId(), attCard, 1);
					}
				} else {
					logInfo.addTarget(LogDetailInfo.ATTACK, defCard.getRealId(), 0);
				}
			}
		} else {
			logInfo.addTarget(LogDetailInfo.ATTACK, defCard == null ? "hero" + enemyId : defCard.getRealId());
		}

		if (defender.getHp() <= 0) {
			isWin = true;
			logger.info("玩家：{}，房间Id：{}，血量：{}，胜利玩家：{}", defender.getPlayerId(), this.roomId, defender.getHp(), playerId);
		}
		
		if (!isMine) {
			attCard.setStatus(TroopCard.CONTROL, false);
			this.troopStatusSync(attCard, TroopCard.CONTROL);
		} else {
			this.troopStatusSync(attCard, TroopCard.ATTACKED);
			this.troopStatusSync(attCard, TroopCard.ENCHANT);
		}

		int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(attAreaIndex);
		Area oppoArea = defender.getArea(oppoAreaIndex);
		for (ArtifactCard artifact : oppoArea.getArtifact()) {
			if (artifact.isAttackLimitOnce()) {
				for (TroopCard troop : attackerArea.getTroops()) {
					if (troop.getUid() == attCard.getUid()) {
						continue;
					}
					if (troop.isAttack(attacker)) {
						troop.attack();
						troop.setStatus(TroopCard.AWAKE_ONLY, true);
					}
					this.troopStatusSync(troop, TroopCard.ATTACKED);
				}
			}
		}

		this.heroHpSync(attacker);
		this.heroHpSync(defender);
		
		syncEnd(playerId);
		
		clearRoleState(playerId);
		
		if (isWin) {
			settlement(enemyId);
		} else {
			settlement(playerId);
		}
		logSync(logInfo);
		attCard.setLogInfo(null);
		
		return true;
	}

	private void splash(TroopCard attCard, TroopCard defCard, BattleRole attacker, ArrayList<TroopCard> aliveTroops) {
		if (this.hit(defCard, attCard, true, false, aliveTroops)) {
			logger.info("玩家：{}，房间Id：{}，攻击卡牌：{}，防御卡牌：{}，造成{}点溅射伤害。", attacker.getPlayerId(), this.roomId, attCard.getRealId(),
					defCard.getRealId(), attCard.getAttack());
		}
	}

	private void attackByCard(TroopCard attCard, TroopCard defCard, BattleRole attacker, BattleRole defender, ArrayList<TroopCard> aliveTroops) {
		SkillManager.getInstance().lifeDrain(this, attCard, attacker, attCard.getAttack());
		SkillManager.getInstance().lifeDrain(this, defCard, defender, defCard.getAttack());
		int attHp = attCard.getRealHp();
		int defHp = defCard.getRealHp();
		
		int excessDamage = 0;
		if (attCard.isExcessDamage()) {
			excessDamage = defCard.getHp() - attCard.getAttack();
		}
		this.hit(attCard, defCard, false, false, aliveTroops);
		boolean isHit = this.hit(defCard, attCard, true, true, aliveTroops);
		
		logger.info("玩家：{}，房间Id：{}，攻击卡牌：{}，防御卡牌：{}，造成{}点伤害，造成{}点反伤。", attacker.getPlayerId(), this.roomId,
				attCard.getRealId(), defCard.getRealId(), defHp - defCard.getRealHp(), attHp - attCard.getRealHp());
		
		if (isHit && excessDamage < 0) {
			if (defender.getStatus(TroopCard.DOUBLE_DAMAGE)) {
				excessDamage *= 2;
			}
			defender.addHp(excessDamage);
			excessDamageSync(attCard.getPlayerId(), defCard.getUid(), -excessDamage);
			if (checkPlayer(attCard.getPlayerId()) && attCard.getStatus(TriggerManager.BREACH)) {
				this.triggerManager.breachEffect(this, attCard, attacker, attacker.getArea(attCard.getAreaIndex()));
			}
			attCard.setAttackTarget(defender);
			triggerEffect(TriggerManager.DAMAGE_AFTER, attCard.getPlayerId(), attCard, 1);
			attCard.setAttackTarget(defCard);
			attacker.getQuestManager().addCount(QuestManager.DAMAGE, QuestManager.HERO, -excessDamage);
			logger.info("玩家：{}，房间Id：{}，攻击卡牌：{}，防御卡牌：{}，造成{}点碾压伤害。", attacker.getPlayerId(), this.roomId,
					attCard.getRealId(), defCard.getRealId(), -excessDamage);
		}
	}

	public void cardDeath(TroopCard card) {
		cardDeath(card, 0);
	}
	
	public void cardDeath(TroopCard card, int destroy) {
		if (card.getHp() <= 0) {
			BattleRole fighter = this.fighters.get(card.getPlayerId());
			Area area = card.getArea();
			
			card.setOldArea(area);
			card.setDead(true);

			triggerEffect(TriggerManager.DEATH, card.getPlayerId(), card, 1);

			SkillManager.getInstance().removeEffect(this, card, fighter, false);

			if (destroy == 1 || !card.isUndead()) {
				if (area != null) {
					area.removeTroop(card);
				}
			}

			fighter.getAttackLimitSpell().remove(card);
			fighter.getDiscards().add(card);

			triggerManager.delTriggerEvent(card);
			triggerEffect(TriggerManager.TROOP_CHANGE, fighter.getPlayerId(), 1);
			SkillManager.getInstance().removeCostEffect(this, card, false);

			attackLimitTroopSync(fighter, area, card);
			
			logger.info("玩家：{}，房间Id：{}, 卡牌：{}，死亡。", fighter.getPlayerId(), this.roomId, card.getRealId());
		}
	}
	
	public void deathcrySync(ArrayList<CardBase> cards) {
		Collections.sort(cards, new CardComparator());
		for (CardBase card : cards) {
			deathcrySync(card);
		}
	}
	
	public void deathcry(ArrayList<CardBase> cards) {
		Collections.sort(cards, new CardComparator());
		for (CardBase card : cards) {
			deathcry(card);
		}
	}
	
	public void deathcrySync(CardBase card) {
		if (card.isDeathcry()) {
			BattleRole fighter = getBattleRole(card.getPlayerId());
			Area area = card.getOldArea();
			triggerManager.deathcryEffectSync(this, card, fighter, area);
		}
	}
	
	public void deathcry(CardBase card) {
		if (card.isDeathcry()) {
			BattleRole fighter = getBattleRole(card.getPlayerId());
			Area area = card.getOldArea();
			triggerManager.deathcryEffect(this, card, fighter, area);
		}
	}

	public void cardKill(CardBase card) {
		BattleRole fighter = this.fighters.get(card.getPlayerId());
		Area area = fighter.getArea(card.getAreaIndex());
		card.setOldArea(area);
		card.setDead(true);
		area.removeArtiOrTrap(card);
		if (card.getType() == CardModel.ARTIFACT) {
			SkillManager.getInstance().removeEffect(this, card, fighter, false);
			SkillManager.getInstance().removeCostEffect(this, card, false);
		} else if (card.getType() == CardModel.TRAP) {
			triggerEffect(TriggerManager.AREA_TRAP, fighter.getPlayerId(), 1);
		}
		triggerManager.delTriggerEvent(card);
		fighter.getDiscards().add(card);
	}
	
	public void attackLimitTroopSync(BattleRole fighter, Area area, TroopCard card) {
		for (TroopCard troop : area.getTroops()) {
			if ((card == null || troop.getUid() != card.getUid()) && troop.isAttackLimitTroop()) {
				troopStatusSync(troop, TroopCard.ATTACKED);
			}
		}
	}

	public boolean drawCard(int playerId) {
		BattleRole fighter = getBattleRole(playerId);
		if (fighter.isAreaLvUpChangeDraw(checkPlayer(playerId)) != 1) {
			return false;
		}
		ArrayList<CardBase> drawCards = new ArrayList<>();
		CardBase card = deal(fighter);
		
		if (card != null) {
			int resource = fighter.getLvUpResource();
			fighter.setDrawCard(true);
			fighter.addResource(-resource);
			syncStart(playerId);
			
			this.resourceSync(fighter);
			drawCards.add(card);
			drawCardAndSync(fighter, drawCards);
			
			areaLvUpChangeDraw(fighter);
			
			syncEnd(playerId);
		}
		return true;
	}
	
	public boolean areaLvUp(int playerId, int index) {
		LogInfo logInfo = new LogInfo(playerId, LogInfo.AREA_LV_UP);
		logInfos.add(logInfo);
		BattleRole fighter = fighters.get(playerId);
		if (fighter.isAreaLvUp()) {
			logger.error("玩家：{}，房间Id：{}，区域升级失败，该回合已进行过区域升级操作。", playerId, this.roomId);
			return false;
		}
		// 进行区域升级、扣减资源
		Area area = fighter.getArea(index);
		if (area.getLevel() >= Area.MAX_LEVEL) {
			logger.error("玩家：{}，房间Id：{}，区域Index：{}，区域升级失败，该区域已达到最高级。", playerId, this.roomId, index);
			return false;
		}
		int resource = fighter.getLvUpResource();
		if (fighter.getResource() < resource) {
			logger.error("玩家：{}，房间Id：{}，区域Index：{}，玩家当前资源：{}，所需资源：{}，区域升级失败，所需资源不足。", playerId, this.roomId, index,
					fighter.getResource(), resource);
			return false;
		}
		if (area.getLevel() < 0) {
			logger.error("玩家：{}，房间Id：{}，区域Index：{}，该区域已经被摧毁，不能升级。", playerId, roomId, area.getIndex(), area.getLevel());
			return false;
		}

		syncStart(playerId);

		fighter.setAreaLvUp(true);
		fighter.addLvUpResource();
		fighter.addResource(-resource);
		boolean result = areaLvUp(area, fighter);
		
		syncEnd(playerId);
		
		logSync(logInfo);
		return result;
	}
	
	public boolean areaLvUp(Area area, BattleRole fighter) {
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);

		if (area.getLevel() < 0) {
			return false;
		}
		
		area.addLevel();
		fighter.setAreaCount();

		FightMsgSend.areaLvUpResult(this.sessions.get(playerId), true, area.getIndex(), area.getLevel(), fighter);
		FightMsgSend.areaLvUpResult(this.sessions.get(enemyId), false, area.getIndex(), area.getLevel(), fighter);
		
		if (area.getLevel() == 1) {
			summon(playerId, area.getIndex(), String.valueOf(100 + area.getRune()), null, null, false);
		}

		fighter.getQuestManager().addCount(QuestManager.AREA_LV_UP, 1);
		triggerEffect(TriggerManager.AREA, playerId, 1);
		triggerEffect(TriggerManager.AREA_LV_UP, playerId, 1);
		triggerEffect(TriggerManager.SELF_AREA_LV_UP, playerId, area, 1);
		if (area.getLevel() == Area.MAX_LEVEL) {
			triggerEffect(TriggerManager.TEMPLE, playerId, 1);
			fighter.getQuestManager().addCount(QuestManager.TEMPLE, 1);
			areaLvUpChangeDraw(fighter);
		}
		
		logger.info("玩家：{}，房间Id：{}，区域Index：{}，升级区域为{}级。", playerId, roomId, area.getIndex(), area.getLevel());
		this.resourceSync(fighter);
		areaLvUpNeedResource(fighter);
		
		return true;
	}

	public boolean isInRoom(int playerId) {
		return fighters.get(playerId) != null;
	}

	public boolean checkPlayer(int playerId) {
		return nowPlayer == playerId;
	}

	public int getEnemyId(int playerId) {
		for (int id : fighters.keySet()) {
			if (id == playerId) {
				continue;
			}
			return id;
		}
		logger.error("玩家：{}，房间Id：{}，获取另一个玩家Id失败。", playerId, this.roomId);
		return -1;
	}

	@Override
	public void gameEnd() {
		this.destroy();
	}

	@Override
	public void settlement(int playerId) {
		BattleRole loser = this.fighters.get(playerId);
		BattleRole winner = this.fighters.get(getEnemyId(playerId));

		if (loser == null || winner == null) {
			return;
		}
		if (loser.getHp() > 0) {
			return;
		}
		if (getState() == ROOM_DESTORY) {
			return;
		}
		try {
			switch (templet.type) {
			case RoomConst.ROOM_TYPE_PVP:
				pvpSettlement(winner, loser);
				break;
			case RoomConst.ROOM_TYPE_GUIDE:
				if (loser.isRobot()) {
					GuideBattleRole role = (GuideBattleRole)loser;
					role.stop(this);
				} else {
					GuideBattleRole role = (GuideBattleRole)winner;
					role.stop(this);
				}
				guideSettlement(winner, loser);
				break;
			case RoomConst.ROOM_TYPE_ROBOT:
				// TODO 机器人结算
				if (loser.isRobot()) {
					BattleNAIRole role = (BattleNAIRole)loser;
					role.stop(this);
				} else {
					BattleNAIRole role = (BattleNAIRole)winner;
					role.stop(this);
				}
				robotSettlement(winner, loser);
				break;
			}
			
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			GameRoomManager.getInstance().destroyRoom(this.getRoomId());
		}
	}
	
	private void pvpSettlement(BattleRole winner, BattleRole loser) {
		winner.getQuestManager().settlement(this, winner, true);
		loser.getQuestManager().settlement(this, loser, false);
		
		ArrayList<Object> winnerResultInfo = GameRoomDao.settlement(true, winner, loser);
		ArrayList<Object> loserResultInfo = GameRoomDao.settlement(false, loser, winner);
		
		pvpSettlementSync(this.sessions.get(winner.getPlayerId()), true, winnerResultInfo, winner);
		pvpSettlementSync(this.sessions.get(loser.getPlayerId()), false, loserResultInfo, loser);
		
		RedisProxy.getInstance().savePlayerGuide(loser.getPlayerId());
		RedisProxy.getInstance().savePlayerGuide(winner.getPlayerId());
	}
	
	private void pvpSettlementSync(ISession session, boolean isWin, ArrayList<Object> result, BattleRole fighter) {
		PlayerInfo player = (PlayerInfo) session.attachment();
		if (!player.isOnline()) {
			return;
		}
		FightMsgSend.pvpSettlement(session, isWin, result, fighter.getRank());
	}
	
	private void guideSettlement(BattleRole winner, BattleRole loser) {
		// TODO 新手引导结算
		if (!winner.isRobot()) {
			RedisProxy.getInstance().savePlayerGuideID(winner.getPlayerId(), this.templet.arg1+1);
			guideSettlementSync(this.sessions.get(winner.getPlayerId()), true);
		} else if (!loser.isRobot()) {
			guideSettlementSync(this.sessions.get(loser.getPlayerId()), false);
		}
	}
	
	private void guideSettlementSync(ISession session, boolean isWin) {
		PlayerInfo player = (PlayerInfo) session.attachment();
		if (!player.isOnline()) {
			return;
		}
		FightMsgSend.guideSettlement(session, player.getPlayerId(), isWin);
	}
	
	private void robotSettlement(BattleRole winner, BattleRole loser) {
		if (winner.isRobot()) {
			loser.getQuestManager().settlement(this, loser, false);
			ArrayList<Object> loserResultInfo = GameRoomDao.settlement(false, loser, winner);
			pvpSettlementSync(this.sessions.get(loser.getPlayerId()), false, loserResultInfo, loser);
			RedisProxy.getInstance().savePlayerGuide(loser.getPlayerId());
		} else {
			winner.getQuestManager().settlement(this, winner, true);
			ArrayList<Object> winnerResultInfo = GameRoomDao.settlement(true, winner, loser);
			pvpSettlementSync(this.sessions.get(winner.getPlayerId()), true, winnerResultInfo, winner);
			RedisProxy.getInstance().savePlayerGuide(winner.getPlayerId());
		}
	}

	@Override
	public void destroy() {
		this.state = ROOM_DESTORY;
		this.players.clear();
		interruptTimer();
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public TempletBase getTemplet() {
		return templet;
	}

	public void setTemplet(TempletBase templet) {
		this.templet = templet;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public void addRound() {
		this.round++;
	}

	public boolean isTurboMode() {
		return getRound() >= this.templet.turboModeRound;
	}

	public boolean turboModeStart() {
		return getRound() == this.templet.turboModeRound;
	}

	@Override
	public void resetPlayer(PlayerInfo player) {

	}

	@Override
	public void resend(PlayerInfo player) {

	}

	@Override
	public void otherPlayerSyncMessage(PlayerInfo player) {

	}

	@Override
	public void exitRoomMessage(PlayerInfo player) {

	}

	@Override
	public void exitRoomSync(int playerId) {

	}

	@Override
	public boolean exitRoomPre(PlayerInfo player, ISession session, int state) {
		return false;
	}

	public boolean skillEffect(CardBase card, int playerId, Area area, BattleRole fighter, int sendType, boolean isPlayCard) {
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
		if (skill == null || skill.size() == 0) {
			return false;
		}
		CardBase target = null;
		boolean isEffect = false;

		for (SkillModel model : skill.values()) {
			if (!isPlayCard && model.Cancel == 0) {
				continue;
			}
			if (!isPlayCard && SkillManager.getInstance().isAutoTrigger(model)) {
				continue;
			}
			SkillArg arg = new SkillArg(this, playerId, card, area, fighter, model, sendType);
			if (card.getType() == CardModel.SPELL && SkillManager.LEADER.equals(model.Genius)) {
				target = (CardBase) card.getTarget().get(0);
				arg.setSelfCard(target);
				arg.setTriggerOwner(target);
				arg.setArea(fighter.getArea(target.getAreaIndex()));
			}
			if (SkillManager.getInstance().triggerRegister(arg)) {
				isEffect = true;
			}
		}
		return isEffect;
	}

	public void trapEffect(CardBase card, int playerId, Area area, BattleRole fighter, int sendType) {
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
		if (skill == null || skill.size() == 0) {
			return;
		}
		for (SkillModel model : skill.values()) {
			TrapTriggerManager.getInstance().effect(this, playerId, card, area, fighter, model, sendType);
		}
	}

	public void triggerEffect(String eventName, int playerId, int sendType) {
		triggerEffect(eventName, playerId, null, sendType);
	}

	public void triggerEffect(String eventName, int playerId, Object trigger, int sendType) {
		
		triggerManager.triggerEffect(this, eventName, playerId, trigger, sendType);
		
		int enemyId = getEnemyId(playerId);
		switch (eventName) {
		case TriggerManager.FIRST:
		case TriggerManager.SECOND:
		case TriggerManager.FIRST_SPELL:
		case TriggerManager.FIRST_TROOP:
		case TriggerManager.TROOP:
		case TriggerManager.AREA_LV_UP:
		case TriggerManager.TROOP_CHANGE:
		case TriggerManager.ADD_TROOP:
		case TriggerManager.DISCARDS:
		case TriggerManager.HAND_CARD_CHANGE:
		case TriggerManager.SPELL:
		case TriggerManager.SPELL_AFTER:
		case TriggerManager.DEATH:
		case TriggerManager.ATTACK_BEFORE:
		case TriggerManager.DAMAGE_BEFORE:
		case TriggerManager.ATTACK_AFTER:
		case TriggerManager.DAMAGE_AFTER:
			triggerManager.triggerEffect(this, eventName, enemyId, trigger, sendType);
		}
	}

	public void removeEffect(String eventName, int sendType) {
		ArrayList<TriggerEvent> triggerEvent = this.triggerManager.getTriggerEvent(eventName);
		if (triggerEvent != null && triggerEvent.size() > 0) {
			for (TriggerEvent event : triggerEvent) {
				if (TriggerManager.MY_TURN.equals(eventName)) {
					if (event.getTriggerCard().getPlayerId() != nowPlayer) {
						continue;
					}
				}
				SkillManager.getInstance().removeEffect(eventName, this, event.getCard(), event.getModel());
				SkillManager.getInstance().removeCostEffect(this, event.getCard(), false);
				if (TriggerManager.THIS_TURN.equals(eventName) || TriggerManager.THIS_ATTACK.equals(eventName)) {
					triggerManager.delTriggerEvent(eventName, event.getCard().getUid());
				}
			}
		}
	}

	public boolean hit(TroopCard defCard, TroopCard atkCard, boolean isAttack, boolean isSend, ArrayList<TroopCard> aliveTroops) {
		int hp = atkCard.getAttack();
		boolean attacked = false;
		try {
			if (hp == 0) {
				return false;
			}
			if (isAttack) {
				if (defCard.isAttackInvincible()) {
					hp = 0;
				}
				if (defCard.isOppAreaAttack()) { // 只有对面区域的攻击者才能对它造成伤害
					int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(defCard.getAreaIndex());
					if (atkCard.getAreaIndex() != oppoAreaIndex) {
						hp = 0;
					}
				}
			} else {
				if (defCard.isDefenderInvincible()) {
					hp = 0;
				}
			}
			
			if (defCard.isInvincible()) { // 无敌
				hp = 0;
			}
			if (hp > 0 && defCard.isForceShield()) {
				defCard.setForceShield(false);
				logger.info("卡牌：{}，发动圣盾。", defCard.getRealId());
				this.troopStatusSync(defCard, TroopCard.FORCE_SHIELD);
				return false;
			}
			defCard.addHp(-hp);
			attacked = true;
		} finally {
			if (isSend) {
				// 是否重击
				int isThump = 0;
				if (hp >= ConfigData.arguments.get("SUPER_THUMP_VALUE")) {
					isThump = 2;
				} else if (hp >= ConfigData.arguments.get("THUMP_VALUE")) {
					isThump = 1;
				}
				FightMsgSend.attack(this.sessions.get(atkCard.getPlayerId()), atkCard.getUid(), atkCard.getRealHp(), defCard.getUid(), defCard.getRealHp(), isThump);
				FightMsgSend.attack(this.sessions.get(defCard.getPlayerId()), atkCard.getUid(), atkCard.getRealHp(), defCard.getUid(), defCard.getRealHp(), isThump);
			}
		}
		if (attacked) {
			defCard.setStatusTrun(TroopCard.LAST_DAMAGE, hp);
			if (defCard.isAlive()) {
				if (hp > 0) {
					troopDamagedSync(defCard);
					aliveTroops.add(defCard);
				}
			}
			if (hp > 0) {
				BattleRole role = this.getBattleRole(atkCard.getPlayerId());
				role.getQuestManager().addCount(QuestManager.DAMAGE, QuestManager.TROOP, hp);
				return true;
			}
		}
		return false;
	}

	public void deckCardNumberSync(BattleRole fighter) {
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		int number = fighter.getDecks().size();
		FightMsgSend.deckCardNumberSync(this.sessions.get(playerId), true, number);
		FightMsgSend.deckCardNumberSync(this.sessions.get(enemyId), false, number);
	}
	
	public void troopDamagedSync(TroopCard troop) {
		if (checkPlayer(troop.getPlayerId())) {
			return;
		}
		troop.setStatus(TroopCard.DAMAGED, true);
		troopStatusSync(troop, TroopCard.DAMAGED);
	}

	public void troopStatusSync(ArrayList<TroopCard> troops, String status) {
		if (troops.size() == 0) {
			return;
		}
		for (TroopCard troop : troops) {
			troopStatusSync(troop, status);
		}
	}

	public void roleStatusSync(String status, BattleRole role) {
		switch (status) {
		case BattleRole.AMPLIFY:
			roleStatusSync(role, BattleRole.AMPLIFY);
			break;

		case SkillManager.HERO_CANT_BE_ATTACK:
			roleCanBeAttack(role);
			break;

		case SkillManager.AREA_LV_UP_CHANGE_DRAW:
			areaLvUpChangeDraw(role);
			break;
		}
	}
	
	public void copyAndPlay(CardBase card) {
		int playerId = card.getPlayerId();
		BattleRole fighter = getBattleRole(playerId);
		FightMsgSend.copyAndPlay(this.sessions.get(playerId), card, fighter);
	}

	public void transform(int playerId, TroopCard from, TroopCard to) {
		int enemyId = getEnemyId(playerId);
		FightMsgSend.transform(this.sessions.get(playerId), playerId, from, to);
		FightMsgSend.transform(this.sessions.get(enemyId), enemyId, from, to);
		playTroopStatusSync(to);
	}

	public void transform(BattleRole fighter, ArrayList<CardBase> from, ArrayList<CardBase> to) {
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.changeHandCards(this.sessions.get(playerId), playerId, fighter, from, to);
		FightMsgSend.changeHandCards(this.sessions.get(enemyId), enemyId, fighter, from, to);
	}
	
	public void summonSync(int playerId, CardBase card) {
		if (card == null) {
			return;
		}
		int enemyId = getEnemyId(playerId);
		switch (card.getType()) {
		case CardModel.TROOP:
			TroopCard troop = (TroopCard) card;
			FightMsgSend.summonTroop(this.sessions.get(playerId), playerId, troop);
			FightMsgSend.summonTroop(this.sessions.get(enemyId), enemyId, troop);
			FightMsgSend.cardCostSync(this.sessions.get(playerId), troop, getBattleRole(troop.getPlayerId()), false);
			FightMsgSend.cardCostSync(this.sessions.get(enemyId), troop, getBattleRole(troop.getPlayerId()), false);
			playTroopStatusSync(troop);
			break;

		case CardModel.ARTIFACT:
			ArtifactCard artifact = (ArtifactCard) card;
			FightMsgSend.summonArtifact(this.sessions.get(playerId), playerId, artifact);
			FightMsgSend.summonArtifact(this.sessions.get(enemyId), enemyId, artifact);
			break;

		case CardModel.TRAP:
			TrapCard trap = (TrapCard) card;
			FightMsgSend.summonTrap(this.sessions.get(playerId), playerId, trap);
			FightMsgSend.summonTrap(this.sessions.get(enemyId), enemyId, trap);
			break;
		}
	}

	public void summonHandCardSync(int playerId, CardBase card) {
		if (card == null) {
			return;
		}
		int enemyId = getEnemyId(playerId);
		switch (card.getType()) {
		case CardModel.TROOP:
			TroopCard troop = (TroopCard) card;
			FightMsgSend.summonHandCardToPlay(this.sessions.get(playerId), playerId, troop);
			FightMsgSend.summonHandCardToPlay(this.sessions.get(enemyId), enemyId, troop);
			FightMsgSend.cardCostSync(this.sessions.get(playerId), troop, getBattleRole(troop.getPlayerId()), false);
			FightMsgSend.cardCostSync(this.sessions.get(enemyId), troop, getBattleRole(troop.getPlayerId()), false);
			playTroopStatusSync(troop);
			break;
		}
	}
	
	public void playTroopStatusSync(TroopCard troop) {
		troopStatusTrueSync(troop, TroopCard.SLEEP);
		troopStatusTrueSync(troop, TroopCard.FLIGHT);
		troopStatusTrueSync(troop, TroopCard.FORCE_SHIELD);
		troopStatusTrueSync(troop, TroopCard.SPELL_BLOCK);
		troopStatusTrueSync(troop, TroopCard.LIFEDRAIN);
		troopStatusTrueSync(troop, TroopCard.SPEED);
		troopStatusTrueSync(troop, TroopCard.GUARDIAN);
		troopStatusTrueSync(troop, TroopCard.ATTACKED);
		troopStatusTrueSync(troop, TroopCard.INVINCIBLE);
	}
	
	private void troopStatusTrueSync(TroopCard troop, String status) {
		if (troop.isAlive() && troop.getStatus(status)) {
			troopStatusSync(troop, status);
			if (TroopCard.SPEED.equals(status)) {
				troopStatusSync(troop, TroopCard.SLEEP);
				troopStatusSync(troop, TroopCard.ATTACKED);
				troopStatusSync(troop, TroopCard.ENCHANT);
			}
		}
	}
	
	public void stunSync(TroopCard card) {
		Iterator<Entry<String, Integer>> iterator = card.getStatus().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Integer> next = iterator.next();
			String status = next.getKey();
			switch (status) {
			case TroopCard.GUARDIAN:
			case TroopCard.FLIGHT:
			case TroopCard.LIFEDRAIN:
			case TroopCard.FORCE_SHIELD:
			case TroopCard.SPELL_BLOCK:
			case TroopCard.AVOID_ATTACKED:
			case TroopCard.AVOID_OPP_ATTACKED:
			case TroopCard.ALWAYS_ATTACK_HERO:
			case BattleRole.AMPLIFY:
				if (next.getValue() > 0) {
					this.troopStatusSync(card, status);
				}
				break;
			case TroopCard.SPEED:
				this.troopStatusSync(card, TroopCard.SLEEP);
				this.troopStatusSync(card, TroopCard.SPEED);
				this.troopStatusSync(card, TroopCard.ATTACKED);
				this.troopStatusSync(card, TroopCard.ENCHANT);
				break;
			}
		}
		this.troopStatusSync(card, TroopCard.STUN);
	}
	
	public void troopStatusSync(TroopCard troop, String status) {
		int playerId = troop.getPlayerId();
		int enemyId = getEnemyId(playerId);
		BattleRole fighter = this.fighters.get(playerId);
		FightMsgSend.troopStatusSync(this.sessions.get(playerId), status, playerId, troop, fighter);
		FightMsgSend.troopStatusSync(this.sessions.get(enemyId), status, enemyId, troop, fighter);
		troop.setChange(false);
	}

	public void troopMoveSync(TroopCard troop, Area area, int oldAreaIndex, int oldMainIndex) {
		int playerId = troop.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.moveSync(this.sessions.get(playerId), playerId, troop, area.getPlayerId() == playerId, oldAreaIndex, oldMainIndex);
		FightMsgSend.moveSync(this.sessions.get(enemyId), enemyId, troop, area.getPlayerId() == enemyId, oldAreaIndex, oldMainIndex);
	}

	public void troopSync(int playerId, ArrayList<TroopCard> troops) {
		if (troops.size() == 0) {
			return;
		}
		int enemyId = getEnemyId(playerId);
		for (TroopCard troop : troops) {
			if (troop.isAttrChange()) {
				troopSync(playerId, enemyId, troop);
				troop.setAttrChange(false);
				cardDeath(troop);
				checkDead(troop);
			}
		}
	}

	public void troopSync(int playerId, int enemyId, TroopCard troop) {
		FightMsgSend.troopSync(this.sessions.get(playerId), troop);
		FightMsgSend.troopSync(this.sessions.get(enemyId), troop);
	}
	
	public void heroHpSync(BattleRole fighter) {
		int enemyId = getEnemyId(fighter.getPlayerId());
		FightMsgSend.heroHpSync(this.sessions.get(fighter.getPlayerId()), fighter.getPlayerId(), fighter);
		FightMsgSend.heroHpSync(this.sessions.get(enemyId), enemyId, fighter);
	}

	public void destoryCard(int playerId, ArrayList<CardBase> cards) {
		if (cards == null || cards.size() == 0) {
			return;
		}
		int enemyId = getEnemyId(playerId);
		FightMsgSend.destoryCard(this.sessions.get(playerId), cards);
		FightMsgSend.destoryCard(this.sessions.get(enemyId), cards);
	}

	public void destoryCard(int playerId, CardBase card) {
		int enemyId = getEnemyId(playerId);
		FightMsgSend.destoryCard(this.sessions.get(playerId), card, false);
		FightMsgSend.destoryCard(this.sessions.get(enemyId), card, false);
	}

	public void replaceDestoryCard(int playerId, CardBase card) {
		int enemyId = getEnemyId(playerId);
		FightMsgSend.destoryCard(this.sessions.get(playerId), card, false);
		FightMsgSend.destoryCard(this.sessions.get(enemyId), card, false);
	}

	public void destoryArea(int playerId, Area area) {
		int enemyId = getEnemyId(playerId);
		FightMsgSend.destoryArea(this.sessions.get(playerId), playerId, area);
		FightMsgSend.destoryArea(this.sessions.get(enemyId), enemyId, area);
	}

	public void troopDeathcrySync(CardBase card) {
		int playerId = card.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.troopDeathcrySync(this.sessions.get(playerId), card);
		FightMsgSend.troopDeathcrySync(this.sessions.get(enemyId), card);
	}

	public void troopEnchantSync(TroopCard card) {
		int playerId = card.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.troopEnchantSync(this.sessions.get(playerId), card);
		FightMsgSend.troopEnchantSync(this.sessions.get(enemyId), card);
	}

	public void startSync(TroopCard card) {
		int playerId = card.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.startSync(this.sessions.get(playerId), card);
		FightMsgSend.startSync(this.sessions.get(enemyId), card);
	}

	public void warcrySync(CardBase card) {
		int playerId = card.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.warcrySync(this.sessions.get(playerId), card);
		FightMsgSend.warcrySync(this.sessions.get(enemyId), card);
	}

	public void reduceHpSync(int playerId, int attUid, int defUid, int reduceHp) {
		int enemyId = getEnemyId(playerId);
		FightMsgSend.reduceHpSync(this.sessions.get(playerId), attUid, defUid, reduceHp);
		FightMsgSend.reduceHpSync(this.sessions.get(enemyId), attUid, defUid, reduceHp);
	}

	public void findCardSync(BattleRole fighter) {
		if (playState == PLAY_STATE_AUTO) {
			return;
		}
		if (fighter.getStatus(BattleRole.CHECK)) {
			return;
		}
		if (fighter.getFindCards().size() == 0) {
			return;
		}
		FindCard findCard = fighter.getFindCards().get(0);
		if (findCard.isSend()) {
			logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。Find信息已发送，不能重复发送。", fighter.getPlayerId(), this.roomId, findCard.getCardId());
			return;
		}
		findCard.setSend(true);
		switch (findCard.getType()) {
		case FindCard.FIND:
			findCardSync(findCard, fighter);
			logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。Find信息发送。", fighter.getPlayerId(), this.roomId, findCard.getCardId());
			break;

		case FindCard.CONSTRUCT_TRAP:
			constructTrap(fighter.getPlayerId(), findCard, fighter);
			logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。组装陷阱信息发送。", fighter.getPlayerId(), this.roomId, findCard.getCardId());
			break;
			
		case FindCard.SUMMON:
			FightMsgSend.summonSelect(this.sessions.get(fighter.getPlayerId()), findCard.getAreaIndex());
			logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。召唤替换信息发送。", fighter.getPlayerId(), this.roomId, findCard.getCardId());
			break;
			
		case FindCard.REVEAL:
			findCardSync(findCard, fighter);
			logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。揭示信息发送。", fighter.getPlayerId(), this.roomId, findCard.getCardId());
			break;
			
		case FindCard.SUMMON_COPY:
			CardBase cardBase = findCard.getCards().remove(0);
			copyAndPlay(cardBase);
			logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。复制打出信息发送。", fighter.getPlayerId(), this.roomId, findCard.getCardId());
			break;
		}
	}
	
	public void findCardSync(FindCard findCard, BattleRole fighter) {
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.findCardSync(this.sessions.get(playerId), playerId, findCard, fighter);
		FightMsgSend.findCardSync(this.sessions.get(enemyId), enemyId, findCard, fighter);
	}
	
	public void constructTrap(int playerId, FindCard findCard, BattleRole fighter) {
		int enemyId = getEnemyId(playerId);
		FightMsgSend.constructTrap(this.sessions.get(playerId), playerId, findCard, fighter);
		FightMsgSend.constructTrap(this.sessions.get(enemyId), enemyId, findCard, fighter);
	}
	
	public void checkCardSync(BattleRole fighter, CardBase card) {
		FightMsgSend.cardCheckSync(this.sessions.get(fighter.getPlayerId()), card, fighter);
	}
	
	public void roleStatusSync(BattleRole fighter, String strType) {
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		int type = BattleRole.getStatusType(strType);
		if (type < 0) {
			return;
		}
		FightMsgSend.roleStatusSync(this.sessions.get(playerId), true, type, fighter.getStatusCount(strType));
		FightMsgSend.roleStatusSync(this.sessions.get(enemyId), false, type, fighter.getStatusCount(strType));
	}
	
	public void roleCanBeAttack(BattleRole fighter) {
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.roleCanBeAttack(this.sessions.get(playerId), fighter);
		FightMsgSend.roleCanBeAttack(this.sessions.get(enemyId), fighter);
	}
	
	public void areaLvUpChangeDraw(BattleRole fighter) {
		int value = fighter.isAreaLvUpChangeDraw(checkPlayer(fighter.getPlayerId()));
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.areaLvUpChangeDraw(this.sessions.get(playerId), true, value);
		FightMsgSend.areaLvUpChangeDraw(this.sessions.get(enemyId), false, value);
	}
	
	public ArrayList<CardBase> drawCardAndSync(BattleRole fighter, ArrayList<CardBase> drawCards) {
		ArrayList<CardBase> cards = new ArrayList<>();
		if (fighter.getStatus(BattleRole.CHECK)) {
			return cards;
		}
		if (fighter.getFindCards().size() != 0) {
			logger.error("=====================find未结束=======================");
		}
		if (drawCards.size() == 0) {
			return cards;
		}
		int count = 0;
		for (CardBase card : drawCards) {
			CardBase drawCard = card;
			if (card.isDraw()) { // 抽取
				drawCard = deal(fighter);
			} else if (card.getDrawCostNumber() != null) { // 抽取并减耗
				Integer cost = card.getDrawCostNumber();
				drawCard = deal(fighter);
				drawCard.addCost(cost);
			} else if (card.isDrawCostBySubType()) { // 抽取并减耗
				drawCard = deal(fighter);
				Integer cost = card.getDrawCostNumberBySubType(drawCard.getSubType());
				if (cost != null) {
					drawCard.addCost(cost);
				}
			}
			if (drawCard != null) {
				if (card.getLogInfo() != null) {
					card.getLogInfo().addTarget(LogDetailInfo.DRAW, drawCard.getRealId());
				}
				cards.add(drawCard);
				if (drawCard.getCost(fighter) >= 7) {
					count++;
				}
			}
		}
		
		addHandCards(fighter, cards);
		drawCardSync(fighter, cards);

		for (int i = 0; i < count; i++) {
			triggerEffect(TriggerManager.COST_UP_7, fighter.getPlayerId(), 1);
		}
		return cards;
	}

	public void drawCardSync(BattleRole fighter, ArrayList<CardBase> cards) {
		if (cards.size() == 0) {
			return;
		}
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.cardSync(this.sessions.get(playerId), cards, fighter);
		FightMsgSend.enemyCardSync(this.sessions.get(enemyId), cards, fighter);
		for (CardBase card : cards) {
			if (card.getType() == CardModel.TROOP) {
				TroopCard troop = (TroopCard) card;
				if (troop.isChange()) {
					this.handcardAttrSync(troop);
				}
			}
		}
	}

	public void discardSync(int playerId, ArrayList<CardBase> cards) {
		if (cards.size() == 0) {
			return;
		}
		int enemyId = getEnemyId(playerId);
		FightMsgSend.discardSync(this.sessions.get(playerId), true, cards);
		FightMsgSend.discardSync(this.sessions.get(enemyId), false, cards);
	}

	public ArrayList<TroopCard> getTroopsByPlayerId(int playerId) {
		ArrayList<TroopCard> result = new ArrayList<>();
		BattleRole enemy = this.fighters.get(playerId);
		for (Area area : enemy.getAreas()) {
			result.addAll(area.getTroops());
		}
		return result;
	}

	public ArrayList<TroopCard> getTroopsByPlayerId(int playerId, CardBase selfCard, SkillModel model) {
		ArrayList<TroopCard> result = new ArrayList<>();
		BattleRole enemy = this.fighters.get(playerId);
		for (Area area : enemy.getAreas()) {
			result.addAll(area.getTroops());
		}
		if (TriggerManager.WARCRY.equals(model.Trigger)) {
			result.remove(selfCard);
		}
		return result;
	}

	public int getTroopCountBySubType(int playerId, String subtype) {
		BattleRole role = this.fighters.get(playerId);
		int count = 0;
		for (Area area : role.getAreas()) {
			for (TroopCard troop : area.getTroops()) {
				if (subtype.equals(troop.getSubType())) {
					count++;
				}
			}
		}
		return count;
	}
	
	public ArrayList<CardBase> getCardsByPlayerId(int playerId) {
		ArrayList<CardBase> result = new ArrayList<>();
		BattleRole enemy = this.fighters.get(playerId);
		for (Area area : enemy.getAreas()) {
			result.addAll(area.getTroops());
			result.addAll(area.getArtiTraps());
		}
		return result;
	}
	
	public void resourceSync(BattleRole fighter) {
		FightMsgSend.resourceSync(this.sessions.get(fighter.getPlayerId()), true, fighter.getResource(), fighter.getReplResource());
		FightMsgSend.resourceSync(this.sessions.get(getEnemyId(fighter.getPlayerId())), false, fighter.getResource(), fighter.getReplResource());
	}
	
	public CardBase summon(int playerId, int areaIndex, String cardId, TroopCard copy, Integer mainRowIndex, boolean isSummon) {
		BattleRole fighter = this.fighters.get(playerId);
		Area area = fighter.getArea(areaIndex);
		if (area.getLevel() <= 0) {
			return null;
		}
		cardId = SkillManager.getInstance().getRandowCardId(cardId);
		CardBase card = createCard(playerId, cardId);
		if (card == null) {
			return null;
		}
		return summon(playerId, fighter, area, card, copy, mainRowIndex, isSummon, true);
	}
	
	public void summonReplaceCard(BattleRole fighter, int cardUid) {
		int playerId = fighter.getPlayerId();
		FindCard findCard = fighter.getFindCards().remove(0);
		CardBase cardBase = findCard.getCards().get(0);
		Area area = fighter.getArea(findCard.getAreaIndex());
		for (TroopCard temp : area.getTroops()) {
			if (temp.getUid() == cardUid) {
				int mainRowIndex = temp.getMainRowIndex();
				temp.setHp(0);
				temp.setDeathcry(false);
				cardDeath(temp, 1);
				replaceDestoryCard(temp.getPlayerId(), temp);
				CardBase summonCard = summon(playerId, fighter, area, cardBase, null, mainRowIndex, false, true);
				if (summonCard != null) {
					syncEnd(playerId);
					logger.info("玩家：{}，房间Id：{}，区域：{}，销毁卡牌Uid：{}。召唤替换场上牌。", fighter.getPlayerId(), roomId, mainRowIndex, temp.getUid());
				}
				break;
			}
		}
		
		syncStart(playerId);
		this.findCardSync(fighter);
		syncEnd(playerId);
		if (fighter.getFindCards().size() == 0) {
			fighter.setInterruptSkillArg(null);
			switch (fighter.getState()) {
			case BattleRole.START:
				nextStart(playerId, false);
				break;
			case BattleRole.END:
				nextTurnFinish(playerId, false);
				break;
			}
		}
	}
	
	private void summonReplaceCard(BattleRole fighter, Area area, CardBase card) {
		FindCard findCard = new FindCard();
		findCard.setCardId(card.getRealId());
		findCard.setCardUid(card.getUid());
		findCard.setType(FindCard.SUMMON);
		findCard.addCards(card);
		findCard.setAreaIndex(area.getIndex());
		if (fighter.getFindCards().size() == 0) {
			findCard.setFirst(true);
		}
		fighter.getFindCards().add(findCard);
		findCardSync(fighter);
		logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。Summon信息存储。", fighter.getPlayerId(), this.getRoomId(), findCard.getCardId());
		if (fighter.isState()) {
			fighter.setInterruptSkillArg(new SkillArg());
		}
	}
	
	public CardBase summon(int playerId, BattleRole fighter, Area area, CardBase card, TroopCard copy, Integer mainRowIndex, boolean isSummon, boolean isSend) {
		switch (card.getType()) {
		case CardModel.TROOP:
			TroopCard troop = (TroopCard) card;
			if (area.troopIsFull()) {
				if (isSummon) {
					summonReplaceCard(fighter, area, card);
					return null;
				}
				return null;
			}
			if (copy != null) {
				troop.copy(copy);
			}
			if (mainRowIndex == null) {
				area.addTroop(troop);
			} else {
				area.addTroop(troop);
			}
			
			if (isSend) {
				if (fighter.getHandCards().indexOf(card) != -1) {
					summonHandCardSync(playerId, card);
				} else {
					summonSync(playerId, card);
				}
			}
			
			skillEffect(card, playerId, area, fighter, 1, true);

			for (ArtifactCard artifact : area.getArtifact()) {
				skillEffect(artifact, playerId, area, fighter, 1, false);
			}

			for (TroopCard t : area.getTroops()) {
				if (t.getUid() != troop.getUid() && t.isLeader()) {
					skillEffect(t, playerId, area, fighter, 1, false);
				}
			}
			triggerEffect(TriggerManager.TROOP_CHANGE, fighter.getPlayerId(), 1);
			triggerEffect(TriggerManager.AREA_TROOP, playerId, troop, 0);
			triggerEffect(TriggerManager.ADD_TROOP, playerId, card, 1);
			
			return troop;
			
		case CardModel.ARTIFACT:
			ArtifactCard artifact = (ArtifactCard) card;
			if (area.getLevel() != Area.MAX_LEVEL) {
				return null;
			}
			if (area.artiOrTrapIsFull()) {
				return null;
			}
			area.setArtifact(artifact);
			
			if (isSend) {
				summonSync(playerId, card);
			}
			
			skillEffect(card, playerId, area, fighter, 1, true);

			return artifact;
			
		case CardModel.TRAP:
			TrapCard trap = (TrapCard) card;
			if (area.getLevel() != Area.MAX_LEVEL) {
				return null;
			}
			if (area.artiOrTrapIsFull()) {
				return null;
			}
			area.setTrap(trap);
			
			if (isSend) {
				summonSync(playerId, card);
			}
			
			trapEffect(card, playerId, area, fighter, 0);
			
			return trap;
		}
		return null;
	}

	public CardBase change(int playerId, int areaIndex, String cardId, TroopCard oldCard) {
		CardBase card = createCard(playerId, cardId);
		if (card == null) {
			return null;
		}
		if (card.getType() != CardModel.TROOP) {
			return null;
		}
		return change(playerId, areaIndex, card, oldCard);
	}
	
	public CardBase change(int playerId, int areaIndex, CardBase card, TroopCard oldCard) {
		BattleRole fighter = this.fighters.get(playerId);
		Area area = oldCard.getArea();
		if (area.getLevel() <= 0) {
			return null;
		}
		
		int mainRowIndex = oldCard.getMainRowIndex();
		oldCard.setHp(0);
		oldCard.setDeathcry(false);
		cardDeath(oldCard, 1);
		
		TroopCard troop = (TroopCard) card;
		area.addTroop(mainRowIndex, troop);
		
		transform(playerId, oldCard, troop);
		
		skillEffect(card, playerId, area, fighter, 1, true);

		for (ArtifactCard artifact: area.getArtifact()) {
			skillEffect(artifact, playerId, area, fighter, 1, false);
		}

		for (TroopCard t : area.getTroops()) {
			if (t.getUid() != troop.getUid() && t.isLeader()) {
				skillEffect(t, playerId, area, fighter, 1, false);
			}
		}
		triggerEffect(TriggerManager.TROOP_CHANGE, fighter.getPlayerId(), 1);
		triggerEffect(TriggerManager.AREA_TROOP, playerId, troop, 1);
		triggerEffect(TriggerManager.ADD_TROOP, playerId, card, 1);
		
		return troop;
	}
	
	private void attackLimitSpellSync(BattleRole fighter) {
		for (Area area : fighter.getAreas()) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.isAttackLimitSpell()) {
					troopStatusSync(troop, TroopCard.ATTACKED);
				}
			}
		}
	}

	public void costSync(CardBase selfCard, ArrayList<CardBase> cards, String genius, boolean show) {
		if (cards.size() == 0) {
			return;
		}
		for (CardBase card : cards) {
			if (selfCard != null && card.getUid() == selfCard.getUid()) {
				continue;
			}
			if (Tools.isEmptyString(genius) || genius.equals(card.getGenius())) {
				FightMsgSend.cardCostSync(this.sessions.get(card.getPlayerId()), card, getBattleRole(card.getPlayerId()), show);
				if (card.getArea() != null) {
					FightMsgSend.cardCostSync(this.sessions.get(getEnemyId(card.getPlayerId())), card, getBattleRole(card.getPlayerId()), show);
				}
			}
		}
	}

	public void handcardAttrSync(BattleRole fighter, ArrayList<TroopCard> cards) {
		if (cards.size() == 0) {
			return;
		}
		int playerId = fighter.getPlayerId();
		for (TroopCard card : cards) {
			FightMsgSend.handcardAttrSync(this.sessions.get(playerId), card);
		}
	}

	public void handCardToDeck(CardBase card) {
		int enemyId = getEnemyId(card.getPlayerId());
		FightMsgSend.handCardToDeck(this.sessions.get(card.getPlayerId()), card.getPlayerId(), card);
		FightMsgSend.handCardToDeck(this.sessions.get(enemyId), enemyId, card);
	}

	public void handcardAttrSync(TroopCard card) {
		FightMsgSend.handcardAttrSync(this.sessions.get(card.getPlayerId()), card);
	}
	
	public void artiTriggerSync(CardBase card) {
		FightMsgSend.artiTriggerSync(this.sessions.get(card.getPlayerId()), card.getUid());
		FightMsgSend.artiTriggerSync(this.sessions.get(getEnemyId(card.getPlayerId())), card.getUid());
	}
	
	public void trapTriggerSync(CardBase card) {
		FightMsgSend.trapTriggerSync(this.sessions.get(card.getPlayerId()), card.getUid());
		FightMsgSend.trapTriggerSync(this.sessions.get(getEnemyId(card.getPlayerId())), card.getUid());
	}
	
	public void lifedrainSync(CardBase card) {
		FightMsgSend.lifedrainSync(this.sessions.get(card.getPlayerId()), card.getUid());
		FightMsgSend.lifedrainSync(this.sessions.get(getEnemyId(card.getPlayerId())), card.getUid());
	}
	
	public void logSync(LogInfo logInfo) {
		for (BattleRole role : this.fighters.values()) {
			int playerId = role.getPlayerId();
			ISession session = this.sessions.get(playerId);
			FightMsgSend.logSync(session, playerId, logInfo);
		}
	}
	
	public void readySync(int playerId) {
		FightMsgSend.readySync(this.sessions.get(playerId), playerId);
		FightMsgSend.readySync(this.sessions.get(getEnemyId(playerId)), playerId);
	}
	
	public void syncStart(int playerId) {
		BattleRole role = getBattleRole(playerId);
		BattleRole enemy = getBattleRole(getEnemyId(playerId));
		role.msgBegin();
		enemy.msgBegin();
		FightMsgSend.syncStart(this.sessions.get(playerId));
		FightMsgSend.syncStart(this.sessions.get(enemy.getPlayerId()));
	}
	
	public void syncEnd(int playerId) {
		BattleRole role = getBattleRole(playerId);
		BattleRole enemy = getBattleRole(getEnemyId(playerId));
		role.msgEnd();
		enemy.msgEnd();
		FightMsgSend.syncEnd(this.sessions.get(playerId));
		FightMsgSend.syncEnd(this.sessions.get(enemy.getPlayerId()));
	}
	
	public void interruptCardSync(int playerId, CardBase card) {
		BattleRole enemy = getBattleRole(getEnemyId(playerId));
		FightMsgSend.interruptCard(this.sessions.get(playerId), card);
		FightMsgSend.interruptCard(this.sessions.get(enemy.getPlayerId()), card);
	}
	
	public void amplifySync(CardBase card, int amplify) {
		if (card.getType() != CardModel.TROOP) {
			return;
		}
		FightMsgSend.amplifySync(this.sessions.get(card.getPlayerId()), card.getUid(), amplify);
		FightMsgSend.amplifySync(this.sessions.get(getEnemyId(card.getPlayerId())), card.getUid(), amplify);
	}
	
	public void returnHandCardsSync(BattleRole fighter, ArrayList<CardBase> cards) {
		if (cards.size() == 0) {
			return;
		}
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		ISession session = this.sessions.get(playerId);
		ISession enemySession = this.sessions.get(enemyId);
		for (CardBase card : cards) {
			FightMsgSend.returnHandCardsSync(session, playerId, card, this.fighters.get(card.getPlayerId()));
			FightMsgSend.returnHandCardsSync(enemySession, enemyId, card, this.fighters.get(card.getPlayerId()));
			FightMsgSend.cardCostSync(this.sessions.get(card.getPlayerId()), card, this.fighters.get(card.getPlayerId()), false);
		}
	}
	
	public void areaLvUpNeedResource(BattleRole fighter) {
		int playerId = fighter.getPlayerId();
		int enemyId = getEnemyId(playerId);
		FightMsgSend.areaLvUpNeedResource(this.sessions.get(playerId), fighter.getPlayerId(), fighter.getLvUpResource());
		FightMsgSend.areaLvUpNeedResource(this.sessions.get(enemyId), fighter.getPlayerId(), fighter.getLvUpResource());
	}
	
	public void revealCardSync(CardBase card) {
		int playerId = card.getPlayerId();
		int enemyId = getEnemyId(playerId);
		BattleRole fighter = this.getBattleRole(playerId);
//		FightMsgSend.revealCardSync(this.sessions.get(playerId), card, fighter);
		FightMsgSend.revealCardSync(this.sessions.get(enemyId), card, fighter);
	}
	
	public void excessDamageSync(int playerId, int defCardUid, int excessDamage) {
		int enemyId = getEnemyId(playerId);
		FightMsgSend.excessDamageSync(this.sessions.get(playerId), defCardUid, excessDamage);
		FightMsgSend.excessDamageSync(this.sessions.get(enemyId), defCardUid, excessDamage);
	}
	
	@SuppressWarnings("unused")
	private boolean haveGuardian(BattleRole fighter, TroopCard attCard) {
		for (Area area : fighter.getAreas()) {
			if (haveGuardian(area, attCard)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean haveGuardian(Area area, TroopCard attCard) {
		int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(area.getIndex());
		ArrayList<TroopCard> troops = area.getTroops();
		for (TroopCard troop : troops) {
			if (troop.isMermaidLover() && troops.size() > 1) {
				continue;
			}
			if (!troop.canBeAttack()) {
				continue;
			}
			if (troop.isGuardian() && (!troop.isFlight() || oppoAreaIndex == attCard.getAreaIndex())) {
				return true;
			}
		}
		return false;
	}
	
	public Object getRandomTarget(BattleRole defender, TroopCard attCard) {
		ArrayList<TroopCard> troops = new ArrayList<>();
		ArrayList<TroopCard> guardians = new ArrayList<>();

		int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(attCard.getAreaIndex());
		Area oppoArea = defender.getArea(oppoAreaIndex);
		ArrayList<TroopCard> tempList = oppoArea.getTroops();
		
		/*
		 * 如果攻击者只能攻击英雄
		 */
		if (attCard.isHeroAttacker()) {
			if (tempList.size() > 0) {
				return null;
			}
			return defender;
		}
		/*
		 * 如果对面区域有部队
		 */
		if (tempList.size() > 0) {
			for (TroopCard troop : tempList) {
				if (!troop.canBeAttack()) {
					continue;
				}
				if (!troop.canBeOppAttack()) {
					continue;
				}
				if (troop.isMermaidLover() && tempList.size() > 1) {
					continue;
				}
				if (troop.isStun()) {
					continue;
				}
				if (troop.isGuardian()) {
					setTargetList(guardians, troop);
				} else {
					setTargetList(troops, troop);
				}
			}
			if (guardians.size() > 0) {
				return random(guardians, null);
			} else if (troops.size() > 0) {
				return random(troops, null);
			}
		}
		/*
		 * 如果对面区域没可攻击部队
		 */
		for (Area area : defender.getAreas()) {
			if (area.getIndex() == oppoAreaIndex) {
				continue;
			}
			tempList = area.getTroops();
			if (tempList.size() > 0) {
				for (TroopCard troop : tempList) {
					if (!troop.canBeAttack()) {
						continue;
					}
					if (troop.isFlight()) { // 其他区域的部队无法攻击闪避部队
						continue;
					}
					if (troop.isMermaidLover() && tempList.size() > 1) {
						continue;
					}
					if (troop.isStun()) {
						continue;
					}
					if (troop.isGuardian()) {
						setTargetList(guardians, troop);
					} else {
						setTargetList(troops, troop);
					}
				}
			}
		}

		if (guardians.size() > 0) {
			return random(guardians, null);
		} else {
			return random(troops, defender);
		}
	}
	
	/**
	 * 打出卡牌计数
	 * 
	 * @param card
	 * @param fighter
	 */
	private void playCardCount(CardBase card, BattleRole fighter) {
		fighter.getQuestManager().playCardCount(card);
		fighter.playCard(card);
		
		int playerId = fighter.getPlayerId();
		
		LogInfo logInfo = new LogInfo(playerId, LogInfo.PLAY_CARD);
		logInfo.setCardId(card.getRealId());
		logInfo.setCardType(card.getType());
		logInfos.add(logInfo);
		card.setLogInfo(logInfo);
		
		if (!Tools.isEmptyString(card.getSubType())) {
		switch (card.getSubType()) {
			case CardModel.TALE:
				fighter.addStatusCount(CardModel.TALE, 1);
				if (fighter.setPlayCards(card.getId())) {
					fighter.addCostArg(SkillManager.COST_TALE, 1);
					ArrayList<CardBase> list = new ArrayList<>();
					list.addAll(this.getTroopsByPlayerId(playerId));
					list.addAll(fighter.getHandCards());
					costSync(card, list, SkillManager.COST_TALE, false);
				}
				break;
			case CardModel.PLANT:
				fighter.addStatusCount(CardModel.PLANT, 1);
				fighter.addCostArg(SkillManager.COST_PLANT, 1);
				ArrayList<CardBase> list = new ArrayList<>();
				list.addAll(this.getTroopsByPlayerId(playerId));
				list.addAll(fighter.getHandCards());
				costSync(card, list, SkillManager.COST_PLANT, false);
				break;
			}
		}
	}
	
	/**
	 * 打出卡牌技能效果
	 * 
	 * @param card
	 * @param area
	 * @param fighter
	 */
	private void playCardEffect(CardBase card, Area area, BattleRole fighter) {
		int playerId = fighter.getPlayerId();
		switch (card.getType()) {
		case CardModel.TROOP:
			triggerEffect(TriggerManager.FIRST_TROOP, playerId, card, 1);
			skillEffect(card, playerId, area, fighter, 1, true);
			getTriggerManager().warcry(this, card, fighter, area);
			areaSkillEffect(area);
			triggerEffect(TriggerManager.TROOP, playerId, card, 1);
			triggerEffect(TriggerManager.TROOP_CHANGE, playerId, 1);
			triggerEffect(TriggerManager.ADD_TROOP, playerId, card, 1);
			triggerEffect(TriggerManager.AREA_TROOP, playerId, card, 1);
			fighter.setStatusTrun(BattleRole.LAST_TROOP, Integer.parseInt(card.getRealId()));
			break;
		case CardModel.SPELL:
			triggerEffect(TriggerManager.FIRST_SPELL, playerId, card, 1);
			triggerEffect(TriggerManager.SPELL, playerId, card, 1);
			skillEffect(card, playerId, area, fighter, 1, true);
			triggerEffect(TriggerManager.SPELL_AFTER, playerId, card, 1);
			break;
		case CardModel.TRAP:
			triggerEffect(TriggerManager.FIRST_SPELL, playerId, card, 1);
			triggerEffect(TriggerManager.SPELL, playerId, card, 1);
			trapEffect(card, playerId, area, fighter, 0);
			triggerEffect(TriggerManager.SPELL_AFTER, playerId, card, 1);
			triggerEffect(TriggerManager.TRAP, playerId, card, 1);
			triggerEffect(TriggerManager.TROOP_CHANGE, playerId, 1);
			break;
		case CardModel.ARTIFACT:
			boolean isEffect = skillEffect(card, playerId, area, fighter, 1, true);
			if (isEffect) {
				this.artiTriggerSync(card);
			}
			getTriggerManager().warcry(this, card, fighter, area);
			triggerEffect(TriggerManager.ARTIFACT, playerId, card, 1);
			break;
		}

		triggerEffect(TriggerManager.TALE, playerId, card, 1);
		triggerEffect(TriggerManager.PLANT, playerId, card, 1);
		triggerEffect(TriggerManager.PLAY_CARD, playerId, card, 1);
		triggerEffect(TriggerManager.FIRST, playerId, card, 1);
		triggerEffect(TriggerManager.SECOND, playerId, card, 1);
		logSync(card.getLogInfo());
		card.setLogInfo(null);
	}
	
	private void setTargetList(ArrayList<TroopCard> troops, TroopCard troop) {
		troops.add(troop);
	}
	
	public IBattleObject random(ArrayList<TroopCard> troops, BattleRole fighter) {
		if (troops.size() == 0 && fighter == null) {
			return null;
		}
		int count = fighter == null ? troops.size() : troops.size() + 1;
		int i = Tools.random(1, count);
		if (i > troops.size()) {
			return fighter;
		} else {
			return troops.get(i - 1);
		}
	}
	
	public boolean checkDead(CardBase card) {
		if (card.isDead()) {
			this.destoryCard(card.getPlayerId(), card);
			return true;
		}
		return false;
	}
	
	@Override
	public void giveup(int loser) {
		BattleRole role = this.getBattleRole(loser);
		role.setHp(0);
		settlement(loser);
	}

	public BattleRole getBattleRole(int playerId) {
		return this.fighters.get(playerId);
	}
	
	public HashMap<Integer, BattleRole> getBattleRoles() {
		return this.fighters;
	}

	public TriggerManager getTriggerManager() {
		return triggerManager;
	}

	public void setTriggerManager(TriggerManager triggerManager) {
		this.triggerManager = triggerManager;
	}

	public CardDeckModel getDeck(int playerId) {
		return decks.get(playerId);
	}
	
	private void notice() {
		// TODO 客户端提示
		for (Integer playerId : fighters.keySet()) {
			if (this.ready.get(playerId) != null) {
				continue;
			}
			FightMsgSend.notice(this.sessions.get(playerId));
		}
		/*
		 * 倒计时15秒。。
		 */
		logger.info("房间：{}，倒计时15秒后自动开始。", this.roomId);
		int seconds = 17000;
		future = GameTimer.getScheduled().schedule(() -> autoReady(), seconds, TimeUnit.MILLISECONDS);
	}
	
	private void autoReady() {
		GameRoomManager.getInstance().getLock().lock(getRoomId());
		try {
			for (Integer playerId : fighters.keySet()) {
				ready(playerId);
			}
		} finally {
			GameRoomManager.getInstance().getLock().unlock(getRoomId());
		}
	}
	
	public void startTimer() {
		if (future != null && future.getDelay(TimeUnit.MILLISECONDS) > 0) {
			interruptTimer();
		}
		/*
		 * 倒计时45秒。。
		 */
		int seconds = 45000;
		future = GameTimer.getScheduled().schedule(() -> notice(), seconds, TimeUnit.MILLISECONDS);
		logger.info("房间：{}，倒计时60秒后自动开始。开始计时。", this.roomId);
	}
	
	public void interruptTimer() {
		if (future != null && future.getDelay(TimeUnit.MILLISECONDS) > 0) {
			future.cancel(false);
			logger.info("房间：{}，中止计时", this.roomId);
		}
	}
	
	private void turnNotice() {
		// TODO 客户端提示
		for (Integer playerId : fighters.keySet()) {
			FightMsgSend.turnNotice(this.sessions.get(playerId));
		}
		/*
		 * 倒计时15秒。。
		 */
		logger.info("房间：{}，倒计时15秒后结束回合。", this.roomId);
		int seconds = 17000;
		future = GameTimer.getScheduled().schedule(() -> turnEnd(), seconds, TimeUnit.MILLISECONDS);
	}
	
	private void turnEnd() {
		GameRoomManager.getInstance().getLock().lock(getRoomId());
		try {
			autoTurnEnd(nowPlayer);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(getRoomId());
		}
	}
	
	private void autoTurnEnd(int playerId) {
		playState = PLAY_STATE_AUTO;
		BattleRole fighter = this.getBattleRole(playerId);
		boolean isEnd = fighter.getState() == BattleRole.END;
		if (fighter.getStatus(BattleRole.CHECK)) {
			checkCardSelect(playerId, Tools.random());
		}
		if (fighter.getStatus(BattleRole.TARGET_SELECT)) {
			SkillArg arg = fighter.getInterruptSkillArg();
			if (arg != null) {
				SkillManager.getInstance().randomTarget(arg);
			}
			interruptRestart(playerId);
		}
		if (fighter.getFindCards().size() != 0) {
			while (fighter.getFindCards().size() != 0) {
				FindCard findCard = fighter.getFindCards().get(0);
				switch (findCard.getType()) {
				case FindCard.FIND:
				case FindCard.REVEAL:
					int index = Tools.random(0, findCard.getCards().size() - 1);
					findCardSelect(playerId, index);
					logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。Find自动选择，Index：{}。", fighter.getPlayerId(), this.roomId, findCard.getCardId(), index);
					break;

				case FindCard.CONSTRUCT_TRAP:
					index = Tools.random(0, findCard.getCards().size() - 1);
					CardBase cardBase = findCard.getCards().get(index);
					constructTrapCardSelect(playerId, cardBase.getUid());
					logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。组装陷阱自动选择，cardUid：{}。", fighter.getPlayerId(), this.roomId, findCard.getCardId(), cardBase.getUid());
					break;
					
				case FindCard.SUMMON:
					index = Tools.random(0, 2);
					cardBase = findCard.getCards().get(index);
					summonReplaceCard(fighter, cardBase.getUid());
					logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。召唤替换自动选择，cardUid：{}。", fighter.getPlayerId(), this.roomId, findCard.getCardId(), cardBase.getUid());
					break;
					
				case FindCard.SUMMON_COPY:
					fighter.getFindCards().remove(0);
					logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。复制打出自动跳过。", fighter.getPlayerId(), this.roomId, findCard.getCardId());
					break;
				}
			}
		}
		if (!isEnd) {
			turnFinish(playerId);
		}
	}
	
	public int getPlayState() {
		return playState;
	}

	public void setPlayState(int playState) {
		this.playState = playState;
	}

	public void startTurnTimer() {
		if (future != null && future.getDelay(TimeUnit.MILLISECONDS) > 0) {
			interruptTimer();
		}
		if (!isCountDown()) {
			return;
		}
		/*
		 * TODO 倒计时75秒
		 */
		future = GameTimer.getScheduled().schedule(() -> turnNotice(), turnCountDownTime, TimeUnit.MILLISECONDS);
		logger.info("房间：{}，倒计时75秒后结束回合。当前回合玩家：{}。开始计时。", this.roomId, nowPlayer);
	}
	
	public HashMap<String, ArrayList<CardBase>> getConstructTrapCards() {
		return constructTrapCards;
	}

	public void setConstructTrapCards(HashMap<String, ArrayList<CardBase>> constructTrapCards) {
		this.constructTrapCards = constructTrapCards;
	}

	public boolean isCountDown() {
		return countDown;
	}

	public void setCountDown(boolean countDown) {
		this.countDown = countDown;
	}

	public int getTurnCountDownTime() {
		return turnCountDownTime;
	}

	public void setTurnCountDownTime(int turnCountDownTime) {
		this.turnCountDownTime = turnCountDownTime;
	}
	
	public ISession getSession(int playerId) {
		return this.sessions.get(playerId);
	}
	
	public void setRoleState(int playerId, int state) {
		BattleRole role = getBattleRole(playerId);
		BattleRole enemy = getBattleRole(getEnemyId(playerId));
		role.setState(state);
		enemy.setState(BattleRole.OTHER);
	}
	
	public void clearRoleState(int playerId) {
 		BattleRole role = getBattleRole(playerId);
		if (role.getInterruptSkillArg() != null) {
			return;
		}
		BattleRole enemy = getBattleRole(getEnemyId(playerId));
		role.clearState();
		enemy.clearState();
	}
	
	private void addDestoryCard(ArrayList<CardBase> destoryList, ArrayList<CardBase> deadList, CardBase card) {
		if (card.isDead()) {
			deadList.add(card);
		}
		if (card.getArea() == null) {
			destoryList.add(card);
		}
	}
}
