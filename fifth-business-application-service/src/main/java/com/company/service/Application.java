package com.company.service;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.services.jbpm.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application  {	
    @Autowired
    private ProcessService processService;
    @Autowired
    private RuntimeDataService runtimeDataService;
    @Autowired
    private UserTaskService userTaskService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
 
    @GetMapping("/evaluation")
    public ResponseEntity<Long> startEvaluation(Principal principal, @RequestParam String employee) throws Exception {
    	Long processInstanceId = -1L;
    	if ( principal != null ) {
	    	Map<String, Object> vars = new HashMap<>();
	    	vars.put("employee", employee);
	    	processInstanceId = processService.startProcess("Evaluation_1.0.0-SNAPSHOT", "Evaluation.Evaluation", vars);
    	}
    	return ResponseEntity.ok(processInstanceId);
    }

    @GetMapping("/selfeval")
    public ResponseEntity<Integer> selfEvaluation(Principal principal, @RequestParam String selfeval) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("selfeval", selfeval);
    	List<TaskSummary> taskSummaries = runtimeDataService.getTasksAssignedAsPotentialOwner(principal.getName(), new QueryFilter());
    	taskSummaries.forEach(s->{
    		userTaskService.start(s.getId(), principal.getName());
    		userTaskService.complete(s.getId(), principal.getName(), params);
    	});
    	return ResponseEntity.ok(taskSummaries.size());
    }

    @GetMapping("/hreval")
    public ResponseEntity<Integer> hrEvaluation(Principal principal, @RequestParam String hreval) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("hreval", hreval);
    	List<TaskSummary> taskSummaries = runtimeDataService.getTasksAssignedAsPotentialOwner(principal.getName(), new QueryFilter());
    	taskSummaries.forEach(s->{
    		userTaskService.claim(s.getId(), principal.getName());
    		userTaskService.start(s.getId(), principal.getName());
    		userTaskService.complete(s.getId(), principal.getName(), params);
    	});
    	return ResponseEntity.ok(taskSummaries.size());
    }

    @GetMapping("/pmeval")
    public ResponseEntity<Integer> pmEvaluation(Principal principal, @RequestParam String pmeval) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("pmeval", pmeval);
    	List<TaskSummary> taskSummaries = runtimeDataService.getTasksAssignedAsPotentialOwner(principal.getName(), new QueryFilter());
    	taskSummaries.forEach(s->{
    		userTaskService.claim(s.getId(), principal.getName());
    		userTaskService.start(s.getId(), principal.getName());
    		userTaskService.complete(s.getId(), principal.getName(), params);
    	});
    	return ResponseEntity.ok(taskSummaries.size());
    }

    @GetMapping("/instances")
    public ResponseEntity<List<VariableInstance>> instances(Principal principal, @RequestParam Long processInstanceId) throws Exception {
    	VariableInstanceList vi = ConvertUtils.convertToVariablesList(runtimeDataService.getVariablesCurrentState(processInstanceId));
    	return ResponseEntity.ok(Arrays.asList(vi.getVariableInstances()));
    }
    
}
