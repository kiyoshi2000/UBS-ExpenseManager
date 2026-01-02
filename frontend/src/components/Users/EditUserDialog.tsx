import { useState, useEffect } from "react";
import { AlertCircle } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getManagersByDepartment, User } from "@/api/user.api";
import { listDepartments } from "@/services/department.service";
import { Department } from "@/types/department";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  validateEmail,
  UserValidationErrors,
} from "@/utils/validation";

interface EditUserFormData {
  name: string;
  managerEmail: string;
  departmentId: string;
}

interface EditUserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: {
    id: number;
    name: string;
    email: string;
    role: string;
    department?: string;
    manager?: {
      id: number;
      name: string;
      email: string;
    };
  } | null;
  onSubmit: (data: EditUserFormData) => void;
  error?: string;
}

export const EditUserDialog = ({
  open,
  onOpenChange,
  user,
  onSubmit,
  error,
}: EditUserDialogProps) => {
  const [formData, setFormData] = useState<EditUserFormData>({
    name: user?.name || "",
    managerEmail: user?.manager?.email || "",
    departmentId: "",
  });

  const [errors, setErrors] = useState<UserValidationErrors>({});
  const [departments, setDepartments] = useState<Department[]>([]);
  const [managers, setManagers] = useState<User[]>([]);

  // Load departments on mount
  useEffect(() => {
    const loadDepartments = async () => {
      try {
        const response = await listDepartments();
        setDepartments(response.data);
      } catch (error) {
        console.error("Error loading departments:", error);
      }
    };
    loadDepartments();
  }, []);

  // Find department ID by name and set it in form
  useEffect(() => {
    if (user?.department && departments.length > 0 && open) {
      const dept = departments.find(d => d.name === user.department);
      if (dept) {
        setFormData(prev => ({ ...prev, departmentId: dept.id.toString() }));
      }
    }
  }, [user?.department, departments, open]);

  // Load managers when department changes or dialog opens
  useEffect(() => {
    const loadManagers = async () => {
      if (!formData.departmentId || !open) {
        setManagers([]);
        return;
      }
      
      try {
        const managersList = await getManagersByDepartment(Number(formData.departmentId));
        setManagers(managersList);
      } catch (error) {
        console.error("Error loading managers:", error);
        setManagers([]);
      }
    };
    loadManagers();
  }, [formData.departmentId, open]);

  // Sync form data when user prop changes or dialog opens
  useEffect(() => {
    if (user && open && departments.length > 0) {
      const dept = departments.find(d => d.name === user.department);
      setFormData({
        name: user.name,
        managerEmail: user.manager?.email || "",
        departmentId: dept ? dept.id.toString() : "",
      });
      setErrors({});
    }
  }, [user, open, departments]);

  const handleInputChange = (field: keyof EditUserFormData, value: string) => {
    setFormData({ ...formData, [field]: value });

    // Real-time validation
    const newErrors = { ...errors };
    
    const userRole = user?.role?.toUpperCase() || "";

    if (field === "name") {
      if (value.trim()) {
        delete newErrors.name;
      } else {
        newErrors.name = "Name is required";
      }
    }

    if (field === "departmentId") {
      if (value) {
        delete newErrors.departmentId;
        // Clear manager when department changes
        setFormData(prev => ({ ...prev, departmentId: value, managerEmail: "" }));
        return; // Early return since we already updated formData
      } else {
        newErrors.departmentId = "Department is required";
      }
    }

    if (field === "managerEmail") {
      // Manager email is required only for EMPLOYEE role
      if (userRole.includes("EMPLOYEE")) {
        const emailError = validateEmail(value);
        if (emailError) {
          newErrors.managerEmail = emailError === "Email is required" 
            ? "Manager email is required" 
            : "Invalid manager email";
        } else {
          delete newErrors.managerEmail;
        }
      } else {
        // For other roles, manager email is optional
        if (value.trim()) {
          const emailError = validateEmail(value);
          if (emailError) {
            newErrors.managerEmail = "Invalid manager email";
          } else {
            delete newErrors.managerEmail;
          }
        } else {
          delete newErrors.managerEmail;
        }
      }
    }

    setErrors(newErrors);
  };

  const isFormValid = (): boolean => {
    if (!formData.name.trim()) return false;
    
    const userRole = user?.role?.toUpperCase() || "";
    
    // Manager email is required only for EMPLOYEE role
    if (userRole.includes("EMPLOYEE")) {
      if (!formData.managerEmail.trim()) return false;
      if (validateEmail(formData.managerEmail)) return false; // Has error
    } else {
      // For other roles, if manager email is provided, it must be valid
      if (formData.managerEmail.trim() && validateEmail(formData.managerEmail)) {
        return false; // Has error
      }
    }
    
    return true;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (isFormValid()) {
      onSubmit(formData);
      // Don't close the modal here - let the parent handle it on success
    }
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!newOpen) {
      setErrors({});
    }
    onOpenChange(newOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent 
        className="sm:max-w-[500px]"
        onInteractOutside={(e) => e.preventDefault()}
        onEscapeKeyDown={(e) => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>Edit User</DialogTitle>
          <DialogDescription>
            Update the user information below
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="flex items-start gap-3 rounded-lg bg-red-50 p-4 dark:bg-red-950/50">
            <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <h3 className="text-sm font-medium text-red-800 dark:text-red-300">
                Error updating user
              </h3>
              <p className="mt-1 text-sm text-red-700 dark:text-red-400">
                {error}
              </p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Employee Email (Read-only) */}
          <div className="space-y-2">
            <Label htmlFor="email" className="text-sm font-medium">
              Email
            </Label>
            <Input
              id="email"
              type="email"
              value={user?.email || ""}
              disabled
              className="bg-gray-100 dark:bg-gray-800 cursor-not-allowed"
            />
            <p className="text-xs text-gray-500">This field cannot be edited</p>
          </div>

          {/* Role (Read-only) */}
          <div className="space-y-2">
            <Label htmlFor="role" className="text-sm font-medium">
              Role
            </Label>
            <Input
              id="role"
              value={user?.role || ""}
              disabled
              className="bg-gray-100 dark:bg-gray-800 cursor-not-allowed"
            />
            <p className="text-xs text-gray-500">This field cannot be edited</p>
          </div>

          {/* Name */}
          <div className="space-y-2">
            <Label htmlFor="name" className="text-sm font-medium">
              Name <span className="text-red-600">*</span>
            </Label>
            <Input
              id="name"
              placeholder="John Silva"
              value={formData.name}
              onChange={(e) =>
                handleInputChange("name", e.target.value)
              }
              className={errors.name ? "border-red-500" : ""}
            />
            {errors.name && (
              <p className="text-sm text-red-600">{errors.name}</p>
            )}
          </div>

          {/* Department */}
          <div className="space-y-2">
            <Label htmlFor="departmentId" className="text-sm font-medium">
              Department <span className="text-red-600">*</span>
            </Label>
            <select
              id="departmentId"
              value={formData.departmentId}
              onChange={(e) =>
                handleInputChange("departmentId", e.target.value)
              }
              className={`file:text-foreground flex h-9 w-full rounded-md border bg-transparent px-3 py-1 text-sm shadow-xs transition-[color,box-shadow] outline-none focus:outline-none focus:border-ring focus:ring-ring/50 focus:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50 dark:bg-input/30 dark:focus:ring-ring/50 ${
                errors.departmentId ? "border-destructive aria-invalid:border-destructive" : "border-input"
              }`}
            >
              <option value="">Select a department</option>
              {departments.map((dept) => (
                <option key={dept.id} value={dept.id}>
                  {dept.name}
                </option>
              ))}
            </select>
            {errors.departmentId && (
              <p className="text-sm text-red-600">{errors.departmentId}</p>
            )}
          </div>

          {/* Manager */}
          <div className="space-y-2">
            <Label htmlFor="managerEmail" className="text-sm font-medium">
              Manager{" "}
              {user?.role?.toUpperCase().includes("EMPLOYEE") && (
                <span className="text-red-600">*</span>
              )}
            </Label>
            <select
              id="managerEmail"
              value={formData.managerEmail}
              onChange={(e) =>
                handleInputChange("managerEmail", e.target.value)
              }
              disabled={!formData.departmentId}
              className={`file:text-foreground flex h-9 w-full rounded-md border bg-transparent px-3 py-1 text-sm shadow-xs transition-[color,box-shadow] outline-none focus:outline-none focus:border-ring focus:ring-ring/50 focus:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50 dark:bg-input/30 dark:focus:ring-ring/50 ${
                errors.managerEmail ? "border-destructive aria-invalid:border-destructive" : "border-input"
              }`}
            >
              <option value="">{!formData.departmentId ? "Select a department first" : "Select a manager (optional)"}</option>
              {managers.map((manager) => (
                <option key={manager.id} value={manager.email}>
                  {manager.name} ({manager.email})
                </option>
              ))}
            </select>
            {errors.managerEmail && (
              <p className="text-sm text-red-600">{errors.managerEmail}</p>
            )}
          </div>

          <DialogFooter className="gap-2 pt-4">
            <button
              type="button"
              onClick={() => handleOpenChange(false)}
              className="inline-flex h-10 items-center justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 cursor-pointer dark:border-gray-700 dark:bg-gray-950 dark:text-gray-400 dark:hover:bg-gray-900"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!isFormValid()}
              onClick={handleSubmit}
              className={`inline-flex h-10 items-center justify-center rounded-md px-4 py-2 text-sm font-medium text-white transition-colors ${
                isFormValid()
                  ? "bg-blue-600 hover:bg-blue-700 cursor-pointer dark:bg-blue-700 dark:hover:bg-blue-600"
                  : "bg-gray-400 cursor-not-allowed dark:bg-gray-700"
              }`}
            >
              Save Changes
            </button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
