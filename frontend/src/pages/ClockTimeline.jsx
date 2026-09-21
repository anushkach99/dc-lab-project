import { useState, useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import * as api from '../services/api';
import { ScatterChart, Scatter, XAxis, YAxis, ZAxis, Tooltip, ResponsiveContainer } from 'recharts';

export default function ClockTimeline() {
  const ws = useWebSocket();
  const [events, setEvents] = useState([]);

  useEffect(() => {
    if (!ws.connected) {
      const fetchEvents = async () => {
        try { const data = await api.getEvents(); setEvents(data || []); } catch(e) {}
      };
      fetchEvents();
      const intId = setInterval(fetchEvents, 3000);
      return () => clearInterval(intId);
    } else {
      setEvents(ws.events.length ? ws.events : events);
    }
  }, [ws.connected, ws.events]);

  const sortedEvents = [...events].sort((a, b) => b.lamportTimestamp - a.lamportTimestamp);

  const nodes = [...new Set(events.map(e => e.nodeId))];
  
  const chartData = events.map(e => ({
    x: e.lamportTimestamp,
    y: nodes.indexOf(e.nodeId),
    name: e.nodeId,
    desc: e.description,
    type: e.eventType
  }));

  return (
    <div>
      <div className="header">
        <h2>Clock Timeline</h2>
        <span className="badge" style={{ backgroundColor: 'var(--accent)', color: 'white', padding: '6px 12px' }}>Exp 3 - Distributed Event Timeline (Lamport Logical Clock)</span>
      </div>

      <div className="card" style={{ marginBottom: '20px', backgroundColor: 'rgba(67, 97, 238, 0.05)', borderLeft: '4px solid var(--accent)' }}>
        <p style={{ fontSize: '0.9rem', lineHeight: '1.6' }}>
          <strong>Lamport logical clocks</strong> provide causal event ordering in distributed systems. They do NOT synchronize physical time. 
          If event A causally precedes event B, then LC(A) &lt; LC(B). Events are ordered by Lamport timestamp to establish a consistent view of the distributed computation.
        </p>
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <h3 style={{ marginBottom: '15px' }}>Lamport Clock Scatter Timeline</h3>
        <div style={{ height: '300px' }}>
          <ResponsiveContainer width="100%" height="100%">
            <ScatterChart margin={{ top: 20, right: 20, bottom: 20, left: 20 }}>
              <XAxis type="number" dataKey="x" name="Lamport TS" />
              <YAxis type="number" dataKey="y" name="Node" tickFormatter={tick => nodes[tick] || ''} tickCount={nodes.length} />
              <Tooltip cursor={{ strokeDasharray: '3 3' }} formatter={(val, name, props) => [props.payload.desc, \`TS: \${props.payload.x}\`]} />
              <Scatter name="Events" data={chartData} fill="var(--accent)" />
            </ScatterChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid-2">
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Event Timeline (Visual)</h3>
          <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
            {sortedEvents.map((e, i) => (
              <div key={i} className="timeline-item">
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <strong style={{ fontSize: '1.2rem', color: 'var(--accent)' }}>LC: {e.lamportTimestamp}</strong>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{new Date(e.timestamp).toLocaleTimeString()}</span>
                </div>
                <div style={{ margin: '5px 0' }}>
                  <span className="badge" style={{ backgroundColor: 'var(--border)' }}>{e.nodeId}</span>
                  <span className="badge" style={{ backgroundColor: 'rgba(46,196,182,0.2)', color: 'var(--success)', marginLeft: '5px' }}>{e.eventType}</span>
                </div>
                <p style={{ fontSize: '0.9rem' }}>{e.description}</p>
                {e.taskId && <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Task: {e.taskId}</div>}
              </div>
            ))}
          </div>
        </div>
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Event Log Table</h3>
          <div className="table-wrapper" style={{ maxHeight: '400px', overflowY: 'auto' }}>
            <table className="table">
              <thead>
                <tr>
                  <th>LC TS</th>
                  <th>Node ID</th>
                  <th>Event Type</th>
                  <th>Description</th>
                </tr>
              </thead>
              <tbody>
                {sortedEvents.map((e, i) => (
                  <tr key={i}>
                    <td><strong>{e.lamportTimestamp}</strong></td>
                    <td>{e.nodeId}</td>
                    <td>{e.eventType}</td>
                    <td style={{ fontSize: '0.85rem' }}>{e.description}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
