import { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';

export function useWebSocket() {
  const [workers, setWorkers] = useState([]);
  const [systemStatus, setSystemStatus] = useState({});
  const [tasks, setTasks] = useState([]);
  const [events, setEvents] = useState([]);
  const [logs, setLogs] = useState([]);
  const [loadBalancing, setLoadBalancing] = useState({});
  const [replication, setReplication] = useState({});
  const [election, setElection] = useState({});
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;
    
    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/workers', msg => {
          try { setWorkers(JSON.parse(msg.body)); } catch(e) {}
        });
        client.subscribe('/topic/system', msg => {
          try { setSystemStatus(JSON.parse(msg.body)); } catch(e) {}
        });
        client.subscribe('/topic/tasks', msg => {
          try { setTasks(JSON.parse(msg.body)); } catch(e) {}
        });
        client.subscribe('/topic/events', msg => {
          try { setEvents(JSON.parse(msg.body)); } catch(e) {}
        });
        client.subscribe('/topic/logs', msg => {
          try { setLogs(JSON.parse(msg.body)); } catch(e) {}
        });
        client.subscribe('/topic/load-balancing', msg => {
          try { setLoadBalancing(JSON.parse(msg.body)); } catch(e) {}
        });
        client.subscribe('/topic/replication', msg => {
          try { setReplication(JSON.parse(msg.body)); } catch(e) {}
        });
        client.subscribe('/topic/election', msg => {
          try { setElection(JSON.parse(msg.body)); } catch(e) {}
        });
      },
      onDisconnect: () => setConnected(false),
      onWebSocketError: () => setConnected(false)
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  return { workers, systemStatus, tasks, events, logs, loadBalancing, replication, election, connected };
}
