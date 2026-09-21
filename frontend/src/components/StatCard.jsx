export default function StatCard({ title, value, subtitle, color = 'var(--accent)', icon }) {
  return (
    <div className="card stat-card" style={{ borderLeftColor: color }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <h3 style={{ fontSize: '1rem', color: 'var(--text-secondary)' }}>{title}</h3>
        {icon && <span style={{ fontSize: '1.5rem' }}>{icon}</span>}
      </div>
      <div className="stat-value">{value}</div>
      {subtitle && <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '5px' }}>{subtitle}</div>}
    </div>
  );
}
