import api from './api';

export const authService = {
  login: async (username, password) => {
    const response = await api.post('/auth/signup', { username, password });
    if (response.data.token) {
      localStorage.setItem('username', response.data.username);
    }
    return response.data;
  },

  register: async (userData) => {
    const response = await api.post('/auth/signin', userData);
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('username');
  },

  getCurrentUser: () => {
    return localStorage.getItem('username');
  },

  getToken: () => {
    return localStorage.getItem('token');
  }
}; 