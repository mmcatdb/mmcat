import { CommonPage } from "@/components/CommonPage";
import { Button, Slider } from "@nextui-org/react";
import { CollapsibleTable } from "@/components/CollapsibleTable";

export function Databases() {
    return (
        <CommonPage>
            <h1>Databases</h1>
            <p>
                Some text.
            </p>
            <Slider 
                label="Temperature" 
                step={0.01} 
                maxValue={1} 
                minValue={0} 
                defaultValue={0.4}
                className="max-w-md bg-purple-400 p-4 rounded-lg my-5"
            />
            <Button>+ Add project</Button>
            <div className="mt-5">
                <CollapsibleTable />
            </div>
        </CommonPage>
    );
}
