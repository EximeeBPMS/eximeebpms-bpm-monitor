package org.eximeebpms.bpm.extension.monitor;

import org.eximeebpms.bpm.engine.delegate.DelegateExecution;
import org.eximeebpms.bpm.engine.delegate.JavaDelegate;
import org.eximeebpms.bpm.engine.impl.cfg.TransactionState;
import org.eximeebpms.bpm.engine.impl.context.Context;
import org.eximeebpms.bpm.engine.RuntimeService;

public class IncidentCreator implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        execution.createIncident("incident", "incident", "incident");
        RuntimeService runtimeService = execution.getProcessEngine().getRuntimeService();
        Context.getCommandContext().getTransactionContext().addTransactionListener(TransactionState.COMMITTED,
                commandContext -> {
                    runtimeService.suspendProcessInstanceById(execution.getProcessInstanceId());
                });
    }

}
