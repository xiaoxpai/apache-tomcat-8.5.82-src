/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.core;

import java.util.concurrent.TimeUnit;

import org.apache.catalina.Executor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.ResizableExecutor;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.TaskThreadFactory;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;

public class StandardThreadExecutor extends LifecycleMBeanBase
        implements Executor, ResizableExecutor {

    protected static final StringManager sm = StringManager.getManager(StandardThreadExecutor.class);

    // ---------------------------------------------- Properties
    /**
     * Default thread priority
     * 默认线程的优先级
     */
    protected int threadPriority = Thread.NORM_PRIORITY;

    /**
     * Run threads in daemon or non-daemon state
     * 守护线程
     */
    protected boolean daemon = true;

    /**
     * Default name prefix for the thread name
     * 线程名称的前缀
     */
    protected String namePrefix = "tomcat-exec-";

    /**
     * max number of threads
     * 最大线程数默认200个
     */
    protected int maxThreads = 200;

    /**
     * min number of threads
     * 最小空闲线程25个
     */
    protected int minSpareThreads = 25;

    /**
     * idle time in milliseconds
     * 超时时间为6000毫秒
     */
    protected int maxIdleTime = 60000;

    /**
     * The executor we use for this component
     * 线程池容器
     */
    protected ThreadPoolExecutor executor = null;

    /**
     * the name of this thread pool
     * 线程池名称
     */
    protected String name;

    /**
     * The maximum number of elements that can queue up before we reject them
     * 队列最大限度值
     */
    protected int maxQueueSize = Integer.MAX_VALUE;

    /**
     * After a context is stopped, threads in the pool are renewed. To avoid
     * renewing all threads at the same time, this delay is observed between 2
     * threads being renewed.
     * 为了避免在上下文停止之后，所有的线程在同一时间段被更新，所以进行线程的延迟操作
     */
    protected long threadRenewalDelay =
        org.apache.tomcat.util.threads.Constants.DEFAULT_THREAD_RENEWAL_DELAY;

    //任务队列
    private TaskQueue taskqueue = null;
    // ---------------------------------------------- Constructors
    public StandardThreadExecutor() {
        //empty constructor for the digester
    }


    // ---------------------------------------------- Public Methods

    /**
     * Start the component and implement the requirements
     * of {@link org.apache.catalina.util.LifecycleBase#startInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected void startInternal() throws LifecycleException {
        //1.实例化任务队列
        taskqueue = new TaskQueue(maxQueueSize);
        //自定义的线程池工厂类，实现了JDK的ThreadFactory接口
        TaskThreadFactory tf = new TaskThreadFactory(namePrefix,daemon,getThreadPriority());

       //这里的ThreadPoolExecutor是tomcat自定义的（并非是JDK所属的）
        executor = new ThreadPoolExecutor(getMinSpareThreads(), getMaxThreads(), maxIdleTime, TimeUnit.MILLISECONDS,taskqueue, tf);
        executor.setThreadRenewalDelay(threadRenewalDelay);
        //设置任务容器的父级线程池对象
        taskqueue.setParent(executor);

        //设置容器启动状态
        setState(LifecycleState.STARTING);
    }


    /**
     * 停止容器时的生命周期方法，关闭线程池和资源清理
     * Stop the component and implement the requirements
     * of {@link org.apache.catalina.util.LifecycleBase#stopInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    @Override
    protected void stopInternal() throws LifecycleException {

        setState(LifecycleState.STOPPING);
        if (executor != null) {
            executor.shutdownNow();
        }
        executor = null;
        taskqueue = null;
    }


    /**
     * 这个执行线程方法有超时操作，参考 org.apache.catalina.Executor 接口
     * @param command the runnable task
     * @param timeout the length of time to wait for the task to complete
     * @param unit    the units in which timeout is expressed
     *
     */
    @Override
    @Deprecated
    public void execute(Runnable command, long timeout, TimeUnit unit) {
        if (executor != null) {
            executor.execute(command,timeout,unit);
        } else {
            throw new IllegalStateException(sm.getString("standardThreadExecutor.notStarted"));
        }
    }


    /**
     * JDK默认操作线程的方法，参考java.util.concurrent.Executor 接口
     * @param command the runnable task
     */
    @Override
    public void execute(Runnable command) {
        if (executor != null) {
            // Note any RejectedExecutionException due to the use of TaskQueue
            // will be handled by the o.a.t.u.threads.ThreadPoolExecutor
            executor.execute(command);
        } else {
            throw new IllegalStateException(sm.getString("standardThreadExecutor.notStarted"));
        }
    }

    public void contextStopping() {
        if (executor != null) {
            executor.contextStopping();
        }
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public boolean isDaemon() {

        return daemon;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    @Override
    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
        if (executor != null) {
            executor.setKeepAliveTime(maxIdleTime, TimeUnit.MILLISECONDS);
        }
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        if (executor != null) {
            executor.setMaximumPoolSize(maxThreads);
        }
    }

    public void setMinSpareThreads(int minSpareThreads) {
        this.minSpareThreads = minSpareThreads;
        if (executor != null) {
            executor.setCorePoolSize(minSpareThreads);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxQueueSize(int size) {
        this.maxQueueSize = size;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public long getThreadRenewalDelay() {
        return threadRenewalDelay;
    }

    public void setThreadRenewalDelay(long threadRenewalDelay) {
        this.threadRenewalDelay = threadRenewalDelay;
        if (executor != null) {
            executor.setThreadRenewalDelay(threadRenewalDelay);
        }
    }

    // Statistics from the thread pool
    @Override
    public int getActiveCount() {
        return (executor != null) ? executor.getActiveCount() : 0;
    }

    public long getCompletedTaskCount() {
        return (executor != null) ? executor.getCompletedTaskCount() : 0;
    }

    public int getCorePoolSize() {
        return (executor != null) ? executor.getCorePoolSize() : 0;
    }

    public int getLargestPoolSize() {
        return (executor != null) ? executor.getLargestPoolSize() : 0;
    }

    @Override
    public int getPoolSize() {
        return (executor != null) ? executor.getPoolSize() : 0;
    }

    public int getQueueSize() {
        return (executor != null) ? executor.getQueue().size() : -1;
    }


    /**
     * 由于继承了 org.apache.tomcat.util.thread.ResizableExecutor 接口，所以可以重新定义线程池大小
     * @param corePoolSize 核心线程池大小
     * @param maximumPoolSize 最大核心线程池大小
     * @return
     */
    @Override
    public boolean resizePool(int corePoolSize, int maximumPoolSize) {
        if (executor == null) {
            return false;
        }

        executor.setCorePoolSize(corePoolSize);
        executor.setMaximumPoolSize(maximumPoolSize);
        return true;
    }


    @Override
    public boolean resizeQueue(int capacity) {
        return false;
    }


    @Override
    protected String getDomainInternal() {
        // No way to navigate to Engine. Needs to have domain set.
        return null;
    }

    @Override
    protected String getObjectNameKeyProperties() {
        return "type=Executor,name=" + getName();
    }
}
