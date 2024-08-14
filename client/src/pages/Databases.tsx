import { CommonPage } from "@/components/CommonPage";
import { Button } from "@nextui-org/react";
import { CollapsibleTable } from "@/components/databases/CollapsibleTable";

export function Databases() {
    return (
        <CommonPage>
            <h1>Databases in projects</h1>
            <Button startContent={<p>🐢</p>}>Add project</Button>
            <div className="mt-5">
                <CollapsibleTable />
            </div>
        </CommonPage>
    );
}
