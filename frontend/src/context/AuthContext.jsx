import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);

  const login = (email, password) => {
    // Mock login logic, will connect to backend later
    setIsAuthenticated(true);
    setUser({ email, name: 'Клієнт' });
  };

  const register = (name, email, password) => {
    // Mock register logic
    setIsAuthenticated(true);
    setUser({ email, name });
  };

  const logout = () => {
    setIsAuthenticated(false);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
