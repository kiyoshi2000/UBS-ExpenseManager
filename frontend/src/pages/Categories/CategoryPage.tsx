import { useState, useEffect } from "react";
import { Plus, Edit, CheckCircle } from "lucide-react";
import { SearchInput } from "@/components/ui/SearchInput";
import { ActionButton } from "@/components/ui/ActionButton";
import { DataTable, ColumnDef, RowAction } from "@/components/DataTable";
import { CreateCategoryDialog } from "@/components/Categories/CreateCategoryDialog";
import { EditCategoryDialog, EditCategoryFormData } from "@/components/Categories/EditCategoryDialog";
import { ConfirmationDialog } from "@/components/ConfirmationDialog";
import { TablePagination } from "@/components/Pagination";
import { getErrorMessage } from "@/types/api-error";
import {
  getCategories,
  createCategory,
  updateCategory,
  Category as ApiCategory,
} from "@/api/category.api";
import { CreateCategoryFormData } from "@/utils/validation";

interface Category {
  id: number;
  name: string;
  dailyBudget: number;
  dailyBudgetCurrency: string;
  monthlyBudget: number;
  monthlyBudgetCurrency: string;
}

export const CategoriesPage = () => {
  const [searchValue, setSearchValue] = useState<string>("");
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [createErrorMessage, setCreateErrorMessage] = useState<string>("");

  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);
  const [editErrorMessage, setEditErrorMessage] = useState<string>("");

  const [openSuccessDialog, setOpenSuccessDialog] = useState(false);
  const [openCreateSuccessDialog, setOpenCreateSuccessDialog] = useState(false);

  useEffect(() => {
    fetchCategories(currentPage, 10, searchQuery);
  }, [currentPage, searchQuery]);

  const fetchCategories = async (page: number, size: number, search: string = "") => {
    try {
      setLoading(true);
      setError(null);

      const pageNumber = page - 1;

      const response = await getCategories({
        page: pageNumber,
        size,
        search: search || undefined,
      });

      const mappedCategories: Category[] = response.content.map((c: ApiCategory) => ({
        id: c.id,
        name: c.name,
        dailyBudget: c.dailyBudget,
        dailyBudgetCurrency: c.dailyBudgetCurrency,
        monthlyBudget: c.monthlyBudget,
        monthlyBudgetCurrency: c.monthlyBudgetCurrency,
      }));

      setCategories(mappedCategories);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err) {
      console.error("Error fetching categories:", err);
      setError("Failed to load categories");
    } finally {
      setLoading(false);
    }
  };

  const handleAddCategory = () => {
    setCreateErrorMessage("");
    setOpenCreateDialog(true);
  };

  const handleCreateCategory = async (data: CreateCategoryFormData): Promise<void> => {
    try {
      await createCategory({
        name: data.name,
        dailyBudget: parseInt(data.dailyBudget),
        dailyBudgetCurrency: data.dailyBudgetCurrency,
        monthlyBudget: parseInt(data.monthlyBudget),
        monthlyBudgetCurrency: data.monthlyBudgetCurrency
      });
      setOpenCreateDialog(false);
      await fetchCategories(currentPage, 10, searchQuery);
      setOpenCreateSuccessDialog(true);
    } catch (err) {
      const errorMsg = getErrorMessage(err);
      setCreateErrorMessage(errorMsg);
      console.error("Error creating category:", err);
    }
  };

  const handleEditCategory = (category: Category) => {
    setSelectedCategory(category);
    setEditErrorMessage("");
    setOpenEditDialog(true);
  };

  const handleUpdateCategory = async (data: EditCategoryFormData) => {
    if (!selectedCategory) return;
    try {
      await updateCategory(selectedCategory.id, data);
      setOpenEditDialog(false);
      await fetchCategories(currentPage, 10, searchQuery);
      setOpenSuccessDialog(true);
    } catch (err) {
      const errorMsg = getErrorMessage(err);
      setEditErrorMessage(errorMsg);
      console.error("Error updating category:", err);
    }
  };

  const handlePageChange = (page: number) => setCurrentPage(page);
  const handleSearch = () => {
    setSearchQuery(searchValue);
    setCurrentPage(1);
  };

  const columns: ColumnDef<Category>[] = [
    { key: "name", label: "Name" },
    { key: "dailyBudget", label: "Daily Budget" },
    { key: "monthlyBudget", label: "Monthly Budget" },
  ];

  const actions: RowAction<Category>[] = [
    { label: "Edit", icon: <Edit className="h-4 w-4" />, onClick: handleEditCategory, color: "blue" },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Category Management</h1>
        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
          Manage categories, daily and monthly budgets
        </p>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900">
        <div className="flex items-center justify-between gap-4">
          <SearchInput
            placeholder="Search category"
            value={searchValue}
            onChange={setSearchValue}
            onSearch={handleSearch}
          />
          <ActionButton
            label="Add Category"
            icon={<Plus className="h-4 w-4" />}
            onClick={handleAddCategory}
          />
        </div>
      </div>

      <DataTable
        columns={columns}
        data={categories}
        actions={actions}
        emptyMessage={loading ? "Loading..." : error || "No categories found"}
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

      <CreateCategoryDialog
        open={openCreateDialog}
        onOpenChange={setOpenCreateDialog}
        onSubmit={handleCreateCategory}
        error={createErrorMessage}
      />

      <EditCategoryDialog
        open={openEditDialog}
        onOpenChange={setOpenEditDialog}
        category={selectedCategory}
        onSubmit={handleUpdateCategory}
        error={editErrorMessage}
      />

      <ConfirmationDialog
        open={openCreateSuccessDialog}
        onOpenChange={setOpenCreateSuccessDialog}
        title="Category Created"
        description="The category has been successfully created."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenCreateSuccessDialog(false)}
      />

      <ConfirmationDialog
        open={openSuccessDialog}
        onOpenChange={setOpenSuccessDialog}
        title="Category Updated"
        description="The category has been successfully updated."
        confirmText="Done"
        variant="success"
        icon={<CheckCircle className="h-6 w-6 text-green-600" />}
        onConfirm={() => setOpenSuccessDialog(false)}
      />
    </div>
  );
};
