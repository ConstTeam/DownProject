namespace MS
{
	public class ModuleDataFirst
	{
		public const byte MODULE_PING			= 0;	//心跳消息
		public const byte MODULE_LOGIN			= 1;	//登陆模块
		public const byte MODULE_ROLE			= 2;	//玩家
        public const byte MODULE_CARD           = 3;	//卡牌模块
		public const byte MODULE_GM				= 4;	//GM
        public const byte MODULE_LADDER         = 5;	//天梯
        public const byte MODULE_TASK           = 6;    //任务
		public const byte MODULE_GUIDE			= 7;	//指引

		public const byte MODULE_BATTLE_LOGIN	= 101;	//战斗服登录相关
		public const byte MODULE_BATTLE			= 102;	//战斗逻辑
	}
}
