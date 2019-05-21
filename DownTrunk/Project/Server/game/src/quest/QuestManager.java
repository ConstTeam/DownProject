package quest;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.card.CardModel;
import config.model.quest.QuestModel;
import config.model.quest.QuestSubModel;
import db.QuestDao;
import module.card.CardBase;
import module.card.TroopCard;
import module.fight.BattleRole;
import module.quest.Quest;
import module.scene.GameRoom;
import util.Tools;

public class QuestManager implements IQuestConst {

	private static final Logger logger = LoggerFactory.getLogger(QuestManager.class);

	private static QuestManager getInstance() {
		return new QuestManager();
	}
	
	private ArrayList<Quest> questInfo;

	private HashMap<String, Integer> questCount;
	
	private HashMap<String, HashMap<Integer, Integer>> troopStateMap;
	
	public static QuestManager init(int playerId) {
		QuestManager questManager = getInstance();
		questManager.questCount = new HashMap<>();
		questManager.questInfo = new ArrayList<>();
		questManager.troopStateMap = new HashMap<>();
		
		ArrayList<Quest> questInfo = QuestDao.getQuestInfo(playerId, null);
		if (questInfo == null) {
			logger.error("玩家Id：{}，任务初始化失败。", playerId);
			return null;
		}
		for (Quest quest : questInfo) {
			if (quest.getState() != 0) {
				continue;
			}
			QuestModel questModel = ConfigData.questModels.get(quest.getQuestId());
			if (questModel == null) {
				logger.error("玩家Id：{}，任务Id：{}。任务在配置中已不存在。", playerId, quest.getQuestId());
				continue;
			}
			QuestSubModel questSubModel = ConfigData.questSubModels.get(questModel.SubId);
			if (questSubModel == null) {
				logger.error("玩家Id：{}，任务SubId：{}。任务在Sub配置中已不存在。", playerId, questModel.SubId);
				continue;
			}
			if (questManager.isCountAction(questSubModel.Action)) {
				questManager.questCount.put(questManager.getKey(questSubModel.Action, questSubModel.Arg1, questSubModel.Arg2), 0);
			}
			if (STATE.equals(questSubModel.Arg1)) {
				questManager.troopStateMap.put(questSubModel.Arg2, new HashMap<>());
			}
			questManager.questInfo.add(quest);
		}
		return questManager;
	}
	
	public void addCount(String action, String arg1, Object arg2, int value) {
		String key = getKey(action, arg1, arg2);
		if (questCount.get(key) != null) {
			questCount.put(key, questCount.get(key) + value);
		}
	}
	
	public void addCount(String action, String arg1, int value) {
		addCount(action, arg1, null, value);
	}
	
	public void addCount(String action, int value) {
		addCount(action, null, null, value);
	}
	
	private String getKey(String action, String arg1, Object obj) {
		String arg2 = obj == null ? null : String.valueOf(obj);
		if (Tools.isEmptyString(arg1)) {
			return action;
		}
		if (Tools.isEmptyString(arg2)) {
			return String.format("%s-%s", action, arg1);
		}
		return String.format("%s-%s-%s", action, arg1, String.valueOf(arg2));
	}
	
	public void playCardCount(CardBase card) {
		CardModel cardModel = ConfigData.cardModels.get(card.getRealId());
		if (cardModel == null) {
			logger.error("卡牌Id：{}，在配置表中不存在！", card.getRealId());
			return;
		}
		addCount(PLAY_CARD, RUNE, cardModel.RuneStr, 1);
		addCount(PLAY_CARD, TYPE, card.getType(), 1);
		
		switch (card.getType()) {
		case CardModel.SPELL:
		case CardModel.TRAP:
			// 法术和陷阱计入法术卡
			addCount(PLAY_CARD, TYPE, 5, 1);
			break;
		}
	}

	public void cardStateCount(TroopCard troop, String state) {
		HashMap<Integer,Integer> map = this.troopStateMap.get(state);
		if (map == null) {
			return;
		}
		if (map.get(troop.getUid()) != null) {
			return;
		}
		if (!troop.getStatus(state)) {
			return;
		}
		map.put(troop.getUid(), troop.getUid());
		addCount(PLAY_CARD, STATE, state, 1);
	}
	
	private boolean isCountAction(String action) {
		switch (action) {
		case BATTLE:
		case DECK:
		case TURBO:
			return false;
		}
		return true;
	}
	
	public static HashMap<Integer, Integer> getInitQuest() {
		HashMap<Integer, Integer> result = new HashMap<>();
		HashMap<Integer, Integer> questTypes = new HashMap<>();
		for (int i = 1; i <= 4; i++) {
			QuestModel model = getRandomQuest(i, null, questTypes);
			questTypes.put(model.Type, model.Type);
			result.put(i, model.ID);
		}
		return result;
	}
	
