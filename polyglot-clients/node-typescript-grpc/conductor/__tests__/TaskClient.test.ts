import * as grpc from "@grpc/grpc-js";
import * as taskClient from "../src/TaskClient";
import * as tasks from "../proto/service/task_service_pb";
import { TaskServiceClient } from "../proto/service/task_service_grpc_pb";
import { TaskResult } from "../proto/model/taskresult_pb";

type cb_getQueueAllInfo = (
  error: grpc.ServiceError | null,
  response: tasks.QueueAllInfoResponse
) => void;
type cb_getQueueInfo = (
  error: grpc.ServiceError | null,
  response: tasks.QueueInfoResponse
) => void;
type cb_getTask = (
  error: grpc.ServiceError | null,
  response: tasks.GetTaskResponse
) => void;
type cb_updateTask = (
  error: grpc.ServiceError | null,
  response: tasks.UpdateTaskResponse
) => void;
type cb_poll = (
  error: grpc.ServiceError | null,
  response: tasks.PollResponse
) => void;
type cb_getTaskLogs = (
  error: grpc.ServiceError | null,
  response: tasks.GetTaskLogsResponse
) => void;

jest.mock("../proto/service/task_service_grpc_pb", () => {
  // Works and lets you check for constructor calls:
  return {
    TaskServiceClient: jest.fn().mockImplementation(() => {
      return {
        getQueueAllInfo: (
          req: tasks.QueueAllInfoRequest,
          cb: cb_getQueueAllInfo
        ) => {
          cb(null, new tasks.QueueAllInfoResponse());
        },
        getQueueInfo: (req: tasks.QueueInfoRequest, cb: cb_getQueueInfo) => {
          cb(null, new tasks.QueueInfoResponse());
        },
        getTask: (req: tasks.GetTaskRequest, cb: cb_getTask) => {
          cb(null, new tasks.GetTaskResponse());
        },
        updateTask: (req: tasks.UpdateTaskResponse, cb: cb_updateTask) => {
          cb(null, new tasks.UpdateTaskResponse());
        },
        poll: (req: tasks.PollRequest, cb: cb_poll) => {
          cb(null, new tasks.PollResponse());
        },
        getTaskLogs: (req: tasks.GetTaskLogsResponse, cb: cb_getTaskLogs) => {
          cb(null, new tasks.GetTaskLogsResponse());
        },
      };
    }),
  };
});

test("getQueueAllInfo", () => {
  const client = new taskClient.TaskClient("localhost:0000");
  const callback = jest.fn();
  client.getQueueAllInfo(callback);
  expect(callback).toHaveBeenCalledWith({ message: null }, { value: [] });
});

test("getQueueInfo", () => {
  const client = new taskClient.TaskClient("localhost:0000");
  const callback = jest.fn();
  client.getQueueInfo(callback);
  expect(callback).toHaveBeenCalledWith({ message: null }, { value: [] });
});

test("getTask", () => {
  const client = new taskClient.TaskClient("localhost:0000");
  const callback = jest.fn();
  client.getTask(callback, "1");
  expect(callback).toHaveBeenCalledWith(
    { message: null },
    { value: undefined }
  );
});

test("updateTask", () => {
  const client = new taskClient.TaskClient("localhost:0000");
  const callback = jest.fn();
  client.updateTask(callback, new TaskResult());
  expect(callback).toHaveBeenCalledWith({ message: null }, { value: "" });
});

test("poll", () => {
  const client = new taskClient.TaskClient("localhost:0000");
  const callback = jest.fn();
  client.poll(callback, "task_1", "111", "*");
  expect(callback).toHaveBeenCalledWith(
    { message: null },
    { value: undefined }
  );
});

test("getTaskLogs", () => {
  const client = new taskClient.TaskClient("localhost:0000");
  const callback = jest.fn();
  client.getTaskLogs(callback, "1");
  expect(callback).toHaveBeenCalledWith({ message: null }, { value: [] });
});
