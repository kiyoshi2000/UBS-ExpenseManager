import { Department } from "../types/department";
import { updateDepartment } from "@/api/department.api";
import { DepartmentFormData } from "@/utils/validation";
import { DepartmentForm } from "./DepartmentForm";
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
}

export const EditDepartmentDialog = ({
  department,
  open,
  onOpenChange,
  onSuccess,
}: Props) => {
  async function handleSubmit(data: DepartmentFormData) {
    await updateDepartment(department.id, {
      ...data,
      dailyBudget: data.dailyBudget ?? null,
    });
    onSuccess();
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

        <DepartmentForm
          initialData={department}
          onSubmit={handleSubmit}
        />
      </DialogContent>
    </Dialog>
  );
};
