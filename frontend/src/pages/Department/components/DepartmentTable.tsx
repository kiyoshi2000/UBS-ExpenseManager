import { Department } from "@/types/department";

/**
 * DepartmentTable
 *
 * Displays a list of departments in a tabular format.
 *
 * This component is purely presentational:
 * - No API calls
 * - No business logic
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
    <table className="w-full border-collapse">
      <thead>
        <tr className="border-b">
          <th className="text-left p-2">Name</th>
          <th className="text-left p-2">Monthly Budget</th>
          <th className="text-left p-2">Currency</th>
          {canEdit && <th className="p-2">Actions</th>}
        </tr>
      </thead>

      <tbody>
        {departments.map((dept) => (
          <tr key={dept.id} className="border-b">
            <td className="p-2">{dept.name}</td>
            <td className="p-2">{dept.monthlyBudget}</td>
            <td className="p-2">{dept.currency}</td>

            {canEdit && (
              <td className="p-2 space-x-2">
                <button
                  onClick={() => onEdit(dept)}
                  className="text-blue-600"
                >
                  Edit
                </button>

                <button
                  onClick={() => onDelete(dept.id)}
                  className="text-red-600"
                >
                  Delete
                </button>
              </td>
            )}
          </tr>
        ))}
      </tbody>
    </table>
  );
};
