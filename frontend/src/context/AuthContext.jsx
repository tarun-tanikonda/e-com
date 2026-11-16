import React, { createContext, useContext, useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';

const AuthContext = createContext(null);

const getStoredToken = () => {
  const t = localStorage.getItem('token');
  return t && t !== 'undefined' && t !== 'null' && t.trim() !== '' ? t.trim() : null;
};

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(getStoredToken);
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem('user');
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(true);

  // Restore and verify user session on initial boot only
  useEffect(() => {
    const restoreSession = async () => {
      const storedToken = getStoredToken();
      if (!storedToken) {
        setUser(null);
        setToken(null);
        setLoading(false);
        return;
      }
      try {
        const response = await axiosClient.get('/api/v1/users/me');
        if (response.data) {
          setUser(response.data);
          localStorage.setItem('user', JSON.stringify(response.data));
        }
      } catch (err) {
        console.warn('Session verification notice on boot:', err.message);
        if (err.response && err.response.status === 401) {
          logout();
        }
      } finally {
        setLoading(false);
      }
    };

    restoreSession();
  }, []);

  const login = (jwtToken, userData) => {
    if (!jwtToken) return;
    const cleanToken = jwtToken.trim();
    localStorage.setItem('token', cleanToken);
    setToken(cleanToken);
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        role: user?.role || null,
        login,
        logout,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
