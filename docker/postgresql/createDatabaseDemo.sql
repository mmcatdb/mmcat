DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS mapping;
DROP TABLE IF EXISTS logical_model;
DROP TABLE IF EXISTS data_source;
DROP TABLE IF EXISTS database_for_mapping;

-- DROP TABLE IF EXISTS schema_morphism_in_category;
-- DROP TABLE IF EXISTS schema_object_in_category;
-- DROP TABLE IF EXISTS schema_morphism;
-- DROP TABLE IF EXISTS schema_object;
DROP TABLE IF EXISTS schema_category_update;
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
        "version": "1",
        "objects": [
            {
                "key": {"value":  1},
                "data": {"ids": {"type": "Signatures",  "signatureIds": [["1"]]},                   "label": "Customer",  "superId": ["1"]},
                "metadata": {"position": {"x": -99, "y": -5}}
            },
            {
                "key": {"value":  2},
                "data": {"ids": {"type": "Value",       "signatureIds": [[]]},                      "label": "Id",        "superId": ["EMPTY"]},
                "metadata": {"position": {"x": -138, "y": 94}}
            },
            {
                "key": {"value":  3},
                "data": {"ids": {"type": "Signatures",  "signatureIds": [["2", "3;1"]]},            "label": "Order",     "superId": ["2", "3;1"]},
                "metadata": {"position": {"x": 134, "y": -85}}
            },
            {
                "key": {"value":  4},
                "data": {"ids": {"type": "Value",       "signatureIds": [[]]},                      "label": "Number",    "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 140, "y": -188}}
            },
            {
                "key": {"value":  5},
                "data": {"ids": {"type": "Signatures",  "signatureIds": [["4"]]},                   "label": "Product",   "superId": ["4"]},
                "metadata": {"position": {"x": 128, "y": 85}}
            },
            {
                "key": {"value":  6},
                "data": {"ids": {"type": "Value",       "signatureIds": [[]]},                      "label": "Id",        "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 47, "y": 189}}
            },
            {
                "key": {"value":  7},
                "data": {"ids": {"type": "Value",       "signatureIds": [[]]},                      "label": "Name",      "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 125, "y": 187}}
            },
            {
                "key": {"value":  8},
                "data": {"ids": {"type": "Value",       "signatureIds": [[]]},                      "label": "Price",     "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 213, "y": 189}}
            },
            {
                "key": {"value":  9},
                "data": {"ids": {"type": "Signatures",  "signatureIds": [["7;3;1", "7;2", "8;4"]]}, "label": "Items",     "superId": ["7;3;1", "7;2", "8;4"]},
                "metadata": {"position": {"x": 136, "y": -6}}
            },
            {
                "key": {"value": 10},
                "data": {"ids": {"type": "Value",       "signatureIds": [[]]},                      "label": "Quantity", "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 258, "y": -5}}
            },
            {
                "key": {"value": 11},
                "data": {"ids": {"type": "Signatures",  "signatureIds": [["11;12", "10"]]},         "label": "Contact",  "superId": ["11;12", "10"]},
                "metadata": {"position": {"x": 271, "y": -83}}
            },
            {
                "key": {"value": 12},
                "data": {"ids": {"type": "Value",       "signatureIds": [[]]},                      "label": "Value",    "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 273, "y": -190}}
            },
            {
                "key": {"value": 13},
                "data": {"ids": {"type": "Signatures",  "signatureIds": [["12"]]},                  "label": "Type",     "superId": ["12"]},
                "metadata": {"position": {"x": 394, "y": -86}}
            },
            {
                "key": {"value": 14},
                "data": {"ids": {"type": "Value",       "signatureIds": [[]]},                      "label": "Name",     "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 399, "y": -180}}
            },
            {
                "key": {"value": 15},
                "data": {"ids": {"type": "Signatures",  "signatureIds": [["15;1", "14;1"]]},        "label": "Friend",   "superId": ["15;1", "14;1"]},
                "metadata": {"position": {"x": -130, "y": -120}}
            },
            {
                "key": {"value": 16},
                "data": {"ids": {"type": "Value",       "signatureIds": [[]]},                      "label": "Since",    "superId": ["EMPTY"]},
                "metadata": {"position": {"x": -60, "y": -150}}
            }
        ],
        "morphisms": [
            {"min": "ONE",  "label": "",      "domKey": {"value":  1}, "codKey": {"value":  2}, "signature":  "1"},
            {"min": "ONE",  "label": "",      "domKey": {"value":  3}, "codKey": {"value":  4}, "signature":  "2"},
            {"min": "ONE",  "label": "",      "domKey": {"value":  3}, "codKey": {"value":  1}, "signature":  "3"},
            {"min": "ONE",  "label": "",      "domKey": {"value":  5}, "codKey": {"value":  6}, "signature":  "4"},
            {"min": "ZERO", "label": "",      "domKey": {"value":  5}, "codKey": {"value":  7}, "signature":  "5"},
            {"min": "ZERO", "label": "",      "domKey": {"value":  5}, "codKey": {"value":  8}, "signature":  "6"},
            {"min": "ONE",  "label": "#role", "domKey": {"value":  9}, "codKey": {"value":  3}, "signature":  "7"},
            {"min": "ONE",  "label": "#role", "domKey": {"value":  9}, "codKey": {"value":  5}, "signature":  "8"},
            {"min": "ONE",  "label": "",      "domKey": {"value":  9}, "codKey": {"value": 10}, "signature":  "9"},
            {"min": "ONE",  "label": "",      "domKey": {"value": 11}, "codKey": {"value": 12}, "signature": "10"},
            {"min": "ONE",  "label": "",      "domKey": {"value": 11}, "codKey": {"value": 13}, "signature": "11"},
            {"min": "ONE",  "label": "",      "domKey": {"value": 13}, "codKey": {"value": 14}, "signature": "12"},
            {"min": "ZERO", "label": "",      "domKey": {"value":  3}, "codKey": {"value": 11}, "signature": "13"},
            {"min": "ONE",  "label": "",      "domKey": {"value": 15}, "codKey": {"value":  1}, "signature": "14"},
            {"min": "ONE",  "label": "",      "domKey": {"value": 15}, "codKey": {"value":  1}, "signature": "15"},
            {"min": "ONE",  "label": "",      "domKey": {"value": 15}, "codKey": {"value": 16}, "signature": "16"}
        ]
    }'),
    ('{
        "label": "Tables to document",
        "version": "0",
        "objects": [
            {
                "key": {"value": 1},
                "data": {"label": "customer", "ids": {"type": "Signatures", "signatureIds": [["4"]]}, "superId": ["4"]},
                "metadata": {"position": {"x": -21, "y": 135}}
            },
            {
                "key": {"value": 2},
                "data": {"label": "full name", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": -94, "y": 287}}
            },
            {
                "key": {"value": 3},
                "data": {"label": "id", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 48, "y": 286}}
            },
            {
                "key": {"value": 4},
                "data": {"label": "contact", "ids": {"type": "Signatures", "signatureIds": [["1"]]}, "superId": ["1"]},
                "metadata": {"position": {"x": -250, "y": 109}}
            },
            {
                "key": {"value": 5},
                "data": {"label": "type", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": -415, "y": 54}}
            },
            {
                "key": {"value": 6},
                "data": {"label": "value", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": -403, "y": 152}}
            },
            {
                "key": {"value": 7},
                "data": {"label": "id", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": -344, "y": 233}}
            },
            {
                "key": {"value": 8},
                "data": {"label": "customer contact", "ids": {"type": "Signatures", "signatureIds": [["7;4", "6;1"]]}, "superId": ["7;4", "6;1"]},
                "metadata": {"position": {"x": -172, "y": 194}}
            },
            {
                "key": {"value": 9},
                "data": {"label": "order", "ids": {"type": "Signatures", "signatureIds": [["13"]]}, "superId": ["13"]},
                "metadata": {"position": {"x": 191, "y": 129}}
            },
            {
                "key": {"value": 10},
                "data": {"label": "created", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 334, "y": 232}}
            },
            {
                "key": {"value": 11},
                "data": {"label": "paid", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 175, "y": 286}}
            },
            {
                "key": {"value": 12},
                "data": {"label": "sent", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 269, "y": 295}}
            },
            {
                "key": {"value": 13},
                "data": {"label": "note", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 347, "y": 127}}
            },
            {
                "key": {"value": 14},
                "data": {"label": "delivery address", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 338, "y": 30}}
            },
            {
                "key": {"value": 15},
                "data": {"label": "id", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 251, "y": -23}}
            },
            {
                "key": {"value": 16},
                "data": {"label": "product", "ids": {"type": "Signatures", "signatureIds": [["15"]]}, "superId": ["15"]},
                "metadata": {"position": {"x": -79, "y": -64}}
            },
            {
                "key": {"value": 17},
                "data": {"label": "price", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": -187, "y": 5}}
            },
            {
                "key": {"value": 18},
                "data": {"label": "name", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": -221, "y": -96}}
            },
            {
                "key": {"value": 19},
                "data": {"label": "id", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": -148, "y": -177}}
            },
            {
                "key": {"value": 20},
                "data": {"label": "order item", "ids": {"type": "Signatures", "signatureIds": [["21;13", "20;15"]]}, "superId": ["21;13", "20;15"]},
                "metadata": {"position": {"x": 94, "y": -22}}
            },
            {
                "key": {"value": 21},
                "data": {"label": "amount", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 49, "y": -150}}
            },
            {
                "key": {"value": 22},
                "data": {"label": "total price", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 166, "y": -145}}
            }
        ],
        "morphisms": [
            {"domKey": {"value": 4}, "codKey": {"value": 7}, "min": "ONE", "signature": "1"},
            {"domKey": {"value": 4}, "codKey": {"value": 6}, "min": "ONE", "signature": "2"},
            {"domKey": {"value": 4}, "codKey": {"value": 5}, "min": "ONE", "signature": "3"},
            {"domKey": {"value": 1}, "codKey": {"value": 3}, "min": "ONE", "signature": "4"},
            {"domKey": {"value": 1}, "codKey": {"value": 2}, "min": "ONE", "signature": "5"},
            {"domKey": {"value": 8}, "codKey": {"value": 4}, "min": "ONE", "signature": "6"},
            {"domKey": {"value": 8}, "codKey": {"value": 1}, "min": "ONE", "signature": "7"},
            {"domKey": {"value": 9}, "codKey": {"value": 10}, "min": "ZERO", "signature": "8"},
            {"domKey": {"value": 9}, "codKey": {"value": 12}, "min": "ZERO", "signature": "9"},
            {"domKey": {"value": 9}, "codKey": {"value": 11}, "min": "ZERO", "signature": "10"},
            {"domKey": {"value": 9}, "codKey": {"value": 13}, "min": "ONE", "signature": "11"},
            {"domKey": {"value": 9}, "codKey": {"value": 14}, "min": "ONE", "signature": "12"},
            {"domKey": {"value": 9}, "codKey": {"value": 15}, "min": "ONE", "signature": "13"},
            {"domKey": {"value": 9}, "codKey": {"value": 1}, "min": "ONE", "signature": "14"},
            {"domKey": {"value": 16}, "codKey": {"value": 19}, "min": "ONE", "signature": "15"},
            {"domKey": {"value": 16}, "codKey": {"value": 18}, "min": "ONE", "signature": "16"},
            {"domKey": {"value": 16}, "codKey": {"value": 17}, "min": "ONE", "signature": "17"},
            {"domKey": {"value": 20}, "codKey": {"value": 22}, "min": "ONE", "signature": "18"},
            {"domKey": {"value": 20}, "codKey": {"value": 21}, "min": "ONE", "signature": "19"},
            {"domKey": {"value": 20}, "codKey": {"value": 16}, "min": "ONE", "signature": "20"},
            {"domKey": {"value": 20}, "codKey": {"value": 9}, "min": "ONE", "signature": "21"}
        ]
    }'),
    ('{
        "label": "Querying example",
        "version": "0",
        "objects": [
            {
                "key": {"value": 1},
                "data": {"label": "Customer", "ids": {"type": "Signatures", "signatureIds": [["2"]]}, "superId": ["2"]},
                "metadata": {"position": {"x": 561, "y": 415}}
            },
            {
                "key": {"value": 2},
                "data": {"label": "Name", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 556, "y": 532}}
            },
            {
                "key": {"value": 3},
                "data": {"label": "Id", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 552, "y": 178}}
            },
            {
                "key": {"value": 4},
                "data": {"label": "Surname", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 717, "y": 405}}
            },
            {
                "key": {"value": 5},
                "data": {"label": "Friends", "ids": {"type": "Signatures", "signatureIds": [["4;2", "5;2"]]}, "superId": ["4;2", "5;2"]},
                "metadata": {"position": {"x": 722, "y": 525}}
            },
            {
                "key": {"value": 6},
                "data": {"label": "Contact", "ids": {"type": "Signatures", "signatureIds": [["7", "8", "6;2"]]}, "superId": ["7", "8", "6;2"]},
                "metadata": {"position": {"x": 293, "y": 307}}
            },
            {
                "key": {"value": 7},
                "data": {"label": "Key", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 147, "y": 308}}
            },
            {
                "key": {"value": 8},
                "data": {"label": "Value", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 294, "y": 190}}
            },
            {
                "key": {"value": 9},
                "data": {"label": "Orders", "ids": {"type": "Signatures", "signatureIds": [["9;2", "10;11"]]}, "superId": ["9;2", "10;11"]},
                "metadata": {"position": {"x": 141, "y": 404}}
            },
            {
                "key": {"value": 10},
                "data": {"label": "Order", "ids": {"type": "Signatures", "signatureIds": [["11"]]}, "superId": ["11"]},
                "metadata": {"position": {"x": 155, "y": 206}}
            },
            {
                "key": {"value": 11},
                "data": {"label": "Id", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 189, "y": 520}}
            },
            {
                "key": {"value": 12},
                "data": {"label": "Items", "ids": {"type": "Signatures", "signatureIds": [["12;11", "13;14"]]}, "superId": ["12;11", "13;14"]},
                "metadata": {"position": {"x": 189, "y": 656}}
            },
            {
                "key": {"value": 13},
                "data": {"label": "Product", "ids": {"type": "Signatures", "signatureIds": [["14"]]}, "superId": ["14"]},
                "metadata": {"position": {"x": 300, "y": 657}}
            },
            {
                "key": {"value": 14},
                "data": {"label": "Number", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 433, "y": 304}}
            },
            {
                "key": {"value": 15},
                "data": {"label": "Name", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 559, "y": 299}}
            },
            {
                "key": {"value": 16},
                "data": {"label": "Price", "ids": {"type": "Value", "signatureIds": [[]]}, "superId": ["EMPTY"]},
                "metadata": {"position": {"x": 439, "y": 533}}
            }
        ],
        "morphisms": [
            {"domKey": {"value": 1}, "codKey": {"value": 2}, "min": "ONE", "label": "", "signature": "1"},
            {"domKey": {"value": 1}, "codKey": {"value": 3}, "min": "ONE", "label": "", "signature": "2"},
            {"domKey": {"value": 1}, "codKey": {"value": 4}, "min": "ONE", "label": "", "signature": "3"},
            {"domKey": {"value": 5}, "codKey": {"value": 1}, "min": "ONE", "label": "", "signature": "4"},
            {"domKey": {"value": 5}, "codKey": {"value": 1}, "min": "ONE", "label": "", "signature": "5"},
            {"domKey": {"value": 6}, "codKey": {"value": 1}, "min": "ONE", "label": "", "signature": "6"},
            {"domKey": {"value": 6}, "codKey": {"value": 7}, "min": "ONE", "label": "", "signature": "7"},
            {"domKey": {"value": 6}, "codKey": {"value": 8}, "min": "ONE", "label": "", "signature": "8"},
            {"domKey": {"value": 9}, "codKey": {"value": 1}, "min": "ONE", "label": "", "signature": "9"},
            {"domKey": {"value": 9}, "codKey": {"value": 10}, "min": "ONE", "label": "", "signature": "10"},
            {"domKey": {"value": 10}, "codKey": {"value": 14}, "min": "ONE", "label": "", "signature": "11"},
            {"domKey": {"value": 12}, "codKey": {"value": 10}, "min": "ONE", "label": "", "signature": "12"},
            {"domKey": {"value": 12}, "codKey": {"value": 13}, "min": "ONE", "label": "", "signature": "13"},
            {"domKey": {"value": 13}, "codKey": {"value": 11}, "min": "ONE", "label": "", "signature": "14"},
            {"domKey": {"value": 13}, "codKey": {"value": 15}, "min": "ONE", "label": "", "signature": "15"},
            {"domKey": {"value": 13}, "codKey": {"value": 16}, "min": "ONE", "label": "", "signature": "16"}
        ]
    }');

