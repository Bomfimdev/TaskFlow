import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Dashboard = () => {
  const [tasks, setTasks] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          setError('Você precisa estar logado para ver suas tarefas.');
          return;
        }
        const response = await axios.get('http://localhost:8080/api/tasks', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setTasks(response.data);
        setError('');
      } catch (err) {
        setError('Erro ao carregar as tarefas.');
        setTasks([]);
      }
    };

    fetchTasks();
  }, []);

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <h1 className="text-3xl font-bold mb-6 text-center">Dashboard - TicTaref</h1>
      {error && <p className="text-red-500 text-center mb-4">{error}</p>}
      {tasks.length === 0 && !error ? (
        <p className="text-gray-500 text-center">Nenhuma tarefa encontrada.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {tasks.map((task) => (
            <div key={task.id} className="bg-white p-4 rounded-lg shadow-md">
              <h2 className="text-xl font-semibold">{task.title}</h2>
              <p className="text-gray-600">{task.description}</p>
              <p className="text-sm text-gray-500 mt-2">Status: {task.status}</p>
              <p className="text-sm text-gray-500">
                Prazo: {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'Sem prazo'}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Dashboard;