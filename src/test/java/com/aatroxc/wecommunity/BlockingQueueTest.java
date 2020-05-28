package com.aatroxc.wecommunity;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author mafei007
 * @date 2020/5/5 16:53
 */


public class BlockingQueueTest {

	public static void main(String[] args) {
		// 队列容量为10
		BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(10);
		new Thread(new Producer(blockingQueue), "生产者1").start();

		new Thread(new Consumer(blockingQueue), "消费者1").start();
		new Thread(new Consumer(blockingQueue), "消费者2").start();
		new Thread(new Consumer(blockingQueue), "消费者3").start();
	}

}

class Producer implements Runnable {

	private final BlockingQueue<Integer> blockingQueue;

	Producer(BlockingQueue<Integer> blockingQueue) {
		this.blockingQueue = blockingQueue;
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < 100; i++) {
				Thread.sleep(20);
				blockingQueue.put(i);
				System.out.println(Thread.currentThread().getName() + "正在生产:" + i + ", 队列size:" + blockingQueue.size());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class Consumer implements Runnable {
	private final BlockingQueue<Integer> blockingQueue;

	Consumer(BlockingQueue<Integer> blockingQueue) {
		this.blockingQueue = blockingQueue;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(new Random().nextInt(1000));
				Integer take = blockingQueue.take();
				System.out.println(Thread.currentThread().getName() + "正在消费：" + take + ", 队列size:" + blockingQueue.size());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}