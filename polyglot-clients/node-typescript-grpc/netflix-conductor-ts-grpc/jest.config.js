/** @type {import('ts-jest/dist/types').InitialOptionsTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ["<rootDir>/src/","<rootDir>/__tests__/"],
  transform: {
    "^.+\\.ts?$": "ts-jest"
  },
  setupFilesAfterEnv: [],
  testMatch: [
    "<rootDir>/__tests__/*.test.ts"
  ],
  testPathIgnorePatterns:["/node_modules/"]
};