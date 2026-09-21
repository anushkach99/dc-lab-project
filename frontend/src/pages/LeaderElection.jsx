import { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import * as api from '../services/api';
import StatCard from '../components/StatCard';
import StatusBadge from '../components/StatusBadge';

export default function LeaderElection() {
  const ws = useWebSocket();
  const [data, setData] = useState({ status: {}, workers: [], events: [] });
  const [algo, setAlgo] = useState('BULLY');

  const fetchData = async () => {
    try {
      const [s, w] = await Promise.all([api.getElectionStatus(), api.getWorkers()]);
      setData(prev => ({ ...prev, status: s || {}, workers: w || [] }));
    } catch (e) {}
  };

  useEffect(() => {
    if (!ws.connected) {
      fetchData();
      const intId = setInterval(fetchData, 3000);
      return () => clearInterval(intId);
    } else {
      setData(prev => ({
        status: Object.keys(ws.election).length ? ws.election : prev.status,
        workers: ws.workers.length ? ws.workers : prev.workers,
        events: ws.election.events || prev.events
      }));
    }
  }, [ws.connected, ws.election, ws.workers]);

  const handleStart = async () => { await api.startElection(algo); fetchData(); };
  const handleFailMaster = async () => { await api.simulateMasterFailure(); fetchData(); };

  return (
    <div>
      <div className="header">
        <h2>Leader Election</h2>
        <span className="badge" style={{ backgroundColor: 'var(--accent)', color: 'white', padding: '6px 12px' }}>Exp 4 - Leader Election (Bully & Ring Algorithms)</span>
      </div>

      <div className="grid-2" style={{ marginBottom: '20px' }}>
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Algorithm Selection</h3>
          <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
            <button className={\`btn \${algo === 'BULLY' ? 'btn-primary' : ''}\`} style={{ flex: 1, backgroundColor: algo !== 'BULLY' ? 'var(--border)' : '', color: algo !== 'BULLY' ? 'var(--text-primary)' : '' }} onClick={() => setAlgo('BULLY')}>BULLY</button>
            <button className={\`btn \${algo === 'RING' ? 'btn-primary' : ''}\`} style={{ flex: 1, backgroundColor: algo !== 'RING' ? 'var(--border)' : '', color: algo !== 'RING' ? 'var(--text-primary)' : '' }} onClick={() => setAlgo('RING')}>RING</button>
          </div>
          <div style={{ display: 'flex', gap: '10px' }}>
            <button className="btn btn-success" style={{ flex: 1 }} onClick={handleStart}>Start Election</button>
            <button className="btn btn-danger" style={{ flex: 1 }} onClick={handleFailMaster}>Simulate Master Failure</button>
          </div>
        </div>
        <StatCard title="Current Coordinator" value={data.status.coordinatorId || 'UNKNOWN'} color="#9c27b0" subtitle={\`Status: \${data.status.state || 'IDLE'}\`} />
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <h3 style={{ marginBottom: '15px' }}>Ring Topology / Node Status</h3>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '15px', justifyContent: 'center', padding: '20px' }}>
          {data.workers.map(w => (
            <div key={w.id} style={{ 
              padding: '15px', 
              borderRadius: '50%', 
              width: '100px', 
              height: '100px', 
              display: 'flex', 
              flexDirection: 'column', 
              alignItems: 'center', 
              justifyContent: 'center',
              backgroundColor: w.id === data.status.coordinatorId ? 'rgba(156, 39, 176, 0.1)' : 'var(--bg-card)',
              border: w.id === data.status.coordinatorId ? '3px solid #9c27b0' : '1px solid var(--border)',
              boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
            }}>
              <strong style={{ fontSize: '1.2rem' }}>{w.id.split('-')[w.id.split('-').length-1] || w.id}</strong>
              <div style={{ fontSize: '0.7rem', marginTop: '5px' }}>{w.status}</div>
              {w.id === data.status.coordinatorId && <span>👑</span>}
            </div>
          ))}
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: '15px' }}>Election Event Log</h3>
        <div className="table-wrapper" style={{ maxHeight: '300px', overflowY: 'auto' }}>
          <table className="table">
            <thead>
              <tr>
                <th>Time</th>
                <th>From</th>
                <th>To</th>
                <th>Message</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              {data.events && data.events.map((e, i) => (
                <tr key={i}>
                  <td>{new Date(e.timestamp).toLocaleTimeString()}</td>
                  <td>{e.fromNode}</td>
                  <td>{e.toNode}</td>
                  <td><span className="badge" style={{ backgroundColor: 'var(--border)' }}>{e.messageType}</span></td>
                  <td>{e.description}</td>
                </tr>
              ))}
              {(!data.events || data.events.length === 0) && <tr><td colSpan="5" style={{ textAlign: 'center' }}>No election events recorded</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
