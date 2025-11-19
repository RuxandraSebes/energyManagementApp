// src/api/peopleApi.js

const BASE_URL = "/people";

// Reusable function to get headers with JWT
const getAuthHeaders = (contentType) => {
  const token = localStorage.getItem("jwt");
  const headers = {};
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  if (contentType) {
    headers["Content-Type"] = contentType;
  }
  return headers;
};

export const getPeople = async () => {
  // ADD AUTHORIZATION HEADER
  const res = await fetch(BASE_URL, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error("Failed to fetch people");
  return await res.json();
};

export const addPerson = async (person) => {
  // ADD AUTHORIZATION HEADER
  const res = await fetch(BASE_URL, {
    method: "POST",
    headers: getAuthHeaders("application/json"),
    body: JSON.stringify(person),
  });
  if (!res.ok) throw new Error("Failed to add person");
  return await res.json();
};

export const updatePerson = async (person) => {
  // ADD AUTHORIZATION HEADER
  const res = await fetch(`${BASE_URL}/${person.id}`, {
    method: "PUT",
    headers: getAuthHeaders("application/json"),
    body: JSON.stringify(person),
  });
  // Note: The original returned res.json() without checking res.ok()
  if (!res.ok) throw new Error("Failed to update person");
  return res.json();
};

export const deletePerson = async (id) => {
  // ADD AUTHORIZATION HEADER
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error("Failed to delete person");
  // Original had no return
};