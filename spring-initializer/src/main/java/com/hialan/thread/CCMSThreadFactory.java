package com.hialan.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 参考 CountingThreadFactory实现,加入tenantId以区分不同用户的线程
 * 
 * @author xiaojing.qu
 * 
 */
public class CCMSThreadFactory implements ThreadFactory {

	final static Logger logger = LoggerFactory.getLogger(CCMSThreadFactory.class);

	final String threadGroupName;
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final AtomicInteger count = new AtomicInteger();
	final String namePrefix;

	/**
	 * 构造一个ThreadFactory
	 * 
	 * @param tenantId
	 *            租户ID如 qiushi，samllye
	 * @param serviceShortName
	 *            线程组的名称（业务的简称）
	 * @param workerNamePrefix
	 *            线程名的前缀
	 * @param threadPriority
	 *            线程的优先级
	 */
	public CCMSThreadFactory(String tenantId, String serviceShortName, String workerNamePrefix, int threadPriority) {
		threadGroupName = "<" + tenantId + ">" + "-" + serviceShortName;
		group = new ThreadGroup(threadGroupName);
		threadPriority = (threadPriority > Thread.MAX_PRIORITY || threadPriority < Thread.MIN_PRIORITY) ? Thread.NORM_PRIORITY
				: threadPriority;
		group.setMaxPriority(threadPriority);
		namePrefix = "<" + tenantId + ">" + "-" + serviceShortName + "-" + workerNamePrefix + "-";
	}

	public Thread newThread(Runnable r) {
		String threadName = namePrefix + threadNumber.getAndIncrement();
		Thread newThread = new Thread(group, new RunnableWithLifeCycle(this, r), threadName, 0);
		newThread.setDaemon(false);
		newThread.setPriority(group.getMaxPriority());
		int newCount = count.incrementAndGet();
		logger.info("ThreadGroup:{}创建新线程,现有活动线程数{}", threadGroupName, newCount);
		return newThread;
	}

	/**
	 * 获取当前线程池创建的并且还活着的线程数量
	 * 
	 * @return
	 */
	public int getNumberOfAliveThreads() {
		return count.get();
	}

	private void threadExecutionComplete() {
		int newCount = count.decrementAndGet();
		//logger.info("ThreadGroup:{}中线程{}执行结束,现有活动线程数{}", new Object[] { threadGroupName,
		//		Thread.currentThread().getName(), newCount });
	}

	private static class RunnableWithLifeCycle implements Runnable {

		private final Runnable actualRunnable;
		private final CCMSThreadFactory ccmsThreadFactory;

		public RunnableWithLifeCycle(CCMSThreadFactory ccmsThreadFactory, Runnable actualRunnable) {
			super();
			this.ccmsThreadFactory = ccmsThreadFactory;
			this.actualRunnable = actualRunnable;
		}

		public void run() {
			try {
				if (actualRunnable != null) {
					actualRunnable.run();
				}
			} catch (Throwable t) {
				//logger.info("Thread:[{}] throw Exception:[{}]", Thread.currentThread().getName()
				//		, t.getMessage());
				t.printStackTrace();
			} finally {
				ccmsThreadFactory.threadExecutionComplete();
			}
		}

	}

}
