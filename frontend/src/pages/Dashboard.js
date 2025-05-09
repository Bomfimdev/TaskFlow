import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const [tasks, setTasks] = useState([]);
  const [error, setError] = useState('');
  const [newTask, setNewTask] = useState({ title: '', description: '', dueDate: '', status: 'Pendente', archived: false });
  const navigate = useNavigate();

  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('Você precisa estar logado para ver suas tarefas. Token não encontrado.');
        return;
      }
      console.log('Token enviado:', token);
      const response = await axios.get('http://localhost:8080/api/tasks', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setTasks(response.data);
      setError('');
    } catch (err) {
      console.error('Erro na requisição:', err);
      setError(`Erro ao carregar as tarefas: ${err.message}`);
      setTasks([]);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('Você precisa estar logado para criar tarefas.');
        return;
      }
      console.log('Token enviado para criar tarefa:', token);
      console.log('Corpo da requisição:', JSON.stringify(newTask, null, 2)); // Log detalhado do objeto
      await axios.post('http://localhost:8080/api/tasks', newTask, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setNewTask({ title: '', description: '', dueDate: '', status: 'Pendente', archived: false });
      fetchTasks();
    } catch (err) {
      console.error('Erro ao criar tarefa:', err.response || err);
      setError(`Erro ao criar tarefa: ${err.message}`);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-center">Dashboard - TicTaref</h1>
        <button
          onClick={handleLogout}
          className="bg-red-500 text-white px-4 py-2 rounded-md hover:bg-red-600 transition duration-300"
        >
          Sair
        </button>
      </div>
      <div className="mb-6">
        <h2 className="text-xl font-semibold mb-4">Criar Nova Tarefa</h2>
        <form onSubmit={handleCreateTask} className="bg-white p-4 rounded-lg shadow-md">
          <div className="mb-4">
            <label className="block text-gray-700 mb-2" htmlFor="title">
              Título
            </label>
            <input
              type="text"
              id="title"
              value={newTask.title}
              onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Digite o título da tarefa"
              required
            />
          </div>
          <div className="mb-4">
            <label className="block text-gray-700 mb-2" htmlFor="description">
              Descrição
            </label>
            <textarea
              id="description"
              value={newTask.description}
              onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Digite a descrição da tarefa"
            />
          </div>
          <div className="mb-4">
            <label className="block text-gray-700 mb-2" htmlFor="dueDate">
              Prazo
            </label>
            <input
              type="date"
              id="dueDate"
              value={newTask.dueDate}
              onChange={(e) => setNewTask({ ...newTask, dueDate: e.target.value })}
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <button
            type="submit"
            className="w-full bg-blue-500 text-white p-2 rounded-md hover:bg-blue-600 transition duration-300"
          >
            Criar Tarefa
          </button>
        </form>
      </div>
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