import React, { useState, useEffect } from "react";
import { getPeople } from "../api/peopleApi";
import { getDevices } from "../api/devicesApi";
import { useAuth } from "../contexts/AuthContext";
import "../App.css";

// Displays the user's single profile and their list of devices
export default function UserDashboard() { // Renamed for consistency with error log
    // --- 1. ALL HOOKS MUST BE UNCONDITIONAL AT THE TOP ---
    const { isUser, isAuthenticated } = useAuth();
    const [profile, setProfile] = useState(null);
    const [devices, setDevices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // --- 2. EFFECT HOOK (ALWAYS called) ---
    useEffect(() => {
        // If the route is accessed but the role is wrong (e.g., admin), stop loading immediately.
        if (!isAuthenticated || !isUser) {
            setLoading(false);
            setError("Access Denied for this role.");
            return;
        }

        const loadData = async () => {
            setLoading(true);
            setError(null);
            try {
                // 1. Fetch Profile (Backend filters this to only return the user's single profile)
                const peopleData = await getPeople();
                if (peopleData.length === 1) {
                    setProfile(peopleData[0]);
                } else if (peopleData.length === 0) {
                    setError("Profile data not found. Please contact support.");
                } else {
                    setError("Multiple profiles found, authentication error.");
                }

                // 2. Fetch Devices (Backend filters this to only return the user's devices)
                const devicesData = await getDevices();
                setDevices(devicesData);

            } catch (err) {
                console.error("Dashboard data fetch error:", err);
                setError("Failed to load dashboard data. Please log out and log in again.");
            } finally {
                setLoading(false);
            }
        };

        loadData();
    }, [isAuthenticated, isUser]); 

    // --- 3. CONDITIONAL RENDERING ---
    // If access was denied inside the effect, or if the data is loading:
    if (loading) {
        return <div className="text-center mt-10 text-blue-600">Loading your data...</div>;
    }
    
    // Check for explicit error messages (including the Access Denied message set in useEffect)
    if (error) {
        return <div className="text-center mt-10 text-red-600">Error: {error}</div>;
    }

    const renderDeviceCard = (d) => (
        <div key={d.id} className="bg-white p-4 rounded-xl shadow-lg border border-gray-200">
            <h3 className="text-xl font-bold text-gray-800">{d.name}</h3>
            <p className="text-sm text-gray-600">ID: {d.id.substring(0, 8)}...</p>
            <p>Location: <span className="font-medium">{d.location}</span></p>
            <p>Max Consumption: <span className="font-medium">{d.maxConsumption} kWh</span></p>
        </div>
    );

    return (
        <div className="p-6 max-w-7xl mx-auto">
            <h1 className="text-4xl font-extrabold text-gray-900 mb-8 border-b pb-2">User Dashboard</h1>

            {/* Profile Section */}
            <section className="mb-10 p-6 bg-blue-50 rounded-xl shadow-md">
                <h2 className="text-2xl font-semibold text-blue-800 mb-4">My Profile</h2>
                {profile ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-gray-700">
                        <p><span className="font-medium">Name:</span> {profile.name}</p>
                        <p><span className="font-medium">Age:</span> {profile.age}</p>
                        <p><span className="font-medium">Address:</span> {profile.address}</p>
                        <p><span className="font-medium">Person ID:</span> {profile.id}</p>
                    </div>
                ) : (
                    <p className="text-red-500">Profile data missing.</p>
                )}
            </section>

            {/* Devices Section */}
            <section>
                <h2 className="text-2xl font-semibold text-gray-800 mb-4">My Devices ({devices.length})</h2>
                {devices.length > 0 ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                        {devices.map(renderDeviceCard)}
                    </div>
                ) : (
                    <p className="text-gray-500">You currently have no devices assigned.</p>
                )}
            </section>
        </div>
    );
}