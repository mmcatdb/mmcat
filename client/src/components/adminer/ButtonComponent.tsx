import { Button } from '@nextui-org/react';
import type { Id } from '@/types/id';

type ButtonComponentProps = Readonly<{
    key: unknown;
    value: Id;
}>;

export function ButtonComponent({ key, value }: ButtonComponentProps) {
    return (
        <Button color="primary" variant="ghost">
          {value}
        </Button>
      );
};
