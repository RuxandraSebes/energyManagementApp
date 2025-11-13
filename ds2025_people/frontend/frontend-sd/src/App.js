import React from "react";
import { BrowserRouter as Router, Routes, Route, NavLink } from "react-router-dom";
import People from "./pages/People";
import Devices from "./pages/Devices";
import Auth from "./pages/Auth";
import "./App.css"; 

export default function App() {
  return (
    <Router>
      <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <header >
          <h1>Smart Management</h1>
          <div>
            <NavLink to="/people">People</NavLink>
            <NavLink to="/devices"> Devices</NavLink>
          </div>
        </header>

        <main>
          <Routes>
             <Route path="/" element={<Auth />} />   
            <Route path="/people" element={<People />} />
            <Route path="/devices" element={<Devices />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}
