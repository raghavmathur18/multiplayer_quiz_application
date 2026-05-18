import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function TeacherDashboard() {
  const [classes, setClasses] = useState([]);
  const [newClassName, setNewClassName] = useState('');
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

  const fetchClasses = async (teacherId) => {
    try {
      const res = await axios.get(`/api/classes/teacher/${teacherId}`);
      setClasses(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleCreateClass = async (e) => {
    e.preventDefault();
    try {
      await axios.post('/api/classes/create', {
        name: newClassName,
        teacher: { id: user.id }
      });
      setNewClassName('');
      fetchClasses(user.id);
    } catch (err) {
      console.error('Failed to create class');
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
        <h2>Teacher Dashboard</h2>
        <div>
          <span style={{ marginRight: '1rem' }}>Welcome, {user.name}</span>
          <button className="btn btn-primary" onClick={handleLogout}>Logout</button>
        </div>
      </div>
      <div className="container" style={{ marginTop: '2rem' }}>
        <div className="card" style={{ marginBottom: '2rem' }}>
          <h3>Create a New Class</h3>
          <form onSubmit={handleCreateClass} style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
            <input 
              type="text" 
              className="input-field" 
              placeholder="Class Name" 
              value={newClassName}
              onChange={(e) => setNewClassName(e.target.value)}
              style={{ marginBottom: 0 }}
              required 
            />
            <button type="submit" className="btn btn-accent" style={{ whiteSpace: 'nowrap' }}>Create Class</button>
          </form>
        </div>

        <h3>Your Classes</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem', marginTop: '1rem' }}>
          {classes.map(c => (
            <div key={c.id} className="card">
              <h4>{c.name}</h4>
              <p style={{ color: 'var(--text-muted)' }}>Code: <strong style={{ color: 'var(--accent)' }}>{c.classCode}</strong></p>
              <button className="btn btn-primary" style={{ marginTop: '1rem', width: '100%' }}>Manage Quizzes</button>
            </div>
          ))}
          {classes.length === 0 && <p>No classes found. Create one above!</p>}
        </div>
      </div>
    </div>
  );
}
