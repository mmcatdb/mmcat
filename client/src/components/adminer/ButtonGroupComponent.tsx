import { ButtonGroup } from '@nextui-org/react';
import { ButtonComponent } from '@/components/adminer/ButtonComponent';
import type { Datasource, DatasourceType } from '@/types/datasource';

type ButtonGroupComponentProps = Readonly<{
  db: DatasourceType;
  data: Datasource[];
}>;

export function ButtonGroupComponent({ db, data }: ButtonGroupComponentProps) {
  return (
      <ButtonGroup>
        {data
          .filter((item) => item.type === db)
          .map((item, index) => (
            <ButtonComponent key={index} value={item.id}></ButtonComponent>
          ))}
      </ButtonGroup>
    );
};
