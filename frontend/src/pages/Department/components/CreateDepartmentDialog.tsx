import { AlertCircle } from "lucide-react";
import { DepartmentForm } from "./DepartmentForm";
import { createDepartment } from "@/api/department.api";
import { DepartmentFormData } from "@/utils/validation/department.schema";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
  error?: string;
}

export const CreateDepartmentDialog = ({
  open,
  onOpenChange,
  onSuccess,
  error,
}: Props) => {
  async function handleSubmit(data: DepartmentFormData) {
    await createDepartment(data);
    onSuccess();
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[480px]">
        <DialogHeader>
          <DialogTitle>Create Department</DialogTitle>
          <DialogDescription>
            Configure budgets and currency for the department
          </DialogDescription>
        </DialogHeader>

        {error && (
          <div className="flex gap-2 rounded bg-red-50 p-3">
            <AlertCircle className="h-4 w-4 text-red-600" />
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <DepartmentForm onSubmit={handleSubmit} />
      </DialogContent>
    </Dialog>
  );
};
