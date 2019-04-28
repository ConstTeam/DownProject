package net;

/**
 * 处理单个业务模块的服务类
 * 
 * @author water
 * 
 */
public interface IService {

	/** 处理具体业务逻辑 */
	public void doMessage(ISession session, IByteBuffer data);

	/** 得到环境类 */
	public IContext getContext();

	/** 初始化 */
	public void init(IContext context);

	/** 销毁 */
	public void destroy();
}
