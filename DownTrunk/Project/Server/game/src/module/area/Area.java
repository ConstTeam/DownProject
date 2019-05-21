package module.area;

import java.util.ArrayList;
import java.util.HashMap;

import config.model.card.CardModel;
import module.card.ArtifactCard;
import module.card.CardBase;
import module.card.TrapCard;
import module.card.TroopCard;

public class Area {

	/** 区域主行最多3。Index最大值2 */
	public static final int ROW_MAX_INDEX = 3;
	/** 神器陷阱每个区域最多2个 */
	public static final int SUB_ROW_MAX = 2;
	/** 区域等级上限 */
	public static final int MAX_LEVEL = 2;
	
	/** 区域消耗资源上限 */
	public static final int MAX_COST = 3;

	/** 玩家id */
	private int playerId;
	/** 区域索引 */
	private int index;
	/** 区域符文 */
	private int rune;
	/** 区域等级*/
	private int level;
	/** 主行HashMap<Index, TroopCard> */
	private ArrayList<TroopCard> mainRow = new ArrayList<>(ROW_MAX_INDEX);
	/** 神器陷阱HashMap<Index, TroopCard> */
	private HashMap<Integer, CardBase> subRow = new HashMap<>(SUB_ROW_MAX);

	public Area(int index, int playerId) {
		setIndex(index);
		setPlayerId(playerId);
	}
	
	public ArrayList<TrapCard> getTrap() {
		ArrayList<TrapCard> list = new ArrayList<>();
		for (CardBase card : this.subRow.values()) {
			if (card == null) {
				continue;
			}
			if (card.getType() == CardModel.TRAP) {
				list.add((TrapCard)card);
			}
		}
		return list;
	}

	public void setTrap(TrapCard trap) {
		addArtiOrTrap(trap);
	}

	public ArrayList<ArtifactCard> getArtifact() {
		ArrayList<ArtifactCard> list = new ArrayList<>();
		for (CardBase card : this.subRow.values()) {
			if (card == null) {
				continue;
			}
			if (card.getType() == CardModel.ARTIFACT) {
				list.add((ArtifactCard)card);
			}
		}
		return list;
	}

	public void setArtifact(ArtifactCard artifact) {
		addArtiOrTrap(artifact);
	}
	
	public ArrayList<CardBase> getArtiTraps() {
		ArrayList<CardBase> list = new ArrayList<>();
		for (CardBase card : this.subRow.values()) {
			if (card == null) {
				continue;
			}
			list.add(card);
		}
		return list;
	}
	
	public ArrayList<TroopCard> getTroops() {
		ArrayList<TroopCard> list = new ArrayList<>();
		list.addAll(this.mainRow);
		return list;
	}
	
	public ArrayList<TroopCard> getOppCanAttackTroops() {
		ArrayList<TroopCard> list = new ArrayList<>();
		for (TroopCard troop : this.mainRow) {
			if (troop != null && !troop.isStun() && troop.canBeOppAttack()) {
				list.add(troop);
			}
		}
		return list;
	}
	
	public void addTroop(TroopCard card) {
		this.mainRow.add(card);
		card.setArea(this);
	}

	public void addArtiOrTrap(CardBase card) {
		for (int i = 0; i < SUB_ROW_MAX; i++) {
			if (this.subRow.get(i) == null) {
				this.subRow.put(i, card);
				card.setArea(this);
				return;
			}
		}
	}
	
	public void addTroop(int mainRowIndex, TroopCard card) {
		this.mainRow.add(mainRowIndex, card);
		card.setArea(this);
	}

	public void addArtiOrTrap(int mainRowIndex, CardBase card) {
		mainRowIndex = mainRowIndex - ROW_MAX_INDEX;
		this.subRow.put(mainRowIndex, card);
		card.setArea(this);
	}
	
	public boolean removeCard(CardBase card) {
		switch (card.getType()) {
		case CardModel.TROOP:
			TroopCard troop = (TroopCard) card;
			return removeTroop(troop);

		case CardModel.TRAP:
		case CardModel.ARTIFACT:
			return removeArtiOrTrap(card);
		}
		return false;
	}
	
	public int getMainRowIndex(CardBase card) {
		switch (card.getType()) {
		case CardModel.TROOP:
			if (this.mainRow.indexOf(card) == -1) {
				return -1;
			}
			return this.mainRow.indexOf(card);
		case CardModel.TRAP:
		case CardModel.ARTIFACT:
			for (int i = 0; i < SUB_ROW_MAX; i++) {
				if (this.subRow.get(i) != null) {
					if (this.subRow.get(i).getUid() == card.getUid()) {
						return i + Area.ROW_MAX_INDEX;
					}
				}
			}
		}
		return -1;
	}
	
	public boolean removeTroop(TroopCard card) {
		if (this.mainRow.indexOf(card) != -1) {
			this.mainRow.remove(card);
			card.setArea(null);
			return true;
		}
		return false;
	}
	
	public boolean removeArtiOrTrap(CardBase card) {
		for (int i = 0; i < SUB_ROW_MAX; i++) {
			if (this.subRow.get(i) != null) {
				if (this.subRow.get(i).getUid() == card.getUid()) {
					this.subRow.remove(i);
					card.setArea(null);
					return true;
				}
			}
		}
		return false;
	}
	
	public int getMainRowSize() {
		return this.mainRow.size();
	}
	
	public int getSubRowSize() {
		int count = 0;
		for (int i = 0; i < SUB_ROW_MAX; i++) {
			if (this.subRow.get(i) != null) {
				count++;
			}
		}
		return count;
	}
	
	public boolean troopIsFull() {
		return mainRow.size() >= ROW_MAX_INDEX;
	}
	
	public boolean artiOrTrapIsFull() {
		return !haveArtiOrTrapSeat();
	}
	
	public boolean troopIsEmpty() {
		return mainRow.size() == 0;
	}
	
	public boolean haveTroopSeat() {
		if (this.level <= 0) {
			return false;
		}
		return this.mainRow.size() < ROW_MAX_INDEX;
	}
	
	public boolean haveArtiOrTrapSeat() {
		if (this.level != MAX_LEVEL) {
			return false;
		}
		for (int i = 0; i < SUB_ROW_MAX; i++) {
			if (this.subRow.get(i) == null) {
				return true;
			}
		}
		return false;
	}
	
	public TroopCard getTroopByIndex(int index) {
		if (this.mainRow.size() <= index) {
			return null;
		}
		return this.mainRow.get(index);
	}
	
	public CardBase getArtiOrTrapByIndex(int index) {
		index = index - ROW_MAX_INDEX;
		return this.subRow.get(index);
	}
	
	public CardBase getCardByIndex(int index) {
		if (index < ROW_MAX_INDEX) {
			return getTroopByIndex(index);
		} else {
			return getArtiOrTrapByIndex(index);
		}
	}

	public int getRune() {
		return rune;
	}

	public void setRune(int rune) {
		this.rune = rune;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public int addLevel() {
		return this.level++;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
}
