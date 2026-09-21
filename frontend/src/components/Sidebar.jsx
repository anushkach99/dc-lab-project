import { NavLink } from 'react-router-dom';
import { useWebSocket } from '../hooks/useWebSocket';

export default function Sidebar() {
  const { connected } = useWebSocket();

  return (
    <aside className="sidebar">
      <div className="sidebar-title">⚡ DC Lab</div>
      <nav className="sidebar-nav">
        <NavLink to="/" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>📊</span> <span className="nav-text">Overview</span>
        </NavLink>
        <NavLink to="/workers" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>🖥️</span> <span className="nav-text">Worker Nodes</span>
        </NavLink>
        <NavLink to="/jobs" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>📋</span> <span className="nav-text">Jobs & Tasks</span>
        </NavLink>
        <NavLink to="/load-balancing" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>⚖️</span> <span className="nav-text">Load Balancing</span>
        </NavLink>
        <NavLink to="/clock" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>🕐</span> <span className="nav-text">Clock Timeline</span>
        </NavLink>
        <NavLink to="/election" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>👑</span> <span className="nav-text">Leader Election</span>
        </NavLink>
        <NavLink to="/replication" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>🔄</span> <span className="nav-text">Replication</span>
        </NavLink>
        <NavLink to="/experiments" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>🧪</span> <span className="nav-text">Experiment Lab</span>
        </NavLink>
        <NavLink to="/logs" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>📜</span> <span className="nav-text">System Logs</span>
        </NavLink>
        <NavLink to="/settings" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
          <span>⚙️</span> <span className="nav-text">Settings</span>
        </NavLink>
      </nav>
      <div className="sidebar-footer">
        <div className={`connection-dot ${connected ? 'connected' : ''}`}></div>
        <span>{connected ? 'WS Connected' : 'Disconnected'}</span>
      </div>
    </aside>
  );
}
