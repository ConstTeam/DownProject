package net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NioWriteDelay {
	protected ExecutorService executorService;

	protected ScheduledThreadPoolExecutor stpe;

	public static int MAX_SEND = 256;

	public NioWriteDelay(int pool, int thread) {
		executorService = Executors.newFixedThreadPool(pool);
		stpe = new ScheduledThreadPoolExecutor(thread, new ThreadFactory() {

			private final AtomicInteger threadNumber = new AtomicInteger(1);

			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("Timermanager-nioWrite-"
						+ threadNumber.getAndIncrement());
				return t;
			}
		});
		stpe.setMaximumPoolSize(4);
		stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
	}

	public NioWriteDelay() {
		executorService = Executors.newFixedThreadPool(1);
		stpe = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {

			private final AtomicInteger threadNumber = new AtomicInteger(1);

			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("Timermanager-nioWrite-"
						+ threadNumber.getAndIncrement());
				return t;
			}
		});
		stpe.setMaximumPoolSize(4);
		stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
	}

	protected ScheduledFuture<?> schedule(Runnable r, long delay) {
		return stpe.schedule(r, delay, TimeUnit.MILLISECONDS);
	}

	public void sendDealy(ISendData net, IByteBuffer data) {
		addWork(new SendWork(net, data));
	}

	protected void addWork(Runnable work) {
		executorService.execute(work);
	}

	public class SendWork implements Runnable {

		protected ISendData net;

		protected IByteBuffer data;

		protected int sendCount;

		protected SendWork(ISendData net, IByteBuffer data) {
			this.net = net;
			this.data = data;
			sendCount = 0;
		}

		@Override
		public void run() {
			try {

				synchronized (data) {
					int i = data.available();
					if (i == 0) {
						net.setIsbusy(false);
						return;
					}
					int sendlen = this.net.sendDataImpl(data.getRawBytes(), 0,
							i);
					data.setReadPos(sendlen);
					data.pack();
					if (sendlen == i) {
						net.setIsbusy(false);
						return;
					}
					sendCount++;
					if (sendCount > MAX_SEND) {
						net.close();
						net.setIsbusy(false);
						return;
					}
				}
				schedule(this, 100);// 未刷新完缓冲区，100毫秒后继续
			} catch (Exception e) {
				net.setIsbusy(false);
				net.close();
				e.printStackTrace();
			}

		}
	}

	// public class AddWork implements Runnable
	// {
	//
	// protected Runnable job;
	//
	// public AddWork(Runnable job)
	// {
	// this.job = job;
	// }
	//
	// public void run()
	// {
	// addWork(job);
	// }
	// }
}
