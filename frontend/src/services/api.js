import axios from 'axios';

const api = axios.create({
  baseURL: '/api'
});

export const getSystemStatus = () => api.get('/system/status').then(res => res.data);
export const getWorkers = () => api.get('/workers').then(res => res.data);
export const registerWorker = (data) => api.post('/workers/register', data).then(res => res.data);
export const getJobs = () => api.get('/jobs').then(res => res.data);
export const createJob = (numTasks, complexity) => api.post('/jobs', { numTasks, complexity }).then(res => res.data);
export const startJob = (jobId) => api.post(`/jobs/${jobId}/start`).then(res => res.data);
export const getJob = (jobId) => api.get(`/jobs/${jobId}`).then(res => res.data);
export const getTasks = () => api.get('/tasks').then(res => res.data);
export const getTasksByJob = (jobId) => api.get(`/tasks/job/${jobId}`).then(res => res.data);
export const getLoadBalancingStatus = () => api.get('/load-balancing/status').then(res => res.data);
export const triggerRebalance = () => api.post('/load-balancing/rebalance').then(res => res.data);
export const getElectionStatus = () => api.get('/election/status').then(res => res.data);
export const startElection = (algorithm) => api.post('/election/start', { algorithm }).then(res => res.data);
export const simulateMasterFailure = () => api.post('/election/fail-master').then(res => res.data);
export const getReplicationStatus = () => api.get('/replication/status').then(res => res.data);
export const triggerSync = () => api.post('/replication/sync').then(res => res.data);
export const simulateReplicaDelay = (enable) => api.post('/replication/simulate-delay', { enable }).then(res => res.data);
export const getEvents = () => api.get('/events').then(res => res.data);
export const getLogs = () => api.get('/logs').then(res => res.data);
export const getSettings = () => api.get('/settings').then(res => res.data);
export const updateSettings = (settings) => api.put('/settings', settings).then(res => res.data);
export const startDemo = () => api.post('/demo/start').then(res => res.data);
export const stopDemo = () => api.post('/demo/stop').then(res => res.data);
export const simulateStraggler = (workerId) => api.post('/demo/simulate-straggler', { workerId }).then(res => res.data);
export const restoreWorker = (workerId) => api.post('/demo/restore-worker', { workerId }).then(res => res.data);
export const runFullExperiment = () => api.post('/demo/run-full-experiment').then(res => res.data);

export default api;
