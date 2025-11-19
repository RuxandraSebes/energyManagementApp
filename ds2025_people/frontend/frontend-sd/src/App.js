import React from "react";
import { BrowserRouter as Router, Routes, Route, NavLink, Navigate } from "react-router-dom";
import People from "./pages/People";
import Devices from "./pages/Devices";
import Auth from "./pages/Auth";
import Dashboard from "./pages/UserDashboard"; // <-- NOU
import { AuthProvider, useAuth } from "./contexts/AuthContext"; // <-- NOU
import "./App.css"; 

// Component for conditional navigation links
const Navigation = () => {
  const { isAdmin, isAuthenticated, logout } = useAuth();

  const handleLogout = () => {
    logout();
    // Force refresh to clear all application state and redirect to Auth
    window.location.href = "/"; 
  };

  if (!isAuthenticated) return null;

  return (
    <div className="flex space-x-4">
      {isAdmin ? (
        <>
          <NavLink to="/people" className={({ isActive }) => isActive ? "font-bold text-blue-300" : "hover:text-gray-300"}>People (Admin)</NavLink>
          <NavLink to="/devices" className={({ isActive }) => isActive ? "font-bold text-blue-300" : "hover:text-gray-300"}>Devices (Admin)</NavLink>
        </>
      ) : (
        // User views are centralized in the dashboard
        <NavLink to="/dashboard" className={({ isActive }) => isActive ? "font-bold text-blue-300" : "hover:text-gray-300"}>My Dashboard</NavLink>
      )}
      <button 
        onClick={handleLogout} 
        className="text-sm font-medium text-red-400 hover:text-red-300 transition duration-150"
      >
        Logout
      </button>
    </div>
  );
};

// Component that protects routes from unauthenticated access
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    // Redirect to login if not authenticated
    return <Navigate to="/" replace />;
  }
  return children;
};

// Main App component wrapper for the context
export default function AppWrapper() {
  return (
    <Router>
      <AuthProvider>
        <App />
      </AuthProvider>
    </Router>
  );
}

// Actual App structure
function App() {
  const { isAuthenticated, isAdmin } = useAuth();
  
  // Logic to redirect authenticated users who land on the root path
  const handleRootRedirect = () => {
    if (isAuthenticated) {
        return <Navigate to={isAdmin ? "/people" : "/dashboard"} replace />;
    }
    return <Auth />;
  }

  return (
    <div className="flex flex-col min-h-screen">
      <header className="flex justify-between items-center bg-gray-800 text-white p-4 shadow-md">
        <h1 className="text-xl font-bold">Smart Management</h1>
        <Navigation />
      </header>

      <main className="flex-grow p-4">
        <Routes>
          {/* Public route (handles login/register and redirects if authenticated) */}
          <Route path="/" element={handleRootRedirect()} />   
          
          {/* Protected Admin Routes (Admin sees all, User redirect handled by Auth logic) */}
          <Route path="/people" element={<ProtectedRoute><People /></ProtectedRoute>} />
          <Route path="/devices" element={<ProtectedRoute><Devices /></ProtectedRoute>} />
          
          {/* Protected User Dashboard Route */}
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          
        </Routes>
      </main>
    </div>
  );
}