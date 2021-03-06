# MultiThreading (Concurrency) examples
The project covers different cases of multi-threading in java.

## Description

The project contains a lot of cases to work with multi-threading in old/new java version.

Also see <a href="https://github.com/StepanMelnik/MultiThreading_BestPractice">MultiThreading Best Practice</a> project with some suggestions to work with multi threading in java.

_________

The following cases covered in unit tests:
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#synchronizedmethodtest">SynchronizedMethod</a> case describes how to properly increase a value in synchronized method by a few threads;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#volatilevariabletest">VolatileVariable</a> case demonstrates how to use a global variable between threads;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#objectclass-level-lock">Object/Class level lock</a> case demonstrates how to lock an object on the Method/Class level;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#waitnotifynotifyall-operations-in-thread">Wait/Notify/NotifyAll operations in thread</a> case demonstrates how to work with **wait**, **notify** and **notifyAll** methods in the threads to communicate;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#uncaughtexceptionhandler">Uncaught Exception Handler</a> case shows how to catch an error in a thread;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#interrupt-thread">Interrupt thread</a> case demonstrates what method can be interrupted in the thread and how to catch the error;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#atomic-operation">Atomic operation</a> case compares how to work with counter variable in safe mode using synchronized method and AtomicInteger instance;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#count-down-threads">Count down threads</a> case describes how CountDownLatch and CyclicBarrier solutions work;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#consumer-producer">Consumer-Producer</a> case shows how to organize Consumer-Producer system using FIFO and LIFO queue;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#reentrantlock">ReentrantLock</a> cases show how to work with ReentrantLock, ReentrantReadWriteLock and lock conditions;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#stampedlock">StampedLock</a> cases demonstrate StampedLock features;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#semaphore">Semaphore</a> cases show how Semaphore and Mutex as Semaphore work;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#executorservice">ExecutorServiceTest</a> test demonstrates {@link ExecutorService} features;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#fork-join">Fork-Join</a> cases demonstrates how RecursiveAction and RecursiveTask work in Fork-Join framework;
* <a href="https://github.com/StepanMelnik/MultiThreading_Examples#completablefuture">CompletableFuture</a> cases shows different cases to work with {@link CompletableFuture} features;


_________

### SynchronizedMethodTest

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/synchronizedmethod/SynchronizedMethodTest.java#L32">SynchronizedMethodTest#testNotSynchronizedMethod</a> test creates two threads with different delays in runnable object.

Two threads call not synchronized method to increase the same value. As a result we should get a result with not ordered [1..30] range.
It means the logic is not thread safe.


<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/synchronizedmethod/SynchronizedMethodTest.java#L83">SynchronizedMethodTest#testSynchronizedMethod</a> test creates two threads with different delays in runnable object.

Two threads call synchronized method to increase the same value. As a result we get a proper result with [1..30] range in thread safe logic.

### VolatileVariableTest

Volatile member is shared in main memory that consumes extra memory. Also it's not easy to debug a program with Volatile members if any problem occurs.

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/sharedvariable/VolatileVariableTest.java#L29">VolatileVariableTest#testSharedVariable</a> test creates Server instance with isRunning volatile variable. According to the variable we can start or stop Server thread. 

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/sharedvariable/VolatileIncrementVariableTest.java">VolatileIncrementVariableTest</a> test shows that Volatile variables share the visibility features of synchronized, but none of the atomicity features.


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

### Consumer-Producer

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/consumerproducer/ConsumerProducerFifoTest.java">ConsumerProducerFifoTest</a> test demonstrates how to work with Consumer and Producer using {@link BlockingArray}.

This queue orders elements FIFO (first-in-first-out).

{@link ArrayBlockingQueue} works with final predefined capacity. The main Lock based on ReentrantLock implementation with notEmpty and notFull conditions.

    Compare the solution with low level implementation in {@link WaitNotifyThreadTest}.

### ReentrantLock

#### ReentrantLock

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/reentrantlock/ReentrantLockTest.java">ReentrantLockTest</a> test demonstrates how to work with {@link ReentrantLock#lock} and {@link ReentrantLock#tryLock(long, TimeUnit)} (no synchronized method required).

#### ReentrantReadWriteLock

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/reentrantlock/ReentrantReadWriteLockTest.java">ReentrantReadWriteLockTest</a> test demonstrates why HashMap is not thread safe. Also the test works with {@link ReentrantReadWriteLockTest#HashMapDecorator} that allows to use read/write locking. {@link HashMapDecorator#putAndGet} works with lock to put/get a value in HashMap. 

#### ReentrantLockWithCondition

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/reentrantlock/ReentrantLockWithConditionTest.java">ReentrantLockWithConditionTest</a> test demonstrates how to work with conditions in the lock.

### StampedLock

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/stampedlock/StampedLockTest.java">StampedLockTest</a> test demonstrates StampedLock features.


### Semaphore

#### SemaphoreTest
<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/semaphore/SemaphoreTest.java">SemaphoreTest</a> test demonstrates how semaphore works with predefined permits.

#### SemaphoreAsMutexTest
<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/semaphore/SemaphoreAsMutexTest.java">SemaphoreAsMutexTest</a> test demonstrates how to implement Mutex using semaphore.

### ExecutorService

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/executorservice/ExecutorServiceTest.java">ExecutorServiceTest</a> test demonstrates {@link ExecutorService} features.

### Fork-Join

#### RecursiveNodeForkJoinTest
<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/forkjoin/RecursiveNodeForkJoinTest.java">RecursiveNodeForkJoinTest</a> test shows how to calculate a sum in the tree of nodes.

#### RecursiveNodeForkJoinTest
<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/forkjoin/RecursiveNodeForkJoinTest.java">RecursiveNodeForkJoinTest</a> test shows how RecursiveAction and RecursiveTask work in Fork-Join framework.


### CompletableFuture
<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/completablefuture/CompletableFutureTest.java">CompletableFutureTest</a> test shows different cases to work with {@link CompletableFuture} features.

### Guava Striped locking 
<a href="https://github.com/StepanMelnik/MultiThreading_Examples/blob/master/src/test/java/com/sme/multithreading/guava/StrippedLockTest.java">StrippedLockTest</a> test compares ReentrantLock with <a href="https://github.com/google/guava/wiki/StripedExplained">Striped</a> implementation in Guava.


## Build

Clone and install <a href="https://github.com/StepanMelnik/Parent.git">Parent</a> project before building.

Clone current project.

### Maven
	> mvn clean test


