/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2;

import cat.transformations.algorithms2.schema.AbstractSchema;
import cat.transformations.algorithms2.model.AbstractCategoricalMorphism;
import cat.transformations.algorithms2.model.AbstractCategoricalObject;
import cat.transformations.algorithms2.model.AbstractModel;
import cat.transformations.algorithms2.model.AbstractInstance;
import cat.transformations.algorithms2.model.AbstractObjectType;
import cat.transformations.algorithms2.model.AbstractRecordProperty;
import cat.transformations.algorithms2.model.Cardinality;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavel.contos
 */
public class TransformationInstToModel {

	private static final Logger LOGGER = Logger.getLogger(TransformationInstToModel.class.getName());
//	LOGGER.log(Level.INFO, String.format("text", args));

	private final AbstractInstance instance;
	private final AbstractModel model;
	private final AbstractSchema schema;

	public TransformationInstToModel(AbstractInstance instance, AbstractModel model) {
		this.instance = instance;
		this.model = model;

		if (model.isSchemaRequired()) {
			schema = model.getSchema();
			System.out.println("POTREBUJES SCHEMA - POSTAVIT TABULKY PRED NAPLNENIM DATY");
		} else {
			// schema first, data later   or data first, schema later pristupy rozlisuj
			schema = null;
		}
	}

	// tenhle algoritmus musi mit 2 casti - nejprve postaveni schematu, tzn. tabulek, az potom naplneni daty
	public void processData() {
	}

