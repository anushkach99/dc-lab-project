import { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import * as api from '../services/api';
import StatusBadge from '../components/StatusBadge';
import StatCard from '../components/StatCard';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

export default function JobsTasks() {
  const ws = useWebSocket();
  const [jobs, setJobs] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [form, setForm] = useState({ count: 100, complexity: 'MEDIUM' });

  const fetchData = async () => {
    try {
      const [j, t] = await Promise.all([api.getJobs(), api.getTasks()]);
      setJobs(j || []);
      setTasks(t || []);
    } catch (e) {}
  };

  useEffect(() => {
    if (!ws.connected) {
      fetchData();
      const intId = setInterval(fetchData, 3000);
      return () => clearInterval(intId);
    } else {
      setTasks(ws.tasks.length ? ws.tasks : tasks);
      api.getJobs().then(res => setJobs(res)).catch(() => {});
    }
  }, [ws.connected, ws.tasks]);

  const handleCreate = async () => { await api.createJob(form.count, form.complexity); fetchData(); };
  const handleStart = async (id) => { await api.startJob(id); fetchData(); };

  const taskStats = tasks.reduce((acc, t) => {
    acc[t.status] = (acc[t.status] || 0) + 1;
    return acc;
  }, {});
  
  const chartData = [
    { name: 'Pending', count: taskStats.PENDING || 0 },
    { name: 'Running', count: taskStats.RUNNING || 0 },
    { name: 'Completed', count: taskStats.COMPLETED || 0 },
    { name: 'Failed', count: taskStats.FAILED || 0 }
  ];

  return (
    <div>
      <div className="header">
        <h2>Jobs & Tasks</h2>
        <span className="badge" style={{ backgroundColor: 'var(--accent)', color: 'white', padding: '6px 12px' }}>Exp 2 - Multithreaded Execution</span>
      </div>

      <div className="grid-4" style={{ marginBottom: '20px' }}>
        <StatCard title="Active Threads" value={jobs.filter(j=>j.status==='RUNNING').length * 4} color="var(--accent)" />
        <StatCard title="Queued Tasks" value={taskStats.PENDING || 0} color="var(--text-secondary)" />
        <StatCard title="Running Tasks" value={taskStats.RUNNING || 0} color="var(--warning)" />
        <StatCard title="Completed Tasks" value={taskStats.COMPLETED || 0} color="var(--success)" />
      </div>

      <div className="grid-2">
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Create Job</h3>
          <div style={{ display: 'flex', gap: '15px', alignItems: 'flex-end' }}>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', fontSize: '0.85rem', marginBottom: '5px' }}>Task Count</label>
              <select style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid var(--border)' }} value={form.count} onChange={e => setForm({...form, count: Number(e.target.value)})}>
                <option value={100}>100</option>
                <option value={500}>500</option>
                <option value={1000}>1000</option>
              </select>
            </div>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', fontSize: '0.85rem', marginBottom: '5px' }}>Complexity</label>
              <select style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid var(--border)' }} value={form.complexity} onChange={e => setForm({...form, complexity: e.target.value})}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </div>
            <button className="btn btn-primary" onClick={handleCreate}>Create Job</button>
          </div>
        </div>
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Task Status Distribution</h3>
          <div style={{ height: '100px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <XAxis dataKey="name" tick={{fontSize: 12}} />
                <Tooltip />
                <Bar dataKey="count" fill="var(--accent)" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: '15px' }}>Jobs List</h3>
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>Job ID</th>
                <th>Total Tasks</th>
                <th>Completed</th>
                <th>Running</th>
                <th>Status</th>
                <th>Progress</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {jobs.map(j => {
                const prog = j.totalTasks ? (j.completedTasks / j.totalTasks) * 100 : 0;
                return (
                  <tr key={j.id}>
                    <td>{j.id}</td>
                    <td>{j.totalTasks}</td>
                    <td>{j.completedTasks}</td>
                    <td>{j.runningTasks}</td>
                    <td><StatusBadge status={j.status} /></td>
                    <td style={{ width: '150px' }}>
                      <div className="progress-bar-bg"><div className="progress-bar-fill" style={{ width: \`\${prog}%\` }}></div></div>
                    </td>
                    <td>
                      {j.status === 'CREATED' && <button className="btn btn-success" style={{ padding: '4px 8px', fontSize: '0.8rem' }} onClick={() => handleStart(j.id)}>Start</button>}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: '15px' }}>Recent Tasks (Sample)</h3>
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>Task ID</th>
                <th>Worker</th>
                <th>Status</th>
                <th>Start Time</th>
                <th>Exec Time (ms)</th>
                <th>Lamport TS</th>
              </tr>
            </thead>
            <tbody>
              {tasks.slice(0, 10).map(t => (
                <tr key={t.id}>
                  <td>{t.id}</td>
                  <td>{t.workerId || '-'}</td>
                  <td><StatusBadge status={t.status} /></td>
                  <td>{t.startTime ? new Date(t.startTime).toLocaleTimeString() : '-'}</td>
                  <td>{t.executionTime || '-'}</td>
                  <td>{t.lamportTimestamp || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
