const BASE_URL = "/people";

export const getPeople = async () => {
  const res = await fetch(BASE_URL);
  if (!res.ok) throw new Error("Failed to fetch people");
  return await res.json();
};

export const addPerson = async (person) => {
  const res = await fetch(BASE_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(person),
  });
  if (!res.ok) throw new Error("Failed to add person");
  return await res.json();
};

export const updatePerson = async (person) => {
  const res = await fetch(`${BASE_URL}/${person.id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(person),
  });
  return res.json();
};

export const deletePerson = async (id) => {
  await fetch(`${BASE_URL}/${id}`, {
    method: "DELETE",
  });
};