CREATE TABLE schema_category_update (
    id SERIAL PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL
);

INSERT INTO "schema_category_update" ("schema_category_id", "json_value")
VALUES
    (1, '{"operations": [
        {"smo": {"type": "createObject", "key": {"value": 1}, "object": {
            "ids": {"type": "Signatures", "signatureIds": [["1"]]},
            "label": "Customer",
            "superId": ["1"]
        }}, "version": "0:1"},
        {"smo": {"type": "createObject", "key": {"value": 2}, "object": {
            "ids": {"type": "Value", "signatureIds": [[]]},
            "label": "Id",
            "superId": ["EMPTY"]
        }}, "version": "0:2"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 1},
            "codKey": {"value": 2},
            "signature": "1"
        }}, "version": "0:3"}
    ], "nextVersion": "1", "prevVersion": "0"}'),
    (1, '{"operations": [
        {"smo": {"type": "createObject", "key": {"value": 3}, "object": {
            "ids": {"type": "Signatures", "signatureIds": [["2", "3;1"]]},
            "label": "Order",
            "superId": ["2", "3;1"]
        }}, "version": "0:4"},
        {"smo": {"type": "createObject", "key": {"value": 4}, "object": {
            "ids": {"type": "Value", "signatureIds": [[]]},
            "label": "Number",
            "superId": ["EMPTY"]
        }}, "version": "0:5"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 3},
            "codKey": {"value": 4},
            "signature": "2"
        }}, "version": "0:6"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 3},
            "codKey": {"value": 1},
            "signature": "3"
        }}, "version": "0:7"},
        {"smo": {"type": "createObject", "key": {"value": 5}, "object": {
            "ids": {"type": "Signatures", "signatureIds": [["4"]]},
            "label": "Product",
            "superId": ["4"]
        }}, "version": "0:8"},
        {"smo": {"type": "createObject", "key": {"value": 6}, "object": {
            "ids": {"type": "Value", "signatureIds": [[]]},
            "label": "Id",
            "superId": ["EMPTY"]
        }}, "version": "0:9"},
        {"smo": {"type": "createObject", "key": {"value": 7}, "object": {
            "ids": {"type": "Value", "signatureIds": [[]]},
            "label": "Name",
            "superId": ["EMPTY"]
        }}, "version": "0:10"},
        {"smo": {"type": "createObject", "key": {"value": 8}, "object": {
            "ids": {"type": "Value", "signatureIds": [[]]},
            "label": "Price",
            "superId": ["EMPTY"]
        }}, "version": "0:11"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 5},
            "codKey": {"value": 6},
            "signature": "4"
        }}, "version": "0:12"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ZERO",
            "label": "",
            "domKey": {"value": 5},
            "codKey": {"value": 7},
            "signature": "5"
        }}, "version": "0:13"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ZERO",
            "label": "",
            "domKey": {"value": 5},
            "codKey": {"value": 8},
            "signature": "6"
        }}, "version": "0:14"},
        {"smo": {"type": "createObject", "key": {"value": 9}, "object": {
            "ids": {"type": "Signatures", "signatureIds": [["7;3;1", "7;2", "8;4"]]},
            "label": "Items",
            "superId": ["7;3;1", "7;2", "8;4"]
        }}, "version": "0:14.1"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "#role",
            "domKey": {"value": 9},
            "codKey": {"value": 3},
            "signature": "7"
        }}, "version": "0:14.2"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "#role",
            "domKey": {"value": 9},
            "codKey": {"value": 5},
            "signature": "8"
        }}, "version": "0:14.3"},
        {"smo": {"name": "addSet", "type": "composite"}, "version": "0:15"},
        {"smo": {"type": "createObject", "key": {"value": 10}, "object": {
            "ids": {"type": "Value", "signatureIds": [[]]},
            "label": "Quantity",
            "superId": ["EMPTY"]
        }}, "version": "0:15.1"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 9},
            "codKey": {"value": 10},
            "signature": "9"
        }}, "version": "0:15.2"},
        {"smo": {"name": "addProperty", "type": "composite"}, "version": "0:16"}
    ], "nextVersion": "2", "prevVersion": "1"}'),
    (1, '{"operations": [
        {"smo": {"type": "createObject", "key": {"value": 11}, "object": {
            "ids": {"type": "Signatures", "signatureIds": [["11;12", "10"]]},
            "label": "Contact",
            "superId": ["11;12", "10"]
        }}, "version": "0:17"},
        {"smo": {"type": "createObject", "key": {"value": 12}, "object": {
            "ids": {"type": "Value", "signatureIds": [[]]},
            "label": "Value",
            "superId": ["EMPTY"]
        }}, "version": "0:17.1"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 11},
            "codKey": {"value": 12},
            "signature": "10"
        }}, "version": "0:17.2"},
        {"smo": {"name": "addProperty", "type": "composite"}, "version": "0:18"},
        {"smo": {"type": "createObject", "key": {"value": 13}, "object": {
            "ids": {"type": "Signatures", "signatureIds": [["12"]]},
            "label": "Type",
            "superId": ["12"]
        }}, "version": "0:18.1"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 11},
            "codKey": {"value": 13},
            "signature": "11"
        }}, "version": "0:18.2"},
        {"smo": {"name": "addProperty", "type": "composite"}, "version": "0:19"},
        {"smo": {"type": "createObject", "key": {"value": 14}, "object": {
            "ids": {"type": "Value", "signatureIds": [[]]},
            "label": "Name",
            "superId": ["EMPTY"]
        }}, "version": "0:19.1"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 13},
            "codKey": {"value": 14},
            "signature": "12"
        }}, "version": "0:19.2"},
        {"smo": {"name": "addProperty", "type": "composite"}, "version": "0:20"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ZERO",
            "label": "",
            "domKey": {"value": 3},
            "codKey": {"value": 11},
            "signature": "13"
        }}, "version": "0:21"}
    ], "nextVersion": "3", "prevVersion": "2"}'),
    (1, '{"operations": [
        {"smo": {"type": "createObject", "key": {"value": 15}, "object": {
            "ids": {"type": "Signatures", "signatureIds": [["15;1", "14;1"]]},
            "label": "Friend",
            "superId": ["15;1", "14;1"]
        }}, "version": "0:22"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 15},
            "codKey": {"value": 1},
            "signature": "14"
        }}, "version": "0:23"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 15},
            "codKey": {"value": 1},
            "signature": "15"
        }}, "version": "0:24"},
        {"smo": {"type": "createObject", "key": {"value": 16}, "object": {
            "ids": {"type": "Value", "signatureIds": [[]]},
            "label": "Since",
            "superId": ["EMPTY"]
        }}, "version": "0:24.1"},
        {"smo": {"type": "createMorphism", "morphism": {
            "min": "ONE",
            "label": "",
            "domKey": {"value": 15},
            "codKey": {"value": 16},
            "signature": "16"
        }}, "version": "0:24.2"},
        {"smo": {"name": "addProperty", "type": "composite"}, "version": "0:25"}
    ], "nextVersion": "4", "prevVersion": "3"}');

