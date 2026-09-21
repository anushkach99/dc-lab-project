import { useState, useEffect, useRef } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import * as api from '../services/api';

export default function SystemLogs() {
  const ws = useWebSocket();
  const [logs, setLogs] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [search, setSearch] = useState('');
  const [autoScroll, setAutoScroll] = useState(true);
  const logEndRef = useRef(null);

  useEffect(() => {
    if (!ws.connected) {
      const fetchLogs = async () => {
        try { const data = await api.getLogs(); setLogs(data || []); } catch(e) {}
      };
      fetchLogs();
      const intId = setInterval(fetchLogs, 3000);
      return () => clearInterval(intId);
    } else {
      setLogs(ws.logs.length ? ws.logs : logs);
    }
  }, [ws.connected, ws.logs]);

  useEffect(() => {
    if (autoScroll && logEndRef.current) {
      logEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [logs, filter, search, autoScroll]);

  const categories = ['ALL', 'MASTER', 'WORKER', 'JOB', 'LB', 'ELECTION', 'REPLICATION'];

  const filteredLogs = logs.filter(l => {
    if (filter !== 'ALL' && l.category !== filter) return false;
    if (search && !l.message.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  const getCategoryColor = (cat) => {
    const map = {
      MASTER: '#9c27b0', WORKER: 'var(--success)', JOB: 'var(--accent)', 
      LB: 'var(--warning)', ELECTION: '#e91e63', REPLICATION: '#00bcd4'
    };
    return map[cat] || 'var(--text-secondary)';
  };

  return (
    <div style={{ height: 'calc(100vh - 100px)', display: 'flex', flexDirection: 'column' }}>
      <div className="header">
        <h2>System Logs</h2>
      </div>

      <div className="card" style={{ padding: '15px', marginBottom: '15px', display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', gap: '5px' }}>
          {categories.map(c => (
            <button 
              key={c}
              className={`btn ${filter === c ? 'btn-primary' : ''}`}
              style={{ padding: '4px 10px', fontSize: '0.8rem', backgroundColor: filter !== c ? 'var(--border)' : '', color: filter !== c ? 'var(--text-primary)' : '' }}
              onClick={() => setFilter(c)}
            >
              {c}
            </button>
          ))}
        </div>
        <input 
          type="text" 
          placeholder="Search logs..." 
          style={{ padding: '8px 12px', borderRadius: '4px', border: '1px solid var(--border)', flexGrow: 1 }}
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
          <input type="checkbox" id="autoscroll" checked={autoScroll} onChange={e => setAutoScroll(e.target.checked)} />
          <label htmlFor="autoscroll" style={{ fontSize: '0.9rem' }}>Auto-scroll</label>
        </div>
        <button className="btn btn-danger" style={{ padding: '6px 12px', fontSize: '0.85rem' }} onClick={() => setLogs([])}>Clear</button>
      </div>

      <div className="card" style={{ flexGrow: 1, overflowY: 'auto', backgroundColor: 'var(--bg-primary)', fontFamily: 'monospace', padding: '15px', borderRadius: '4px' }}>
        {filteredLogs.map((l, i) => (
          <div key={i} style={{ marginBottom: '6px', fontSize: '0.85rem', lineHeight: '1.4' }}>
            <span style={{ color: 'var(--text-secondary)', marginRight: '10px' }}>[{new Date(l.timestamp).toLocaleTimeString()}]</span>
            <span style={{ color: getCategoryColor(l.category), fontWeight: 'bold', display: 'inline-block', width: '80px' }}>[{l.category}]</span>
            <span style={{ marginLeft: '10px', wordBreak: 'break-all' }}>{l.message}</span>
          </div>
        ))}
        {filteredLogs.length === 0 && <div style={{ color: 'var(--text-secondary)', textAlign: 'center', marginTop: '20px' }}>No logs found</div>}
        <div ref={logEndRef} />
      </div>
    </div>
  );
}
