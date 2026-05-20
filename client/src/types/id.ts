// TODO fix
// export type Id = SpecialType<string, 'Id'>;
export type Id = string;

export type Entity = {
    id: Id;
};

// TODO fix
// export type VersionId = UniqueType<string, 'version'>;
export type VersionId = string;

export function compareVersionIdsAsc(a: VersionId, b: VersionId) {
    return parseInt(a) - parseInt(b);
};

// The local parts aren't compared on BE - so we don't compare them here either.
// If it's required, the full comparison can be implemented like this ... However, the local part should probably depend on the specific use case.
//
// export function compareVersionIdsAsc(a: VersionId, b: VersionId) {
//     const [ aGlobal, aLocal ] = a.split(':');
//     const [ bGlobal, bLocal ] = b.split(':');

//     const aInt = parseInt(aGlobal);
//     const bInt = parseInt(bGlobal);

//     const intComparison = aInt - bInt;
//     if (intComparison !== 0)
//         return intComparison;

//     if (!aLocal)
//         return !bLocal ? 0 : -1;
//     if (!bLocal)
//         return 1;

//     if (a < b)
//         return -1;
//     else if (a > b)
//         return 1;
//     return 0;
// };
