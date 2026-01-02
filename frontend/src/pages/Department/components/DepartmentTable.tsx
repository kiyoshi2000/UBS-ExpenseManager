import { Department } from "@/types/department";

/**
 * DepartmentTable
 *
 * Presentational component for listing departments.
 *
 * Improvements:
 * - Clear action buttons (visual + hover)
 * - Better spacing and readability
 */

type Props = {
  departments: Department[];
  canEdit: boolean;
  onEdit: (department: Department) => void;
  onDelete: (id: number) => void;
};

export const DepartmentTable = ({
  departments,
  canEdit,
  onEdit,
  onDelete,
}: Props) => {
  return (
    <div className="border rounded">
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b bg-muted/40">
            <th className="text-left p-3">Name</th>
            <th className="text-left p-3">Monthly Budget</th>
            <th className="text-left p-3">Currency</th>
            {canEdit && <th className="text-left p-3">Actions</th>}
          </tr>
        </thead>

        <tbody>
          {departments.length === 0 ? (
            <tr>
              <td className="p-3 text-muted-foreground" colSpan={canEdit ? 4 : 3}>
                No departments found.
              </td>
            </tr>
          ) : (
            departments.map((dept) => (
              <tr key={dept.id} className="border-b">
                <td className="p-3">{dept.name}</td>
                <td className="p-3">{dept.monthlyBudget}</td>
                <td className="p-3">{dept.currency}</td>

                {canEdit && (
                  <td className="p-3 flex gap-2">
                    <button
                      onClick={() => onEdit(dept)}
                      className="px-3 py-1 rounded bg-blue-600 text-white hover:bg-blue-700"
                    >
                      Edit
                    </button>

                    <button
                      onClick={() => onDelete(dept.id)}
                      className="px-3 py-1 rounded bg-red-600 text-white hover:bg-red-700"
                    >
                      Delete
                    </button>
                  </td>
                )}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};
