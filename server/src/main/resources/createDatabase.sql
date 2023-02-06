DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS mapping;
DROP TABLE IF EXISTS logical_model;
DROP TABLE IF EXISTS data_source;
DROP TABLE IF EXISTS database_for_mapping;

-- DROP TABLE IF EXISTS schema_morphism_in_category;
-- DROP TABLE IF EXISTS schema_object_in_category;
-- DROP TABLE IF EXISTS schema_morphism;
-- DROP TABLE IF EXISTS schema_object;
DROP TABLE IF EXISTS schema_category;

-- Incrementation of the sequnce for generating ids:
-- SELECT nextval('tableName_seq_id')

-- TODO name to label

CREATE TABLE schema_category (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO schema_category (json_value)
VALUES
    ('{
        "label": "Article example",
        "version": "0",
        "objects": [
            {"label": "Customer", "position": {"x": -99, "y": -5}, "ids": {"type": "Signatures", "signatureIds": [[[1]]]}, "key": {"value": 1}, "superId": [[1]]},
            {"label": "Id", "position": {"x": -138, "y": 94}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 2}, "superId": [[]]},
            {"label": "Orders", "position": {"x": -11, "y": -75}, "ids": {"type": "Signatures", "signatureIds": [[[1, 3]]]}, "key": {"value": 3}, "superId": [[1, 3]]},
            {"label": "Order", "position": {"x": 134, "y": -85}, "ids": {"type": "Signatures", "signatureIds": [[[4], [1, 3, 2]]]}, "key": {"value": 4}, "superId": [[4], [1, 3, 2]]},
            {"label": "Number", "position": {"x": 140, "y": -188}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 5}, "superId": [[]]},
            {"label": "Contact", "position": {"x": 271, "y": -83}, "ids": {"type": "Signatures", "signatureIds": [[[8, 7], [6]]]}, "key": {"value": 6}, "superId": [[8, 7], [6]]},
            {"label": "Value", "position": {"x": 273, "y": -190}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 7}, "superId": [[]]},
            {"label": "Type", "position": {"x": 394, "y": -86}, "ids": {"type": "Signatures", "signatureIds": [[[8]]]}, "key": {"value": 8}, "superId": [[8]]},
            {"label": "Name", "position": {"x": 399, "y": -180}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 9}, "superId": [[]]},
            {"label": "Items", "position": {"x": 136, "y": -6}, "ids": {"type": "Signatures", "signatureIds": [[[1, 3, 2, -9], [4, -9], [12, 10]]]}, "key": {"value": 10}, "superId": [[1, 3, 2, -9], [4, -9], [12, 10]]},
            {"label": "Quantity", "position": {"x": 258, "y": -5}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 11}, "superId": [[]]},
            {"label": "Product", "position": {"x": 128, "y": 85}, "ids": {"type": "Signatures", "signatureIds": [[[12]]]}, "key": {"value": 12}, "superId": [[12]]},
            {"label": "Id", "position": {"x": 47, "y": 189}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 13}, "superId": [[]]},
            {"label": "Name", "position": {"x": 125, "y": 187}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 14}, "superId": [[]]},
            {"label": "Price", "position": {"x": 213, "y": 189}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 15}, "superId": [[]]}
        ],
        "morphisms": [
            {"domKey": {"value": 1}, "codKey": {"value": 2}, "max": "ONE", "min": "ONE", "signature": [1]},
            {"domKey": {"value": 2}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "signature": [-1]},
            {"domKey": {"value": 4}, "codKey": {"value": 3}, "max": "ONE", "min": "ONE", "signature": [2]},
            {"domKey": {"value": 3}, "codKey": {"value": 4}, "max": "ONE", "min": "ONE", "signature": [-2]},
            {"domKey": {"value": 3}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "signature": [3]},
            {"domKey": {"value": 1}, "codKey": {"value": 3}, "max": "STAR", "min": "ZERO", "signature": [-3]},
            {"domKey": {"value": 4}, "codKey": {"value": 5}, "max": "ONE", "min": "ONE", "signature": [4]},
            {"domKey": {"value": 5}, "codKey": {"value": 4}, "max": "STAR", "min": "ZERO", "signature": [-4]},
            {"domKey": {"value": 4}, "codKey": {"value": 6}, "max": "STAR", "min": "ZERO", "signature": [5]},
            {"domKey": {"value": 6}, "codKey": {"value": 4}, "max": "STAR", "min": "ZERO", "signature": [-5]},
            {"domKey": {"value": 6}, "codKey": {"value": 7}, "max": "ONE", "min": "ONE", "signature": [6]},
            {"domKey": {"value": 7}, "codKey": {"value": 6}, "max": "STAR", "min": "ZERO", "signature": [-6]},
            {"domKey": {"value": 6}, "codKey": {"value": 8}, "max": "ONE", "min": "ONE", "signature": [7]},
            {"domKey": {"value": 8}, "codKey": {"value": 6}, "max": "STAR", "min": "ZERO", "signature": [-7]},
            {"domKey": {"value": 8}, "codKey": {"value": 9}, "max": "ONE", "min": "ONE", "signature": [8]},
            {"domKey": {"value": 9}, "codKey": {"value": 8}, "max": "ONE", "min": "ONE", "signature": [-8]},
            {"domKey": {"value": 4}, "codKey": {"value": 10}, "max": "STAR", "min": "ONE", "signature": [9]},
            {"domKey": {"value": 10}, "codKey": {"value": 4}, "max": "ONE", "min": "ONE", "signature": [-9]},
            {"domKey": {"value": 10}, "codKey": {"value": 12}, "max": "ONE", "min": "ONE", "signature": [10]},
            {"domKey": {"value": 12}, "codKey": {"value": 10}, "max": "STAR", "min": "ZERO", "signature": [-10]},
            {"domKey": {"value": 10}, "codKey": {"value": 11}, "max": "ONE", "min": "ZERO", "signature": [11]},
            {"domKey": {"value": 11}, "codKey": {"value": 10}, "max": "STAR", "min": "ZERO", "signature": [-11]},
            {"domKey": {"value": 12}, "codKey": {"value": 13}, "max": "ONE", "min": "ONE", "signature": [12]},
            {"domKey": {"value": 13}, "codKey": {"value": 12}, "max": "ONE", "min": "ONE", "signature": [-12]},
            {"domKey": {"value": 12}, "codKey": {"value": 14}, "max": "STAR", "min": "ZERO", "signature": [13]},
            {"domKey": {"value": 14}, "codKey": {"value": 12}, "max": "STAR", "min": "ZERO", "signature": [-13]},
            {"domKey": {"value": 12}, "codKey": {"value": 15}, "max": "STAR", "min": "ZERO", "signature": [14]},
            {"domKey": {"value": 15}, "codKey": {"value": 12}, "max": "STAR", "min": "ZERO", "signature": [-14]}
        ]
    }'),
    ('{
        "label": "Tables to document",
        "version": "0",
        "objects": [
            {"label": "customer", "position": {"x": -21, "y": 135}, "ids": {"type": "Signatures", "signatureIds": [[[4]]]}, "key": {"value": 1}, "superId": [[4]]},
            {"label": "full name", "position": {"x": -94, "y": 287}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 2}, "superId": [[]]},
            {"label": "id", "position": {"x": 48, "y": 286}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 3}, "superId": [[]]},
            {"label": "contact", "position": {"x": -250, "y": 109}, "ids": {"type": "Signatures", "signatureIds": [[[1]]]}, "key": {"value": 4}, "superId": [[1]]},
            {"label": "type", "position": {"x": -415, "y": 54}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 5}, "superId": [[]]},
            {"label": "value", "position": {"x": -403, "y": 152}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 6}, "superId": [[]]},
            {"label": "id", "position": {"x": -344, "y": 233}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 7}, "superId": [[]]},
            {"label": "customer contact", "position": {"x": -172, "y": 194}, "ids": {"type": "Signatures", "signatureIds": [[[4, 7], [1, 6]]]}, "key": {"value": 8}, "superId": [[4, 7], [1, 6]]},
            {"label": "order", "position": {"x": 191, "y": 129}, "ids": {"type": "Signatures", "signatureIds": [[[13]]]}, "key": {"value": 9}, "superId": [[13]]},
            {"label": "created", "position": {"x": 334, "y": 232}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 10}, "superId": [[]]},
            {"label": "paid", "position": {"x": 175, "y": 286}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 11}, "superId": [[]]},
            {"label": "sent", "position": {"x": 269, "y": 295}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 12}, "superId": [[]]},
            {"label": "note", "position": {"x": 347, "y": 127}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 13}, "superId": [[]]},
            {"label": "delivery address", "position": {"x": 338, "y": 30}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 14}, "superId": [[]]},
            {"label": "id", "position": {"x": 251, "y": -23}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 15}, "superId": [[]]},
            {"label": "product", "position": {"x": -79, "y": -64}, "ids": {"type": "Signatures", "signatureIds": [[[15]]]}, "key": {"value": 16}, "superId": [[15]]},
            {"label": "price", "position": {"x": -187, "y": 5}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 17}, "superId": [[]]},
            {"label": "name", "position": {"x": -221, "y": -96}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 18}, "superId": [[]]},
            {"label": "id", "position": {"x": -148, "y": -177}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 19}, "superId": [[]]},
            {"label": "order item", "position": {"x": 94, "y": -22}, "ids": {"type": "Signatures", "signatureIds": [[[13, 21], [15, 20]]]}, "key": {"value": 20}, "superId": [[13, 21], [15, 20]]},
            {"label": "amount", "position": {"x": 49, "y": -150}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 21}, "superId": [[]]},
            {"label": "total price", "position": {"x": 166, "y": -145}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 22}, "superId": [[]]}
        ],
        "morphisms": [
            {"domKey": {"value": 4}, "codKey": {"value": 7}, "max": "ONE", "min": "ONE", "signature": [1]},
            {"domKey": {"value": 7}, "codKey": {"value": 4}, "max": "ONE", "min": "ONE", "signature": [-1]},
            {"domKey": {"value": 4}, "codKey": {"value": 6}, "max": "ONE", "min": "ONE", "signature": [2]},
            {"domKey": {"value": 6}, "codKey": {"value": 4}, "max": "STAR", "min": "ONE", "signature": [-2]},
            {"domKey": {"value": 4}, "codKey": {"value": 5}, "max": "ONE", "min": "ONE", "signature": [3]},
            {"domKey": {"value": 5}, "codKey": {"value": 4}, "max": "STAR", "min": "ZERO", "signature": [-3]},
            {"domKey": {"value": 1}, "codKey": {"value": 3}, "max": "ONE", "min": "ONE", "signature": [4]},
            {"domKey": {"value": 3}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "signature": [-4]},
            {"domKey": {"value": 1}, "codKey": {"value": 2}, "max": "ONE", "min": "ONE", "signature": [5]},
            {"domKey": {"value": 2}, "codKey": {"value": 1}, "max": "STAR", "min": "ONE", "signature": [-5]},
            {"domKey": {"value": 8}, "codKey": {"value": 4}, "max": "ONE", "min": "ONE", "signature": [6]},
            {"domKey": {"value": 4}, "codKey": {"value": 8}, "max": "STAR", "min": "ONE", "signature": [-6]},
            {"domKey": {"value": 8}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "signature": [7]},
            {"domKey": {"value": 1}, "codKey": {"value": 8}, "max": "STAR", "min": "ZERO", "signature": [-7]},
            {"domKey": {"value": 9}, "codKey": {"value": 10}, "max": "ONE", "min": "ZERO", "signature": [8]},
            {"domKey": {"value": 10}, "codKey": {"value": 9}, "max": "STAR", "min": "ONE", "signature": [-8]},
            {"domKey": {"value": 9}, "codKey": {"value": 12}, "max": "ONE", "min": "ZERO", "signature": [9]},
            {"domKey": {"value": 12}, "codKey": {"value": 9}, "max": "STAR", "min": "ONE", "signature": [-9]},
            {"domKey": {"value": 9}, "codKey": {"value": 11}, "max": "ONE", "min": "ZERO", "signature": [10]},
            {"domKey": {"value": 11}, "codKey": {"value": 9}, "max": "STAR", "min": "ONE", "signature": [-10]},
            {"domKey": {"value": 9}, "codKey": {"value": 13}, "max": "ONE", "min": "ONE", "signature": [11]},
            {"domKey": {"value": 13}, "codKey": {"value": 9}, "max": "STAR", "min": "ONE", "signature": [-11]},
            {"domKey": {"value": 9}, "codKey": {"value": 14}, "max": "ONE", "min": "ONE", "signature": [12]},
            {"domKey": {"value": 14}, "codKey": {"value": 9}, "max": "STAR", "min": "ONE", "signature": [-12]},
            {"domKey": {"value": 9}, "codKey": {"value": 15}, "max": "ONE", "min": "ONE", "signature": [13]},
            {"domKey": {"value": 15}, "codKey": {"value": 9}, "max": "ONE", "min": "ONE", "signature": [-13]},
            {"domKey": {"value": 9}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "signature": [14]},
            {"domKey": {"value": 1}, "codKey": {"value": 9}, "max": "STAR", "min": "ZERO", "signature": [-14]},
            {"domKey": {"value": 16}, "codKey": {"value": 19}, "max": "ONE", "min": "ONE", "signature": [15]},
            {"domKey": {"value": 19}, "codKey": {"value": 16}, "max": "ONE", "min": "ONE", "signature": [-15]},
            {"domKey": {"value": 16}, "codKey": {"value": 18}, "max": "ONE", "min": "ONE", "signature": [16]},
            {"domKey": {"value": 18}, "codKey": {"value": 16}, "max": "STAR", "min": "ONE", "signature": [-16]},
            {"domKey": {"value": 16}, "codKey": {"value": 17}, "max": "ONE", "min": "ONE", "signature": [17]},
            {"domKey": {"value": 17}, "codKey": {"value": 16}, "max": "STAR", "min": "ONE", "signature": [-17]},
            {"domKey": {"value": 20}, "codKey": {"value": 22}, "max": "ONE", "min": "ONE", "signature": [18]},
            {"domKey": {"value": 22}, "codKey": {"value": 20}, "max": "STAR", "min": "ONE", "signature": [-18]},
            {"domKey": {"value": 20}, "codKey": {"value": 21}, "max": "ONE", "min": "ONE", "signature": [19]},
            {"domKey": {"value": 21}, "codKey": {"value": 20}, "max": "STAR", "min": "ONE", "signature": [-19]},
            {"domKey": {"value": 20}, "codKey": {"value": 16}, "max": "ONE", "min": "ONE", "signature": [20]},
            {"domKey": {"value": 16}, "codKey": {"value": 20}, "max": "STAR", "min": "ZERO", "signature": [-20]},
            {"domKey": {"value": 20}, "codKey": {"value": 9}, "max": "ONE", "min": "ONE", "signature": [21]},
            {"domKey": {"value": 9}, "codKey": {"value": 20}, "max": "STAR", "min": "ONE", "signature": [-21]}
        ]
    }'),
    ('{
        "label": "Querying example",
        "version": "0",
        "objects": [
            {"label": "Customer", "position": {"x": 561, "y": 415}, "ids": {"type": "Signatures", "signatureIds": [[[2]]]}, "key": {"value": 1}, "superId": [[2]]},
            {"label": "Name", "position": {"x": 556, "y": 532}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 2}, "superId": [[]]},
            {"label": "Id", "position": {"x": 552, "y": 178}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 3}, "superId": [[]]},
            {"label": "Surname", "position": {"x": 717, "y": 405}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 4}, "superId": [[]]},
            {"label": "Friends", "position": {"x": 722, "y": 525}, "ids": {"type": "Signatures", "signatureIds": [[[2, 4], [2, 5]]]}, "key": {"value": 5}, "superId": [[2, 4], [2, 5]]},
            {"label": "Contact", "position": {"x": 293, "y": 307}, "ids": {"type": "Signatures", "signatureIds": [[[7], [8], [2, 6]]]}, "key": {"value": 6}, "superId": [[7], [8], [2, 6]]},
            {"label": "Key", "position": {"x": 147, "y": 308}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 7}, "superId": [[]]},
            {"label": "Value", "position": {"x": 294, "y": 190}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 8}, "superId": [[]]},
            {"label": "Orders", "position": {"x": 141, "y": 404}, "ids": {"type": "Signatures", "signatureIds": [[[2, 9], [11, 10]]]}, "key": {"value": 9}, "superId": [[2, 9], [11, 10]]},
            {"label": "Order", "position": {"x": 155, "y": 206}, "ids": {"type": "Signatures", "signatureIds": [[[11]]]}, "key": {"value": 10}, "superId": [[11]]},
            {"label": "Id", "position": {"x": 189, "y": 520}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 11}, "superId": [[]]},
            {"label": "Items", "position": {"x": 189, "y": 656}, "ids": {"type": "Signatures", "signatureIds": [[[11, 12], [14, 13]]]}, "key": {"value": 12}, "superId": [[11, 12], [14, 13]]},
            {"label": "Product", "position": {"x": 300, "y": 657}, "ids": {"type": "Signatures", "signatureIds": [[[14]]]}, "key": {"value": 13}, "superId": [[14]]},
            {"label": "Number", "position": {"x": 433, "y": 304}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 14}, "superId": [[]]},
            {"label": "Name", "position": {"x": 559, "y": 299}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 15}, "superId": [[]]},
            {"label": "Price", "position": {"x": 439, "y": 533}, "ids": {"type": "Value", "signatureIds": [[]]}, "key": {"value": 16}, "superId": [[]]}
        ],
        "morphisms": [
            {"domKey": {"value": 1}, "codKey": {"value": 2}, "max": "ONE", "min": "ONE", "label": "", "signature": [1]},
            {"domKey": {"value": 2}, "codKey": {"value": 1}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-1]},
            {"domKey": {"value": 1}, "codKey": {"value": 3}, "max": "ONE", "min": "ONE", "label": "", "signature": [2]},
            {"domKey": {"value": 3}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "label": "", "signature": [-2]},
            {"domKey": {"value": 1}, "codKey": {"value": 4}, "max": "ONE", "min": "ONE", "label": "", "signature": [3]},
            {"domKey": {"value": 4}, "codKey": {"value": 1}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-3]},
            {"domKey": {"value": 5}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "label": "", "signature": [4]},
            {"domKey": {"value": 1}, "codKey": {"value": 5}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-4]},
            {"domKey": {"value": 5}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "label": "", "signature": [5]},
            {"domKey": {"value": 1}, "codKey": {"value": 5}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-5]},
            {"domKey": {"value": 6}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "label": "", "signature": [6]},
            {"domKey": {"value": 1}, "codKey": {"value": 6}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-6]},
            {"domKey": {"value": 6}, "codKey": {"value": 7}, "max": "ONE", "min": "ONE", "label": "", "signature": [7]},
            {"domKey": {"value": 7}, "codKey": {"value": 6}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-7]},
            {"domKey": {"value": 6}, "codKey": {"value": 8}, "max": "ONE", "min": "ONE", "label": "", "signature": [8]},
            {"domKey": {"value": 8}, "codKey": {"value": 6}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-8]},
            {"domKey": {"value": 9}, "codKey": {"value": 1}, "max": "ONE", "min": "ONE", "label": "", "signature": [9]},
            {"domKey": {"value": 1}, "codKey": {"value": 9}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-9]},
            {"domKey": {"value": 9}, "codKey": {"value": 10}, "max": "ONE", "min": "ONE", "label": "", "signature": [10]},
            {"domKey": {"value": 10}, "codKey": {"value": 9}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-10]},
            {"domKey": {"value": 10}, "codKey": {"value": 14}, "max": "ONE", "min": "ONE", "label": "", "signature": [11]},
            {"domKey": {"value": 14}, "codKey": {"value": 10}, "max": "ONE", "min": "ONE", "label": "", "signature": [-11]},
            {"domKey": {"value": 12}, "codKey": {"value": 10}, "max": "ONE", "min": "ONE", "label": "", "signature": [12]},
            {"domKey": {"value": 10}, "codKey": {"value": 12}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-12]},
            {"domKey": {"value": 12}, "codKey": {"value": 13}, "max": "ONE", "min": "ONE", "label": "", "signature": [13]},
            {"domKey": {"value": 13}, "codKey": {"value": 12}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-13]},
            {"domKey": {"value": 13}, "codKey": {"value": 11}, "max": "ONE", "min": "ONE", "label": "", "signature": [14]},
            {"domKey": {"value": 11}, "codKey": {"value": 13}, "max": "ONE", "min": "ONE", "label": "", "signature": [-14]},
            {"domKey": {"value": 13}, "codKey": {"value": 15}, "max": "ONE", "min": "ONE", "label": "", "signature": [15]},
            {"domKey": {"value": 15}, "codKey": {"value": 13}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-15]},
            {"domKey": {"value": 13}, "codKey": {"value": 16}, "max": "ONE", "min": "ONE", "label": "", "signature": [16]},
            {"domKey": {"value": 16}, "codKey": {"value": 13}, "max": "STAR", "min": "ZERO", "label": "", "signature": [-16]}
        ]
    }');

