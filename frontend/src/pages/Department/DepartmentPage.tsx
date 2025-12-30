import { useEffect, useState } from "react";
import { Department } from "@/types/department";
import {
  listDepartments,
  createDepartment,
  updateDepartment,
  deleteDepartment,
} from "@/services/department.service";
import { DepartmentTable } from "./components/DepartmentTable";
import { DepartmentForm } from "./components/DepartmentForm";

/**
 * DepartmentPage
 *
 * Orchestrates:
 * - Department list
 * - Create / Edit / Delete flows
 */

export const DepartmentPage = () => {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [editing, setEditing] = useState<Department | null>(null);
  const [showForm, setShowForm] = useState(false);
  const role = localStorage.getItem("user_role");
  const canEdit = role === "FINANCE" || role === "ROLE_FINANCE";


  async function loadDepartments() {
    const { data } = await listDepartments();
    setDepartments(data);
  }

  useEffect(() => {
    loadDepartments();
  }, []);

  async function handleSubmit(data: any) {
    if (editing) {
      await updateDepartment(editing.id, data);
    } else {
      await createDepartment(data);
    }

    setShowForm(false);
    setEditing(null);
    loadDepartments();
  }

  async function handleDelete(id: number) {
    await deleteDepartment(id);
    loadDepartments();
  }

  return (
    <div className="p-6">
      <h1 className="text-3xl mb-4">Departments</h1>

      {canEdit && (
        <button onClick={() => setShowForm(true)}>
          Create Department
        </button>
      )}

      <DepartmentTable
        departments={departments}
        canEdit={canEdit}
        onEdit={(d) => {
          setEditing(d);
          setShowForm(true);
        }}
        onDelete={handleDelete}
      />

      {showForm && (
        <DepartmentForm
          initialData={editing}
          onSubmit={handleSubmit}
          onCancel={() => setShowForm(false)}
        />
      )}
    </div>
  );
};
