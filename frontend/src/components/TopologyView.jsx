import StatusBadge from './StatusBadge';

export default function TopologyView({ workers = [], coordinator = 'MASTER' }) {
  return (
    <div className="topology">
      <div className="topology-master">
        <h3>👑 {coordinator}</h3>
        <StatusBadge status="COORDINATOR" />
      </div>
      <div className="topology-workers">
        {workers.map(w => (
          <div key={w.id} className="topology-worker">
            <h4>{w.id}</h4>
            <div style={{ margin: '10px 0' }}>
              <span className="badge" style={{ backgroundColor: 'var(--border)' }}>{w.type || 'NODE'}</span>
            </div>
            <StatusBadge status={w.status} />
          </div>
        ))}
        {workers.length === 0 && <p>No workers connected</p>}
      </div>
    </div>
  );
}
