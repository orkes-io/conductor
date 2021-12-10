import * as grpc from "@grpc/grpc-js";
import * as net from "./common";
import { WorkflowDef } from "../proto/model/workflowdef_pb";
import { MetadataServiceClient } from "../proto/service/metadata_service_grpc_pb";
import * as metadata from "../proto/service/metadata_service_pb";
import { TaskDef } from "../proto/model/taskdef_pb";

export class MetadataClient {
  private grpcClient: MetadataServiceClient;

  public constructor(address: string) {
    this.grpcClient = new MetadataServiceClient(
      address,
      grpc.credentials.createInsecure()
    );
  }

  public getTask(
    callback: (error: net.Error, response: net.Response) => void,
    taskName: string
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new metadata.GetTaskRequest();
      req.setTaskType(taskName);

      this.grpcClient.getTask(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getTask();
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }

  public updateTask(
    callback: (error: net.Error, response: net.Response) => void,
    taskDef: TaskDef
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new metadata.UpdateTaskRequest();
      req.setTask(taskDef);

      this.grpcClient.updateTask(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response;
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }

  public createTasks(
    callback: (error: net.Error, response: net.Response) => void,
    taskDef: TaskDef
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new metadata.CreateTasksRequest();
      req.addDefs(taskDef);

      this.grpcClient.createTasks(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response;
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }

  public deleteTask(
    callback: (error: net.Error, response: net.Response) => void,
    taskType: string
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new metadata.DeleteTaskRequest();
      req.setTaskType(taskType);

      this.grpcClient.deleteTask(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response;
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }

  public updateWorkflows(
    callback: (error: net.Error, response: net.Response) => void,
    workflowDef: WorkflowDef
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new metadata.UpdateWorkflowsRequest();
      req.addDefs(workflowDef);

      this.grpcClient.updateWorkflows(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response;
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }

  public createWorkflow(
    callback: (error: net.Error, response: net.Response) => void,
    name: string
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const def = new WorkflowDef();
      def.setName(name);

      const req = new metadata.CreateWorkflowRequest();
      req.setWorkflow(def);

      this.grpcClient.createWorkflow(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response;
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }
}
