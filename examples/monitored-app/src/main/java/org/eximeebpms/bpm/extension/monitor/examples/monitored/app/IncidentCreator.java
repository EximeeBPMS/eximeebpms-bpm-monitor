package org.eximeebpms.bpm.extension.monitor.examples.monitored.app;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.JavaDelegate;

public class IncidentCreator implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        execution.createIncident("incident", "incident", "incident");
    }

}
