package com.arctel.oms.domain.dto;

import com.arctel.oms.domain.OmsJob;
import com.arctel.oms.support.JobSupport;
import jakarta.annotation.Resource;

public class JobRunnable implements Runnable {
    OmsJob omsJob;

    public JobRunnable(OmsJob omsJob) {
        this.omsJob = omsJob;
    }

    public void taskRun() {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Override
    public void run() {
        taskRun();
    }
}
