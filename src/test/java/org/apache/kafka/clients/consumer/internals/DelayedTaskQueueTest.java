package org.apache.kafka.clients.consumer.internals;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * 延迟任务跟踪队列测试
 *
 * @author wanggang
 *
 */
public class DelayedTaskQueueTest {

	private DelayedTaskQueue scheduler = new DelayedTaskQueue();
	private ArrayList<DelayedTask> executed = new ArrayList<>();

	@Test
	public void testScheduling() {
		// Empty scheduler
		assertEquals(Long.MAX_VALUE, scheduler.nextTimeout(0));
		scheduler.poll(0);
		assertEquals(Collections.emptyList(), executed);

		TestTask task1 = new TestTask();
		TestTask task2 = new TestTask();
		TestTask task3 = new TestTask();
		scheduler.add(task1, 20);
		assertEquals(20, scheduler.nextTimeout(0));
		scheduler.add(task2, 10);
		assertEquals(10, scheduler.nextTimeout(0));
		scheduler.add(task3, 30);
		assertEquals(10, scheduler.nextTimeout(0));

		scheduler.poll(5);
		assertEquals(Collections.emptyList(), executed);
		assertEquals(5, scheduler.nextTimeout(5));

		scheduler.poll(10);
		assertEquals(Arrays.asList(task2), executed);
		assertEquals(10, scheduler.nextTimeout(10));

		scheduler.poll(20);
		assertEquals(Arrays.asList(task2, task1), executed);
		assertEquals(20, scheduler.nextTimeout(10));

		scheduler.poll(30);
		assertEquals(Arrays.asList(task2, task1, task3), executed);
		assertEquals(Long.MAX_VALUE, scheduler.nextTimeout(30));
	}

	@Test
	public void testRemove() {
		TestTask task1 = new TestTask();
		TestTask task2 = new TestTask();
		TestTask task3 = new TestTask();
		scheduler.add(task1, 20);
		scheduler.add(task2, 10);
		scheduler.add(task3, 30);
		scheduler.add(task1, 40);
		assertEquals(10, scheduler.nextTimeout(0));

		scheduler.remove(task2);
		assertEquals(20, scheduler.nextTimeout(0));

		scheduler.remove(task1);
		assertEquals(30, scheduler.nextTimeout(0));

		scheduler.remove(task3);
		assertEquals(Long.MAX_VALUE, scheduler.nextTimeout(0));
	}

	private class TestTask implements DelayedTask {

		@Override
		public void run(long now) {
			executed.add(this);
		}

	}

}
