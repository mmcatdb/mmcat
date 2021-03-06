/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.koupil
 */
public enum AbstractType {
	// -------------------------------| RELACNI MODEL      | DOKUMENTOVY MODEL  | MODEL KLIC HODNOTA | SLOUPCOVY MODEL    | GRAFOVY MODEL      | RDF MODEL          |
	KIND, // -------------------------| tabulka            | kolekce            | BUCKET?            | tabulka            | label              | ?                  |
	RECORD, // -----------------------| radek tabulky      | dokument           | dvojice            | radek tabulky      | vrchol / hrana     | RDF triple         |
	ARRAY, // ------------------------| vztah _:N          | vnorene complx arr | ?                  | vnorene complx arr | ------------------ | ?                  | # Pouze pole poli nebo pole vnorenych dokumentu (complex)
	INLINED, // ----------------------| # TODO? BUDEME POUZIVAT?
	ATTRIBUTE, // --------------------| 1-atrib. tabulka FK| property           | HODNOTA?           | column             | property           | object             | # Atribut, ktery je samostatne v tabulce a odkazuje se na hlavni tabulku pomoci FK
	MULTI_ATTRIBUTE, // --------------| vztah _:N atribut  | vnorene simple arr | ?                  | vnorene simple arr | vnorene pole       | mnozina objects    | # Atribut s kardinalitou _:N, tedy pole jednoduchych (simple) atributu
	INLINED_ATTRIBUTE, // ------------| column             | property           | HODNOTA?           | column             | property           | object             | # Atribut, ktery je primo soucasti hlavni tabulky
	STRUCTURED_ATTRIBUTE, // ---------| n-atrib. tabulka FK| vnoreny dokument   | ?                  | complex column     | ------------------ | {TRIPLES} + BLANK? |
	INLINED_STRUCTURED_ATTRIBUTE, // -| n-atrib. v tabulce | vnoreny dokument   | ?                  | complex column     | ------------------ | {TRIPLES} + BLANK? |
	IDENTIFIER, // -------------------| PK                 | identifier         | klic               | identifier         | identifier         | ?                  |
	MULTI_IDENTIFIER, // -------------| # TODO? BUDEME POUZIVAT?
	REFERENCE, // --------------------| FK                 | reference          | ?                  | ------------------ | ------------------ | ?                  |
	MULTI_REFERENCE	// ---------------| ?                  | vnorene refer arr  | ?                  | ------------------ | ------------------ | ?                  |

//	VALUE	// over, jestli je to spravne!
}
