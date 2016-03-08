package org.javasimon.examples;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.javasimon.StopwatchSample;
import org.javasimon.utils.BenchmarkUtils;
import org.javasimon.utils.GoogleChartImageGenerator;
import org.javasimon.utils.SimonUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Measures how long it takes to execute a lot of get-start-stop cycles in heavily multithreaded environment.
 *
 * @author <a href="mailto:virgo47@gmail.com">Richard "Virgo" Richter</a>
 * @since 3.1
 */
public final class MultithreadedStopwatchLoad extends Thread {

	public static final int TOTAL_TASK_RUNS = 300000;

	private MultithreadedStopwatchLoad() {
	}

	/**
	 * Entry point of the demo application.
	 *
	 * @param args command line arguments
	 * @throws InterruptedException when sleep is interrupted
	 */
	public static void main(String[] args) throws InterruptedException {
		ExampleUtils.fillManagerWithSimons(100000);
		final ExecutorService executorService = Executors.newFixedThreadPool(1000);
		StopwatchSample[] results = BenchmarkUtils.run(1, 2,
			new BenchmarkUtils.Task("1") {
				@Override
				public void perform() throws Exception {
					new MultithreadedTester(TOTAL_TASK_RUNS, 1, executorService).execute();
				}
			},
			new BenchmarkUtils.Task("2") {
				@Override
				public void perform() throws Exception {
					new MultithreadedTester(TOTAL_TASK_RUNS, 2, executorService).execute();
				}
			},
			new BenchmarkUtils.Task("3") {
				@Override
				public void perform() throws Exception {
					new MultithreadedTester(TOTAL_TASK_RUNS, 3, executorService).execute();
				}
			},
			new BenchmarkUtils.Task("4") {
				@Override
				public void perform() throws Exception {
					new MultithreadedTester(TOTAL_TASK_RUNS, 4, executorService).execute();
				}
			},
			new BenchmarkUtils.Task("5") {
				@Override
				public void perform() throws Exception {
					new MultithreadedTester(TOTAL_TASK_RUNS, 5, executorService).execute();
				}
			},
			new BenchmarkUtils.Task("100") {
				@Override
				public void perform() throws Exception {
					new MultithreadedTester(TOTAL_TASK_RUNS, 100, executorService).execute();
				}
			}
		);
		executorService.shutdown();

		System.out.println("\nGoogle Chart avg:\n" +
			GoogleChartImageGenerator.barChart("Multithreaded test", results));
	}

	/**
	 * Class executes test-case for a selected number of threads. Total number of stopwatches is
	 * always the same (roughly, not counting division errors), loop count for a thread changes accordingly
	 * to the number of threads.
	 */
	private static class MultithreadedTester extends Thread {

		// name of the Simon will be the same like the name of this class
		private final String NAME = SimonUtils.generateName();

		private final int threads;
		private final int loop;
		private ExecutorService executorService;

		private final Runnable task = new TestTask();

		private final CountDownLatch latch;

		/**
		 * Creates the tester specifying total number of task runs, number of threads and thread pool
		 * (optional, can be {@code null}).
		 *
		 * @param taskRuns total number of task runs
		 * @param threads number of threads executing part of the run count
		 * @param executorService service used to execute tasks, can be {@code null}
		 */
		private MultithreadedTester(int taskRuns, int threads, ExecutorService executorService) {
			System.out.println("Creating Multithreaded test for " + threads + " threads");
			this.threads = threads;
			this.loop = taskRuns / threads;
			this.executorService = executorService;

			latch = new CountDownLatch(threads);
		}

		void execute() throws InterruptedException {
			Stopwatch stopwatch = SimonManager.getStopwatch(NAME);
			for (int i = 1; i <= threads; i++) {
				startTask();
				if (i % 500 == 0) {
					System.out.println("Created thread: " + i +
						" (already executed loops " + stopwatch.getCounter() +
						", currently active " + stopwatch.getActive() + ")");
				}
			}
			System.out.println("All threads created (already executed loops " + stopwatch.getCounter() +
				", currently active " + stopwatch.getActive() + ")");
			// here we wait for all threads to end
			latch.await();
			System.out.println("All threads finished: " + stopwatch.sample());
		}

		private void startTask() {
			if (executorService == null) {
				new Thread(task).start();
			} else {
				executorService.submit(task);
			}
		}

		class TestTask implements Runnable {
			/** Run method implementing the code performed by the thread. */
			@Override
			public void run() {
				for (int i = 0; i < loop; i++) {
					Stopwatch stopwatch = SimonManager.getStopwatch(NAME);
					try (Split ignored = stopwatch.start()) {
						StopwatchSample sample = stopwatch.sample();
						//noinspection ResultOfMethodCallIgnored
						sample.toString();
					}
				}
				// signal to latch that the thread is finished
				latch.countDown();
			}
		}
	}
}