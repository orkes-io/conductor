import * as grpc from "@grpc/grpc-js";
import { WorkflowClient } from "./WorkflowClient";
import { TaskClient } from "./TaskClient";
import { MetadataClient } from "./MetadataClient";

export class WorkflowClientManager {
  public workflowClient: WorkflowClient;
  public taskClient: TaskClient;
  public metadataClient: MetadataClient;

  public constructor(address: string) {
    this.workflowClient = new WorkflowClient(address);
    this.taskClient = new TaskClient(address);
    this.metadataClient = new MetadataClient(address);
  }
}
