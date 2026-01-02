import { useEffect, useState } from "react";
import { Department } from "@/types/department";
import { DepartmentFormData } from "@/utils/validation";
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
 *
 * Improvements:
 * - Better UX (clear buttons, better layout)
 * - Proper error handling and feedback
 * - Correct Create flow resets editing state
 */

export const DepartmentPage = () => {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [editing, setEditing] = useState<Department | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const role = localStorage.getItem("user_role");
  const canEdit = role === "FINANCE" || role === "ROLE_FINANCE";

  async function loadDepartments() {
    setError(null);
    try {
      const { data } = await listDepartments();
      setDepartments(data);
    } catch {
      setError("Failed to load departments. Please try again.");
    }
  }

  useEffect(() => {
    loadDepartments();
  }, []);

  async function handleSubmit(data: DepartmentFormData) {
    setError(null);
    try {
      if (editing) {
        await updateDepartment(editing.id, data);
      } else {
        await createDepartment(data);
      }

      setShowForm(false);
      setEditing(null);
      await loadDepartments();
      alert(editing ? "Department updated successfully!" : "Department created successfully!");
    } catch {
      alert("Failed to save department. Check permissions or invalid data.");
    }
  }

  async function handleDelete(id: number) {
    setError(null);
    try {
      await deleteDepartment(id);
      await loadDepartments();
      alert("Department deleted successfully!");
    } catch {
      alert("Failed to delete department. Check permissions.");
    }
  }

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-semibold">Departments</h1>

        {canEdit && (
          <button
            onClick={() => {
              setEditing(null);
              setShowForm(true);
            }}
            className="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700"
          >
            + Create Department
          </button>
        )}
      </div>

      {error && <p className="text-destructive">{error}</p>}

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
        <div className="mt-4">
          <DepartmentForm
            initialData={editing}
            onSubmit={handleSubmit}
            onCancel={() => setShowForm(false)}
          />
        </div>
      )}
    </div>
  );
};
