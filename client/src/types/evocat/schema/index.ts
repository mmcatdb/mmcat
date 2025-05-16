import { SMOType, type SMOFromServer, type SMO } from './smo';
import { CreateObjex, type CreateObjexFromServer } from './createObjex';
import { DeleteObjex, type DeleteObjexFromServer } from './deleteObjex';
import { UpdateObjex, type UpdateObjexFromServer } from './updateObjex';
import { CreateMorphism, type CreateMorphismFromServer } from './createMorphism';
import { DeleteMorphism, type DeleteMorphismFromServer } from './deleteMorphism';
import { UpdateMorphism, type UpdateMorphismFromServer } from './updateMorphism';
import { Composite, type CompositeFromServer } from './composite';

export function smoFromServer(input: SMOFromServer): SMO {
    switch (input.type) {
    case SMOType.CreateObjex:
        return CreateObjex.fromServer(input as CreateObjexFromServer);
    case SMOType.DeleteObjex:
        return DeleteObjex.fromServer(input as DeleteObjexFromServer);
    case SMOType.UpdateObjex:
        return UpdateObjex.fromServer(input as UpdateObjexFromServer);
    case SMOType.CreateMorphism:
        return CreateMorphism.fromServer(input as CreateMorphismFromServer);
    case SMOType.DeleteMorphism:
        return DeleteMorphism.fromServer(input as DeleteMorphismFromServer);
    case SMOType.UpdateMorphism:
        return UpdateMorphism.fromServer(input as UpdateMorphismFromServer);
    case SMOType.Composite:
        return Composite.fromServer(input as CompositeFromServer);
    }
}

export {
    type SMO,
    type SMOFromServer,
    CreateObjex,
    DeleteObjex,
    UpdateObjex,
    CreateMorphism,
    DeleteMorphism,
    UpdateMorphism,
    Composite,
};
