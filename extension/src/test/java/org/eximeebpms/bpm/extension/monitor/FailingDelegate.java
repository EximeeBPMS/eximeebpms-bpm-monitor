package org.eximeebpms.bpm.extension.monitor;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.JavaDelegate;

public class FailingDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        throw new RuntimeException("simulated job failure");
    }
}
