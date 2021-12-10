import * as grpc from "@grpc/grpc-js";
import * as net from "./common";
import { WorkflowServiceClient } from "../proto/service/workflow_service_grpc_pb";
import {
  GetRunningWorkflowsRequest,
  GetWorkflowsRequest,
  GetWorkflowStatusRequest,
} from "../proto/service/workflow_service_pb";
import { StartWorkflowRequest } from "../proto/model/startworkflowrequest_pb";

export class WorkflowClient {
  private grpcClient: WorkflowServiceClient;

  public constructor(address: string) {
    this.grpcClient = new WorkflowServiceClient(
      address,
      grpc.credentials.createInsecure()
    );
  }

  public getWorkflows(
    callback: (error: net.Error, response: net.Response) => void
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new GetWorkflowsRequest();
      this.grpcClient.getWorkflows(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getWorkflowsByIdMap().arr_;
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }

  public startWorkflow(
    callback: (error: net.Error, response: net.Response) => void,
    name: string
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new StartWorkflowRequest();
      req.setName(name);
      this.grpcClient.startWorkflow(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getWorkflowId();
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }

  public getWorkflowStatus(
    callback: (error: net.Error, response: net.Response) => void,
    workflowId: string,
    includeTasks?: boolean
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new GetWorkflowStatusRequest();
      req.setWorkflowId(workflowId);
      req.setIncludeTasks(includeTasks ?? false);
      this.grpcClient.getWorkflowStatus(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getStatus;
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }

  public getRunningWorkflows(
    callback: (error: net.Error, response: net.Response) => void,
    workflowName?: string,
    version?: number
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new GetRunningWorkflowsRequest();
      if (workflowName) req.setName(workflowName);

      if (version) req.setVersion(version);

      this.grpcClient.getRunningWorkflows(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getWorkflowIdsList();
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }
}
