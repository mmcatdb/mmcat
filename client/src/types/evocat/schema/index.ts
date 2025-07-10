import { SMOType, type SMOResponse, type SMO } from './smo';
import { CreateObjex, type CreateObjexResponse } from './createObjex';
import { DeleteObjex, type DeleteObjexResponse } from './deleteObjex';
import { UpdateObjex, type UpdateObjexResponse } from './updateObjex';
import { CreateMorphism, type CreateMorphismResponse } from './createMorphism';
import { DeleteMorphism, type DeleteMorphismResponse } from './deleteMorphism';
import { UpdateMorphism, type UpdateMorphismResponse } from './updateMorphism';
import { Composite, type CompositeResponse } from './composite';

export function smoFromResponse(input: SMOResponse): SMO {
    switch (input.type) {
    case SMOType.CreateObjex:
        return CreateObjex.fromResponse(input as CreateObjexResponse);
    case SMOType.DeleteObjex:
        return DeleteObjex.fromResponse(input as DeleteObjexResponse);
    case SMOType.UpdateObjex:
        return UpdateObjex.fromResponse(input as UpdateObjexResponse);
    case SMOType.CreateMorphism:
        return CreateMorphism.fromResponse(input as CreateMorphismResponse);
    case SMOType.DeleteMorphism:
        return DeleteMorphism.fromResponse(input as DeleteMorphismResponse);
    case SMOType.UpdateMorphism:
        return UpdateMorphism.fromResponse(input as UpdateMorphismResponse);
    case SMOType.Composite:
        return Composite.fromResponse(input as CompositeResponse);
    }
}

export {
    type SMO,
    type SMOResponse,
    CreateObjex,
    DeleteObjex,
    UpdateObjex,
    CreateMorphism,
    DeleteMorphism,
    UpdateMorphism,
    Composite,
};
