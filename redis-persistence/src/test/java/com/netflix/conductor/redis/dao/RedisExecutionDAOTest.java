/*
 * Copyright 2022 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.conductor.redis.dao;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.redis.jedis.JedisStandalone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.netflix.conductor.common.config.TestObjectMapperConfiguration;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.core.config.ConductorProperties;
import com.netflix.conductor.dao.ExecutionDAO;
import com.netflix.conductor.dao.ExecutionDAOTest;
import com.netflix.conductor.model.TaskModel;
import com.netflix.conductor.redis.config.RedisProperties;
import com.netflix.conductor.redis.jedis.JedisMock;
import com.netflix.conductor.redis.jedis.JedisProxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.commands.JedisCommands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TestObjectMapperConfiguration.class})
@RunWith(SpringRunner.class)
public class RedisExecutionDAOTest extends ExecutionDAOTest {

    private static GenericContainer redis =
            new GenericContainer(DockerImageName.parse("redis:6.2.6-alpine"))
                    .withExposedPorts(6379);


    private RedisExecutionDAO executionDAO;

    @Autowired private ObjectMapper objectMapper;

    @Before
    public void init() {

        redis.start();

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(2);
        config.setMaxTotal(10);

        JedisPool jedisPool = new JedisPool(config, redis.getHost(), redis.getFirstMappedPort());

        ConductorProperties conductorProperties = mock(ConductorProperties.class);
        RedisProperties properties = mock(RedisProperties.class);
        when(properties.getEventExecutionPersistenceTTL()).thenReturn(Duration.ofSeconds(5));
        JedisStandalone standalone = new JedisStandalone(jedisPool);
        executionDAO = new RedisExecutionDAO(new JedisProxy(standalone), objectMapper, conductorProperties, properties);
    }

    @Test
    public void testTaskUpdate() {
        String taskId = UUID.randomUUID().toString();
        String workflowId = UUID.randomUUID().toString();
        String taskDefName = "simple_task_0";

        TaskModel task = new TaskModel();
        task.setTaskId(taskId);
        task.setWorkflowInstanceId(workflowId);
        task.setReferenceTaskName("ref_name");
        task.setTaskDefName(taskDefName);
        task.setTaskType(taskDefName);
        task.setWorkflowTask(new WorkflowTask());
        task.getWorkflowTask().setAsyncComplete(false);
        task.setStatus(TaskModel.Status.IN_PROGRESS);
        List<TaskModel> tasks = executionDAO.createTasks(Collections.singletonList(task));
        assertEquals(1, tasks.size());
        TaskModel fromDAO = executionDAO.getTask(taskId);
        assertNotNull(fromDAO);
        assertEquals(task.getTaskId(), fromDAO.getTaskId());

        task.setStatus(TaskModel.Status.COMPLETED);
        executionDAO.updateTask(task);
        fromDAO = executionDAO.getTask(taskId);
        assertNotNull(fromDAO);
        assertEquals(task.getTaskId(), fromDAO.getTaskId());
        assertEquals(task.getStatus(), fromDAO.getStatus());
        assertEquals(TaskModel.Status.COMPLETED, fromDAO.getStatus());

        //Let's try to update the task back to in progress
        task.setStatus(TaskModel.Status.IN_PROGRESS);
        executionDAO.updateTask(task);
        fromDAO = executionDAO.getTask(taskId);
        assertNotNull(fromDAO);
        assertEquals(task.getTaskId(), fromDAO.getTaskId());
        assertEquals(task.getStatus(), fromDAO.getStatus());
        //The task moves back to in progress since its not async complete
        assertEquals(TaskModel.Status.IN_PROGRESS, fromDAO.getStatus());

        task.setStatus(TaskModel.Status.COMPLETED);
        task.getWorkflowTask().setAsyncComplete(true);
        executionDAO.updateTask(task);

        fromDAO = executionDAO.getTask(taskId);
        assertNotNull(fromDAO);
        assertEquals(task.getTaskId(), fromDAO.getTaskId());
        assertEquals(TaskModel.Status.COMPLETED, fromDAO.getStatus());

        //Now, let's mark the task as in progress while its marked as async omplete
        task.setStatus(TaskModel.Status.IN_PROGRESS);
        executionDAO.updateTask(task);
        fromDAO = executionDAO.getTask(taskId);
        assertNotNull(fromDAO);
        assertEquals(task.getTaskId(), fromDAO.getTaskId());
        assertNotEquals(task.getStatus(), fromDAO.getStatus());
        //The task status does not change
        assertEquals(TaskModel.Status.COMPLETED, fromDAO.getStatus());

    }

    @Test
    public void testCorrelateTaskToWorkflowInDS() {
        String workflowId = "workflowId";
        String taskId = "taskId1";
        String taskDefName = "task1";

        TaskDef def = new TaskDef();
        def.setName("task1");
        def.setConcurrentExecLimit(1);

        TaskModel task = new TaskModel();
        task.setTaskId(taskId);
        task.setWorkflowInstanceId(workflowId);
        task.setReferenceTaskName("ref_name");
        task.setTaskDefName(taskDefName);
        task.setTaskType(taskDefName);
        task.setStatus(TaskModel.Status.IN_PROGRESS);
        List<TaskModel> tasks = executionDAO.createTasks(Collections.singletonList(task));
        assertNotNull(tasks);
        assertEquals(1, tasks.size());

        executionDAO.correlateTaskToWorkflowInDS(taskId, workflowId);
        tasks = executionDAO.getTasksForWorkflow(workflowId);
        assertNotNull(tasks);
        assertEquals(workflowId, tasks.get(0).getWorkflowInstanceId());
        assertEquals(taskId, tasks.get(0).getTaskId());
    }

    @Override
    protected ExecutionDAO getExecutionDAO() {
        return executionDAO;
    }
}
