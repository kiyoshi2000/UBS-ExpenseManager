import { ReactNode } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export interface ColumnDef<TData> {
  key: keyof TData;
  label: string;
  render?: (row: TData) => ReactNode;
  width?: string;
}

export interface RowAction<TData> {
  label: string;
  icon: ReactNode;
  onClick: (row: TData) => void;
  color?: "blue" | "red" | "green" | "yellow";
  shouldShow?: (row: TData) => boolean;
}

export interface DataTableProps<TData> {
  columns: ColumnDef<TData>[];
  data: TData[];
  actions?: RowAction<TData>[];
  emptyMessage?: string;
  className?: string;
  rowClassName?: string;
}

const colorClasses = {
  blue: "text-blue-600",
  red: "text-red-600",
  green: "text-green-600",
  yellow: "text-yellow-600",
};

export const DataTable = <TData,>(
  {
    columns,
    data,
    actions,
    emptyMessage = "Nenhum dado disponível",
    className = "",
    rowClassName = "",
  }: DataTableProps<TData>
) => {
    return (
      <div className={`rounded-lg border border-gray-200 bg-white dark:border-gray-800 dark:bg-gray-900 ${className}`}>
        <Table>
          <TableHeader>
            <TableRow>
              {columns.map((column) => (
                <TableHead key={String(column.key)} style={column.width ? { width: column.width } : {}}>
                  {column.label}
                </TableHead>
              ))}
              {actions && actions.length > 0 && (
                <TableHead className="w-20">Ações</TableHead>
              )}
            </TableRow>
          </TableHeader>
          <TableBody>
            {data.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={columns.length + (actions?.length ? 1 : 0)}
                  className="text-center py-8 text-gray-500"
                >
                  {emptyMessage}
                </TableCell>
              </TableRow>
            ) : (
              data.map((row, rowIndex) => (
                <TableRow 
                  key={rowIndex} 
                  className={`hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors ${rowClassName}`}
                >
                  {columns.map((column) => (
                    <TableCell key={String(column.key)}>
                      {column.render
                        ? column.render(row)
                        : String(row[column.key])}
                    </TableCell>
                  ))}
                  {actions && actions.length > 0 && (
                    <TableCell>
                      <div className="flex items-center gap-2">
                        {actions
                          .filter((action) => !action.shouldShow || action.shouldShow(row))
                          .map((action, actionIndex) => (
                          <button
                            key={actionIndex}
                            onClick={() => action.onClick(row)}
                            className="rounded p-1 hover:bg-gray-100 dark:hover:bg-gray-800 cursor-pointer transition-colors"
                            title={action.label}
                          >
                            <div
                              className={`${colorClasses[action.color || "blue"]}`}
                            >
                              {action.icon}
                            </div>
                          </button>
                        ))}
                      </div>
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    );
  };

DataTable.displayName = "DataTable";
