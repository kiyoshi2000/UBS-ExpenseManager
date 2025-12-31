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
  CreateUserFormData,
  validateUserForm,
  UserValidationErrors,
  validateEmail,
} from "@/utils/validation";

interface CreateUserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateUserFormData) => void;
  error?: string;
}

export const CreateUserDialog = ({
  open,
  onOpenChange,
  onSubmit,
  error,
}: CreateUserDialogProps) => {
  const [formData, setFormData] = useState<CreateUserFormData>({
    name: "",
    email: "",
    role: "",
    managerEmail: "",
  });

  const [errors, setErrors] = useState<UserValidationErrors>({});

  // Reset form when dialog closes successfully
  useEffect(() => {
    if (!open) {
      handleReset();
    }
  }, [open]);

  const handleInputChange = (field: keyof CreateUserFormData, value: string) => {
    setFormData({ ...formData, [field]: value });
    
    // Real-time validation - remove error for this field if it becomes valid
    const newErrors = { ...errors };
    
    // Validate the specific field
    if (field === "name") {
      if (value.trim()) {
        delete newErrors.name;
      } else {
        newErrors.name = "Name is required";
      }
    }
    
    if (field === "email") {
      const emailError = validateEmail(value);
      if (emailError) {
        newErrors.email = emailError;
      } else {
        delete newErrors.email;
      }
    }
    
    if (field === "role") {
      if (value) {
        delete newErrors.role;
        // If changing from employee, clear manager email error if not needed
        if (value !== "employee") {
          delete newErrors.managerEmail;
        }
      } else {
        newErrors.role = "Role is required";
      }
    }
    
    if (field === "managerEmail") {
      if (formData.role === "employee") {
        const emailError = validateEmail(value);
        if (emailError) {
          newErrors.managerEmail = emailError === "Email is required" 
            ? "Manager email is required for employees" 
            : "Invalid manager email";
        } else {
          delete newErrors.managerEmail;
        }
      } else if (value) {
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
    
    setErrors(newErrors);
  };

  const isFormValid = (): boolean => {
    const validationErrors = validateUserForm(formData);
    return Object.keys(validationErrors).length === 0;
  };

  const handleReset = () => {
    setFormData({
      name: "",
      email: "",
      role: "",
      managerEmail: "",
    });
    setErrors({});
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const validationErrors = validateUserForm(formData);
    
    if (Object.keys(validationErrors).length === 0) {
      onSubmit(formData);
      // Don't close the modal here - let the parent handle it on success
    }
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!newOpen) {
      handleReset();
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
          <DialogTitle>Create New User</DialogTitle>
          <DialogDescription>
            Fill in the details of the new user below
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="flex items-start gap-3 rounded-lg bg-red-50 p-4 dark:bg-red-950/50">
            <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <h3 className="text-sm font-medium text-red-800 dark:text-red-300">
                Error creating user
              </h3>
              <p className="mt-1 text-sm text-red-700 dark:text-red-400">
                {error}
              </p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
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

          {/* Email */}
          <div className="space-y-2">
            <Label htmlFor="email" className="text-sm font-medium">
              Email <span className="text-red-600">*</span>
            </Label>
            <Input
              id="email"
              type="email"
              placeholder="john@ubs.com"
              value={formData.email}
              onChange={(e) =>
                handleInputChange("email", e.target.value)
              }
              className={errors.email ? "border-red-500" : ""}
            />
            {errors.email && (
              <p className="text-sm text-red-600">{errors.email}</p>
            )}
          </div>

          {/* Role */}
          <div className="space-y-2">
            <Label htmlFor="role" className="text-sm font-medium">
              Role <span className="text-red-600">*</span>
            </Label>
            <select
              id="role"
              value={formData.role}
              onChange={(e) =>
                handleInputChange("role", e.target.value)
              }
              className={`file:text-foreground flex h-9 w-full rounded-md border bg-transparent px-3 py-1 text-sm shadow-xs transition-[color,box-shadow] outline-none focus:outline-none focus:border-ring focus:ring-ring/50 focus:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50 dark:bg-input/30 dark:focus:ring-ring/50 ${
                errors.role ? "border-destructive aria-invalid:border-destructive" : "border-input"
              }`}
            >
              <option value="">Select a role</option>
              <option value="employee">Employee</option>
              <option value="manager">Manager</option>
              <option value="finance">Finance</option>
            </select>
            {errors.role && (
              <p className="text-sm text-red-600">{errors.role}</p>
            )}
          </div>

          {/* Manager Email */}
          <div className="space-y-2">
            <Label htmlFor="managerEmail" className="text-sm font-medium">
              Manager Email{" "}
              {formData.role === "employee" && (
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
              Create User
            </button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
