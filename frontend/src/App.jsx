import { Routes, Route } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import OverviewDashboard from './pages/OverviewDashboard';
import WorkerNodes from './pages/WorkerNodes';
import JobsTasks from './pages/JobsTasks';
import LoadBalancing from './pages/LoadBalancing';
import ClockTimeline from './pages/ClockTimeline';
import LeaderElection from './pages/LeaderElection';
import Replication from './pages/Replication';
import ExperimentLab from './pages/ExperimentLab';
import SystemLogs from './pages/SystemLogs';
import Settings from './pages/Settings';
import './App.css';

function App() {
  const [isDark, setIsDark] = useState(false);

  useEffect(() => {
    document.body.setAttribute('data-theme', isDark ? 'dark' : 'light');
  }, [isDark]);

  const toggleTheme = () => setIsDark(!isDark);

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="content">
        <header className="header">
          <div></div>
          <button className="theme-toggle" onClick={toggleTheme}>
            {isDark ? '☀️' : '🌙'}
          </button>
        </header>
        <Routes>
          <Route path="/" element={<OverviewDashboard />} />
          <Route path="/workers" element={<WorkerNodes />} />
          <Route path="/jobs" element={<JobsTasks />} />
          <Route path="/load-balancing" element={<LoadBalancing />} />
          <Route path="/clock" element={<ClockTimeline />} />
          <Route path="/election" element={<LeaderElection />} />
          <Route path="/replication" element={<Replication />} />
          <Route path="/experiments" element={<ExperimentLab />} />
          <Route path="/logs" element={<SystemLogs />} />
          <Route path="/settings" element={<Settings />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
