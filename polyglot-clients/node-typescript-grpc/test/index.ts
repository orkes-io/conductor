import { TaskClient, WorkflowClient
    , ConductorWorker, TaskReturnObject } from 'netflix-conductor-ts-grpc';

const exec_function = () => ({'status': 'COMPLETED', 'output': {}, 'logs': []} as TaskReturnObject);

let cc = new ConductorWorker("localhost:8090", 2, 2000, "1b32c647-2f33-4c7c-8b83-72bba98ab584");

cc.Start((err, resp) => {
    console.log(err);
    console.log(resp);
}, "task_1", exec_function);

//console.log(iEqual(13));

// const service = new TaskClient("localhost:8090");
// service.GetQueueAllInfo((err,resp) => {
//     console.log(err);
//     console.log(resp);
// })

// const service = new WorkflowClient("localhost:8090");
// service.StartWorkflow("kitchensink", (err,resp) => {
//     console.log(err);
//     console.log(resp);
// })