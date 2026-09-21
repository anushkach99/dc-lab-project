import { useState, useEffect } from 'react';
import * as api from '../services/api';

export default function Settings() {
  const [settings, setSettings] = useState({
    mode: 'DEMO',
    network: { masterIp: 'localhost', workerIps: 'localhost', rmiPort: 1099, restPort: 8080 },
    scheduling: { stragglerThreshold: 0.5, maxStaleness: 5, heartbeatInterval: 5000 },
    jobConfig: { defaultTaskCount: 100, defaultComplexity: 'MEDIUM' }
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.getSettings().then(res => { if (res) setSettings(res); }).catch(()=>{});
  }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      await api.updateSettings(settings);
      alert('Settings saved successfully for current session');
    } catch(e) {
      alert('Failed to save settings');
    }
    setSaving(false);
  };

  const handleStartDemo = async () => { await api.startDemo(); alert('Demo mode started'); };
  const handleStopDemo = async () => { await api.stopDemo(); alert('Demo mode stopped'); };

  const handleChange = (category, field, value) => {
    if (category) {
      setSettings(prev => ({ ...prev, [category]: { ...prev[category], [field]: value } }));
    } else {
      setSettings(prev => ({ ...prev, [field]: value }));
    }
  };

  return (
    <div>
      <div className="header">
        <h2>System Settings</h2>
      </div>

      <div className="card" style={{ marginBottom: '20px', backgroundColor: 'rgba(255, 159, 28, 0.1)', borderLeft: '4px solid var(--warning)' }}>
        <p style={{ fontSize: '0.9rem' }}>Settings are loaded from application.yml on startup. Runtime changes affect current session only.</p>
      </div>

      <div className="grid-2">
        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Mode Configuration</h3>
          <div style={{ marginBottom: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px' }}>Operation Mode</label>
            <select style={{ width: '100%', padding: '8px', border: '1px solid var(--border)', borderRadius: '4px' }} value={settings.mode} onChange={e => handleChange(null, 'mode', e.target.value)}>
              <option value="DEMO">Demo (Single Machine)</option>
              <option value="LAN">LAN (Distributed)</option>
            </select>
          </div>
          <div style={{ display: 'flex', gap: '10px' }}>
            <button className="btn btn-success" style={{ flex: 1 }} onClick={handleStartDemo}>Start Demo Workers</button>
            <button className="btn btn-danger" style={{ flex: 1 }} onClick={handleStopDemo}>Stop Demo Workers</button>
          </div>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Network</h3>
          <div className="grid-2">
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontSize: '0.85rem' }}>Master IP</label>
              <input type="text" style={{ width: '100%', padding: '8px', border: '1px solid var(--border)', borderRadius: '4px' }} value={settings.network.masterIp} onChange={e => handleChange('network', 'masterIp', e.target.value)} />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontSize: '0.85rem' }}>RMI Port</label>
              <input type="number" style={{ width: '100%', padding: '8px', border: '1px solid var(--border)', borderRadius: '4px' }} value={settings.network.rmiPort} onChange={e => handleChange('network', 'rmiPort', parseInt(e.target.value))} />
            </div>
          </div>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Scheduling & LB</h3>
          <div style={{ marginBottom: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontSize: '0.85rem' }}>Straggler Threshold ({settings.scheduling.stragglerThreshold})</label>
            <input type="range" min="0.1" max="1.0" step="0.1" style={{ width: '100%' }} value={settings.scheduling.stragglerThreshold} onChange={e => handleChange('scheduling', 'stragglerThreshold', parseFloat(e.target.value))} />
          </div>
          <div className="grid-2">
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontSize: '0.85rem' }}>Max Staleness</label>
              <input type="number" style={{ width: '100%', padding: '8px', border: '1px solid var(--border)', borderRadius: '4px' }} value={settings.scheduling.maxStaleness} onChange={e => handleChange('scheduling', 'maxStaleness', parseInt(e.target.value))} />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontSize: '0.85rem' }}>Heartbeat Int (ms)</label>
              <input type="number" style={{ width: '100%', padding: '8px', border: '1px solid var(--border)', borderRadius: '4px' }} value={settings.scheduling.heartbeatInterval} onChange={e => handleChange('scheduling', 'heartbeatInterval', parseInt(e.target.value))} />
            </div>
          </div>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: '15px' }}>Job Config</h3>
          <div className="grid-2">
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontSize: '0.85rem' }}>Default Tasks</label>
              <input type="number" style={{ width: '100%', padding: '8px', border: '1px solid var(--border)', borderRadius: '4px' }} value={settings.jobConfig.defaultTaskCount} onChange={e => handleChange('jobConfig', 'defaultTaskCount', parseInt(e.target.value))} />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontSize: '0.85rem' }}>Complexity</label>
              <select style={{ width: '100%', padding: '8px', border: '1px solid var(--border)', borderRadius: '4px' }} value={settings.jobConfig.defaultComplexity} onChange={e => handleChange('jobConfig', 'defaultComplexity', e.target.value)}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </div>
          </div>
          <div style={{ marginTop: '20px' }}>
            <button className="btn btn-primary" style={{ width: '100%' }} onClick={handleSave} disabled={saving}>
              {saving ? 'Saving...' : 'Save All Settings'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
