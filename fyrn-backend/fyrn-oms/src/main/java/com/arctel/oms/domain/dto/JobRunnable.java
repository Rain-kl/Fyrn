package com.arctel.oms.domain.dto;

import com.arctel.oms.domain.OmsJob;
import com.arctel.oms.support.ThreadPoolJobSupport;

public abstract class JobRunnable {

    protected OmsJob omsJob;

    ThreadPoolJobSupport threadPoolJobSupport;

    public JobRunnable(ThreadPoolJobSupport threadPoolJobSupport) {
        this.threadPoolJobSupport = threadPoolJobSupport;
    }

    protected abstract void taskRun();

    public void run(OmsJob omsJob) {
        this.omsJob = omsJob;
        taskRun();
    }
}