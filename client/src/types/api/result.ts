export const ResultSuccess: Result = { status: true };
export const ResultError: Result = { status: false, error: 'Error' };

export function DataResultSuccess<Data>(data: Data): DataResult<Data> {
    return { status: true, data };
}

export function onSuccess<Data, NewData>(result: DataResult<Data>, next: (data: Data) => NewData): DataResult<NewData> {
    return result.status === true ? DataResultSuccess(next(result.data)) : result;
}

export type Result<DataType = undefined> = DataType extends undefined ? EmptyResult : DataResult<DataType>;

export type EmptyResult = {
    status: true;
} | {
    status: false;
    error: any;
};

export type DataResult<Data = any> = {
    status: true;
    data: Data;
} | {
    status: false;
    error: any;
};

export enum Crud {
    None,
    Create,
    Update,
    Delete,
}

export type Operation<Data> = {
    type: Crud;
    data: Data;
};
