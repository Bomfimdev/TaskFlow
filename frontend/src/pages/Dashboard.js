import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const [tasks, setTasks] = useState([]);
  const [error, setError] = useState('');
  const [dateError, setDateError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [newTask, setNewTask] = useState({ title: '', description: '', dueDate: '', status: 'Pendente', archived: false });
  const [editingTask, setEditingTask] = useState(null);
  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  const fetchTasks = async () => {
    try {
      if (!token) {
        setError('Você precisa estar logado para ver suas tarefas. Token não encontrado.');
        navigate('/login');
        return;
      }
      console.log('Token enviado:', token);
      const response = await axios.get('http://localhost:8080/api/tasks?includeArchived=true', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      console.log('Tarefas recebidas:', response.data);
      setTasks(response.data);
      console.log('Estado tasks atualizado:', response.data); // Log adicional para verificar o estado
      setError('');
    } catch (err) {
      console.error('Erro na requisição:', err);
      setError(`Erro ao carregar as tarefas: ${err.message}`);
      setTasks([]);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, []);

  const validateDueDate = (dueDate) => {
    if (!dueDate) return true;
    const selectedDate = new Date(dueDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    selectedDate.setHours(0, 0, 0, 0);
    return selectedDate >= today;
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    setDateError('');
    setSuccessMessage('');

    if (!validateDueDate(newTask.dueDate)) {
      setDateError('A data de vencimento deve ser no mesmo dia ou uma data futura.');
      return;
    }

    try {
      if (!token) {
        setError('Você precisa estar logado para criar tarefas.');
        return;
      }
      console.log('Token enviado para criar tarefa:', token);
      console.log('Corpo da requisição:', JSON.stringify(newTask, null, 2));
      const response = await axios.post('http://localhost:8080/api/tasks', newTask, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      console.log('Resposta da criação da tarefa:', response.data);
      setNewTask({ title: '', description: '', dueDate: '', status: 'Pendente', archived: false });
      setDateError('');
      setSuccessMessage('Tarefa criada com sucesso!');
      setTimeout(() => setSuccessMessage(''), 3000);
      fetchTasks();
    } catch (err) {
      console.error('Erro ao criar tarefa:', err.response || err);
      setError(`Erro ao criar tarefa: ${err.message}`);
    }
  };

  const handleEditTask = (task) => {
    setEditingTask(task);
    setNewTask({
      title: task.title,
      description: task.description,
      dueDate: task.dueDate ? task.dueDate.split('T')[0] : '',
      status: task.status,
      archived: task.archived,
    });
    setDateError('');
    setSuccessMessage('');
  };

  const handleUpdateTask = async (e) => {
    e.preventDefault();
    setDateError('');
    setSuccessMessage('');

    if (!validateDueDate(newTask.dueDate)) {
      setDateError('A data de vencimento deve ser no mesmo dia ou uma data futura.');
      return;
    }

    try {
      if (!token) {
        setError('Você precisa estar logado para atualizar tarefas.');
        return;
      }
      const updatedTask = {
        title: newTask.title,
        description: newTask.description,
        dueDate: newTask.dueDate,
        status: newTask.status,
        archived: newTask.archived,
      };
      await axios.put(`http://localhost:8080/api/tasks/${editingTask.id}`, updatedTask, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setEditingTask(null);
      setNewTask({ title: '', description: '', dueDate: '', status: 'Pendente', archived: false });
      setDateError('');
      setSuccessMessage('Tarefa atualizada com sucesso!');
      setTimeout(() => setSuccessMessage(''), 3000);
      fetchTasks();
    } catch (err) {
      console.error('Erro ao atualizar tarefa:', err.response || err);
      setError(`Erro ao atualizar tarefa: ${err.message}`);
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Tem certeza que deseja excluir esta tarefa?')) {
      return;
    }

    try {
      if (!token) {
        setError('Você precisa estar logado para excluir tarefas.');
        return;
      }
      await axios.delete(`http://localhost:8080/api/tasks/${taskId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setSuccessMessage('Tarefa excluída com sucesso!');
      setTimeout(() => setSuccessMessage(''), 3000);
      fetchTasks();
    } catch (err) {
      console.error('Erro ao excluir tarefa:', err.response || err);
      setError(`Erro ao excluir tarefa: ${err.message}`);
    }
  };

  const handleArchiveTask = async (taskId) => {
    try {
      if (!token) {
        setError('Você precisa estar logado para arquivar tarefas.');
        return;
      }
      await axios.post(`http://localhost:8080/api/tasks/${taskId}/archive`, {}, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setSuccessMessage('Tarefa arquivada com sucesso!');
      setTimeout(() => setSuccessMessage(''), 3000);
      fetchTasks();
    } catch (err) {
      console.error('Erro ao arquivar tarefa:', err.response || err);
      setError(`Erro ao arquivar tarefa: ${err.message}`);
    }
  };

  const handleUnarchiveTask = async (taskId) => {
    try {
      if (!token) {
        setError('Você precisa estar logado para desarquivar tarefas.');
        return;
      }
      const response = await axios.post(`http://localhost:8080/api/tasks/${taskId}/unarchive`, {}, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      console.log('Resposta do desarquivamento:', response.data);
      setSuccessMessage('Tarefa desarquivada com sucesso!');
      setTimeout(() => setSuccessMessage(''), 3000);
      fetchTasks();
    } catch (err) {
      console.error('Erro ao desarquivar tarefa:', err.response || err);
      setError(`Erro ao desarquivar tarefa: ${err.message}`);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-center">Dashboard - TicTaref</h1>
        <button
          onClick={handleLogout}
          className="bg-red-500 text-white px-4 py-1 rounded-md hover:bg-red-600 transition duration-300"
        >
          Sair
        </button>
      </div>
      <div className="mb-6">
        <h2 className="text-xl font-semibold mb-4">{editingTask ? 'Editar Tarefa' : 'Criar Nova Tarefa'}</h2>
        <form onSubmit={editingTask ? handleUpdateTask : handleCreateTask} className="bg-white p-4 rounded-lg shadow-md">
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
              onChange={(e) => {
                setNewTask({ ...newTask, dueDate: e.target.value });
                setDateError('');
              }}
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {dateError && <p className="text-red-500 text-sm mt-1">{dateError}</p>}
          </div>
          <button
            type="submit"
            className="w-full bg-blue-500 text-white p-2 rounded-md hover:bg-blue-600 transition duration-300"
          >
            {editingTask ? 'Atualizar Tarefa' : 'Criar Tarefa'}
          </button>
          {editingTask && (
            <button
              type="button"
              onClick={() => {
                setEditingTask(null);
                setNewTask({ title: '', description: '', dueDate: '', status: 'Pendente', archived: false });
                setDateError('');
              }}
              className="w-full bg-gray-500 text-white p-2 rounded-md mt-2 hover:bg-gray-600 transition duration-300"
            >
              Cancelar
            </button>
          )}
        </form>
      </div>
      {successMessage && (
        <p className="text-green-500 text-center mb-4">{successMessage}</p>
      )}
      {error && <p className="text-red-500 text-center mb-4">{error}</p>}
      {tasks.length === 0 && !error ? (
        <p className="text-gray-500 text-center">Nenhuma tarefa encontrada.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {tasks.map((task) => {
            const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'Concluída';
            return (
              <div key={task.id} className="bg-white p-4 rounded-lg shadow-md">
                <h2 className="text-xl font-semibold">{task.title}</h2>
                <p className="text-gray-600">{task.description}</p>
                <p className="text-sm text-gray-500 mt-2">Status: {task.status}</p>
                <p className="text-sm text-gray-500">
                  Prazo: {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'Sem prazo'}
                </p>
                {isOverdue && <p className="text-sm text-red-500 font-semibold">Vencida</p>}
                <p className="text-sm text-gray-500">
                  {task.archived ? 'Arquivada' : 'Ativa'}
                </p>
                <div className="mt-2 flex space-x-2">
                  <button
                    onClick={() => handleEditTask(task)}
                    className="bg-yellow-500 text-white px-3 py-1 rounded-md hover:bg-yellow-600 transition duration-300"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => handleDeleteTask(task.id)}
                    className="bg-red-500 text-white px-3 py-1 rounded-md hover:bg-red-600 transition duration-300"
                  >
                    Excluir
                  </button>
                  {task.archived ? (
                    <button
                      onClick={() => handleUnarchiveTask(task.id)}
                      className="bg-green-500 text-white px-3 py-1 rounded-md hover:bg-green-600 transition duration-300"
                    >
                      Desarquivar
                    </button>
                  ) : (
                    <button
                      onClick={() => handleArchiveTask(task.id)}
                      className="bg-gray-500 text-white px-3 py-1 rounded-md hover:bg-gray-600 transition duration-300"
                    >
                      Arquivar
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Dashboard;