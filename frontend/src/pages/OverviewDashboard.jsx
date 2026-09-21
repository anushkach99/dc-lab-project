import { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import * as api from '../services/api';
import StatCard from '../components/StatCard';
import TopologyView from '../components/TopologyView';

export default function OverviewDashboard() {
  const ws = useWebSocket();
  const [data, setData] = useState({ system: {}, workers: [], events: [], logs: [], jobs: [] });

  useEffect(() => {
    let interval;
    const fetchData = async () => {
      try {
        const [sys, w, e, l, j] = await Promise.all([
          api.getSystemStatus().catch(() => ({})),
          api.getWorkers().catch(() => []),
          api.getEvents().catch(() => []),
          api.getLogs().catch(() => []),
          api.getJobs().catch(() => [])
        ]);
        setData({ system: sys || {}, workers: w || [], events: e || [], logs: l || [], jobs: j || [] });
      } catch (err) {}
    };

    if (!ws.connected) {
      fetchData();
      interval = setInterval(fetchData, 3000);
    } else {
      setData(prev => ({
        ...prev,
        workers: ws.workers.length ? ws.workers : prev.workers,
        system: Object.keys(ws.systemStatus).length ? ws.systemStatus : prev.system,
        events: ws.events.length ? ws.events : prev.events,
        logs: ws.logs.length ? ws.logs : prev.logs
      }));
    }
    return () => clearInterval(interval);
  }, [ws.connected, ws.workers, ws.systemStatus, ws.events, ws.logs]);

  const { system, workers, events, logs, jobs } = data;
  const onlineCount = workers.filter(w => w.status !== 'OFFLINE' && w.status !== 'FAILED').length;
  const stragglerCount = workers.filter(w => w.status === 'STRAGGLER').length;
  const activeJobs = jobs.filter(j => j.status === 'RUNNING').length;

  return (
    <div>
      <h2 style={{ marginBottom: '20px' }}>System Overview</h2>
      
      <div className="grid-6">
        <StatCard title="System Status" value={system.status || 'UNKNOWN'} color={system.status === 'ONLINE' ? 'var(--success)' : 'var(--warning)'} />
        <StatCard title="Coordinator" value={system.coordinator || 'None'} color="#9c27b0" />
        <StatCard title="Online Workers" value={onlineCount} color="var(--success)" />
        <StatCard title="Active Jobs" value={activeJobs} color="var(--accent)" />
        <StatCard title="Total Tasks" value={system.completedTasks || 0} color="var(--accent)" subtitle="Completed" />
        <StatCard title="Stragglers" value={stragglerCount} color="var(--warning)" />
      </div>

      <div className="card" style={{ marginTop: '20px' }}>
        <h3 style={{ marginBottom: '20px' }}>Network Topology</h3>
        <TopologyView workers={workers} coordinator={system.coordinator || 'MASTER'} />
      </div>

      <div className="grid-2">
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Recent Events</h3>
          <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
            {events.slice(0, 10).map((e, i) => (
              <div key={i} className="timeline-item">
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>LC: {e.lamportTimestamp}</div>
                <div>{e.description}</div>
              </div>
            ))}
          </div>
        </div>
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Recent Logs</h3>
          <div style={{ maxHeight: '300px', overflowY: 'auto', fontFamily: 'monospace', fontSize: '0.85rem' }}>
            {logs.slice(0, 15).map((l, i) => (
              <div key={i} style={{ marginBottom: '8px', borderBottom: '1px solid var(--border)', paddingBottom: '4px' }}>
                <span style={{ color: 'var(--text-secondary)', marginRight: '10px' }}>{new Date(l.timestamp).toLocaleTimeString()}</span>
                <span className="badge" style={{ marginRight: '10px' }}>{l.category}</span>
                <span>{l.message}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
