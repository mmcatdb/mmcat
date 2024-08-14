import React from "react";
import { Table, TableHeader, TableColumn, TableBody, TableRow, TableCell, User, Chip, Tooltip } from "@nextui-org/react";
import { EditIcon } from "@/components/icons/EditIcon";
import { DeleteIcon } from "@/components/icons/DeleteIcon";
import { EyeIcon } from "@/components/icons/EyeIcon";

// TODO: Add pagination and `Add New` Button
// https://nextui.org/docs/components/table#custom-styles

interface User {
  id: number;
  label: string;
  type: string;
  host: string;
  port: string;
  database: string;
  actions: string;
};

type ColumnKey = "label" | "id" | "type" | "actions";

const columns = [
    {name: "LABEL", uid: "label"},
    {name: "ID", uid: "id"},
    {name: "TYPE", uid: "type"},
    {name: "ACTIONS", uid: "actions"},
];

const users = [
    {
      id: 1,
      label: "PostgreSQL - Basic",
      type: "postgreSQL",
      host: "mmcat-postgresql",
      port: "5432",
      database: "example_basic",
      actions: "todo: delete this",
    },
    {
        id: 2,
        label: "MongoDB - Basic",
        type: "mongoDB",
        host: "mmcat-mongodb",
        port: "27017",
        database: "example_basic",
        actions: "todo: delete this",
    },
];

export default function DatabasesTable() {
  const renderCell = React.useCallback((user: User, columnKey: ColumnKey) => {
    const cellValue = user[columnKey];

    switch (columnKey) {
      case "label":
        return (
          <div className="flex flex-col">
            <p className="text-bold text-sm capitalize">{cellValue}</p>
          </div>
        );
      case "id":
        return (
          <div className="flex flex-col">
            <p className="text-bold text-sm capitalize">{cellValue}</p>
          </div>
        );
      case "type":
        return (
          <Chip className="capitalize" size="sm" variant="flat">
            {cellValue}
          </Chip>
        );
      case "actions":
        return (
          <div className="relative flex items-center gap-2">
            <Tooltip content="Details">
              <span className="text-lg text-default-400 cursor-pointer active:opacity-50">
                <EyeIcon />
              </span>
            </Tooltip>
            <Tooltip content="Edit user">
              <span className="text-lg text-default-400 cursor-pointer active:opacity-50">
                <EditIcon />
              </span>
            </Tooltip>
            <Tooltip color="danger" content="Delete user">
              <span className="text-lg text-danger cursor-pointer active:opacity-50">
                <DeleteIcon />
              </span>
            </Tooltip>
          </div>
        );
      default:
        return cellValue;
    }
  }, []);

  return (
    <Table removeWrapper aria-label="Project 2(NUMBER) details">
      <TableHeader columns={columns}>
        {(column) => (
          <TableColumn key={column.uid}>
            {column.name}
          </TableColumn>
        )}
      </TableHeader>
      <TableBody items={users}>
        {(item) => (
          <TableRow key={item.id}>
            {columns.map((column) => (
              <TableCell key={column.uid}>
                {renderCell(item, column.uid as ColumnKey)}
              </TableCell>
            ))}
          </TableRow>
        )}
      </TableBody>
    </Table>
  );
}
