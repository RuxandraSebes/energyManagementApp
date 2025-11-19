import React, { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext(null);

// Minimal utility to decode JWT and get the role
const decodeJwt = (token) => {
    try {
        // Get the payload (second part of the JWT)
        const base64Url = token.split('.')[1];
        // Replace URL-safe characters and decode Base64
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const payload = JSON.parse(atob(base64));
        return payload;
    } catch (e) {
        // Token is invalid, expired, or malformed
        return null;
    }
};

export const AuthProvider = ({ children }) => {
    const [userRole, setUserRole] = useState(null);

    // Check for existing token on initial load
    useEffect(() => {
        const token = localStorage.getItem('jwt');
        if (token) {
            const payload = decodeJwt(token);
            if (payload && payload.role) {
                setUserRole(payload.role.toLowerCase());
            }
        }
    }, []);

    const login = (token) => {
        localStorage.setItem('jwt', token);
        const payload = decodeJwt(token);
        if (payload && payload.role) {
            const role = payload.role.toLowerCase();
            setUserRole(role);
            return role;
        }
        setUserRole(null);
        return null;
    };

    const logout = () => {
        localStorage.removeItem('jwt');
        setUserRole(null);
    };

    const isAdmin = userRole === 'admin';
    const isUser = userRole === 'user';
    const isAuthenticated = userRole !== null;

    return (
        <AuthContext.Provider value={{ userRole, isAdmin, isUser, isAuthenticated, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);