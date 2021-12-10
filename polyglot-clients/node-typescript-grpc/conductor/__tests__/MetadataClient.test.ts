import * as grpc from "@grpc/grpc-js";
import * as metadataClient from "../src/MetadataClient";
import * as metadata from "../proto/service/metadata_service_pb";
import { MetadataServiceClient } from "../proto/service/metadata_service_grpc_pb";
import { TaskDef } from "../proto/model/taskdef_pb";
import { WorkflowDef } from "../proto/model/workflowdef_pb";

type cb_createTasks = (
  error: grpc.ServiceError | null,
  response: metadata.CreateTasksResponse
) => void;
type cb_createWorkflow = (
  error: grpc.ServiceError | null,
  response: metadata.CreateWorkflowResponse
) => void;
type cb_deleteTask = (
  error: grpc.ServiceError | null,
  response: metadata.DeleteTaskResponse
) => void;
type cb_getTask = (
  error: grpc.ServiceError | null,
  response: metadata.GetTaskResponse
) => void;
type cb_updateTask = (
  error: grpc.ServiceError | null,
  response: metadata.UpdateTaskResponse
) => void;
type cb_updateWorkflows = (
  error: grpc.ServiceError | null,
  response: metadata.UpdateWorkflowsResponse
) => void;

jest.mock("../proto/service/metadata_service_grpc_pb", () => {
  // Works and lets you check for constructor calls:
  return {
    MetadataServiceClient: jest.fn().mockImplementation(() => {
      return {
        createTasks: (req: metadata.CreateTasksRequest, cb: cb_createTasks) => {
          cb(null, new metadata.CreateTasksResponse());
        },
        createWorkflow: (
          req: metadata.CreateTasksRequest,
          cb: cb_createWorkflow
        ) => {
          cb(null, new metadata.CreateWorkflowResponse());
        },
        deleteTask: (req: metadata.CreateTasksRequest, cb: cb_deleteTask) => {
          cb(null, new metadata.DeleteTaskResponse());
        },
        getTask: (req: metadata.CreateTasksRequest, cb: cb_getTask) => {
          cb(null, new metadata.GetTaskResponse());
        },
        updateTask: (req: metadata.CreateTasksRequest, cb: cb_updateTask) => {
          cb(null, new metadata.UpdateTaskResponse());
        },
        updateWorkflows: (
          req: metadata.CreateTasksRequest,
          cb: cb_updateWorkflows
        ) => {
          cb(null, new metadata.UpdateWorkflowsResponse());
        },
      };
    }),
  };
});

test("createTasks", () => {
  const client = new metadataClient.MetadataClient("localhost:0000");
  const callback = jest.fn();
  client.createTasks(callback, new TaskDef());
  expect(callback).toHaveBeenCalled();
});

test("createWorkflow", () => {
  const client = new metadataClient.MetadataClient("localhost:0000");
  const callback = jest.fn();
  client.createWorkflow(callback, "111");
  expect(callback).toHaveBeenCalled();
});

test("deleteTask", () => {
  const client = new metadataClient.MetadataClient("localhost:0000");
  const callback = jest.fn();
  client.deleteTask(callback, "1");
  expect(callback).toHaveBeenCalled();
});

test("getTask", () => {
  const client = new metadataClient.MetadataClient("localhost:0000");
  const callback = jest.fn();
  client.getTask(callback, "metadata_1");
  expect(callback).toHaveBeenCalled();
});

test("updateTask", () => {
  const client = new metadataClient.MetadataClient("localhost:0000");
  const callback = jest.fn();
  client.updateTask(callback, new TaskDef());
  expect(callback).toHaveBeenCalled();
});

test("updateWorkflows", () => {
  const client = new metadataClient.MetadataClient("localhost:0000");
  const callback = jest.fn();
  client.updateWorkflows(callback, new WorkflowDef());
  expect(callback).toHaveBeenCalled();
});
