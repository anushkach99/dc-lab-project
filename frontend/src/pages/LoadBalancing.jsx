import { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import * as api from '../services/api';
import StatCard from '../components/StatCard';
import StatusBadge from '../components/StatusBadge';
import { BarChart, Bar, LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';

export default function LoadBalancing() {
  const ws = useWebSocket();
  const [data, setData] = useState({ stats: {}, workers: [], redistributions: [] });

  const fetchData = async () => {
    try {
      const [status, w] = await Promise.all([api.getLoadBalancingStatus(), api.getWorkers()]);
      setData({ stats: status || {}, workers: w || [], redistributions: status?.redistributions || [] });
    } catch (e) {}
  };

  useEffect(() => {
    if (!ws.connected) {
      fetchData();
      const intId = setInterval(fetchData, 3000);
      return () => clearInterval(intId);
    } else {
      setData(prev => ({
        ...prev,
        stats: Object.keys(ws.loadBalancing).length ? ws.loadBalancing : prev.stats,
        workers: ws.workers.length ? ws.workers : prev.workers
      }));
    }
  }, [ws.connected, ws.loadBalancing, ws.workers]);

  const handleRebalance = async () => { await api.triggerRebalance(); fetchData(); };

  const taskData = data.workers.map(w => ({
    name: w.id,
    Completed: w.metrics?.completedTasks || 0,
    Remaining: w.metrics?.pendingTasks || 0
  }));

  const throughputData = data.workers.map(w => ({
    name: w.id,
    Throughput: w.metrics?.throughput || 0
  }));

  return (
    <div>
      <div className="header">
        <h2>Load Balancing</h2>
        <span className="badge" style={{ backgroundColor: 'var(--accent)', color: 'white', padding: '6px 12px' }}>Exp 6 - Heterogeneity-Aware Dynamic Load Balancing</span>
      </div>

      <div className="grid-3" style={{ marginBottom: '20px' }}>
        <StatCard title="Total Tasks" value={data.stats.totalTasks || 0} color="var(--accent)" />
        <StatCard title="Redistributions" value={data.stats.redistributionCount || 0} color="var(--success)" />
        <StatCard title="Stragglers Detected" value={data.stats.stragglersDetected || 0} color="var(--warning)" />
      </div>

      <div className="grid-2" style={{ marginBottom: '20px' }}>
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Task Distribution</h3>
          <div style={{ height: '250px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={taskData}>
                <XAxis dataKey="name" tick={{fontSize: 12}} />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="Completed" stackId="a" fill="var(--success)" />
                <Bar dataKey="Remaining" stackId="a" fill="var(--warning)" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Worker Throughput</h3>
          <div style={{ height: '250px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={throughputData}>
                <XAxis dataKey="name" tick={{fontSize: 12}} />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="Throughput" stroke="var(--accent)" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
          <h3>Worker Workload</h3>
          <div>
            <span style={{ marginRight: '15px', fontSize: '0.85rem' }}>Straggler Threshold: {data.stats.threshold || '0.5'}</span>
            <button className="btn btn-primary" onClick={handleRebalance}>Manual Rebalance</button>
          </div>
        </div>
        <div className="grid-4">
          {data.workers.map(w => (
            <div key={w.id} className="card" style={{ border: w.status === 'STRAGGLER' ? '2px solid var(--warning)' : '1px solid var(--border)' }}>
              <h4>{w.id}</h4>
              <StatusBadge status={w.status} />
              <div style={{ marginTop: '10px', fontSize: '0.85rem' }}>
                <div>Completed: {w.metrics?.completedTasks || 0}</div>
                <div>Remaining: {w.metrics?.pendingTasks || 0}</div>
                <div>Avg Time: {w.metrics?.avgTaskTime || 0} ms</div>
              </div>
            </div>
          ))}
        </div>
      </div>
      
      <div className="card">
        <h3 style={{ marginBottom: '15px' }}>Redistribution Timeline</h3>
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Source (Straggler)</th>
                <th>Target</th>
                <th>Tasks Moved</th>
              </tr>
            </thead>
            <tbody>
              {data.redistributions.map((r, i) => (
                <tr key={i}>
                  <td>{new Date(r.timestamp).toLocaleTimeString()}</td>
                  <td>{r.source}</td>
                  <td>{r.target}</td>
                  <td>{r.taskCount}</td>
                </tr>
              ))}
              {data.redistributions.length === 0 && <tr><td colSpan="4" style={{ textAlign: 'center' }}>No redistributions yet</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
