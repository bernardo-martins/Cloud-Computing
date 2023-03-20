package scc.serverless;

import com.microsoft.azure.functions.annotation.*;
import scc.cache.RedisLayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class TimerFunction {
	// removes every 24 hours
    @FunctionName("periodic-removal")
    public void cosmosFunction(@TimerTrigger(name = "periodicRemoval", schedule = "24 * * * * * *") String timerInfo,
            ExecutionContext context) throws JsonProcessingException {
        RedisLayer.deleteResource("activity", 10);
    }
}