CREATE TABLE database_for_mapping (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO database_for_mapping (json_value)
VALUES
    ('{ "type": "mongodb", "label": "MongoDB - Basic",
        "settings": {
            "host": "localhost",
            "port": "27017",
            "database": "mm_example_basic",
            "authenticationDatabase": "admin",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL - Basic",
        "settings": {
            "host": "localhost",
            "port": "5432",
            "database": "mm_example_basic",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "neo4j", "label": "Neo4j - Basic",
        "settings": {
            "host": "localhost",
            "port": "7687",
            "database": "neo4j",
            "username": "neo4j",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL - TTD",
        "settings": {
            "host": "localhost",
            "port": "5432",
            "database": "mm_example_ttd",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "mongodb", "label": "MongoDB - Query",
        "settings": {
            "host": "localhost",
            "port": "27017",
            "database": "mm_example_query",
            "username": "mmcat_user",
            "password": "mmcat_password",
            "authenticationDatabase": "admin"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL - Query",
        "settings": {
            "host": "localhost",
            "port": "5432",
            "database": "mm_example_query",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "neo4j", "label": "Neo4j - Query",
        "settings": {
            "host": "localhost",
            "port": "7687",
            "database": "neo4j",
            "username": "neo4j",
            "password": "mmcat_password"
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
    (1, 3, '{"label": "Neo4j - Friend"}'),
    (2, 4, '{"label": "Postgres import"}'),
    (2, 1, '{"label": "Mongo export"}'),
    (3, 5, '{"label": "MongoDB"}'),
    (3, 6, '{"label": "PostgreSQL"}'),
    (3, 7, '{"label": "Neo4j"}');

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
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 3},
        "primaryKey": ["2", "3;1"],
        "kindName": "order",
        "accessPath": {
            "name": {"type": "STATIC", "value": "order"}, "subpaths": [
                {
                    "name": {"type": "STATIC", "value": "_id"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "customer"}, "signature": "3;1"},
                        {"name": {"type": "STATIC", "value": "number"}, "signature": "2"}
                    ], "signature": "EMPTY", "isAuxiliary": true
                },
                {
                    "name": {"type": "STATIC", "value": "contact"}, "subpaths": [
                        {"name": {"signature": "11;12"}, "signature": "10"}
                    ], "signature": "13", "isAuxiliary": false
                },
                {
                    "name": {"type": "STATIC", "value": "items"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "id"}, "signature": "8;4"},
                        {"name": {"type": "STATIC", "value": "name"}, "signature": "8;5"},
                        {"name": {"type": "STATIC", "value": "price"}, "signature": "8;6"},
                        {"name": {"type": "STATIC", "value": "quantity"}, "signature": "9"}
                    ], "signature": "-7", "isAuxiliary": false
                }
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (2, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 1},
        "primaryKey": ["1"],
        "kindName": "customer",
        "accessPath": {
            "name": {"type": "STATIC", "value": "customer"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": "1"}
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (3, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 15},
        "primaryKey": [],
        "kindName": "Friend",
        "accessPath": {
            "name": {"type": "STATIC", "value": "friend"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "since"}, "signature": "16"},
                {
                    "name": {"type": "STATIC", "value": "_from.Customer"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "customer_id"}, "signature": "1"}
                    ], "signature": "15", "isAuxiliary": false
                },
                {
                    "name": {"type": "STATIC", "value": "_to.Customer"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "customer_id"}, "signature": "1"}
                    ], "signature": "14", "isAuxiliary": false
                }
            ], "signature": "EMPTY", "isAuxiliary": true}
        }'
    ),
    (4, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 1},
        "primaryKey": ["4"],
        "kindName": "app_customer",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_customer"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": "4"},
                {"name": {"type": "STATIC", "value": "full_name"}, "signature": "5"}
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (4, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 4},
        "primaryKey": ["1"],
        "kindName": "app_contact",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_contact"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": "1"},
                {"name": {"type": "STATIC", "value": "value"}, "signature": "2"},
                {"name": {"type": "STATIC", "value": "type"}, "signature": "3"}
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (4, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 8},
        "primaryKey": ["7;4", "6;1"],
        "kindName": "app_customer_contact",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_customer_contact"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "customer_id"}, "signature": "7;4"},
                {"name": {"type": "STATIC", "value": "contact_id"}, "signature": "6;1"}
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (4, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 9},
        "primaryKey": ["13"],
        "kindName": "app_order",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_order"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": "13"},
                {"name": {"type": "STATIC", "value": "delivery_address"}, "signature": "12"},
                {"name": {"type": "STATIC", "value": "note"}, "signature": "11"},
                {"name": {"type": "STATIC", "value": "created"}, "signature": "8"},
                {"name": {"type": "STATIC", "value": "sent"}, "signature": "9"},
                {"name": {"type": "STATIC", "value": "paid"}, "signature": "10"},
                {"name": {"type": "STATIC", "value": "customer_id"}, "signature": "14;4"}
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (4, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 16},
        "primaryKey": ["15"],
        "kindName": "app_product",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_product"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": "15"},
                {"name": {"type": "STATIC", "value": "name"}, "signature": "16"},
                {"name": {"type": "STATIC", "value": "price"}, "signature": "17"}
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (4, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 20},
        "primaryKey": ["21;13", "20;15"],
        "kindName": "app_order_item",
        "accessPath": {
            "name": {"type": "STATIC", "value": "app_order_item"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "order_id"}, "signature": "21;13"},
                {"name": {"type": "STATIC", "value": "product_id"}, "signature": "20;15"},
                {"name": {"type": "STATIC", "value": "amount"}, "signature": "19"},
                {"name": {"type": "STATIC", "value": "total_price"}, "signature": "18"}
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (5, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 9},
        "primaryKey": ["13"],
        "kindName": "order",
        "accessPath": {
            "name": {"type": "STATIC", "value": "order"}, "subpaths": [
                {
                    "name": {"type": "STATIC", "value": "customer"}, "subpaths": [
                        {
                            "name": {"type": "STATIC", "value": "contact"}, "subpaths": [
                                {"name": {"signature": "3"}, "signature": "2"}
                            ], "signature": "-7;6", "isAuxiliary": false
                        },
                        {"name": {"type": "STATIC", "value": "name"}, "signature": "5"}
                    ], "signature": "14", "isAuxiliary": false
                },
                {"name": {"type": "STATIC", "value": "address"}, "signature": "12"},
                {"name": {"type": "STATIC", "value": "note"}, "signature": "11"},
                {
                    "name": {"type": "STATIC", "value": "events"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "created"}, "signature": "8"},
                        {"name": {"type": "STATIC", "value": "sent"}, "signature": "9"},
                        {"name": {"type": "STATIC", "value": "paid"}, "signature": "10"}
                    ], "signature": "EMPTY", "isAuxiliary": true
                },
                {
                    "name": {"type": "STATIC", "value": "items"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "amount"}, "signature": "19"},
                        {"name": {"type": "STATIC", "value": "total_price"}, "signature": "18"},
                        {"name": {"type": "STATIC", "value": "name"}, "signature": "20;16"}
                    ], "signature": "-21", "isAuxiliary": false
                }
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (6, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 10},
        "primaryKey": ["11"],
        "kindName": "order",
        "accessPath": {
            "name": {"type": "STATIC", "value": "order"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "number"}, "signature": "11"},
                {
                    "name": {"type": "STATIC", "value": "customers"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "id"}, "signature": "2"}
                    ], "signature": "-10;9", "isAuxiliary": false
                },
                {
                    "name": {"type": "STATIC", "value": "items"}, "subpaths": [
                        {"name": {"type": "STATIC", "value": "id"}, "signature": "13;14"},
                        {"name": {"type": "STATIC", "value": "name"}, "signature": "13;15"},
                        {"name": {"type": "STATIC", "value": "price"}, "signature": "13;16"}
                    ], "signature": "-12", "isAuxiliary": false
                }
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (7, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 1},
        "primaryKey": ["2"],
        "kindName": "customer",
        "accessPath": {
            "name": {"type": "STATIC", "value": "customer"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "id"}, "signature": "2"},
                {"name": {"type": "STATIC", "value": "name"}, "signature": "1"},
                {"name": {"type": "STATIC", "value": "surname"}, "signature": "3"}
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (8, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 5},
        "primaryKey": ["4;2", "5;2"],
        "kindName": "friends",
        "accessPath": {
            "name": {"type": "STATIC", "value": "friends"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "first_customer_id"}, "signature": "4;2"},
                {"name": {"type": "STATIC", "value": "second_customer_id"}, "signature": "5;2"}
            ], "signature": "EMPTY", "isAuxiliary": true
        }}'
    ),
    (8, '{
        "version": "0",
        "categoryVersion": "0",
        "rootObjectKey": {"value": 6},
        "primaryKey": ["7", "8", "6;2"],
        "kindName": "contact",
        "accessPath": {
            "name": {"type": "STATIC", "value": "contact"}, "subpaths": [
                {"name": {"type": "STATIC", "value": "key"}, "signature": "7"},
                {"name": {"type": "STATIC", "value": "value"}, "signature": "8"},
                {"name": {"type": "STATIC", "value": "customer_id"}, "signature": "6;2"}
            ], "signature": "EMPTY", "isAuxiliary": true
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
    (1, 1, null, '{"label": "Import Order", "type": "ModelToCategory", "state": "Ready"}'),
    (1, 1, null, '{"label": "Export Order", "type": "CategoryToModel", "state": "Ready"}'),
    (1, 2, null, '{"label": "Import Customer", "type": "ModelToCategory", "state": "Ready"}'),
    (1, 2, null, '{"label": "Export Customer", "type": "CategoryToModel", "state": "Ready"}'),
    (1, 3, null, '{"label": "Import Friend", "type": "ModelToCategory", "state": "Ready"}'),
    (1, 3, null, '{"label": "Export Friend", "type": "CategoryToModel", "state": "Ready"}'),
    (2, 4, null, '{"label": "Import from Postgres", "type": "ModelToCategory", "state": "Ready"}'),
    (2, 5, null, '{"label": "Export to Mongo", "type": "CategoryToModel", "state": "Ready"}');