CREATE TABLE database_for_mapping (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO database_for_mapping (json_value)
VALUES
    ('{ "type": "mongodb", "label": "MongoDB",
        "settings": {
            "host": "localhost",
            "port": "27017",
            "database": "mmcat_server_data",
            "authenticationDatabase": "admin",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL",
        "settings": {
            "host": "localhost",
            "port": "5432",
            "database": "mmcat_server_data",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL TTD",
        "settings": {
            "host": "localhost",
            "port": 5432,
            "database": "mmcat_server_ttd",
            "password": "mmcat_password",
            "username": "mmcat_user"
        }
    }'),
    ('{ "type": "mongodb", "label": "MongoDB Experiments",
        "settings": {
            "host": "localhost",
            "port": "27017",
            "database": "mmcat_server_experiments",
            "authenticationDatabase": "admin",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL Experiments",
        "settings": {
            "host": "localhost",
            "port": "5432",
            "database": "mmcat_server_experiments",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "mongodb", "label": "MongoDB - Querying",
        "settings": {
            "host": "localhost",
            "port": 27017,
            "database": "mmcat_server_querying",
            "password": "mmcat_password",
            "username": "mmcat_user",
            "authenticationDatabase": "admin"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL - Querying",
        "settings": {
            "host": "localhost",
            "port": 5432,
            "database": "mmcat_server_querying",
            "password": "mmcat_password",
            "username": "mmcat_user"
        }
    }'),
    ('{ "type": "postgresql", "label": "Neo4j - Querying",
        "settings": {
            "host": "localhost",
            "port": 5432,
            "database": "mmcat_server_querying",
            "password": "mmcat_password",
            "username": "mmcat_user"
        }
    }');

CREATE TABLE data_source (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO data_source (json_value)
VALUES
    ('{
        "url": "https://nosql.ms.mff.cuni.cz/mmcat/data-sources/test2.jsonld",
        "label": "Czech business registry",
        "type": "JsonLdStore"
    }');

CREATE TABLE logical_model (
    id SERIAL PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    database_id INTEGER NOT NULL REFERENCES database_for_mapping,
    json_value JSONB NOT NULL
);

INSERT INTO logical_model (schema_category_id, database_id, json_value)
VALUES
    (1, 1, '{"label": "Mongo - Order"}'),
    (1, 2, '{"label": "Postgres - Customer"}'),
    (2, 3, '{"label": "Postgres import"}'),
    (2, 1, '{"label": "Mongo export"}'),
    (3, 6, '{"label": "MongoDB"}'),
    (3, 7, '{"label": "PostgreSQL"}'),
    (3, 8, '{"label": "Neo4j"}');

CREATE TABLE mapping (
    id SERIAL PRIMARY KEY,
    logical_model_id INTEGER NOT NULL REFERENCES logical_model,
    -- root_object_id INTEGER NOT NULL REFERENCES schema_object,
    json_value JSONB NOT NULL
);

-- databázový systém může obsahovat více databázových instancí
    -- - v jedné db instanci musí být jména kindů atd unikátní

-- Property kindName is supposed to have the same value as the static name of the root property.
-- The reasons are that:
--      a) Sometimes we want to show only the label of the mapping, so we use the kindName for it without the necessity to access whole access path.
--      b) Some display components on the frontent use only the access path, so the information should be there.
INSERT INTO mapping (logical_model_id, json_value)
VALUES
    (1, '{
        "rootObjectKey": {"value": 4},
        "primaryKey": [[4], [1, 3, 2]],
        "kindName": "order",
        "accessPath": {
            "name": {"type": "STATIC", "value": "order"}, "subpaths": [
                {
                    "name": {"type": "STATIC", "value": "_id"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "customer"}, "signature": [1, 3, 2]},
                        {"name": {"type": "STATIC", "value": "number"}, "signature": [4]}
                    ], "signature": [], "isAuxiliary": true
                }, {
                    "name": {"type": "STATIC", "value": "contact"}, "subpaths": [
                        {"name": {"signature": [8, 7]}, "signature": [6]}
                    ], "signature": [5], "isAuxiliary": false
                }, {
                    "name": {"type": "STATIC", "value": "items"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "id"}, "signature": [12, 10]},
                        {"name": {"type": "STATIC", "value": "name"}, "signature": [13, 10]},
                        {"name": {"type": "STATIC", "value": "price"}, "signature": [14, 10]},
                        {"name": {"type": "STATIC", "value": "quantity"}, "signature": [11]}
                    ], "signature": [9], "isAuxiliary": false
                }
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (2, '{"rootObjectKey": {"value": 1}, "primaryKey": [[1]], "kindName": "customer",
        "accessPath": {
            "name": {"type": "STATIC", "value": "customer"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": [1]}
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (3, '{"rootObjectKey": {"value": 1}, "primaryKey": [[4]], "kindName": "app_customer",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_customer"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": [4]},
                {"name": {"type": "STATIC", "value": "full_name"}, "signature": [5]}
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (3, '{"rootObjectKey": {"value": 4}, "primaryKey": [[1]], "kindName": "app_contact",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_contact"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": [1]},
                {"name": {"type": "STATIC", "value": "value"}, "signature": [2]},
                {"name": {"type": "STATIC", "value": "type"}, "signature": [3]}
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (3, '{"rootObjectKey": {"value": 8}, "primaryKey": [[4, 7], [1, 6]], "kindName": "app_customer_contact",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_customer_contact"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "customer_id"}, "signature": [4, 7]},
                {"name": {"type": "STATIC", "value": "contact_id"}, "signature": [1, 6]}
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (3, '{"rootObjectKey": {"value": 9}, "primaryKey": [[13]], "kindName": "app_order",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_order"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": [13]},
                {"name": {"type": "STATIC", "value": "delivery_address"}, "signature": [12]},
                {"name": {"type": "STATIC", "value": "note"}, "signature": [11]},
                {"name": {"type": "STATIC", "value": "created"}, "signature": [8]},
                {"name": {"type": "STATIC", "value": "sent"}, "signature": [9]},
                {"name": {"type": "STATIC", "value": "paid"}, "signature": [10]},
                {"name": {"type": "STATIC", "value": "customer_id"}, "signature": [4, 14]}
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (3, '{"rootObjectKey": {"value": 16}, "primaryKey": [[15]], "kindName": "app_product",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_product"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": [15]},
                {"name": {"type": "STATIC", "value": "name"}, "signature": [16]},
                {"name": {"type": "STATIC", "value": "price"}, "signature": [17]}
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (3, '{"rootObjectKey": {"value": 20}, "primaryKey": [[13, 21], [15, 20]], "kindName": "app_order_item",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_order_item"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "order_id"}, "signature": [13, 21]},
                {"name": {"type": "STATIC", "value": "product_id"}, "signature": [15, 20]},
                {"name": {"type": "STATIC", "value": "amount"}, "signature": [19]},
                {"name": {"type": "STATIC", "value": "total_price"}, "signature": [18]}
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (4, '{"rootObjectKey": {"value": 9}, "primaryKey": [[13]], "kindName": "order",
        "accessPath": {
            "name": {"type": "STATIC", "value": "order"}, "subpaths": [
                {
                    "name": {"type": "STATIC", "value": "customer"}, "subpaths": [
                        {
                            "name": {"type": "STATIC", "value": "contact"}, "subpaths": [
                                {"name": {"signature": [3]}, "signature": [2]}
                            ], "signature": [6, -7], "isAuxiliary": false
                        },
                        {"name": {"type": "STATIC", "value": "name"}, "signature": [5]}
                    ], "signature": [14], "isAuxiliary": false
                },
                {"name": {"type": "STATIC", "value": "address"}, "signature": [12]},
                {"name": {"type": "STATIC", "value": "note"}, "signature": [11]}, {
                    "name": {"type": "STATIC", "value": "events"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "created"}, "signature": [8]},
                        {"name": {"type": "STATIC", "value": "sent"}, "signature": [9]},
                        {"name": {"type": "STATIC", "value": "paid"}, "signature": [10]}
                    ], "signature": [], "isAuxiliary": true
                }, {
                    "name": {"type": "STATIC", "value": "items"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "amount"}, "signature": [19]},
                        {"name": {"type": "STATIC", "value": "total_price"}, "signature": [18]},
                        {"name": {"type": "STATIC", "value": "name"}, "signature": [16, 20]}
                    ], "signature": [-21], "isAuxiliary": false
                }
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (5, '{"rootObjectKey": {"value": 10}, "primaryKey": [[11]], "kindName": "order",
        "accessPath": {
            "name": {"type": "STATIC", "value": "order"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "number"}, "signature": [11]}, {
                    "name": {"type": "STATIC", "value": "customers"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "id"}, "signature": [2]}
                    ], "signature": [9, -10], "isAuxiliary": false
                }, {
                    "name": {"type": "STATIC", "value": "items"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "id"}, "signature": [14, 13]},
                        {"name": {"type": "STATIC", "value": "name"}, "signature": [15, 13]},
                        {"name": {"type": "STATIC", "value": "price"}, "signature": [16, 13]}
                    ], "signature": [-12], "isAuxiliary": false
                }
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (6, '{"rootObjectKey": {"value": 1}, "primaryKey": [[2]], "kindName": "customer",
        "accessPath": {
            "name": {"type": "STATIC", "value": "customer"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": [2]},
                {"name": {"type": "STATIC", "value": "name"}, "signature": [1]},
                {"name": {"type": "STATIC", "value": "surname"}, "signature": [3]}
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (7, '{"rootObjectKey": {"value": 5}, "primaryKey": [[2, 4], [2, 5]], "kindName": "friends",
        "accessPath": {
            "name": {"type": "STATIC", "value": "friends"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "first_customer_id"}, "signature": [2, 4]},
                {"name": {"type": "STATIC", "value": "second_customer_id"}, "signature": [2, 5]}
            ], "signature": [], "isAuxiliary": true
        }}'
    ),
    (7, '{"rootObjectKey": {"value": 6}, "primaryKey": [[7], [8], [2, 6]], "kindName": "contact",
        "accessPath": {
            "name": {"type": "STATIC", "value": "contact"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "key"}, "signature": [7]},
                {"name": {"type": "STATIC", "value": "value"}, "signature": [8]},
                {"name": {"type": "STATIC", "value": "customer_id"}, "signature": [2, 6]}
            ], "signature": [], "isAuxiliary": true
        }}'
    );

CREATE TABLE job (
    id SERIAL PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    logical_model_id INTEGER REFERENCES logical_model,
    data_source_id INTEGER REFERENCES database_for_mapping, -- TODO make job to contain either logical_model_id or data_source_id
    json_value JSONB NOT NULL
    -- přidat typ jobu, vstup, výstup, vše serializované v jsonu
        -- podobně jako ukládání logování
        -- součástí log4j je nastavení kam se to dá ukládat, resp. do libovolné kombinace uložišť
            -- např. prometheus, zabbix, kibana - monitorování stavu aplikace

);

INSERT INTO job (schema_category_id, logical_model_id, data_source_id, json_value)
VALUES
    (1, 1, null, '{"label": "Import Order", "type": "ModelToCategory", "status": "Ready"}'),
    (1, 1, null, '{"label": "Export Order", "type": "CategoryToModel", "status": "Ready"}'),
    (1, 2, null, '{"label": "Import Customer", "type": "ModelToCategory", "status": "Ready"}'),
    (1, 2, null, '{"label": "Export Customer", "type": "CategoryToModel", "status": "Ready"}'),
    (2, 3, null, '{"label": "Import from Postgres", "type": "ModelToCategory", "status": "Ready"}'),
    (2, 4, null, '{"label": "Export to Mongo", "type": "CategoryToModel", "status": "Ready"}');
