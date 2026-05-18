import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function StudentDashboard() {
  const [classes, setClasses] = useState([]);
  const [classCode, setClassCode] = useState('');
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      navigate('/login');
      return;
    }
    const u = JSON.parse(storedUser);
    setUser(u);
    fetchClasses(u.id);
  }, [navigate]);

  const fetchClasses = async (studentId) => {
    try {
      const res = await axios.get(`/api/classes/student/${studentId}`);
      setClasses(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleJoinClass = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`/api/classes/join/${user.id}/${classCode}`);
      setClassCode('');
      fetchClasses(user.id);
    } catch (err) {
      alert('Failed to join class. Check the class code.');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    navigate('/login');
  };

  if (!user) return null;

  return (
    <div>
      <div className="glass-header" style={{ padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Student Dashboard</h2>
        <div>
          <span style={{ marginRight: '1rem' }}>Welcome, {user.name}</span>
          <button className="btn btn-primary" onClick={handleLogout}>Logout</button>
        </div>
      </div>
      <div className="container" style={{ marginTop: '2rem' }}>
        <div className="card" style={{ marginBottom: '2rem' }}>
          <h3>Join a Class</h3>
          <form onSubmit={handleJoinClass} style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
            <input 
              type="text" 
              className="input-field" 
              placeholder="Class Code (e.g. A1B2C3)" 
              value={classCode}
              onChange={(e) => setClassCode(e.target.value)}
              style={{ marginBottom: 0 }}
              required 
            />
            <button type="submit" className="btn btn-accent" style={{ whiteSpace: 'nowrap' }}>Join Class</button>
          </form>
        </div>

        <h3>Your Joined Classes</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem', marginTop: '1rem' }}>
          {classes.map(c => (
            <div key={c.id} className="card">
              <h4>{c.name}</h4>
              <p style={{ color: 'var(--text-muted)' }}>Teacher: {c.teacher?.name}</p>
              <button className="btn btn-primary" style={{ marginTop: '1rem', width: '100%' }}>View Quizzes</button>
            </div>
          ))}
          {classes.length === 0 && <p>You haven't joined any classes yet.</p>}
        </div>
      </div>
    </div>
  );
}
