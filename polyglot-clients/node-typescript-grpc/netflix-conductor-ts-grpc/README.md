This is typescript enabled client for nodejs development. This client will connect to your Neflix Conductor backend server over grpc protocol.

It only supports grpc.

Actual code is in folder 'netflix-conductor-ts-grpc'

The 'test' folder is the client application to test the package.
```
cd test
yarn install 
yarn ts-node index.ts
```
for local testing
```
cd netflix-conductor-ts-grpc
yarn link

cd ../test
yarn link netflix-conductor-ts-grpc
yarn run ts-node index.ts
```
