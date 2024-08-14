import { Accordion, AccordionItem } from "@nextui-org/react";
import { Table, TableHeader, TableColumn, TableBody, TableRow, TableCell } from "@nextui-org/react";
import DatabasesTable from "@/components/databases/DatabasesTable";

export function CollapsibleTable() {
  return (
    <Accordion variant="splitted" selectionMode="multiple">
      <AccordionItem title="Project 1">
        <Table removeWrapper aria-label="Project 1 details">
          <TableHeader>
            <TableColumn>LABEL</TableColumn>
            <TableColumn>ID</TableColumn>
            <TableColumn>TYPE</TableColumn>
          </TableHeader>
          <TableBody>
            <TableRow>
              <TableCell>PostgreSQL - Basic</TableCell>
              <TableCell>1</TableCell>
              <TableCell>postgresql</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </AccordionItem>

      <AccordionItem title="Project 2">
        <DatabasesTable />
      </AccordionItem>
    </Accordion>
  );
}
