export default function StatusBadge({ status }) {
  const map = {
    ONLINE: 'badge-online',
    OFFLINE: 'badge-offline',
    BUSY: 'badge-busy',
    IDLE: 'badge-idle',
    STRAGGLER: 'badge-straggler',
    COORDINATOR: 'badge-coordinator',
    RUNNING: 'badge-busy',
    COMPLETED: 'badge-online',
    FAILED: 'badge-offline',
    PENDING: 'badge-idle'
  };

  const className = map[status] || 'badge-idle';

  return <span className={\`badge \${className}\`}>{status}</span>;
}