	public void processSchema() {

		for (String name : instance.objectsKeySet(AbstractObjectType.KIND)) {
			// MEL BYS SPRAVNE VYTAHNOUT PRVNI HODNOTU A DELAT TO PRO NI - VKLADANI DAT!
			// ODVOZENI SCHEMATU JE JINA VEC - TAM NEZALEZI NA HODNOTACH, ALE JAK ODVODIS DATOVE TYPY? METADATA?
			Set<String> visited = new TreeSet<>();

//			AbstractCategoricalObject object = instance.get(name);
//			if (!object.getType().equals(AbstractObjectType.KIND)) {
//				LOGGER.log(Level.INFO, String.format("SKIP: Object %s je typu %s", object.getName(), object.getType()));
//				continue;
//			}
//			LOGGER.log(Level.INFO, String.format("OK:   Object %s je typu %s", object.getName(), object.getType()));
			// WARN: POKRACUJEME JEN S KIND! V PRIPADE RELACNI TABULKY SE MUZE STAT, ZE VNORENY KIND NEBUDE ZPRACOVAN - TAKZE NEKDE OZNACUJ, CO MAS HOTOVO - NEHOTOVE POTOM ZPRACUJ POZDEJI
			visited.add(name);

			schema.createKind(name);

			// tady musis vytvorit slozitejsi fronty, bude v nich ukladano vice veci...
			// pohybuj se pruchodem do sirky, ale pouze smerem dolu - nechces zpetny chod!
			// QUEUE
			// QUEUE...
			Queue<AbstractCategoricalMorphism> queue = new LinkedList<>();	// add, poll
			Queue<String> queueOfNames = new LinkedList<>();

			for (String morphismName : instance.morphismsKeySet(name)) {
				AbstractCategoricalMorphism morphism = instance.getMorphism(morphismName);
				AbstractCategoricalObject domain = morphism.getDomain();
				AbstractCategoricalObject codomain = morphism.getCodomain();

				if (!domain.getName().equals(name)) {
					LOGGER.log(Level.INFO, String.format("\tSKIP: Morfism %s", morphism.getName()));
					// Zajimaji te pouze morfismy, ktere vedou z NAME - ostatni zahod
					// TODO: implementuj lepe instancni a schematickou kategorii - mel bys mit moznost rovnou vratit vsechny morfismy pro danou domain nebo codomain
					// mozna by si kazdy objekt mel pamatovat, do kterych morfismu vstupuje a ze kterych vychazi
					continue;
				}

				queue.add(morphism);

				while (!queue.isEmpty()) {
					morphism = queue.poll();

					LOGGER.log(Level.INFO, String.format("\tOK:   Morfism %s", morphism.getName()));

					if (visited.contains(codomain.getName())) {
						// Bud uz byl uzel zpracovan, nebo se jedna o rodice - musis overit
						if (codomain.getName().equals(name)) {
							// pokud se jedna o rodice, tak jen reference je povolena
							System.out.println("JEDNA SE O RODICE - V PORADKU, POKRACUJ");
							System.out.println("ALE MOZNA BUDE POTREBA ZPRACOVAT RODICE TADY!");
							continue;	// WARN: TADY BUDE MOZNE JEN NECO...
						} else {
							// pokud se nejedna o rodice, tak uz bylo zpracovano - preskoc
							continue;
						}
					}

					// jedna se o rodice, v poradku
					System.out.println("KDY VKLADAT JMENA DO FRONTY? CO KDYZ TED TO NENI JEDNODUCHY ATRIBUT?");
					visited.add(codomain.getName()); // WARN: opravdu vkladat ted? co kdyz je to neatributovy typ? pak bys ho mel vlozit do fronty a oznacit jako zpracovany az pozdeji

					switch (codomain.getType()) {
						case KIND ->
							System.out.println("NEMUZE NASTAT, KIND NEMUZE BYT VE VZTAHU S JINYM KIND");

						case NESTED_KIND ->
							System.out.println("NEMUZE NASTAT, POUZE V PRIPADE ARRAY");

						case ARRAY -> {

							// na array je treba reagovat
							schema.createArray(codomain.getName());
							// TODO: CREATE ARRAY FK, CREATE ARRAY "PK"

							// musis najit potomka array, tedy array.items
							// pak zavolat schema.addRelationship(entityA, relationship, entityB, cardinality);
							// v tomto pripade bys volal schema.addRelationship(entityA, array, array.items, one/many_to_many);
							System.out.println("ANO");	// zavolej processArray, bude to zaviset na modelu, takze modelove process array, ale co kdyz to bude mit vnoreny dokument? ... tak jednoduse to nejde
							// V TOM PRIPADE ALE ROVNOU MUSIS ZAVOLAT PROCESS ATRIBUTY ARRAY, KDYBY NAHODOU NEJAKE BYLY
							// A DO FRONTY HODIT ENTITY_B

							for (String RENAME : instance.morphismsKeySet(codomain.getName())) {
								// TADY MAS VSECHNY MORFISMY, KTERE VEDOU Z ARRAY ZPET!
								// A TED MUZES RESIT GETTYPE
								AbstractCategoricalMorphism morphismRENAME = instance.getMorphism(RENAME);
								if (morphismRENAME.getCodomainName().equals(morphism.getDomainName())) {
									System.out.println(morphismRENAME.getName() + " ::::: " + morphism.getDomainName() + " EQUALS TO " + morphismRENAME.getCodomainName() + " -> SKIP");
									continue;
								}
								System.out.println("PRACUJ S: " + morphismRENAME.getName());
								AbstractCategoricalObject x = instance.get(morphismRENAME.getCodomainName());

								switch (x.getType()) {
									case ARRAY -> {
										System.out.println("ARRAY NASTALO");
									}
									case NESTED_KIND -> {
										System.out.println("NESTED_KIND NASTALO");
									}
									case RELATIONSHIP_ATTRIBUTE -> {
//								System.out.println("MUZE TAHLE SITUACE TADY NASTAT?");
										// KDYZ JSOU NA TOMHLE MISTE, PAK MUSI JIT VZTAHU, KTERY PREDCHAZEL
										// tyhle relationship atributy vzdycky dostane do hloubky prochazena entita, protoze parent by jich pak musel mit mnoho a neslo by urcit, ke komu patri...
										schema.createAttribute(name, codomain.getName(), x.getName(), x.getDataType(), Cardinality.ONE_TO_ONE);// ZPRACUJ NA MISTE!
									}
									case RELATIONSHIP_MULTI_ATTRIBUTE -> {
//								System.out.println("MUZE TAHLE SITUACE TADY NASTAT?");
										// KDYZ JSOU NA TOMHLE MISTE, PAK MUSI JIT VZTAHU, KTERY PREDCHAZEL
										// tyhle relationship atributy vzdycky dostane do hloubky prochazena entita, protoze parent by jich pak musel mit mnoho a neslo by urcit, ke komu patri...
										schema.createAttribute(name, codomain.getName(), x.getName(), x.getDataType(), Cardinality.ONE_TO_MANY);//ROZDIL U AGREGATU/GRAFU/TABULKY
									}
									case RELATIONSHIP_INLINED_ATTRIBUTE -> {
//								System.out.println("MUZE TAHLE SITUACE TADY NASTAT?");
										// KDYZ JSOU NA TOMHLE MISTE, PAK MUSI JIT VZTAHU, KTERY PREDCHAZEL
										// tyhle relationship atributy vzdycky dostane do hloubky prochazena entita, protoze parent by jich pak musel mit mnoho a neslo by urcit, ke komu patri...
										schema.createAttribute(name, codomain.getName(), x.getName(), Cardinality.ONE_TO_ONE);// ZPRACUJ NA MISTE!
									}
									case RELATIONSHIP_STRUCTURED_ATTRIBUTE -> {
//								System.out.println("MUZE TAHLE SITUACE TADY NASTAT?");
										// KDYZ JSOU NA TOMHLE MISTE, PAK MUSI JIT VZTAHU, KTERY PREDCHAZEL
										// tyhle relationship atributy vzdycky dostane do hloubky prochazena entita, protoze parent by jich pak musel mit mnoho a neslo by urcit, ke komu patri...
										schema.createStructuredAttribute(name, codomain.getName(), x.getName()); // NESTED DOKUMENT, NESTED TABULKA, JAK MOC MA BYT TAKOVY STURKTUROVANY ATRIBUT ZANOROVANY?
										System.out.println("TODO: ADD X TO QUEUE");// TADY ZAROVEN VLOZ DO FRONTY TEN OBJEKT, MUSI Se ZPRACOVAT V DALSIM KROCE!
									}
									case RELATIONSHIP_INLINED_STRUCTURED_ATTRIBUTE -> {
//								System.out.println("MUZE TAHLE SITUACE TADY NASTAT?");
										// KDYZ JSOU NA TOMHLE MISTE, PAK MUSI JIT VZTAHU, KTERY PREDCHAZEL
										// tyhle relationship atributy vzdycky dostane do hloubky prochazena entita, protoze parent by jich pak musel mit mnoho a neslo by urcit, ke komu patri...
										schema.createInlinedStructuredAttribute(name, codomain.getName(), x.getName());// U TABULKY PRIMO SLOUPCE, U DOKUMENTU, TAKZE PROCESS INLINED... A JSOU TAM JMENA A HODNOTY VSEHO, TY 
										System.out.println("TODO: ADD X TO QUEUE");// TADY ZAROVEN VLOZ DO FRONTY TEN OBJEKT, MUSI Se ZPRACOVAT V DALSIM KROCE!
									}
								}

							}

						}
						// musis se rozhodovat a drzet si v tomto algoritmu, co s tim budes delat...
						// idealne nebo jako zpracuj referenci, pokud je potreba, a to dalsi posli do zasobniku (array)
						// JESTE MUSIS ROZLISOVAT TYPY MODELU - PRO DOKUMENTOVY JE LEPSI TRANSFORMACE CELEHO DOKUMENTU ZAROVEN, ZATIMCO PRO JINE JE LEPSI DELAT TO PO RADCICH...
						// SCHEMA VYTVORIS SNADNO, STACI TI PRUCHOD KATEGORII ... NALITI DAT JE PAK NECO JINEHO. POTREBUJES AGGREGATE ORIENTED PRISTUP (cely agregat najednou) A ENTITY ORIENTED PRISTUP - po radcich
						// LEPSI JE TEN AGGREGATE ORIENTED, PROTOZE TI ZAJISTI, ZE BUDES MIT RIDICI DATA OPRAVDU V TABULKACH - ZAVISLOSTNI STROM SI TIM VYTVORIS

						case INLINED ->
							System.out.println("NEPOUZIVA SE");

						case ATTRIBUTE ->
							schema.createAttribute(name, codomain.getName(), codomain.getDataType(), Cardinality.ONE_TO_ONE);// ZPRACUJ NA MISTE!

						case MULTI_ATTRIBUTE ->
							schema.createAttribute(name, codomain.getName(), codomain.getDataType(), Cardinality.ONE_TO_MANY);//ROZDIL U AGREGATU/GRAFU/TABULKY

						case INLINED_ATTRIBUTE ->
							schema.createAttribute(name, codomain.getName(), codomain.getDataType(), Cardinality.ONE_TO_ONE);// ZPRACUJ NA MISTE!

						case STRUCTURED_ATTRIBUTE -> {
							schema.createStructuredAttribute(name, codomain.getName()); // NESTED DOKUMENT, NESTED TABULKA, JAK MOC MA BYT TAKOVY STURKTUROVANY ATRIBUT ZANOROVANY?
							System.out.println("TODO: ADD CODOMAIN TO QUEUE");// TADY ZAROVEN VLOZ DO FRONTY TEN OBJEKT, MUSI Se ZPRACOVAT V DALSIM KROCE!
						}

						case INLINED_STRUCTURED_ATTRIBUTE -> {
							schema.createInlinedStructuredAttribute(name, codomain.getName());// U TABULKY PRIMO SLOUPCE, U DOKUMENTU, TAKZE PROCESS INLINED... A JSOU TAM JMENA A HODNOTY VSEHO, TY 
							System.out.println("TODO: ADD CODOMAIN TO QUEUE");// TADY ZAROVEN VLOZ DO FRONTY TEN OBJEKT, MUSI Se ZPRACOVAT V DALSIM KROCE!
						}

						case IDENTIFIER ->
							schema.createAttribute(name, codomain.getName(), codomain.getDataType(), Cardinality.ONE_TO_ONE);// ZPRACUJ NA MISTE!
						// IDENTIFIKATORY Z POHLEDU INTEGRITNICH OMEZENI SE BUDOU RESIT POZDEJI, MOZNA TEDY NENI NUTNE ROZLISOVAT ATRIBUTY / IDENTIFIKATORY

						case MULTI_IDENTIFIER ->
							schema.createAttribute(name, codomain.getName(), codomain.getDataType(), Cardinality.ONE_TO_MANY);//ROZDIL U AGREGATU/GRAFU/TABULKY
						// IDENTIFIKATORY Z POHLEDU INTEGRITNICH OMEZENI SE BUDOU RESIT POZDEJI, MOZNA TEDY NENI NUTNE ROZLISOVAT ATRIBUTY / IDENTIFIKATORY

						case REFERENCE ->
							System.out.println("ZPRACUJES POZDEJI");

						case MULTI_REFERENCE ->
							System.out.println("ZPRACUJES POZDEJI");

					}

					//if (morphism.)
				}

			}

			// PRIDEJ INTEGRITNI OMEZENI NEJPRVE PRO REFERENCE - PRI NICH NEKDY JESTE VYTVARIS ATRIBUTY
			// A ZAROVEN POSTAV INTEGRITNI OMEZENI, POKUD SI TO VYZADUJE MODEL
			// TED MAS PRIDANY VSECHNY ATRIBUTY V KIND, TAK VYTVOR INTEGRITNI OMEZENI TYPU PRIMARY KEY
			// over, zda sloupce identifikatoru jsou opravdu typu IDENTIFIER NEBO REFERENCE? PAK MUSIS PRIDAT INTEGRITNI OMEZENI...
		}

	}

}
