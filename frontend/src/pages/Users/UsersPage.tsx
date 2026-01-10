import { useState, useEffect } from "react";
import { Plus, Edit, CheckCircle, AlertCircle, RotateCcw } from "lucide-react";
import { SearchInput } from "@/components/ui/SearchInput";
import { ActionButton } from "@/components/ui/ActionButton";
import { DataTable, ColumnDef, RowAction } from "@/components/DataTable";
import { CreateUserDialog } from "@/components/Users/CreateUserDialog";
import { EditUserDialog } from "@/components/Users/EditUserDialog";
import { ConfirmationDialog } from "@/components/ConfirmationDialog";
import { Switch } from "@/components/ui/switch";
import { TablePagination } from "@/components/Pagination";
import { CreateUserFormData } from "@/utils/validation";
import { getErrorMessage } from "@/types/api-error";
import {
  getUsers,
  createUser,
  updateUser,
  deactivateUser,
  reactivateUser,
  User as ApiUser,
} from "@/api/user.api";

interface EditUserFormData {
  name: string;
  managerEmail: string;
  departmentId: string;
}

interface User {
  id: number;
  email: string;
  role: string;
  name: string;
  manager?: {
    id: number;
    name: string;
    email: string;
  };
  department: string;
  status: string;
}

export const UsersPage = () => {
  const [searchValue, setSearchValue] = useState<string>("");
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [createErrorMessage, setCreateErrorMessage] = useState<string>("");
  const [openCreateSuccessDialog, setOpenCreateSuccessDialog] = useState(false);
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [openSuccessDialog, setOpenSuccessDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [openDeleteSuccessDialog, setOpenDeleteSuccessDialog] = useState(false);
  const [openDeleteErrorDialog, setOpenDeleteErrorDialog] = useState(false);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string>("");
  const [openReactivateDialog, setOpenReactivateDialog] = useState(false);
  const [openReactivateSuccessDialog, setOpenReactivateSuccessDialog] = useState(false);
  const [openReactivateErrorDialog, setOpenReactivateErrorDialog] = useState(false);
  const [reactivateErrorMessage, setReactivateErrorMessage] = useState<string>("");
  const [editErrorMessage, setEditErrorMessage] = useState<string>("");
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [userToDelete, setUserToDelete] = useState<User | null>(null);
  const [userToReactivate, setUserToReactivate] = useState<User | null>(null);

  useEffect(() => {
    fetchUsers(currentPage, 10, searchQuery);
  }, [currentPage, searchQuery]);

  const fetchUsers = async (page: number, size: number, search: string = ""): Promise<void> => {
    try {
      setLoading(true);
      setError(null);
      
      // Spring Boot pages are 0-indexed, but UI uses 1-indexed
      const pageNumber = page - 1;
      
      const response = await getUsers({
        page: pageNumber,
        size,
        sort: ["active,desc", "name,asc"],
        search: search || undefined,
        includeInactive: true,
      });
      
      // Map API payload to local User shape
      const usersWithDepartment: User[] = response.content.map((u: ApiUser) => ({
        id: u.id,
        name: u.name,
        email: u.email,
        department: u.department?.name || "-",
        manager: u.manager ?? undefined,
        role: u.role,
        // Map boolean active -> status string used by table
        status: u.active ? "Active" : "Inactive",
      }));

      setUsers(usersWithDepartment);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err) {
      console.error("Error fetching users:", err);
      setError("Failed to load users");
    } finally {
      setLoading(false);
    }
  };

  const handleAddEmployee = (): void => {
    setCreateErrorMessage("");
    setOpenCreateDialog(true);
  };

  const handleCreateUser = async (data: CreateUserFormData): Promise<void> => {
    try {
      await createUser({
        email: data.email,
        password: "123456", // Default password for registration
        name: data.name,
        role: data.role.toUpperCase(),
        managerEmail: data.managerEmail || undefined,
        departmentId: data.departmentId ? parseInt(data.departmentId) : undefined,
      });

      setOpenCreateDialog(false);
      setCreateErrorMessage("");
      await fetchUsers(currentPage, 10, searchQuery);
      setOpenCreateSuccessDialog(true);
    } catch (err) {
      const errorMsg = getErrorMessage(err);
      setCreateErrorMessage(errorMsg);
      console.error("Error creating user:", err);
    }
  };

  const handleEdit = (user: User): void => {
    setSelectedUser(user);
    setEditErrorMessage("");
    setOpenEditDialog(true);
  };

  const handleEditUser = async (data: EditUserFormData): Promise<void> => {
    if (!selectedUser) return;

    try {
      // Remove ROLE_ prefix if present
      const roleValue = selectedUser.role.replace(/^ROLE_/, "");

      await updateUser(selectedUser.id, {
        email: selectedUser.email,
        password: "123456", // Using default password for updates
        role: roleValue,
        name: data.name,
        managerEmail: data.managerEmail || undefined,
        departmentId: data.departmentId ? parseInt(data.departmentId) : undefined,
      });
      
      setOpenEditDialog(false);
      setEditErrorMessage("");
      // Refresh users list after update
      await fetchUsers(currentPage, 10, searchQuery);
      setOpenSuccessDialog(true);
    } catch (err) {
      const errorMsg = getErrorMessage(err);
      setEditErrorMessage(errorMsg);
      console.error("Error updating user:", err);
    }
  };

  const handleStatusToggle = (user: User): void => {
    if (user.status === "Active") {
      setUserToDelete(user);
      setOpenDeleteDialog(true);
    } else {
      setUserToReactivate(user);
      setOpenReactivateDialog(true);
    }
  };

  const handleConfirmReactivate = async (): Promise<void> => {
    if (userToReactivate) {
      try {
        await reactivateUser(userToReactivate.id);
        setOpenReactivateDialog(false);
        await fetchUsers(currentPage, 10, searchQuery);
        setOpenReactivateSuccessDialog(true);
      } catch (err) {
        setOpenReactivateDialog(false);
        const errorMsg = getErrorMessage(err);
        setReactivateErrorMessage(errorMsg);
        setOpenReactivateErrorDialog(true);
        console.error("Error reactivating user:", err);
      }
    }
  };

  const handleConfirmDelete = async (): Promise<void> => {
    if (userToDelete) {
      try {
        await deactivateUser(userToDelete.id);
        setOpenDeleteDialog(false);
        await fetchUsers(currentPage, 10, searchQuery);
        setOpenDeleteSuccessDialog(true);
      } catch (err) {
        setOpenDeleteDialog(false);
        const errorMsg = getErrorMessage(err);
        setDeleteErrorMessage(errorMsg);
        setOpenDeleteErrorDialog(true);
        console.error("Error deactivating user:", err);
      }
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleSearch = () => {
    setSearchQuery(searchValue);
    setCurrentPage(1); // Reset to first page when searching
  };

  const columns: ColumnDef<User>[] = [
    {
      key: "name",
      label: "Name",
      render: (value: User[keyof User]) => {
        const displayValue = typeof value === "string" ? value : "";
        return <span className="font-medium">{displayValue}</span>;
      },
    },
    {
      key: "email",
      label: "Email",
    },
    {
      key: "department",
      label: "Department",
    },
    {
      key: "manager",
      label: "Manager",
      render: (value: User[keyof User]) => {
        if (value && typeof value === "object") {
          const manager = value as { email?: string; name?: string };
          return String(manager.email || manager.name || "-");
        }
        return String(value || "-");
      },
    },

    {
      key: "role",
      label: "Role",
      render: (value: User[keyof User]) => {
        const roleValue = typeof value === "string" ? value : "";
        
        if (roleValue === "ROLE_EMPLOYEE" || roleValue === "EMPLOYEE") {
          return "Employee";
        }
        if (roleValue === "ROLE_FINANCE" || roleValue === "FINANCE") {
          return "Finance";
        }
        if (roleValue === "ROLE_MANAGER" || roleValue === "MANAGER") {
          return "Manager";
        }
        
        // Remove ROLE_ prefix and capitalize
        return roleValue.replace("ROLE_", "").charAt(0).toUpperCase() + 
               roleValue.replace("ROLE_", "").slice(1).toLowerCase();
      },
    },
    {  key: "status",
      label: "Status",
      render: (_value: User[keyof User], row: User) => {
        const isActive = row.status === "Active";
        return (
          <div className="flex items-center gap-2">
            <Switch
              checked={isActive}
              onCheckedChange={() => handleStatusToggle(row)}
            />
            <span
              className={`text-xs font-semibold ${
                isActive
                  ? "text-green-800 dark:text-green-200"
                  : "text-red-800 dark:text-red-200"
              }`}
            >
              {row.status}
            </span>
          </div>
        );
      },
    },
  ];

  const actions: RowAction<User>[] = [
    {
      label: "Edit",
      icon: <Edit className="h-4 w-4" />,
      onClick: handleEdit,
      color: "blue",
      shouldShow: (row) => row.status === "Active",
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          User Management
        </h1>
        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
          Manage users, roles, and permissions
        </p>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900">
        <div className="flex items-center justify-between gap-4">
          <SearchInput
            placeholder="Search employee"
            value={searchValue}
            onChange={setSearchValue}
            onSearch={handleSearch}
          />
          <ActionButton
            label="Add User"
            icon={<Plus className="h-4 w-4" />}
            onClick={handleAddEmployee}
          />
        </div>
      </div>

      <DataTable
        columns={columns}
        data={users}
        actions={actions}
        emptyMessage={loading ? "Loading..." : error ? error : "No Users Found"}
      />

      {!loading && !error && totalElements > 0 && (
        <div className="rounded-lg border border-gray-200 bg-white dark:border-gray-800 dark:bg-gray-900">
          <TablePagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
            itemsPerPage={10}
            totalItems={totalElements}
          />
        </div>
      )}

      <CreateUserDialog
        open={openCreateDialog}
        onOpenChange={setOpenCreateDialog}
        onSubmit={handleCreateUser}
        error={createErrorMessage}
      />

      <ConfirmationDialog
        open={openCreateSuccessDialog}
        onOpenChange={setOpenCreateSuccessDialog}
        title="User Created"
        description="The user has been successfully created."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenCreateSuccessDialog(false)}
      />

      <EditUserDialog
        open={openEditDialog}
        onOpenChange={setOpenEditDialog}
        user={selectedUser}
        onSubmit={handleEditUser}
        error={editErrorMessage}
      />

      <ConfirmationDialog
        open={openSuccessDialog}
        onOpenChange={setOpenSuccessDialog}
        title="User Updated"
        description="The user has been successfully updated."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenSuccessDialog(false)}
      />

      <ConfirmationDialog
        open={openDeleteDialog}
        onOpenChange={setOpenDeleteDialog}
        title="Deactivate User"
        description={`Are you sure you want to deactivate ${userToDelete?.name}? `}
        confirmText="Deactivate"
        variant="danger"
        icon={<AlertCircle className="h-6 w-6 text-red-600" />}
        onConfirm={handleConfirmDelete}
      />

      <ConfirmationDialog
        open={openDeleteSuccessDialog}
        onOpenChange={setOpenDeleteSuccessDialog}
        title="User Deactivated"
        description="The user has been successfully deactivated."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenDeleteSuccessDialog(false)}
      />

      <ConfirmationDialog
        open={openDeleteErrorDialog}
        onOpenChange={setOpenDeleteErrorDialog}
        title="Error Deactivating User"
        description={deleteErrorMessage}
        confirmText="Close"
        variant="danger"
        icon={<AlertCircle className="h-6 w-6 text-red-600" />}
        onConfirm={() => setOpenDeleteErrorDialog(false)}
      />

      <ConfirmationDialog
        open={openReactivateDialog}
        onOpenChange={setOpenReactivateDialog}
        title="Reactivate User"
        description={`Are you sure you want to reactivate ${userToReactivate?.name}?`}
        confirmText="Reactivate"
        variant="success"
        icon={<RotateCcw className="h-6 w-6 text-green-600" />}
        onConfirm={handleConfirmReactivate}
      />

      <ConfirmationDialog
        open={openReactivateSuccessDialog}
        onOpenChange={setOpenReactivateSuccessDialog}
        title="User Reactivated"
        description="The user has been successfully reactivated."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenReactivateSuccessDialog(false)}
      />

      <ConfirmationDialog
        open={openReactivateErrorDialog}
        onOpenChange={setOpenReactivateErrorDialog}
        title="Error Reactivating User"
        description={reactivateErrorMessage}
        confirmText="Close"
        variant="danger"
        icon={<AlertCircle className="h-6 w-6 text-red-600" />}
        onConfirm={() => setOpenReactivateErrorDialog(false)}
      />
    </div>
  );
};
