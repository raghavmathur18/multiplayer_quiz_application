import { useParams } from 'react-router-dom';

export default function QuizArena() {
  const { quizId } = useParams();

  return (
    <div className="container" style={{ marginTop: '2rem' }}>
      <div className="card">
        <h2>Quiz Arena</h2>
        <p>Loading quiz {quizId}...</p>
      </div>
    </div>
  );
}
