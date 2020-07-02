# MultiThreading examples
The project covers different cases of multi-threading in java.

## Description

The project contains a lot of cases to work with multi-threading in old/new java version.

### SynchronizedMethodTest

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/synchronizedmethod/SynchronizedMethodTest.java#L32">SynchronizedMethodTest#testNotSynchronizedMethod</a> test creates two threads with different delays in runnable object.

Two threads call not synchronized method to increase the same value. As a result we should get a result with not ordered [1..30] range.
It means the logic is not thread safe.


<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/synchronizedmethod/SynchronizedMethodTest.java#L89">SynchronizedMethodTest#testSynchronizedMethod</a> test creates two threads with different delays in runnable object.

Two threads call synchronized method to increase the same value. As a result we get a proper result with [1..30] range in thread safe logic.

### VolatileVariableTest

Volatile member is shared in main memory that consumes extra memory. Also it's not easy to debug a program with Volatile members if any problem occurs.

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/sharedvariable/VolatileVariableTest.java#L29">VolatileVariableTest#testSharedVariable</a> test creates Server instance with isRunning volatile variable. According to the variable we can start or stop Server thread. 

### Object/Class level lock

#### Object level lock
<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/lockobject/ObjectLevelLockTest.java">ObjectLevelLockTest</a> test demonstrates how to work with Object level lock:

* synchronized method
* synchronized block in method
* synchronized object lock in method. 

#### Class level lock

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/lockobject/ClassLevelLockTest.java">ClassLevelLockTest</a> test demonstrates how to lock static methods on Class level.


### Wait/Notify/NotifyAll operations in thread

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/waitnotify/WaitNotifyThreadTest.java">WaitNotifyThreadTest</a> test demonstrates how to work with wait/notify/notifyAll methods.

**Explanation**:
* "testWaitNotify" method starts Producer thread and two Consumer threads;
* Consumer threads wait Producer to notify when Message POJO is ready to consume;
* Producer, after a delay, updates Message POJO and notifies all other threads to proceed;
* Consumers wake up and get Message POJO filled in Consumer.
* Pay attention that Message POJO is synchronized in all wait/notify/notifyAll methods. 

### UncaughtExceptionHandler

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/exceptionhandler/ThreadUncaughtExceptionHandlerTest.java">ThreadUncaughtExceptionHandlerTest</a> test demonstrates how to catch an exception in thread.


### Interrupt thread

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/threadinterrupt/ThreadInterruptTest.java">ThreadInterruptTest</a> tests different cases when we interrupt (send signal to) a thread and how the thread works with InterruptedException.

### Atomic operation

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/atomic/TharedSafeCounterTest.java">TharedSafeCounterTest</a> test works with counter variable in safe mode using synchronized method and AtomicInteger instance. 


### Count down threads

Definition : According to Oracle docs,
* CountDownLatch  is a synchronization aid that allows one or more threads to wait until a set of operations being performed in other threads completes;
* CyclicBarrier is a synchronization aid that allows a set of threads to all wait for each other to reach a common barrier point.


#### CountDownLatch
<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/countdown/CountDownLatchTest.java#L24">CountDownLatchTest#testCountDown</a> test creates 3 threads and controls all of them in CountDownLatch instance to wait each other.

#### CyclicBarrier

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/countdown/CyclicBarrierTest.java#L26">CountDownLatchTest#testCyclicBarrier</a> test creates 3 threads and synchronize them in barrier to all wait for each other to reach a common barrier point.

## Build

Clone and install <a href="https://github.com/StepanMelnik/Parent.git">Parent</a> project before building.

Clone current project.

### Maven
	> mvn clean test


