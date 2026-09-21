import { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import * as api from '../services/api';
import StatusBadge from '../components/StatusBadge';

export default function WorkerNodes() {
  const ws = useWebSocket();
  const [workers, setWorkers] = useState([]);

  const fetchWorkers = async () => {
    try {
      const data = await api.getWorkers();
      setWorkers(data || []);
    } catch (e) {}
  };

  useEffect(() => {
    if (!ws.connected) {
      fetchWorkers();
      const intId = setInterval(fetchWorkers, 3000);
      return () => clearInterval(intId);
    } else {
      setWorkers(ws.workers.length ? ws.workers : workers);
    }
  }, [ws.connected, ws.workers]);

  const handleSimulate = async (id) => { await api.simulateStraggler(id); fetchWorkers(); };
  const handleRestore = async (id) => { await api.restoreWorker(id); fetchWorkers(); };

  return (
    <div>
      <div className="header">
        <h2>Worker Nodes</h2>
        <span className="badge" style={{ backgroundColor: 'var(--accent)', color: 'white', padding: '6px 12px' }}>Exp 1 - Java RMI Communication</span>
      </div>

      <div className="grid-3">
        {workers.map(w => {
          const total = w.metrics?.completedTasks + w.metrics?.pendingTasks || 0;
          const prog = total > 0 ? (w.metrics?.completedTasks / total) * 100 : 0;
          
          return (
            <div key={w.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                <h3 style={{ fontSize: '1.5rem' }}>{w.id}</h3>
                <StatusBadge status={w.status} />
              </div>
              
              <div style={{ marginBottom: '15px' }}>
                <span className="badge" style={{ backgroundColor: 'var(--border)', marginRight: '10px' }}>{w.type || 'NODE'}</span>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{w.ip || '127.0.0.1'}</span>
              </div>

              <div style={{ fontSize: '0.9rem', marginBottom: '15px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div>CPU: {w.specs?.cores || 4} cores @ {w.specs?.speed || 2.4}GHz</div>
                <div>RAM: {w.specs?.ram || 8} GB</div>
                <div>Throughput: {w.metrics?.throughput || 0} t/s</div>
                <div>Avg Time: {w.metrics?.avgTaskTime || 0} ms</div>
                <div style={{ gridColumn: '1 / -1' }}>Lamport Clock: <strong style={{ color: 'var(--accent)' }}>{w.lamportClock || 0}</strong></div>
              </div>

              <div style={{ marginBottom: '5px', fontSize: '0.85rem', display: 'flex', justifyContent: 'space-between' }}>
                <span>CPU Usage</span>
                <span>{w.metrics?.cpuUsage || 0}%</span>
              </div>
              <div className="progress-bar-bg" style={{ marginBottom: '15px' }}>
                <div className="progress-bar-fill" style={{ width: `${w.metrics?.cpuUsage || 0}%` }}></div>
              </div>

              <div style={{ marginBottom: '5px', fontSize: '0.85rem', display: 'flex', justifyContent: 'space-between' }}>
                <span>Tasks (C/P)</span>
                <span>{w.metrics?.completedTasks || 0} / {w.metrics?.pendingTasks || 0}</span>
              </div>
              <div className="progress-bar-bg" style={{ marginBottom: '15px' }}>
                <div className="progress-bar-fill" style={{ width: `${prog}%`, backgroundColor: 'var(--success)' }}></div>
              </div>

              <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                <button className="btn btn-primary" style={{ flex: 1, fontSize: '0.8rem' }} onClick={() => handleSimulate(w.id)}>Simulate Straggler</button>
                <button className="btn" style={{ flex: 1, fontSize: '0.8rem', backgroundColor: 'var(--border)' }} onClick={() => handleRestore(w.id)}>Restore</button>
              </div>
            </div>
          );
        })}
        {workers.length === 0 && <div className="card" style={{ gridColumn: '1 / -1', textAlign: 'center' }}>No workers registered</div>}
      </div>
    </div>
  );
}
