# appTimer
This WebObjects frameworks allows one to create tasks, schedule them (in a particular way) and run them via SingleThreadExecutor.
See the <a href="https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html">ExecutorService</a> class for this.
The framework is agnostic about the class that it is running, requiring only that it have a run() method.
