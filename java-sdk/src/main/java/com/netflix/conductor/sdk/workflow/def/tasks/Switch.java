package com.netflix.conductor.sdk.workflow.def.tasks;

import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import java.util.*;
import java.util.function.Function;

/**
 * Switch Task
 */
public class Switch extends Task {

    public static final String VALUE_PARAM_NAME = "value-param";

    public static final String JAVASCRIPT_NAME = "javascript";

    private String caseExpression;

    private boolean useJavascript;

    private List<Task> defaultTasks = new ArrayList<>();
    
    private Map<String, List<Task>> branches = new HashMap<>();

    private WorkerTask decisionMakerTask;

    private String decisionMakerTaskName;

    public Switch(String taskReferenceName, String caseExpression) {
        super(taskReferenceName, TaskType.SWITCH);
        this.caseExpression = caseExpression;
    }

    public <T> Switch(String taskReferenceName, Function<T, String> decisionMaker) {
        super(taskReferenceName, TaskType.SWITCH);
        this.decisionMakerTaskName = "_decide_" + taskReferenceName;
        this.caseExpression = "${" + decisionMakerTaskName + ".output.branch}";

        decisionMakerTask = new WorkerTask<T>("_decide_" + taskReferenceName, input -> {
            String selected = decisionMaker.apply(input);
            Map<String, String> output = new HashMap<>();
            output.put("branch", selected);
            return output;
        });
    }

    public Switch useJavascript() {
        useJavascript = true;
        return this;
    }

    public Switch defaultCase(Task... tasks) {
        defaultTasks = Arrays.asList(tasks);
        return this;
    }

    public Switch defaultCase(String... workerTasks) {
        for(String workerTask : workerTasks) {
            this.defaultTasks.add(new SimpleTask(workerTask, workerTask));
        }
        return this;
    }

    public Switch switchCase(String caseValue, Task... tasks) {
        branches.put(caseValue, Arrays.asList(tasks));
        return this;
    }


    public Switch switchCase(String caseValue, String... workerTasks) {
        List<Task> tasks = new ArrayList<>(workerTasks.length);
        int i = 0;
        for(String workerTask : workerTasks) {
            tasks.add(new SimpleTask(workerTask, workerTask));
        }
        branches.put(caseValue, tasks);
        return this;
    }

    public List<Task> getDefaultTasks() {
        return defaultTasks;
    }

    public Map<String, List<Task>> getBranches() {
        return branches;
    }

    @Override
    public List<WorkflowTask> getWorkflowDefTasks() {

        WorkflowTask switchTaskDef = toWorkflowTask();

        List<WorkflowTask> switchTasks = new ArrayList<>();
        switchTasks.add(switchTaskDef);

        if(decisionMakerTask != null) {
            useJavascript = false;
            List<WorkflowTask> decisionMakerWorkflowDefs = decisionMakerTask.getWorkflowDefTasks();
            switchTasks.add(0, decisionMakerWorkflowDefs.get(0));
        }

        if(useJavascript) {
            switchTaskDef.setEvaluatorType(JAVASCRIPT_NAME);
            switchTaskDef.setExpression(caseExpression);

        } else {
            switchTaskDef.setEvaluatorType(VALUE_PARAM_NAME);
            switchTaskDef.getInputParameters().put("switchCaseValue", caseExpression);
            switchTaskDef.setExpression("switchCaseValue");
        }

        Map<String, List<WorkflowTask>> decisionCases = new HashMap<>();
        branches.entrySet().forEach(entry -> {
            String decisionCase = entry.getKey();
            List<Task> decisionTasks = entry.getValue();
            List<WorkflowTask> decionTaskDefs = new ArrayList<>(decisionTasks.size());
            for (Task decisionTask : decisionTasks) {
                decionTaskDefs.addAll(decisionTask.getWorkflowDefTasks());
            }
            decisionCases.put(decisionCase, decionTaskDefs);
        });

        switchTaskDef.setDecisionCases(decisionCases);
        List<WorkflowTask> defaultCaseTaskDefs = new ArrayList<>(defaultTasks.size());
        for (Task defaultTask : defaultTasks) {
            defaultCaseTaskDefs.addAll(defaultTask.getWorkflowDefTasks());
        }
        switchTaskDef.setDefaultCase(defaultCaseTaskDefs);

        return switchTasks;
    }

    @Override
    public List<WorkerTask> getWorkerExecutedTasks() {
        List<WorkerTask> workerExecutedTasks = new ArrayList<>();
        if(decisionMakerTask != null) {
            workerExecutedTasks.add(decisionMakerTask);
        }
        branches.entrySet().forEach(entry -> {
            List<Task> decisionTasks = entry.getValue();
            for (Task decisionTask : decisionTasks) {
                workerExecutedTasks.addAll(decisionTask.getWorkerExecutedTasks());
            }
        });
        for (Task defaultTask : defaultTasks) {
            workerExecutedTasks.addAll(defaultTask.getWorkerExecutedTasks());
        }
        return workerExecutedTasks;
    }


}
