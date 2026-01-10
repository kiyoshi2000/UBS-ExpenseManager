import { useState, useEffect } from "react";
import { Plus, Edit, CheckCircle } from "lucide-react";
import { SearchInput } from "@/components/ui/SearchInput";
import { ActionButton } from "@/components/ui/ActionButton";
import { DataTable, ColumnDef, RowAction } from "@/components/DataTable";
import { CreateCategoryDialog } from "@/components/Categories/CreateCategoryDialog";
import {
  EditCategoryDialog,
  EditCategoryFormData,
} from "@/components/Categories/EditCategoryDialog";
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
  monthlyBudget: number;
  currencyName: string;
}

export const CategoriesPage = () => {
  const [searchValue, setSearchValue] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [openEditDialog, setOpenEditDialog] = useState(false);

  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);

  const [createErrorMessage, setCreateErrorMessage] = useState("");
  const [editErrorMessage, setEditErrorMessage] = useState("");

  const [openCreateSuccessDialog, setOpenCreateSuccessDialog] = useState(false);
  const [openSuccessDialog, setOpenSuccessDialog] = useState(false);

  useEffect(() => {
    fetchCategories(currentPage, 10, searchQuery);
  }, [currentPage, searchQuery]);

  const fetchCategories = async (
    page: number,
    size: number,
    search = ""
  ) => {
    try {
      setLoading(true);
      setError(null);

      const response = await getCategories({
        page: page - 1,
        size,
        search: search || undefined,
      });

      const mapped: Category[] = response.content.map(
        (c: ApiCategory) => ({
          id: c.id,
          name: c.name,
          dailyBudget: c.dailyBudget,
          monthlyBudget: c.monthlyBudget,
          currencyName: c.currencyName,
        })
      );

      setCategories(mapped);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err) {
      console.error(err);
      setError("Failed to load categories");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCategory = async (
    data: CreateCategoryFormData
  ) => {
    try {
      await createCategory({
        name: data.name,
        dailyBudget: data.dailyBudget,
        monthlyBudget: data.monthlyBudget,
        currencyName: data.currencyName,
      });

      setOpenCreateDialog(false);
      setCreateErrorMessage("");
      await fetchCategories(currentPage, 10, searchQuery);
      setOpenCreateSuccessDialog(true);
    } catch (err) {
      setCreateErrorMessage(getErrorMessage(err));
    }
  };

  const handleEditCategory = (category: Category) => {
    setSelectedCategory(category);
    setEditErrorMessage("");
    setOpenEditDialog(true);
  };

  const handleUpdateCategory = async (
    data: EditCategoryFormData
  ) => {
    if (!selectedCategory) return;

    try {
      await updateCategory(selectedCategory.id, {
        ...data,
        currencyName: data.currencyName,
      });

      setOpenEditDialog(false);
      await fetchCategories(currentPage, 10, searchQuery);
      setOpenSuccessDialog(true);
    } catch (err) {
      setEditErrorMessage(getErrorMessage(err));
    }
  };

  const handleSearch = () => {
    setSearchQuery(searchValue);
    setCurrentPage(1);
  };

  const formatCurrency = (value: number, currency: "USD" | "BRL") => {
    const formatted = new Intl.NumberFormat(
      currency === "BRL" ? "pt-BR" : "en-US",
      {
        style: "currency",
        currency,
      }
    ).format(value);
    
    // Ensure consistent spacing for USD (add space after $ if not present)
    if (currency === "USD" && formatted.startsWith("$")) {
      return formatted.replace("$", "$ ");
    }
    return formatted;
  };

  const columns: ColumnDef<Category>[] = [
    {
      key: "name",
      label: "Name",
      render: (row: Category) => {
        return <span className="font-medium">{row.name}</span>;
      },
    },
    {
      key: "currencyName",
      label: "Currency",
    },
    {
      key: "dailyBudget",
      label: "Daily Budget",
      headerAlign: "right",
      render: (row: Category) => (
        <span className="text-right block">
          {formatCurrency(
            row.dailyBudget,
            row.currencyName as "USD" | "BRL"
          )}
        </span>
      ),
    },
    {
      key: "monthlyBudget",
      label: "Monthly Budget",
      headerAlign: "right",
      render: (row: Category) => (
        <span className="text-right block">
          {formatCurrency(
            row.monthlyBudget,
            row.currencyName as "USD" | "BRL"
          )}
        </span>
      ),
    },
  ];

  const actions: RowAction<Category>[] = [
    {
      label: "Edit",
      icon: <Edit className="h-4 w-4" />,
      onClick: handleEditCategory,
      color: "blue",
    },
  ];

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-3xl font-bold">
          Category Management
        </h1>
        <p className="text-sm text-muted-foreground">
          Manage categories and budgets
        </p>
      </header>

      <div className="rounded-lg border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900">
        <div className="flex justify-between gap-4">
          <SearchInput
            placeholder="Search category"
            value={searchValue}
            onChange={setSearchValue}
            onSearch={handleSearch}
          />
          <ActionButton
            label="Add Category"
            icon={<Plus className="h-4 w-4" />}
            onClick={() => { setCreateErrorMessage(""); setOpenCreateDialog(true) }}
          />
        </div>
      </div>

      <DataTable
        columns={columns}
        data={categories}
        actions={actions}
        emptyMessage={
          loading ? "Loading..." : error || "No categories found"
        }
      />

      {!loading && !error && totalElements > 0 && (
        <div className="rounded-lg border border-gray-200 bg-white dark:border-gray-800 dark:bg-gray-900">
          <TablePagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
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
