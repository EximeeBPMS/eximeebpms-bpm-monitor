package org.eximeebpms.bpm.extension.monitor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({MonitorHistoryListener.class, SnapshotMonitors.class, ScriptViolationMeterListener.class, ScriptViolationSnapshotMonitor.class})
public class MonitoringAutoConfiguration {
}
