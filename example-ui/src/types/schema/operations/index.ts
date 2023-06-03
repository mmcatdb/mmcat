import { SMOType, type SMOFromServer, type SMO } from './schemaModificationOperation';
import { CreateObject, type CreateObjectFromServer } from './createObject';
import { DeleteObject, type DeleteObjectFromServer } from './deleteObject';
import { CreateMorphism, type CreateMorphismFromServer } from './createMorphism';
import { DeleteMorphism, type DeleteMorphismFromServer } from './deleteMorphism';
import { EditMorphism, type EditMorphismFromServer } from './editMorphism';
import { Composite, type CompositeFromServer } from './composite';

export function fromServer(input: SMOFromServer): SMO {
    switch (input.type) {
    case SMOType.CreateObject:
        return CreateObject.fromServer(input as CreateObjectFromServer);
    case SMOType.DeleteObject:
        return DeleteObject.fromServer(input as DeleteObjectFromServer);
    case SMOType.CreateMorphism:
        return CreateMorphism.fromServer(input as CreateMorphismFromServer);
    case SMOType.DeleteMorphism:
        return DeleteMorphism.fromServer(input as DeleteMorphismFromServer);
    case SMOType.EditMorphism:
        return EditMorphism.fromServer(input as EditMorphismFromServer);
    case SMOType.Composite:
        return Composite.fromServer(input as CompositeFromServer);
    }
}

export {
    type SMO,
    type SMOFromServer,
    CreateObject,
    DeleteObject,
    CreateMorphism,
    DeleteMorphism,
    EditMorphism,
    Composite,
};
