import { useState, useEffect } from "react";
import { AlertCircle } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
}

interface EditUserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: {
    id: number;
    name: string;
    email: string;
    role: string;
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
  });

  const [errors, setErrors] = useState<UserValidationErrors>({});

  // Sync form data when user prop changes or dialog opens
  useEffect(() => {
    if (user && open) {
      setFormData({
        name: user.name,
        managerEmail: user.manager?.email || "",
      });
      setErrors({});
    }
  }, [user, open]);

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

          {/* Manager Email */}
          <div className="space-y-2">
            <Label htmlFor="managerEmail" className="text-sm font-medium">
              Manager Email{" "}
              {user?.role?.toUpperCase().includes("EMPLOYEE") && (
                <span className="text-red-600">*</span>
              )}
            </Label>
            <Input
              id="managerEmail"
              type="email"
              placeholder="manager@ubs.com"
              value={formData.managerEmail}
              onChange={(e) =>
                handleInputChange("managerEmail", e.target.value)
              }
              className={errors.managerEmail ? "border-red-500" : ""}
            />
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
