# MultiThreading example
The project covers different cases of multi-threading in java.

## Description

TODO

### SynchronizedMethodTest

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/SynchronizedMethodTest.java">SynchronizedMethodTest#testNotSynchronizedMethod</a> test creates two threads with different delays in runnable object.

Two threads call not synchronized method to increase the same value. As a result we should get a result with not ordered [1..30] range.
It means the logic is not thread safe.


<a href="https://github.com/StepanMelnik/MultiThreading_Examples/SynchronizedMethodTest.java">SynchronizedMethodTest#testSynchronizedMethod</a> test creates two threads with different delays in runnable object.

Two threads call synchronized method to increase the same value. As a result we get a proper result with [1..30] range in thread safe logic.

### VolatileVariableTest

Volatile member is shared in main memory that consumes extra memory. Also it's not easy to debug a program with Volatile members if any problem occurs.

<a href="https://github.com/StepanMelnik/MultiThreading_Examples/VolatileVariableTest.java">VolatileVariableTest#testSharedVariable</a> test creates Server instance with isRunning volatile variable. According to the variable we can start or stop Server thread. 


