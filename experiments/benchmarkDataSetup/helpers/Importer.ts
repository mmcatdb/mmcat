import { MongoClient } from 'mongodb'
import neo4j from 'neo4j-driver'
import { Client as PostgresClient } from 'pg'
import fs from 'fs'
import path from 'path'

const rootDir = path.join(import.meta.dirname, '..', '..', '..')

export class Importer {
    readonly databaseName: string
    readonly username: string
    readonly password: string

    readonly scalingFactor: number
    readonly recordKeyIndexes: Map<DataRecord[], Map<string, Map<object, DataRecord[]>>>

    constructor(databaseName: string, scalingFactor: number) {
        this.databaseName = databaseName
        this.scalingFactor = scalingFactor
        this.recordKeyIndexes = new Map<DataRecord[], Map<string, Map<object, DataRecord[]>>>()

        process.loadEnvFile(path.join(rootDir, '.env'))

        this.username = process.env.EXAMPLE_USERNAME!
        this.password = process.env.EXAMPLE_PASSWORD!
    }

    generateRecords(baseScale: number, recordCreationFunc: () => DataRecord, removeDuplicatesOfFields?: string[]): DataRecord[] {
        let output: DataRecord[] = []
        for (let i = 0; i < baseScale * this.scalingFactor; i++) {
            output.push(recordCreationFunc())
        }

        if (removeDuplicatesOfFields) {
            output.sort((a, b) => {
                for (const field of removeDuplicatesOfFields) {
                    if (a[field] != b[field]) return a[field] - b[field]
                }
                return 0
            })
            output = output.filter((a, idx) => {
                const b = output[idx - 1]
                for (const field of removeDuplicatesOfFields) {
                    if (a[field] != b[field]) return true
                }
                return false
            })

        }

        return output
    }

    private getOrCreateRecordKeyIndex(records: DataRecord[], key: string): Map<object, DataRecord[]> {
        let recordIndexes = this.recordKeyIndexes.get(records)
        if (recordIndexes === undefined) {
            recordIndexes = new Map<string, Map<object, DataRecord[]>>
            this.recordKeyIndexes.set(records, recordIndexes)
        }

        let keyIndex = recordIndexes.get(key)
        if (keyIndex === undefined) {
            keyIndex = new Map<object, DataRecord[]>

            // populate
            for (const record of records) {
                const value = record[key]

                let recordsWithValue = keyIndex.get(value)
                if (recordsWithValue === undefined) {
                    recordsWithValue = []
                    keyIndex.set(records, recordsWithValue)
                }
                recordsWithValue.push(record)
            }

            recordIndexes.set(key, keyIndex)
        }

        return keyIndex
    }

    findRecordByKey(records: DataRecord[], key: string, value: any): DataRecord[] {
        const index = this.getOrCreateRecordKeyIndex(records, key)
        return index.get(value)!
    }

    async importData(settings: ImportSettings) {
        if (settings.postgreSQL) {
            await this.importPostgreSQL(settings.postgreSQL)
        }
        if (settings.mongoDB) {
            await this.importMongoDB(settings.mongoDB)
        }
        if (settings.neo4j) {
            await this.importNeo4j(settings.neo4j)
        }
    }

    private async importPostgreSQL(kinds: PostgreSQLKindSettings[]) {
        const client = new PostgresClient({
            user: this.username,
            password: this.password,
            host: 'localhost',
            port: 3204,
            database: this.databaseName,
        })
        await client.connect()

        for (let i = kinds.length - 1; i >= 0; i--) {
            await client.query(`DROP TABLE IF EXISTS "${kinds[i].name}"`)
        }

        for (const kind of kinds) {
            await client.query(`CREATE TABLE "${kind.name}" (${kind.schema})`)

            function sanitizeForSQL(input: any): string {
                return input.toString().replaceAll("'", "''")
            }

            const BATCH_SIZE = 50_000

            for (let i = 0; i < kind.data.length; i += BATCH_SIZE) {
                const values = kind.data.slice(i, i + BATCH_SIZE).map(record => {
                    const output: string[] = []
                    for (const [key, value] of Object.entries(kind.structure)) {
                        if (value === false) {
                            continue
                        } else if (value === true) {
                            output.push(`'${sanitizeForSQL(record[key])}'`)
                        } else if (typeof(value) === 'string') {
                            output.push(`'${sanitizeForSQL(record[value])}'`)
                        } else {
                            throw new Error('PostgreSQL can only have flat relations')
                        }
                    }
                    return `(${output.join(',')})`
                }).join(',')

                await client.query(`INSERT INTO "${kind.name}" VALUES ${values}`)
            }
        }

        await client.end()
    }

    private async importMongoDB(kinds: MongoDBKindSettings[]) {

        const url = 'mongodb://localhost:3205';
        const client = new MongoClient(url, {
            auth: { username: this.username, password: this.password }
        });
        await client.connect();
        const db = client.db(this.databaseName);

        function projectRecord(oldRecord: any, structure: Structure): DataRecord {
            const newRecord: DataRecord = {}
            for (const [key, value] of Object.entries(structure)) {
                if (value === false) {
                    continue
                } else if (value === true) {
                    newRecord[key] = oldRecord[key]
                } else if (typeof(value) == 'string') {
                    newRecord[key] = oldRecord[value]
                } else if (value instanceof SubCollection) {
                    newRecord[key] = value
                        .getData(oldRecord)
                        .map(subRecord => typeof(value.structure) == 'string'
                            ? subRecord[value.structure]
                            : projectRecord(subRecord, value.structure)
                        )
                } else {
                    newRecord[key] = projectRecord(oldRecord, value)
                }
            }

            return newRecord
        }


        for (const kind of kinds) {
            const collection = db.collection(kind.name);

            await collection.deleteMany()

            const BATCH_SIZE = 10_000
            for (let i = 0; i < kind.data.length; i += BATCH_SIZE) {
                const values = kind.data
                    .slice(i, i + BATCH_SIZE)
                    .map(record => projectRecord(record, kind.structure))

                await collection.insertMany(values)
            }
        }

        await client.close()
    }

