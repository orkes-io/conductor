import * as grpc from "@grpc/grpc-js";
import * as workflowClient from "../src/WorkflowClient";
import * as workflow from "../proto/service/workflow_service_pb";
import { WorkflowServiceClient } from "../proto/service/workflow_service_grpc_pb";

type cb_getWorkflows = (
  error: grpc.ServiceError | null,
  response: workflow.GetWorkflowsResponse
) => void;
type cb_getWorkflowStatus = (
  error: grpc.ServiceError | null,
  response: workflow.GetWorkflowStatusResponse
) => void;
type cb_getRunningWorkflows = (
  error: grpc.ServiceError | null,
  response: workflow.GetRunningWorkflowsResponse
) => void;
type cb_startWorkflow = (
  error: grpc.ServiceError | null,
  response: workflow.StartWorkflowResponse
) => void;

jest.mock("../proto/service/workflow_service_grpc_pb", () => {
  // Works and lets you check for constructor calls:
  return {
    WorkflowServiceClient: jest.fn().mockImplementation(() => {
      return {
        getWorkflows: (
          req: workflow.GetWorkflowsRequest,
          cb: cb_getWorkflows
        ) => {
          cb(null, new workflow.GetWorkflowsResponse());
        },
        getWorkflowStatus: (
          req: workflow.GetWorkflowsRequest,
          cb: cb_getWorkflowStatus
        ) => {
          cb(null, new workflow.GetWorkflowStatusResponse());
        },
        getRunningWorkflows: (
          req: workflow.GetWorkflowsRequest,
          cb: cb_getRunningWorkflows
        ) => {
          cb(null, new workflow.GetRunningWorkflowsResponse());
        },
        startWorkflow: (
          req: workflow.GetWorkflowsRequest,
          cb: cb_startWorkflow
        ) => {
          cb(null, new workflow.StartWorkflowResponse());
        },
      };
    }),
  };
});

test("getWorkflows", () => {
  const client = new workflowClient.WorkflowClient("localhost:0000");
  const callback = jest.fn();
  client.getWorkflows(callback);
  expect(callback).toHaveBeenCalledWith({ message: null }, { value: [] });
});

test("getQueueInfo", () => {
  const client = new workflowClient.WorkflowClient("localhost:0000");
  const callback = jest.fn();
  client.getWorkflowStatus(callback, "111", false);
  expect(callback).toHaveBeenCalledWith(
    { message: null },
    { value: undefined }
  );
});

test("getTask", () => {
  const client = new workflowClient.WorkflowClient("localhost:0000");
  const callback = jest.fn();
  client.getRunningWorkflows(callback, "1");
  expect(callback).toHaveBeenCalledWith({ message: null }, { value: [] });
});

test("updateTask", () => {
  const client = new workflowClient.WorkflowClient("localhost:0000");
  const callback = jest.fn();
  client.startWorkflow(callback, "workflow_1");
  expect(callback).toHaveBeenCalledWith({ message: null }, { value: "" });
});
