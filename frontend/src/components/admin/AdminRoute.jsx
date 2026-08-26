import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import AdminLayout from './AdminLayout';

function AdminRoute() {
  const isAdminAuthed = localStorage.getItem('adminAuth') === 'true';

  if (!isAdminAuthed) {
    return <Navigate to="/admin/login" replace />;
  }

  return <AdminLayout />;
}

export default AdminRoute;