	public static HashMap<Integer, Integer> getFlushQuest(ArrayList<Quest> quests, HashMap<Integer, Integer> questIds) {
		HashMap<Integer, Integer> result = new HashMap<>();
		HashMap<Integer, Integer> questTypes = new HashMap<>();
		for (Quest quest: quests) {
			if (quest.getState() == 0) {
				QuestModel model = ConfigData.questModels.get(quest.getQuestId());
				if (model != null) {
					questTypes.put(model.Type, model.Type);
				} else {
					quest.setState(3);
				}
				QuestSubModel questSubModel = ConfigData.questSubModels.get(model.SubId);
				if (questSubModel == null) {
					quest.setState(3);
				}
			}
		}
		for (Quest quest: quests) {
			if (quest.getState() == 0) {
				continue;
			}
			QuestModel model = getRandomQuest(quest.getIndex(), questIds, questTypes);
			questTypes.put(model.Type, model.Type);
			result.put(quest.getIndex(), model.ID);
		}
		return result;
	}
	
	public static QuestModel getRandomQuest(int index, HashMap<Integer, Integer> questIds, HashMap<Integer, Integer> questTypes) {
		ArrayList<QuestModel> questModels = new ArrayList<>();
		switch (index) {
		case 1:
		case 2:
			questModels.addAll(ConfigData.questModelsByLevel.get(1));
			questModels.addAll(ConfigData.questModelsByLevel.get(2));
			break;
		case 3:
			questModels.addAll(ConfigData.questModelsByLevel.get(2));
			questModels.addAll(ConfigData.questModelsByLevel.get(3));
			break;
		case 4:
			questModels.addAll(ConfigData.questModelsByLevel.get(2));
			questModels.addAll(ConfigData.questModelsByLevel.get(3));
			questModels.addAll(ConfigData.questModelsByLevel.get(4));
			break;
		}
		
		QuestModel result = null;
		while (result == null) {
			if (questModels.size() == 0) {
				logger.error("任务出现随机列表为空情况！将从所有任务中随机一条。");
				questModels.addAll(ConfigData.questModels.values());
				int random = Tools.random(0, questModels.size() - 1);
				return questModels.get(random);
			}
			int random = Tools.random(0, questModels.size() - 1);
			QuestModel model = questModels.get(random);
			if (questIds != null && questIds.get(model.ID) != null) {
				questModels.remove(model);
				continue;
			}
			if (questTypes != null && questTypes.get(model.Type) != null) {
				questModels.remove(model);
				continue;
			}
			
			result = model;
		}
		
		return result;
	}
	
	private void checkQuest(Quest quest, QuestModel questModel) {
		if (quest.getValue() >= questModel.Value) {
			quest.setState(1);
		}
	}
	
	private void addQuestValue(Quest quest, QuestSubModel questSubModel, GameRoom room, BattleRole role, boolean isWin) {
		if (questSubModel.Win && !isWin) {
			return;
		}
		
		if (isCountAction(questSubModel.Action)) {
			Integer value = questCount.get(getKey(questSubModel.Action, questSubModel.Arg1, questSubModel.Arg2));
			if (value != null) {
				if (questSubModel.CountType == 1) {
					if (questSubModel.Value > 0) {
						if (value >= questSubModel.Value) {
							quest.addValue(1);
						}
					} else if (questSubModel.Value < 0) {
						if (value == 0) {
							quest.addValue(1);
						}
					}
				} else if (questSubModel.CountType == 2) {
					if (value != 0) {
						quest.addValue(value);
					}
				}
			}
			return;
		}
		
		switch (questSubModel.Action) {
		case BATTLE:
			quest.addValue(1);
			break;
			
		case DECK:
			break;
		case TURBO:
			if (room.isTurboMode()) {
				quest.addValue(1);
			}
			break;
		}
	}
	
	public void settlement(GameRoom room, BattleRole role, boolean isWin) {
		
		int playerId = role.getPlayerId();
		for (Quest quest : this.questInfo) {
			if (quest.getState() != 0) {
				continue;
			}
			QuestModel questModel = ConfigData.questModels.get(quest.getQuestId());
			if (questModel == null) {
				logger.error("玩家Id：{}，任务Id：{}。任务在配置中已不存在。", playerId, quest.getQuestId());
				continue;
			}
			QuestSubModel questSubModel = ConfigData.questSubModels.get(questModel.SubId);
			if (questSubModel == null) {
				logger.error("玩家Id：{}，任务SubId：{}。任务在Sub配置中已不存在。", playerId, questModel.SubId);
				continue;
			}
			addQuestValue(quest, questSubModel, room, role, isWin);
			checkQuest(quest, questModel);
		}
	}
	
	public ArrayList<Quest> getQuestInfo() {
		return questInfo;
	}
}
