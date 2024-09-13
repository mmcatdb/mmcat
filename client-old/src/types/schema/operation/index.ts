import { SMOType, type SMOFromServer, type SMO } from './schemaModificationOperation';
import { CreateObject, type CreateObjectFromServer } from './createObject';
import { DeleteObject, type DeleteObjectFromServer } from './deleteObject';
import { UpdateObject, type UpdateObjectFromServer } from './updateObject';
import { CreateMorphism, type CreateMorphismFromServer } from './createMorphism';
import { DeleteMorphism, type DeleteMorphismFromServer } from './deleteMorphism';
import { UpdateMorphism, type UpdateMorphismFromServer } from './updateMorphism';
import { Composite, type CompositeFromServer } from './composite';

export function fromServer(input: SMOFromServer): SMO {
    switch (input.type) {
    case SMOType.CreateObject:
        return CreateObject.fromServer(input as CreateObjectFromServer);
    case SMOType.DeleteObject:
        return DeleteObject.fromServer(input as DeleteObjectFromServer);
    case SMOType.UpdateObject:
        return UpdateObject.fromServer(input as UpdateObjectFromServer);
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
    CreateObject,
    DeleteObject,
    UpdateObject,
    CreateMorphism,
    DeleteMorphism,
    UpdateMorphism,
    Composite,
};
