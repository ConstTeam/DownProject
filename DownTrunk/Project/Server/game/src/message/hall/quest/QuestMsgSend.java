package message.hall.quest;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.hall.HallMsgModuleConst;
import module.quest.Quest;
import module.quest.SignInQuest;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;

/**
 * 任务模块消息发送
 *
 */
public class QuestMsgSend {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(QuestMsgSend.class);
	
	public static void questInfo(IByteBuffer data, Quest quest) {
		data.writeByte(quest.getIndex());
		data.writeInt(quest.getQuestId());
		data.writeInt(quest.getValue());
		data.writeByte(quest.getState());
	}
	
	public static void questInfoSync(ISession session, ArrayList<Quest> questInfos) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(HallMsgModuleConst.QUEST_RESPONSE);
		data.writeByte(QuestMsgConst.QUEST_INFO_SYNC);
		
		data.writeByte(questInfos.size());
		for (Quest quest : questInfos) {
			questInfo(data, quest);
		}
		
		session.send(data);
	}
	
	public static void questInfoSingleSync(ISession session, Quest questInfo) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(HallMsgModuleConst.QUEST_RESPONSE);
		data.writeByte(QuestMsgConst.QUEST_INFO_SINGLE_SYNC);
		
		questInfo(data, questInfo);
		
		session.send(data);
	}
	
	public static void signInQuestSync(ISession session, SignInQuest signIn) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(HallMsgModuleConst.QUEST_RESPONSE);
		data.writeByte(QuestMsgConst.SIGNIN_INFO_SYNC);
		
		data.writeBoolean(signIn.getCanReceive());
		data.writeInt(signIn.getSignInDay());
		data.writeUTF(signIn.getPhoneNumber());
		
		session.send(data);
	}
	
	public static void receiveSignInQuestSync(ISession session, int Gold, String CardId, int cardPackCount, SignInQuest signIn) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(HallMsgModuleConst.QUEST_RESPONSE);
		data.writeByte(QuestMsgConst.RECEIVE_SIGNIN_RET);
		
		data.writeInt(signIn.getSignInDay());
		data.writeInt(Gold);
		data.writeUTF(CardId);
		data.writeInt(cardPackCount);
		
		session.send(data);
	}
}
