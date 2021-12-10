import * as grpc from "@grpc/grpc-js";
import * as net from "./common";
import { TaskServiceClient } from "../proto/service/task_service_grpc_pb";
import * as tasks from "../proto/service/task_service_pb";
import { TaskResult } from "../proto/model/taskresult_pb";

export class TaskClient {
  private grpcClient: TaskServiceClient;

  public constructor(address: string) {
    this.grpcClient = new TaskServiceClient(
      address,
      grpc.credentials.createInsecure()
    );
  }

  public getTask(
    callback: (error: net.Error, response: net.Response) => void,
    taskId: string
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new tasks.GetTaskRequest();
      req.setTaskId(taskId);

      this.grpcClient.getTask(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getTask();
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;
    }
    callback(error, resp);
  }

  public updateTask(
    callback: (error: net.Error, response: net.Response) => void,
    taskResultObj: TaskResult
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new tasks.UpdateTaskRequest();
      req.setResult(taskResultObj);

      this.grpcClient.updateTask(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getTaskId();
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;
    }
    callback(error, resp);
  }

  public poll(
    callback: (error: net.Error, response: net.Response) => void,
    taskReferenceName: string,
    workerId?: string,
    domain?: string
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new tasks.PollRequest();
      req.setTaskType(taskReferenceName);
      if (workerId) req.setWorkerId(workerId);
      if (domain) req.setDomain(domain);

      this.grpcClient.poll(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getTask();
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;
    }
    callback(error, resp);
  }

  public getTaskLogs(
    callback: (error: net.Error, response: net.Response) => void,
    taskId: string
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new tasks.GetTaskLogsRequest();
      req.setTaskId(taskId);

      this.grpcClient.getTaskLogs(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getLogsList();
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;
    }
    callback(error, resp);
  }

  public getQueueInfo(
    callback: (error: net.Error, response: net.Response) => void,
    taskId?: string
  ) {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new tasks.QueueInfoRequest();

      this.grpcClient.getQueueInfo(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getQueuesMap().arr_;
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;
    }
    callback(error, resp);
  }

  public getQueueAllInfo(
    callback: (error: net.Error, response: net.Response) => void
  ): void {
    const error = new net.Error();
    const resp = new net.Response();
    try {
      const req = new tasks.QueueAllInfoRequest();
      this.grpcClient.getQueueAllInfo(req, function (err, response) {
        error.message = err && err.details;
        resp.value = response && response.getQueuesMap().arr_;
        callback(error, resp);
      });
    } catch (ex: unknown) {
      if (ex instanceof Error) error.message = ex.message;

      callback(error, resp);
    }
  }
}
