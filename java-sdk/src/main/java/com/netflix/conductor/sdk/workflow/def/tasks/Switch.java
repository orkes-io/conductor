package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import java.util.*;
import java.util.function.Function;

public class Switch extends BaseWorkflowTask {

    public static final String VALUE_PARAM_NAME = "value-param";

    public static final String JAVASCRIPT_NAME = "javascript";

    private String caseExpression;

    private boolean useJavascript;

    private List<BaseWorkflowTask> defaultTasks = new ArrayList<>();
    
    private Map<String, List<BaseWorkflowTask>> branches = new HashMap<>();

    private Function<Object, String> decisionMaker;

    private String decisionMakerTaskName;

    public Switch(String taskReferenceName, String caseExpression) {
        super(taskReferenceName, TaskType.SWITCH);
        this.caseExpression = caseExpression;
    }

    public Switch(String taskReferenceName, Function<Object, String> decisionMaker) {
        super(taskReferenceName, TaskType.SWITCH);
        this.decisionMakerTaskName = "decide_" + taskReferenceName;
        this.decisionMaker = decisionMaker;
        this.caseExpression = "${" + decisionMakerTaskName + ".output.branch}";
    }

    public Switch useJavascript() {
        useJavascript = true;
        return this;
    }

    public Switch defaultCase(BaseWorkflowTask... tasks) {
        defaultTasks = Arrays.asList(tasks);
        return this;
    }

    public Switch defaultCase(String... workerTasks) {
        for(String workerTask : workerTasks) {
            this.defaultTasks.add(new WorkerTask(workerTask));
        }
        return this;
    }

    public Switch switchCase(String caseValue, BaseWorkflowTask... tasks) {
        branches.put(caseValue, Arrays.asList(tasks));
        return this;
    }


    public Switch switchCase(String caseValue, String... workerTasks) {
        List<BaseWorkflowTask> tasks = new ArrayList<>(workerTasks.length);
        for(String workerTask : workerTasks) {
            tasks.add(new WorkerTask(workerTask));
        }
        branches.put(caseValue, tasks);
        return this;
    }

    public List<BaseWorkflowTask> getDefaultTasks() {
        return defaultTasks;
    }

    public Map<String, List<BaseWorkflowTask>> getBranches() {
        return branches;
    }

    @Override
    public List<WorkflowTask> updateWorkflowTask(WorkflowTask workflowTask) {
        List<WorkflowTask> switchTasks = new ArrayList<>(2);

        if(decisionMaker != null) {
            useJavascript = false;
            /*
            SimpleWorkerTask.create(decisionMakerTaskName, o -> {
                String selected = decisionMaker.apply(o);
                Map<String, String> output = new HashMap<>();
                output.put("branch", selected);
                return output;
            });

             */
            List<WorkflowTask> decisionMakerTask = new WorkerTask(decisionMakerTaskName).toWorkflowTask();
            switchTasks.addAll(decisionMakerTask);      //Add a task for generating the decision
        }

        if(useJavascript) {

            workflowTask.setEvaluatorType(JAVASCRIPT_NAME);
            workflowTask.setExpression(caseExpression);

        } else {
            workflowTask.setEvaluatorType(VALUE_PARAM_NAME);
            workflowTask.getInputParameters().put("switchCaseValue", caseExpression);
            workflowTask.setExpression("switchCaseValue");
        }

        Map<String, List<WorkflowTask>> decisionCases = new HashMap<>();
        branches.entrySet().forEach(entry -> {
            String decisionCase = entry.getKey();
            List<BaseWorkflowTask> tasks = entry.getValue();
            List<WorkflowTask> workflowTasks = new ArrayList<>(tasks.size());
            tasks.forEach(task -> workflowTasks.addAll(task.toWorkflowTask()));
            decisionCases.put(decisionCase, workflowTasks);
        });

        workflowTask.setDecisionCases(decisionCases);
        List<WorkflowTask> defaultCases = new ArrayList<>(defaultTasks.size());
        defaultTasks.forEach(task -> defaultCases.addAll(task.toWorkflowTask()));
        workflowTask.setDefaultCase(defaultCases);

        switchTasks.add(workflowTask);


        return switchTasks;
    }
}
