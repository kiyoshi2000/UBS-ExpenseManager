import { useEffect, useState } from "react";
import { Plus } from "lucide-react";
import { DataTable, ColumnDef, RowAction } from "@/components/DataTable";
import { ConfirmationDialog } from "@/components/ConfirmationDialog";
import { ActionButton } from "@/components/ui/ActionButton";
import {
  getDepartments,
  deleteDepartment,
} from "@/api/department.api";
import { Department } from "./types/department";
import { CreateDepartmentDialog } from "./components/CreateDepartmentDialog";
import { EditDepartmentDialog } from "./components/EditDepartmentDialog";

export const DepartmentPage = () => {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [openCreate, setOpenCreate] = useState(false);
  const [editing, setEditing] = useState<Department | null>(null);
  const [toDelete, setToDelete] = useState<Department | null>(null);

  const canEdit = true; // mesmo pattern do UserPage

  async function load() {
    try {
      setLoading(true);
      const data = await getDepartments();
      setDepartments([...data]);
    } catch {
      setError("Failed to load departments");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const columns: ColumnDef<Department>[] = [
    { key: "name", label: "Name" },
    { key: "monthlyBudget", label: "Monthly Budget" },
    { key: "dailyBudget", label: "Daily Budget" },
    { key: "currency", label: "Currency" },
  ];

  const actions: RowAction<Department>[] = [
    {
      label: "Edit",
      onClick: (row) => setEditing(row),
      shouldShow: () => canEdit,
    },
    {
      label: "Delete",
      onClick: (row) => setToDelete(row),
      color: "red",
      shouldShow: () => canEdit,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Departments</h1>

        {canEdit && (
          <ActionButton
            label="Create Department"
            icon={<Plus className="h-4 w-4" />}
            onClick={() => setOpenCreate(true)}
          />
        )}
      </div>

      <DataTable
        columns={columns}
        data={departments}
        actions={actions}
        emptyMessage={loading ? "Loading..." : error || "No departments found"}
      />

      <CreateDepartmentDialog
        open={openCreate}
        onOpenChange={setOpenCreate}
        onSuccess={async () => {
          setOpenCreate(false);
          await load();
        }}
      />

      {editing && (
        <EditDepartmentDialog
          department={editing}
          open={!!editing}
          onOpenChange={() => setEditing(null)}
          onSuccess={() => {
            setEditing(null);
            load();
          }}
        />
      )}

      {toDelete && (
        <ConfirmationDialog
          open={!!toDelete}
          title="Delete Department"
          description={`Are you sure you want to delete ${toDelete.name}?`}
          confirmText="Delete"
          variant="danger"
          onConfirm={async () => {
            await deleteDepartment(toDelete.id);
            setToDelete(null);
            load();
          }}
          onOpenChange={() => setToDelete(null)}
        />
      )}
    </div>
  );
};
