import React, { useState } from "react";
import "../App.css";

export default function Auth() {
  const [isLogin, setIsLogin] = useState(true);
  const [form, setForm] = useState({ username: "", password: "", role: "user" });

  const BASE_URL = "http://localhost/auth";

  const handleSubmit = async (e) => {
    e.preventDefault();

    const endpoint = isLogin ? "login" : "register";
    const body = new URLSearchParams(form);

    const res = await fetch(`${BASE_URL}/${endpoint}`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body,
    });

    const text = await res.text();

    if (isLogin) {
      if (text.startsWith("ey")) {
        localStorage.setItem("jwt", text);
        alert("Login success!");
      } else {
        alert("error" + text);
      }
    } else {
      alert(text);
      if (text.toLowerCase().includes("success")) setIsLogin(true);
    }
  };

  return (
    <div style={{ padding: "2rem" }}>
      <div>
        <h2>{isLogin ? "Login" : "Register"}</h2>

        <form
          onSubmit={handleSubmit}
          style={{ display: "flex", flexDirection: "column", gap: "0.5rem", width: "250px" }}
        >
          <input
            placeholder="Username"
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
          />
          <input
            placeholder="Password"
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
          {!isLogin && (
            <input
              placeholder="Role (user/admin)"
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}
            />
          )}
          <button type="submit">{isLogin ? "Login" : "Register"}</button>
        </form>

        <p style={{ marginTop: "1rem" }}>
          {isLogin ? (
            <>
              Don't have an account?{" "}
              <button
                type="button"
                onClick={() => setIsLogin(false)}
                style={{ border: "none", background: "none", color: "blue", cursor: "pointer" }}
              >
                Register
              </button>
            </>
          ) : (
            <>
              Already have an account?{" "}
              <button
                type="button"
                onClick={() => setIsLogin(true)}
                style={{ border: "none", background: "none", color: "blue", cursor: "pointer" }}
              >
                Login
              </button>
            </>
          )}
        </p>
      </div>
    </div>
  );
}