    private async importNeo4j(kinds: Neo4jKindSettings[]) {
        const driver = neo4j.driver(
            'neo4j://localhost:3206',
            neo4j.auth.basic('neo4j', this.password) // the free version only has 1 db and user (neo4j)
        )
        const session = driver.session()

        function isRelationship(settings: Neo4jKindSettings) {
            return (settings as any).from || (settings as any).to
        }

        const BATCH_SIZE = 10_000
        for (const kind of kinds) {
            if (isRelationship(kind)) continue
            await session.run(`
                MATCH (a:${kind.name})
                CALL { WITH a
                    DETACH DELETE a
                } IN TRANSACTIONS OF ${BATCH_SIZE} ROWS
            `)
        }

        for (const kind of kinds) {
            const filename = csvExporter.export(kind.data, this.databaseName, kind.name)

            function attributes(structure: Structure) {
                const attributes: string[] = []
                for (const [key, value] of Object.entries(structure)) {
                    if (value === false) {
                        continue
                    } else if (value === true) {
                        attributes.push(`${key}: row.${key}`)
                    } else if (typeof(value) === 'string') {
                        attributes.push(`${key}: row.${value}`)
                    } else {
                        throw new Error('Neo4j import so far only supports flat relations')
                        // NOTE: Originally, I thought we will need apoc.load_json() for non-flat relations, but MERGE might actually suffice; regardless, that's a question for later
                    }
                }
                return attributes.join(', ')
            }

            if (isRelationship(kind)) {
                const rkind = kind as Neo4jRelationshipSettings
                await session.run(`
                    LOAD CSV WITH HEADERS FROM 'file:///${filename}' AS row
                    MATCH (a:${rkind.from.label} { ${attributes(rkind.from.match)} }),
                          (b:${rkind.to.label} { ${attributes(rkind.to.match)} })
                    CREATE (a)-[:${rkind.name} { ${attributes(rkind.structure)} }]->(b)
                `)
            } else {
                await session.run(`
                    LOAD CSV WITH HEADERS FROM 'file:///${filename}' AS row
                    CREATE (:${kind.name} { ${attributes(kind.structure)} })
                `)
            }
        }

        await session.close()
        await driver.close()
    }
}

const csvExporter = {
    exported: new Map<DataRecord[], string>(),
    export(records: DataRecord[], dbName: string, kindName?: string) {
        const existing = csvExporter.exported.get(records)
        if (existing) return existing

        const filename = `generated-${dbName}-${kindName ?? csvExporter.exported.size}.csv`
        const filepath = path.join(rootDir, 'data', filename)

        const keys = Object.keys(records[0])

        const header = keys.map(k => `"${csvExporter.sanitizeForCSV(k)}"`).join(',')
        const values = records.map(r =>
            keys.map(k => `"${csvExporter.sanitizeForCSV(r[k])}"`).join(',')
        )

        fs.writeFileSync(filepath, header + '\r\n' + values.join('\r\n'))
        csvExporter.exported.set(records, filename)
        return filename
    },

    sanitizeForCSV(input: any): string {
        return input.toString().replaceAll('"', '""')
    },
}

/**
 * Given a generation function and a range, repeatedly generates until the output is within the range.
 * 
 * It is not too smart and in extreme cases might significantly slow down the computations, but should be fine when used carefully.
 */
export function generateWithinRange(generationFunc: () => number, min: number = -Infinity, max: number = Infinity) {
    let generated = generationFunc()
    while (generated < min || generated > max) {
        generated = generationFunc()
    }
    return generated
}


// region types

type DataRecord = any

type Structure = {
    [key: string]: boolean|string|Structure|SubCollection
}

export class SubCollection { // always under an array
    private readonly data: DataRecord[] | SubCollectionDataFunc
    public readonly structure: Structure | string

    constructor(data: DataRecord[] | SubCollectionDataFunc, structure: Structure | string) {
        this.data = data
        this.structure = structure
    }

    getData(superRecord: DataRecord): DataRecord[] {
        if (typeof(this.data) == 'function') {
            return this.data(superRecord)
        } else {
            return this.data
        }
    }
}

type SubCollectionDataFunc = (record: DataRecord) => DataRecord[] // NOTE: later *maybe* replace (or add to) the single (topmost) record for some context in nested SubCollections, then you can do something like record.super.super.whatever_key

// NOTE: in the future there could also be a Join class or function here so that records can be combined more ways than subcollections, however joining would require aliasing the attributes into the result, which is too complicated for now

type ImportSettings = {
    postgreSQL?: PostgreSQLKindSettings[],
    mongoDB?: MongoDBKindSettings[],
    neo4j?: Neo4jKindSettings[],
}

type PostgreSQLKindSettings = {
    name: string,
    schema: string,
    data: DataRecord[],
    structure: Structure,
}

type MongoDBKindSettings = {
    name: string,
    data: DataRecord[],
    structure: Structure,
}

type Neo4jNodeSettings = {
    name: string,
    data: DataRecord[],
    structure: Structure,
}
type Neo4jRelationshipSettings = {
    name: string,
    data: DataRecord[],
    structure: Structure,
    from: {
        label: string,
        match: Structure
    },
    to: {
        label: string,
        match: Structure
    },
}
type Neo4jKindSettings = Neo4jNodeSettings | Neo4jRelationshipSettings

// endregion
