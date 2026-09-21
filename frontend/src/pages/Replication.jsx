import { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import * as api from '../services/api';
import StatusBadge from '../components/StatusBadge';

export default function Replication() {
  const ws = useWebSocket();
  const [data, setData] = useState({ status: {}, logs: [] });
  const [delayEnabled, setDelayEnabled] = useState(false);

  const fetchData = async () => {
    try {
      const s = await api.getReplicationStatus();
      setData(prev => ({ ...prev, status: s || {} }));
    } catch (e) {}
  };

  useEffect(() => {
    if (!ws.connected) {
      fetchData();
      const intId = setInterval(fetchData, 3000);
      return () => clearInterval(intId);
    } else {
      setData(prev => ({
        status: Object.keys(ws.replication).length ? ws.replication : prev.status,
        logs: ws.replication.logs ? [...prev.logs, ...ws.replication.logs].slice(-50) : prev.logs
      }));
    }
  }, [ws.connected, ws.replication]);

  const handleSync = async () => { await api.triggerSync(); fetchData(); };
  const handleToggleDelay = async () => { 
    await api.simulateReplicaDelay(!delayEnabled); 
    setDelayEnabled(!delayEnabled); 
  };

  const pState = data.status.primary || {};
  const bState = data.status.backup || {};
  const staleness = data.status.staleness || 0;
  const maxStaleness = data.status.maxStaleness || 5;
  const stalenessPct = Math.min((staleness / maxStaleness) * 100, 100);
  
  let stalenessColor = 'var(--success)';
  if (stalenessPct > 50) stalenessColor = 'var(--warning)';
  if (stalenessPct > 80) stalenessColor = 'var(--danger)';

  return (
    <div>
      <div className="header">
        <h2>Replication & Consistency</h2>
        <span className="badge" style={{ backgroundColor: 'var(--accent)', color: 'white', padding: '6px 12px' }}>Exp 5 - Replication & Bounded Staleness</span>
      </div>

      <div className="grid-2" style={{ marginBottom: '20px' }}>
        <div className="card" style={{ borderTop: '4px solid var(--accent)' }}>
          <h3>Primary State</h3>
          <div style={{ marginTop: '20px', textAlign: 'center' }}>
            <div style={{ fontSize: '3rem', fontWeight: 'bold' }}>v{pState.version || 0}</div>
            <div style={{ margin: '10px 0' }}><StatusBadge status={pState.status || 'ONLINE'} /></div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Last Update: {pState.lastUpdate ? new Date(pState.lastUpdate).toLocaleTimeString() : '-'}</div>
          </div>
        </div>
        <div className="card" style={{ borderTop: '4px solid var(--success)' }}>
          <h3>Backup State</h3>
          <div style={{ marginTop: '20px', textAlign: 'center' }}>
            <div style={{ fontSize: '3rem', fontWeight: 'bold' }}>v{bState.version || 0}</div>
            <div style={{ margin: '10px 0' }}><StatusBadge status={bState.status || 'ONLINE'} /></div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Last Sync: {bState.lastSync ? new Date(bState.lastSync).toLocaleTimeString() : '-'}</div>
          </div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <h3 style={{ marginBottom: '15px' }}>Consistency Metrics</h3>
        <div className="grid-2">
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
              <span>Staleness (Lag)</span>
              <span>{staleness} / {maxStaleness} updates</span>
            </div>
            <div className="progress-bar-bg" style={{ height: '12px' }}>
              <div className="progress-bar-fill" style={{ width: \`\${stalenessPct}%\`, backgroundColor: stalenessColor }}></div>
            </div>
          </div>
          <div style={{ display: 'flex', gap: '15px', alignItems: 'flex-end', justifyContent: 'flex-end' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{data.status.pendingUpdates || 0}</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Pending Updates</div>
            </div>
            <button className="btn btn-primary" onClick={handleSync}>Force Sync</button>
            <button className={\`btn \${delayEnabled ? 'btn-danger' : 'btn-warning'}\`} onClick={handleToggleDelay}>
              {delayEnabled ? 'Disable Delay' : 'Simulate Delay'}
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: '15px' }}>Sync History</h3>
        <div className="table-wrapper" style={{ maxHeight: '300px', overflowY: 'auto' }}>
          <table className="table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Type</th>
                <th>From Version</th>
                <th>To Version</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {data.logs && data.logs.map((l, i) => (
                <tr key={i}>
                  <td>{new Date(l.timestamp).toLocaleTimeString()}</td>
                  <td>{l.type}</td>
                  <td>v{l.fromVersion}</td>
                  <td>v{l.toVersion}</td>
                  <td><StatusBadge status={l.status} /></td>
                </tr>
              ))}
              {(!data.logs || data.logs.length === 0) && <tr><td colSpan="5" style={{ textAlign: 'center' }}>No sync history available</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
