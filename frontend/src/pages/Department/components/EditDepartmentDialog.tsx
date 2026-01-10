import { AlertCircle } from "lucide-react";
import { Department } from "../types/department";
import { updateDepartment } from "@/api/department.api";
import { DepartmentFormData } from "@/utils/validation";
import { DepartmentForm } from "./DepartmentForm";
import { getErrorMessage } from "@/types/api-error";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

interface Props {
  department: Department;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
  onError: (error: string) => void;
  error?: string;
}

export const EditDepartmentDialog = ({
  department,
  open,
  onOpenChange,
  onSuccess,
  onError,
  error,
}: Props) => {
  async function handleSubmit(data: DepartmentFormData) {
    try {
      await updateDepartment(department.id, {
        ...data,
        dailyBudget: data.dailyBudget ?? null,
      });
      onSuccess();
    } catch (err) {
      const errorMsg = getErrorMessage(err);
      onError(errorMsg);
      console.error("Error updating department:", err);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent 
        className="sm:max-w-[480px]"
        onInteractOutside={(e) => e.preventDefault()}
        >
        <DialogHeader>
          <DialogTitle>Edit Department</DialogTitle>
        </DialogHeader>

        {error && (
          <div className="flex items-start gap-3 rounded-lg bg-red-50 p-4 dark:bg-red-950/50">
            <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <h3 className="text-sm font-medium text-red-800 dark:text-red-300">
                Error updating department
              </h3>
              <p className="mt-1 text-sm text-red-700 dark:text-red-400">
                {error}
              </p>
            </div>
          </div>
        )}

        <DepartmentForm
          initialData={department}
          onSubmit={handleSubmit}
        />
      </DialogContent>
    </Dialog>
  );
};
