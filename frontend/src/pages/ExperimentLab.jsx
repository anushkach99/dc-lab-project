import { useNavigate } from 'react-router-dom';
import * as api from '../services/api';
import { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';

export default function ExperimentLab() {
  const navigate = useNavigate();
  const ws = useWebSocket();
  const [data, setData] = useState({ workers: [], jobs: [], events: [], system: {}, loadBalancing: {} });

  useEffect(() => {
    if (!ws.connected) {
      Promise.all([api.getWorkers(), api.getJobs(), api.getEvents(), api.getSystemStatus(), api.getLoadBalancingStatus()])
        .then(([w, j, e, s, lb]) => setData({ workers: w||[], jobs: j||[], events: e||[], system: s||{}, loadBalancing: lb||{} }))
        .catch(()=>{});
    } else {
      setData({ workers: ws.workers, jobs: [], events: ws.events, system: ws.systemStatus, loadBalancing: ws.loadBalancing });
    }
  }, [ws.connected, ws.workers, ws.events, ws.systemStatus, ws.loadBalancing]);

  const handleRunFull = async () => {
    await api.runFullExperiment();
    alert("Full experiment sequence initiated. Check logs and dashboards.");
  };

  const expData = [
    { id: 1, title: 'Exp 1 - Java RMI Communication', desc: 'Workers register with the master scheduler via Java RMI. The master maintains a distributed worker registry.', status: `${data.workers.length} Registered`, link: '/workers' },
    { id: 2, title: 'Exp 2 - Multithreaded Distributed Execution', desc: 'The master distributes tasks to workers concurrently using thread pools. Workers execute tasks in parallel.', status: `Active Threads`, link: '/jobs' },
    { id: 3, title: 'Exp 3 - Lamport Logical Clock', desc: 'Each node maintains a Lamport clock. Events are timestamped and ordered to establish causality across the distributed system.', status: `${data.events.length} Events`, link: '/clock' },
    { id: 4, title: 'Exp 4 - Bully + Ring Election', desc: 'Leader election ensures the system survives coordinator failure. Both Bully and Ring algorithms are implemented.', status: `Leader: ${data.system.coordinator || 'None'}`, link: '/election' },
    { id: 5, title: 'Exp 5 - Replication & Consistency', desc: 'Scheduler state is replicated from primary to backup with bounded-staleness guarantees.', status: `Sync Status`, link: '/replication' },
    { id: 6, title: 'Exp 6 - Dynamic Load Balancing', desc: 'Heterogeneity-aware scheduling distributes work by worker capability. Stragglers are detected and work is dynamically redistributed.', status: `${data.loadBalancing.redistributionCount || 0} Redistributed`, link: '/load-balancing' },
  ];

  return (
    <div>
      <div className="header">
        <h2>Distributed Systems Experiment Lab</h2>
      </div>

      <div className="grid-3" style={{ marginBottom: '30px' }}>
        {expData.map(e => (
          <div key={e.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
            <h3 style={{ marginBottom: '10px' }}>{e.title}</h3>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginBottom: '15px', flexGrow: 1 }}>{e.desc}</p>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
              <span className="badge" style={{ backgroundColor: 'var(--border)' }}>{e.status}</span>
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
              <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => navigate(e.link)}>View Exp</button>
              <button className="btn" style={{ flex: 1, backgroundColor: 'var(--border)' }} onClick={() => navigate('/logs')}>View Logs</button>
            </div>
          </div>
        ))}
      </div>

      <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
        <h3 style={{ marginBottom: '15px' }}>Automated Full System Test</h3>
        <p style={{ marginBottom: '20px', color: 'var(--text-secondary)', maxWidth: '600px', margin: '0 auto 20px auto' }}>
          Run the complete suite of distributed system experiments in sequence. This will automatically spawn jobs, simulate failures, trigger leader election, and test load balancing.
        </p>
        <button className="btn btn-success" style={{ padding: '15px 30px', fontSize: '1.1rem' }} onClick={handleRunFull}>Run Full Experiment Suite</button>
      </div>
    </div>
  );
}
